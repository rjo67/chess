package org.rjo.chess;

/**
 * Represents the colours of the sides in the game.
 * 
 * @author rich
 */
public enum Colour {

   WHITE, BLACK;

   @Override
   public String toString() {
      return name().charAt(0) + name().substring(1).toLowerCase();
   }

   public static Colour oppositeColour(Colour colour) {
      return (colour == Colour.WHITE ? BLACK : WHITE);
   }
}
