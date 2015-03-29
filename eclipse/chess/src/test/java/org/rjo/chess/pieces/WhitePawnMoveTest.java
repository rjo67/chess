package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WhitePawnMoveTest {

   private King opponentsKing;
   private King myKing;

   @Before
   public void setup() {
      opponentsKing = new King(Colour.BLACK, true);
      myKing = new King(Colour.WHITE, true);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Pawn pawn = new Pawn(Colour.WHITE, true);
      TestUtil.checkMoves(
            pawn.findMoves(game),
            new HashSet<>(Arrays.asList("a2-a3", "a2-a4", "b2-b3", "b2-b4", "c2-c3", "c2-c4", "d2-d3", "d2-d4",
                  "e2-e3", "e2-e4", "f2-f3", "f2-f4", "g2-g3", "g2-g4", "h2-h3", "h2-h4")));
   }

   @Test
   public void blockedPawn() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a2, Square.a3);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a3-a4")));
   }

   @Test
   public void captureLeft() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.b2);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Pawn opponentsPawn = new Pawn(Colour.BLACK, Square.a3, Square.b3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsPawn, opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b2xa3")));
   }

   @Test
   public void captureRight() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a2);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Pawn opponentsPawn = new Pawn(Colour.BLACK, Square.a3, Square.b3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsPawn, opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a2xb3")));
   }

   @Test
   public void promotionCheckKnight() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.g7);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King opponentsKing = new King(Colour.BLACK, Square.e7);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game),
            new HashSet<>(Arrays.asList("g7-g8=Q", "g7-g8=B", "g7-g8=N+", "g7-g8=R")));
   }

   @Test
   public void promotionCheckBishop() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.g7);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King opponentsKing = new King(Colour.BLACK, Square.e6);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game),
            new HashSet<>(Arrays.asList("g7-g8=Q+", "g7-g8=B+", "g7-g8=N", "g7-g8=R")));
   }

   @Test
   public void promotionCheckRook() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.g7);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King opponentsKing = new King(Colour.BLACK, Square.b8);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game),
            new HashSet<>(Arrays.asList("g7-g8=Q+", "g7-g8=B", "g7-g8=N", "g7-g8=R+")));
   }

   @Test
   public void enpassantRight() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing, new Pawn(Colour.BLACK, Square.b5)));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      game.getChessboard().setEnpassantSquare(Square.b6);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a5-a6", "a5xb6")));
   }

   @Test
   public void enpassantRightMoveAndUnmove() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing, new Pawn(Colour.BLACK, Square.b5)));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      game.getChessboard().setEnpassantSquare(Square.b6);
      String fenBefore = Fen.encode(game);
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a5, Square.b6, PieceType.PAWN);
      move.setEnpassant(true);
      game.move(move);
      game.unmove(move);
      assertEquals(fenBefore, Fen.encode(game));
   }

   @Test
   public void enpassantLeft() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.b5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing, new Pawn(Colour.BLACK, Square.a5)));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      game.getChessboard().setEnpassantSquare(Square.a6);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b5-b6", "b5xa6")));
   }

   @Test
   public void enpassantLeftMoveAndUnmove() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.b5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing, new Pawn(Colour.BLACK, Square.a5)));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      game.getChessboard().setEnpassantSquare(Square.a6);
      String fenBefore = Fen.encode(game);
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.b5, Square.a6, PieceType.PAWN);
      move.setEnpassant(true);
      game.move(move);
      game.unmove(move);
      assertEquals(fenBefore, Fen.encode(game));
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.b5, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing, new Pawn(Colour.BLACK, Square.c5)));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      game.getChessboard().setEnpassantSquare(Square.c6);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b5-b6", "b5xc6", "d5-d6", "d5xc6")));
   }

   @Test
   public void checkRight() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King king = new King(Colour.BLACK, Square.e7);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d6+")));
   }

   @Test
   public void checkLeft() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King king = new King(Colour.BLACK, Square.c7);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d6+")));
   }

   @Test
   public void checkCaptureLeft() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King king = new King(Colour.BLACK, Square.b7);
      Pawn opponentsPawn = new Pawn(Colour.BLACK, Square.c6);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king, opponentsPawn));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d6", "d5xc6+")));
   }

   @Test
   public void checkCaptureRight() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King king = new King(Colour.BLACK, Square.f7);
      Pawn opponentsPawn = new Pawn(Colour.BLACK, Square.d6, Square.e6);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king, opponentsPawn));
      Game game = new Game(new Chessboard(myPieces, opponentsPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5xe6+")));
   }

   @Test
   public void attacksSquare() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn, myKing));
      King king = new King(Colour.BLACK, Square.f7);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king));
      Chessboard chessboard = new Chessboard(myPieces, opponentsPieces);
      assertTrue(pawn.attacksSquare(chessboard, Square.e6));
      assertFalse(pawn.attacksSquare(chessboard, Square.d6));
      assertFalse(pawn.attacksSquare(chessboard, Square.e7));
   }
}
