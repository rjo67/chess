package org.rjo.chess;

import java.util.BitSet;
import java.util.Random;

import org.junit.Test;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayInfo;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.ray.RayUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RayTest {

   @Test
   public void emptySquares() {
      Game game = Fen.decode("r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w - - 0 0");
      int[] expectedEmptySquares = new int[] { 9, 5, 11, 10, 12, 11, 13, 4, 10, 14, 11, 12, 11, 19, 15, 17, 5, 7, 10,
            18, 14, 15, 17, 13, 7, 15, 15, 14, 16, 19, 18, 11, 13, 13, 17, 15, 18, 19, 22, 15, 10, 17, 15, 19, 17, 23,
            17, 17, 14, 12, 15, 17, 14, 17, 19, 13, 6, 10, 12, 12, 15, 13, 11, 8 };
      long start = System.currentTimeMillis();
      for (int i = 0; i < expectedEmptySquares.length; i++) {
         int nbrEmptySquares = 0;
         for (RayType ray : RayType.values()) {
            RayInfo info = RayUtils.findFirstPieceOnRay(Colour.BLACK, game.getChessboard().getEmptySquares()
                  .getBitSet(), game.getChessboard().getAllPieces(Colour.WHITE).getBitSet(), ray.getInstance(), i);
            // System.out.println(ray + " " + info);
            nbrEmptySquares += info.getEmptySquares().size();
         }
         assertEquals("bad sq " + Square.fromBitIndex(i), expectedEmptySquares[i], nbrEmptySquares);
      }
      System.out.println("emptySquares: " + (System.currentTimeMillis() - start));

   }

   @Test
   public void discoveredCheck() {
      Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
      // now move: Be4-f5 (discovered check)
      // need to manipulate chessboard to remove the bishop at e4
      BitSet emptySquares = game.getChessboard().getEmptySquares().getBitSet();
      emptySquares.set(Square.e4.bitIndex());
      BitSet myPieces = game.getChessboard().getAllPieces(Colour.BLACK).getBitSet();
      myPieces.clear(Square.e4.bitIndex());
      assertTrue(RayUtils.discoveredCheck(Colour.BLACK, game.getChessboard(), emptySquares, myPieces, Square.e1,
            Square.e4));
   }

   @Test
   public void discoveredCheck2() {
      Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R w - - 0 0");
      // now move: b5-b6 (discovered check)
      // need to manipulate chessboard to remove the pawn at b5
      BitSet emptySquares = game.getChessboard().getEmptySquares().getBitSet();
      emptySquares.set(Square.b5.bitIndex());
      BitSet myPieces = game.getChessboard().getAllPieces(Colour.WHITE).getBitSet();
      myPieces.clear(Square.b5.bitIndex());
      assertTrue(RayUtils.discoveredCheck(Colour.WHITE, game.getChessboard(), emptySquares, myPieces, Square.e8,
            Square.b5));
   }

   @Test
   public void noDiscoveredCheck() {
      Game game = Fen.decode("r3k2r/p3n2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
      // now move: Be4-f5 (not discovered check)
      // need to manipulate chessboard to remove the bishop at e4
      BitSet emptySquares = game.getChessboard().getEmptySquares().getBitSet();
      emptySquares.set(Square.e4.bitIndex());
      BitSet myPieces = game.getChessboard().getAllPieces(Colour.BLACK).getBitSet();
      myPieces.clear(Square.e4.bitIndex());
      assertFalse(RayUtils.discoveredCheck(Colour.BLACK, game.getChessboard(), emptySquares, myPieces, Square.e1,
            Square.e4));
   }

   private static boolean onSameDiagonal(Square sq1, Square sq2) {
      return Math.abs(sq1.rank() - sq2.rank()) == Math.abs(sq1.file() - sq2.file());
   }

   protected static boolean attacksSquareDiagonally(BitSet emptySquares, Square startSquare, Square targetSquare) {
      // give up straight away if start and target are the same
      if (startSquare == targetSquare) {
         return false;
      }
      if (!onSameDiagonal(startSquare, targetSquare)) {
         return false;
      }
      int rankOffset = startSquare.rank() > targetSquare.rank() ? -1 : 1;
      int fileOffset = startSquare.file() > targetSquare.file() ? -1 : 1;
      int bitPosn = startSquare.bitIndex();
      boolean reachedTargetSquare = false;
      boolean foundNonEmptySquare = false;
      while (!reachedTargetSquare && !foundNonEmptySquare) {
         bitPosn += (8 * rankOffset) + fileOffset;
         if (bitPosn == targetSquare.bitIndex()) {
            reachedTargetSquare = true;
         } else if (!emptySquares.get(bitPosn)) {
            foundNonEmptySquare = true;
         }
      }
      return reachedTargetSquare;
   }

   @Test
   public void speedOfGetRay() {
      long start = System.currentTimeMillis();
      int cnt = 0;
      Random rnd = new Random();
      for (int i = 0; i < 1000000; i++) {
         Ray ray = RayUtils.getRay(Square.fromBitIndex(rnd.nextInt(64)), Square.fromBitIndex(rnd.nextInt(64)));
         if (ray != null) {
            cnt++;
         }
      }
      System.out.println("getRay: " + cnt + ", " + (System.currentTimeMillis() - start));
   }

   @Test
   public void speed() {
      BitSet emptySquares = BitSet.valueOf(new long[] { -1 });
      BitSet myPieces = BitSet.valueOf(new long[] { 0 });
      Square startSquare = Square.a4;
      Square targetSquare = Square.d7;
      long start = System.currentTimeMillis();
      Ray ray = RayUtils.getRay(startSquare, targetSquare);
      for (int i = 0; i < 1000000; i++) {
         // attacksSquareDiagonally(emptySquares, startSquare, targetSquare);
         RayUtils.findFirstPieceOnRay(Colour.WHITE, emptySquares, myPieces, ray, startSquare.bitIndex());
      }
      System.out.println("speed: " + (System.currentTimeMillis() - start));
   }
}
