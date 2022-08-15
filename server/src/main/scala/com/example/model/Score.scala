package com.example.model

import com.example.model.Cards.Cards
import com.example.model.Card._
import com.example.model.CardSet.InoShikaCho

object Score {
  def getScore(hand: Cards): Int = {
    println(hikariScore(hand), animalScore(hand), tanzakuScore(hand), simpleScore(hand), sakeScore(hand))
    monthScore(hand)
    val score = (
      hikariScore(hand) +
        animalScore(hand) +
        tanzakuScore(hand) +
        simpleScore(hand) +
        sakeScore(hand)
      )
    score
  }

  private def hikariScore(hand: Cards): Int = {
    val hikari = hand.filter(c => c.isHikari)
    val hikariCount = hikari.length
    val existsRainMan = hikari.contains(OnoNoMichikaze)
    val score = hikariCount match {
      case 5 => 10
      case 4 => if (!existsRainMan) 8 else 7
      case 3 => if (!existsRainMan) 5 else 0
      case _ => 0
    }
    score
  }

  private def animalScore(hand: Cards): Int = {
    val animal = hand.filter(c => c.isAnimal)
    val animalCount = animal.length
    val isInoShikaCho: Boolean = animal.filter(c => InoShikaCho.contains(c)).length == 3
    val score: Int = isInoShikaCho match {
      case true => 5 + animalCount - 3
      case false => {
        if (animalCount >= 5) 1 + animalCount - 5
        else 0
      }
    }
    score
  }

  private def tanzakuScore(hand: Cards): Int = {
    val tanzaku = hand.filter(c => c.isTanzaku)
    val tanzakuCount = tanzaku.length
    val isPoetic = tanzaku.filter(c => c.isPoeticTanzaku).length == 3
    val isBlue = tanzaku.filter(c => c.isBlueTanzaku).length == 3
    val score: Int = {
      if (isPoetic && isBlue) 10 + tanzakuCount - 6
      else if (isPoetic || isBlue) 5 + tanzakuCount - 3
      else if (tanzakuCount >= 5) 1 + tanzakuCount - 5
      else 0
    }
    score
  }

  private def simpleScore(hand: Cards): Int = {
    val simple = hand.filter(c => c.isSimple)
    val simpleCount = simple.length
    val score = {
      if (simpleCount >= 10) 1 + simpleCount - 10
      else 0
    }
    score
  }

  private def sakeScore(hand: Cards): Int = {
    val isSake = hand.contains(Sake)
    val isMoon = hand.contains(Moon)
    val isCurtain = hand.contains(Curtain)
    val score = {
      if (isSake && isMoon && isCurtain) 10
      else if (isSake && isMoon) 5
      else if (isSake && isCurtain) 5
      else 0
    }
    score
  }

  def monthScore(hand: Cards):Int = {
    val month = hand.groupBy(c => c.suit)
    val pair = month.groupBy(p => p._2.length == 2)
    val fullmonth = month.groupBy(m => m._2.length == 4)
    val pairCount = pair.get(true) match {
      case None => 0
      case Some(value) => value.count(x => x._2.length == 2)
    }
    val monCount = fullmonth.get(true) match {
      case None => 0
      case Some(value) => value.count(x => x._2.length == 4)
    }
    val score = {
      if (pairCount == 4 || monCount == 1) 6
      else if (monCount == 2) 14
      else 0
    }
    score
  }
}