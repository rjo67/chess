package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.SquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class MoveTest {

   private static final SquareInfo WHITE_PAWN = new SquareInfo(PieceType.PAWN, Colour.WHITE);
   private static final SquareInfo BLACK_PAWN = new SquareInfo(PieceType.PAWN, Colour.BLACK);

   @Test
   public void move() {
      Move m = Move.createMove(Square.a5, WHITE_PAWN, Square.a6);
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
      Move m = Move.createCapture(Square.a5, WHITE_PAWN, Square.b6, BLACK_PAWN);
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
      Move m = Move.createEnpassantMove(Square.b5, WHITE_PAWN, Square.c6, WHITE_PAWN);
      assertTrue(m.isCapture());
      assertTrue(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.c5.index(), m.getSquareOfPawnCapturedEnpassant());

      m = Move.createEnpassantMove(Square.b4, BLACK_PAWN, Square.a3, BLACK_PAWN);
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

      Move m = kingsCastling ? Move.createKingssideCastlingMove(col) : Move.createQueenssideCastlingMove(col);
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
      Move m = Move.createPromotionMove(Square.e7, WHITE_PAWN, Square.e8, PieceType.QUEEN);
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertTrue(m.isPromotion());
      assertEquals(PieceType.QUEEN, m.getPromotedPiece());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void promotionCapture() {
      Move m = Move.createPromotionCaptureMove(Square.e7, WHITE_PAWN, Square.d8, BLACK_PAWN, PieceType.ROOK);
      assertTrue(m.isCapture());
      assertFalse(m.isEnpassant());
      assertTrue(m.isPromotion());
      assertEquals(PieceType.ROOK, m.getPromotedPiece());
      assertFalse(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }

   @Test
   public void pawnTwoSquaresMove() {
      Move m = Move.createPawnTwoSquaresForwardMove(Square.e2, WHITE_PAWN, Square.e4);
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
      assertFalse(m.isPromotion());
      assertTrue(m.isPawnTwoSquaresForward());
      assertFalse(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(0, m.getSquareOfPawnCapturedEnpassant());
   }
}
