package entities

import improbable.behaviours.PlayerDelegations
import improbable.corelib.util.EntityOwner
import improbable.math.Coordinates
import improbable.papi.engine.EngineId
import improbable.papi.entity.{EntityPrefab, EntityRecordTemplate}
import player.{LocalPlayer, PlayerValues}
import position.SimplePosition

object PlayerDescription {
  def apply(position: Coordinates, owner: EngineId): EntityRecordTemplate = new EntityDescription {
    val prefab = EntityPrefab("Player")
    val states = Seq(SimplePosition(position), EntityOwner(Some(owner)), PlayerValues(8), LocalPlayer())
    val behaviours = Seq(descriptorOf[PlayerDelegations])
  }.record
}



