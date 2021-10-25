package org.rjo.newchess.game;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class Game {

   private Position posn;
   private int halfmoveClock;
   private int moveNumber;

   private Game() {
      this(new Position());
   }

   public Game(Position posn) {
      this.posn = posn;
   }

   /**
    * creates a new 'game' with the normal starting position.
    */
   public static Game create() {
      Game g = new Game();

      // white pieces
      g.posn.addPiece(Colour.WHITE, Piece.QUEEN, Square.d1);
      g.posn.addPiece(Colour.WHITE, Piece.KING, Square.e1);
      g.posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.c1);
      g.posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.f1);
      g.posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.b1);
      g.posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.g1);
      g.posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      g.posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      for (Square sq : new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2 }) {
         g.posn.addPiece(Colour.WHITE, Piece.PAWN, sq);
      }
      // black pieces
      g.posn.addPiece(Colour.BLACK, Piece.QUEEN, Square.d8);
      g.posn.addPiece(Colour.BLACK, Piece.KING, Square.e8);
      g.posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.c8);
      g.posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.f8);
      g.posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.b8);
      g.posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.g8);
      g.posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      g.posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      for (Square sq : new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7 }) {
         g.posn.addPiece(Colour.BLACK, Piece.PAWN, sq);
      }

      g.posn.setSideToMove(Colour.WHITE);
      g.posn.setCastlingRights(new boolean[][] { { true, true }, { true, true } });
      g.setHalfmoveClock(0);
      g.setMoveNumber(1);
      return g;
   }

   public Position getPosition() {
      return posn;
   }

   public void setHalfmoveClock(Integer halfmoves) {
      this.halfmoveClock = halfmoves;
   }

   public int getHalfmoveClock() {
      return halfmoveClock;
   }

   public void setMoveNumber(Integer fullmoves) {
      this.moveNumber = fullmoves;
   }

   public int getMoveNumber() {
      return moveNumber;
   }
}
