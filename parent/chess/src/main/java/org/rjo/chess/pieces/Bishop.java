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
		return AbstractBitBoardPiece.pieceSquareValue(pieces, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Bishop class -- with no pieces on the board. Delegates to Bishop(Colour, boolean) with parameter
	 * false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public Bishop(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the Bishop class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public Bishop(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the Bishop class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Bishop(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Bishop class with the required squares (can be null) or the default start squares. Setting
	 * <code>startPosition</code> true has precedence over <code>startSquares</code>.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
	 *           ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Bishop(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.BISHOP);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = null;
		requiredSquares = getColour() == Colour.WHITE ? new Square[] { Square.c1, Square.f1 } : new Square[] { Square.c8, Square.f8 };
		initPosition(requiredSquares);
	}

	@Override
	public List<Move> findMoves(Position posn,
			boolean kingInCheck,
			PositionInfo posnInfo) {
		List<Move> moves = new ArrayList<>(30);

		for (int indexOfPiece = pieces.getBitSet().nextSetBit(0); indexOfPiece >= 0; indexOfPiece = pieces.getBitSet()
				.nextSetBit(indexOfPiece + 1)) {
			RayType[] raysToSearch;
			var pinnedPiece = posnInfo.isPiecePinned(PieceType.BISHOP, Square.fromBitIndex(indexOfPiece));
			if (pinnedPiece.isPresent()) {
				// if pinned horizontally/vertically, can't move
				if (!pinnedPiece.get().getRay().isDiagonal()) {
					continue;
				}
				// search only the pinned ray and its opposite
				raysToSearch = new RayType[2];
				raysToSearch[0] = pinnedPiece.get().getRay();
				raysToSearch[1] = pinnedPiece.get().getRay().getOpposite();
			} else {
				raysToSearch = new RayType[] { RayType.SOUTHWEST, RayType.SOUTHEAST, RayType.NORTHWEST, RayType.NORTHEAST };
			}
			moves.addAll(searchByPiece(posn, indexOfPiece, posnInfo, raysToSearch));
		}

		//		// make sure king is not/no longer in check
		//		Square myKing = posn.getKingPosition(colour);
		//		KingCheck kingChecker = new KingCheck(posn, Colour.oppositeColour(colour), myKing);
		//		moves.removeIf(move -> kingChecker.isKingInCheck(move, kingInCheck.isCheck()));
		return moves;
	}

	@Override
	public boolean doesMoveLeaveOpponentInCheck(Move move,
			@SuppressWarnings("unused") Piece[] pieces,
			@SuppressWarnings("unused") Square opponentsKing,
			BitBoard[] checkingBitboards) {
		return checkingBitboards[1].get(move.to().bitIndex());
	}

	@Override
	public boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache) {
		for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
			if (attacksSquare(emptySquares, Square.fromBitIndex(i), targetSq, checkCache, false/* TODO */
					, false)) {
				return true;
			}
		}
		return false;
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
