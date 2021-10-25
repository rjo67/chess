package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class CheckTest {

   @ParameterizedTest
   @CsvSource({ "BISHOP,d8", "ROOK,d5", "KNIGHT,b3", "PAWN,b6" })
   public void whiteKingCannotMoveIntoCheck(String piece, String square) {
      PieceType pt = PieceType.valueOf(piece);
      Square sq = Square.valueOf(square);

      Position p = Fen.decode("8/8/8/8/K1k5/8/8/8 w - - 0 1").getPosition();
      p.addPiece(Colour.BLACK, pt, sq);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ka4-a3");
   }

   @ParameterizedTest
   @CsvSource({ "PAWN,c7" })
   public void blackKingCannotMoveIntoCheck(String piece, String square) {
      PieceType pt = PieceType.valueOf(piece);
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
   public void kingInDiscoveredCheckEnpassantPossible() {
      // taken from "posn3", after white's move g2-g3. 8/8/8/KP5r/1R3p1k/6P1/8/8 b - - 0 0
      Position p = Fen.decode("8/8/8/KP5r/1R3p1k/6P1/8/8 b - - 0 0").getPosition();
      assertTrue(p.isKingInCheck());
      // TODO fix this test when the concept of discovered check is stored in a Move
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), "Kh4-g4", "Kh4-h3", "Kh4-g5", "Kh4xg3");
   }

}
