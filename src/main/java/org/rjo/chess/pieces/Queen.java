package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.CheckStates;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.ray.BaseRay;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.util.Stopwatch;

/**
 * Stores information about the queens (still) in the game.
 *
 * @author rich
 */
public class Queen extends SlidingPiece {
	private static final Logger LOG = LogManager.getLogger(Queen.class);

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
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the board.
	 */
	public Queen(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Queen class with the required squares (can be null) or the default start squares. Setting 'startPosition' true has precedence
	 * over 'startSquares'.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of 'startSquares' will be ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the board.
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
	public List<Move> findMoves(Position posn, boolean kingInCheck) {
		Stopwatch stopwatch = new Stopwatch();

		List<Move> moves = new ArrayList<>(30);

		/*
		 * search for moves in all compass directions.
		 */
		for (RayType rayType : RayType.values()) {
			moves.addAll(search(posn, BaseRay.getRay(rayType)));
		}
		// make sure my king is not/no longer in check
		Square myKing = King.findKing(getColour(), posn);
		Iterator<Move> iter = moves.listIterator();
		while (iter.hasNext()) {
			Move move = iter.next();
			boolean inCheck = Position.isKingInCheck(posn, move, Colour.oppositeColour(getColour()), myKing, kingInCheck);
			if (inCheck) {
				iter.remove();
			}
		}

		long time = stopwatch.read();
		if (time != 0) {
			LOG.debug("found " + moves.size() + " moves in " + time);
		}
		return moves;
	}

	@Override
	public boolean isOpponentsKingInCheckAfterMove(Position posn, Move move, Square opponentsKing, BitSet emptySquares,
			SquareCache<CheckStates> checkCache, SquareCache<Boolean> discoveredCheckCache) {
		/*
		 * many moves have the same starting square. If we've already checked for discovered check for this square, then can use the cached result.
		 * (Discovered check only looks along one ray from move.from() to the opponent's king.)
		 */
		boolean isCheck = findRankOrFileCheck(posn, emptySquares, move, opponentsKing);
		if (!isCheck) {
			isCheck = findDiagonalCheck(posn, emptySquares, move, opponentsKing, checkCache);
		}
		// if it's already check, don't need to calculate discovered check
		if (!isCheck) {
			Boolean lookup = discoveredCheckCache.lookup(move.from());
			if (lookup != null) {
				isCheck = lookup;
			} else {
				isCheck = Position.checkForDiscoveredCheck(posn, move, getColour(), opponentsKing);
				discoveredCheckCache.store(move.from(), isCheck);
			}
		}
		return isCheck;
	}

	@Override
	public boolean attacksSquare(BitSet emptySquares, Square targetSq, SquareCache<CheckStates> checkCache) {
		for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
			if (attacksSquare(emptySquares, Square.fromBitIndex(i), targetSq, checkCache)) {
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
	 * @return true if targetSquare is attacked from startSquare, otherwise false.
	 */
	public static boolean attacksSquare(BitSet emptySquares, Square startSquare, Square targetSquare, SquareCache<CheckStates> checkCache) {
		if (attacksSquareRankOrFile(emptySquares, startSquare, targetSquare)) {
			return true;
		}
		if (attacksSquareDiagonally(emptySquares, startSquare, targetSquare, checkCache)) {
			return true;
		}
		return false;
	}
}