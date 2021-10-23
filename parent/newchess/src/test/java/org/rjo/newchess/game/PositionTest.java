package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class PositionTest {

   @Test
   public void emptySquares() {
      Position posn = new Position();
      for (int i = 0; i < 64; i++) {
         assertTrue(posn.isEmpty(i));
      }
   }

   @Test
   public void addPiece() {
      Position posn = new Position();
      int sq = 0;
      for (Colour col : new Colour[] { Colour.WHITE, Colour.BLACK }) {
         for (PieceType pt : PieceType.values()) {
            posn.addPiece(col, pt, sq);
            assertTrue(!posn.isEmpty(sq));
            assertEquals(col, posn.colourOfPieceAt(sq));
            assertEquals(pt, posn.pieceAt(sq));
            sq++;
         }
      }
   }

   @Test
   public void copyConstructor() {
      Position oldPosn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      oldPosn.setEnpassantSquare(Square.e6);
      oldPosn.setSideToMove(Colour.BLACK);

      Position newPosn = new Position(oldPosn);
      assertEquals(Square.e6, newPosn.getEnpassantSquare());
      assertEquals(Colour.BLACK, newPosn.getSideToMove());
      assertTrue(newPosn.canCastleKingsside(Colour.WHITE));
      assertFalse(newPosn.canCastleKingsside(Colour.BLACK));
      assertFalse(newPosn.canCastleQueensside(Colour.WHITE));
      assertTrue(newPosn.canCastleQueensside(Colour.BLACK));
      assertEquals(Square.e1.index(), newPosn.getKingsSquare(Colour.WHITE));
      assertEquals(Square.e8.index(), newPosn.getKingsSquare(Colour.BLACK));
      assertEquals(PieceType.KING, newPosn.pieceAt(Square.e1));
      assertEquals(Colour.WHITE, newPosn.colourOfPieceAt(Square.e1));
      assertEquals(PieceType.KING, newPosn.pieceAt(Square.e8));
      assertEquals(Colour.BLACK, newPosn.colourOfPieceAt(Square.e8));

      // the fields are just copied
      assertSame(oldPosn.kingsSquare, newPosn.kingsSquare);
      assertSame(oldPosn.castlingRights, newPosn.castlingRights);

      // board is shallow cloned, i.e. the board[] contents are the same
      assertNotSame(oldPosn.board, newPosn.board);
      assertSame(oldPosn.raw(Square.e1), newPosn.raw(Square.e1));
      // blank squares use the same object
      assertSame(oldPosn.raw(Square.a2), newPosn.raw(Square.a2));
   }

   @Test
   public void moveNonCapture() {
      Position posn = new Position(Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.ROOK, Square.b3);
      assertEquals("4k3/8/8/8/8/1R6/8/4K3 w - -", posn.getFen());

      Position posn2 = posn.move(Move.createMove(Square.b3, posn.raw(Square.b3), Square.b5));
      assertEquals("4k3/8/8/1R6/8/8/8/4K3 b - -", posn2.getFen());
      assertEquals(PieceType.ROOK, posn2.pieceAt(Square.b5));
      assertBoardClonedCorrectly(posn, posn2, Square.b3, Square.b5);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
      assertSame(posn.castlingRights, posn2.castlingRights);
   }

   @Test
   public void moveCapture() {
      Position posn = new Position(Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.BISHOP, Square.b3);
      posn.addPiece(Colour.BLACK, PieceType.QUEEN, Square.d5);
      assertEquals("4k3/8/8/3q4/8/1B6/8/4K3 w - -", posn.getFen());

      Position posn2 = posn.move(Move.createCapture(Square.b3, posn.raw(Square.b3), Square.d5, posn.raw(Square.d5)));
      assertEquals("4k3/8/8/3B4/8/8/8/4K3 b - -", posn2.getFen());
      assertEquals(PieceType.BISHOP, posn2.pieceAt(Square.d5));
      assertBoardClonedCorrectly(posn, posn2, Square.b3, Square.d5);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
      assertSame(posn.castlingRights, posn2.castlingRights);
   }

   @Test
   public void rookMoveLosesCastlingRights() {
      Position posn = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      posn.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      assertEquals("4k3/8/8/8/8/8/8/R3K2R w KQ -", posn.getFen());

      Position posn2 = posn.move(Move.createMove(Square.h1, posn.raw(Square.h1), Square.h2));
      assertEquals("4k3/8/8/8/8/8/7R/R3K3 b Q -", posn2.getFen());
      assertFalse(posn2.canCastleKingsside(Colour.WHITE));
      assertTrue(posn2.canCastleQueensside(Colour.WHITE));
      assertBoardClonedCorrectly(posn, posn2, Square.h1, Square.h2);
      assertCastlingrightsClonedCorrectly(posn, posn2);

      // now queensside rook moves
      posn2.setSideToMove(Colour.WHITE);
      Position posn3 = posn2.move(Move.createMove(Square.a1, posn2.raw(Square.a1), Square.a2));
      assertEquals("4k3/8/8/8/8/8/R6R/4K3 b - -", posn3.getFen());
      assertFalse(posn3.canCastleKingsside(Colour.WHITE));
      assertFalse(posn3.canCastleQueensside(Colour.WHITE));
      assertBoardClonedCorrectly(posn2, posn3, Square.a1, Square.a2);
      assertCastlingrightsClonedCorrectly(posn2, posn3);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
   }

   @Test
   public void whiteKingssideCastle() {
      Position posn = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      assertEquals("4k3/8/8/8/8/8/8/4K2R w KQ -", posn.getFen());

      Position posn2 = posn.move(Move.createKingssideCastlingMove(Square.e1.index(), posn.raw(Square.e1.index()), Colour.WHITE));
      assertEquals("4k3/8/8/8/8/8/8/5RK1 b Q -", posn2.getFen());
      assertEquals(PieceType.KING, posn2.pieceAt(Square.g1));
      assertEquals(PieceType.ROOK, posn2.pieceAt(Square.f1));
      assertTrue(posn2.isEmpty(Square.e1));
      assertTrue(posn2.isEmpty(Square.h1));
      assertFalse(posn2.canCastleKingsside(Colour.WHITE));
      assertTrue(posn2.canCastleQueensside(Colour.WHITE));
      assertBoardClonedCorrectly(posn, posn2, Square.e1, Square.f1, Square.g1, Square.h1);
      assertCastlingrightsClonedCorrectly(posn, posn2);
      assertKingsSquareClonedCorrectly(posn, posn2);
      assertEquals(Square.g1.index(), posn2.kingsSquare[Colour.WHITE.ordinal()]);
      assertEquals(Square.e8.index(), posn2.kingsSquare[Colour.BLACK.ordinal()]);
   }

   @Test
   public void whiteQueenssideCastle() {
      Position posn = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      assertEquals("4k3/8/8/8/8/8/8/R3K3 w KQ -", posn.getFen());

      Position posn2 = posn.move(Move.createQueenssideCastlingMove(Square.e1.index(), posn.raw(Square.e1.index()), Colour.WHITE));
      assertCastlingrightsClonedCorrectly(posn, posn2);
      assertEquals("4k3/8/8/8/8/8/8/2KR4 b K -", posn2.getFen());
      assertEquals(PieceType.KING, posn2.pieceAt(Square.c1));
      assertEquals(PieceType.ROOK, posn2.pieceAt(Square.d1));
      assertTrue(posn2.isEmpty(Square.e1));
      assertTrue(posn2.isEmpty(Square.a1));
      assertTrue(posn2.canCastleKingsside(Colour.WHITE));
      assertFalse(posn2.canCastleQueensside(Colour.WHITE));
      assertBoardClonedCorrectly(posn, posn2, Square.e1, Square.d1, Square.c1, Square.a1);
      assertCastlingrightsClonedCorrectly(posn, posn2);
      assertKingsSquareClonedCorrectly(posn, posn2);
      assertEquals(Square.c1.index(), posn2.kingsSquare[Colour.WHITE.ordinal()]);
      assertEquals(Square.e8.index(), posn2.kingsSquare[Colour.BLACK.ordinal()]);
   }

   @Test
   public void blackKingssideCastle() {
      Position posn = new Position(new boolean[][] { { false, false }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      posn.setSideToMove(Colour.BLACK);
      assertEquals("4k2r/8/8/8/8/8/8/4K3 b kq -", posn.getFen());

      Position posn2 = posn.move(Move.createKingssideCastlingMove(Square.e8.index(), posn.raw(Square.e8.index()), Colour.BLACK));
      assertEquals("5rk1/8/8/8/8/8/8/4K3 w q -", posn2.getFen());
      assertEquals(PieceType.KING, posn2.pieceAt(Square.g8));
      assertEquals(PieceType.ROOK, posn2.pieceAt(Square.f8));
      assertTrue(posn2.isEmpty(Square.e8));
      assertTrue(posn2.isEmpty(Square.h8));
      assertFalse(posn2.canCastleKingsside(Colour.BLACK));
      assertTrue(posn2.canCastleQueensside(Colour.BLACK));
      assertBoardClonedCorrectly(posn, posn2, Square.e8, Square.f8, Square.g8, Square.h8);
      assertCastlingrightsClonedCorrectly(posn, posn2);
      assertKingsSquareClonedCorrectly(posn, posn2);
      assertEquals(Square.e1.index(), posn2.kingsSquare[Colour.WHITE.ordinal()]);
      assertEquals(Square.g8.index(), posn2.kingsSquare[Colour.BLACK.ordinal()]);
   }

   @Test
   public void blackQueenssideCastle() {
      Position posn = new Position(new boolean[][] { { false, false }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      posn.setSideToMove(Colour.BLACK);
      assertEquals("r3k3/8/8/8/8/8/8/4K3 b kq -", posn.getFen());

      Position posn2 = posn.move(Move.createQueenssideCastlingMove(Square.e8.index(), posn.raw(Square.e8.index()), Colour.BLACK));
      assertEquals("2kr4/8/8/8/8/8/8/4K3 w k -", posn2.getFen());
      assertEquals(PieceType.KING, posn2.pieceAt(Square.c8));
      assertEquals(PieceType.ROOK, posn2.pieceAt(Square.d8));
      assertTrue(posn2.isEmpty(Square.e8));
      assertTrue(posn2.isEmpty(Square.a8));
      assertTrue(posn2.canCastleKingsside(Colour.BLACK));
      assertFalse(posn2.canCastleQueensside(Colour.BLACK));
      assertBoardClonedCorrectly(posn, posn2, Square.e8, Square.d8, Square.c8, Square.a8);
      assertCastlingrightsClonedCorrectly(posn, posn2);
      assertKingsSquareClonedCorrectly(posn, posn2);
      assertEquals(Square.e1.index(), posn2.kingsSquare[Colour.WHITE.ordinal()]);
      assertEquals(Square.c8.index(), posn2.kingsSquare[Colour.BLACK.ordinal()]);
   }

   @Test
   public void enpassant() {
      Position posn = new Position(Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.PAWN, Square.b2);
      posn.addPiece(Colour.BLACK, PieceType.PAWN, Square.c4);
      assertEquals("4k3/8/8/8/2p5/8/1P6/4K3 w - -", posn.getFen());

      // make pawn move, which sets the enpassant square in the position
      Position posn2 = posn.move(Move.createPawnTwoSquaresForwardMove(Square.b2, posn.raw(Square.b2), Square.b4));
      assertEquals("4k3/8/8/8/1Pp5/8/8/4K3 b - b3", posn2.getFen());
      assertEquals(PieceType.PAWN, posn2.pieceAt(Square.b4));
      assertBoardClonedCorrectly(posn, posn2, Square.b4, Square.b2);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
      assertSame(posn.castlingRights, posn2.castlingRights);
      assertEquals(Square.b3, posn2.getEnpassantSquare());

      Move epMove = Move.createEnpassantMove(Square.c4, posn2.raw(Square.c4), Square.b3, posn2.raw(Square.b3));
      assertEquals(Square.b4.index(), epMove.getSquareOfPawnCapturedEnpassant());
      Position posn3 = posn2.move(epMove);
      assertBoardClonedCorrectly(posn2, posn3, Square.b3, Square.b4, Square.c4);
      posn2 = null; // avoid typos referencing the wrong posn
      assertEquals("4k3/8/8/8/8/1p6/8/4K3 w - -", posn3.getFen());
      assertTrue(posn3.isEmpty(Square.b4));
      assertFalse(posn3.isEmpty(Square.b3));
      assertEquals(PieceType.PAWN, posn3.pieceAt(Square.b3));
      assertEquals(Colour.BLACK, posn3.colourOfPieceAt(Square.b3));
      assertNull(posn3.getEnpassantSquare()); // after move, the ep square should be null again
      assertSame(posn3.kingsSquare, posn3.kingsSquare);
      assertSame(posn3.castlingRights, posn3.castlingRights);
   }

   @Test
   public void promotionNonCapture() {
      Position posn = new Position(Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.PAWN, Square.c7);
      assertEquals("4k3/2P5/8/8/8/8/8/4K3 w - -", posn.getFen());

      Position posn2 = posn.move(Move.createPromotionMove(Square.c7, posn.raw(Square.c7), Square.c8, PieceType.QUEEN));
      assertEquals("2Q1k3/8/8/8/8/8/8/4K3 b - -", posn2.getFen());
      assertEquals(PieceType.QUEEN, posn2.pieceAt(Square.c8));
      assertEquals(Colour.WHITE, posn2.colourOfPieceAt(Square.c8));
      assertTrue(posn2.isEmpty(Square.c7));
      assertBoardClonedCorrectly(posn, posn2, Square.c7, Square.c8);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
      assertSame(posn.castlingRights, posn2.castlingRights);
   }

   @Test
   public void promotionCapture() {
      Position posn = new Position(Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, PieceType.PAWN, Square.c7);
      posn.addPiece(Colour.BLACK, PieceType.BISHOP, Square.b8);
      assertEquals("1b2k3/2P5/8/8/8/8/8/4K3 w - -", posn.getFen());

      Position posn2 = posn.move(Move.createPromotionCaptureMove(Square.c7, posn.raw(Square.c7), Square.b8, posn.raw(Square.b8), PieceType.KNIGHT));
      assertEquals("1N2k3/8/8/8/8/8/8/4K3 b - -", posn2.getFen());
      assertEquals(PieceType.KNIGHT, posn2.pieceAt(Square.b8));
      assertEquals(Colour.WHITE, posn2.colourOfPieceAt(Square.b8));
      assertTrue(posn2.isEmpty(Square.c7));
      assertBoardClonedCorrectly(posn, posn2, Square.c7, Square.b8);
      assertSame(posn.kingsSquare, posn2.kingsSquare);
      assertSame(posn.castlingRights, posn2.castlingRights);
   }

   private void assertBoardClonedCorrectly(Position oldPosn, Position newPosn, Square... squaresToCheck) {
      assertNotSame(oldPosn.board, newPosn.board);
      for (Square sq : squaresToCheck) {
         assertNotSame(oldPosn.raw(sq), newPosn.raw(sq), "square " + sq);
      }
   }

   private void assertCastlingrightsClonedCorrectly(Position oldPosn, Position newPosn) {
      assertNotSame(oldPosn.castlingRights, newPosn.castlingRights);
   }

   private void assertKingsSquareClonedCorrectly(Position oldPosn, Position newPosn) {
      assertNotSame(oldPosn.kingsSquare, newPosn.kingsSquare);
   }

}
