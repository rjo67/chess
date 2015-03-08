package org.rjo.chess.pieces;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the methods in Piece.
 *
 * @author rich
 */
public class PieceTest {

   @Test
   public void addPiece() {
      Queen queen = new Queen(Colour.WHITE, Square.b6);
      queen.addPiece(Square.a6);
      assertEquals(2, queen.getBitBoard().getBitSet().cardinality());
   }

   @Test
   public void removePiece() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a6);
      pawn.removePiece(Square.a6);
      assertTrue(pawn.getBitBoard().getBitSet().isEmpty());
   }

   @Test(expected = IllegalArgumentException.class)
   public void removeNonExistentPiece() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a5);

      pawn.removePiece(Square.a6);
   }

   @Test
   public void move() {
      Queen queen = new Queen(Colour.WHITE, Square.b6);
      queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.b6, Square.a6));
      assertFalse(queen.getBitBoard().getBitSet().get(Square.b6.bitIndex()));
      assertTrue(queen.getBitBoard().getBitSet().get(Square.a6.bitIndex()));
   }

   @Test(expected = IllegalArgumentException.class)
   public void badMoveNonExistingPiece() {
      Queen queen = new Queen(Colour.WHITE, Square.b6);
      queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.g6, Square.a6));
      assertFalse(queen.getBitBoard().getBitSet().get(Square.b6.bitIndex()));
      assertTrue(queen.getBitBoard().getBitSet().get(Square.a6.bitIndex()));
   }

   @Test
   public void promotionMove() {
      Pawn pawn = new Pawn(Colour.WHITE, Square.a7);
      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a7, Square.a8);
      move.setPromotionPiece(PieceType.BISHOP);
      pawn.move(move);
      assertTrue(pawn.getBitBoard().getBitSet().isEmpty());
   }

}
