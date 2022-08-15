package com.example.game

import com.example.model.Card.CardBack
import com.example.model.Cards._
import com.example.model._
import io.circe.syntax.EncoderOps


case class Game(gameStatus: GameStatus, playerInfo: PlayerInfo, opponent: Option[PlayerInfo], tab: CardStack, deck: CardStack, card: Option[Card], round: Int, countRound: Int = 12 ) {

  def getMatchTabCard(card: Card): Cards = tab.getMatchCards(card)

  def checkChosenCardHand(card: Card): Boolean = playerInfo.hand.cards.contains(card)

  def currentPlayer = this.playerInfo.player

  def existsPlayer(player: Player): Boolean = {
    val players = Set(Some(playerInfo.player), opponent.map(o => o.player))
    players.contains(Some(player))
  }

  private def matchTab(card: Card): Game = {
    val openHand = this.playerInfo.openHand
    val matchCards = this.tab.getMatchCards(card)
    val nextStatus: GameStatus = this.gameStatus match {
      case ChooseCardHand => if (matchCards.size == 2) WaitTabCardForHand else DeckCard
      case DeckCard => if (matchCards.size == 2) WaitTabCardForDesk else CalcScore
      case x => x
    }
    val nextHand = {
      if (this.gameStatus == ChooseCardHand) playerInfo.hand.removed(card)
      else playerInfo.hand
    }
    val nextGame = matchCards.size match {
      case 0 =>
        this.copy(gameStatus = nextStatus,
          tab = tab.add(card),
          playerInfo = playerInfo.copy(hand = nextHand),
          card = None)
      case 1 | 3 =>
        this.copy(gameStatus = nextStatus,
          playerInfo = playerInfo.copy(hand = nextHand, openHand = openHand.addCards(card +: matchCards)),
          tab = tab.removedCards(matchCards),
          card = None)
      case 2 =>
        this.copy(gameStatus = nextStatus,
          playerInfo = playerInfo.copy(hand = nextHand),
          card = Some(card))
    }
    nextGame
  }

  def choseHandCard(card: Card): Either[String, Game] = {
    if (!checkChosenCardHand(card))
      Left("Chose the right card")
    else Right(Game.nextStep(matchTab(card)))
  }

  def choseTabCard(card: Card): Either[String, Game] = {
    if (!this.tab.cards.contains(card) || Option(card.suit) != this.card.map(_.suit))
      Left("Chose the right card")
    else {
      val nextGameStatus: GameStatus = this.gameStatus match {
        case WaitTabCardForHand => DeckCard
        case WaitTabCardForDesk => CalcScore
        case x => x
      }
      val nextPlayerInfo = playerInfo.copy(openHand = playerInfo.openHand.addCards(List(card, this.card.get)))
      val game = this.copy(gameStatus = nextGameStatus,
        playerInfo = nextPlayerInfo,
        tab = this.tab.removed(card),
        card = None)
      Right(Game.nextStep(game))
    }
  }

  def makeDecision(text: String): Either[String, Game] = {
    Decision.fromString(text)
      .fold(left => Left(left),
        right => Right(right match {
          case KoiKoi => Game.nextStep(this.copy(gameStatus = ChooseCardHand, playerInfo = this.opponent.get, opponent = Some(playerInfo)))
          case Win => Game.nextStep(this.copy(gameStatus = Winner))
        }))
  }

  def leaveGame(player: Player): Either[String, Game] = {
    //if (player == playerInfo.player || player == this.opponent.get) Right(this.copy(gameStatus = LeaveGame))
    if (!existsPlayer(player)) Right(this.copy(gameStatus = LeaveGame))
    else Left("You are not in the game")
  }

  def getInfo(player: Player): Option[String] = {
    import io.circe.generic.auto._

    if (!existsPlayer(player))
      None
    else {
      val currentInfo = if (player == playerInfo.player) playerInfo else opponent.get
      val opponentInfo = if (player == playerInfo.player) opponent.get else playerInfo
      val my: ShowPlayer = ShowPlayer(turn = player == currentPlayer,
        hand = currentInfo.hand,
        openHand = currentInfo.openHand,
        scoreTotal = currentInfo.scoreTotal,
        score = currentInfo.score)
      val opponentP = ShowPlayer(turn = opponentInfo.player == currentPlayer,
        hand = opponentInfo.hand.copy(cards = opponentInfo.hand.cards.map(_ => CardBack)),
        openHand = opponentInfo.openHand,
        scoreTotal = opponentInfo.scoreTotal,
        score = opponentInfo.score)
      val info2: ShowInfo = ShowInfo(status = this.gameStatus,
        round = this.round,
        my = my,
        opponent = opponentP,
        tab = this.tab,
        card = this.card)
      Some(info2.asJson.noSpaces)
    }
  }

  def startGame: Game = {
    val (stack, deck) = CardStack.deal()
    val initScore = stack.map(h => Score.monthScore(h.cards))
    val nextPlayerInfo = this.playerInfo.newHand(hand = stack(0))
    val nextOpponent = this.opponent.map(o => o.newHand(hand = stack(1)))
    val tab = stack(2)
    println(s"intial state player1: ${stack(0)} \n player2: ${stack(1)} \n tab: ${tab.cards} \n deck: {$deck}")
    this.copy(gameStatus = ChooseCardHand, playerInfo = nextPlayerInfo, opponent = nextOpponent, tab, deck, None)
  }

  def joinGame(player: Player): Either[String, Game] = {
    this.opponent match {
      case Some(value) => Left("Game full")
      case None => if (playerInfo.player != player)
        Right(Game.nextStep(this.copy(gameStatus = StartGame, opponent = Some(PlayerInfo(player)))))
      else Left("You already join in the game")
    }
  }

  def makeEvent(player: Player, gameEvent: GameEvent): Either[String, Game] = gameEvent match {
    case HandCard(_, card) =>
      if (player != currentPlayer) Left("It's not your turn")
      else if (gameStatus != ChooseCardHand) Left("Wrong command")
      else choseHandCard(card)
    case TabCard(_, card) =>
      if (player != currentPlayer) Left("It's not your turn")
      else if (gameStatus != WaitTabCardForDesk && gameStatus != WaitTabCardForHand) Left("Wrong command")
      else choseTabCard(card)
    case PlayerDecision(_, text) =>
      if (player != currentPlayer) Left("It's not your turn")
      else if (gameStatus != WaitDecision) Left("Wrong command")
      else makeDecision(text)
    case Leave(_) => leaveGame(player)
    case JoinGame(_) => joinGame(player)
  }
}

