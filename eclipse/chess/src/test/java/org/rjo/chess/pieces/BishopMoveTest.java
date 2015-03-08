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

public class BishopMoveTest {

   private King opponentsKing;

   @Before
   public void setup() {
      opponentsKing = new King(Colour.BLACK, true);
   }

   @Test
   public void locations() {
      Bishop b = new Bishop(Colour.WHITE);
      b.initPosition();
      Square[] locn = b.getLocations();
      Set<Square> set = new HashSet<>(Arrays.asList(locn));
      assertTrue(set.contains(Square.c1));
      assertTrue(set.contains(Square.f1));
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Bishop whiteBishop = new Bishop(Colour.WHITE);
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>());
   }

   @Test
   public void moveFromMiddleOfBoard() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Pawn pawn = new Pawn(Colour.BLACK, Square.g7);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(
            whiteBishop.findMoves(game),
            new HashSet<>(Arrays.asList("Bd4-e5", "Bd4-f6", "Bd4xg7", "Bd4-c5", "Bd4-b6", "Bd4-a7", "Bd4-e3", "Bd4-f2",
                  "Bd4-g1", "Bd4-c3", "Bd4-b2", "Bd4-a1")));
   }

   @Test
   public void moveFromA1() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game),
            new HashSet<>(Arrays.asList("Ba1-b2", "Ba1-c3", "Ba1-d4", "Ba1-e5", "Ba1-f6", "Ba1-g7", "Ba1-h8")));
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromA1WithBlockade() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.WHITE, Square.d4);
      King king = new King(Colour.WHITE, Square.c3);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Ba1-b2")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA1WithCapture() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Ba1-b2", "Ba1-c3", "Ba1xd4")));
   }

   @Test
   public void moveFromH1() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game),
            new HashSet<>(Arrays.asList("Bh1-g2", "Bh1-f3", "Bh1-e4", "Bh1-d5", "Bh1-c6+", "Bh1-b7", "Bh1-a8")));
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromH1WithBlockade() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h1);
      Pawn pawn = new Pawn(Colour.WHITE, Square.e4);
      King king = new King(Colour.WHITE, Square.f1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Bh1-g2", "Bh1-f3")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromH1WithCapture() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.e4);
      King king = new King(Colour.WHITE, Square.f1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteBishop));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Bh1-g2", "Bh1-f3", "Bh1xe4")));
   }

   @Test
   public void moveFromA8() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game),
            new HashSet<>(Arrays.asList("Ba8-b7", "Ba8-c6+", "Ba8-d5", "Ba8-e4", "Ba8-f3", "Ba8-g2", "Ba8-h1")));
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromA8WithBlockade() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.WHITE, Square.d5);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Ba8-b7", "Ba8-c6+")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA8WithCapture() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.d5);
      King king = new King(Colour.WHITE, Square.c8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteBishop));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Ba8-b7", "Ba8-c6+", "Ba8xd5")));
   }

   @Test
   public void moveFromH8() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game),
            new HashSet<>(Arrays.asList("Bh8-g7", "Bh8-f6", "Bh8-e5", "Bh8-d4", "Bh8-c3", "Bh8-b2", "Bh8-a1")));
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromH8WithBlockade() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.WHITE, Square.e5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(pawn, king, whiteBishop));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Bh8-g7", "Bh8-f6")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromH8WithCapture() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.e5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteBishop));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteBishop.findMoves(game), new HashSet<>(Arrays.asList("Bh8-g7", "Bh8-f6", "Bh8xe5")));
   }

   @Test
   public void attacksSquare() {
      Bishop whiteBishop = new Bishop(Colour.WHITE, Square.d8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.e5);
      King king = new King(Colour.WHITE, Square.f8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteBishop));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Chessboard chessboard = new Chessboard(whitePieces, opponentsPieces);
      for (Square sq : new Square[] { Square.c7, Square.b6, Square.a5, Square.e7, Square.f6, Square.g5, Square.h4 }) {
         assertTrue("square " + sq, whiteBishop.attacksSquare(chessboard, sq));
      }
      assertFalse(whiteBishop.attacksSquare(chessboard, Square.c4));
   }
}
