package org.rjo.newchess.piece;

public enum Colour {
   WHITE, BLACK, UNOCCUPIED;

   static {
      WHITE.opposite = BLACK;
      BLACK.opposite = WHITE;
   }

   private Colour opposite;

   public boolean opposes(Colour other) {
      return this.opposite == other;
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

   public Colour opposite() {
      return opposite;
   }

}
