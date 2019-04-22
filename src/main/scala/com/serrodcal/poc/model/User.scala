package com.serrodcal.poc.model

import scalikejdbc._

case class User(id: String, loyaltyPoints: Int)

object User extends SQLSyntaxSupport[User] {

  val u = User.syntax("u")

  override val tableName = "users"

  def apply(rs: WrappedResultSet) = new User(rs.string("id"), rs.int("loyaltyPoints"))

}
