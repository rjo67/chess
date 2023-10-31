package org.rjo.newchess.piece;

import org.rjo.newchess.board.Ray;

public class Pieces {

   private final static byte BITMASK_0 = 0b00000001;
   private final static byte BITMASK_1 = BITMASK_0 << 1;
   private final static byte BITMASK_2 = BITMASK_1 << 1;
   private final static byte BITMASK_3 = BITMASK_2 << 1;
   private final static byte BITMASK_4 = BITMASK_3 << 1;
   private final static byte BITMASK_5 = BITMASK_4 << 1;
   private final static byte BITMASK_6 = BITMASK_5 << 1;
   private final static byte BITMASK_7 = (byte) (BITMASK_6 << 1);

   private final static byte COLOUR_MASK = BITMASK_0; // 0=white,1=black
   private final static byte PAWN_MASK = BITMASK_1;
   private final static byte ROOK_MASK = BITMASK_2;
   private final static byte KNIGHT_MASK = BITMASK_3;
   private final static byte BISHOP_MASK = BITMASK_4;
   private final static byte QUEEN_MASK = BITMASK_5;
   private final static byte KING_MASK = BITMASK_6;
   private final static byte SLIDING_PIECE_MASK = BITMASK_7;
   private final static byte BISHOP_OR_QUEEN_MASK = BISHOP_MASK | QUEEN_MASK;
   private final static byte ROOK_OR_QUEEN_MASK = ROOK_MASK | QUEEN_MASK;
   private final static byte PAWN_OR_KNIGHT_MASK = PAWN_MASK | KNIGHT_MASK;

   // bit 0 (right): reserved for colour, not set here
   // bits 1-6: piece info
   // bit 7 (left): sliding piece
   private final static byte PAWN = PAWN_MASK;
   private final static byte ROOK = ROOK_MASK | SLIDING_PIECE_MASK;
   private final static byte KNIGHT = KNIGHT_MASK;
   private final static byte BISHOP = BISHOP_MASK | SLIDING_PIECE_MASK;
   private final static byte QUEEN = QUEEN_MASK | SLIDING_PIECE_MASK;
   private final static byte KING = KING_MASK;

   public final static byte WHITE_PAWN = PAWN;
   public final static byte WHITE_ROOK = ROOK;
   public final static byte WHITE_KNIGHT = KNIGHT;
   public final static byte WHITE_BISHOP = BISHOP;
   public final static byte WHITE_QUEEN = QUEEN;
   public final static byte WHITE_KING = KING;
   public final static byte BLACK_PAWN = PAWN | COLOUR_MASK;
   public final static byte BLACK_ROOK = ROOK | COLOUR_MASK;
   public final static byte BLACK_KNIGHT = KNIGHT | COLOUR_MASK;
   public final static byte BLACK_BISHOP = BISHOP | COLOUR_MASK;
   public final static byte BLACK_QUEEN = QUEEN | COLOUR_MASK;
   public final static byte BLACK_KING = KING | COLOUR_MASK;

   public final static int[] ROOK_MOVE_OFFSETS = { -10, -1, 1, 10 };
   public final static int[] KNIGHT_MOVE_OFFSETS = { -21, -19, -12, -8, 8, 12, 19, 21 };
   public final static int[] BISHOP_MOVE_OFFSETS = { -11, -9, 9, 11 };
   public final static int[] QUEEN_MOVE_OFFSETS = { -11, -10, -9, -1, 1, 9, 10, 11 };
   public final static int[] KING_MOVE_OFFSETS = { -11, -10, -9, -1, 1, 9, 10, 11 };

   private Pieces() {
      // cannot be instantiated
   }

   public static boolean isWhitePiece(byte piece) {
      return (piece != 0) && (piece & COLOUR_MASK) == 0;
   }

   public static boolean isBlackPiece(byte piece) {
      return (piece & COLOUR_MASK) != 0;
   }

   public static boolean isSlidingPiece(byte piece) {
      return (piece & SLIDING_PIECE_MASK) != 0;
   }

   public static boolean isPawn(byte piece) {
      return (piece & PAWN_MASK) != 0;
   }

   public static boolean isRook(byte piece) {
      return (piece & ROOK_MASK) != 0;
   }

