package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class MoveGeneratorTest {

   @Test
   public void queenMoves() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.QUEEN, Square.d1);
      List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(21, moves.size(), "error:" + moves);
   }

   @Test
   public void rookMoves() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.d5);
      List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(14, moves.size(), "error:" + moves);

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.b7);
      moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(14, moves.size(), "error:" + moves);
   }

   @Test
   public void bishopMoves() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.d5);
      List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(13, moves.size(), "error:" + moves);

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.a1);
      moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(7, moves.size(), "error:" + moves);
   }

   @Test
   public void knightMoves() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.d5);
      List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(8, moves.size(), "error:" + moves);

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.a1);
      moves = new MoveGenerator().findMoves(p, Colour.WHITE);
      assertEquals(2, moves.size(), "error:" + moves);
   }

   @Test
   public void kingMoves() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.KING, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Kd5-c6", "Kd5-d6", "Kd5-e6", "Kd5-c5", "Kd5-e5", "Kd5-c4", "Kd5-d4", "Kd5-e4");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.KING, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ka1-a2", "Ka1-b1", "Ka1-b2");

      // castling
      p = new Position(new boolean[][] { { true, false, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> move.getMovingPiece() == PieceType.KING || move.isKingssideCastle() || move.isQueenssideCastle(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O");
      // castling Q-side
      p = new Position(new boolean[][] { { false, true, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> move.getMovingPiece() == PieceType.KING || move.isKingssideCastle() || move.isQueenssideCastle(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O-O");
   }

   @Test
   public void whitekingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, false, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      // no rook on h1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, false, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.g1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, false }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.f1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { false, true }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      // no rook on a1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { false, true }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.b1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { false, true }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { false, true }, { true, true } });
      p.addPiece(Colour.WHITE, PieceType.KING, Square.e1);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastle() || move.isQueenssideCastle());

   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      // no rook on h8
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.g8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.g1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.f8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.f1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      // no rook on a8
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.c8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());

      p = new Position(new boolean[][] { { true, true }, { true, false } });
      p.addPiece(Colour.BLACK, PieceType.KING, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.d8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastle() || move.isQueenssideCastle());
   }

   @Test
   public void pawnMovesWhiteNoCapture() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "d5-d6");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "a2-a3", "a2-a4");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE));
   }

   @Test
   public void pawnMovesWhiteCapture() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "d5-d6", "d5xc6");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.b2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "b2-b3", "b2-b4", "b2xa3", "b2xc3");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d4);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.c5); // own colour
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.e5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "d4-d5", "c5-c6", "d4xe5");
   }

   @Test
   public void pawnMovesWhitePromotion() {
      Position p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q");

      p = new Position();
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.c8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q",
            // promotion capture moves
            "d7xc8=R", "d7xc8=N", "d7xc8=B", "d7xc8=Q");
   }

   @Test
   public void pawnMovesBlackNoCapture() {
      Position p = new Position();
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "a7-a6", "a7-a5");

      p = new Position();
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "d5-d4");
   }

   @Test
   public void pawnMovesBlackPromotion() {
      Position p = new Position();
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q");

      p = new Position();
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q",
            // promotion capture moves
            "d2xc1=R", "d2xc1=N", "d2xc1=B", "d2xc1=Q");
   }
}
