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
 * Stores information about the queens (still) in the game.
 *
 * @author rich
 */
public class Queen extends SlidingPiece {

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 900;

	/**
	 * stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function
	 */
	// Important: array value [0] corresponds to square a1; [63] == h8.
	private static int[] SQUARE_VALUE =
	// @formatter:off
			new int[] { -20, -10, -10, -5, -5, -10, -10, -20, -10, 0, 5, 0, 0, 0, 0, -10, -10, 0, 5, 5, 5, 5, 0, -10, 0,
					0, 5, 5, 5, 5, 0, -5, -5, 0, 5, 5, 5, 5, 0, -5, -10, 5, 5, 5, 5, 5, 0, -10, -10, 0, 0, 0, 0, 0, 0,
					-10, -20, -10, -10, -5, -5, -10, -10, -20 };
	// @formatter:on

	@Override
	public int calculatePieceSquareValue() {
		return pieceSquareValue(location, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Queen class, defining the start square.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param location the required starting square of the piece. Cannot be null.
	 */
	public Queen(Colour colour, Square location) {
		super(colour, PieceType.QUEEN, location);
	}

	/**
	 * Copy constructor.
	 *
	 * @param the Queen to copy.
	 */
	public Queen(Piece other) {
		this(other.getColour(), other.getLocation());
		if (other.getType() != PieceType.QUEEN) {
			throw new IllegalStateException("piece is wrong type! " + other);
		}
	}

	@Override
	public List<Move> findMoves(Position posn,
			boolean kingInCheck,
			PositionInfo posnInfo) {
		List<Move> moves = new ArrayList<>(30);

		// search in all directions (unless pinned, in which case can limit the rays searched)

		RayType[] raysToSearch;
		var pinnedPiece = posnInfo.isPiecePinned(PieceType.QUEEN, location);
		if (pinnedPiece.isPresent()) {
			// search only the pinned ray and its opposite
			raysToSearch = new RayType[2];
			raysToSearch[0] = pinnedPiece.get().getRay();
			raysToSearch[1] = pinnedPiece.get().getRay().getOpposite();
		} else {
			raysToSearch = RayType.values();
		}
		moves.addAll(searchByPiece(posn, location.bitIndex(), posnInfo, raysToSearch));
		return moves;
	}

	@Override
	public boolean doesMoveLeaveOpponentInCheck(Move move,
			@SuppressWarnings("unused") Pieces pieces,
			@SuppressWarnings("unused") Square opponentsKing,
			BitBoard[] checkingBitboards) {
		return checkingBitboards[0].get(move.to().bitIndex()) || checkingBitboards[1].get(move.to().bitIndex());
	}

	@Override
	public boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache) {
		return attacksSquare(emptySquares, location, targetSq, checkCache, false /* TODO */
				, false);
	}

	/**
	 * static version of {@link #attacksSquare(BitSetUnifier, Square, PositionCheckState)}, for use from Pawn.
	 *
	 * @param emptySquares the empty squares
	 * @param startSquare start square (i.e. where the queen is)
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
		if (attacksSquareRankOrFile(emptySquares, startSquare, targetSquare, checkCache, isCapture, isPromotion)) {
			return true;
		}
		return attacksSquareDiagonally(emptySquares, startSquare, targetSquare, checkCache, isCapture, isPromotion);
	}
}
