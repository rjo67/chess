package org.rjo.chess.ray;

import java.util.Iterator;

import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public class SouthRay extends BaseRay {

   private static SouthRay instance;

   private SouthRay() {
      super(new PieceType[] { PieceType.QUEEN, PieceType.ROOK });
   }

   public static SouthRay instance() {
      if (instance == null) {
         instance = new SouthRay();
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
      final int offset = -8;
      private int startSquareIndex;

      public Itr(int startSquareIndex) {
         this.startSquareIndex = startSquareIndex;
      }

      @Override
      public boolean hasNext() {
         return startSquareIndex + offset >= 0;
      }

      @Override
      public Integer next() {
         startSquareIndex += offset;
         return startSquareIndex;
      }

   }
}
