package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Game;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.move.MoveGenerator.MoveNode;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

public class StandardMovesTest {

   @Test
   public void startingPosn() {
      Game g = Game.create();
      List<IMove> moves = new MoveGenerator().findMoves(g.getPosition(), Colour.WHITE);
      assertEquals(20, moves.size(), "error:" + moves);
   }

   @Test
   public void queenMoves() {
      Position p = new Position(Square.f2, Square.a7);
      p.addPiece(Colour.WHITE, Piece.QUEEN, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.QUEEN_FILTER, "Qd1-c2", "Qd1-b3", "Qd1-a4+", "Qd1-d2", "Qd1-d3", "Qd1-d4+",
            "Qd1-d5", "Qd1-d6", "Qd1-d7+", "Qd1-d8", "Qd1-e2", "Qd1-f3", "Qd1-g4", "Qd1-h5", "Qd1-c1", "Qd1-b1", "Qd1-a1+", "Qd1-e1", "Qd1-f1", "Qd1-g1",
            "Qd1-h1");
   }

   @Test
   public void slidingMoveNodes() {
      MoveNode rookMove = MoveGenerator.moveNodes[Piece.ROOK.ordinal()][Square.c2.index()];
      do {
         System.out.println("c2-" + Square.toSquare(rookMove.getTo()));
         rookMove = rookMove.next[0];
      } while (rookMove != null);
   }

   @Test
   public void rookMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ROOK_FILTER, "Rd5-d6", "Rd5-d7", "Rd5-d8+", "Rd5-c5", "Rd5-b5+", "Rd5-a5",
            "Rd5-e5", "Rd5-f5", "Rd5-g5", "Rd5-h5", "Rd5-d4", "Rd5-d3", "Rd5-d2", "Rd5-d1");

      p = new Position(Square.a1, Square.e8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.b7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ROOK_FILTER, "Rb7-b8+", "Rb7-a7", "Rb7-c7", "Rb7-d7", "Rb7-e7+", "Rb7-f7",
            "Rb7-g7", "Rb7-h7", "Rb7-b6", "Rb7-b5", "Rb7-b4", "Rb7-b3", "Rb7-b2", "Rb7-b1");

      // same again, with some extra pieces
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.e7);
      p.addPiece(Colour.BLACK, Piece.QUEEN, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ROOK_FILTER, "Rb7-b8+", "Rb7-a7", "Rb7-c7", "Rb7-d7", "Rb7-b6", "Rb7-b5",
            "Rb7-b4", "Rb7xb3");

   }

   @Test
   public void bishopMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.BISHOP_FILTER, "Bd5-c6", "Bd5-b7", "Bd5-a8", "Bd5-e6", "Bd5-f7", "Bd5-g8",
            "Bd5-c4", "Bd5-b3", "Bd5-a2", "Bd5-e4", "Bd5-f3", "Bd5-g2", "Bd5-h1");

      p = new Position(Square.b1, Square.c8);
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.BISHOP_FILTER, "Ba1-b2", "Ba1-c3", "Ba1-d4", "Ba1-e5", "Ba1-f6", "Ba1-g7",
            "Ba1-h8");
   }

   @Test
   public void knightMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.KNIGHT_FILTER, "Nd5-c7", "Nd5-e7", "Nd5-f6", "Nd5-f4", "Nd5-e3", "Nd5-c3",
            "Nd5-b4", "Nd5-b6");

      p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.KNIGHT, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.KNIGHT_FILTER, "Na1-b3", "Na1-c2");
   }

   @Test
   public void kingMoves() {
      Position p = new Position(Square.d5, Square.b8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Kd5-c6", "Kd5-d6", "Kd5-e6", "Kd5-c5", "Kd5-e5", "Kd5-c4", "Kd5-d4", "Kd5-e4");

      p = new Position(Square.a1, Square.b8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ka1-a2", "Ka1-b1", "Ka1-b2");

      // king adjacent to other king
      p = new Position(Square.d6, Square.e8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Kd6-c5", "Kd6-c6", "Kd6-c7", "Kd6-d5", "Kd6-e6", "Kd6-e5");

      // castling
      p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> Pieces.isKing(move.getMovingPiece()) || move.isKingssideCastling() || move.isQueenssideCastling(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O");
      // castling Q-side
      p = new Position(new boolean[][] { { false, true, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> Pieces.isKing(move.getMovingPiece()) || move.isKingssideCastling() || move.isQueenssideCastling(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O-O");
   }

}
