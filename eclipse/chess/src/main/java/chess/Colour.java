package chess;

public enum Colour {

   White, Black;

   public static Colour oppositeColour(Colour colour) {
      switch (colour) {
      case White:
         return Colour.Black;
      case Black:
         return Colour.White;
      }
      throw new IllegalArgumentException("not possible");
   }
}
