package org.rjo.newchess.game;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Pieces;

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
      g.posn.addPiece(Pieces.generateQueen(Colour.WHITE), Square.d1);
      g.posn.addPiece(Pieces.generateKing(Colour.WHITE), Square.e1);
      g.posn.addPiece(Pieces.generateBishop(Colour.WHITE), Square.c1);
      g.posn.addPiece(Pieces.generateBishop(Colour.WHITE), Square.f1);
      g.posn.addPiece(Pieces.generateKnight(Colour.WHITE), Square.b1);
      g.posn.addPiece(Pieces.generateKnight(Colour.WHITE), Square.g1);
      g.posn.addPiece(Pieces.generateRook(Colour.WHITE), Square.a1);
      g.posn.addPiece(Pieces.generateRook(Colour.WHITE), Square.h1);
      for (Square sq : new Square[] { Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2 }) {
         g.posn.addPiece(Pieces.generatePawn(Colour.WHITE), sq);
      }
      // black pieces
      g.posn.addPiece(Pieces.generateQueen(Colour.BLACK), Square.d8);
      g.posn.addPiece(Pieces.generateKing(Colour.BLACK), Square.e8);
      g.posn.addPiece(Pieces.generateBishop(Colour.BLACK), Square.c8);
      g.posn.addPiece(Pieces.generateBishop(Colour.BLACK), Square.f8);
      g.posn.addPiece(Pieces.generateKnight(Colour.BLACK), Square.b8);
      g.posn.addPiece(Pieces.generateKnight(Colour.BLACK), Square.g8);
      g.posn.addPiece(Pieces.generateRook(Colour.BLACK), Square.a8);
      g.posn.addPiece(Pieces.generateRook(Colour.BLACK), Square.h8);
      for (Square sq : new Square[] { Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7, Square.h7 }) {
         g.posn.addPiece(Pieces.generatePawn(Colour.BLACK), sq);
      }

      g.posn.setSideToMove(Colour.WHITE);
      g.posn.setCastlingRights(new boolean[][] { { true, true }, { true, true } });
      g.setHalfmoveClock(0);
      g.setMoveNumber(1);
      return g;
   }

   public Position getPosition() { return posn; }

   public void setHalfmoveClock(Integer halfmoves) { this.halfmoveClock = halfmoves; }

   public int getHalfmoveClock() { return halfmoveClock; }

   public void setMoveNumber(Integer fullmoves) { this.moveNumber = fullmoves; }

   public int getMoveNumber() { return moveNumber; }
}
