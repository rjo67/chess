package org.rjo.chess.ray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.pieces.PieceType;

public class SouthWestRay extends BaseRay {

   private static SouthWestRay instance;
   private static final List<Integer>[] RAY_SQUARES;

   static {
      final int offset = -9;
      RAY_SQUARES = new List[64];
      for (int i = 0; i < 64; i++) {
         RAY_SQUARES[i] = new ArrayList<>(8);
         int startSquareIndex = i + offset;
         while ((startSquareIndex >= 0) && (startSquareIndex % 8 != 7)) {
            RAY_SQUARES[i].add(startSquareIndex);
            startSquareIndex += offset;
         }
      }
   }

   private SouthWestRay() {
      super(RayType.SOUTHWEST, true, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
   }

   public static SouthWestRay instance() {
      if (instance == null) {
         instance = new SouthWestRay();
      }
      return instance;
   }

   @Override
   public Iterator<Integer> squaresFrom(int startSquareIndex) {
      return RAY_SQUARES[startSquareIndex].iterator();
   }

}