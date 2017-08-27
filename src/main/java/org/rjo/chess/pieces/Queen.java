package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.CheckRestriction;
import org.rjo.chess.Colour;
import org.rjo.chess.KingCheck;
import org.rjo.chess.Move;
import org.rjo.chess.Move.CheckInformation;
import org.rjo.chess.Position;
import org.rjo.chess.PositionCheckState;
import org.rjo.chess.Square;
import org.rjo.chess.ray.BaseRay;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.util.BitSetUnifier;
import org.rjo.chess.util.SquareCache;

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
		return AbstractBitBoardPiece.pieceSquareValue(pieces.getBitSet(), getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Queen class -- with no pieces on the board. Delegates to Queen(Colour, boolean) with parameter false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public Queen(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the Queen class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public Queen(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the Queen class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Queen(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Queen class with the required squares (can be null) or the default start squares. Setting
	 * <code>startPosition</code> true has precedence over <code>startSquares</code>.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
	 *           ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Queen(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.QUEEN);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = null;
		requiredSquares = getColour() == Colour.WHITE ? new Square[] { Square.d1 } : new Square[] { Square.d8 };
		initPosition(requiredSquares);
	}

	@Override
	public List<Move> findMoves(Position posn,
			CheckInformation kingInCheck,
			CheckRestriction checkRestriction) {

		List<Move> moves = findPotentialMoves(posn, checkRestriction);

		// make sure my king is not/no longer in check
		Square myKing = posn.getKingPosition(colour);
		Colour opponentsColour = Colour.oppositeColour(colour);
		Iterator<Move> iter = moves.listIterator();
		while (iter.hasNext()) {
			Move move = iter.next();
			if (KingCheck.isKingInCheck(posn, move, opponentsColour, myKing, kingInCheck.isCheck())) {
				iter.remove();
			}
		}

		return moves;
	}

	@Override
	public List<Move> findPotentialMoves(Position posn,
			CheckRestriction checkRestriction) {

		List<Move> moves = new ArrayList<>(30);

		/*
		 * search for moves in all compass directions.
		 */
		for (RayType rayType : RayType.values()) {
			moves.addAll(search(posn, BaseRay.getRay(rayType), checkRestriction));
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

		if (findRankOrFileCheck(posn, emptySquares, move, opponentsKing, checkCache)
				|| findDiagonalCheck(posn, emptySquares, move, opponentsKing, checkCache)) {
			return new CheckInformation(move.getPiece(), move.to());
		}
		// if it's already check, don't need to calculate discovered check

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
			if (attacksSquare(emptySquares, Square.fromBitIndex(i), targetSq, checkCache, false/** TODO */
					, false)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * static version of {@link #attacksSquare(BitSet, Square, SquareCache)}, for use from Pawn.
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
		if (attacksSquareDiagonally(emptySquares, startSquare, targetSquare, checkCache, isCapture, isPromotion)) {
			return true;
		}
		return false;
	}
}
