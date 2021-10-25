package org.rjo.newchess.move;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.CheckInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class CastlingMovesTest {

   @Test
   public void whitekingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      // no rook on h1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
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
      p.setKingInCheck(new CheckInfo(Piece.ROOK, Square.h1.index()));
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      // no rook on a1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
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
   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on h8
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
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
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on a8
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.c8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.d8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());
   }
}
