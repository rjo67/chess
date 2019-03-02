package org.rjo.chess;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.base.Move;

public class MoveTestUtil {

	private MoveTestUtil() {

	}

	/**
	 * Given a list of moves, returns a list containing the checking moves contained therein.
	 *
	 * @param moves the list of moves
	 * @return a list of the checking moves
	 */
	public static List<Move> getChecksMove(List<Move> moves) {
		List<Move> checks = new ArrayList<>();
		for (Move move : moves) {
			if (move.isCheck()) {
				checks.add(move);
			}
		}
		return checks;
	}

	/**
	 * Given a list of moves, returns a list containing the captures contained therein.
	 *
	 * @param moves the list of moves
	 * @return a list of the captures
	 */
	public static List<Move> getCapturesMove(List<Move> moves) {
		List<Move> captures = new ArrayList<>();
		for (Move move : moves) {
			if (move.isCapture()) {
				captures.add(move);
			}
		}
		return captures;
	}

	/**
	 * Given a list of moves, returns a list containing the checking moves contained therein.
	 *
	 * @param moves the list of moves
	 * @return a list of the checking moves
	 */
	public static List<String> getChecksString(List<String> moves) {
		List<String> checks = new ArrayList<>();
		for (String move : moves) {
			if (move.contains("+")) {
				checks.add(move);
			}
		}
		return checks;
	}

	/**
	 * Given a list of moves, returns a list containing the captures contained therein.
	 *
	 * @param moves the list of moves
	 * @return a list of the captures
	 */
	public static List<String> getCapturesString(List<String> moves) {
		List<String> captures = new ArrayList<>();
		for (String move : moves) {
			if (move.contains("x")) {
				captures.add(move);
			}
		}
		return captures;
	}
}
