package chess;

public enum Colour {

   WHITE, BLACK;

   @Override
   public String toString() {
      return name().charAt(0) + name().substring(1).toLowerCase();
   }

   public static Colour oppositeColour(Colour colour) {
      switch (colour) {
      case WHITE:
         return Colour.BLACK;
      case BLACK:
         return Colour.WHITE;
      }
      throw new IllegalArgumentException("not possible");
   }
}
