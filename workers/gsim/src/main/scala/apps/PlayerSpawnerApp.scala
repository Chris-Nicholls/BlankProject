package apps

import entities.{QuantizedBotDescription, PlayerDescription}
import improbable.flagz.{FlagContainer, FlagInfo, ScalaFlagz}
import improbable.logging.Logger
import improbable.math.Coordinates
import improbable.papi.world.AppWorld
import improbable.papi.world.messaging.{EngineConnected, EngineDisconnected}
import improbable.papi.worldapp.WorldApp
import improbable.unity.fabric.engine.EnginePlatform

import scala.util.Random

class PlayerSpawnerApp(world: AppWorld, logger: Logger) extends WorldApp {

  logger.info("Starting Player app")
  val range = SpawnSettings.botSpawnRange.get().toInt
  (0 until SpawnSettings.botSpawnNumber.get.toInt).foreach{
    i =>
      world.entities.spawnEntity(QuantizedBotDescription.apply(Coordinates((Random.nextDouble()-0.5)*range, 0, (Random.nextDouble()-0.5)*range)))
  }

  def engineConnected(engineConnectedMsg: EngineConnected): Unit = {
    val entityid = world.entities.spawnEntity(PlayerDescription.apply(Coordinates.zero,engineConnectedMsg.engineId))
    logger.info(s"Spawning player $entityid for ${engineConnectedMsg.engineId}")
  }

  def engineDisconnected(engineDisconnectedMsg: EngineDisconnected): Unit = {

  }

  world.messaging.subscribe {
    case engineConnectedMsg: EngineConnected =>
      if (engineConnectedMsg.enginePlatform == EnginePlatform.UNITY_CLIENT_ENGINE) {
        engineConnected(engineConnectedMsg)
      }

    case engineDisconnectedMsg: EngineDisconnected =>
      if (engineDisconnectedMsg.enginePlatform == EnginePlatform.UNITY_CLIENT_ENGINE) {
        engineDisconnected(engineDisconnectedMsg)
      }
  }
}


object SpawnSettings extends FlagContainer {

  @FlagInfo(help="How many bots to spawn", name="bot_spawn_number")
  val botSpawnNumber = ScalaFlagz.valueOf(10)

  @FlagInfo(help="Spawn range of bots", name="bot_spawn_range")
  val botSpawnRange = ScalaFlagz.valueOf(500)

  @FlagInfo(help="Speed of bots", name="bot_speed")
  val botSpeed = ScalaFlagz.valueOf(5)


  @FlagInfo(help="the scale of the quantization", name="quantization_scale")
  val quantizationScale = ScalaFlagz.valueOf(0.3f)
}