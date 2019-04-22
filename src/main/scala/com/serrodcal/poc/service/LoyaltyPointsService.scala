package com.serrodcal.poc.service

import cats.effect.Sync
import cats.syntax.all._
import com.serrodcal.poc.exception.UserNotFound
import com.serrodcal.poc.model.User
import com.serrodcal.poc.repository.UserRepository

class LoyaltyPointsService[F[_]: Sync: UserRepository] {

  def getUserPoint(id: String): F[User] = {
    (for{
      maybeUser <- UserRepository[F].findUserById(id)
      user <- maybeUser.fold[F[User]](Sync[F].raiseError(UserNotFound))(Sync[F].pure)
    }yield user).handleErrorWith { error =>
      Sync[F].delay(println(s"Error when getUserPoint with id $id")).*>(Sync[F].raiseError(error))
    }
  }

  def addPoints(id: String, pointsToAdd: Int): F[Unit] = ???

}
