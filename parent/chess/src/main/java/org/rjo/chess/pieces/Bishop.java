package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.pieces.PieceManager.Pieces;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.PositionInfo;

/**
 * Stores information about the bishops (still) in the game.
 *
 * @author rich
 */
public class Bishop extends SlidingPiece {

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 330;

	/**
	 * stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function
	 */
	// Important: array value [0] corresponds to square a1; [63] == h8.
	private static int[] SQUARE_VALUE =
	// @formatter:off
			new int[] {
					-20, -10, -10, -10, -10, -10, -10, -20,
					-10,   5,   0,   0,   0,   0,   5, -10,
					-10,  10,  10,  10,  10,  10,  10, -10,
					-10,   0,  10,  10,  10,  10,   0, -10,
					-10,   5,   5,  10,  10,   5,   5, -10,
					-10,   0,   5,  10,  10,   5,   0, -10,
					-10,   0,   0,   0,   0,   0,   0, -10,
					-20, -10, -10, -10, -10, -10, -10, -20, };
	// @formatter:on

	@Override
	public int calculatePieceSquareValue() {
		return pieceSquareValue(location, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Bishop class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param location the required starting square of the piece. Cannot be null.
	 */
	public Bishop(Colour colour, Square location) {
		super(colour, PieceType.BISHOP, location);
	}

	/**
	 * Copy constructor.
	 *
	 * @param the Bishop to copy.
	 */
	public Bishop(Piece other) {
		this(other.getColour(), other.getLocation());
		if (other.getType() != PieceType.BISHOP) {
			throw new IllegalStateException("piece is wrong type! " + other);
		}
	}

	@Override
	public List<Move> findMoves(Position posn,
			boolean kingInCheck,
			PositionInfo posnInfo) {
		List<Move> moves = new ArrayList<>(20);

		RayType[] raysToSearch;
		var pinnedPiece = posnInfo.isPiecePinned(PieceType.BISHOP, location);
		if (pinnedPiece.isPresent()) {
			// if pinned horizontally/vertically, can't move
			if (!pinnedPiece.get().getRay().isDiagonal()) {
				return moves;
			}
			// search only the pinned ray and its opposite
			raysToSearch = new RayType[2];
			raysToSearch[0] = pinnedPiece.get().getRay();
			raysToSearch[1] = pinnedPiece.get().getRay().getOpposite();
		} else {
			raysToSearch = new RayType[] { RayType.SOUTHWEST, RayType.SOUTHEAST, RayType.NORTHWEST, RayType.NORTHEAST };
		}
		moves.addAll(searchByPiece(posn, location.bitIndex(), posnInfo, raysToSearch));

		return moves;
	}

	@Override
	public boolean doesMoveLeaveOpponentInCheck(Move move,
			@SuppressWarnings("unused") Pieces pieces,
			@SuppressWarnings("unused") Square opponentsKing,
			BitBoard[] checkingBitboards) {
		return checkingBitboards[1].get(move.to().bitIndex());
	}

	@Override
	public Piece attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache) {
		if (attacksSquare(emptySquares, location, targetSq, checkCache, false/* TODO */
				, false)) {
			return this;
		} else {
			return null;
		}
	}

	/**
	 * static version of {@link #attacksSquare(BitSetUnifier, Square, PositionCheckState)}, for use from Pawn.
	 *
	 * @param emptySquares the empty squares
	 * @param startSquare start square (i.e. where the bishop is)
	 * @param targetSquare square being attacked (i.e. where the king is)
	 * @param checkCache cache of previously found results
	 * @param isCapture if the move is a capture
	 * @param isPromotion if the move is a pawn promotion
	 * @return true if targetSquare is attacked from startSquare, otherwise false.
	 */
	public static boolean attacksSquare(BitSetUnifier emptySquares,
			Square startSquare,
			Square targetSquare,
			PositionCheckState checkCache,
			boolean isCapture,
			boolean isPromotion) {
		return attacksSquareDiagonally(emptySquares, startSquare, targetSquare, checkCache, isCapture, isPromotion);
	}

}
