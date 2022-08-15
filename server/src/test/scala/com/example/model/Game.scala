//package com.example.model
//
//import cats.effect.{ExitCode, IO, IOApp}
//import cats.effect.concurrent.Ref
//import com.example.game.Game
//
//object RefTestGame extends IOApp {
//  override def run(args: List[String]): IO[ExitCode] = {
//    val player1: Player = Player("player1")
//    val player2: Player = Player("player2")
//    val state: IO[Ref[IO, Game]] = Ref.of[IO, Game](Game(player1))
//
//    //def add(state: Ref[IO, Map[String, Int]], i: (String, Int)): IO[Unit] = state.update(acc => acc + i)
//
//    val program = for {
//      stateRef <- state
//      _ <- stateRef.update(game => game.joinGame(player2).getOrElse(game))
//      _ <- stateRef.update(game => game.choseHandCard(game.playerInfo.hand.cards.head) match {
//        case Left(err) => game
//        case Right(g) => g
//      }
//      )
//      updatedRef <- stateRef.get
//    } yield updatedRef
//
//    program.flatMap(res => IO(println(res))).map(_ => ExitCode.Success)
//  }
//}