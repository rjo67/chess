package org.rjo.newchess.piece;

public enum Colour {
   // this order is important: unoccupied=0 is useful for the default in
   // Position::board
   UNOCCUPIED, WHITE, BLACK;

   public boolean opposes(Colour other) {
      return (this == WHITE && other == BLACK) || (this == BLACK && other == WHITE);
   }

   public static Colour convert(int val) {
      switch (val) {
      case 0:
         return Colour.UNOCCUPIED;
      case 1:
         return Colour.WHITE;
      case 2:
         return Colour.BLACK;
      default:
         throw new IllegalArgumentException("invalid Colour value: '" + val + "'");
      }
   }

}
