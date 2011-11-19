package com.dmp.model

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord

import org.squeryl.dsl.CompositeKey2
import org.squeryl.KeyedEntity

/**
 * User: dmp
 * Date: 11/18/11
 * Time: 7:42 PM
 */

class Room private() extends Record[Room] with KeyedRecord[Long] {

  override def meta = Room

  override val idField = new LongField(this)

  val name = new StringField(this, 50)

  lazy val employees = MySchema.roomAssignments.right(this)

}

object Room extends Room with MetaRecord[Room]

class RoomAssignment(val employeeId: Long, val roomId: Long) extends KeyedEntity[CompositeKey2[Long,Long]] {
  def id = compositeKey(employeeId, roomId)
}