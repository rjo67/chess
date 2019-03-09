package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.SystemFlags;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.Ray;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.check.CheckStates;

/**
 * Represents the pieces which can move over a greater distance: rooks, bishops, queens.
 *
 * @author rich
 */
public abstract class SlidingPiece extends AbstractBitBoardPiece {

	protected SlidingPiece(Colour colour, PieceType type) {
		super(colour, type);
	}

	/**
	 * Searches for moves in the direction specified by the {@link Ray} implementation. This is for rooks, bishops, and
	 * queens.
	 * <p>
	 * <b>New implementation, using bitsets.</b>
	 * </p>
	 * <p>
	 * See http://www.craftychess.com/hyatt/bitmaps.html.
	 * <p>
	 * For a bishop on f5, and NW ray: <code>
	 * <pre>
	 *  diag_attacks = northWestRay[f5];
	 *  blockers=diag_attacks & occupied_squares;
	 *  blocking_square = FirstOne(blockers);
	 *  diag_attacks ^= northWestRay[blocking_square];
	 * </pre>
	 * </code>
	 *
	 * @param posn state of the board
	 * @param ray the ray (direction) in which to search
	 * @param checkRestriction info about the squares which come into consideration. Normally all are allowed. If the king
	 *           is in check then this object will contain only the squares which will potentially get out of check.
	 * @param isInCheck (tmp) flag to indicate if in check
	 * @return the moves found
	 */
	protected List<Move> search(Position posn,
			Ray ray,
			BitBoard checkRestriction,
			boolean isInCheck) {
		List<Move> moves = new ArrayList<>(30);
		for (int indexOfPiece = pieces.getBitSet().nextSetBit(0); indexOfPiece >= 0; indexOfPiece = pieces.getBitSet()
				.nextSetBit(indexOfPiece + 1)) {

			final Colour opponentsColour = Colour.oppositeColour(getColour());
			boolean blockingSquareContainsEnemyPiece = false;

			// from http://www.craftychess.com/hyatt/bitmaps.html:
			//
			// 'rayAttack' is the bitmap for the attacks in the ray's direction from the square 'indexOfPiece'.
			// blockers becomes a bitmap of any pieces sitting on this ray.
			// We find the first blocking piece, and then exclusive-OR attackBitBoard[blocking_square]
			// with the original 'rayAttack' which effectively 'cuts off' the attacks beyond that point.

			BitBoard rayAttack = new BitBoard(ray.getAttackBitBoard(indexOfPiece)); // clone

			// remove occupied squares along the ray
			BitBoard blockers = new BitBoard(ray.getAttackBitBoard(indexOfPiece)); // clone
			blockers.getBitSet().and(posn.getTotalPieces().getBitSet());

			// find blocking square, i.e. first square on this ray which contains a piece
			int blockingSquare;
			if (ray.getRayType().isBitIndicesIncrease()) {
				blockingSquare = blockers.getBitSet().nextSetBit(indexOfPiece);
			} else {
				blockingSquare = blockers.getBitSet().previousSetBit(indexOfPiece);
			}
			if (blockingSquare != -1) {
				// truncate the attacks beyond the blocking piece
				rayAttack.getBitSet().xor(ray.getAttackBitBoard(blockingSquare).getBitSet());
				blockingSquareContainsEnemyPiece = posn.getAllPieces(opponentsColour).get(blockingSquare);
			}

			// remove squares in checkRestriction
			if (isInCheck) {
				rayAttack.getBitSet().and(checkRestriction.getBitSet());
			}

			// add moves.
			// Possible implementations:
			//
			// (1) start at 'fromSquare'. Then however need to iterate in the correct direction, e.g.:
			//			if ((ray.getRayType() == RayType.NORTHWEST) || (ray.getRayType() == RayType.NORTHEAST)) {
			//				for (int sqIndex = rayAttack.getBitSet().nextSetBit(indexOfPiece); sqIndex >= 0; sqIndex = rayAttack.getBitSet()
			//							.nextSetBit(sqIndex + 1)) {
			//					addMove(moves, fromSquare, sqIndex, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn);
			// 			}
			//			} else {
			//				for (int sqIndex = indexOfPiece; (sqIndex = rayAttack.getBitSet().previousSetBit(sqIndex - 1)) >= 0;) {
			//					addMove(moves, fromSquare, sqIndex, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn);
			//				}
			//			}
			// or (2) only look at the squares along the ray in question (then need to query the bitset)
			//			int sqIndex = -99;
			//			Iterator<Integer> iter = ray.squaresFrom(indexOfPiece);
			//			while (iter.hasNext() && (sqIndex != blockingSquare)) /* abort as soon as we've processed the blocking square */ {
			//				sqIndex = iter.next();
			//				if (rayAttack.get(sqIndex)) {
			//					addMove(moves, fromSquare, sqIndex, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn);
			//				}
			//			}
			//
			// or (3) 'iterate' over the squares array.
			//			Square fromSquare = Square.fromBitIndex(indexOfPiece);
			//			for (int sqIndex2 : ray.squaresFromAsArray(indexOfPiece)) {
			//				if (rayAttack.get(sqIndex2)) {
			//					addMove(moves, fromSquare, sqIndex2, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn);
			//				} else if (sqIndex2 == blockingSquare) {
			//					break;
			//				}
			//			}
			Square fromSquare = Square.fromBitIndex(indexOfPiece);
			if (ray.getRayType().isBitIndicesIncrease()) {
				for (int sqIndex = rayAttack.getBitSet().nextSetBit(indexOfPiece); sqIndex >= 0; //
						sqIndex = rayAttack.getBitSet().nextSetBit(sqIndex + 1)) {
					if (addMove(moves, fromSquare, sqIndex, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn)) {
						break;
					}
				}
			} else {
				for (int sqIndex = indexOfPiece; (sqIndex = rayAttack.getBitSet().previousSetBit(sqIndex - 1)) >= 0;) {
					if (addMove(moves, fromSquare, sqIndex, blockingSquare, blockingSquareContainsEnemyPiece, opponentsColour, posn)) {
						break;
					}
				}
			}
		}
		return moves;
	}

