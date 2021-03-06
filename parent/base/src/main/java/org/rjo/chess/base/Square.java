package org.rjo.chess.base;

/**
 * An enum which maps between board squares and their bit positions (A1 == 0, H8 == 63) and vice versa.
 *
 * @author rich
 */
public enum Square {
	//@formatter:off
   a1( 0), a2( 8), a3(16), a4(24), a5(32), a6(40), a7(48), a8(56),
   b1( 1), b2( 9), b3(17), b4(25), b5(33), b6(41), b7(49), b8(57),
   c1( 2), c2(10), c3(18), c4(26), c5(34), c6(42), c7(50), c8(58),
   d1( 3), d2(11), d3(19), d4(27), d5(35), d6(43), d7(51), d8(59),
   e1( 4), e2(12), e3(20), e4(28), e5(36), e6(44), e7(52), e8(60),
   f1( 5), f2(13), f3(21), f4(29), f5(37), f6(45), f7(53), f8(61),
   g1( 6), g2(14), g3(22), g4(30), g5(38), g6(46), g7(54), g8(62),
   h1( 7), h2(15), h3(23), h4(31), h5(39), h6(47), h7(55), h8(63);
   //@formatter:on

	// lookup int->Square
	private final static Square[] bitIndexToSquare = new Square[64];
	static {
		for (Square sq : Square.values()) {
			bitIndexToSquare[sq.bitIndex] = sq;
		}
	}

	private int bitIndex;
	private int rank;// Rank of this square (0..7)
	private int file;// File of this square (0..7)

	Square(int bitIndex) {
		this.bitIndex = bitIndex;
		this.rank = bitIndex / 8;
		this.file = bitIndex % 8;
	}

	/**
	 * Rank of this square (0..7)
	 *
	 * @return
	 */
	public int rank() {
		return rank;
	}

	/**
	 * File of this square (0..7)
	 *
	 * @return
	 */
	public int file() {
		return file;
	}

	/**
	 * Delivers the bit posn of this square.
	 *
	 * @return the bit posn 0..63
	 */
	public int bitIndex() {
		return bitIndex;
	}

	/**
	 * Is this square adjacent to the given square?
	 * 
	 * @param other the other square
	 * @return true when adjacent
	 */
	public boolean isAdjacentTo(Square other) {
		return Math.abs(this.rank - other.rank) <= 1 && Math.abs(this.file - other.file) <= 1;
	}

	/**
	 * Gives the matching square to a bit posn.
	 *
	 * @param bitIndex the required bit index (0..63)
	 * @return the matching square or null
	 */
	public static Square fromBitIndex(int bitIndex) {
		return bitIndexToSquare[bitIndex];
	}

	/**
	 * Maps a string "a5" to the corresponding enum value.
	 *
	 * @param coord a string of length 2. Values A..H and 1..8.
	 * @return a Square or null.
	 */
	public static Square fromString(String coord) {
		if (coord.length() != 2) {
			throw new IllegalArgumentException("expected a string of length 2. Got '" + coord + "'");
		}
		String fileStr = String.valueOf(coord.charAt(0)).toLowerCase();
		char file = fileStr.charAt(0);
		int rank = Character.digit(coord.charAt(1), 10);
		if (file < 'a' || file > 'h') {
			throw new IllegalArgumentException("bad value for 'file': " + file);
		}
		if (rank < 1 || rank > 8) {
			throw new IllegalArgumentException("bad value for 'rank': " + rank);
		}
		return Square.fromRankAndFile(rank - 1, file - 'a');
	}

	/**
	 * Maps from a rank/file mapping to a Square.
	 *
	 * @param rank the rank (0..7)
	 * @param file the file (0..7)
	 * @return the square
	 */
	public static Square fromRankAndFile(int rank,
			int file) {
		int bitPosn = (rank * 8) + file;
		return Square.fromBitIndex(bitPosn);
	}

	/**
	 * Returns the square where the (captured) pawn must be, given the enpassant square. e.g. given b6, returns b5.
	 * <p>
	 * Or, for black, given d3, returns d4.
	 *
	 * @param sq start square. Must be either on the 6th rank (for a black move) or on the 3rd rank (for a white move).
	 * @return the square where the pawn moved to (on the 5th rank for black or the 4th rank for white).
	 */
	public static Square findMoveFromEnpassantSquare(Square sq) {
		if (sq.rank() == 5) {
			return Square.fromRankAndFile(4, sq.file());
		} else if (sq.rank() == 2) {
			return Square.fromRankAndFile(3, sq.file());
		} else {
			throw new IllegalArgumentException("must specify an enpassant square, but got: " + sq);
		}
	}

	/**
	 * Returns the enpassant square, given the (pawn) move. e.g. given b5, returns b6. Or a4, returns a3.
	 *
	 * @param sq the square where the pawn moved to (on the 5th rank for black or the 4th rank for white).
	 * @return enpassant square. Must be either on the 6th rank (for a black move) or on the 3rd rank (for a white move).
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
}
