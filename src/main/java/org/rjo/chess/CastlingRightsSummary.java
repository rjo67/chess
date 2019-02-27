package org.rjo.chess;

import java.util.EnumSet;

import org.rjo.chess.pieces.PieceType;

/**
 * Which castling rights are available.
 *
 * @author rich
 * @since 2017-08-29
 */
public class CastlingRightsSummary {

	public enum CastlingRights {
		QUEENS_SIDE, KINGS_SIDE
    }

	public static final CastlingRightsSummary NO_RIGHTS = new CastlingRightsSummary(false, false);

	private boolean kingsSideCastling;
	private boolean queensSideCastling;

	public CastlingRightsSummary(boolean kingsSide, boolean queensSide) {
		this.kingsSideCastling = kingsSide;
		this.queensSideCastling = queensSide;
	}

	// copy constructor
	public CastlingRightsSummary(CastlingRightsSummary other) {
		this.kingsSideCastling = other.canCastleKingsSide();
		this.queensSideCastling = other.canCastleQueensSide();
	}

	// temp constructor
	public CastlingRightsSummary(EnumSet<CastlingRights> castlingRights) {
		if (castlingRights.contains(CastlingRights.KINGS_SIDE)) {
			kingsSideCastling = true;
		}
		if (castlingRights.contains(CastlingRights.QUEENS_SIDE)) {
			queensSideCastling = true;
		}

	}

	/**
	 * Checks if, after <code>move</code>, <code>sideToMove</code> can (still) castle king's-side.
	 * <p>
	 * Actual check is to see if <code>move</code> is a rook move from h1/h8, and king's-side castling was possible
	 * beforehand.
	 *
	 * @param castlingRights whether castling was allowed before the move
	 * @param sideToMove which side is moving
	 * @param move the move
	 * @return true if <code>sideToMove</code> can no longer castle on the king's side after the move. Will return FALSE if
	 *         could not castle king's side before the move.
	 */
	public static boolean kingsSideCastlingRightsGoneAfterMove(CastlingRightsSummary castlingRights,
			Colour sideToMove,
			Move move) {
		if (castlingRights.canCastleKingsSide() && PieceType.ROOK == move.getPiece()) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h1 : Square.h8;
			return move.from() == targetSquare;
		} else {
			return false;
		}
	}

	/**
	 * Checks if, after <code>move</code>, <code>sideToMove</code> can (still) castle queen's-side.
	 * <p>
	 * Actual check is to see if <code>move</code> is a rook move from a1/a8, and queen's-side castling was possible
	 * beforehand.
	 *
	 * @param castlingRights whether castling was allowed before the move
	 * @param sideToMove which side is moving
	 * @param move the move
	 * @return true if <code>sideToMove</code> can no longer castle on the queen's side after the move. Will return FALSE if
	 *         could not castle queens's side before the move.
	 */
	public static boolean queensSideCastlingRightsGoneAfterMove(CastlingRightsSummary castlingRights,
			Colour sideToMove,
			Move move) {
		if (castlingRights.canCastleQueensSide() && PieceType.ROOK == move.getPiece()) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a1 : Square.a8;
			return move.from() == targetSquare;
		} else {
			return false;
		}
	}

	/**
	 * Checks if, after <code>move</code>, the opponent can (still) castle king's-side.
	 * <p>
	 * Actual check is to see if <code>move</code> is a capture on h8/h1, and king's-side castling was possible beforehand.
	 *
	 * @param opponentsCastlingRights whether castling was allowed before the move FOR OPPONENT
	 * @param sideToMove which side is moving
	 * @param move the move
	 * @return true if the opponent can no longer castle on the king's side after the move
	 */
	public static boolean opponentKingsSideCastlingRightsGoneAfterMove(CastlingRightsSummary opponentsCastlingRights,
			Colour sideToMove,
			Move move) {
		if (move.isCapture() && opponentsCastlingRights.canCastleKingsSide()) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h8 : Square.h1;
			return move.to() == targetSquare;
		} else {
			return false;
		}
	}

	/**
	 * Checks if, after <code>move</code>, the opponent can (still) castle queen's-side.
	 * <p>
	 * Actual check is to see if <code>move</code> is a capture on a8/a1, and queen's-side castling was possible beforehand.
	 *
	 * @param opponentsCastlingRights whether castling was allowed before the move FOR OPPONENT
	 * @param sideToMove which side is moving
	 * @param move the move
	 * @return true if the opponent can no longer castle on the queen's side after the move
	 */
	public static boolean opponentQueensSideCastlingRightsGoneAfterMove(CastlingRightsSummary opponentsCastlingRights,
			Colour sideToMove,
			Move move) {
		if (move.isCapture() && opponentsCastlingRights.canCastleQueensSide()) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a8 : Square.a1;
			return move.to() == targetSquare;
		} else {
			return false;
		}
	}

	public boolean canCastleKingsSide() {
		return kingsSideCastling;
	}

	public boolean canCastleQueensSide() {
		return queensSideCastling;
	}

	public boolean canCastle(CastlingRights rights) {
		switch (rights) {
		case KINGS_SIDE:
			return kingsSideCastling;
		case QUEENS_SIDE:
			return queensSideCastling;
		default:
			throw new IllegalStateException("unknown rights: " + rights);
		}
	}

	/**
	 * Can castle at all?
	 *
	 * @return true if can castle kings or queens side
	 */
	public boolean canCastle() {
		return kingsSideCastling || queensSideCastling;
	}

	public boolean cannotCastle() {
		return !canCastle();
	}

	public void removeKingsSideCastlingRight() {
		kingsSideCastling = false;
	}

	public void removeQueensSideCastlingRight() {
		queensSideCastling = false;
	}

	@Override
	public String toString() {
		return (kingsSideCastling ? "K" : "-") + (queensSideCastling ? "Q" : "-");
	}

}
