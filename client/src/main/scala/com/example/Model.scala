package com.example

import io.circe.{Decoder, Encoder}
import scala.collection.immutable.HashSet

import com.example.Model.Card._
import com.example.Model.CardSet._
import io.circe.syntax._


object Model{

  type Cards = List[Card]

  object CardSet {
    val Hikari: HashSet[Card] = HashSet(CarneAndSun, Curtain, Moon, OnoNoMichikaze, ChinesePhoenix)
    val Animal: HashSet[Card] = HashSet(BushWarbler, Cuckoo, EightPlankBridge, Cho, Ino,  Geese, Sake, Shika,  Swallow)
    val PoeticTanzaku: HashSet[Card] = HashSet(Card(1, 2), Card(2, 2), Card(3, 2))
    val BlueTanzaku: HashSet[Card] = HashSet(Card(6, 2), Card(9, 2), Card(10, 2))
    val RedTanzaku: HashSet[Card] = HashSet(Card(4, 2), Card(5, 2), Card(7, 2), Card(11, 3))
    val InoShikaCho: HashSet[Card] = HashSet(Ino, Shika, Cho)
    val AllTanzaku: HashSet[Card] = PoeticTanzaku ++ BlueTanzaku ++ RedTanzaku
    val SpecCards: HashSet[Card] = Hikari ++ Animal ++ AllTanzaku
  }
  case class Card(suit: Int, rank: Int){
    def isHikari: Boolean = Hikari.contains(this)
    def isAnimal: Boolean = Animal.contains(this)
    def isSimple: Boolean = this == Sake || (!isHikari & !isAnimal & !isTanzaku)
    def isTanzaku: Boolean = AllTanzaku.contains(this)
    def isBlueTanzaku: Boolean = BlueTanzaku.contains(this)
    def isRedTanzaku: Boolean = RedTanzaku.contains(this)
    def isPoeticTanzaku: Boolean = PoeticTanzaku.contains(this)
    require(suit >= 0 && suit <= 12, s"Invalid card suit: $suit")
    require(rank >= 0 && rank <= 4, s"Invalid card number: $rank")
    def getName = f"${suit}%02d${rank}"
  }

  object Card {
    implicit val decoderCard: Decoder[Card] = Decoder.decodeString.emap(str => Card.apply(str))
    implicit val encoderCard: Encoder[Card] = Encoder.encodeString.contramap[Card](_.getName)

    object CardBack extends Card(suit = 0, rank = 0)
    object CarneAndSun extends Card(suit = 1, rank = 1)
    object Curtain extends Card(suit = 3, rank = 1)
    object Moon extends Card(suit = 8, rank = 1)
    object OnoNoMichikaze extends Card(suit = 11, rank = 1)
    object ChinesePhoenix extends Card(suit = 12, rank = 1)

    object BushWarbler extends Card(suit = 2, rank = 1)
    object Cuckoo extends Card(suit = 4, rank = 1)
    object EightPlankBridge extends Card(suit = 5, rank = 1)
    object Cho extends Card(suit = 6, rank = 1)
    object Ino extends Card(suit = 7, rank = 1)
    object Geese extends Card(suit = 8, rank = 2)
    object Sake extends Card(suit = 9, rank = 1)
    object Shika extends Card(suit = 10, rank = 1)
    object Swallow extends Card(suit = 11, rank = 2)

    object WildCard extends Card(suit = 11, rank = 4)

    val fullDeck: List[Card] = {
      for {
        suit <- 1 to 12
        rank <- 1 to 4
      } yield Card(suit, rank)
    }.toList

    private val pattern = "^([0-1][0-9])\\s*([0-4])$".r

    def apply(text: String): Either[String, Card] = text match {
      case "000" => Right(Card(0,0))
      case pattern(suit, rank) => {
        val card = Card(suit = suit.toInt, rank = rank.toInt)
        Right(card)
      }
      case _ => Left(s"Invalid card string ${text}")
    }

  }

  sealed trait GameStatus

  case object WaitOpponent extends GameStatus
  case object StartGame extends GameStatus
  case object ChooseCardHand extends GameStatus
  case object WaitTabCardForHand extends GameStatus
  case object DeckCard extends GameStatus
  case object WaitTabCardForDesk extends GameStatus
  case object CalcScore extends GameStatus
  case object WaitDecision extends GameStatus
  case object Winner extends GameStatus
  case object EmptyDeck extends GameStatus
  case object Finish extends GameStatus
  case object End extends GameStatus
  case object LeaveGame extends GameStatus


  case class CardStack(cards: Cards)
  case class ShowPlayer(turn: Boolean, hand: CardStack, openHand: CardStack, scoreTotal: Int, score: Int)
  case class ShowInfo(status: GameStatus, round: Int, tab: CardStack,
                       my: ShowPlayer,
                       opponent: ShowPlayer,
                       card: Option[Card])

  object ShowInfo{
    def empty: ShowInfo = ShowInfo(status = WaitOpponent, round = 0, tab = CardStack(Nil),
      my = ShowPlayer(turn = false, hand = CardStack(Nil), openHand = CardStack(Nil), scoreTotal = 0, score = 0),
      opponent = ShowPlayer(turn = false, hand = CardStack(Nil), openHand = CardStack(Nil), scoreTotal = 0, score = 0),
      card = None)
  }

  import io.circe.generic.semiauto._
  sealed  trait GameEvent{
    protected val act: String
  }

  case class JoinGame(act:String = "join") extends GameEvent
  case class HandCard(act:String ="hand", card: Card) extends GameEvent
  case class TabCard(act:String ="tab", card: Card) extends GameEvent
  case class PlayerDecision(act:String ="des", text: String) extends GameEvent
  case class Leave(act:String ="leave") extends GameEvent

  object GameEvent {

    implicit val encodeJoinGame:Encoder[JoinGame] = deriveEncoder[JoinGame]
    implicit val encodeHandCard:Encoder[HandCard] = deriveEncoder[HandCard]
    implicit val encodeTabCard:Encoder[TabCard] = deriveEncoder[TabCard]
    implicit val encodeDecision:Encoder[PlayerDecision] = deriveEncoder[PlayerDecision]
    implicit val encodeLeavePlayer:Encoder[Leave] = deriveEncoder[Leave]

    implicit val encodeAction:Encoder[GameEvent] = Encoder.instance {
      case join: JoinGame => join.asJson
      case leave: Leave => leave.asJson
      case des: PlayerDecision => des.asJson
      case tab: TabCard => tab.asJson
      case hand: HandCard => hand.asJson
    }
  }
}

