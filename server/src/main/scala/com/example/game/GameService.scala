package com.example.game

import cats.Functor
import cats.effect.concurrent.Ref
import cats.effect.{Sync, Timer}
import cats.syntax.all._
import com.example
import com.example.model._
import fs2.Stream
import org.http4s.websocket.WebSocketFrame

import scala.concurrent.duration.DurationInt
import org.http4s.websocket.WebSocketFrame.Text


trait GameService[F[_]] {

  def add(gameId: String, player: Player, round: Int): F[Game]

  def get(gameId: String): F[Option[Game]]

  def delete(gameId: String): F[Unit]

  def joinGame(gameId: String, player: Player): F[Either[String, String]]

  def makeEvent(gameId: String, player: Player, event: GameEvent): F[Text]

  def getInfo(gameId: String, player: Player): F[Stream[F, WebSocketFrame.Text]]
}


object GameService {

  sealed trait CartError

  object CartError {
    case object CurrencyMismatch extends CartError
  }

  def of[F[_] : Sync : Timer]: F[GameService[F]] =
    Ref.of(Map.empty[String, Game]).map(apply(_))

  def apply[F[_] : Functor : Sync : Timer](
                                            state: Ref[F, Map[String, Game]]
                                          ): GameService[F] =
    new GameService[F] {
      def add(gameId: String, player: Player, round: Int = 12): F[Game] =
        state.modify { games =>
          val game = games.getOrElse(gameId, Game(player, round))
          (games + (gameId -> game), game)
        }

      def get(gameId: String): F[Option[Game]] =
        state.get.map { games =>
          games.get(gameId).map { v => v }
        }

      def delete(gameId: String): F[Unit] =
        state.update { games => games.removed(gameId) }

      def joinGame(gameId: String, player: Player): F[Either[String, String]] =
        state.modify { games =>
          val gameOrError = games.get(gameId).map(_.joinGame(player)).getOrElse(Left("Error gameId"))
          gameOrError match {
            case Left(msg) => (games, msg.asLeft)
            case Right(game) => (games + (gameId -> game), "Ok".asRight)
          }
        }

      def makeEvent(gameId: String, player: Player, event: GameEvent): F[Text] =
        state.modify { games =>
          val gameOrError = games.get(gameId).map(_.makeEvent(player, event)).getOrElse(Left("Error gameId"))
          gameOrError match {
            case Left(msg) => (games, Text(msg))
            case Right(game) => (games + (gameId -> game), Text("Ok"))
          }
        }

      def getInfo(gameId: String, player: Player): F[Stream[F, WebSocketFrame.Text]] = {
        for {
          gs <- get(gameId)
          prevState <- Ref.of[F, Option[Game]](gs)
        } yield Stream.repeatEval(
          for {
            games <- state.get
            answer <- prevState.modify { prevGame =>
              games.get(gameId).collect {
                case game if game.some != prevGame =>
                  game.getInfo(player)
              } match {
                case Some(value) => (games.get(gameId), value)
                case None => (prevGame, None)
              }
            }
          } yield answer
        ).collect { case Some(json) => Text(json) }.metered(100.millisecond).repeat
      }
    }
}