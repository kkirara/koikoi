package com.example.model

sealed trait GameStatus{
  protected def allowPrevious: List[GameStatus]
  protected def allowedNext: List[GameStatus]
}

case object WaitOpponent extends GameStatus {
  protected def allowPrevious: List[GameStatus] = ???
  protected def allowedNext: List[GameStatus] = List(StartGame)
}

case object StartGame extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(WaitOpponent, Finish)
  protected def allowedNext: List[GameStatus] = List(ChooseCardHand, StartGame, Finish)
}

case object ChooseCardHand extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(StartGame, CalcScore, WaitDecision)
  protected def allowedNext: List[GameStatus] = List(DeckCard, WaitTabCardForHand)
}

case object WaitTabCardForHand extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(ChooseCardHand)
  protected def allowedNext: List[GameStatus] = List(DeckCard)
}

case object DeckCard extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(ChooseCardHand, WaitTabCardForHand)
  protected def allowedNext: List[GameStatus] = List(CalcScore, WaitTabCardForDesk)
}

case object WaitTabCardForDesk extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(DeckCard)
  protected def allowedNext: List[GameStatus] = List(CalcScore)
}

case object CalcScore extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(DeckCard, WaitTabCardForHand)
  protected def allowedNext: List[GameStatus] = List(WaitDecision, ChooseCardHand)
}

case object WaitDecision extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(CalcScore)
  protected def allowedNext: List[GameStatus] = List(Winner, ChooseCardHand)
}

case object Winner extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(WaitDecision)
  protected def allowedNext: List[GameStatus] = List(Finish)
}

case object EmptyDeck extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(ChooseCardHand)//???
  protected def allowedNext: List[GameStatus] = List(Finish)
}

case object Finish extends GameStatus {
  protected def allowPrevious: List[GameStatus] = List(ChooseCardHand, Winner, EmptyDeck, LeaveGame)
  protected def allowedNext: List[GameStatus] = List(StartGame, End)
}

case object End extends GameStatus{
  protected def allowPrevious: List[GameStatus] = List(Finish, LeaveGame)
  protected def allowedNext: List[GameStatus] = List()
}

case object LeaveGame extends GameStatus {
  protected def allowPrevious: List[GameStatus] = ???
  protected def allowedNext: List[GameStatus] = List(Finish)
}