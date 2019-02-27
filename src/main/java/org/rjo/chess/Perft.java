package org.rjo.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Starting point for 'Perft' tests.
 *
 * @author rich
 */
public class Perft {

	private static final Logger MOVE_LOGGER = LogManager.getLogger("MOVE-LOG");

	/** flag to make sure logging is switched off when starting from main */
	private static boolean LOG_MOVES = true;

	public static final int DEFAULT_NBR_THREADS = 3;

	// see PerftTest::posn6ply5
	// 5ply: 164.075.551 moves
	private static int[] EXPECTED_MOVES = new int[] { 46, 2079, 89890, 3894594, 164075551 };
	private static final int REQD_DEPTH = 5;

	static class MoveResult {
		Move move;
		int nbrMoves;

		public MoveResult(Move move, int nbrMoves) {
			this.move = move;
			this.nbrMoves = nbrMoves;
		}
	}

	private Perft() {
	}

	public static void main(String[] args) {
		Game game = Fen.decode("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");
		int nbrThreads = DEFAULT_NBR_THREADS;
		LOG_MOVES = false;
		if (args.length == 1) {
			nbrThreads = Integer.parseInt(args[0]);
		}
		System.out.println(String.format("Perft::posn6ply%d starting (%d threads)...", REQD_DEPTH, nbrThreads));
		StopWatch sw = new StopWatch();
		sw.start();
		int moves = Perft.findAndCountMoves(game.getPosition(), Colour.WHITE, REQD_DEPTH, nbrThreads);
		sw.stop();
		long time = sw.getTime();
		System.out
				.println(
						String.format(Locale.GERMANY, "%dply: %,12d moves (%,9d ms) (%7.1f moves/ms) (%7.1f nanos/move)", REQD_DEPTH, moves, time,
								((moves * 1.0) / time), ((sw.getNanoTime() * 1.0) / moves)));
		if (moves != EXPECTED_MOVES[REQD_DEPTH - 1]) {
			System.out.println("ERROR: wrong number of moves");
		}
	}

	/**
	 * Finds and counts the moves (combination of {@link #findMoves(Position, Colour, int, int)} and {@link #countMoves(Map)}).
	 *
	 * @param posn a game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @param nbrThreads number of threads
	 * @return nbr of moves in the move map
	 */
	public static int findAndCountMoves(Position posn,
			Colour sideToMove,
			int depth,
			int nbrThreads) {
		Map<String, Integer> moveMap = findMoves(posn, sideToMove, depth, nbrThreads);
		return countMoves(moveMap);
	}

	/**
	 * Find the number of possible moves at the given depth, starting at the current position given by <code>game</code>.
	 * I.e., for a depth of 2 and start colour white, all of black's moves will be returned for each of the possible white
	 * moves. NB: Only leaf nodes are counted.
	 *
	 * @param posn a game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return a map containing all the start moves for the <code>sideToMove</code> and for each map entry, a number
	 *         representing how many leaf nodes there are from this starting move.
	 */
	public static Map<String, Integer> findMoves(Position posn,
			Colour sideToMove,
			int depth,
			int nbrThreads) {
		if (nbrThreads == 1) {
			return findMovesSingleThreaded(posn, sideToMove, depth);
		} else {
			return findMovesMultiThreaded(posn, sideToMove, depth, nbrThreads);
		}
	}

	// singlethreaded version. good for debugging, since the moves are generated in a well-defined order
	public static Map<String, Integer> findMovesSingleThreaded(Position posn,
			Colour sideToMove,
			int depth) {
		if (depth < 1) {
			throw new IllegalArgumentException("depth must be >= 1");
		}

		Map<String, Integer> moveMap = new HashMap<>();
		for (final Move move : posn.findMoves(sideToMove)) {
			logMove(depth, move, posn);
			Position posnAfterMove = posn.move(move);
			moveMap.put(move.toString(), findMovesInternal(move, posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1).nbrMoves);
		}
		return moveMap;
	}

