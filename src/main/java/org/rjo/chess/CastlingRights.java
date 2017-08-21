package org.rjo.chess;

import java.util.EnumSet;

import org.rjo.chess.pieces.PieceType;

/**
 * Whether castling king's or queen's side is possible.
 *
 * @author rich
 */
public enum CastlingRights {

	QUEENS_SIDE, KINGS_SIDE;

	public static final EnumSet<CastlingRights> NO_RIGHTS = EnumSet.noneOf(CastlingRights.class);

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
	public static boolean kingsSideCastlingRightsGoneAfterMove(EnumSet<CastlingRights> castlingRights,
			Colour sideToMove,
			Move move) {
		if ((PieceType.ROOK == move.getPiece()) && castlingRights.contains(CastlingRights.KINGS_SIDE)) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h1 : Square.h8;
			return (move.from() == targetSquare);
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
	public static boolean queensSideCastlingRightsGoneAfterMove(EnumSet<CastlingRights> castlingRights,
			Colour sideToMove,
			Move move) {
		if ((PieceType.ROOK == move.getPiece()) && castlingRights.contains(CastlingRights.QUEENS_SIDE)) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a1 : Square.a8;
			return (move.from() == targetSquare);
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
	public static boolean opponentKingsSideCastlingRightsGoneAfterMove(EnumSet<CastlingRights> opponentsCastlingRights,
			Colour sideToMove,
			Move move) {
		if (move.isCapture() && opponentsCastlingRights.contains(CastlingRights.KINGS_SIDE)) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h8 : Square.h1;
			return (move.to() == targetSquare);
		} else {
			return false;
		}
	}

	/**
	 * Checks if, after <code>move</code>, the opponent can (still) castle queen's-side.
	 * <p>
	 * Actual check is to see if <code>move</code> is a rook move from a1/a8, and queen's-side castling was possible
	 * beforehand.
	 *
	 * @param opponentsCastlingRights whether castling was allowed before the move FOR OPPONENT
	 * @param sideToMove which side is moving
	 * @param move the move
	 * @return true if the opponent can no longer castle on the queen's side after the move
	 */
	public static boolean opponentQueensSideCastlingRightsGoneAfterMove(EnumSet<CastlingRights> opponentsCastlingRights,
			Colour sideToMove,
			Move move) {
		if (move.isCapture() && opponentsCastlingRights.contains(CastlingRights.QUEENS_SIDE)) {
			Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a8 : Square.a1;
			return (move.to() == targetSquare);
		} else {
			return false;
		}
	}
}
