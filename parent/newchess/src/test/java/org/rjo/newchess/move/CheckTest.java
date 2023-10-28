package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Perft;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class CheckTest {

   @ParameterizedTest
   @CsvSource({ "BISHOP,d8", "ROOK,d5", "KNIGHT,b3", "PAWN,b6" })
   public void whiteKingCannotMoveIntoCheck(String piece, String square) {
      Piece pt = Piece.valueOf(piece);
      Square sq = Square.valueOf(square);

      Position p = Fen.decode("8/8/8/8/K1k5/8/8/8 w - - 0 1").getPosition();
      p.addPiece(Colour.BLACK, pt, sq);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ka4-a3");
   }

   @ParameterizedTest
   @CsvSource({ "PAWN,c7" })
   public void blackKingCannotMoveIntoCheck(String piece, String square) {
      Piece pt = Piece.valueOf(piece);
      Square sq = Square.valueOf(square);

      Position p = new Position(Square.e2, Square.e7);
      p.setSideToMove(Colour.BLACK);
      p.addPiece(Colour.WHITE, pt, sq);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "Ke7-e8", "Ke7-f8", "Ke7-f7", "Ke7-f6", "Ke7-e6", "Ke7-d6", "Ke7-d7");
   }

   @Test
   public void kingInCheckEnpassantPossible() {
      // taken from numpty4, after black's move f7-f5. Prior: 8/5p2/8/2k3P1/p3K3/8/1P6/8 b - - 0 10
      Position p = Fen.decode("8/8/8/2k2pP1/4K3/8/8/8 w - f6 0 10").getPosition();
      assertTrue(p.isKingInCheck());
      assertEquals(Square.f6, p.getEnpassantSquare());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "g5xf6 ep", "Ke4-e5", "Ke4xf5", "Ke4-f4", "Ke4-f3", "Ke4-e3", "Ke4-d3");
   }

   @Test
   public void kingInCheckPawnPinned() {
      // taken from "posn3", after white's move g2-g3. 8/8/8/KP5r/1R3p1k/6P1/8/8 b - - 0 0
      Position p = Fen.decode("8/8/8/KP5r/1R3p1k/6P1/8/8 b - - 0 0").getPosition();
      assertTrue(p.isKingInCheck());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "Kh4-g4", "Kh4-h3", "Kh4-g5", "Kh4xg3");
   }

   @Test
   public void pawnMoveAlongSameRayTowardsKingNoCheck() {
      // pawn move d7-d6 should not be check
      Position p = Fen.decode("3qk3/3p4/8/8/8/8/3K4/3Q4 b - - 0 6").getPosition();
      assertFalse(p.isKingInCheck());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d7-d6", "d7-d5");
   }

   @Test
   public void pawnMoveAlongSameRayAwayFromKingNoCheck() {
      // pawn move d4-d3 should not be check
      Position p = Fen.decode("4k3/8/8/3K4/3p4/8/8/3r4 b - - 0 6").getPosition();
      assertFalse(p.isKingInCheck());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d4-d3");
   }

   // see pawnMoveAlongSameRayTowardsKingNoCheck, but b/c of the capture it's not quite the same logic in
   // isKingInCheckAfterMove
   @Test
   public void pawnCaptureAlongSameRayNoCheck() {
      // capture d7xc6 should not be check
      Position p = Fen.decode("3kq3/3p4/2P5/8/K7/8/8/8 b - - 0 6").getPosition();
      assertFalse(p.isKingInCheck());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d7xc6", "d7-d6", "d7-d5");
   }

   @Test
   public void pawnPromotionCheck() {
      // capture b7xa8=B+ is check
      Position p = Fen.decode("n1n5/PPP5/2k5/8/8/8/4Kppp/5N1N w - -").getPosition();
      assertFalse(p.isKingInCheck());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "b7-b8=N+", "b7-b8=R", "b7-b8=B", "b7-b8=Q", "b7xc8=N",
            "b7xc8=B", "b7xc8=R", "b7xc8=Q", "b7xa8=N", "b7xa8=B+", "b7xa8=R", "b7xa8=Q+");
   }

   // our king is in check, which moves block?
   @Test
   public void kingInCheckMovesBlock() {
      Position p = Fen.decode("3r4/B3k3/R7/8/Q7/3K4/4N3/8 w - -").getPosition();
      assertTrue(p.isKingInCheck());
      // make sure correct moves are generated
      TestUtil.checkMoves(new MoveGenerator(true).findMoves(p, Colour.WHITE), "Ba7-d4", "Ra6-d6", "Qa4-d7+", "Qa4-d4", "Ne2-d4", "Kd3-c4", "Kd3-c3", "Kd3-c2",
            "Kd3-e4", "Kd3-e3");
      var NBR_ITERS = 100_000;
      var sw = StopWatch.createStarted();
      for (int i = 0; i < NBR_ITERS; i++) {
         List<IMove> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
         assertEquals(10, moves.size(), "found moves: " + moves);
      }
      System.out.println("kingInCheckMovesBlock: " + sw.getTime());
   }

   @Test
   public void doubleCheckCache() {
      // taken from doubleCheckB, original: 8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1
      // after black's move ne4-d2: -
      Position p = Fen.decode("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1").getPosition();
      assertFalse(p.isKingInCheck());
      IMove m = new CheckMove(Move.createMove(Square.f4, p.pieceAt(Square.f4), Square.e2), new PieceSquareInfo(Piece.QUEEN, Square.f5));
      Position p2 = p.move(m);
      Map<String, Integer> moveMap = Perft.findMoves(p2, Colour.WHITE, 3, 1);
      int moves = Perft.countMoves(moveMap);
      // should be 544, was 545: Kf2-g2=143 should be 142
      // assertEquals(544, moves, String.format("wrong nbr of moves at depth 3\nmoveMap: %s\n", moveMap));
      Position p3 = p2.move(Move.createMove(Square.f2, p2.pieceAt(Square.f2), Square.g2));
      System.out.println(p3);
      System.out.println(p3);
      moveMap = Perft.findMoves(p3, Colour.BLACK, 2, 1);
      moves = Perft.countMoves(moveMap);
      // should be 142, was 143.
      // assertEquals(142, moves, String.format("wrong nbr of moves at depth 2\nmoveMap: %s\n", moveMap));
      m = new CheckMove(Move.createMove(Square.f5, p3.pieceAt(Square.f5), Square.f2), new PieceSquareInfo(Piece.QUEEN, Square.f2));
      Position p4 = p3.move(m);
      System.out.println(p4);
      // fen 8/8/2k5/8/8/8/4nqK1/8 w - -
      moveMap = Perft.findMoves(p4, Colour.WHITE, 1, 1);
      moves = Perft.countMoves(moveMap);
      assertEquals(3, moves, String.format("wrong nbr of moves at depth 1\nmoveMap: %s\n", moveMap));
   }

}
