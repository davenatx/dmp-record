package com.dmp.model

import org.squeryl.Schema

import net.liftweb.squerylrecord.RecordTypeMode._
import net.liftweb.record.field._

/**
 * User: dmp
 * Date: 11/18/11
 * Time: 4:48 PM
 */

object MySchema extends Schema {
  val companies = table[Company]
  val employees = table[Employee]
  val rooms = table[Room]

  val companyToEmployees = oneToManyRelation(companies, employees).via((c, e) => c.id === e.companyId )

  val roomAssignments = manyToManyRelation(employees, rooms).via[RoomAssignment]((employee, room, roomAssignment) =>
    (roomAssignment.employeeId === employee.idField, roomAssignment.roomId === room.idField))

  on(employees)( e =>
    declare(e.companyId defineAs (indexed("idx_employee_companyId")),
      e.email defineAs indexed("idx_employee_email")))

  /**
   * Creates some test instances of companies and employees
   * and saves them in the database.
   */
  def createTestData {
    import TestData._

    allCompanies.foreach(companies.insert(_))
    allEmployees.foreach(employees.insert(_))
    allRooms.foreach(rooms.insert(_))

    e1.rooms.associate(r1)
    e1.rooms.associate(r2)
  }

}


import java.util.Calendar

object TestData {
    val c1 = Company.createRecord.name("First Company USA").
      created(Calendar.getInstance()).
      country(Countries.USA).postCode("12345")
    val c2 = Company.createRecord.name("Second Company USA").
      created(Calendar.getInstance()).
      country(Countries.USA).postCode("54321")
    val c3 = Company.createRecord.name("Company or Employee").
      created(Calendar.getInstance()).
      country(Countries.Canada).postCode("1234")

    val allCompanies = List(c1, c2, c3)

    lazy val e1 = Employee.createRecord.companyId(c1.idField.is).
      name("Peter Example").
      email("peter@example.com").salary(BigDecimal(345)).
      locale(java.util.Locale.GERMAN.toString()).
      timeZone("Europe/Berlin").password("exampletest").
      admin(false).departmentNumber(2).role(EmployeeRole.Programmer).
      photo(Array[Byte](0, 1, 2, 3, 4))

    lazy val e2 = Employee.createRecord.companyId(c2.idField.is).
      name("Company or Employee").
      email("test@example.com").salary(BigDecimal("123.123")).
      locale(java.util.Locale.US.toString()).
      timeZone("America/Los_Angeles").password("test").
      admin(true).departmentNumber(1).role(EmployeeRole.Manager).
      photo(Array[Byte](1))

    lazy val allEmployees = List(e1, e2)

    val r1 = Room.createRecord.name("Room 1")
    val r2 = Room.createRecord.name("Room 2")
    val r3 = Room.createRecord.name("Room 3")

    val allRooms = List(r1, r2, r3)
  }