	/**
	 * Add a move to <code>moves</code>.
	 *
	 * @param moves
	 * @param fromSquare
	 * @param sqIndex
	 * @param blockingSquare
	 * @param blockingSquareContainsEnemyPieceFinal
	 * @param opponentsColour
	 * @param posn
	 * @return true if blocking square reached, otherwise false.
	 */
	private boolean addMove(List<Move> moves,
			Square fromSquare,
			int sqIndex,
			int blockingSquare,
			boolean blockingSquareContainsEnemyPieceFinal,
			Colour opponentsColour,
			Position posn) {
		if (sqIndex == blockingSquare) {
			// if opponent's piece, add as capture
			if (blockingSquareContainsEnemyPieceFinal) {
				moves.add(new Move(type, colour, fromSquare, Square.fromBitIndex(sqIndex),
						posn.pieceAt(Square.fromBitIndex(sqIndex), opponentsColour)));
			}
			return true;
		} else {
			moves.add(new Move(type, colour, fromSquare, Square.fromBitIndex(sqIndex)));
			return false;
		}

	}

	/**
	 * Checks if the given move would place the opponent's king in check.
	 * <p>
	 * This is for bishop-type moves.
	 *
	 * @param posn the position. Only used if emptySquares is null.
	 * @param emptySquares bitset of all empty squares. if null, will be created from posn.getEmptySquares(). If not null,
	 *           <code>posn</code> is not used.
	 * @param move the move
	 * @param opponentsKing where the opponent's king is
	 * @return true if this move is a check
	 */
	protected boolean findDiagonalCheck(Position posn,
			BitSetUnifier emptySquares,
			Move move,
			Square opponentsKing,
			PositionCheckState checkCache) {

		/*
		 * two optimizations: <br> 1) if the ray move.from <-> king and move.to <-> king is the same, then it can only be check
		 * if we've captured a piece <br> 2) if the ray move.from <-> move.to is the opposite to move.to <-> king, then it can't
		 * be check <p> See for example r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 <br> case 1:
		 * Bc4-d5 cannot be check (same diagonal and not captured a piece)<br> case 1a: Bc4xf7 is of course check<br> case 2: A
		 * move Bc4-b3 cannot be check (moved on the same diagonal away from the opponent's king) <p>
		 */

		Ray destSquareToKing = RayUtils.getDiagonalRay(move.to(), opponentsKing);
		if (destSquareToKing == null) {
			checkCache.setNotCheck(null, move.to());
			return false;
		}
		// have we already processed this startSquare?
		if (checkCache.isCheckStatusKnownForSquare(move.to(), destSquareToKing.getRayType())) {
			return checkCache.squareHasCheckStatus(move.to(), destSquareToKing.getRayType());
		}
		Ray originSquareToKing = RayUtils.getDiagonalRay(move.from(), opponentsKing);
		// case 1:
		if (originSquareToKing != null) {
			if (originSquareToKing == destSquareToKing) {
				// move.from <-> king and move.to <-> king have the same ray
				if (!emptySquares.get(move.to().bitIndex())) {
					// if there are now just empty squares between move.to and the king, this is a CHECK_IF_CAPTURE. Otherwise NOT_CHECK
					Iterator<Integer> iter = destSquareToKing.squaresFrom(move.to());
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
						checkCache.setNotCheck(destSquareToKing.getRayType(), move.to());
						return false;
					} else if (foundKing) {
						// no intervening pieces
						checkCache.setCheckIfCapture(destSquareToKing.getRayType(), move.to());
						return true;
					} else {
						throw new IllegalStateException("exited loop without finding the king");
					}
				} else {
					// move.from <-> king and move.to <-> king has the same ray, so cannot be check since didn't capture a piece
					checkCache.setNotCheck(destSquareToKing.getRayType(), move.to());
					return false;
				}
			}

			// case 2:
			// TODO this is only correct in context of above 'if'
			Ray fromDestinationToOrigin = RayUtils.getRay(move.to(), move.from());
			if (fromDestinationToOrigin.oppositeOf(destSquareToKing)) {
				checkCache.setNotCheck(destSquareToKing.getRayType(), move.to());
				return false;
			}
		}
		return attacksSquareDiagonally(emptySquares == null ? posn.getEmptySquares() : emptySquares, move.to(), opponentsKing,
				checkCache, move.isCapture(), move.isPromotion());
	}

	/**
	 * Checks if a bishop/queen on the given startSquare attacks the given targetSquare, i.e. the target square can be
	 * reached (diagonally) from the start square and there are no intervening pieces.
	 * <p>
	 * NB <b>do not use this method for checks</b>, see instead
	 * {@link #findDiagonalCheck(Position, BitSetUnifier, Move, Square, PositionCheckState)}.
	 *
	 * @param emptySquares the empty squares of the board
	 * @param startSquare start square
	 * @param targetSquare target square
	 * @param checkCache (optional -- but not null) will be updated with results from the search.
	 * @param isCapture true if the move was a capture
	 * @param isPromotion true if the move was a promotion (important for the checkcache)
	 * @return true if the target square is attacked (diagonally) from the start square.
	 */
	public static boolean attacksSquareDiagonally(BitSetUnifier emptySquares,
			Square startSquare,
			Square targetSquare,
			PositionCheckState checkCache,
			boolean isCapture,
			boolean isPromotion) {
		// give up straight away if start and target are the same
		if (startSquare == targetSquare) {
			return false;
		}
		Ray ray = RayUtils.getDiagonalRay(startSquare, targetSquare);
		if (ray == null) {
			// do not set checkCache -- not a diagonal check but could be orthogonal
			return false;
		}
		// have we already processed this startSquare?
		if (checkCache.isCheckStatusKnownForSquare(startSquare, ray.getRayType())) {
			return checkCache.squareHasCheckStatus(startSquare, ray.getRayType());
		}
		// if move.from() and move.to() are on the same ray, then it can't be check
		Iterator<Integer> squaresFrom = ray.squaresFrom(startSquare);
		boolean finished = false;
		List<Integer> squaresVisited = new ArrayList<>();
		squaresVisited.add(startSquare.bitIndex());
		CheckStates overallCheckState = CheckStates.UNKNOWN;
		while (squaresFrom.hasNext() && !finished) {
			int currentSquare = squaresFrom.next();
			if (currentSquare == targetSquare.bitIndex()) {
				overallCheckState = CheckStates.CHECK;
				finished = true;
			} else {
				// if already in cache, does not get added to squaresVisited to simplify the logic at the end
				if (checkCache.isCheckStatusKnownForSquare(currentSquare, ray.getRayType())) {
					overallCheckState = checkCache.getCheckState(currentSquare, ray.getRayType());
					finished = true;
				} else {
					if (!emptySquares.get(currentSquare)) {
						overallCheckState = CheckStates.NOT_CHECK;
						finished = true;
					} else {
						squaresVisited.add(currentSquare);
					}
				}
			}
		}
		if (SystemFlags.USE_CHECK_STATE) {

			if (isPromotion) {
				// don't update check-cache for promotions

				// no-op
			} else {
				switch (overallCheckState) {
				case CHECK_IF_CAPTURE:
					// all squares visited are set to NOT_CHECK, since we didn't capture the piece
					checkCache.setNotCheck(ray.getRayType(), squaresVisited);
					break;
				case CHECK:
					checkCache.setCheck(ray.getRayType(), squaresVisited);
					// special case: if we captured a piece on the start square, then record this as CHECK_WITH_CAPTURE
					if (isCapture) {
						checkCache.setCheckIfCapture(ray.getRayType(), startSquare);
					}
					break;
				case NOT_CHECK:
					checkCache.setNotCheck(ray.getRayType(), squaresVisited);
					break;
				case UNKNOWN:
					throw new IllegalStateException(
							"overall check state has not been determined, startSquare: " + startSquare + ", targetSquare: "
									+ targetSquare + ", checkCache:\n" + checkCache);
				}
			}
		}
		return overallCheckState == CheckStates.CHECK;
	}

	/**
	 * Checks if the given move would place the opponent's king in check, i.e. the destination square of the move attacks
	 * the location of the king along a rank or file.
	 * <p>
	 * This is for rook-type moves.
	 *
	 * @param posn the position. Only used if emptySquares is null.
	 * @param emptySquares bitset of all empty squares. if null, will be created from posn.getEmptySquares().
	 * @param move the move
	 * @param opponentsKing where the opponent's king is
	 * @param checkCache checkCache
	 * @return true if this move is a check
	 */
	protected boolean findRankOrFileCheck(Position posn,
			BitSetUnifier emptySquares,
			Move move,
			Square opponentsKing,
			PositionCheckState checkCache) {
		// abort if dest sq rank/file is not the same as the king's rank/file
		if (move.to().file() == opponentsKing.file() || move.to().rank() == opponentsKing.rank()) {
			return attacksSquareRankOrFile(emptySquares == null ? posn.getEmptySquares() : emptySquares, move.to(), opponentsKing,
					checkCache, move.isCapture(), move.isPromotion());
		} else {
			checkCache.setNotCheck(null, move.to());
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
	 * @param checkCache check cache
	 * @param isCapture true if the move was a capture
	 * @param isPromotion true if the move was a promotion (important for the checkcache)
	 * @return true if the target square is attacked (straight-line) from the start square.
	 */
	// public, since King need this too for castling
	public static boolean attacksSquareRankOrFile(BitSetUnifier emptySquares,
			Square startSquare,
			Square targetSquare,
			PositionCheckState checkCache,
			boolean isCapture,
			boolean isPromotion) {
		// give up straight away if start and target are the same
		if (startSquare == targetSquare) {
			return false;
		}
		Ray ray = RayUtils.getOrthogonalRay(startSquare, targetSquare);
		if (ray == null) {
			// do not set checkCache, since it may contain info for other types of pieces (e.g. not orthogonal but diagonal check)
			return false;
		}
		// do we already know the status of the start square?
		if (checkCache.isCheckStatusKnownForSquare(startSquare, ray.getRayType())) {
			return checkCache.squareHasCheckStatus(startSquare, ray.getRayType());
		}
		Iterator<Integer> squaresFrom = ray.squaresFrom(startSquare);
		List<Integer> squaresVisited = new ArrayList<>();
		squaresVisited.add(startSquare.bitIndex());
		CheckStates overallCheckState = CheckStates.UNKNOWN;
		boolean finished = false;
		while (squaresFrom.hasNext() && !finished) {
			int currentSquare = squaresFrom.next();
			if (currentSquare == targetSquare.bitIndex()) {
				overallCheckState = CheckStates.CHECK;
				finished = true;
			} else {
				if (checkCache.isCheckStatusKnownForSquare(currentSquare, ray.getRayType())) {
					overallCheckState = checkCache.getCheckState(currentSquare, ray.getRayType());
					finished = true;
				} else {
					if (!emptySquares.get(currentSquare)) {
						overallCheckState = CheckStates.NOT_CHECK;
						finished = true;
					} else {
						squaresVisited.add(currentSquare);
					}
				}
			}
		}

		//
		// Update check-cache state.
		//

		if (SystemFlags.USE_CHECK_STATE) {

			if (isPromotion) {//&& ((ray.getRayType() == RayType.NORTH) || (ray.getRayType() == RayType.SOUTH))) {
				// The update is ignored in case of a pawn promotion where the pawn --> king is the north ray (south for white)
				// e.g. this position 8/3k4/8/8/8/5K2/3N1p1p/7r with black to move.
				// f2-f1=R is check, however setting the cache then leads to Rh1-f1 also being check...
				// Unfortunately don't know the move's colour here, so have to treat NORTH and SOUTH the same

				// no-op

			} else {
				switch (overallCheckState) {
				case CHECK_IF_CAPTURE:
					// all squares visited are set to NOT_CHECK, since we didn't capture the piece
					checkCache.setNotCheck(ray.getRayType(), squaresVisited);
					break;
				case CHECK:
					checkCache.setCheck(ray.getRayType(), squaresVisited);
					// special case: if we captured a piece on the start square, then record this as CHECK_WITH_CAPTURE
					if (isCapture) {
						checkCache.setCheckIfCapture(ray.getRayType(), startSquare);
					}
					break;
				case NOT_CHECK:
					checkCache.setNotCheck(ray.getRayType(), squaresVisited);
					break;
				case UNKNOWN:
					throw new IllegalStateException(
							"overall check state has not been determined, startSquare: " + startSquare + ", targetSquare: "
									+ targetSquare + ", checkCache:\n" + checkCache);
				}
			}
		}

		return overallCheckState == CheckStates.CHECK;
	}

}
