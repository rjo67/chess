package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

public class MoveTest {

   private static final byte WHITE_PAWN = Pieces.generatePawn(Colour.WHITE);
   private static final byte BLACK_PAWN = Pieces.generatePawn(Colour.BLACK);

   @Test
   public void move() {
      IMove m = TestUtil.createMove(Square.a5, WHITE_PAWN, Square.a6);
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void capture() {
      IMove m = TestUtil.createCapture(Square.a5, WHITE_PAWN, Square.b6, BLACK_PAWN);
      assertTrue(m.isCapture());
      assertFalse(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void enpassant() {
      Move m = Move.createEnpassantMove(Square.b5.index(), Square.c6.index(), Colour.WHITE);
      assertTrue(m.isCapture());
      assertTrue(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.c5.index(), m.getSquareOfPawnCapturedEnpassant());

      m = Move.createEnpassantMove(Square.b4.index(), Square.a3.index(), Colour.BLACK);
      assertTrue(m.isCapture());
      assertTrue(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.a4.index(), m.getSquareOfPawnCapturedEnpassant());
   }

   @ParameterizedTest
   @CsvSource({ "WHITE,e1,g1,true,false", "BLACK,e8,g8,true,false", "WHITE,e1,c1,false,true", "BLACK,e8,c8,false,true" })
   public void castling(String colourStr, String originStr, String targetStr, String kingsside, String queensside) {
      Colour col = Colour.valueOf(colourStr);
      Square originSq = Square.valueOf(originStr);
      Square targetSq = Square.valueOf(targetStr);
      boolean kingsCastling = Boolean.parseBoolean(kingsside);
      boolean queensCastling = Boolean.parseBoolean(queensside);

      IMove m = kingsCastling ? Move.KINGS_CASTLING_MOVE[col.ordinal()] : Move.QUEENS_CASTLING_MOVE[col.ordinal()];
      assertEquals(originSq.index(), m.getOrigin());
      assertEquals(targetSq.index(), m.getTarget());
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertEquals(kingsCastling, m.isKingssideCastling());
      assertEquals(queensCastling, m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void promotion() {
      IMove m = Move.createPromotionMove(Square.e7.index(), Square.e8.index(), Pieces.generateQueen(null));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertTrue(m.isPromotion());
      assertEquals(Piece.QUEEN, Pieces.toPiece(m.getPromotedPiece()));
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void promotionCapture() {
      IMove m = Move.createPromotionCaptureMove(Square.e7.index(), Square.d8.index(), BLACK_PAWN, Pieces.generateRook(null));
      assertTrue(m.isCapture());
      assertFalse(m.isEnpassant());
      assertTrue(m.isPromotion());
      assertEquals(Piece.ROOK, Pieces.toPiece(m.getPromotedPiece()));
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void pawnTwoSquaresMove() {
      IMove m = MoveGenerator.pawnMoves[Colour.WHITE.ordinal()][Square.e2.index()].getNext()[0].getMove();
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertTrue(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }
}
