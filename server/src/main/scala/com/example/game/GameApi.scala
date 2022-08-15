package com.example.game

import cats.effect.{ExitCode, IO, IOApp}
import com.example.model._
import fs2.Pipe
import fs2.concurrent.Queue
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.ExecutionContext
import io.circe.parser.decode
import org.http4s.websocket.WebSocketFrame.{Close, Text}


object GameApp extends IOApp {

  // Let's build a WebSocket server using Http4s.

  private def echoRoute(gameService: GameService[IO]) = HttpRoutes.of[IO] {
    case POST -> Root / "game" / gameID / playerName / round =>
      val player: Player = Player(name = playerName)
      val gameId: String = gameID
      for {
        game <- gameService.add(gameId, player, round.toInt)
        response <- if (game.existsPlayer(player)) Ok(gameId)
        else BadRequest("Game is busy")
      } yield response


    case DELETE -> Root / "game" / gameID =>
      val gameId: String = gameID
      for {
        _ <- gameService.delete(gameId)
        response <- Ok("Game delete")
      } yield response

    //websocat "ws://localhost:9002/game/x/y"
    case req@GET -> Root / "game" / gameID / playerName / round =>
      //val player = req.cookies.find(_.name == "player" ).flatMap(playerName => Some(Player(playerName.content))).get
      //val gameId = req.cookies.find(_.name == "ID" ).flatMap(gameCook => Some(gameCook.content)).get

      def echoGame(gameId: String, player: Player): Pipe[IO, WebSocketFrame, WebSocketFrame] =
        _.evalMap {
          case Close(_) =>
            for {
              _ <- gameService.delete(gameId)
              response <- IO.pure(Text("Game delete"))
            } yield response

          case Text(text, _) => decode[GameEvent](text) match {
            case Left(err) => IO.pure(Text(err.getMessage()))
            case Right(event) => gameService.makeEvent(gameId, player, event)
          }

          case _ => IO.pure(Text("unsupported"))

        }

      val player: Player = Player(playerName)
      val gameId = gameID
      println(s"${player} ${gameId}")

      for {
        gameOpt <- gameService.get(gameId)
        game <- gameOpt match {
          case None => gameService.add(gameId, player, round.toInt)
          case Some(g) => IO(g)
        }
        _ <- IO(println(game.gameStatus))
        answer <- if (game.existsPlayer(player) || game.gameStatus == WaitOpponent) for {
          gameInfo <- gameService.getInfo(gameId, player)
          queue <- Queue.unbounded[IO, WebSocketFrame]
          response <- WebSocketBuilder[IO].build(
            receive = queue.enqueue,
            send = queue.dequeue.through(echoGame(gameId, player)) merge gameInfo,
          )
        } yield response
        else BadRequest("Game is busy, turn other name")
      } yield answer
  }

  private def httpApp(gameService: GameService[IO]): HttpApp[IO] = {
    echoRoute(gameService)
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    for {
      gameService <- GameService.of[IO]
      _ <- BlazeServerBuilder[IO](ExecutionContext.global)
        //.bindHttp(port = 9002, host = "localhost")
        .bindHttp(port = 9002, host = "192.168.1.68")
        .withHttpApp(httpApp(gameService))
        .serve
        .compile
        .drain
    } yield ExitCode.Success
}
