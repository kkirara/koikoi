//import com.example.model.Cards.Card
//import com.example.model.Score
//
////package com.example.model
//object GameX{
//
//  // val SpecCards: List[Card] = Hikari ++ Animal ++ AllBound
//  //
//  case class Deck() {
//    private var deck = scala.util.Random.shuffle(Card.fullDeck)
//    def pop: Card = {
//      val top = deck.head
//      deck = deck.tail
//      top
//    }
//
//    def pop(n: Int): List[Card] = {
//      val top = deck.take(n)
//      deck = deck.drop(n)
//      top
//    }
//
//    def isEmpty: Boolean = deck.isEmpty
//
//    def getDeck: List[Card] = deck
//  }
//
//  case class Table(cards: List[Card]) {
//    var cardsTable = cards
//
//    def getMatch(card: Card): List[Card] = {
//      cardsTable.filter(c => c.suit == card.suit)
//    }
//
//    def putCard(card: Card) = {
//      cardsTable = card +: cardsTable
//    }
//
//    def getCard(card:Card): Either[String, Card] = {
//      if (cardsTable.contains(card)) {
//        cardsTable = cardsTable.filter(_ != card)
//        Right(card)
//      } else {
//        Left(s"на столе нет этой карты $card")
//      }
//    }
//
//    def isEmpty = cardsTable.isEmpty
//
//  }
//
//  case class Player(name: String) {
//    var hand: List[Card] = Nil
//    var openHand: List[Card] = Nil
//    var score: Int = 0
//    var scoreChange: Boolean = false
//
//    def choseCard(card: Card): Either[String, Card]  = {
//      if (hand.contains(card)) {
//        hand = hand.filter(c => c == card)
//        Right(card)
//      } else Left(s"нет карты в руке $card")
//    }
//
//    def handIsEmpty: Boolean = hand.isEmpty
//
//    def addOpenHand(cards: List[Card]) = {
//      openHand ++= cards
//    }
//
//    def addHand(cards: List[Card]) = {
//      hand ++= cards
//    }
//
//    def calcScore = {
//      val newScore = Score.getScore(openHand)
//      if (newScore != score) {
//        scoreChange = true
//        score = newScore
//      } else {
//        scoreChange = false
//      }
//    }
//
//    def getScore = score
//  }
//}

