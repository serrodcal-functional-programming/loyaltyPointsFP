package com.serrodcal.poc.repository

import cats.effect.Sync
import com.serrodcal.poc.model.User
import scalikejdbc._

trait UserRepository[F[_]] {
  def findUserById(id: String): F[Option[User]]
  def updateLoyaltyPointsUser(id: String, points: Int): F[Unit]
}

object UserRepository {

  def apply[F[_]](implicit ev: UserRepository[F]) = ev

  implicit def inMemory[F[_]: Sync](implicit session: DBSession) = new UserRepository[F] {

    override def findUserById(id: String): F[Option[User]] = Sync[F].delay(
      withSQL {
        select.from(User as User.u).where.eq(User.u.id, id)
      }.map(rs => User(rs)).single().apply())

    override def updateLoyaltyPointsUser(id: String, points: Int): F[Unit] = Sync[F].delay(
      withSQL{
        update(User as User.u).set(User.u.loyaltyPoints -> points).where.eq(User.u.id, id)
      }.update().apply()
    )

  }

}
