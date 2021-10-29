package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class PawnMovesTest {

   @Test
   public void pawnMovesWhiteNoCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.a2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "a2-a3", "a2-a4");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.a2);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER);
   }

   @Test
   public void pawnMovesWhiteCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d5);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.c6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.b2);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.a3);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.c3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "b2-b3", "b2-b4", "b2xa3", "b2xc3");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d4);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.c5); // own colour
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.e5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d4-d5", "c5-c6", "d4xe5");
   }

   @Test
   public void pawnMovesBlackNoCapture() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.a7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "a7-a6", "a7-a5");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d5-d4");
   }

   // ----------------------------------------------- promotion --------------------------------

   @Test
   public void pawnMovesWhitePromotion() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d7);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.c8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q",
            // promotion capture moves
            "d7xc8=R", "d7xc8=N", "d7xc8=B", "d7xc8=Q");
   }

   @Test
   public void pawnMovesBlackPromotion() {
      Position p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.d2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q");

      p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.d2);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q",
            // promotion capture moves
            "d2xc1=R", "d2xc1=N", "d2xc1=B", "d2xc1=Q");
   }

   // ----------------------------------------------- enpassant --------------------------------

   @Test
   public void pawnMovesWhiteEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c6);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f6);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.e5);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.g5);
      TestUtil.checkMoves(new MoveGenerator(false).findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "e5-e6", "e5xf6 ep", "g5-g6", "g5xf6 ep");
   }

   @Test
   public void pawnMovesBlackEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c3);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.d4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d4-d3", "d4xc3 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f3);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.e4);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.g4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "e4-e3", "e4xf3 ep", "g4-g3", "g4xf3 ep");
   }

   @Test
   public void enpassantPossibleButPawnPinned() {
      // taken from "posn3", after white's move g2-g4 (enpassant at g3 but black pawn is pinned)
      // this tests that the pawn move f4-f3 is ok, but f4xg3 ep is not allowed due to well-concealed pin.
      // (Need to ignore square g4 in the empty square calculation (movingpiece-->king))
      Position p = Fen.decode("8/8/8/KP5r/1R3pPk/8/8/8 b - g3 0 0").getPosition();
      assertFalse(p.isKingInCheck());
      assertEquals(Square.g3, p.getEnpassantSquare());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "Kh4-g5", "Kh4xg4", "Kh4-g3", "Kh4-h3", "Rh5-h6", "Rh5-h7", "Rh5-h8", "Rh5-g5",
            "Rh5-f5", "Rh5-e5", "Rh5-d5", "Rh5-c5", "Rh5xb5+", "f4-f3");
   }

   @Test
   public void enpassantPossibleButPawnPinned2() {
      // taken from "posn3", after white's move e2-e4 (enpassant at e3 but black pawn is pinned)
      // this tests that the pawn move f4-f3 is ok, but f4xe3 ep is not allowed due to well-concealed pin
      // (Need to ignore square e4 in the empty square calculation (ray-->movingpiece))
      Position p = Fen.decode("8/8/8/KP5r/1R2Pp1k/8/6P1/8 b - e3 0 0").getPosition();
      assertFalse(p.isKingInCheck());
      assertEquals(Square.e3, p.getEnpassantSquare());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "Kh4-g5", "Kh4-g4", "Kh4-g3", "Rh5-h6", "Rh5-h7", "Rh5-h8", "Rh5-g5", "Rh5-f5",
            "Rh5-e5", "Rh5-d5", "Rh5-c5", "Rh5xb5+", "f4-f3");
   }

}
