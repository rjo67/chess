package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class CastlingMovesTest {

   @Test
   public void kingsCastlingMoveWhite() {
      IMove m = Move.KINGS_CASTLING_MOVE[Colour.WHITE.ordinal()];
      assertTrue(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.e1, Square.toSquare(m.getOrigin()));
      assertEquals(Square.g1, Square.toSquare(m.getTarget()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void kingsCastlingMoveBlack() {
      IMove m = Move.KINGS_CASTLING_MOVE[Colour.BLACK.ordinal()];
      assertTrue(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.e8, Square.toSquare(m.getOrigin()));
      assertEquals(Square.g8, Square.toSquare(m.getTarget()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void queensCastlingMoveWhite() {
      IMove m = Move.QUEENS_CASTLING_MOVE[Colour.WHITE.ordinal()];
      assertFalse(m.isKingssideCastling());
      assertTrue(m.isQueenssideCastling());
      assertEquals(Square.e1, Square.toSquare(m.getOrigin()));
      assertEquals(Square.c1, Square.toSquare(m.getTarget()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void queensCastlingMoveBlack() {
      IMove m = Move.QUEENS_CASTLING_MOVE[Colour.BLACK.ordinal()];
      assertFalse(m.isKingssideCastling());
      assertTrue(m.isQueenssideCastling());
      assertEquals(Square.e8, Square.toSquare(m.getOrigin()));
      assertEquals(Square.c8, Square.toSquare(m.getTarget()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void capturingRookDisallowsCastling() {
      // black to move, should change white's castling rights
      Position posn = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      Position p2 = posn.move(TestUtil.createCapture(Square.g3, Square.h1, posn.pieceAt(Square.h1)));
      assertFalse(p2.canCastleKingsside(Colour.WHITE));
      assertTrue(p2.canCastleQueensside(Colour.WHITE));

      posn = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      p2 = posn.move(TestUtil.createCapture(Square.d4, Square.a1, posn.pieceAt(Square.a1)));
      assertTrue(p2.canCastleKingsside(Colour.WHITE));
      assertFalse(p2.canCastleQueensside(Colour.WHITE));

      // white to move, should change blacks's castling rights
      posn = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = posn.move(TestUtil.createCapture(Square.e5, Square.h8, posn.pieceAt(Square.h8)));
      assertFalse(p2.canCastleKingsside(Colour.BLACK));
      assertTrue(p2.canCastleQueensside(Colour.BLACK));

      posn = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = posn.move(TestUtil.createCapture(Square.b7, Square.a8, posn.pieceAt(Square.a8)));
      assertTrue(p2.canCastleKingsside(Colour.BLACK));
      assertFalse(p2.canCastleQueensside(Colour.BLACK));

   }

   @Test
   public void whitekingssideCastlingNotAllowed() {
      // no castling rights
      Position posn = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      // no rook on h1
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void whitekingssideCastlingInterveningPiece() {
      var posn = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.g1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());

      posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.f1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void whitekingssideCastlingThroughCheckedSquare() {
      // bishop
      var posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.g2);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());

      // knight
      posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());

      // pawn
      posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.BLACK, Piece.PAWN, Square.g2);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());

      // pawn further away: castling is ok
      posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.BLACK, Piece.PAWN, Square.h3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling(), "O-O");

      // a piece of our own colour should be ignored
      posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling(), "O-O");
   }

   @Test
   public void whitekingssideCastlingWhilstInCheck() {
      // castling when in check is not allowed
      var posn = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      posn.setKingInCheck(new PieceSquareInfo(Piece.ROOK, Square.h1.index()));
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isKingssideCastling());
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      // no castling rights
      Position posn = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      // no rook on a1
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void whitequeenssideCastlingInterveningPiece() {
      var posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.b1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.c1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.d1);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void whitequeenssideCastlingThroughCheckedSquare() {
      var posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.a3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());

      // make sure a knight of our own colour is ignored
      posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling(), "O-O-O");

      posn = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      posn.addPiece(Colour.BLACK, Piece.PAWN, Square.c2);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.WHITE), (p, m) -> m.isQueenssideCastling());
   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      // no castling rights
      Position posn = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on h8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));
   }

   @Test
   public void blackkingssideCastlingInterveningPiece() {
      var posn = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.g8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.g1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.f8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.f1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void blackkingssideCastlingThroughCheckedSquare() {
      // bishop
      var posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.e6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());

      // knight
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.h6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());

      // pawn
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.g7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());
      // pawn
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.h7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());
      // pawn
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.f7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());
      // pawn
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.e7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling());

      // pawn further away: castling is ok
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.h6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling(), "O-O");

      // a piece of our own colour should be ignored
      posn = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.h6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling(), "O-O");
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      // no castling rights
      Position posn = new Position(new boolean[][] { { false, false }, { false, false } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      // no rook on a8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));
   }

   @Test
   public void blackqueenssideCastlingInterveningPiece() {
      var posn = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.b8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.c8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());

      posn = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.BLACK, Piece.BISHOP, Square.d8);
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      posn.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isKingssideCastling() || m.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void blackqueenssideCastlingThroughCheckedSquare() {
      // bishop
      var posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.BISHOP, Square.e6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling());

      // knight
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.KNIGHT, Square.d6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling());

      // pawn
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.b7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling());
      // pawn
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.c7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling());
      // pawn
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.d7);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling());

      // pawn further away: castling is ok
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.WHITE, Piece.PAWN, Square.e6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling(), "O-O-O");

      // a piece of our own colour should be ignored
      posn = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      posn.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      posn.addPiece(Colour.BLACK, Piece.KNIGHT, Square.a6);
      TestUtil.checkMoves(posn, new MoveGenerator().findMoves(posn, Colour.BLACK), (p, m) -> m.isQueenssideCastling(), "O-O-O");
   }

   @Test
   public void speedTestCheckIfCanCastleKingsSide() {
      var p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.g2);

      long start = System.currentTimeMillis();
      MoveGenerator movegen = new MoveGenerator();
      for (int i = 0; i < 10_000_000; i++) {
         assertFalse(movegen.canCastleKingsside(p, Colour.WHITE));
      }
      System.out.println("kingsside: " + (System.currentTimeMillis() - start)); // 10_000_000 times: 240-280ms
   }

   @Test
   public void speedTestCheckIfCanCastleQueensSide() {
      var p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.d2);

      long start = System.currentTimeMillis();
      MoveGenerator movegen = new MoveGenerator();
      for (int i = 0; i < 10_000_000; i++) {
         assertFalse(movegen.canCastleQueensside(p, Colour.WHITE));
      }
      System.out.println("queensside: " + (System.currentTimeMillis() - start)); // 10_000_000 times: 240-280ms
   }

}
