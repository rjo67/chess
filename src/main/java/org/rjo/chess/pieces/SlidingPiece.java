package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.CheckStates;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayInfo;
import org.rjo.chess.ray.RayUtils;

/**
 * Represents the pieces which can move over a greater distance: rooks, bishops, queens.
 *
 * @author rich
 */
public abstract class SlidingPiece extends AbstractBitBoardPiece {

	// lookup table for each square sq1, storing the DIAGONAL ray (or null) for every other square relative to sq1
	private final static Ray[][] DIAGONAL_RAYS_BETWEEN_SQUARES = new Ray[64][64];
	// lookup table for each square sq1, storing the HORIZONTAL/VERTICAL ray (or null) for every other square relative to
	// sq1
	private final static Ray[][] ORTHOGONAL_RAYS_BETWEEN_SQUARES = new Ray[64][64];

	static {
		for (int sq1 = 0; sq1 < 64; sq1++) {
			for (int sq2 = 0; sq2 < 64; sq2++) {
				if (sq1 == sq2) {
					continue;
				}
				Ray ray = RayUtils.getRay(Square.fromBitIndex(sq1), Square.fromBitIndex(sq2));
				if (ray != null) {
					if (ray.isDiagonal()) {
						DIAGONAL_RAYS_BETWEEN_SQUARES[sq1][sq2] = ray;
					} else {
						ORTHOGONAL_RAYS_BETWEEN_SQUARES[sq1][sq2] = ray;
					}
				}
			}
		}
	}

	protected SlidingPiece(Colour colour, PieceType type) {
		super(colour, type);
	}