object Game {

  def apply(player: Player, round: Int): Game = {
    println(player)
    new Game(WaitOpponent, playerInfo = PlayerInfo(player), None, CardStack.empty, CardStack.empty, None, 1, countRound = round)
  }

  def nextStep(game: Game): Game = {
    println("-----------------------")
    println(s"game status: ${game.gameStatus}; player1: ${game.playerInfo.hand} \n " +
      s"player2: ${game.opponent.map(o => o.hand)} \n " +
      s"tab: ${game.tab.cards} \n " +
      s"deck: ${game.deck} \n" +
      s"step: ${game.playerInfo.player.name}")
    if (game.gameStatus == StartGame) {
      println("start new game")
      game.startGame
    } else if (game.gameStatus == Finish) {
      if (game.round == game.countRound) game.copy(gameStatus = End)
      else nextStep(game.copy(gameStatus = StartGame, round = game.round + 1))
    } else if (game.gameStatus == Winner) {
      println("is winner")
      val scoreTotal = game.playerInfo.scoreTotal + game.playerInfo.score
      nextStep(game.copy(gameStatus = Finish, playerInfo = game.playerInfo.copy(scoreTotal = scoreTotal)))
    } else if (game.playerInfo.hand.isEmpty) {
      println("hand empty. Game over")
      nextStep(game.copy(gameStatus = Finish))
    } else if (game.deck.isEmpty && (game.gameStatus == ChooseCardHand && game.gameStatus == DeckCard)) {
      println("deck is empty. draw")
      nextStep(game.copy(gameStatus = Finish))
    } else if (game.tab.isEmpty) {
      println("add card in table in deck")
      val (newCard, newDeck) = game.deck.pop
      val newTab = game.tab.add(newCard)
      nextStep(game.copy(tab = newTab, deck = newDeck))
    } else if (List(WaitTabCardForDesk, WaitTabCardForHand, WaitDecision, WaitOpponent).contains(game.gameStatus)) {
      println("wait")
      game
    } else if (game.gameStatus == DeckCard) {
      val (deckCard, nextDeck) = game.deck.pop
      nextStep(game.matchTab(deckCard).copy(deck = nextDeck))
    } else if (game.gameStatus == CalcScore) {
      val nextPlayerInfo = game.playerInfo.calcScore
      if (nextPlayerInfo.scoreChange) game.copy(gameStatus = WaitDecision, playerInfo = nextPlayerInfo)
      else game.copy(gameStatus = ChooseCardHand, playerInfo = game.opponent.get, opponent = Some(nextPlayerInfo))
    } else game
  }
}




