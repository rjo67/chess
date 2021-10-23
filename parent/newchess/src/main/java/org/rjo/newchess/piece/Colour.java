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

   public Colour opposite() {
      return opposite;
   }

}
