package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.game.Position.CheckInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class OpponentInCheckTest {

   @Test
   public void pawnMoveChecksKing() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.f3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "d6-d7+");
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.ONLY_CHECKS, "f3-f2+");
   }

   @Test
   public void pawnPromotionChecksKing() {
      Position p = new Position(Square.a1, Square.h8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.c7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "c7-c8=R+", "c7-c8=Q+");
      // [c7-c8=R+, c7-c8=N, c7-c8=B, c7-c8=Q+, Ka1-a2, Ka1-b1, Ka1-b2]
      var sw = StopWatch.createStarted();
      var NBR_ITERS = 300000;
      for (int i = 0; i < NBR_ITERS; i++) {
         List<Move> moves = new MoveGenerator().findMoves(p, Colour.WHITE);
         assertEquals(7, moves.size(), "found moves: " + moves);
      }
      System.out.println("pawnPromotionChecksKing: " + sw.getTime()); // ~ 1160
   }

   @Test
   public void knightMoveChecksKing() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.b7);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.e4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "Nb7-d6+", "Ne4-d6+", "Ne4-f6+");
   }

   @Test
   public void bishopMoveChecksKing() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.e4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "Be4-c6+", "Be4-g6+");
   }

   @Test
   public void rookMoveChecksKing() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "Ra7-a8+", "Ra7-e7+");
   }

   @Test
   public void queenMoveChecksKing() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.QUEEN, Square.b6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ONLY_CHECKS, "Qb6-b8+", "Qb6-d8+", "Qb6-e6+", "Qb6-g6+", "Qb6-b5+",
            "Qb6-c6+", "Qb6-e3+");
   }

   @ParameterizedTest
   @CsvSource({ "3r4/4k3/8/8/3RP3/3K4/8/8 w - - 10 10,d4,d7", // rook
         "3r4/8/5k2/8/3RP3/3K4/8/8 w - - 10 10,e4,e5", // pawn
         "3r4/8/5k2/8/3RP3/3K4/7B/8 w - - 10 10,h2,e5", // bishop
         "3r4/8/5k2/1Q6/3RP3/3K4/7B/8 w - - 10 10,b5,g5", // queen
         "3r4/2N5/5k2/1Q6/3RP3/3K4/7B/8 w - - 10 10,c7,e8", // knight
   })
   public void kingInDirectCheckAfterMove(String fen, String moveOrigin, String moveTarget) {
      Position posn = Fen.decode(fen).getPosition();
      Move move = Move.createMove(Square.valueOf(moveOrigin), posn.raw(Square.valueOf(moveOrigin)), Square.valueOf(moveTarget));
      List<CheckInfo> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(1, checkSquares.size());
      assertTrue(TestUtil.squareIsCheckSquare(Square.valueOf(moveTarget), checkSquares));
   }

   @ParameterizedTest
   @CsvSource({ "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,e7,d8,e8", // discovered check from rook
         "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,f4,f5,h6", // discovered check from bishop
         "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,c5,b7,b6", // discovered check from queen
   })
   public void kingInDiscoveredCheckAfterMove(String fen, String moveOrigin, String moveTarget, String checkSquare) {
      Position posn = Fen.decode(fen).getPosition();
      Move move = Move.createMove(Square.valueOf(moveOrigin), posn.raw(Square.valueOf(moveOrigin)), Square.valueOf(moveTarget));
      List<CheckInfo> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(1, checkSquares.size());
      assertTrue(TestUtil.squareIsCheckSquare(Square.valueOf(checkSquare), checkSquares));
   }

   @ParameterizedTest
   @CsvSource({ "5K2/8/1Q5B/2N5/5P2/8/RB3k2/8 w - - 0 1,b2,d4,a2", // check from bishop, discovered check from rook
         "5K2/8/1Q5B/2N5/5P2/8/RB3k2/8 w - - 0 1,c5,d3,b6", // discovered check from knight
   })
   public void kingInDirectAndDiscoveredCheckAfterMove(String fen, String moveOrigin, String moveTarget, String discoverdCheckSquare) {
      Position posn = Fen.decode(fen).getPosition();
      Move move = Move.createMove(Square.valueOf(moveOrigin), posn.raw(Square.valueOf(moveOrigin)), Square.valueOf(moveTarget));
      List<CheckInfo> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(2, checkSquares.size());
      assertTrue(TestUtil.squareIsCheckSquare(Square.valueOf(moveTarget), checkSquares));
      assertTrue(TestUtil.squareIsCheckSquare(Square.valueOf(discoverdCheckSquare), checkSquares));
   }
}
