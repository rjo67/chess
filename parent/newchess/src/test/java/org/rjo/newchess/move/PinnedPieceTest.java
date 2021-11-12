package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Game;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class PinnedPieceTest {

   // test exists in order to optimize the move generator removeMovesLeavingKingInCheckAlongRay
   @Test
   public void noPin() {
      Position p = Fen.decode("4k3/4r3/4R3/4P3/8/8/8/4K3 w - - 0 0").getPosition();
      TestUtil.checkMoves(new MoveGenerator(true).findMoves(p, Colour.WHITE), "Ke1-d1", "Ke1-d2", "Ke1-e2", "Ke1-f1", "Ke1-f2", "Re6-d6", "Re6-c6", "Re6-b6",
            "Re6-a6", "Re6-f6", "Re6-g6", "Re6-h6", "Re6xe7+");

      var NBR_ITERS = 100_000;
      var sw = StopWatch.createStarted();
      for (int i = 0; i < NBR_ITERS; i++) {
         List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
         assertEquals(13, moves.size(), "found moves: " + moves);
      }
      System.out.println("noPin: " + sw.getTime());
   }

   @Test
   public void simplePin() {
      Position p = new Position(Square.e2, Square.g8);
      // d2 pawn is pinned
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.d2);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.b2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d3", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3");
   }

   @Test
   public void pinnedPieceCanMoveAlongRay() {
      // a 'pinned' piece along ray N can still move in direction N
      Position p = new Position(Square.e2, Square.g8);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.e3);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.e7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-d3", "Ke2-e1", "Ke2-f1", "Ke2-f2", "Ke2-f3", "e3-e4");

      p = new Position(Square.e2, Square.h8);
      // d3 bishop is pinned apart from NW/SE ray
      p.addPiece(Colour.WHITE, Piece.BISHOP, Square.d3);
      p.addPiece(Colour.WHITE, Piece.PAWN, Square.b5); // pawn is not pinned
      p.addPiece(Colour.BLACK, Piece.QUEEN, Square.c4);
      p.addPiece(Colour.BLACK, Piece.BISHOP, Square.a6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3", "b5-b6",
            "b5xa6", "Bd3xc4");
   }

   @Test
   public void pinnedPieceCanMoveAlongOppositeRay() {
      // a 'pinned' piece along ray N can still move in direction N or in direction S
      // but in this case not along ray W or E
      Position p = new Position(Square.e2, Square.g8);
      p.addPiece(Colour.WHITE, Piece.ROOK, Square.e4);
      p.addPiece(Colour.BLACK, Piece.ROOK, Square.e7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-d3", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3",
            // along pin ray
            "Re4-e3", "Re4-e5", "Re4-e6", "Re4xe7");
   }

   @Test
   public void pawnMoveAlongRay() {
      Position p = Fen.decode("4k3/4r3/8/8/8/8/4P3/4K3 w - - 0 0").getPosition();
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke1-d1", "Ke1-d2", "Ke1-f1", "Ke1-f2", "e2-e3", "e2-e4");
   }

   @Test
   public void blockCheck() {
      Position p = Fen.decode("3r4/4k3/8/R7/4P3/3K4/1BN1P3/8 w - - 10 10").getPosition();
      assertTrue(p.isKingInCheck());
      assertEquals(1, p.getCheckSquares().size());
      assertTrue(TestUtil.squareIsCheckSquare(Square.d8, p.getCheckSquares()));
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Kd3-c3", "Kd3-c4", "Kd3-e3", "Bb2-d4", "Nc2-d4", "Ra5-d5");
   }

   @Test
   public void pinnedPiece() {
      Game game = Fen.decode("3r4/4k3/8/8/3RP3/3K4/8/8 w - - 10 10");
      var NBR_ITERS = 100000;
      var sw = StopWatch.createStarted();
      for (int i = 0; i < NBR_ITERS; i++) {
         List<Move> moves = new MoveGenerator().findMoves(game.getPosition(), Colour.WHITE);
         assertEquals(11, moves.size(), "found moves: " + moves);
      }
      System.out.println("pinnedPiece: " + sw.getTime());
   }

   @Test
   public void pinnedQueen() {
      Game game = Fen.decode("5K2/4Q3/8/2b1pQ2/8/8/k4r2/8 w - - 0 0");
      var NBR_ITERS = 100000;
      // make sure correct moves are generated
      TestUtil.checkMoves(new MoveGenerator().findMoves(game.getPosition(), Colour.WHITE), "Kf8-e8", "Kf8-g8", "Kf8-f7", "Kf8-g7", "Qe7-d6", "Qe7xc5", "Qf5-f6",
            "Qf5-f7+", "Qf5-f4", "Qf5-f3", "Qf5xf2+");
      var sw = StopWatch.createStarted();
      for (int i = 0; i < NBR_ITERS; i++) {
         List<Move> moves = new MoveGenerator().findMoves(game.getPosition(), Colour.WHITE);
         assertEquals(11, moves.size(), "found moves: " + moves);
      }
      System.out.println("pinnedQueen: " + sw.getTime());
   }
}
