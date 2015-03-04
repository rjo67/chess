package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

public class BlackPawnMoveTest {
   private King opponentsKing;

   @Before
   public void setup() {
      opponentsKing = new King(Colour.WHITE);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Pawn pawn = new Pawn(Colour.BLACK);
      TestUtil.checkMoves(
            pawn.findMoves(game),
            new HashSet<>(Arrays.asList("a7-a6", "a7-a5", "b7-b6", "b7-b5", "c7-c6", "c7-c5", "d7-d6", "d7-d5",
                  "e7-e6", "e7-e5", "f7-f6", "f7-f5", "g7-g6", "g7-g5", "h7-h6", "h7-h5")));
   }

   @Test
   public void blockedPawn() {
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Pawn pawn = new Pawn(Colour.BLACK, Square.a7, Square.a6);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a6-a5")));
   }

   @Test
   public void captureLeft() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Pawn opponentsPawn = new Pawn(Colour.WHITE, Square.a4, Square.b4);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsPawn, opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b5xa4")));
   }

   @Test
   public void captureRight() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Pawn opponentsPawn = new Pawn(Colour.WHITE, Square.a3, Square.b3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsPawn, opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a4xb3")));
   }

   @Test
   public void promotion() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a2);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game),
            new HashSet<>(Arrays.asList("a2-a1=Q+", "a2-a1=B", "a2-a1=N", "a2-a1=R+")));
   }

   @Test
   public void enpassantRight() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      game.getChessboard().setEnpassantSquare(Square.b3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("a4-a3", "a4xb3")));
   }

   @Test
   public void enpassantLeft() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b4);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      game.getChessboard().setEnpassantSquare(Square.a3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b4-b3", "b4xa3")));
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      Pawn pawn = new Pawn(Colour.BLACK, Square.b4, Square.d4);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      // don't need to set up the white pawns; it's enough to set the enpassant square
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      game.getChessboard().setEnpassantSquare(Square.c3);
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("b4-b3", "b4xc3", "d4-d3", "d4xc3")));
   }

   @Test
   public void checkLeft() {
      King king = new King(Colour.WHITE, Square.c3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king));
      Pawn pawn = new Pawn(Colour.BLACK, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d4+")));
   }

   @Test
   public void checkRight() {
      King king = new King(Colour.WHITE, Square.e3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king));
      Pawn pawn = new Pawn(Colour.BLACK, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d4+")));
   }

   @Test
   public void checkCaptureLeft() {
      King king = new King(Colour.WHITE, Square.b3);
      Pawn opponentsPawn = new Pawn(Colour.WHITE, Square.c4);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king, opponentsPawn));

      Pawn pawn = new Pawn(Colour.BLACK, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5-d4", "d5xc4+")));
   }

   @Test
   public void checkCaptureRight() {
      King king = new King(Colour.WHITE, Square.f3);
      Pawn opponentsPawn = new Pawn(Colour.WHITE, Square.e4, Square.d4);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(king, opponentsPawn));
      Pawn pawn = new Pawn(Colour.BLACK, Square.d5);
      Set<Piece> myPieces = new HashSet<>(Arrays.asList(pawn));
      Game game = new Game(new Chessboard(opponentsPieces, myPieces));
      TestUtil.checkMoves(pawn.findMoves(game), new HashSet<>(Arrays.asList("d5xe4+")));
   }
}
