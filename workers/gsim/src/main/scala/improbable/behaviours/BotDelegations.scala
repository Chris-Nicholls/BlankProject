package improbable.behaviours

import bot.Waypoint
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.unity.fabric.{PhysicsEngineConstraint, VisualEngineConstraint}
import player.LocalPlayer
import position.SimplePosition

class BotDelegations(entity: Entity) extends EntityBehaviour {
  entity.delegateState[SimplePosition](PhysicsEngineConstraint)
  entity.delegateState[LocalPlayer](PhysicsEngineConstraint)
  entity.delegateState[Waypoint](PhysicsEngineConstraint)
  entity.addEngineConstraint(PhysicsEngineConstraint)
  entity.addEngineConstraint(VisualEngineConstraint)
}
