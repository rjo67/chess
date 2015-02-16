package org.rjo.chess;

import java.util.BitSet;

public class Main {

   public static void main(String[] args) {

      Chessboard cb = new Chessboard();
      cb.debug();

      BitSet bs = new BitSet(64);
      bs.set(23);
      System.out.println(bs);

      long lo = bs.toLongArray()[0];
      bs = BitSet.valueOf(new long[] { (lo >>> 15) });

      System.out.println(bs);
   }

}
