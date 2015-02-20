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

public class KnightMoveTest {

   private King opponentsKing;

   @Before
   public void setup() {
      opponentsKing = new King(Colour.BLACK);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Knight whiteKnight = new Knight(Colour.WHITE);
      TestUtil.checkMoves(whiteKnight.findMoves(game),
            new HashSet<>(Arrays.asList("Nb1-a3", "Nb1-c3", "Ng1-f3", "Ng1-h3")));
   }

   @Test
   public void moveFromMiddleOfBoard() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.d4);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Pawn pawn = new Pawn(Colour.BLACK, Square.f3);
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil
            .checkMoves(
                  whiteKnight.findMoves(game),
                  new HashSet<>(Arrays.asList("Nd4-e6", "Nd4-f5", "Nd4xf3", "Nd4-e2", "Nd4-c2", "Nd4-b3", "Nd4-c6",
                        "Nd4-b5")));
   }

   @Test
   public void moveFromA1() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na1-b3", "Na1-c2")));
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
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na1-b3")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA1WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a1);
      Pawn pawn = new Pawn(Colour.BLACK, Square.c2);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na1-b3", "Na1xc2")));
   }

   @Test
   public void moveFromH1() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h1);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh1-g3", "Nh1-f2")));
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
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh1-g3")));
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
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh1xg3")));
   }

   @Test
   public void moveFromA8() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na8-b6", "Na8-c7+")));
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
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na8-b6")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromA8WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.a8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.c7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Na8-b6", "Na8xc7+")));
   }

   @Test
   public void moveFromH8() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h8);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Game game = new Game(new Chessboard(whitePieces, new HashSet<>(Arrays.asList(opponentsKing))));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh8-g6", "Nh8-f7")));
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
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh8-g6")));
   }

   /**
    * blocked by enemy pieces
    */
   @Test
   public void moveFromH8WithCapture() {
      Knight whiteKnight = new Knight(Colour.WHITE, Square.h8);
      Pawn pawn = new Pawn(Colour.BLACK, Square.f7);
      Set<Piece> whitePieces = new HashSet<>(Arrays.asList(whiteKnight));
      Set<Piece> opponentsPieces = new HashSet<>(Arrays.asList(pawn, opponentsKing));
      Game game = new Game(new Chessboard(whitePieces, opponentsPieces));
      TestUtil.checkMoves(whiteKnight.findMoves(game), new HashSet<>(Arrays.asList("Nh8-g6", "Nh8xf7")));
   }

}
