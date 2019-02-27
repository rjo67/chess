package org.rjo.chess.pieces;

import org.rjo.chess.Colour;

/**
 * Piece types.
 *
 * @author rich
 */
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

	/**
	 * All the pieces, from pawn to queen. The order here is used by findMoves().
	 */
	public final static PieceType[] ALL_PIECE_TYPES = new PieceType[] { QUEEN, ROOK, KNIGHT, BISHOP, PAWN, KING };

	/**
	 * All the possible pieces, excluding the king.
	 */
	public final static PieceType[] ALL_PIECE_TYPES_EXCEPT_KING = new PieceType[] { PAWN, ROOK, KNIGHT, BISHOP, QUEEN };

	public final static PieceType[] PROMOTION_RELEVANT_PIECE_TYPES = new PieceType[] { ROOK, KNIGHT, BISHOP, QUEEN };

	/**
	 * Just the knight and the pawn.
	 */
	public final static PieceType[] KNIGHT_PAWN = new PieceType[] { KNIGHT, PAWN };

	private String symbol;

	PieceType(String symbol) {
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

	/**
	 * @return pieces which a pawn can promote to
	 */
	public static PieceType[] getPieceTypesForPromotion() {
		return PROMOTION_RELEVANT_PIECE_TYPES;
	}

	/**
	 * @return all piece types
	 */
	public static PieceType[] getPieceTypes() {
		return ALL_PIECE_TYPES;
	}

	public static PieceType getPieceTypeFromSymbol(String symbol) {
		for (PieceType pt : PieceType.values()) {
			if (symbol.toUpperCase().equals(pt.getSymbol())) {
				return pt;
			}
		}
		return null;
	}

}
