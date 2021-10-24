package org.rjo.newchess.move;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.rjo.newchess.TestUtil;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Fen;
import org.rjo.newchess.game.Game;
import org.rjo.newchess.game.Position;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class MoveGeneratorTest {

   @Test
   public void startingPosn() {
      Game g = Game.create();
      List<Move> moves = new MoveGenerator().findMoves(g.getPosition(), Colour.WHITE);
      assertEquals(20, moves.size(), "error:" + moves);
   }

   @Test
   public void queenMoves() {
      Position p = new Position(Square.f2, Square.a7);
      p.addPiece(Colour.WHITE, PieceType.QUEEN, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.QUEEN_FILTER, "Qd1-c2", "Qd1-b3", "Qd1-a4+", "Qd1-d2", "Qd1-d3", "Qd1-d4+",
            "Qd1-d5", "Qd1-d6", "Qd1-d7+", "Qd1-d8", "Qd1-e2", "Qd1-f3", "Qd1-g4", "Qd1-h5", "Qd1-c1", "Qd1-b1", "Qd1-a1+", "Qd1-e1", "Qd1-f1", "Qd1-g1",
            "Qd1-h1");
   }

   @Test
   public void rookMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ROOK_FILTER, "Rd5-d6", "Rd5-d7", "Rd5-d8+", "Rd5-c5", "Rd5-b5+", "Rd5-a5",
            "Rd5-e5", "Rd5-f5", "Rd5-g5", "Rd5-h5", "Rd5-d4", "Rd5-d3", "Rd5-d2", "Rd5-d1");

      p = new Position(Square.a1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.b7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.ROOK_FILTER, "Rb7-b8+", "Rb7-a7", "Rb7-c7", "Rb7-d7", "Rb7-e7+", "Rb7-f7",
            "Rb7-g7", "Rb7-h7", "Rb7-b6", "Rb7-b5", "Rb7-b4", "Rb7-b3", "Rb7-b2", "Rb7-b1");

   }

   @Test
   public void bishopMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.BISHOP_FILTER, "Bd5-c6", "Bd5-b7", "Bd5-a8", "Bd5-e6", "Bd5-f7", "Bd5-g8",
            "Bd5-c4", "Bd5-b3", "Bd5-a2", "Bd5-e4", "Bd5-f3", "Bd5-g2", "Bd5-h1");

      p = new Position(Square.b1, Square.c8);
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.BISHOP_FILTER, "Ba1-b2", "Ba1-c3", "Ba1-d4", "Ba1-e5", "Ba1-f6", "Ba1-g7",
            "Ba1-h8");
   }

   @Test
   public void knightMoves() {
      Position p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.KNIGHT_FILTER, "Nd5-c7", "Nd5-e7", "Nd5-f6", "Nd5-f4", "Nd5-e3", "Nd5-c3",
            "Nd5-b4", "Nd5-b6");

      p = new Position(Square.b1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.KNIGHT_FILTER, "Na1-b3", "Na1-c2");
   }

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
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> move.getMovingPiece() == PieceType.KING || move.isKingssideCastling() || move.isQueenssideCastling(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O");
      // castling Q-side
      p = new Position(new boolean[][] { { false, true, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE),
            move -> move.getMovingPiece() == PieceType.KING || move.isKingssideCastling() || move.isQueenssideCastling(), //
            "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O-O");
   }

   @Test
   public void whitekingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      // no rook on h1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.g1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.f1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // castling through an attacked square not allowed
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, PieceType.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.g2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());

      // a piece of our own colour should be ignored
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.h3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling(), "O-O");

      // castling when in check is not allowed
      p = new Position(new boolean[][] { { true, false }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1);
      p.setKingInCheck(Square.h1.index());
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isKingssideCastling());
   }

   @Test
   public void whitequeensssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      // no rook on a1
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { false, false, }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.b1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      // intervening piece
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.d1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // castling through a checked square not allowed
      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, PieceType.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());

      // make sure a knight of our own colour is ignored
      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      p.addPiece(Colour.WHITE, PieceType.KNIGHT, Square.b3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling(), "O-O-O");

      p = new Position(new boolean[][] { { false, true }, { true, true } }, Square.e1, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), move -> move.isQueenssideCastling());
   }

   @Test
   public void blackkingssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on h8
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.g8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.g1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.h8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.f8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.h1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.f1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());
   }

   @Test
   public void blackqueenssideCastlingNotAllowed() {
      Position p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      // no rook on a8
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      // no castling rights
      p = new Position(new boolean[][] { { true, true }, { false, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.b8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.b1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.c8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());

      p = new Position(new boolean[][] { { true, true }, { true, false } }, Square.e1, Square.e8);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.a8);
      // intervening piece
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.d8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.a1); // added to avoid false negatives
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1); // added to avoid false negatives
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), move -> move.isKingssideCastling() || move.isQueenssideCastling());
   }

   @Test
   public void pawnMovesWhiteNoCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "a2-a3", "a2-a4");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.a2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER);
   }

   @Test
   public void pawnMovesWhiteEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.e5);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.g5);
      TestUtil.checkMoves(new MoveGenerator(false).findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "e5-e6", "e5xf6 ep", "g5-g6", "g5xf6 ep");
   }

   @Test
   public void pawnMovesBlackEnPassant() {
      Position p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.c3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d4-d3", "d4xc3 ep");

      p = new Position(Square.e1, Square.e8);
      p.setEnpassantSquare(Square.f3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.e4);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.g4);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "e4-e3", "e4xf3 ep", "g4-g3", "g4xf3 ep");
   }

   @Test
   public void pawnMovesWhiteCapture() {
      Position p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d5);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d5-d6", "d5xc6");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.b2);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a3);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.c3);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "b2-b3", "b2-b4", "b2xa3", "b2xc3");

      p = new Position(Square.e1, Square.e8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d4);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.c5); // own colour
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.e5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d4-d5", "c5-c6", "d4xe5");
   }

   @Test
   public void pawnMovesWhitePromotion() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d7);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.c8);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), TestUtil.PAWN_FILTER, "d7-d8=R", "d7-d8=N", "d7-d8=B", "d7-d8=Q",
            // promotion capture moves
            "d7xc8=R", "d7xc8=N", "d7xc8=B", "d7xc8=Q");
   }

   @Test
   public void pawnMovesBlackNoCapture() {
      Position p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.a7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "a7-a6", "a7-a5");

      p = new Position(Square.e1, Square.g6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d5);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d5-d4");
   }

   @Test
   public void pawnMovesBlackPromotion() {
      Position p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q");

      p = new Position(Square.b5, Square.h6);
      p.addPiece(Colour.BLACK, PieceType.PAWN, Square.d2);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.c1);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.BLACK), TestUtil.PAWN_FILTER, "d2-d1=R", "d2-d1=N", "d2-d1=B", "d2-d1=Q",
            // promotion capture moves
            "d2xc1=R", "d2xc1=N", "d2xc1=B", "d2xc1=Q");
   }

   // ------------------------------ 'pinned piece' tests

   @Test
   public void simplePin() {
      Position p = new Position(Square.e2, Square.g8);
      // d2 pawn is pinned
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.d2);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.b2);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d3", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3");

   }

   @Test
   public void pinnedPieceCanMoveAlongRay() {
      // a 'pinned' piece along ray N can still move in direction N
      Position p = new Position(Square.e2, Square.g8);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.e3);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.e7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-d3", "Ke2-e1", "Ke2-f1", "Ke2-f2", "Ke2-f3", "e3-e4");

      p = new Position(Square.e2, Square.h8);
      // d3 bishop is pinned apart from NW/SE ray
      p.addPiece(Colour.WHITE, PieceType.BISHOP, Square.d3);
      p.addPiece(Colour.WHITE, PieceType.PAWN, Square.b5); // pawn is not pinned
      p.addPiece(Colour.BLACK, PieceType.QUEEN, Square.c4);
      p.addPiece(Colour.BLACK, PieceType.BISHOP, Square.a6);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3", "b5-b6",
            "b5xa6", "Bd3xc4");
   }

   @Test
   public void pinnedPieceCanMoveAlongOppositeRay() {
      // a 'pinned' piece along ray N can still move in direction N or in direction S
      // but in this case not along ray W or E
      Position p = new Position(Square.e2, Square.g8);
      p.addPiece(Colour.WHITE, PieceType.ROOK, Square.e4);
      p.addPiece(Colour.BLACK, PieceType.ROOK, Square.e7);
      TestUtil.checkMoves(new MoveGenerator().findMoves(p, Colour.WHITE), "Ke2-d1", "Ke2-d2", "Ke2-d3", "Ke2-e1", "Ke2-e3", "Ke2-f1", "Ke2-f2", "Ke2-f3",
            // along pin ray
            "Re4-e3", "Re4-e5", "Re4-e6", "Re4xe7");
   }

   @Test
   @Disabled("enable when this part has been implemented")
   public void blockCheck() {
      Game game = Fen.decode("3r4/4k3/8/R7/4P3/3K4/1BN1P3/8 w - - 10 10");
      List<Move> moves = new MoveGenerator().findMoves(game.getPosition(), Colour.WHITE);
      assertEquals(6, moves.size(), "found moves: " + moves);
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

   // ------------------------------ tests for moves which leave the opponent's king in check
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
      List<Integer> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(1, checkSquares.size());
      assertEquals(Square.valueOf(moveTarget).index(), checkSquares.get(0));
   }

   @ParameterizedTest
   @CsvSource({ "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,e7,d8,e8", // discovered check from rook
         "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,f4,f5,h6", // discovered check from bishop
         "4RK2/4B3/1Q5B/2N5/5P2/1p2k3/8/8 w - - 0 1,c5,b7,b6", // discovered check from queen
   })
   public void kingInDiscoveredCheckAfterMove(String fen, String moveOrigin, String moveTarget, String checkSquare) {
      Position posn = Fen.decode(fen).getPosition();
      Move move = Move.createMove(Square.valueOf(moveOrigin), posn.raw(Square.valueOf(moveOrigin)), Square.valueOf(moveTarget));
      List<Integer> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(1, checkSquares.size());
      assertEquals(Square.valueOf(checkSquare).index(), checkSquares.get(0));
   }

   @ParameterizedTest
   @CsvSource({ "5K2/8/1Q5B/2N5/5P2/8/RB3k2/8 w - - 0 1,b2,d4,a2", // check from bishop, discovered check from rook
         "5K2/8/1Q5B/2N5/5P2/8/RB3k2/8 w - - 0 1,c5,d3,b6", // discovered check from knight
   })
   public void kingInDirectAndDiscoveredCheckAfterMove(String fen, String moveOrigin, String moveTarget, String discoverdCheckSquare) {
      Position posn = Fen.decode(fen).getPosition();
      Move move = Move.createMove(Square.valueOf(moveOrigin), posn.raw(Square.valueOf(moveOrigin)), Square.valueOf(moveTarget));
      List<Integer> checkSquares = new MoveGenerator().isKingInCheckAfterMove(posn, move, posn.getKingsSquare(Colour.BLACK), Colour.BLACK);
      assertEquals(2, checkSquares.size());
      assertTrue(checkSquares.contains(Square.valueOf(moveTarget).index()));
      assertTrue(checkSquares.contains(Square.valueOf(discoverdCheckSquare).index()));
   }
}