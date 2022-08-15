package tutorial.webapp

import com.example.Model.Card.CardBack
import org.scalajs.dom
import org.scalajs.dom.raw._
import org.scalajs.dom.{HttpMethod, RequestInit, RequestMode, document}
import com.example.Model._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future
import scala.scalajs.js

object ClientApp {
  val imgExt = "png"

  def main(args: Array[String]): Unit = {
    document.addEventListener("DOMContentLoaded", { (e: dom.Event) =>
      setupUI()
    })
  }

  def createInput(parent: Element, id: String, placeholder: String): Unit ={
    val input = document.createElement("input")
    input.setAttribute("id", id)
    input.setAttribute("placeholder", placeholder)
    input.setAttribute("minlength", "1")
    input.setAttribute("type", "text")
    input.setAttribute("required","")
    parent.appendChild(input)
  }

  def setupUI(): Unit = {

    val divFooter = document.createElement("div")
    divFooter.setAttribute("class","footer")
    divFooter.setAttribute("id","footer")

    val label = document.createElement("label")
    label.setAttribute("for", "sel-round")
    label.textContent = "Count round "
    divFooter.appendChild(label)

    val selRound = document.createElement("select")
    selRound.setAttribute("class", "sel-round")
    selRound.setAttribute("id", "sel-round")
    List(12,6,3).map{r =>
      val optRound = document.createElement("option")
      optRound.textContent = s"${r}"
      selRound.appendChild(optRound)
    }
    divFooter.appendChild(selRound)

    createInput(divFooter, "gameID", "game")
    createInput(divFooter, "name", "name")

    val butJoin = document.createElement("button")
    butJoin.textContent = "JOIN"
    butJoin.setAttribute("id", "join")
    butJoin.addEventListener("click", { (e: dom.MouseEvent) =>
      inGame()
    })
    divFooter.appendChild(butJoin)

    val butCreate = document.createElement("button")
    butCreate.textContent = "CREATE"
    butCreate.setAttribute("id", "create")
    butCreate.addEventListener("click", { (e: dom.MouseEvent) =>
      inGame()
    })
    divFooter.appendChild(butCreate)

    val parFill = document.createElement("p")
    parFill.setAttribute("id", "fill")
    parFill.setAttribute("style", "display: none;")
    parFill.textContent = "Fill in required fields"

    divFooter.appendChild(parFill)

    document.body.appendChild(divFooter)
  }

  def inGame(): Unit = {
    val nameField = dom.document.getElementById("name").asInstanceOf[dom.HTMLInputElement]
    val gameField = dom.document.getElementById("gameID").asInstanceOf[dom.HTMLInputElement]
    val roundField = dom.document.getElementById("sel-round").asInstanceOf[dom.HTMLSelectElement]
    if (!gameField.value.isBlank && !nameField.value.isBlank) joinChat(gameField.value, nameField.value, roundField.value.toInt)
    else {
      val parFill = dom.document.getElementById("fill")
      parFill.removeAttribute("style")
    }
  }

