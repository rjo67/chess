package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.rjo.chess.BitBoard;
import org.rjo.chess.CheckRestriction;
import org.rjo.chess.Colour;
import org.rjo.chess.KingCheck;
import org.rjo.chess.Move;
import org.rjo.chess.Move.CheckInformation;
import org.rjo.chess.Position;
import org.rjo.chess.PositionCheckState;
import org.rjo.chess.Square;
import org.rjo.chess.util.BitSetFactory;
import org.rjo.chess.util.BitSetUnifier;
import org.rjo.chess.util.SquareCache;

/**
 * Stores information about the knights (still) in the game.
 *
 * @author rich
 */
public class Knight extends AbstractSetPiece {

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 320;

	/**
	 * stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function
	 */
	// Important: array value [0] corresponds to square a1; [63] == h8.
	private static int[] SQUARE_VALUE =
	// @formatter:off
		new int[] {
				-50, -40, -30, -30, -30, -30, -40, -50,
				-40, -20,   0,   5,   5,   0, -20, -40,
				-30,   5,  10,  15,  15,  10,   5, -30,
				-30,   0,  15,  20,  20,  15,   0, -30,
				-30,   5,  15,  20,  20,  15,   5, -30,
				-30,   0,  10,  15,  15,  10,   0, -30,
				-40, -20,   0,   0,   0,   0, -20, -40,
				-50, -40, -30, -30, -30, -30, -40, -50, };
	// @formatter:on

	/**
	 * Stores for each square on the board the possible moves for a knight on that square.
	 */
	private static final BitSetUnifier[] knightMoves = new BitSetUnifier[64];

	// set up knight moves look up table
	static {
		for (int i = 0; i < 64; i++) {
			knightMoves[i] = BitSetFactory.createBitSet(64);
			knightMoves[i].set(i);

			// LHS: blank first file for -10 and +6 - blank first and 2nd file for -17 and +15
			// RHS: blank last file for +10 and -6 - blank 7th and 8th file for +17 and -15.
			// Don't need to blank ranks, these just 'drop off' during the bit shift.

			BitSetUnifier[] work = new BitSetUnifier[8];

			// to avoid wrapping:
			// - work[0,1] == file one blanked
			// - work[2,3] == file two blanked as well
			// - work[4,5] == file 8 blanked
			// - work[6,7] == file 7 blanked as well
			work[0] = (BitSetUnifier) knightMoves[i].clone();
			work[0].and(BitBoard.EXCEPT_FILE[0]);
			work[2] = (BitSetUnifier) work[0].clone();
			work[2].and(BitBoard.EXCEPT_FILE[1]);

			// store another copy
			work[1] = (BitSetUnifier) work[0].clone();
			work[3] = (BitSetUnifier) work[2].clone();

			work[0] = BitSetHelper.shift(work[0], 15); // file-1,rank+2
			work[1] = BitSetHelper.shift(work[1], -17);// file-1,rank-2
			work[2] = BitSetHelper.shift(work[2], 6);// file-2,rank+1
			work[3] = BitSetHelper.shift(work[3], -10);// file-2,rank-1

			work[4] = (BitSetUnifier) knightMoves[i].clone();
			work[4].and(BitBoard.EXCEPT_FILE[7]);
			work[6] = (BitSetUnifier) work[4].clone();
			work[6].and(BitBoard.EXCEPT_FILE[6]);

			// store another copy
			work[5] = (BitSetUnifier) work[4].clone();
			work[7] = (BitSetUnifier) work[6].clone();

			work[4] = BitSetHelper.shift(work[4], 17); // file+1,rank+2
			work[5] = BitSetHelper.shift(work[5], -15);// file+1,rank-2
			work[6] = BitSetHelper.shift(work[6], 10);// file+2,rank+1
			work[7] = BitSetHelper.shift(work[7], -6);// file+2,rank-1

			// store results
			knightMoves[i].clear(i); // clear the start position
			for (BitSetUnifier element : work) {
				knightMoves[i].or(element);
			}
		}
	}

