package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public class WestRay extends BaseRay {

   private static WestRay instance;

   private WestRay() {
      super(new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
   }

   public static WestRay instance() {
      if (instance == null) {
         instance = new WestRay();
      }
      return instance;
   }

   @Override
   public Iterator<Integer> squaresFrom(Square startSquare) {
      return squaresFrom(startSquare.bitIndex());
   }

   @Override
   public Iterator<Integer> squaresFrom(int startSquareIndex) {
      return new Itr(startSquareIndex);
   }

   private class Itr implements Iterator<Integer> {
      final int offset = -1;
      private int startSquareIndex;

      public Itr(int startSquareIndex) {
         this.startSquareIndex = startSquareIndex;
      }

      @Override
      public boolean hasNext() {
         int nextSquareIndex = startSquareIndex + offset;
         if (nextSquareIndex < 0) {
            return false;
         }
         return nextSquareIndex % 8 != 7;
      }

      @Override
      public Integer next() {
         startSquareIndex += offset;
         return startSquareIndex;
      }

   }
}
