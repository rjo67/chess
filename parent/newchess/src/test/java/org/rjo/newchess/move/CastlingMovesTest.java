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
import org.rjo.newchess.piece.Pieces;

public class CastlingMovesTest {

   @Test
   public void kingsCastlingMoveWhite() {
      Move m = Move.createKingssideCastlingMove(Colour.WHITE);
      assertTrue(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.e1, Square.toSquare(m.getOrigin()));
      assertEquals(Square.g1, Square.toSquare(m.getTarget()));
      assertTrue(Pieces.isKing(m.getMovingPiece()));
      assertEquals(Colour.WHITE, Pieces.colourOf(m.getMovingPiece()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void kingsCastlingMoveBlack() {
      Move m = Move.createKingssideCastlingMove(Colour.BLACK);
      assertTrue(m.isKingssideCastling());
      assertFalse(m.isQueenssideCastling());
      assertEquals(Square.e8, Square.toSquare(m.getOrigin()));
      assertEquals(Square.g8, Square.toSquare(m.getTarget()));
      assertTrue(Pieces.isKing(m.getMovingPiece()));
      assertEquals(Colour.BLACK, Pieces.colourOf(m.getMovingPiece()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void queensCastlingMoveWhite() {
      Move m = Move.createQueenssideCastlingMove(Colour.WHITE);
      assertFalse(m.isKingssideCastling());
      assertTrue(m.isQueenssideCastling());
      assertEquals(Square.e1, Square.toSquare(m.getOrigin()));
      assertEquals(Square.c1, Square.toSquare(m.getTarget()));
      assertTrue(Pieces.isKing(m.getMovingPiece()));
      assertEquals(Colour.WHITE, Pieces.colourOf(m.getMovingPiece()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void queensCastlingMoveBlack() {
      Move m = Move.createQueenssideCastlingMove(Colour.BLACK);
      assertFalse(m.isKingssideCastling());
      assertTrue(m.isQueenssideCastling());
      assertEquals(Square.e8, Square.toSquare(m.getOrigin()));
      assertEquals(Square.c8, Square.toSquare(m.getTarget()));
      assertTrue(Pieces.isKing(m.getMovingPiece()));
      assertEquals(Colour.BLACK, Pieces.colourOf(m.getMovingPiece()));
      assertFalse(m.isCapture());
      assertFalse(m.isEnpassant());
   }

   @Test
   public void capturingRookDisallowsCastling() {
      // black to move, should change white's castling rights
      Position p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      Position p2 = p.move(Move.createCapture(Square.g3, p.pieceAt(Square.g3), Square.h1, p.pieceAt(Square.h1)));
      assertFalse(p2.canCastleKingsside(Colour.WHITE));
      assertTrue(p2.canCastleQueensside(Colour.WHITE));

      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.d4, p.pieceAt(Square.d4), Square.a1, p.pieceAt(Square.a1)));
      assertTrue(p2.canCastleKingsside(Colour.WHITE));
      assertFalse(p2.canCastleQueensside(Colour.WHITE));

      // white to move, should change blacks's castling rights
      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.e5, p.pieceAt(Square.e5), Square.h8, p.pieceAt(Square.h8)));
      assertFalse(p2.canCastleKingsside(Colour.BLACK));
      assertTrue(p2.canCastleQueensside(Colour.BLACK));

      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.b7, p.pieceAt(Square.b7), Square.a8, p.pieceAt(Square.a8)));
      assertTrue(p2.canCastleKingsside(Colour.BLACK));
      assertFalse(p2.canCastleQueensside(Colour.BLACK));

   }

   @Test
   public void whitekingssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      // no rook on h1
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void whitekingssideCastlingInterveningPiece() {
      var p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.g1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.f1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void whitekingssideCastlingThroughCheckedSquare() {
      // bishop
      var p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // knight
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // pawn
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // pawn further away: castling is ok
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling(), "O-O");

      // a piece of our own colour should be ignored
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling(), "O-O");
   }

   @Test
   public void whitekingssideCastlingWhilstInCheck() {
      // castling when in check is not allowed
      var p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.setKingInCheck(new PieceSquareInfo(Piece.ROOK, Square.h1.index()));
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      // no rook on a1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void whitequeenssideCastlingInterveningPiece() {
      var p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.b1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void whitequeenssideCastlingThroughCheckedSquare() {
      var p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // make sure a knight of our own colour is ignored
      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling(), "O-O-O");

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.c2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());
   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on h8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));
   }

   @Test
   public void blackkingssideCastlingInterveningPiece() {
      var p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.g8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.g1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.f8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.f1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void blackkingssideCastlingThroughCheckedSquare() {
      // bishop
      var p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.e6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());

      // knight
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.h6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());

      // pawn
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.g7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());
      // pawn
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.h7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());
      // pawn
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.f7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());
      // pawn
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.e7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling());

      // pawn further away: castling is ok
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.h6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling(), "O-O");

      // a piece of our own colour should be ignored
      p = new Position(new boolean[][] { { true, false }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.h6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling(), "O-O");
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      // no rook on a8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));
   }

   @Test
   public void blackqueenssideCastlingInterveningPiece() {
      var p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.c8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.d8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());
   }

   // castling through an attacked square not allowed
   @Test
   public void blackqueenssideCastlingThroughCheckedSquare() {
      // bishop
      var p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.e6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling());

      // knight
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.d6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling());

      // pawn
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.b7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling());
      // pawn
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.c7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling());
      // pawn
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling());

      // pawn further away: castling is ok
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.e6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling(), "O-O-O");

      // a piece of our own colour should be ignored
      p = new Position(new boolean[][] { { true, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.a6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isQueenssideCastling(), "O-O-O");
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