	/**
	 * Constructs the Knight class -- with no pieces on the board. Delegates to Knight(Colour, boolean) with parameter
	 * false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public Knight(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the Knight class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public Knight(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the Knight class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Knight(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Knight class with the required squares (can be null) or the default start squares. Setting
	 * <code>startPosition</code> true has precedence over <code>startSquares</code>.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
	 *           ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Knight(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.KNIGHT);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = getColour() == Colour.WHITE ? new Square[] { Square.b1, Square.g1 }
				: new Square[] { Square.b8, Square.g8 };
		initPosition(requiredSquares);
	}

	@Override
	public int calculatePieceSquareValue() {
		return pieceSquareValue(pieces, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	@Override
	public List<Move> findMoves(Position posn,
			CheckInformation kingInCheck,
			CheckRestriction checkRestriction) {
		final Square myKing = posn.getKingPosition(colour);
		final Colour oppositeColour = Colour.oppositeColour(colour);

		List<Move> moves = findPotentialMoves(posn, checkRestriction);

		/*
		 * Iterates over all possible moves/captures. If the move would leave our king in check, it is illegal and is removed.
		 */
		Iterator<Move> iter = moves.listIterator();
		while (iter.hasNext()) {
			Move move = iter.next();
			if (KingCheck.isKingInCheck(posn, move, oppositeColour, myKing, kingInCheck.isCheck())) {
				iter.remove();
			}
		}
		return moves;
	}

	@Override
	public List<Move> findPotentialMoves(Position posn,
			CheckRestriction checkRestriction) {
		List<Move> moves = new ArrayList<>(20);
		final Colour oppositeColour = Colour.oppositeColour(getColour());
		final BitSetUnifier allMyPiecesBitSet = posn.getAllPieces(getColour()).getBitSet();
		final BitSetUnifier allOpponentsPiecesBitSet = posn.getAllPieces(oppositeColour).getBitSet();

		/*
		 * for each knight on the board, finds its moves using the lookup table
		 */
		for (Square knightStartSquare : pieces) {
			BitSetUnifier possibleMoves = knightMoves[knightStartSquare.bitIndex()];

			/*
			 * Iterates over all possible moves and stores them as moves or captures
			 */
			for (int k = possibleMoves.nextSetBit(0); k >= 0; k = possibleMoves.nextSetBit(k + 1)) {
				// move can't be to a square with a piece of the same colour on it
				if (!allMyPiecesBitSet.get(k)) {
					// restrict squares i/c of check
					if (checkRestriction.getSquareRestriction().get(k)) {
						Square targetSquare = Square.fromBitIndex(k);
						Move move;
						// decide if capture or not
						if (allOpponentsPiecesBitSet.get(k)) {
							// capture
							move = new Move(PieceType.KNIGHT, getColour(), knightStartSquare, targetSquare,
									posn.pieceAt(targetSquare, oppositeColour));
						} else {
							move = new Move(PieceType.KNIGHT, getColour(), knightStartSquare, targetSquare);
						}
						moves.add(move);
					}
				}
			}
		}
		return moves;
	}

	@Override
	public CheckInformation isOpponentsKingInCheckAfterMove(Position posn,
			Move move,
			Square opponentsKing,
			@SuppressWarnings("unused") BitSetUnifier emptySquares,
			@SuppressWarnings("unused") PositionCheckState checkCache,
			SquareCache<Boolean> discoveredCheckCache) {

		if (checkIfMoveAttacksSquare(move, opponentsKing.bitIndex())) {
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

	/**
	 * Checks whether the given move attacks the given square.
	 *
	 * @param move the move
	 * @param targetSquareIndex index of the target square
	 * @return true if the given move attacks the given square.
	 */
	// also required by Pawn
	public static boolean checkIfMoveAttacksSquare(Move move,
			int targetSquareIndex) {
		// check if the target square is a knight move away from the destination
		// square of the move
		return knightMoves[move.to().bitIndex()].get(targetSquareIndex);
	}

	@Override
	public boolean attacksSquare(@SuppressWarnings("unused") BitSetUnifier emptySquares,
			Square targetSq,
			@SuppressWarnings("unused") PositionCheckState checkCache) {
		Optional<Square> found = pieces.stream().filter(sq -> squareIsReachableFromSquare(sq, targetSq)).findAny();
		return found.isPresent();
	}

	/**
	 * returns true if a knight on <code>startSq</code> attacks <code>targetSq</code>.
	 *
	 * @param startSq start square
	 * @param targetSq target square
	 * @return true if target square is attaced
	 */
	private boolean squareIsReachableFromSquare(Square startSq,
			Square targetSq) {
		BitSetUnifier possibleMovesFromTargetSquare = knightMoves[targetSq.bitIndex()];
		return possibleMovesFromTargetSquare.get(startSq.bitIndex());
	}

	/**
	 * Whether one or more of the knights described in <code>knights</code> attack the square <code>targetSq</code>.
	 *
	 * @param targetSq square to be attacked
	 * @param knights bitset describing where the knights are
	 * @return true if <code>targetSq</code> is attacked by one or more knights
	 */
	public static boolean attacksSquare(Square targetSq,
			BitSetUnifier knights) {
		BitSetUnifier possibleMovesFromTargetSquare = knightMoves[targetSq.bitIndex()];
		return possibleMovesFromTargetSquare.intersects(knights);
	}

}