	public static Map<String, Integer> findMovesMultiThreaded(Position posn,
			Colour sideToMove,
			int depth,
			int nbrThreads) {
		if (depth < 1) {
			throw new IllegalArgumentException("depth must be >= 1");
		}
		ExecutorService threadPool = Executors.newFixedThreadPool(nbrThreads);
		List<Future<MoveResult>> futures = new ArrayList<>(200);

		Map<String, Integer> moveMap = new HashMap<>();
		for (final Move move : posn.findMoves(sideToMove)) {
			logMove(depth, move, posn);
			Position posnAfterMove = posn.move(move);
			Callable<MoveResult> callable = () -> findMovesInternal(move, posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1);
			futures.add(threadPool.submit(callable));
		}
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(2, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (!threadPool.isTerminated()) {
				System.err.println("cancel non-finished tasks");
			}
			threadPool.shutdownNow();
		}
		futures.forEach(fut -> {
			try {
				moveMap.put(fut.get().move.toString(), fut.get().nbrMoves);
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		return moveMap;
	}

	/**
	 * Find the number of possible moves at the given depth, starting at the current position given by <code>game</code>.
	 * I.e., for a depth of 2 and start colour white, all of black's moves will be returned for each of the possible white
	 * moves. NB: Only leaf nodes are counted.
	 *
	 * @param posn the game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return the total number of moves (leaf nodes) found from this position.
	 */
	private static MoveResult findMovesInternal(final Move move,
			final Position posn,
			Colour sideToMove,
			int depth) {
		if (depth == 0) {
			return new MoveResult(move, 1);
		}
		int totalMoves = 0;
		for (Move newMove : posn.findMoves(sideToMove)) {
			logMove(depth, newMove, posn);
			Position posnAfterMove = posn.move(newMove);
			MoveResult moveResult = findMovesInternal(newMove, posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1);
			totalMoves += moveResult.nbrMoves;
		}
		return new MoveResult(move, totalMoves);
	}

	/**
	 * Helper routine to return the total number of moves found, given a map as returned from findMoves.
	 */
	public static int countMoves(Map<String, Integer> moveMap) {
		int nbrMoves = 0;
		for (String move : moveMap.keySet()) {
			nbrMoves += moveMap.get(move);
		}
		return nbrMoves;
	}

	private static void logMove(int depth,
			Move move,
			Position posn) {
		if (LOG_MOVES && MOVE_LOGGER.isDebugEnabled()) {
			//			MOVE_LOGGER.debug(depth + " " + move + " " + Fen.encode(posn) + "\n" + posn.getCheckState()[0] + "\n" + posn.getCheckState()[1]);
			MOVE_LOGGER.debug(depth + " " + move + " " + Fen.encode(posn));
		}
	}

	/**
	 * Like {@link #findMoves(Position, Colour, int, int)} but with the option of storing the moves in a file. (Now uses MOVE_LOGGER
	 * at level TRACE.) Uses more memory than the other method and is therefore not recommended. <b>This is mainly for
	 * PerftTests</b>. We return List of String instead of List of Move to try to conserve memory.
	 *
	 * @param game the game
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @return the list of possible moves at the given depth. The string representation is stored in order to save memory.
     */
	public static List<String> findMovesDebug(Game game,
			Colour sideToMove,
			int depth) {
		return findMovesInternalDebug(game.getPosition(), sideToMove, depth, new ArrayDeque<>(1000), new ArrayList<>(500000));

	}

	/**
	 * Internal method for finding the number of possible moves at the given depth.
	 *
	 * @param posn the game position
	 * @param sideToMove the starting colour
	 * @param depth the required depth to search
	 * @param movesSoFar for debugging purposes: the moves up to this point
	 * @param totalMoves stores all moves found
	 * @return the list of possible moves at the given depth. The string representation is stored in order to save memory.
	 */
	private static List<String> findMovesInternalDebug(Position posn,
			Colour sideToMove,
			int depth,
			Deque<String> movesSoFar,
			List<String> totalMoves) {
		if (depth == 0) {
			return new ArrayList<>();
		}
		// movesAtThisLevel and movesSoFar are only used for "logging"
		List<String> movesAtThisLevel = new ArrayList<>(10000);
		for (Move move : posn.findMoves(sideToMove)) {
			if (MOVE_LOGGER.isTraceEnabled()) {
				movesSoFar.add(move.toString());
			}
			Position posnAfterMove = posn.move(move);

			List<String> movesFromThisPosn = findMovesInternalDebug(posnAfterMove, Colour.oppositeColour(sideToMove), depth - 1, movesSoFar,
					totalMoves);
			if (movesFromThisPosn.isEmpty()) {
				totalMoves.add(move.toString());
				if (MOVE_LOGGER.isTraceEnabled()) {
					movesAtThisLevel.add(move.toString());
				}
			}
			int size = totalMoves.size();
			if (size % 500000 == 0) {
				System.out.println(size);
			}

			if (MOVE_LOGGER.isTraceEnabled()) {
				movesSoFar.removeLast();
			}
		}

		if (MOVE_LOGGER.isTraceEnabled() && !movesAtThisLevel.isEmpty()) {
			boolean check = false;
			boolean capture = false;
			if (!movesSoFar.isEmpty()) {
				check = movesSoFar.peekLast().contains("+");
			}
			if (!movesSoFar.isEmpty()) {
				capture = movesSoFar.peekLast().contains("x");
			}
			MOVE_LOGGER.trace((check ? "CHECK" : "") + (capture ? "CAPTURE" : "") + " moves: " + movesSoFar + " -> "
					+ movesAtThisLevel.size() + ":" + movesAtThisLevel + System.lineSeparator());
		}
		return totalMoves;
	}
}
