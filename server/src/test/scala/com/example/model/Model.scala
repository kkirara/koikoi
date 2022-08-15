//package com.example.model
//
//object CardPlayerV2Main extends App{
//  import com.example.model.Cards._
//  // var (player1, player2, d, tab) = Game.startGame(Player(name = "player1"), Player(name = "player1"))
//
//  def matchTab(card: Card, tab: CardStack, openHand: CardStack): (CardStack, CardStack) = {
//    val matchCards = tab.getMatchCards(card)
//    val (newTab, newOpenHand) = matchCards.size match {
//      case 0 => (tab.add(card), openHand)
//      case 1 | 3 => (tab.removedCards(matchCards), openHand.addCards(card +: matchCards))
//      case 2 => {
//        val cardTab: Card = chooseCard(CardStack(matchCards), "choose card table")
//        (tab.removed(cardTab), openHand.addCards(List(card, cardTab)))
//      }
//    }
//    (newTab, newOpenHand)
//  }
//
//  def chooseCard(cards: CardStack, msg: String = "choose card"): Card = {
//    println(s"${msg}: ${cards}")
//    val cardString = StdIn.readLine()
//    val card = Card(cardString).getOrElse(chooseCard(cards, msg))
//    cards.checkCard(card).getOrElse(chooseCard(cards, msg))
//  }
//
//  def contineRound(playerInfo: PlayerInfo): Boolean = {
//    if (playerInfo.scoreChange) {
//      val dec = {
//        println("Koi-Koi or Win")
//        StdIn.readLine() match {
//          case x: String if x.toUpperCase.trim.startsWith("KOI") => true
//          case _ => false
//        }
//      }
//      dec
//    } else true
//  }
//
//
//  def nextState(playerInfo: PlayerInfo, opponent: PlayerInfo, tab: CardStack,  deck: CardStack, winner: Boolean, step: Int): Unit = {
//    val nextStep = step + 1
//    println("-----------------------")
//    println(s"step: $step\n player1: ${playerInfo.hand} \n player2: ${opponent.hand} \n tab: ${tab.cards} \n deck: {$deck}")
//    if (winner) {
//      //(playerInfo, tab, winner, deck)
//      println("is winner")
//    } else if (playerInfo.hand.isEmpty) {
//      //(playerInfo, tab, winner, deck)
//      println("hand empty. Game over")
//    } else if (deck.isEmpty) {
//      //(playerInfo, tab, winner, deck)
//      println("deck is empty. draw")
//    } else if (tab.isEmpty) {
//      println("add card in table in deck")
//      val (newCard, newDeck) = deck.pop
//      val newTab = tab.add(newCard)
//      nextState(playerInfo, opponent, newTab, newDeck, winner, nextStep)
//    } else {
//      val card = chooseCard(playerInfo.hand)
//      val nextHand = playerInfo.hand.removed(card)
//      val (newTab, newOpenHand) = matchTab(card, tab, playerInfo.openHand)
//      val (deckCard, nextDeck) = deck.pop
//      val (nextTab, nextOpenHand) = matchTab(deckCard, newTab, newOpenHand)
//      val nextPlayer = playerInfo.copy(hand = nextHand, openHand = nextOpenHand).calcScore
//      val nextWinner = !contineRound(nextPlayer)
//      nextState(opponent, nextPlayer, nextTab,  nextDeck, nextWinner, nextStep)
//    }
//  }
//
//  val (hands, deck) = CardStack.deal()
//  val initScore = hands.map(h => Score.monthScore(h.cards))
//  val playerInfo = PlayerInfo(hands(0))
//  val opponent = PlayerInfo(hands(1))
//  val tab = hands(2)
//  println(s"intial state player1: ${playerInfo.hand} \n player2: ${opponent.hand} \n tab: ${tab.cards} \n deck: {$deck}")
//
//  nextState(playerInfo, opponent, tab, deck, false, 0)
//
//}
//
//import cats.effect.concurrent.Ref
//import cats.effect.{ExitCode, IO, IOApp}
//import com.example.model.Model.CardStack
//
//import scala.io.StdIn
//
//object RefTest extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] = {
//    val state: IO[Ref[IO, Int]] = Ref.of[IO, Int](0)
//
//    def add(state: Ref[IO, Int], i: Int): IO[Unit] = state.update(acc => acc + i)
//
//    val program = for {
//      stateRef <- state
//      _ <- add(stateRef, 5)
//      _ <- add(stateRef, 10)
//      updatedRef <- stateRef.get
//    } yield updatedRef
//
//    program.flatMap(res => IO(println(res))).map(_ => ExitCode.Success)
//  }
//}
//
//object RefTest2 extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] = {
//    val state: IO[Ref[IO, Map[String, Int]]] = Ref.of[IO, Map[String, Int]](Map.empty[String,Int])
//
//    def add(state: Ref[IO, Map[String, Int]], i: (String, Int)): IO[Unit] = state.update(acc => acc + i)
//
//    val program = for {
//      stateRef <- state
//      _ <- add(stateRef, ("play1", 5))
//      _ <- add(stateRef, ("play2", 10))
//      _ <- add(stateRef, ("play2", 10))
//      _ <- add(stateRef, ("play1", 1))
//      updatedRef <- stateRef.get
//    } yield updatedRef
//
//    program.flatMap(res => IO(println(res))).map(_ => ExitCode.Success)
//  }
//}
//
////object RefTest3 extends IOApp {
////  override def run(args: List[String]): IO[ExitCode] = {
////    val state: IO[Ref[IO, Player]] = Ref.of[IO, Player](Player(name = "play1"))
////
////    def add(state: Ref[IO, Player], hand: List[Card]): IO[Unit] = state.update(acc => acc.copy(hand = hand))
////    def add(state: Ref[IO, Player], player: Player): IO[Unit] = state.update(acc => player)
////
////    val program = for {
////      stateRef <- state
////      _ <- add(stateRef, hand = CardSet.Hikari.toList)
////      player <- stateRef.get
////      cardC <- player.choseCard(Card.Curtain)
////      _ <- add(stateRef, cardC._2.pure)
////      updatedRef <- stateRef.get
////    } yield updatedRef
////
////    program.flatMap(res => IO(println(res))).map(_ => ExitCode.Success)
////  }
////}