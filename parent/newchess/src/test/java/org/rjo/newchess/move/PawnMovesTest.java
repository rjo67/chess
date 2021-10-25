package org.rjo.newchess.move;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class PawnMovesTest {

   @Test
   public void pawnMovesWhiteNoCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "a2-a3", "a2-a4");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER);
   }

   @Test
   public void pawnMovesWhiteCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.b2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "b2-b3", "b2-b4", "b2xa3", "b2xc3");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d4);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.c5); // own colour
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.e5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d4-d5", "c5-c6", "d4xe5");
   }

   @Test
   public void pawnMovesBlackNoCapture() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "a7-a6", "a7-a5");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d5-d4");
   }

   @Test
   public void pawnMovesWhitePromotion() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.c8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q",
            // promotion capture moves
            "d7xc8=R", "d7xc8=N", "d7xc8=B", "d7xc8=Q");
   }

   @Test
   public void pawnMovesBlackPromotion() {
      Position p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q");

      p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q",
            // promotion capture moves
            "d2xc1=R", "d2xc1=N", "d2xc1=B", "d2xc1=Q");
   }

   // ----------------------------------------------- enpassant --------------------------------

   @Test
   public void pawnMovesWhiteEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.e5);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.g5);
      TestUtil.checkMoves(new MoveGenerator(false).findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "e5-e6", "e5xf6 ep", "g5-g6", "g5xf6 ep");
   }

   @Test
   public void pawnMovesBlackEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d4-d3", "d4xc3 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.e4);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.g4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "e4-e3", "e4xf3 ep", "g4-g3", "g4xf3 ep");
   }
}
