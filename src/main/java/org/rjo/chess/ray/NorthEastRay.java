package org.rjo.chess.ray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.pieces.PieceType;

public class NorthEastRay extends BaseRay {

   private static NorthEastRay instance;
   private static final List<Integer>[] RAY_SQUARES;

   static {
      final int offset = 9;
      RAY_SQUARES = new List[64];
      for (int i = 0; i < 64; i++) {
         RAY_SQUARES[i] = new ArrayList<>(8);
         int startSquareIndex = i + offset;
         while ((startSquareIndex < 64) && (startSquareIndex % 8 != 0)) {
            RAY_SQUARES[i].add(startSquareIndex);
            startSquareIndex += offset;
         }
      }
   }

   private NorthEastRay() {
      super(RayType.NORTHEAST, true, new PieceType[] { PieceType.QUEEN, PieceType.BISHOP });
   }

   public static NorthEastRay instance() {
      if (instance == null) {
         instance = new NorthEastRay();
      }
      return instance;
   }

   @Override
   public Iterator<Integer> squaresFrom(int startSquareIndex) {
      return RAY_SQUARES[startSquareIndex].iterator();
   }

}