//
//import com.example.model.GameX.{Deck, Player, Table}
//
//object CardMain extends App{
//  import com.example.model.Cards._
//  val d = Deck()
//  var hand1: List[Card] = Nil
//  var hand2: List[Card] = Nil
//  var openHand1: List[Card] = Nil
//  var openHand2: List[Card] = Nil
//
//  var table: List[Card] = Nil
//  for ( i <- 1 to 4) {
//    hand1 ++= d.pop(2)
//    hand2 ++= d.pop(2)
//    table ++= d.pop(2)
//  }
//  println(hand1)
//  println(hand2)
//  println(table)
//  val tab = Table(table)
//  println(d.getDeck.length)
//
//  def step1: Unit =
//  {
//    addCardTable
//    val card = hand1.head
//    println(card)
//    hand1 = hand1.tail
//    val matchCard = tab.getMatch(card)
//    if (matchCard.length == 0) tab.putCard(card)
//    else if (matchCard.length == 3) {
//      for {c <- matchCard
//           rc <- tab.getCard(c)} ()
//      openHand1 ++= card +: matchCard
//    }
//    else if ((1 to 2).contains(matchCard.length )) {
//      tab.getCard(matchCard(0))
//      openHand1 ++= List(card,  matchCard(0))
//    }
//  }
//
//  def step2: Unit =
//  {
//    addCardTable
//    val card = hand2.head
//    println(card)
//    hand2 = hand2.tail
//    val matchCard = tab.getMatch(card)
//    if (matchCard.length == 0) tab.putCard(card)
//    else if (matchCard.length == 3) {
//      for {c <- matchCard
//           rc <- tab.getCard(c)} ()
//      openHand2 ++= card +: matchCard
//    }
//    else if ((1 to 2).contains(matchCard.length )) {
//      tab.getCard(matchCard(0))
//      openHand2 ++= List(card,  matchCard(0))
//    }
//  }
//  def addCardTable = {
//    if (!d.isEmpty && tab.isEmpty) {
//      println("!!!!!tab empty!!!!!")
//      tab.putCard(d.pop)
//    }
//  }
//  while (!d.isEmpty && !hand1.isEmpty && !hand2.isEmpty) {
//    println("======================++++++++++++++")
//    if (!hand1.isEmpty) {
//      hand1 = scala.util.Random.shuffle(hand1)
//      step1
//      hand1 = d.pop +: hand1
//      step1
//    }
//    else {
//      println("hand1 empty")
//    }
//    println(s"table1: ${tab.cardsTable}")
//    println(s"Hand1: $hand1")
//    println(s"openHand1: $openHand1")
//    println("**************")
//    if (!hand1.isEmpty) {
//      if (!hand2.isEmpty) {
//        hand2 = scala.util.Random.shuffle(hand2)
//        step2
//        hand2 = d.pop +: hand2
//        step2
//      } else {
//        println("hand2 empty")
//      }
//    }
//    println(s"table2: ${tab.cardsTable}")
//    println(s"Hand2: $hand2")
//    println(s"openHand2: $openHand2")
//    println("======================score:")
//    println(Score.getScore(openHand1))
//    println(Score.getScore(openHand2))
//    println("======================")
//  }
//
//  println("final")
//  println(d.getDeck)
//  println(tab.cardsTable)
//  println(openHand1)
//  println(openHand2)
//  println(hand1)
//  println(hand2)
//}
//
//object CardPlayerMain extends App{
//  import com.example.model.Cards._
//  val d = Deck()
//  var hand1: List[Card] = Nil
//  var hand2: List[Card] = Nil
//  val player1 = Player(name = "player1")
//  val player2 = Player(name = "player2")
//
//  var table: List[Card] = Nil
//  for ( i <- 1 to 4) {
//    hand1 ++= d.pop(2)
//    hand2 ++= d.pop(2)
//    table ++= d.pop(2)
//  }
//  println(hand1)
//  println(hand2)
//  println(table)
//  val tab = Table(table)
//  player1.addHand(hand1)
//  player2.addHand(hand2)
//  println(d.getDeck.length)
//
//  def step(player: Player): Unit =
//  {
//    addCardTable
//    val card = player.hand.head
//    player.choseCard(card)
//    println(card)
//    val matchCard = tab.getMatch(card)
//    if (matchCard.length == 0) {
//      tab.putCard(card)
//    } else if (matchCard.length == 3) {
//      for {c <- matchCard
//           rc <- tab.getCard(c)} ()
//      player.addOpenHand(card +: matchCard)
//    } else if ((1 to 2).contains(matchCard.length )) {
//      tab.getCard(matchCard(0))
//      player.addOpenHand(List(card,  matchCard(0)))
//    }
//  }
//
//  def fullStep(player: Player) = {
//    if (!player.handIsEmpty) {
//      player.hand = scala.util.Random.shuffle(player.hand)
//      step(player)
//      player.hand = d.pop +: player.hand
//      step(player)
//      player.calcScore
//    }
//    else {
//      println(s"${player.name} hand empty")
//    }
//  }
//
//  def addCardTable = {
//    if (!d.isEmpty && tab.isEmpty) {
//      println("!!!!!tab empty!!!!!")
//      tab.putCard(d.pop)
//    }
//  }
//  while (!d.isEmpty && !player1.handIsEmpty && !player2.handIsEmpty) {
//    println("======================++++++++++++++")
//    fullStep(player1)
//    println(s"table1: ${tab.cardsTable}")
//    println(s"Hand1: $player1.hand")
//    println(s"openHand1: $player1.openHand")
//    println("**************")
//    if (!player1.handIsEmpty) {
//      fullStep(player2)
//    }
//    println(s"table2: ${tab.cardsTable}")
//    println(s"Hand2: ${player2.hand}")
//    println(s"openHand2: ${player2.openHand}")
//    println("======================score:")
//    println(player1.getScore)
//    println(player2.getScore)
//    println(d.getDeck.length)
//    println("======================")
//  }
//
//  println("final")
//  println(d.getDeck)
//  println(tab.cardsTable)
//  println(player1.openHand)
//  println(player2.openHand)
//  println(player1.hand)
//  println(player2.hand)
//}