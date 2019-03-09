package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.Move.CheckInformation;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.SquareCache;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.check.BoardInfo;
import org.rjo.chess.position.check.CheckRestriction;
import org.rjo.chess.position.check.KingCheck;

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
			BoardInfo boardInfo) {
		List<Move> moves = new ArrayList<>(30);

		// search for moves
		for (RayType rayType : RayType.RAY_TYPES_DIAGONAL) {
			moves.addAll(search(posn, RayUtils.getRay(rayType), boardInfo.getCheckRestrictedSquares(), boardInfo.isKingInCheck()));
		}
		// make sure my king is not/no longer in check
		Square myKing = posn.getKingPosition(colour);
		KingCheck kingChecker = new KingCheck(posn, Colour.oppositeColour(colour), myKing);
		moves.removeIf(move -> kingChecker.isKingInCheck(move, boardInfo.isKingInCheck()));

		return moves;
	}

	@Override
	public List<Move> findMoves(Position posn,
			CheckInformation kingInCheck,
			CheckRestriction checkRestriction) {
		List<Move> moves = findPotentialMoves(posn, checkRestriction);

		// make sure king is not/no longer in check
		Square myKing = posn.getKingPosition(colour);
		KingCheck kingChecker = new KingCheck(posn, Colour.oppositeColour(colour), myKing);
		moves.removeIf(move -> kingChecker.isKingInCheck(move, kingInCheck.isCheck()));
		return moves;
	}

	@Override
	public List<Move> findPotentialMoves(Position posn,
			CheckRestriction checkRestriction) {
		List<Move> moves = new ArrayList<>(30);

		// search for moves
		for (RayType rayType : RayType.RAY_TYPES_DIAGONAL) {
			moves.addAll(search(posn, RayUtils.getRay(rayType), checkRestriction.getSquareRestriction(), checkRestriction.isInCheck()));
		}

		return moves;
	}

	@Override
	public CheckInformation isOpponentsKingInCheckAfterMove(Position posn,
			Move move,
			Square opponentsKing,
			BitSetUnifier emptySquares,
			PositionCheckState checkCache,
			SquareCache<Boolean> discoveredCheckCache) {

		if (findDiagonalCheck(posn, emptySquares, move, opponentsKing, checkCache)) {
			// if it's already check, don't need to calculate discovered check
			return new CheckInformation(move.getPiece(), move.to());
		}

		/*
		 * many moves have the same starting square. If we've already checked for discovered check for this square, then can use
		 * the cached result. (Discovered check only looks along one ray from move.from() to the opponent's king.)
		 */
		boolean isCheck;
		Boolean lookup = discoveredCheckCache.lookup(move.from());
		if (lookup != null) {
			isCheck = lookup;
		} else {
			isCheck = Position.checkForDiscoveredCheck(posn, move, getColour(), opponentsKing);
			discoveredCheckCache.store(move.from(), isCheck);
		}
		if (isCheck) {
			return new CheckInformation(true);
		} else {
			return CheckInformation.NOT_CHECK;
		}
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
