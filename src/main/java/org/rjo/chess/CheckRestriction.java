package org.rjo.chess;

/**
 * Normally all squares come into consideration for a move. If the king is in check however, the available squares are
 * greatly reduced. In this case either the checking piece must be captured, a piece interposed on the checking ray, or
 * the king must move.
 *
 * @author rich
 * @since 2017-08-27
 */
public class CheckRestriction {

	public static final CheckRestriction NO_RESTRICTION = new CheckRestriction();

	/** a bitboard of the destination squares which come into consideration for moves from this position */
	private BitBoard squareRestriction;
	private boolean inCheck;

	private CheckRestriction() {
		this.squareRestriction = BitBoard.ALL_SET;
		this.inCheck = false;
	}

	public CheckRestriction(BitBoard restriction) {
		this.squareRestriction = restriction;
		this.inCheck = true;
	}

	public BitBoard getSquareRestriction() {
		return squareRestriction;
	}

	public boolean isInCheck() {
		return inCheck;
	}
}
