package org.rjo.newchess.piece;

public enum Colour {
   WHITE, BLACK, UNOCCUPIED;

   public boolean opposes(Colour other) {
      return (this == WHITE && other == BLACK) || (this == BLACK && other == WHITE);
   }

   public static Colour convert(int val) {
      switch (val) {
      case 0:
         return Colour.WHITE;
      case 1:
         return Colour.BLACK;
      case 2:
         return Colour.UNOCCUPIED;
      default:
         throw new IllegalArgumentException("invalid Colour value: '" + val + "'");
      }
   }

}
