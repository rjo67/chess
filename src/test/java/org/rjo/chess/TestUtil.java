package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.rjo.chess.pieces.PieceType;

public class TestUtil {

	public final static Predicate<Move> NOOP_FILTER = (move -> false);
	public final static Predicate<Move> KING_FILTER = (move -> move.getPiece() == PieceType.KING);
	public final static Predicate<Move> PAWN_FILTER = (move -> move.getPiece() == PieceType.PAWN);
	public final static Predicate<Move> KNIGHT_FILTER = (move -> move.getPiece() == PieceType.KNIGHT);

	private TestUtil() {
	}

	public static void checkMoves(List<Move> moves,
			String... requiredMoves) {
		checkMoves(moves, NOOP_FILTER, requiredMoves);
	}

	public static void checkMoves(List<Move> moves,
			Predicate<Move> moveFilter,
			String... requiredMoves) {
		checkMoves(moves, new HashSet<>(Arrays.asList(requiredMoves)), moveFilter);
	}

	/**
	 * Checks that all <code>moves</code> are present in <code>requiredMoves</code>, and there aren't any superfluous moves
	 * in either collection.
	 *
	 * @param moves the moves found
	 * @param requiredMoves the required moves
	 * @param moveFilter an optional predicate to further filter <code>moves</code>, e.g. in order to remove any king moves
	 *           if we're only interested in pawn moves
	 */
	private static void checkMoves(List<Move> moves,
			Set<String> requiredMoves,
			Predicate<Move> moveFilter) {
		// clone moves so as to avoid losing the move list for later tests
		List<Move> moveClone = new ArrayList<>(moves);
		Iterator<Move> iter = moveClone.iterator();
		while (iter.hasNext()) {
			Move m = iter.next();
			if (requiredMoves.contains(m.toString())) {
				requiredMoves.remove(m.toString());
				iter.remove();
			} else if (moveFilter.test(m)) {
				iter.remove();
			}
		}
		assertTrue(requiredMoves.isEmpty(),
				"not all required moves found: " + requiredMoves + (moveClone.isEmpty() ? "" : ". Input-Moves not processed: " + moveClone)				);
		// all required moves found but still some input moves left over?
		assertTrue(moveClone.isEmpty(),"unexpected moves found: " + moveClone); 
	}
}
