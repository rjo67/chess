package org.rjo.newchess.piece;

/**
 * <ul>
 * <li>000 pawn</li>
 * <li>001 rook</li>
 * <li>010 knight</li>
 * <li>011 bishop</li>
 * <li>100 queen</li>
 * <li>101 king</li>
 * </ul>
 * 
 * @author rich
 * @see    "https://www.chessprogramming.org/10x12_Board#Square_Mapping"
 */
public enum PieceType {

   PAWN(false, "", new int[] {}), // offsets not used for pawns
   ROOK(true, "R", new int[] { -10, -1, 1, 10 }), //
   KNIGHT(false, "N", new int[] { -21, -19, -12, -8, 8, 12, 19, 21 }), //
   BISHOP(true, "B", new int[] { -11, -9, 9, 11 }), //
   QUEEN(true, "Q", new int[] { -11, -10, -9, -1, 1, 9, 10, 11 }), //
   KING(false, "K", new int[] { -11, -10, -9, -1, 1, 9, 10, 11 });

   /**
    * number of different piece types in the game.
    */
   public final static int DIFFERENT_PIECE_TYPES = PieceType.values().length;

   private boolean slidingPiece;
   private String symbol;
   // offsets refer to the Board.mailbox structure
   private int[] moveOffsets;

   private PieceType(boolean slidingPiece, String symbol, int[] moveOffsets) {
      this.slidingPiece = slidingPiece;
      this.symbol = symbol;
      this.moveOffsets = moveOffsets;
   }

   public static PieceType convert(int val) {
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
         throw new IllegalArgumentException("illegal PieceType value: '" + val + "'");
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
}
