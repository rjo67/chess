package org.rjo.chess.eval;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.PositionScore;
import org.rjo.chess.SystemFlags;

public class AlphaBeta implements SearchStrategy {

	private static final int MIN_INT = -99999;
	private static final int MAX_INT = -MIN_INT;
	private static final int INITIAL_MAX_DEPTH = 4;

	private static final Logger LOG = LogManager.getLogger(AlphaBeta.class);

	private int nbrNodesEvaluated;
	private int depth;

	private PrintStream outputStream;
	private Map<Position, Position> zobristMap;

	public AlphaBeta(Map<Position, Position> zobristMap) {
		this(System.out, zobristMap);
	}

	public AlphaBeta(PrintStream out, Map<Position, Position> zobristMap) {
		this.outputStream = out;
		this.zobristMap = zobristMap;
		this.depth = INITIAL_MAX_DEPTH;
	}

	@Override
	public String toString() {
		return "AlphaBeta, depth=" + depth;
	}

	@Override
	public int getCurrentDepth() {
		return depth;
	}

	@Override
	public void incrementDepth(int increment) {
		depth += increment;
	}

	@Override
	public MoveInfo findMove(Position posn) {
		nbrNodesEvaluated = 0;
		// always store at least one move (even if all moves are equally bad i.e. return MIN_INT)
		int max = MIN_INT - 1;
		MoveInfo moveInfo = new MoveInfo();
		long overallStartTime = System.currentTimeMillis();
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		for (Move move : moves) {
			boolean newMoveFound = false;
			long startTime = System.currentTimeMillis();
			Line line = new Line(move);
			Position posnAfterMove = posn.move(move);
			int score;
			LOG.debug("           ".substring(depth) + " " + depth + " " + move);
			//
			// check if seen position already
			//
			Optional<PositionScore> previouslyCalculatedScore = checkZobrist(posnAfterMove);
			if (previouslyCalculatedScore.isPresent()) {
				// use the score for this position
				// TODO if it was at a higher depth?
				// TODO need to check for sideToMove?
				score = previouslyCalculatedScore.get().getScore();
				LOG.debug("using previously processed position\n" + posnAfterMove);
			} else {
				score = -alphaBeta(MIN_INT, MAX_INT, depth - 1, posnAfterMove, line);
				PositionScore positionScore = new PositionScore(score, depth);
				updateZobrist(posnAfterMove, positionScore);
				LOG.debug("toplevel: stored posn with score:" + positionScore + ", line: " + line + ":\n" + posnAfterMove);
			}
			if (score > max) {
				max = score;
				moveInfo.setMove(move);
				moveInfo.setLine(line);
				newMoveFound = true;
				outputStream.print("info pv ");
				for (Move m : moveInfo.getLine().getMoves()) {
					outputStream.print(m.toUCIString() + " ");
				}
				outputStream.println("score cp " + score);
			}
			// show current move even if not best (not uci relevant)
			if (!newMoveFound) {
				LOG.debug(
						String.format("(%7s, %8d, %8d, %5dms)%s", move, score, nbrNodesEvaluated, (System.currentTimeMillis() - startTime), ""));
			}
		}
		long overallStopTime = System.currentTimeMillis();
		outputStream.println(String.format("nodes: %7d, time: %7.2fs, %9.2f nodes/s", nbrNodesEvaluated,
				(overallStopTime - overallStartTime) / 1000.0, (1.0 * nbrNodesEvaluated / (overallStopTime - overallStartTime)) * 1000));
		return moveInfo;
	}

	private int alphaBeta(int alpha,
			int beta,
			int depth,
			Position posn,
			Line currentLine) {
		if (depth == 0) {
			nbrNodesEvaluated++;
			return posn.evaluate();
		}
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		for (Move move : moves) {
			Line line = new Line(move);
			int score;
			try {
				Position posnAfterMove = posn.move(move);
				LOG.debug("           ".substring(depth) + " " + depth + " " + move);
				Optional<PositionScore> previouslyCalculatedScore = checkZobrist(posnAfterMove);
				if (previouslyCalculatedScore.isPresent()) {
					// use the score for this position
					// TODO if it was at a higher depth?
					// TODO need to check for sideToMove?
					score = previouslyCalculatedScore.get().getScore();
					LOG.debug("using previously processed position, line: " + currentLine + "\n" + posnAfterMove);
				} else {
					score = -alphaBeta(-beta, -alpha, depth - 1, posnAfterMove, line);
					LOG.debug("           ".substring(depth) + " " + depth + " " + move + " " + score + " -- " + line);
					PositionScore positionScore = new PositionScore(score, depth);
					updateZobrist(posnAfterMove, positionScore);
					LOG.debug("stored posn with score:" + positionScore + ":\n" + posnAfterMove);
				}
			} catch (IllegalStateException x) {
				// possible if trying a line where the king gets captured
				score = beta; // cutoff line
			}
			if (score >= beta) {
				return beta; // fail hard beta-cutoff
			}
			if (score > alpha) {
				alpha = score;
				currentLine.storeLine(line);
			}
		}
		if (moves.isEmpty()) {
			// a mate in two is better than a mate in 3
			return alpha + ((20 - depth) * 3);
		}
		return alpha;
	}

	private Optional<PositionScore> checkZobrist(Position posnAfterMove) {
		if (SystemFlags.USE_ZOBRIST) {
			Position previouslyProcessedPosition = zobristMap.get(posnAfterMove);
			if (previouslyProcessedPosition != null) {
				// use the score for this position
				// TODO if it was at a higher depth?
				// TODO need to check for sideToMove?
				PositionScore score = previouslyProcessedPosition.getPositionScore();
				return Optional.of(score);
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}

	private void updateZobrist(Position posnAfterMove,
			PositionScore positionScore) {
		if (SystemFlags.USE_ZOBRIST) {
			posnAfterMove.setPositionScore(positionScore);
			zobristMap.put(posnAfterMove, posnAfterMove);
		}
	}

	static class Line {
		private Deque<Move> moves;

		public Line() {
			this.moves = new ArrayDeque<>();
		}

		public Line(Move m) {
			this();
			addMove(m);
		}

		public void storeLine(Line line) {
			this.moves = line.moves;
		}

		public void addMove(Move m) {
			moves.addFirst(m);
		}

		public void clearMoves() {
			moves = new ArrayDeque<>();
		}

		public Deque<Move> getMoves() {
			return moves;
		}

		@Override
		public String toString() {
			return moves.toString();
		}
	}

}
