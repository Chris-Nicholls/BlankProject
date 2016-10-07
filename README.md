# SpatialOS Optimisation Tutorial
---

This is a small tutorial to show some examples of how you might optimise your SpatialOS game.

## Introduction

We will start with a very simple implementation and build up some optimisations from there.
This is not intended to be a complete solution, but will hopefully give you some ideas that you can take and use in your own project.

For most project, the vast majority of the component syncronisation tends to comes from position (and possibly rotation) updates.
Although there are many other types of components that get syncronised, they position tends to be updated far more frequently and is therefore the most important thing to optimise to keep bandwidth and CPU usage low.



We will start with the simplest possible implementation of position syncronisation.

```
component SimplePosition {
	id = 1001;
	EntityPosition position = 1;
}
```

This schema file simply defines a component that contains one property `position`.
We give it the type `EntityPosition` which is a special type that tells SpatialOS to take whatever value is in this property to be the global position of the entity.
By updating this componment, you can move entities around the simulation.
Under the hood, it uses a Coordinate type with x, y and z components stored as doubles.

In Unity, we can simply read the position of our game object and update the `SimplePosition` component.
We are using Unity in 2D mode so there is some bookkeeping to convert the x/y coordinates to x/z.
This code will run on whichever worker has write authority on the SimplePosition component on an entity (e.g. on the player entity on the player's client).

```csharp
public class PositionReporter2D : MonoBehaviour
{
    [Require] private SimplePosition.Writer Position;

    private SimplePosition.Update _positionUpdate = new SimplePosition.Update();
    private Coordinates _playerCoordinates;

    public void FixedUpdate()
    {
        var position = transform.position;
        _playerCoordinates.X = position.x;
        _playerCoordinates.Z = position.y; // Swap z & y since unity 2d uses x/y and spatial uses x/z
        _positionUpdate.SetPosition(_playerCoordinates);
        Position.Send(_positionUpdate);
    }
}
```



We can read the component updates from entities that the worker is does not have authority over (e.g. other player's player entities) and pass those to the Unity game object.

```csharp
class PositionVisualizer2D : MonoBehaviour
    {
        [Require] private SimplePosition.Reader Position;

        private Vector3 _position;

        public void OnEnable()
        {
            SetPosition();
        }

        private void SetPosition()
        {
            _position.x = (float) Position.Data.position.X;
            _position.y = (float) Position.Data.position.Z;
            transform.position = _position;
        }

        public void FixedUpdate()
        {
            if (!Position.HasAuthority)
            {
                SetPosition();
            }
        }
    }
```

This is enough to get us started.
We can move entities around, and see other entities move too.
Nice! But it is very wasteful.
We can do much better.
Before we start looking at how we can improve things, we better have some metrics to compare against.
To do this we're going to spin up a simulation with 10,000 simple entities moving around sending state updates.
While this simulation is running, we'll collect various metrics we can use to see if our optimisations are working.


To keep variation between runs as low as possible, we'll run our 10,000 bot simulations with a fixed topology: Two machines, one running 25 instances of Unity in a fixed grid, and the other master server running everything else.
This will end up being overkill, but we want to be able to see the effects of our optimisation as clearly as possible.


The resuts:

| Metric | No Optimisation |
|---------------------------------|:------:|
| Worker to bridge Messages | 200,000 messages/s|
| Worker to Bridge Network | 8.4 MB/s |
| Average Message Size      | 42 B         |
| Bandwidth per entity | 840 B/s |
| CPU usage (Master) | 26 s/s |
| CPU usage (Workers) |  19 s/s |
| Worker Load average | 0.53 |


So first up, do these number make sense?
Well, we have 10,000 entities each sending a sate update inside unity's fixed update loop, which we set to tick at 20hz, so 200,000 messages a second is what we expect.
With 8.4 MB/s bandwidth being used, this gives us 42B per message, and 0.8kB/s per entity.
Again, this is in line with our expectations, since the majority of messages contain three doubles, plus one one variable length int to identify which entity the message is for and one variable length int to identify which component is being updated.

***
##### An Aside on Bandwidth

8.4 MB/s doesn't sound like too much, especially given that this is between two servers in the same datacenter, but if each entity requires 0.8 kB/s to sync, then we severely limit the number of entities each client can check out.
So even though we could handle much higher bandwidth on the server side, we want to keep it as low as possible for the client workers.

***

### Making things better

There are a lot of ways to improve things, but they essentially fall into two camps: Send smaller updates, or send them less often.

### Sending smaller updates

We'll look at sending smaller updates first, since it's slightly simpler to start with.
We are currently using the SpatialOS `Coordinate` type for position.
This is convenient, because SpatialOS uses the coordinate type to determine where your entity is, but is rather wasteful for our application.
We can improve in two ways:
* Our game is 2d, so we don't need a 3rd dimension
* Doubles are overkill for the accuracy we need.

So a first approach to try might be to represent our position as two integer coordinates representing our (x,y) position in units of 10cm.

```
component QuantizedPosition {
  id = 1005;
  sint32 x = 1;
  sint32 y = 2;
  float scale = 3;
}
```

The visualisation of this new position representation can be done fairly simply:

```csharp
class QuantizedPositionVisualizer : MonoBehaviour
{
		[Require]
		private QuantizedPosition.Reader Position;

		private Vector3 _position;

		public void OnEnable()
		{
				SetPosition();
		}

		private void SetPosition()
		{
				_position.x = (Position.Data.x) * Position.Data.scale;
				_position.y = Position.Data.y * Position.Data.scale;
				transform.position = _position;
		}

		public void Update()
		{
				if (!Position.HasAuthority)
				{
						SetPosition();
				}
		}
}
```

And the reporting of position requires even less code:
```csharp    
public class QuantizedPositionReporter : MonoBehaviour
{
    [Require] private QuantizedPosition.Writer Position;

    private QuantizedPosition.Update _positionUpdate = new QuantizedPosition.Update();

    public void FixedUpdate()
    {
        var position = transform.position;
        _positionUpdate.x = (int) (position.x / Position.Data.scale);
        _positionUpdate.y = (int) (position.y / Position.Data.scale);
        Position.Send(_positionUpdate);
    }
}
```

There is one other thing that we need to do before we can test this code.
Because we are not using the EntityPosition type anymore, SpatialOS does not know how to extract the entities position from the QuantizedPosition component.
We have to translate it explicitly.
This isn't too hard to do, but if we forget, then our entities will move about on each worker, but not in the global simulation.

We add the following code to a behaviour running on the GSim:

```
val quantizedPosition = entity.watch[QuantizedPosition]
world.timing.every(1.second){
	val x = quantizedPosition.x.get
	val y = quantizedPosition.y.get
	val scale = quantizedPosition.scale.get
	simplePosition.update.position(Coordinates(x*scale, 0, y*scale)).finishAndSend()
}
```

With this in place, we are good to go and see what effect this has had on our metrics.

| Metric                    | No Optimisation | Fixed Point |
|---------------------------|:------------:|:---------------|
| Worker to bridge Messages | 200,000 msg/s| 200,000 msg/s  |
| Worker to Bridge Network  | 8.4 MB/s     | 4.1 MB/s       |
| Average Message Size      | 42 B         | 20.6 B         |   
| Bandwidth per entity      | 840 B/s      | 413 B/s        |
| CPU usage (Master)        | 26 s/s       | 23 s/s         |
| CPU usage (Workers)       | 19 s/s       | 19 s/s         |
| Worker Load average       | 0.53         | 0.51           |


So with this optimisation, we have roughly cut our bandwidth requirements in half.
Interestingly, the cpu load of SpatialOS itself has also dropped slightly.
This might be down to the fact that we are processing less data, but I suspect it is just noise sice the load on the workers hasn't really dropped.

### Sending fewer updates

This is a good start, but there is now another easy optimisation open to us.
We are still sending updates on every FixedUpdate tick, even if the state has not actually changed.
Simply checking this before we send the update we can avoid sending an update if nothing has changed, or we can just send the x value, if the y value is the same as before.

Our position reporting now looks like this:
```chsarp
public class QuantizedPositionReporter : MonoBehaviour
{
	 [Require] private QuantizedPosition.Writer Position;

	 private QuantizedPosition.Update _positionUpdate = new QuantizedPosition.Update();

	 public void FixedUpdate()
	 {
			 var position = transform.position;
			 var newX = (int) (position.x / Position.Data.scale);
			 var newY = (int) (position.y / Position.Data.scale);

			 if (newX != Position.Data.x)
			 {
					 _positionUpdate.x = newX;
			 }
			 else
			 {
					 _positionUpdate.x = null;
			 }

			 if (newY != Position.Data.y)
			 {
					 _positionUpdate.y = newY;
			 }
			 else
			 {
					 _positionUpdate.y = null;
			 }

			 if (_positionUpdate.x != null || _positionUpdate.y != null)
			 {
					 Position.Send(_positionUpdate);
			 }
	 }
}
```

This change turns out to have a relatively small effect.

| Metric                    | No Optimisation | Quantized | Quantized No Duplication  |
|---------------------------|:------------:|:---------------|-------------------------|
| Worker to bridge Messages | 200k msg/s   | 200k msg/s     | 200k msg/s
| Worker to Bridge Network  | 8.4 MB/s     | 4.1 MB/s       | 3.1 MB/s
| Average Message Size      | 42 B         | 20.6 B         | 15.4 B
| Bandwidth per entity      | 840 B/s      | 413 B/s        | 308 B/s
| CPU usage (Master)        | 26 s/s       | 23 s/s         | 25 s/s
| CPU usage (Workers)       | 19 s/s       | 19 s/s         | 18 s/s
| Worker Load average       | 0.53         | 0.51           | 0.44

In particular, it doesn't really seem to cut down on the number of messages we actually send!
The reason for this is to do with how entities move and the level of quantization.
Suppose we quantize to the nearest 20cm, and our entities move at a speed of 5 m/s.
Our fixed update loop is reporting changes at 20hz, which means our entities move 25cm per tick an need updating anyway.
We could quantize at bigger intervals of course, if we are willing to sacrifice even more precision.
Whether this is acceptable or not really depends on the particular game or simulation and the type of entity that is moving.

So what happens if we up the quantization range to 1 meters, with our bots still moving at 5 m/s?
This is probably far to coarse to use in a real game, but should give us an idea of what we can achieve.


| Metric                    | No Optimisation | Quantized (20 cm) | Quantization (1m)
|---------------------------|:------------:|:---------------|----------------|
| Worker to bridge Messages | 200k msg/s   | 200k msg/s     | 63.4k msg/s
| Worker to Bridge Network  | 8.4 MB/s     | 3.1 MB/s       | 1.0 MB/s
| Average Message Size      | 42 B         | 15.4 B         | 15.7 B
| Bandwidth per entity      | 840 B/s      | 308 B/s        | 102 B/s
| CPU usage (Master)        | 26 s/s       | 25 s/s         | 15 s/s
| CPU usage (Workers)       | 19 s/s       | 18 s/s         | 8 s/s
| Worker Load average       | 0.53         | 0.44           | 0.13

Much better!
If you don't need much precision, then you can do much better than the first implementation.
