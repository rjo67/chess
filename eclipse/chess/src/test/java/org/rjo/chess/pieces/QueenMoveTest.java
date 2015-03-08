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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QueenMoveTest {

   private King opponentsKing;

   @Before
   public void setup() {
      opponentsKing = new King(Colour.BLACK, true);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Queen whiteQueen = new Queen(Colour.WHITE);
      TestUtil.checkMoves(whiteQueen.findMoves(game), new HashSet<>());
   }

   @Test
   public void moveFromMiddleOfBoard() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteQueen));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil
      .checkMoves(
            whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qd4-d5", "Qd4-d6", "Qd4-d7+", "Qd4-d8+", "Qd4-e4+", "Qd4-f4", "Qd4-g4",
                  "Qd4-h4", "Qd4-d3", "Qd4-d2", "Qd4-d1", "Qd4-c4", "Qd4-b4", "Qd4-a4+", "Qd4-e5+", "Qd4-f6",
                  "Qd4-g7", "Qd4-h8+", "Qd4-c5", "Qd4-b6", "Qd4-a7", "Qd4-e3+", "Qd4-f2", "Qd4-g1", "Qd4-c3",
                  "Qd4-b2", "Qd4-a1")));
   }

   @Test
   public void moveFromA1() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteQueen));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(
            whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa1-a2", "Qa1-a3", "Qa1-a4+", "Qa1-a5", "Qa1-a6", "Qa1-a7", "Qa1-a8+",
                  "Qa1-b1", "Qa1-c1", "Qa1-d1", "Qa1-e1+", "Qa1-f1", "Qa1-g1", "Qa1-h1", "Qa1-b2", "Qa1-c3", "Qa1-d4",
                  "Qa1-e5+", "Qa1-f6", "Qa1-g7", "Qa1-h8+")));
   }

   /**
    * queen gets blocked by own pieces
    */
   @Test
   public void moveFromA1WithBlockade() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.WHITE, Square.a4, Square.c3);
      King king = new King(Colour.WHITE, Square.c1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteQueen));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa1-a2", "Qa1-a3", "Qa1-b1", "Qa1-b2")));
   }

   /**
    * queen gets blocked by enemy pieces
    */
   @Test
   public void moveFromA1WithCapture() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.a4, Square.e5);
      King king = new King(Colour.WHITE, Square.c1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteQueen));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(
            whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa1-a2", "Qa1-a3", "Qa1xa4+", "Qa1-b1", "Qa1-b2", "Qa1-c3", "Qa1-d4",
                  "Qa1xe5+")));
   }

   @Test
   public void moveFromA8() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteQueen));
      opponentsKing = new King(Colour.BLACK, Square.c5);
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(
            whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa8-a7+", "Qa8-a6", "Qa8-a5+", "Qa8-a4", "Qa8-a3+", "Qa8-a2", "Qa8-a1",
                  "Qa8-b8", "Qa8-c8+", "Qa8-d8", "Qa8-e8", "Qa8-f8+", "Qa8-g8", "Qa8-h8", "Qa8-b7", "Qa8-c6+",
                  "Qa8-d5+", "Qa8-e4", "Qa8-f3", "Qa8-g2", "Qa8-h1")));
   }

   /**
    * queen gets blocked by own pieces
    */
   @Test
   public void moveFromA8WithBlockade() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.WHITE, Square.a5, Square.c6);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteQueen));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa8-a7", "Qa8-a6", "Qa8-b8", "Qa8-b7")));
   }

   /**
    * queen gets blocked by enemy pieces
    */
   @Test
   public void moveFromA8WithCapture() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.a5, Square.d5);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteQueen));
      opponentsKing = new King(Colour.BLACK, Square.c5);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteQueen.findMoves(game),
            new HashSet<>(Arrays.asList("Qa8-a7+", "Qa8-a6", "Qa8xa5+", "Qa8-b8", "Qa8-b7", "Qa8-c6+", "Qa8xd5+")));
   }

   @Test
   public void attacksSquareStraight() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.e7);
      Pawn pawn = new Pawn(Colour.BLACK, Square.e5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteQueen));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Chessboard chessboard = new Chessboard(whitePieces, opponentsPieces);
      for (Square sq : new Square[] { Square.e8, Square.e6, Square.d7, Square.c7, Square.b7, Square.a7, Square.f7,
            Square.g7, Square.h7 }) {
         assertTrue("square " + sq, whiteQueen.attacksSquare(chessboard, sq));
      }
      assertFalse(whiteQueen.attacksSquare(chessboard, Square.c4));
   }

   @Test
   public void attacksSquareDiag() {
      Queen whiteQueen = new Queen(Colour.WHITE, Square.d8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.e5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteQueen));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Chessboard chessboard = new Chessboard(whitePieces, opponentsPieces);
      for (Square sq : new Square[] { Square.c7, Square.b6, Square.a5, Square.e7, Square.f6, Square.g5, Square.h4 }) {
         assertTrue("square " + sq, whiteQueen.attacksSquare(chessboard, sq));
      }
      assertFalse(whiteQueen.attacksSquare(chessboard, Square.c4));
   }

}
