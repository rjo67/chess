package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.pieces.PieceType;

public class NorthRay extends BaseRay {

   private static NorthRay instance;

   private NorthRay() {
      super(new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
   }

   public static NorthRay instance() {
      if (instance == null) {
         instance = new NorthRay();
      }
      return instance;
   }

   @Override
   public Iterator<Integer> squaresFrom(int startSquareIndex) {
      return new Itr(startSquareIndex);
   }

   private class Itr implements Iterator<Integer> {
      final int offset = 8;
      private int startSquareIndex;

      public Itr(int startSquareIndex) {
         this.startSquareIndex = startSquareIndex;
      }

      @Override
      public boolean hasNext() {
         return startSquareIndex + offset <= 63;
      }

      @Override
      public Integer next() {
         startSquareIndex += offset;
         return startSquareIndex;
      }

   }
}
