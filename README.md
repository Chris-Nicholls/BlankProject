# SpatialOS Optimisation Tutorial
---

This is a small tutorial to show some examples of how you might optimise your SpatialOS game.

## Introduction

We will start with a very simple implementation and build up some optimisations from there. This is not intended to be a complete solution, but will hopefully give you some ideas that you can take and use in your own project.

For most project, the vast majority of the component syncronisation tends to comes from position (and possibly rotation) updates. Although there are many other types of components that get syncronised, they position tends to be updated far more frequently and is therefore the most important thing to optimise to keep bandwidth and CPU usage low.

We will start with the simplest possible implementation of position syncronisation.

```
component SimplePosition {
	id = 1001;
	EntityPosition position = 1;
}
```

This schema file simply defines a component that contains one property `position`. We give it the type `EntityPosition` which is a special type that tells SpatialOS to take whatever value is in this property to be the global position of the entity. By updating this componment, you can move entities around the simulation. Under the hood, it uses a Coordinate type with x, y and z components stored as doubles.

In Unity, we can simply read the position of our game object and update the `SimplePosition` component. We are using Unity in 2D mode so there is some bookkeeping to convert the x/y coordinates to x/z. This code will run on whichever worker has write authority on the SimplePosition component on an entity (e.g. on the player entity on the player's client).

```csharp
public class PositionReporter2D : MonoBehaviour
{
    [Require] private SimplePosition.Writer Position;

    private SimplePosition.Update _positionUpdate = new SimplePosition.Update();
    private Coordinates _playerCoordinates;

    public void Update()
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

        public void Update()
        {
            if (!Position.HasAuthority)
            {
                SetPosition();
            }
        }
    }
```

This is enough to get us started. We can move entities around, and see other entities move too. Nice! But it is very wasteful. We can do much better. Before we start looking at how we can improve things, we better have some metrics to compare against. To do this we're going to spin up a simulation with 10,000 simple entities moving around sending state updates. While this simulation is running, we'll collect various metrics we can use to see if our optimisations are working. 
