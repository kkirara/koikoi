package com.example.model

import com.example.model.Cards._

case class CardStack(cards: Cards) {

  def checkCard(card: Card): Option[Card] =
    if (cards.contains(card)) Some(card) else None

  def add(card: Card): CardStack =
    checkCard(card) match {
      case Some(valCard) => this
      case None => this.copy(card +: cards)
    }

  def removed(card: Card): CardStack =
    checkCard(card) match {
      case Some(valCard) => this.copy(cards.filterNot(_ == valCard))
      case None => this
    }

  def addCards(that: Cards): CardStack = this.copy(that ++ cards)

  def removedCards(that: Cards): CardStack =
    this.copy(cards.filterNot(c => that.contains(c)))

  def isEmpty = cards.isEmpty

  def shuffled: CardStack = {
    this.copy(cards = scala.util.Random.shuffle(cards))
  }

  def getMatchCards(card: Card): Cards =
    cards.filter(c => c.suit == card.suit)

  def getScore: Int = Score.getScore(cards)

  def getSortHand: List[Cards] = {
    val hikaru = cards.filter(_.isHikari)
    val animal = cards.filter(_.isAnimal)
    val tanzaku = cards.filter(_.isTanzaku)
    val simple = cards.filter(_.isSimple)
    List(hikaru, animal, tanzaku, simple)
  }

  def pop(n: Int): (Cards, CardStack) = {
    val top = this.cards.take(n)
    val newCards = this.cards.drop(n)
    (top, CardStack(newCards))
  }

  def pop: (Card, CardStack) = {
    (cards.head, CardStack(cards.tail))
  }
}

object CardStack {
  def sorted: CardStack = CardStack(Card.fullDeck)

  def shuffle: CardStack = CardStack(scala.util.Random.shuffle(Card.fullDeck))

  def empty: CardStack = CardStack(Nil)

  def deal(n: Int = 8, count: Int = 3): (List[CardStack], CardStack) = {
    val initDeck = shuffle
    val (cardsForHand, deck) = initDeck.pop(n * count)
    val cardGroup = cardsForHand.grouped(n / 2).toList
    val listCard = cardGroup.groupBy(cardGroup.indexOf(_) % count).values.toList.map(x => CardStack(x.flatten))
    if (Score.monthScore(listCard.last.cards) == 0) (listCard, deck)
    else deal(n, count)
  }
}