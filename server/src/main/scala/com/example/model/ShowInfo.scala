package com.example.model

case class ShowPlayer(turn: Boolean, hand: CardStack, openHand: CardStack, scoreTotal: Int, score: Int)
case class ShowInfo(status: GameStatus, round: Int, tab: CardStack,
                    my: ShowPlayer,
                    opponent: ShowPlayer,
                    card: Option[Card])
