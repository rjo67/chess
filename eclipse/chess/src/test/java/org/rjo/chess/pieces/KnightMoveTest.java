package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KnightMoveTest {

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
      Knight whiteKnight = new Knight(Colour.WHITE, true);
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nb1-a3", "Nb1-c3", "Ng1-f3", "Ng1-h3");
   }

   @Test
   public void moveFromMiddleOfBoard() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Pawn pawn = new Pawn(Colour.BLACK, Square.f3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nd4-e6", "Nd4-f5", "Nd4xf3", "Nd4-e2", "Nd4-c2", "Nd4-b3",
            "Nd4-c6", "Nd4-b5");
   }

   @Test
   public void moveFromA1() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na1-b3", "Na1-c2");
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromA1WithBlockade() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a1);
      King king = new King(Colour.WHITE, Square.c2);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na1-b3");
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA1WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.c2);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na1-b3", "Na1xc2");
   }

   @Test
   public void moveFromH1() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh1-g3", "Nh1-f2");
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromH1WithBlockade() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h1);
      King king = new King(Colour.WHITE, Square.f2);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh1-g3");
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromH1WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.g3);
      King king = new King(Colour.WHITE, Square.f2);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteKnight));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh1xg3");
   }

   @Test
   public void moveFromA8() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na8-b6", "Na8-c7+");
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromA8WithBlockade() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a8);
      King king = new King(Colour.WHITE, Square.c7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na8-b6");
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA8WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.c7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Na8-b6", "Na8xc7+");
   }

   @Test
   public void moveFromH8() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh8-g6", "Nh8-f7");
   }

   /**
    * blocked by own pieces
    */
   @Test
   public void moveFromH8WithBlockade() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h8);
      King king = new King(Colour.WHITE, Square.f7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(king, whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh8-g6");
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromH8WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.f7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight, myKing));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), "Nh8-g6", "Nh8xf7");
   }

   @Test
   public void discoveredCheck() {
      Game game = Fen.decode("8/8/8/1k4NR/8/4K3/8/8 w - - 0 10");
      List<Move> moves = game.findMoves(Colour.WHITE);
      TestUtil.checkMoves(moves, "Rh5-h6", "Rh5-h7", "Rh5-h8", "Rh5-h4", "Rh5-h3", "Rh5-h2", "Rh5-h1", "Ke3-d2",
            "Ke3-d3", "Ke3-d4", "Ke3-e2", "Ke3-e4", "Ke3-f2", "Ke3-f3", "Ke3-f4", "Ng5-h3+", "Ng5-f3+", "Ng5-e4+",
            "Ng5-e6+", "Ng5-f7+", "Ng5-h7+");
   }

   @Test
   public void attacksSquare() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(opponentsKing));
      Chessboard chessboard = new Chessboard(whitePieces, opponentsPieces);
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.c2));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.b3));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.b5));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.c6));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.e6));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.f5));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.f3));
      assertTrue(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.e2));
      assertFalse(whiteKnight.attacksSquare(chessboard.getEmptySquares().getBitSet(), Square.c5));
   }

}