	/**
	 * This checks all pieces in the given bitset to see if they can attack the given <code>targetSquare</code> along rank or file,
	 * taking into account any intervening pieces.
	 *
	 * @param pieces which pieces are available. This should represent the rooks and queens in the game.
	 * @param emptySquares which squares are currently empty.
	 * @param targetSquare which square should be attacked
	 * @return true if at least one of the given pieces can attack the target square along a rank or file.
	 */
	public static boolean attacksSquareOnRankOrFile(
			BitSet pieces,
			BitSet emptySquares,
			Square targetSquare) {
		// version using canReachTargetSquare is slower than
		// attacksSquareRankOrFile...

		// final BitSet targetSquareBitSet = new BitSet(64);
		// targetSquareBitSet.set(targetSquare.bitIndex());
		// for (MoveHelper helper : new MoveHelper[] {
		// NorthMoveHelper.instance(), EastMoveHelper.instance(),
		// SouthMoveHelper.instance(), WestMoveHelper.instance() }) {
		// if (canReachTargetSquare(pieces, emptySquares, helper,
		// targetSquareBitSet)) {
		// return true;
		// }
		// }
		// return false;
		for (int i = pieces.nextSetBit(0); i >= 0; i = pieces.nextSetBit(i + 1)) {
			if (attacksSquareRankOrFile(emptySquares, Square.fromBitIndex(i), targetSquare)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This checks all pieces in the given bitset to see if they can attack the given <code>targetSquare</code> along a diagonal,
	 * taking into account any intervening pieces.
	 *
	 * @param bishopsAndQueens which pieces are available. This should represent the bishops and queens in the game.
	 * @param emptySquares which squares are currently empty.
	 * @param targetSquare which square should be attacked
	 * @return true if at least one of the given pieces can attack the target square along a diagonal.
	 */
	public static boolean attacksSquareOnDiagonal(
			BitSet bishopsAndQueens,
			BitSet emptySquares,
			Square targetSquare) {
		for (int i = bishopsAndQueens.nextSetBit(0); i >= 0; i = bishopsAndQueens.nextSetBit(i + 1)) {
			if (attacksSquareDiagonally(emptySquares, Square.fromBitIndex(i), targetSquare, null /* TODO checkCache */)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for moves in the direction specified by the {@link Ray} implementation. This is for rooks, bishops, and
	 * queens.
	 *
	 * @param posn state of the board
	 * @param ray the ray (direction) in which to search
	 * @return the moves found
	 */
	protected List<Move> search(
			Position posn,
			Ray ray) {
		List<Move> moves = new ArrayList<>(30);

		final Colour opponentsColour = Colour.oppositeColour(getColour());
		/*
		 * for each piece, use the ray to find emptySquares / firstPiece on the ray
		 */
		BitSet emptySquares = posn.getTotalPieces().flip();
		for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
			Square fromSquareIndex = Square.fromBitIndex(i);

			RayInfo info = RayUtils.findFirstPieceOnRay(getColour(), emptySquares, posn.getAllPieces(getColour()).getBitSet(), ray, i);
			// add 'emptySquares' from result as normal moves
			for (int emptySquareIndex : info.getEmptySquares()) {
				moves.add(new Move(this.getType(), getColour(), fromSquareIndex, Square.fromBitIndex(emptySquareIndex)));
			}
			// if an opponent's piece was also found, add this as capture
			if (info.foundPiece() && (info.getColour() == opponentsColour)) {
				Square sqIndex = Square.fromBitIndex(info.getIndexOfPiece());
				moves.add(new Move(this.getType(), getColour(), fromSquareIndex, sqIndex, posn.pieceAt(sqIndex, opponentsColour)));
			}
		}

		return moves;
	}

	/**
	 * Checks if the given move would place the opponent's king in check.
	 * <p>
	 * This is for bishop-type moves.
	 *
	 * @param posn the position. Only used if emptySquares is null.
	 * @param emptySquares bitset of all empty squares. if null, will be created from posn.getTotalPieces.flip(). If not
	 *           null, <code>posn</code> is not used.
	 * @param move the move
	 * @param opponentsKing where the opponent's king is
	 * @return true if this move is a check
	 */
	protected boolean findDiagonalCheck(
			Position posn,
			BitSet emptySquares,
			Move move,
			Square opponentsKing,
			SquareCache<CheckStates> checkCache) {

		/**
		 * two optimizations: <br>
		 * 1) if the ray move.from <-> king and move.to <-> king is the same, then it can only be check if we've captured a
		 * piece <br>
		 * 2) if the ray move.from <-> move.to is the opposite to move.to <-> king, then it can't be check
		 * <p>
		 * See for example r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 <br>
		 * case 1: Bc4-d5 cannot be check (same diagonal and not captured a piece)<br>
		 * case 1a: Bc4xf7 is of course check<br>
		 * case 2: A move Bc4-b3 cannot be check (moved on the same diagonal away from the opponent's king)
		 * <p>
		 */

		Ray fromDestinationToKing = DIAGONAL_RAYS_BETWEEN_SQUARES[move.to().bitIndex()][opponentsKing.bitIndex()];
		if (fromDestinationToKing == null) {
			checkCache.store(move.to(), CheckStates.NOT_CHECK);
			return false;
		}
		Ray fromOriginToKing = DIAGONAL_RAYS_BETWEEN_SQUARES[move.from().bitIndex()][opponentsKing.bitIndex()];
		// case 1:
		if (!((fromDestinationToKing == null) || (fromOriginToKing == null))) {
			if (fromOriginToKing == fromDestinationToKing) {
				// move.from <-> king and move.to <-> king have the same ray
				if (!emptySquares.get(move.to().bitIndex())) {
					// if there are now just empty squares between move.to and the king, this is a CHECK_IF_CAPTURE. Otherwise NOT_CHECK
					Iterator<Integer> iter = fromDestinationToKing.squaresFrom(move.to());
					boolean foundPiece = false;
					boolean foundKing = false;
					while (iter.hasNext() && !foundPiece && !foundKing) {
						Integer sqIndex = iter.next();
						if (sqIndex == opponentsKing.bitIndex()) {
							foundKing = true;
						} else if (!emptySquares.get(sqIndex)) {
							foundPiece = true;
						}
					}
					if (foundPiece) {
						checkCache.store(move.to(), CheckStates.NOT_CHECK);
						return false;
					} else if (foundKing) {
						// no intervening pieces
						checkCache.store(move.to(), CheckStates.CHECK_IF_CAPTURE);
						return true;
					} else {
						throw new IllegalStateException("exited loop without finding the king");
					}
				} else {
					// move.from <-> king and move.to <-> king has the same ray, so cannot be check since didn't capture a piece
					checkCache.store(move.to(), CheckStates.NOT_CHECK);
					return false;
				}
			}

			// case 2:
			// TODO this is only correct in context of above 'if'
			Ray fromDestinationToOrigin = RayUtils.getRay(move.to(), move.from());
			if (fromDestinationToOrigin.oppositeOf(fromDestinationToKing)) {
				checkCache.store(move.to(), CheckStates.NOT_CHECK);
				return false;
			}
		}
		return attacksSquareDiagonally(emptySquares == null ? posn.getTotalPieces().flip() : emptySquares, move.to(), opponentsKing,
				checkCache);
	}

	/**
	 * Checks if a bishop/queen on the given startSquare attacks the given targetSquare, i.e. the target square can be
	 * reached (diagonally) from the start square and there are no intervening pieces.
	 * <p>
	 * NB <b>do not use this method for checks</b>, see instead
	 * {@link #findDiagonalCheck(Position, BitSet, Move, Square, SquareCache)}.
	 *
	 * @param emptySquares the empty squares of the board
	 * @param startSquare start square
	 * @param targetSquare target square
	 * @param checkCache (optional -- but not null) will be updated with results from the search.
	 * @return true if the target square is attacked (diagonally) from the start square.
	 */
	protected static boolean attacksSquareDiagonally(
			BitSet emptySquares,
			Square startSquare,
			Square targetSquare,
			SquareCache<CheckStates> checkCache) {
		// give up straight away if start and target are the same
		if (startSquare == targetSquare) {
			return false;
		}
		// abort immediately if we've already processed this startSquare
		CheckStates overallState = checkCache.lookup(startSquare);
		if (overallState != null) {
			return overallState == CheckStates.CHECK || overallState == CheckStates.CHECK_IF_CAPTURE;
		}
		Ray ray = DIAGONAL_RAYS_BETWEEN_SQUARES[startSquare.bitIndex()][targetSquare.bitIndex()];
		if (ray == null) {
			checkCache.store(startSquare, CheckStates.NOT_CHECK);
			return false;
		}
		// if move.from() and move.to() are on the same ray, then it can't be check
		Iterator<Integer> squaresFrom = ray.squaresFrom(startSquare);
		boolean finished = false;
		List<Integer> squaresVisited = new ArrayList<>();
		squaresVisited.add(startSquare.bitIndex());
		while (squaresFrom.hasNext() && !finished) {
			CheckStates result;
			int currentSquare = squaresFrom.next();
			if (currentSquare == targetSquare.bitIndex()) {
				overallState = CheckStates.CHECK;
				finished = true;
			} else {
				result = checkCache.lookup(currentSquare);
				// if already in cache, does not get added to squaresVisited to simplify the logic at the end
				if (result != null) {
					overallState = result;
					finished = true;
				} else {
					if (!emptySquares.get(currentSquare)) {
						overallState = CheckStates.NOT_CHECK;
						finished = true;
					} else {
						squaresVisited.add(currentSquare);
					}
				}
			}
		}
		switch (overallState) {
		case CHECK_IF_CAPTURE:
			// all squares visited are set to NOT_CHECK, since we didn't capture the piece
			squaresVisited.stream().forEach(sq -> checkCache.store(sq, CheckStates.NOT_CHECK));
			break;
		case CHECK:
			squaresVisited.stream().forEach(sq -> checkCache.store(sq, CheckStates.CHECK));
			// special case: if we captured a piece on the start square, then record this as CHECK_WITH_CAPTURE
			if (!emptySquares.get(startSquare.bitIndex())) {
				checkCache.store(startSquare, CheckStates.CHECK_IF_CAPTURE);
			}
			break;
		case NOT_CHECK:
			squaresVisited.stream().forEach(sq -> checkCache.store(sq, CheckStates.NOT_CHECK));
			break;
		}

		return overallState == CheckStates.CHECK;
	}

	/**
	 * Checks if the given move would place the opponent's king in check, i.e. the destination square of the move attacks
	 * the location of the king along a rank or file.
	 * <p>
	 * This is for rook-type moves.
	 *
	 * @param posn the position. Only used if emptySquares is null.
	 * @param emptySquares bitset of all empty squares. if null, will be created from posn.getTotalPieces.flip(). In this
	 *           case posn is not used.
	 * @param move the move
	 * @param opponentsKing where the opponent's king is
	 * @return true if this move is a check
	 */
	protected boolean findRankOrFileCheck(
			Position posn,
			BitSet emptySquares,
			Move move,
			Square opponentsKing) {
		// abort if dest sq rank/file is not the same as the king's rank/file
		if (move.to().file() == opponentsKing.file() || move.to().rank() == opponentsKing.rank()) {
			return attacksSquareRankOrFile(emptySquares == null ? posn.getTotalPieces().flip() : emptySquares, move.to(), opponentsKing);
		} else {
			return false;
		}
	}

	/**
	 * Checks if a rook/queen on the given startSquare attacks the given targetSquare, i.e. on the same rank or file and no
	 * intervening pieces. This is for rook-type moves i.e. straight along files or ranks.
	 *
	 * @param emptySquares a bit set representing the empty squares on the board
	 * @param startSquare start square
	 * @param targetSquare target square
	 * @return true if the target square is attacked (straight-line) from the start square.
	 */
	// public, since King need this too for castling
	public static boolean attacksSquareRankOrFile(
			BitSet emptySquares,
			Square startSquare,
			Square targetSquare) {
		// give up straight away if start and target are the same
		if (startSquare == targetSquare) {
			return false;
		}
		Ray ray = ORTHOGONAL_RAYS_BETWEEN_SQUARES[startSquare.bitIndex()][targetSquare.bitIndex()];
		if (ray == null) {
			return false;
		}
		Iterator<Integer> squaresFrom = ray.squaresFrom(startSquare);
		while (squaresFrom.hasNext()) {
			int nextSquare = squaresFrom.next();
			if (nextSquare == targetSquare.bitIndex()) {
				return true;
			} else if (!emptySquares.get(nextSquare)) {
				return false;
			}
		}
		return false;
	}

}
