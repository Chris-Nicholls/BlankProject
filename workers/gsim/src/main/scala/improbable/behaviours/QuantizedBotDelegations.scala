package improbable.behaviours

import bot.Waypoint
import improbable.math.Coordinates
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.papi.world.World
import improbable.unity.fabric.{PhysicsEngineConstraint, VisualEngineConstraint}
import player.LocalPlayer
import position.{QuantizedPosition, SimplePositionWriter}

import scala.concurrent.duration._

class QuantizedBotDelegations(entity: Entity, world: World, position: SimplePositionWriter) extends EntityBehaviour {
  entity.delegateState[QuantizedPosition](PhysicsEngineConstraint)
  entity.delegateState[LocalPlayer](PhysicsEngineConstraint)
  entity.delegateState[Waypoint](PhysicsEngineConstraint)
  entity.addEngineConstraint(PhysicsEngineConstraint)
  entity.addEngineConstraint(VisualEngineConstraint)

  val quantizedPosition = entity.watch[QuantizedPosition]
  world.timing.every(1.second) {
    val x = quantizedPosition.x.get
    val y = quantizedPosition.y.get
    val scale = quantizedPosition.scale.get
    position.update.position(Coordinates(x * scale, 0, y * scale)).finishAndSend()
  }
}
