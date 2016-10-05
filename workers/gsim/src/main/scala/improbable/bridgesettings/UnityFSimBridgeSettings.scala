package improbable.bridgesettings

import improbable.fapi.bridge._
import improbable.fapi.network.RakNetLinkSettings
import improbable.serialization.KryoSerializable
import improbable.unity.fabric.bridge.FSimAssetContextDiscriminator
import improbable.unity.fabric.engine.EnginePlatform
import improbable.unity.fabric.satisfiers.SatisfyPhysics

object UnityFSimBridgeSettings extends BridgeSettingsResolver {

  private val fSimEngineBridgeSettings = BridgeSettings(
    FSimAssetContextDiscriminator(),
    RakNetLinkSettings(),
    EnginePlatform.UNITY_FSIM_ENGINE,
    SatisfyPhysics,
    RangedAuthority(100),
    MetricsEngineLoadPolicy,
    PerEntityOrderedStateUpdateQos
  )

  override def engineTypeToBridgeSettings(engineType: String, metadata: String): Option[BridgeSettings] = {
    if (engineType == EnginePlatform.UNITY_FSIM_ENGINE) {
      Some(fSimEngineBridgeSettings)
    } else {
      None
    }
  }
}
case class RangedAuthority(range: Double) extends EntityInterestPolicy with KryoSerializable {

  private val defaultInterestType = Some(RangedInterest(range))

  override def interestTypeFor(entity: EngineEntity): Option[InterestType] = {
    if (entity.isAuthoritativeOnEngine) {
      defaultInterestType
    } else {
      None
    }
  }
}

