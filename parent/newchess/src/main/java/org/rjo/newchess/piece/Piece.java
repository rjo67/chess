package org.rjo.newchess.piece;

import org.rjo.newchess.board.Ray;

/**
 * 
 * @author rich
 * @see    "https://www.chessprogramming.org/10x12_Board#Square_Mapping"
 */
public enum Piece {

   PAWN(false, "", false, false, new int[] {}) // offsets not used for pawns
   {
      @Override
      public String fenSymbol(Colour colour) {
         return colour == Colour.WHITE ? "P" : "p";
      }
   },
   ROOK(true, "R", true, false, new int[] { -10, -1, 1, 10 }), //
   KNIGHT(false, "N", false, false, new int[] { -21, -19, -12, -8, 8, 12, 19, 21 }), //
   BISHOP(true, "B", false, true, new int[] { -11, -9, 9, 11 }), //
   QUEEN(true, "Q", true, true, new int[] { -11, -10, -9, -1, 1, 9, 10, 11 }), //
   KING(false, "K", false, false, new int[] { -11, -10, -9, -1, 1, 9, 10, 11 });

   /**
    * number of different piece types in the game.
    */
   public final static int DIFFERENT_PIECE_TYPES = Piece.values().length;

   private boolean slidingPiece;
   private String symbol;
   // offsets refer to the Board.mailbox structure
   private int[] moveOffsets;
   // following not relevant (i.e. false) for pawns or knights; or Kings
   private boolean slidesHorizontallyOrVertically;
   private boolean slidesDiagonally;

   private Piece(boolean slidingPiece, String symbol, boolean movesHorizontallyOrVertically, boolean movesDiagonally, int[] moveOffsets) {
      this.slidingPiece = slidingPiece;
      this.symbol = symbol;
      this.slidesHorizontallyOrVertically = movesHorizontallyOrVertically;
      this.slidesDiagonally = movesDiagonally;
      this.moveOffsets = moveOffsets;
   }

   public static Piece convert(int val) {
      switch (val) {
      case 0:
         return PAWN;
      case 1:
         return ROOK;
      case 2:
         return KNIGHT;
      case 3:
         return BISHOP;
      case 4:
         return QUEEN;
      case 5:
         return KING;
      default:
         throw new IllegalArgumentException("illegal Piece value: '" + val + "'");
      }
   }

   public boolean isSlidingPiece() {
      return slidingPiece;
   }

   public int[] getMoveOffsets() {
      return moveOffsets;
   }

   public String symbol(Colour colour) {
      return symbol; // == Colour.WHITE ? symbol : symbol.toLowerCase();
   }

   /**
    * Returns the FEN symbol for this piece. This is usually the 'symbol' in upper or lower case. Exception is the pawn.
    */
   public String fenSymbol(Colour colour) {
      return colour == Colour.WHITE ? symbol : symbol.toLowerCase();
   }

   public boolean slidesHorizontallyOrVertically() {
      return slidesHorizontallyOrVertically;
   }

   public boolean slidesDiagonally() {
      return slidesDiagonally;
   }

   /**
    * @return true if this piece can move along the given ray.
    */
   public boolean canSlideAlongRay(Ray ray) {
      return ((ray.isHorizontal() || ray.isVertical()) && slidesHorizontallyOrVertically) || (ray.isDiagonal() && slidesDiagonally());
   }
}
