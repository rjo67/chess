package org.rjo.chess;

import java.util.EnumSet;

public class MyEnumSet {

   public static void main(String[] args) {
      EnumSet<Square> bs = EnumSet.noneOf(Square.class);
      System.out.println(bs);

      bs.add(Square.e5);
      System.out.println(bs);
   }
}
