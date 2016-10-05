package entities

import improbable.papi.entity.{EntityBehaviour, EntityPrefab, EntityRecordTemplate}
import improbable.papi.entity.behaviour.EntityBehaviourDescriptor
import improbable.papi.entity.state.EntityStateDescriptor

import scala.reflect.{ClassTag, _}


trait EntityDescription{

  val behaviours: Seq[EntityBehaviourDescriptor]
  val states: Seq[EntityStateDescriptor]

  val prefab :EntityPrefab

  def record : EntityRecordTemplate = EntityRecordTemplate(prefab, behaviours, states)

  def descriptorOf[T <: EntityBehaviour : ClassTag]: EntityBehaviourDescriptor = {
    improbable.papi.entity.behaviour.EntityBehaviourConverter.behaviourToDescriptor(classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }
}


object EntityDescription {

  implicit def entityDescriptionToRecordTemplate(description: EntityDescription): EntityRecordTemplate = description.record

}