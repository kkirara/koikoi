package com.example.model

import io.circe.generic.semiauto._
import io.circe._
import cats.syntax.functor._

sealed  trait GameEvent{
  protected val act: String
}

case class JoinGame(act:"join") extends GameEvent
case class HandCard(act:"hand", card: Card) extends GameEvent
case class TabCard(act:"tab", card: Card) extends GameEvent
case class PlayerDecision(act:"des", text: String) extends GameEvent
case class Leave(act:"leave") extends GameEvent

object GameEvent {

  implicit val decoderJoinGame: Decoder[JoinGame] = deriveDecoder[JoinGame]
  implicit val decoderHandCard: Decoder[HandCard] = deriveDecoder[HandCard]
  implicit val decoderTabCard: Decoder[TabCard] = deriveDecoder[TabCard]
  implicit val decoderDecision: Decoder[PlayerDecision] = deriveDecoder[PlayerDecision]
  implicit val decoderLeavePlayer: Decoder[Leave] = deriveDecoder[Leave]

  implicit val decodeAction: Decoder[GameEvent] =
    List[Decoder[GameEvent]](
      Decoder[JoinGame].widen,
      Decoder[HandCard].widen,
      Decoder[TabCard].widen,
      Decoder[PlayerDecision].widen,
      Decoder[Leave].widen
    ).reduceLeft(_ or _)
}

sealed trait Decision
case object KoiKoi extends Decision
case object Win extends Decision

object Decision{
  def fromString(text: String): Either[String, Decision] = {
    text.trim.toLowerCase match {
      case "koi-koi" | "koi" => Right(KoiKoi)
      case "win" => Right(Win)
      case unknown =>
        val errorMsg = s"Unknown decision $unknown. " +
          "Please pick a valid decision [koi-koi, win]"
        Left(errorMsg)
    }
  }
}
