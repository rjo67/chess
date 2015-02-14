package org.rjo.chess.pieces;

import org.rjo.chess.Colour;

public enum PieceType {

   PAWN("") {
      /**
       * Returns the FEN symbol for a pawn (upper or lower case "P").
       * <hr>
       * {@inheritDoc}
       */
      @Override
      public String getFenSymbol(Colour colour) {
         return colour == Colour.WHITE ? "P" : "p";
      }
   },
   ROOK("R"), KNIGHT("N"), BISHOP("B"), QUEEN("Q"), KING("K");

   private String symbol;

   private PieceType(String symbol) {
      this.symbol = symbol;
   }

   @Override
   public String toString() {
      return name().charAt(0) + name().substring(1).toLowerCase();
   }

   public String getSymbol() {
      return symbol;
   }

   /**
    * Returns the FEN symbol for this piece. This is usually the 'symbol' in upper or lower case. Exception is the pawn.
    * 
    * @return the FEN symbol for this piece.
    */
   public String getFenSymbol(Colour colour) {
      return colour == Colour.WHITE ? getSymbol() : getSymbol().toLowerCase();
   }
}
