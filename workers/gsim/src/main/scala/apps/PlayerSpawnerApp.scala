package apps

import entities.PlayerDescription
import improbable.logging.Logger
import improbable.math.Coordinates
import improbable.papi.world.AppWorld
import improbable.papi.world.messaging.{EngineConnected, EngineDisconnected}
import improbable.papi.worldapp.WorldApp
import improbable.unity.fabric.engine.EnginePlatform

class PlayerSpawnerApp(world: AppWorld, logger: Logger) extends WorldApp {

  logger.info("Starting Player app")

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