  def joinChat(gameId: String, name: String, round: Int): Unit = {
    val ws: dom.WebSocket = new dom.WebSocket(s"ws://192.168.1.68:9002/game/${gameId}/${name}/${round}")
    ws.onmessage = { (event: dom.MessageEvent) =>
      val wsMsg = event.data.toString
      //перерисовываю экран
      val infoS: Either[Error, ShowInfo] = decode[ShowInfo](wsMsg)
      val res: Unit = infoS match {
        case Right(value) => showInfoDom(wsMsg)
        case Left(value) => ()
      }
    }

    ws.onopen = (x: Event) => {
      ws.send(JoinGame(act = "join").asJson.noSpaces)
      val sel = document.body.querySelector("#footer")
      sel.innerHTML = ""
      sel.remove()
    }
    ws.onerror = (x: ErrorEvent) => Console.println("some error has occured " + x.message)
    ws.onclose = (x: CloseEvent) => {
      clearDes()
      clearInfoDom()
    }


    def sendEvent(event: GameEvent): Unit = {
      ws.send(event.asJson.noSpaces)
    }

    def addOtherCard(card: Card): Unit = {
      //<div class="cardO"><img src="image/hanafuda/${imgExt}/003.svg"></div>
      val sel = document.body.querySelector("#otherHand")
      val parDiv = document.createElement("div")
      parDiv.setAttribute("class", "cardO")
      val parImg = document.createElement("img")
      parImg.setAttribute("src", s"image/hanafuda/${imgExt}/${card.getName}.${imgExt}")
      parDiv.appendChild(parImg)
      sel.appendChild(parDiv)
    }

    def addHandCard(card: Card, turn: Boolean): Unit = {
      // <!-- document.querySelector("body > div > div.game > div > div.hand > div:nth-child(5) > img") -->
      // <!-- <div class="card"><img class="сardimg" src="image/hanafuda/124.${imgExt}"></div> -->
      val sel = document.body.querySelector("#hand")
      val parDiv = document.createElement("div")
      parDiv.setAttribute("class", "card")
      val parImg = document.createElement("img")
      if (turn == true) {
        parImg.setAttribute("class", "cardimg")
        parImg.addEventListener("click", { (e: dom.MouseEvent) =>
          sendEvent(HandCard(card = card))
        })
      }
      parImg.setAttribute("src", s"image/hanafuda/${imgExt}/${card.getName}.${imgExt}")
      parImg.setAttribute("id", s"${card.getName}")
      parDiv.appendChild(parImg)
      sel.appendChild(parDiv)
    }

    def addTab(card: Card, line: Int, turn: Boolean, waitcard: Option[Card]): Unit = {
      // <!-- <div class="cardB"><img src="image/hanafuda/${imgExt}/054.svg"></div>					 -->
      // <!--document.querySelector("#table1")-->
      val sel = document.body.querySelector(s"#table$line")
      val parDiv = document.createElement("div")
      parDiv.setAttribute("class", "cardB")
      val parImg = document.createElement("img")
      parImg.setAttribute("src", s"image/hanafuda/${imgExt}/${card.getName}.${imgExt}")
      if (turn == true && card.suit == waitcard.getOrElse(CardBack).suit) {
        parImg.setAttribute("class", "cardimg")
        parImg.addEventListener("click", { (e: dom.MouseEvent) =>
          sendEvent(TabCard(card = card))
        })
      }
      parImg.setAttribute("id", s"${card.getName}")
      parDiv.appendChild(parImg)
      sel.appendChild(parDiv)
    }

    def showTab(cards: Cards, turn: Boolean, waitcard: Option[Card]): Unit = {
      val n: Int = cards.size / 2 + cards.size % 2
      val tableLine = cards.grouped(n).toArray
      tableLine(0).map(card => addTab(card, 1, turn, waitcard))
      if (tableLine.size == 2) tableLine(1).map(card => addTab(card, 2, turn, waitcard))
    }

    def addWaitCard(card: Option[Card]): Unit = {
      // <!-- <img id="#waitcard"	src="image/hanafuda/${imgExt}/024.svg">					 -->
      // 			<!-- document.querySelector("#deck > img:nth-child(2)") -->
      if (card.isDefined) {
        val sel = document.body.querySelector("#waitdiv")
        val parImg = document.createElement("img")
        parImg.setAttribute("src", s"image/hanafuda/${imgExt}/${card.get.getName}.${imgExt}")
        parImg.setAttribute("id", "waitcard")
        sel.appendChild(parImg)
      }
    }

    def createScore(selector: String, scoreTotal: Int, turn: Boolean): Unit = {
      // 	<!-- <p>total score: 4545</p> -->
      // <!-- document.querySelector("#me > div.totalScore > p") -->
      val sel = document.body.querySelector(selector)
      val parP = document.createElement("p")
      if (turn) parP.setAttribute("style", "text-indent: 0%;")
      parP.textContent = (if (turn) "✋ " else "") + s"total score: ${scoreTotal}"
      sel.appendChild(parP)
    }

    def addScore(scoreTotal: Int, turn: Boolean): Unit = {
      createScore("#other > div.totalScore", scoreTotal: Int, turn: Boolean)
    }

    def addMyScore(scoreTotal: Int, turn: Boolean): Unit = {
      createScore("#me > div.totalScore", scoreTotal: Int, turn: Boolean)
    }

    def addRound(round: Int): Unit = {
      // <!-- <p id="round">round: 12</p>  -->
      // 	<!-- document.querySelector("#round") -->
      val parP = document.body.querySelector("#round")
      parP.textContent = s"round: ${round}"
    }

    def createOpenHand(selector: String, card: Card): Unit = {
      val sel = document.body.querySelector(selector)
      val parDiv = document.createElement("div")
      parDiv.setAttribute("class", "card")
      val parImg = document.createElement("img")
      parImg.setAttribute("src", s"image/hanafuda/${imgExt}/${card.getName}.${imgExt}")
      parDiv.appendChild(parImg)
      sel.appendChild(parDiv)
    }

    def addOpenHand(card: Card): Unit = {
      // <!-- <div class="card"><img src="image/hanafuda/${imgExt}/124.svg"></div> -->
      // 			<!-- document.querySelector("#other > div.OpenHand > div:nth-child(1) > div:nth-child(1) > img") -->
      createOpenHand("#other > div.OpenHand", card)
    }

    def addMyOpenHand(card: Card): Unit = {
      // 	<!-- <div class="card"><img src="image/hanafuda/${imgExt}/124.svg"></div> -->
      // <!-- document.querySelector("#me > div.OpenHand > div:nth-child(4) > img") -->
      createOpenHand("#me > div.OpenHand", card)
    }


    def showInfoDom(info: String): Unit = {
      val infoS: Either[Error, ShowInfo] = decode[ShowInfo](info)
      val infoF: ShowInfo = infoS.getOrElse(ShowInfo.empty)
      val turnHand: Boolean = infoF.my.turn && infoF.status == ChooseCardHand
      val turnTab: Boolean = infoF.my.turn && (infoF.status == WaitTabCardForDesk || infoF.status == WaitTabCardForHand)

      clearDes()
      showDes(infoF)
      clearInfoDom()

      infoF.opponent.hand.cards.map(card => addOtherCard(card))
      infoF.opponent.openHand.cards.map(card => addOpenHand(card))
      addScore(infoF.opponent.scoreTotal, infoF.opponent.turn)

      infoF.my.hand.cards.map(card => addHandCard(card, turnHand))
      infoF.my.openHand.cards.map(card => addMyOpenHand(card))
      addMyScore(infoF.my.scoreTotal, infoF.my.turn)

      showTab(infoF.tab.cards, turnTab, infoF.card)
      addWaitCard(infoF.card)
      addRound(infoF.round)
    }

    def clearDes(): Unit = {
        val sel = document.body.querySelector("#des")
        if (sel != null) {
          clearSelDom("#des")
          sel.remove()
        }
    }

    def showWinKoi(info: ShowInfo): Unit = {
      //     <div class="b-popup"> <div class="b-popup-content">
      if (info.status == WaitDecision) {
        val parDiv = document.createElement("div")
        parDiv.setAttribute("class", "b-popup")
        parDiv.setAttribute("id", "des")

        val parDivMsg = document.createElement("div")
        parDivMsg.setAttribute("class", "b-popup-content")

        val parMsg = document.createElement("h3")
        parMsg.setAttribute("id","des-msg")

        parMsg.textContent =
          if (info.my.turn == true) s"Your score change. Score = ${info.my.score}"
          else s"Opponent's score change. Score = ${info.opponent.score}"
        parDivMsg.appendChild(parMsg)

        if (info.my.turn == true) {
          val butKoi = document.createElement("button")
          butKoi.textContent = "Koi-Koi"
          butKoi.addEventListener("click", { (e: dom.MouseEvent) =>
            sendEvent(PlayerDecision(text = "koi"))
            clearSelDom("#des")
            document.body.querySelector("#des").remove()
          })

          val butWin = document.createElement("button")
          butWin.textContent = "Win"
          butWin.addEventListener("click", { (e: dom.MouseEvent) =>
            sendEvent(PlayerDecision(text = "win"))
            clearSelDom("#des")
            document.body.querySelector("#des").remove()
          })
          parDivMsg.appendChild(butKoi)
          parDivMsg.appendChild(butWin)
        }

        parDiv.appendChild(parDivMsg)
        document.body.appendChild(parDiv)
      }
    }

    def showNextGame(info: ShowInfo): Unit = {
      if (info.status == End) {
        val parDiv = document.createElement("div")
        parDiv.setAttribute("class", "b-popup")
        parDiv.setAttribute("id", "des")

        val parDivMsg = document.createElement("div")
        parDivMsg.setAttribute("class", "b-popup-content")

        val parMsg = document.createElement("h3")
        parMsg.setAttribute("id","des-msg")

        parMsg.textContent =
          if (info.my.scoreTotal > info.opponent.scoreTotal) "You Win 🎉."
          else if (info.my.scoreTotal == info.opponent.scoreTotal) "You have a draw ☯"
          else "You will be lucky next time \uD83D\uDDFF."
        parDivMsg.appendChild(parMsg)

        val parQuestion = document.createElement("h4")
        parQuestion.textContent = "Star new game?"
        parDivMsg.appendChild(parQuestion)

        val butYes = document.createElement("button")
        butYes.setAttribute("class","Yes")
        butYes.textContent = "Yes"
        butYes.addEventListener("click", { (e: dom.MouseEvent) =>
          ws.close()
          setupUI()
          val selName = document.body.querySelector("#name").asInstanceOf[HTMLInputElement]
          selName.value = name
          val selGame = document.body.querySelector("#gameID").asInstanceOf[HTMLInputElement]
          selGame.value = gameId
        })
        parDivMsg.appendChild(butYes)

        val butNo = document.createElement("button")
        butNo.setAttribute("class","No")
        butNo.textContent = "No"
        butNo.addEventListener("click", { (e: dom.MouseEvent) =>
          ws.close()
        })
        parDivMsg.appendChild(butNo)

        parDiv.appendChild(parDivMsg)
        document.body.appendChild(parDiv)
      }
    }

    def showDes(info: ShowInfo):Unit = {
      if (info.status == WaitDecision) showWinKoi(info)
      else if (info.status == End) showNextGame(info)
      else ()
    }

    def clearInfoDom(): Unit = {
      List("#otherHand",
        "#hand",
        "#table1", "#table2",
        "#other > div.totalScore",
        "#me > div.totalScore",
        "#other > div.OpenHand",
        "#me > div.OpenHand",
        "#waitdiv").map(sel => clearSelDom(sel))
    }

    def clearSelDom(selector: String): Unit = {
      document.body.querySelector(selector).innerHTML = ""
    }
  }

}