   public static boolean isKnight(byte piece) {
      return (piece & KNIGHT_MASK) != 0;
   }

   public static boolean isBishop(byte piece) {
      return (piece & BISHOP_MASK) != 0;
   }

   public static boolean isQueen(byte piece) {
      return (piece & QUEEN_MASK) != 0;
   }

   public static boolean isKing(byte piece) {
      return (piece & KING_MASK) != 0;
   }

   public static boolean isRookOrQueen(byte piece) {
      return (piece & ROOK_OR_QUEEN_MASK) != 0;
   }

   public static boolean isBishopOrQueen(byte piece) {
      return (piece & BISHOP_OR_QUEEN_MASK) != 0;
   }

   public static boolean isPawnOrKnight(byte piece) {
      return (piece & PAWN_OR_KNIGHT_MASK) != 0;
   }

   public static byte generatePawn(Colour colour) {
      return colour == Colour.WHITE ? WHITE_PAWN : BLACK_PAWN;
   }

   public static byte generateKnight(Colour colour) {
      return colour == Colour.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
   }

   public static byte generateRook(Colour colour) {
      return colour == Colour.WHITE ? WHITE_ROOK : BLACK_ROOK;
   }

   public static byte generateBishop(Colour colour) {
      return colour == Colour.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
   }

   public static byte generateQueen(Colour colour) {
      return colour == Colour.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
   }

   public static byte generateKing(Colour colour) {
      return colour == Colour.WHITE ? WHITE_KING : BLACK_KING;
   }

   public static byte generatePiece(Piece pieceType, Colour colour) {
      switch (pieceType) {
      case PAWN:
         return colour == Colour.WHITE ? WHITE_PAWN : BLACK_PAWN;
      case ROOK:
         return colour == Colour.WHITE ? WHITE_ROOK : BLACK_ROOK;
      case KNIGHT:
         return colour == Colour.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT;
      case BISHOP:
         return colour == Colour.WHITE ? WHITE_BISHOP : BLACK_BISHOP;
      case QUEEN:
         return colour == Colour.WHITE ? WHITE_QUEEN : BLACK_QUEEN;
      case KING:
         return colour == Colour.WHITE ? WHITE_KING : BLACK_KING;
      }
      throw new IllegalStateException();
   }

   /**
    * @see Piece#fenSymbol(Colour)
    */
   public static String fenSymbol(byte piece) {
      return toPiece(piece).fenSymbol(colourOf(piece));
   }

   /**
    * @see Piece#symbol(Colour)
    */
   public static String symbol(byte piece) {
      return toPiece(piece).symbol(colourOf(piece));
   }

   // convert from Piece to 'byte'
   public static byte fromPiece(Piece piece, Colour colour) {
      switch (piece) {
      case PAWN:
         return generatePawn(colour);
      case ROOK:
         return generateRook(colour);
      case KNIGHT:
         return generateKnight(colour);
      case BISHOP:
         return generateBishop(colour);
      case QUEEN:
         return generateQueen(colour);
      case KING:
         return generateKing(colour);
      default:
         throw new IllegalStateException("unexpected value: " + piece);
      }
   }

   // convert from 'byte' to Piece
   public static Piece toPiece(byte piece) {
      if (isPawn(piece)) {
         return Piece.PAWN;
      } else if (isRook(piece)) {
         return Piece.ROOK;
      } else if (isKnight(piece)) {
         return Piece.KNIGHT;
      } else if (isBishop(piece)) {
         return Piece.BISHOP;
      } else if (isQueen(piece)) {
         return Piece.QUEEN;
      } else if (isKing(piece)) {
         return Piece.KING;
      } else
         throw new IllegalStateException("unexpected value: " + piece);
   }

   public static Colour colourOf(byte piece) {
      if (piece == 0) { throw new IllegalStateException("colourOf called for empty square"); }
      return isWhitePiece(piece) ? Colour.WHITE : Colour.BLACK;
   }

   public static boolean canSlideAlongRay(byte piece, Ray ray) {
      if (!isSlidingPiece(piece)) { return false; }
      if (ray.isDiagonal()) {
         return (isBishopOrQueen(piece));
      } else {
         return (isRookOrQueen(piece));
      }
   }

}
