package org.rjo.newchess.piece;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.rjo.newchess.board.Ray;

public class PieceTest {

   @Test
   public void colourTest() {
      assertTrue(Pieces.isBlackPiece((byte) 0b00001111));
      assertTrue(Pieces.isWhitePiece((byte) 0b00001110));
      assertFalse(Pieces.isWhitePiece((byte) 0b00000000));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_PAWN, Pieces.WHITE_ROOK, Pieces.WHITE_KNIGHT, Pieces.WHITE_BISHOP, Pieces.WHITE_QUEEN, Pieces.WHITE_KING })
   public void whitePiece(byte piece) {
      assertTrue(Pieces.isWhitePiece(piece));
      assertEquals(Colour.WHITE, Pieces.colourOf(piece));
      assertFalse(Pieces.isBlackPiece(piece));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.BLACK_PAWN, Pieces.BLACK_ROOK, Pieces.BLACK_KNIGHT, Pieces.BLACK_BISHOP, Pieces.BLACK_QUEEN, Pieces.BLACK_KING })
   public void blackPiece(byte piece) {
      assertTrue(Pieces.isBlackPiece(piece));
      assertEquals(Colour.BLACK, Pieces.colourOf(piece));
      assertFalse(Pieces.isWhitePiece(piece));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_ROOK, Pieces.BLACK_ROOK, Pieces.WHITE_BISHOP, Pieces.BLACK_BISHOP, Pieces.WHITE_QUEEN, Pieces.BLACK_QUEEN })
   public void slidingPiece(byte piece) {
      assertTrue(Pieces.isSlidingPiece(piece));
      for (Ray ray : Ray.RAY_TYPES_DIAGONAL) {
         if (Pieces.isRook(piece)) {
            assertFalse(Pieces.canSlideAlongRay(piece, ray));
         } else {
            assertTrue(Pieces.canSlideAlongRay(piece, ray));
         }
      }
      for (Ray ray : Ray.RAY_TYPES_VERTICAL) {
         if (Pieces.isBishop(piece)) {
            assertFalse(Pieces.canSlideAlongRay(piece, ray));
         } else {
            assertTrue(Pieces.canSlideAlongRay(piece, ray));
         }
      }
      for (Ray ray : Ray.RAY_TYPES_HORIZONTAL) {
         if (Pieces.isBishop(piece)) {
            assertFalse(Pieces.canSlideAlongRay(piece, ray));
         } else {
            assertTrue(Pieces.canSlideAlongRay(piece, ray));
         }
      }
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_PAWN, Pieces.BLACK_PAWN, Pieces.WHITE_KNIGHT, Pieces.BLACK_KNIGHT, Pieces.WHITE_KING, Pieces.BLACK_KING })
   public void notSlidingPiece(byte piece) {
      assertFalse(Pieces.isSlidingPiece(piece));
      for (Ray ray : Ray.RAY_TYPES_DIAGONAL) {
         assertFalse(Pieces.canSlideAlongRay(piece, ray));
      }
      for (Ray ray : Ray.RAY_TYPES_VERTICAL) {
         assertFalse(Pieces.canSlideAlongRay(piece, ray));
      }
      for (Ray ray : Ray.RAY_TYPES_HORIZONTAL) {
         assertFalse(Pieces.canSlideAlongRay(piece, ray));
      }
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_PAWN, Pieces.BLACK_PAWN })
   public void pawnsCorrectlyIdentified(byte pawn) {
      assertTrue(Pieces.isPawn(pawn));
      assertFalse(Pieces.isRook(pawn));
      assertFalse(Pieces.isKnight(pawn));
      assertFalse(Pieces.isBishop(pawn));
      assertFalse(Pieces.isQueen(pawn));
      assertFalse(Pieces.isKing(pawn));
      assertFalse(Pieces.isRookOrQueen(pawn));
      assertFalse(Pieces.isBishopOrQueen(pawn));
      assertTrue(Pieces.isPawnOrKnight(pawn));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_ROOK, Pieces.BLACK_ROOK })
   public void rooksCorrectlyIdentified(byte rook) {
      assertFalse(Pieces.isPawn(rook));
      assertTrue(Pieces.isRook(rook));
      assertFalse(Pieces.isKnight(rook));
      assertFalse(Pieces.isBishop(rook));
      assertFalse(Pieces.isQueen(rook));
      assertFalse(Pieces.isKing(rook));
      assertTrue(Pieces.isRookOrQueen(rook));
      assertFalse(Pieces.isBishopOrQueen(rook));
      assertFalse(Pieces.isPawnOrKnight(rook));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_KNIGHT, Pieces.BLACK_KNIGHT })
   public void knightsCorrectlyIdentified(byte knight) {
      assertFalse(Pieces.isPawn(knight));
      assertFalse(Pieces.isRook(knight));
      assertTrue(Pieces.isKnight(knight));
      assertFalse(Pieces.isBishop(knight));
      assertFalse(Pieces.isQueen(knight));
      assertFalse(Pieces.isKing(knight));
      assertFalse(Pieces.isRookOrQueen(knight));
      assertFalse(Pieces.isBishopOrQueen(knight));
      assertTrue(Pieces.isPawnOrKnight(knight));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_BISHOP, Pieces.BLACK_BISHOP })
   public void bishopsCorrectlyIdentified(byte bishop) {
      assertFalse(Pieces.isPawn(bishop));
      assertFalse(Pieces.isRook(bishop));
      assertFalse(Pieces.isKnight(bishop));
      assertTrue(Pieces.isBishop(bishop));
      assertFalse(Pieces.isQueen(bishop));
      assertFalse(Pieces.isKing(bishop));
      assertTrue(Pieces.isBishopOrQueen(bishop));
      assertFalse(Pieces.isRookOrQueen(bishop));
      assertFalse(Pieces.isPawnOrKnight(bishop));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_QUEEN, Pieces.BLACK_QUEEN })
   public void queensCorrectlyIdentified(byte queen) {
      assertFalse(Pieces.isPawn(queen));
      assertFalse(Pieces.isRook(queen));
      assertFalse(Pieces.isKnight(queen));
      assertFalse(Pieces.isBishop(queen));
      assertTrue(Pieces.isQueen(queen));
      assertFalse(Pieces.isKing(queen));
      assertTrue(Pieces.isBishopOrQueen(queen));
      assertTrue(Pieces.isRookOrQueen(queen));
      assertFalse(Pieces.isPawnOrKnight(queen));
   }

   @ParameterizedTest
   @ValueSource(bytes = { Pieces.WHITE_KING, Pieces.BLACK_KING })
   public void kingsCorrectlyIdentified(byte king) {
      assertFalse(Pieces.isPawn(king));
      assertFalse(Pieces.isRook(king));
      assertFalse(Pieces.isKnight(king));
      assertFalse(Pieces.isBishop(king));
      assertFalse(Pieces.isQueen(king));
      assertTrue(Pieces.isKing(king));
      assertFalse(Pieces.isRookOrQueen(king));
      assertFalse(Pieces.isBishopOrQueen(king));
      assertFalse(Pieces.isPawnOrKnight(king));
   }

   @Test
   public void symbol() {
      assertEquals("", Pieces.symbol(Pieces.WHITE_PAWN));
      assertEquals("", Pieces.symbol(Pieces.BLACK_PAWN));
      assertEquals("R", Pieces.symbol(Pieces.WHITE_ROOK));
      assertEquals("R", Pieces.symbol(Pieces.BLACK_ROOK));
      assertEquals("N", Pieces.symbol(Pieces.WHITE_KNIGHT));
      assertEquals("N", Pieces.symbol(Pieces.BLACK_KNIGHT));
      assertEquals("B", Pieces.symbol(Pieces.WHITE_BISHOP));
      assertEquals("B", Pieces.symbol(Pieces.BLACK_BISHOP));
      assertEquals("Q", Pieces.symbol(Pieces.WHITE_QUEEN));
      assertEquals("Q", Pieces.symbol(Pieces.BLACK_QUEEN));
      assertEquals("K", Pieces.symbol(Pieces.WHITE_KING));
      assertEquals("K", Pieces.symbol(Pieces.BLACK_KING));
   }

   @Test
   public void fenSymbol() {
      assertEquals("P", Pieces.fenSymbol(Pieces.WHITE_PAWN));
      assertEquals("p", Pieces.fenSymbol(Pieces.BLACK_PAWN));
      assertEquals("R", Pieces.fenSymbol(Pieces.WHITE_ROOK));
      assertEquals("r", Pieces.fenSymbol(Pieces.BLACK_ROOK));
      assertEquals("N", Pieces.fenSymbol(Pieces.WHITE_KNIGHT));
      assertEquals("n", Pieces.fenSymbol(Pieces.BLACK_KNIGHT));
      assertEquals("B", Pieces.fenSymbol(Pieces.WHITE_BISHOP));
      assertEquals("b", Pieces.fenSymbol(Pieces.BLACK_BISHOP));
      assertEquals("Q", Pieces.fenSymbol(Pieces.WHITE_QUEEN));
      assertEquals("q", Pieces.fenSymbol(Pieces.BLACK_QUEEN));
      assertEquals("K", Pieces.fenSymbol(Pieces.WHITE_KING));
      assertEquals("k", Pieces.fenSymbol(Pieces.BLACK_KING));
   }

}
