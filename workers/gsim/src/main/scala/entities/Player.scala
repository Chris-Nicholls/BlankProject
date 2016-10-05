package entities

import bot.Waypoint
import improbable.behaviours.{BotDelegations, PlayerDelegations}
import improbable.corelib.util.EntityOwner
import improbable.math.{Coordinates, Vector3f}
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


object BotDescription {
  def apply(position: Coordinates): EntityRecordTemplate = new EntityDescription {
    val prefab = EntityPrefab("Bot")
    val states = Seq(SimplePosition(position), PlayerValues(8), LocalPlayer(), Waypoint(Vector3f(position.x.toFloat, position.z.toFloat, 0)))
    val behaviours = Seq(descriptorOf[BotDelegations])
  }.record
}



