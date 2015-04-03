package org.rjo.chess;

import java.util.BitSet;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the updating of Chessboard::'internalStructures'.
 *
 * @author rich
 */
public class ChessboardTest {

   @Test
   public void noncaptureMove() {
      Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10");
      Move move = new Move(PieceType.KNIGHT, Colour.WHITE, Square.d2, Square.b3);

      long start = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
         game.getChessboard().updateStructures(move, false);
      }
      System.out.println("1E06 noncapture move: " + (System.currentTimeMillis() - start));
   }

   @Test
   public void captureMove() {
      Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10");
      Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.f4, Square.h6, PieceType.PAWN);

      long start = System.currentTimeMillis();
      for (int i = 0; i < 1000000; i++) {
         game.getChessboard().updateStructures(move, false);
      }
      System.out.println("1E06 capture move:" + (System.currentTimeMillis() - start));
   }

   @Test
   public void bishopCapture() {
      Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
      Chessboard cb = game.getChessboard();

      Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.f4, Square.h6, PieceType.PAWN);

      InternalState prevState = new InternalState(cb);
      game.move(move);
      InternalState newState = new InternalState(cb);

      BitSet expectedBishopsAndQueens = new BitSet(64);
      expectedBishopsAndQueens.set(14);
      expectedBishopsAndQueens.set(47);

      BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
      assertFalse(expectedEmptySquares.get(Square.f4.bitIndex()));
      expectedEmptySquares.set(Square.f4.bitIndex());

      BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
      assertTrue(expectedTotalPieces.get(Square.f4.bitIndex()));
      expectedTotalPieces.clear(Square.f4.bitIndex());

      BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
      assertTrue(expectedAllPiecesWhite.get(Square.f4.bitIndex()));
      assertFalse(expectedAllPiecesWhite.get(Square.h6.bitIndex()));
      expectedAllPiecesWhite.clear(Square.f4.bitIndex());
      expectedAllPiecesWhite.set(Square.h6.bitIndex());

      BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
      assertTrue(expectedAllPiecesBlack.get(Square.h6.bitIndex()));
      expectedAllPiecesBlack.clear(Square.h6.bitIndex());

      assertEquals(prevState.allRooksAndQueens[0], newState.allRooksAndQueens[0]);
      assertEquals(prevState.allRooksAndQueens[1], newState.allRooksAndQueens[1]);
      assertEquals(expectedBishopsAndQueens, newState.allBishopsAndQueens[0]);
      assertEquals(prevState.allBishopsAndQueens[1], newState.allBishopsAndQueens[1]);
      assertEquals(expectedEmptySquares, newState.emptySquares);
      assertEquals(expectedTotalPieces, newState.totalPieces);
      assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
      assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

      game.unmove(move);
      InternalState newState2 = new InternalState(cb);
      assertEquals(prevState, newState2);
   }

   @Test
   public void pawnCapture() {
      Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
      Chessboard cb = game.getChessboard();

      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.d5, PieceType.PAWN);

      InternalState prevState = new InternalState(cb);
      game.move(move);
      InternalState newState = new InternalState(cb);

      BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
      assertFalse(expectedEmptySquares.get(Square.e4.bitIndex()));
      assertFalse(expectedEmptySquares.get(Square.d5.bitIndex()));
      expectedEmptySquares.set(Square.e4.bitIndex());

      BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
      assertTrue(expectedTotalPieces.get(Square.e4.bitIndex()));
      assertTrue(expectedTotalPieces.get(Square.d5.bitIndex()));
      expectedTotalPieces.clear(Square.e4.bitIndex());

      BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
      assertTrue(expectedAllPiecesWhite.get(Square.e4.bitIndex()));
      assertFalse(expectedAllPiecesWhite.get(Square.d5.bitIndex()));
      expectedAllPiecesWhite.clear(Square.e4.bitIndex());
      expectedAllPiecesWhite.set(Square.d5.bitIndex());

      BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
      assertTrue(expectedAllPiecesBlack.get(Square.d5.bitIndex()));
      expectedAllPiecesBlack.clear(Square.d5.bitIndex());

      assertEquals(prevState.allRooksAndQueens[0], newState.allRooksAndQueens[0]);
      assertEquals(prevState.allRooksAndQueens[1], newState.allRooksAndQueens[1]);
      assertEquals(prevState.allBishopsAndQueens[0], newState.allBishopsAndQueens[0]);
      assertEquals(prevState.allBishopsAndQueens[1], newState.allBishopsAndQueens[1]);
      assertEquals(expectedEmptySquares, newState.emptySquares);
      assertEquals(expectedTotalPieces, newState.totalPieces);
      assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
      assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

      game.unmove(move);
      InternalState newState2 = new InternalState(cb);
      assertEquals(prevState, newState2);
   }

   @Test
   public void pawnPromotionToQueen() {
      Game game = Fen.decode("r3k2r/pP3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
      Chessboard cb = game.getChessboard();

      Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.b7, Square.a8, PieceType.ROOK);
      move.setPromotionPiece(PieceType.QUEEN);

      InternalState prevState = new InternalState(cb);
      game.move(move);
      InternalState newState = new InternalState(cb);

      BitSet expectedRooksAndQueensWhite = new BitSet(64);
      expectedRooksAndQueensWhite.set(Square.a1.bitIndex());
      expectedRooksAndQueensWhite.set(Square.h1.bitIndex());
      expectedRooksAndQueensWhite.set(Square.a8.bitIndex());
      BitSet expectedRooksAndQueensBlack = new BitSet(64);
      expectedRooksAndQueensBlack.set(Square.h8.bitIndex());

      BitSet expectedBishopsAndQueensWhite = new BitSet(64);
      expectedBishopsAndQueensWhite.set(Square.g2.bitIndex());
      expectedBishopsAndQueensWhite.set(Square.f4.bitIndex());
      expectedBishopsAndQueensWhite.set(Square.a8.bitIndex());

      BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
      assertFalse(expectedEmptySquares.get(Square.b7.bitIndex()));
      assertFalse(expectedEmptySquares.get(Square.a8.bitIndex()));
      expectedEmptySquares.set(Square.b7.bitIndex());

      BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
      assertTrue(expectedTotalPieces.get(Square.b7.bitIndex()));
      assertTrue(expectedTotalPieces.get(Square.a8.bitIndex()));
      expectedTotalPieces.clear(Square.b7.bitIndex());

      BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
      assertTrue(expectedAllPiecesWhite.get(Square.b7.bitIndex()));
      assertFalse(expectedAllPiecesWhite.get(Square.a8.bitIndex()));
      expectedAllPiecesWhite.clear(Square.b7.bitIndex());
      expectedAllPiecesWhite.set(Square.a8.bitIndex());

      BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
      assertTrue(expectedAllPiecesBlack.get(Square.a8.bitIndex()));
      expectedAllPiecesBlack.clear(Square.a8.bitIndex());

      assertEquals(expectedRooksAndQueensWhite, newState.allRooksAndQueens[0]);
      assertEquals(expectedRooksAndQueensBlack, newState.allRooksAndQueens[1]);
      assertEquals(expectedBishopsAndQueensWhite, newState.allBishopsAndQueens[0]);
      assertEquals(prevState.allBishopsAndQueens[1], newState.allBishopsAndQueens[1]);
      assertEquals(expectedEmptySquares, newState.emptySquares);
      assertEquals(expectedTotalPieces, newState.totalPieces);
      assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
      assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

      game.unmove(move);
      InternalState newState2 = new InternalState(cb);
      assertEquals(prevState, newState2);
   }

   @Test
   public void blackPawnCapturesEnpassant() {
      Game game = Fen.decode("r3k2r/pP3p2/5npp/n2p4/Pp1PPB2/6P1/3N1PBP/R3K2R b KQkq a3 0 10");
      Chessboard cb = game.getChessboard();

      Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.b4, Square.a3, PieceType.PAWN);
      move.setEnpassant(true);

      InternalState prevState = new InternalState(cb);
      game.move(move);
      InternalState newState = new InternalState(cb);

      BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
      assertFalse(expectedEmptySquares.get(Square.b4.bitIndex()));
      assertFalse(expectedEmptySquares.get(Square.a4.bitIndex()));
      expectedEmptySquares.set(Square.b4.bitIndex());
      expectedEmptySquares.set(Square.a4.bitIndex());
      expectedEmptySquares.clear(Square.a3.bitIndex());

      BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
      assertTrue(expectedTotalPieces.get(Square.b4.bitIndex()));
      assertTrue(expectedTotalPieces.get(Square.a4.bitIndex()));
      expectedTotalPieces.clear(Square.b4.bitIndex());
      expectedTotalPieces.clear(Square.a4.bitIndex());
      expectedTotalPieces.set(Square.a3.bitIndex());

      BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
      assertTrue(expectedAllPiecesWhite.get(Square.a4.bitIndex()));
      expectedAllPiecesWhite.clear(Square.a4.bitIndex());

      BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
      assertTrue(expectedAllPiecesBlack.get(Square.b4.bitIndex()));
      expectedAllPiecesBlack.clear(Square.b4.bitIndex());
      expectedAllPiecesBlack.set(Square.a3.bitIndex());

      assertEquals(prevState.allRooksAndQueens[0], newState.allRooksAndQueens[0]);
      assertEquals(prevState.allRooksAndQueens[1], newState.allRooksAndQueens[1]);
      assertEquals(prevState.allBishopsAndQueens[0], newState.allBishopsAndQueens[0]);
      assertEquals(prevState.allBishopsAndQueens[1], newState.allBishopsAndQueens[1]);
      assertEquals(expectedEmptySquares, newState.emptySquares);
      assertEquals(expectedTotalPieces, newState.totalPieces);
      assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
      assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

      game.unmove(move);
      InternalState newState2 = new InternalState(cb);
      assertEquals(prevState, newState2);
   }

   class InternalState {
      BitSet[] allBishopsAndQueens = new BitSet[2];
      BitSet[] allRooksAndQueens = new BitSet[2];
      BitSet emptySquares;
      BitSet allPiecesWhite;
      BitSet allPiecesBlack;
      BitSet totalPieces;

      InternalState(Chessboard cb) {
         allBishopsAndQueens[0] = cb.getAllBishopsAndQueens()[0].cloneBitSet();
         allBishopsAndQueens[1] = cb.getAllBishopsAndQueens()[1].cloneBitSet();
         allRooksAndQueens[0] = cb.getAllRooksAndQueens()[0].cloneBitSet();
         allRooksAndQueens[1] = cb.getAllRooksAndQueens()[1].cloneBitSet();
         emptySquares = cb.getEmptySquares().cloneBitSet();
         allPiecesWhite = cb.getAllPieces(Colour.WHITE).cloneBitSet();
         allPiecesBlack = cb.getAllPieces(Colour.BLACK).cloneBitSet();
         totalPieces = cb.getTotalPieces().cloneBitSet();
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof InternalState)) {
            return false;
         }
         InternalState other = (InternalState) obj;
         if (!this.allBishopsAndQueens[0].equals(other.allBishopsAndQueens[0])) {
            return false;
         }
         if (!this.allBishopsAndQueens[1].equals(other.allBishopsAndQueens[1])) {
            return false;
         }
         if (!this.allRooksAndQueens[0].equals(other.allRooksAndQueens[0])) {
            return false;
         }
         if (!this.allRooksAndQueens[1].equals(other.allRooksAndQueens[1])) {
            return false;
         }
         if (!this.emptySquares.equals(other.emptySquares)) {
            return false;
         }
         if (!this.allPiecesWhite.equals(other.allPiecesWhite)) {
            return false;
         }
         if (!this.allPiecesBlack.equals(other.allPiecesBlack)) {
            return false;
         }
         if (!this.totalPieces.equals(other.totalPieces)) {
            return false;
         }
         return true;
      }
   }
}
