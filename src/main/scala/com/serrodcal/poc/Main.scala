package com.serrodcal.poc

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.{ActorMaterializer, Materializer}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import com.serrodcal.poc.service.LoyaltyPointsService
import com.typesafe.config.ConfigFactory
import config.DBAccess
import model.User
import scalikejdbc.AutoSession

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{Failure, Success}

object Main extends App{

    implicit val config = ConfigFactory.load()

    implicit val system: ActorSystem = ActorSystem("loyaltyPoints")
    implicit val materializer: Materializer = ActorMaterializer()

    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    implicit val logger = Logging(system, getClass)

    val dbConfig = DBAccess.pure[IO].initConfig()
    implicit val session = AutoSession

    dbConfig.unsafeRunSync()

    val host = config.getString("server.host")
    val port = config.getString("server.port")

    val userRoute = get{
        path("user" / Segment ) { id =>
            logger.info(s"Received request with id: ${id}")
            val program: IO[User] = new LoyaltyPointsService[IO].getUserPoint(id)
            val resultAsync: Future[User] = program.unsafeToFuture()
            onComplete(resultAsync) {
                case Success(user) => complete(user.toString)
                case Failure(_)        => complete(StatusCodes.NotFound, "User not found")
            }
        }
    }

    val addPointRoute = get{
        ???
    }

    val route: Route = userRoute ~ addPointRoute

    val bindingFuture = Http().bindAndHandle(route, host, port.toInt)

    logger.info(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    logger.info(s"Server stopped :(")
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

}
