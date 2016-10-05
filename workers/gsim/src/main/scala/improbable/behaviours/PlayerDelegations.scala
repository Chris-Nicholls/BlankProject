package improbable.behaviours

import improbable.corelib.util.EntityOwner
import improbable.corelib.util.EntityOwnerDelegation._
import improbable.papi.entity.{Entity, EntityBehaviour}
import improbable.unity.fabric.VisualEngineConstraint
import improbable.unity.papi.SpecificEngineConstraint
import player.LocalPlayer
import position.SimplePosition

class PlayerDelegations(entity: Entity) extends EntityBehaviour {
  entity.delegateStateToOwner[SimplePosition]
  entity.delegateStateToOwner[LocalPlayer]
  entity.addEngineConstraint(SpecificEngineConstraint(entity.watch[EntityOwner].ownerId.get.get))
  entity.addEngineConstraint(VisualEngineConstraint)
}
