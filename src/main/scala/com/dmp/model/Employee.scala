package com.dmp.model

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.record.field._
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.annotations.Column

import java.math.MathContext

/**
 * User: dmp
 * Date: 11/18/11
 * Time: 7:36 PM
 */

class Employee private () extends Record[Employee] with KeyedRecord[Long] {

  override def meta = Employee

  @Column(name = "id")
  override val idField = new LongField(this)

  val name = new StringField(this, "")
  val companyId = new LongField(this)
  val email = new EmailField(this, 100)
  val salary = new DecimalField(this, MathContext.UNLIMITED, 2)
  val locale = new LocaleField(this)
  val timeZone = new TimeZoneField(this)
  val password = new PasswordField(this)
  val photo = new OptionalBinaryField(this)
  val admin = new BooleanField(this)
  val departmentNumber = new IntField(this)
  val role = new EnumNameField(this, EmployeeRole)

  lazy val company = MySchema.companyToEmployees.right(this)
  lazy val rooms = MySchema.roomAssignments.left(this)

}

object Employee extends Employee with MetaRecord[Employee]

object EmployeeRole extends Enumeration {
  type EmployeeRole = Value

  val Programmer, Manager = Value
}