package entities

import apps.SpawnSettings
import bot.Waypoint
import improbable.behaviours.{BotDelegations, PlayerDelegations, QuantizedBotDelegations}
import improbable.corelib.util.EntityOwner
import improbable.math.{Coordinates, Vector3f}
import improbable.papi.engine.EngineId
import improbable.papi.entity.{EntityPrefab, EntityRecordTemplate}
import player.{LocalPlayer, PlayerValues}
import position.{QuantizedPosition, SimplePosition}

object PlayerDescription {
  def apply(position: Coordinates, owner: EngineId): EntityRecordTemplate = new EntityDescription {
    val prefab = EntityPrefab("Player")
    val states = Seq(SimplePosition(position), EntityOwner(Some(owner)), PlayerValues(SpawnSettings.botSpeed.get.toInt), LocalPlayer())
    val behaviours = Seq(descriptorOf[PlayerDelegations])
  }.record
}


object BotDescription {
  def apply(position: Coordinates): EntityRecordTemplate = new EntityDescription {
    val prefab = EntityPrefab("Bot")
    val states = Seq(SimplePosition(position), PlayerValues(SpawnSettings.botSpeed.get.toInt), LocalPlayer(), Waypoint(Vector3f(position.x.toFloat, position.z.toFloat, 0)))
    val behaviours = Seq(descriptorOf[BotDelegations])
  }.record
}


object QuantizedBotDescription {
  def apply(position: Coordinates): EntityRecordTemplate = new EntityDescription {
    val scale = SpawnSettings.quantizationScale.get
    val fixedX = (position.x / scale).round.toInt
    val fixedY = (position.z / scale).round.toInt


    val prefab = EntityPrefab("QuantizedBot")
    val states = Seq(SimplePosition(position), QuantizedPosition(fixedX, fixedY, scale), PlayerValues(SpawnSettings.botSpeed.get.toInt), LocalPlayer(), Waypoint(Vector3f(position.x.toFloat, position.z.toFloat, 0)))
    val behaviours = Seq(descriptorOf[QuantizedBotDelegations])
  }.record
}


