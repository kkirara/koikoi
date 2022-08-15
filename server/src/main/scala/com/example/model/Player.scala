package com.example.model


case class PlayerInfo(player: Player, hand: CardStack, openHand: CardStack, score: Int, scoreChange: Boolean, scoreTotal: Int){
  def calcScore: PlayerInfo = {
    val newScore = Score.getScore(openHand.cards)
    val newScoreChange = newScore != this.score
    this.copy(score = newScore, scoreChange = newScoreChange)
  }

  def newHand(hand: CardStack): PlayerInfo =
    this.copy(hand = hand, openHand = CardStack.empty, score = 0, scoreChange = false)
}

object PlayerInfo {
  def apply(player: Player): PlayerInfo = PlayerInfo(player, CardStack.empty,  CardStack.empty, 0, false, 0)
  def apply(hand: CardStack): PlayerInfo = PlayerInfo(Player("Store"), hand,  CardStack.empty, 0, false, 0)
}


case class Player(name: String) {}