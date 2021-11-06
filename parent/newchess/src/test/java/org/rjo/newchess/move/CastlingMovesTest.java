package org.rjo.newchess.move;

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
   public void capturingRookDisallowsCastling() {
      // black to move, should change white's castling rights
      Position p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      Position p2 = p.move(Move.createCapture(Square.g3, p.raw(Square.g3), Square.h1, p.raw(Square.h1)));
      assertFalse(p2.canCastleKingsside(Colour.WHITE));
      assertTrue(p2.canCastleQueensside(Colour.WHITE));

      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R b KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.d4, p.raw(Square.d4), Square.a1, p.raw(Square.a1)));
      assertTrue(p2.canCastleKingsside(Colour.WHITE));
      assertFalse(p2.canCastleQueensside(Colour.WHITE));

      // white to move, should change blacks's castling rights
      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.e5, p.raw(Square.e5), Square.h8, p.raw(Square.h8)));
      assertFalse(p2.canCastleKingsside(Colour.BLACK));
      assertTrue(p2.canCastleQueensside(Colour.BLACK));

      p = Fen.decode("r3k2r/1B6/8/4B3/3b4/6n1/8/R3K2R w KQkq - 0 0").getPosition();
      p2 = p.move(Move.createCapture(Square.b7, p.raw(Square.b7), Square.a8, p.raw(Square.a8)));
      assertTrue(p2.canCastleKingsside(Colour.BLACK));
      assertFalse(p2.canCastleQueensside(Colour.BLACK));

   }

   @Test
   public void whitekingssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.g1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.f1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // castling through an attacked square not allowed
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // a piece of our own colour should be ignored
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling(), "O-O");

      // castling when in check is not allowed
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      p.setKingInCheck(new PieceSquareInfo(Piece.ROOK, Square.h1.index()));
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      // no rook on h1
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.b1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // castling through a checked square not allowed
      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // make sure a knight of our own colour is ignored
      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling(), "O-O-O");

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, Piece.PAWN, Square.c2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      // no rook on a1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.WHITE));
   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.g8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.g1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.f8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.f1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on h8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      // no castling rights
      Position p = new Position(new boolean[][] { { false, false }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.c8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.d8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      final Position p2 = new Position(new boolean[][] { { false, false }, { false, true } }, Square.e1, Square.e8);
      // no rook on a8
      p2.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      assertThrows(IllegalStateException.class, () -> new MoveGenerator().findMoves(p2, Colour.BLACK));

   }
}
