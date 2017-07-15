package org.rjo.chess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Starting point for 'Perft' tests.
 *
 * @author rich
 */
public class Perft {

	private static final Logger MOVE_LOGGER = LogManager.getLogger("MOVE-LOG");

	public static void main(
			String[] args) {
		// see PerftTest::posn6ply5
		// 5ply: 164.075.551 moves
		Game game = Fen.decode("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
		long start = System.currentTimeMillis();
		Map<String, Integer> moveMap = findMoves(game.getPosition(), Colour.WHITE, 5);
		long time = System.currentTimeMillis() - start;
		int moves = Perft.countMoves(moveMap);
		System.out
				.println(String.format(Locale.GERMANY, "5ply: %,12d moves (%,9d ms) (%7.1f moves/ms)", moves, time, ((moves * 1.0) / time)));
		if (moves != 164075551) {
			System.out.println("ERROR: wrong number of moves");
		}
	}

	/**
	 * Find the number of possible moves at the given depth, starting at the current position given by <code>game</code>. I.e., for a
	 * depth of 2 and start colour white, all of black's moves will be returned for each of the possible white moves. NB:
	 * Only leaf nodes are counted.
	 *
	 * @param posn a game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return a map containing all the start moves for the <code>sideToMove</code> and for each map entry, a number representing how
	 *         many leaf nodes there are from this starting move.
	 */
	public static Map<String, Integer> findMoves(
			Position posn,
			Colour sideToMove,
			int depth) {
		if (depth < 1) {
			throw new IllegalArgumentException("depth must be >= 1");
		}
		Map<String, Integer> moveMap = new HashMap<>();
		for (Move move : posn.findMoves(sideToMove)) {
			logMove(depth, move, posn);
			Position posnAfterMove = posn.move(move);
			int nbrMoves = findMovesInternal(posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1);
			moveMap.put(move.toString(), nbrMoves);
		}
		return moveMap;
	}

	/**
	 * Helper routine to return the total number of moves found, given a map as returned from findMoves.
	 */
	public static int countMoves(
			Map<String, Integer> moveMap) {
		int nbrMoves = 0;
		for (String move : moveMap.keySet()) {
			nbrMoves += moveMap.get(move);
		}
		return nbrMoves;
	}

	/**
	 * Find the number of possible moves at the given depth, starting at the current position given by <code>game</code>. I.e., for a
	 * depth of 2 and start colour white, all of black's moves will be returned for each of the possible white moves. NB:
	 * Only leaf nodes are counted.
	 *
	 * @param posn the game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return the total number of moves (leaf nodes) found from this position.
	 */
	private static int findMovesInternal(
			final Position posn,
			Colour sideToMove,
			int depth) {
		if (depth == 0) {
			return 1;
		}
		int totalMoves = 0;
		for (Move move : posn.findMoves(sideToMove)) {
			logMove(depth, move, posn);
			Position posnAfterMove = posn.move(move);
			totalMoves += findMovesInternal(posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1);
		}
		return totalMoves;
	}

	private static void logMove(
			int depth,
			Move move,
			Position posn) {
		if (MOVE_LOGGER.isDebugEnabled()) {
			MOVE_LOGGER.debug(depth + " " + move + " " + Fen.encode(posn));
		}
	}

	/**
	 * Like {@link #findMoves(Game, Colour, int)} but with the option of storing the moves in a file. Uses more memory than
	 * the other method and is therefore not recommended. <b>This is mainly for PerftTests</b>. We return List of String
	 * instead of List of Move to try to conserve memory.
	 *
	 * @param game the game
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return the list of possible moves at the given depth. The string representation is stored in order to save memory.
	 * @throws IOException if can't write to temp file
	 */
	public static List<String> findMovesDebug(
			Game game,
			Colour sideToMove,
			int depth) throws IOException {
		File file = File.createTempFile("perft", null);
		System.out.println("writing to " + file);
		try (Writer debugWriter = new BufferedWriter(new FileWriter(file))) {
			return findMovesInternalDebug(game.getPosition(), sideToMove, depth, new ArrayDeque<String>(1000), new ArrayList<String>(500000),
					debugWriter);
		}
	}

	/**
	 * Internal method for finding the number of possible moves at the given depth.
	 *
	 * @param posn the game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @param movesSoFar for debugging purposes: the moves up to this point
	 * @param totalMoves stores all moves found
	 * @param debugWriter if not null, debug-info will be written to this file
	 * @return the list of possible moves at the given depth. The string representation is stored in order to save memory.
	 */
	private static List<String> findMovesInternalDebug(
			Position posn,
			Colour sideToMove,
			int depth,
			Deque<String> movesSoFar,
			List<String> totalMoves,
			Writer debugWriter) {
		if (depth == 0) {
			return new ArrayList<>();
		}
		// movesAtThisLevel and movesSoFar are only used for "logging"
		List<String> movesAtThisLevel = new ArrayList<>(10000);
		for (Move move : posn.findMoves(sideToMove)) {
			if (debugWriter != null) {
				movesSoFar.add(move.toString());
			}
			Position posnAfterMove = posn.move(move, debugWriter);

			List<String> movesFromThisPosn = findMovesInternalDebug(posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1, movesSoFar,
					totalMoves, debugWriter);
			if (movesFromThisPosn.isEmpty()) {
				totalMoves.add(move.toString());
				if (debugWriter != null) {
					movesAtThisLevel.add(move.toString());
				}
			}
			int size = totalMoves.size();
			if (size % 500000 == 0) {
				System.out.println(size);
			}

			if (debugWriter != null) {
				movesSoFar.removeLast();
			}
		}

		if (debugWriter != null) {
			if (!movesAtThisLevel.isEmpty()) {
				boolean check = false;
				boolean capture = false;
				if (!movesSoFar.isEmpty()) {
					check = movesSoFar.peekLast().contains("+");
				}
				if (!movesSoFar.isEmpty()) {
					capture = movesSoFar.peekLast().contains("x");
				}
				try {
					debugWriter.write((check ? "CHECK" : "") + (capture ? "CAPTURE" : "") + " moves: " + movesSoFar + " -> "
							+ movesAtThisLevel.size() + ":" + movesAtThisLevel + System.lineSeparator());
				} catch (IOException e) {
					throw new RuntimeException("could not write to file", e);
				}
			}
		}
		return totalMoves;
	}
}
