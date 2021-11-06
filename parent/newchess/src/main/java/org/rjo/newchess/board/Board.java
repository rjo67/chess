package org.rjo.newchess.board;

/**
 * 
 * @author rich
 * @see    "https://www.chessprogramming.org/10x12_Board#Square_Mapping"
 */
public class Board {
   /**
    * The board representation, 12x10, using sentinel values (-1) to mark 'out-of-bounds'.
    */
   private final static int mailbox[] = new int[] {
      //@formatter:off
		     -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		     -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		     -1,  0,  1,  2,  3,  4,  5,  6,  7, -1,
		     -1,  8,  9, 10, 11, 12, 13, 14, 15, -1,
		     -1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
		     -1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
		     -1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
		     -1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
		     -1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
		     -1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
		     -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		     -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
        //@formatter:on
   };

   /** the start of the board in the mailbox array */
   public final static int BOARD_OFFSET = 21;

   /**
    * Stores the offsets of the actual chess board contained in the definition of 'mailbox' above. i.e. square A8 is
    * represented by mailbox[mailbox64[0]].
    */
   // TODO rename to 'offsets'?
   private final static int mailbox64[] = new int[] {
      //@formatter:off
		    21, 22, 23, 24, 25, 26, 27, 28,
		    31, 32, 33, 34, 35, 36, 37, 38,
		    41, 42, 43, 44, 45, 46, 47, 48,
		    51, 52, 53, 54, 55, 56, 57, 58,
		    61, 62, 63, 64, 65, 66, 67, 68,
		    71, 72, 73, 74, 75, 76, 77, 78,
		    81, 82, 83, 84, 85, 86, 87, 88,
		    91, 92, 93, 94, 95, 96, 97, 98
		//@formatter:on
   };

   /**
    * Maps between the array offset and the square notation.<br>
    * Slot 0 corresponds to a8, i.e. top left of the chessboard.
    */
   public enum Square {
      //@formatter:off
	   a8( 0), b8( 1), c8( 2), d8( 3), e8( 4), f8( 5), g8( 6), h8( 7),
	   a7( 8), b7( 9), c7(10), d7(11), e7(12), f7(13), g7(14), h7(15),
	   a6(16), b6(17), c6(18), d6(19), e6(20), f6(21), g6(22), h6(23),
	   a5(24), b5(25), c5(26), d5(27), e5(28), f5(29), g5(30), h5(31),
	   a4(32), b4(33), c4(34), d4(35), e4(36), f4(37), g4(38), h4(39),
	   a3(40), b3(41), c3(42), d3(43), e3(44), f3(45), g3(46), h3(47),
	   a2(48), b2(49), c2(50), d2(51), e2(52), f2(53), g2(54), h2(55),
	   a1(56), b1(57), c1(58), d1(59), e1(60), f1(61), g1(62), h1(63);
	    //@formatter:on

      private int index;
      private int rank;// Rank of this square (0..7)
      private int file;// File of this square (0..7)

      // lookup int->Square
      private final static Square[] indexToSquare = new Square[64];
      static {
         for (Square sq : Square.values()) {
            indexToSquare[sq.index] = sq;
         }
      }

      Square(int index) {
         this.index = index;
         this.rank = 7 - index / 8;
         this.file = index % 8;
      }

      public int index() {
         return index;
      }

      public int rank() {
         return rank;
      }

      public int file() {
         return file;
      }

      public static Square toSquare(int index) {
         return indexToSquare[index];
      }

      public static Square fromRankAndFile(int rank, int file) {
         if (rank < 0 || rank > 7 || file < 0 || file > 7) { throw new IllegalArgumentException(String.format("invalid values (%d, %d)", rank, file)); }
         int sq = ((7 - rank) * 8) + file;
         return Square.indexToSquare[sq];
      }

      /**
       * Returns the enpassant square, assuming a pawn move to the given square e.g. given b5, returns b6. Or a4, returns a3.
       *
       * @param  sq the square where the pawn moved to (on the 5th rank for black or the 4th rank for white).
       * @return    enpassant square. Must be either on the 6th rank (for a black move) or on the 3rd rank (for a white move).
       */
      public static Square findEnpassantSquareFromMove(Square sq) {
         if (sq.rank() == 4) {
            return Square.fromRankAndFile(5, sq.file());
         } else if (sq.rank() == 3) {
            return Square.fromRankAndFile(2, sq.file());
         } else {
            throw new IllegalArgumentException("must specify a square on the 4th or 5th rank, but got: " + sq);
         }
      }

      /**
       * Is this square adjacent to the given square?
       * 
       * @param  other the other square
       * @return       true when adjacent
       */
      public boolean adjacentTo(Square other) {
         // Chebyshev distance
         return Math.max(Math.abs(this.rank - other.rank), Math.abs(this.file - other.file)) == 1;
      }

   }

   public static int mailbox(int offset) {
      return mailbox[offset];
   }

   public static int mailbox64(int offset) {
      return mailbox64[offset];
   }

}
