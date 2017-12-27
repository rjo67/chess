package org.rjo.chess.eval;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.Move;
import org.rjo.chess.Position;

public class AlphaBeta3 implements SearchStrategy {
	private static final Logger LOG = LogManager.getLogger(AlphaBeta3.class);

	private static final int MIN_VAL = -99999;
	private static final int MAX_VAL = -MIN_VAL;

	private int startDepth = 4;

	private PrintStream outputStream;

	private static int NBR_POSNS_EVALUATED;

	public AlphaBeta3(PrintStream out) {
		this.outputStream = out;
	}

	@Override
	public MoveInfo findMove(Position posn) {
		NBR_POSNS_EVALUATED = 0;
		SearchResult result = alphabeta(posn, startDepth, MIN_VAL, MAX_VAL, new Line(), true);
		LOG.info("got result: {}, posns evaluated: {}", result, NBR_POSNS_EVALUATED);
		MoveInfo moveInfo = new MoveInfo();
		moveInfo.setMove(result.getLine().get().getMoves().pop());
		return moveInfo;
	}

	@Override
	public int getCurrentDepth() {
		return startDepth;
	}

	@Override
	public void incrementDepth(int increment) {
		startDepth += increment;
	}

	/**
	 * the minimax value of n, searched to depth d. If the value is less than min, returns min. If greater than max, returns
	 * max.
	 *
	 * @param posn current game position
	 * @param depth current depth
	 * @param min current min ("alpha")
	 * @param max current max ("beta")
	 * @param line curren tline
	 * @param maximizingPlayer whether max or min
	 * @return best result
	 */
	private SearchResult alphabeta(Position posn,
			int depth,
			int min,
			int max,
			Line line,
			boolean maximizingPlayer) {
		if (depth == 0) {
			NBR_POSNS_EVALUATED++;
			int score = posn.evaluate();
			LOG.debug("depth 0: evaluating posn currentLine: {}, score {}", line, score);
			return new SearchResult(score, line);
		}
		if (maximizingPlayer) {
			Line currentBestLine = null;
			List<Move> moves = posn.findMoves(posn.getSideToMove());
			LOG.debug("max(): depth {}, currentLine: {}, min {}, max {}, moves: {}", depth, line, min, max, moves);
			for (Move move : moves) {
				Position newPosn = posn.move(move);
				line.addMove(move);
				LOG.debug("max(): depth {}, checking move {}, currentLine: {}, min {}, max {}", depth, move, line, min, max);
				SearchResult result = alphabeta(newPosn, depth - 1, min, max, line, false);
				if (result.getScore() > min) {
					min = result.getScore();
					if (result.getLine().isPresent()) {
						currentBestLine = new Line(result.getLine().get());
					}
					LOG.debug("max(): depth {}, saved new best line: {}, min {}, max {}", depth, currentBestLine, min, max);
				}
				line.removeLastMove();
				if (max <= min) {
					LOG.debug("max(): beta cut-off");
					break; /* beta cut-off */
				}
			}
			if (moves.isEmpty()) {
				if (posn.isInCheck()) {
					LOG.debug("max(): found mate at depth {}, currentLine: {}", depth, line);
					// favour a mate in 5 rather than mate in 3
					return new SearchResult(max - (10 - depth), line);// need to remain below MAX_INT (??)
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(min, currentBestLine);
		} else {
			Line currentBestLine = null;
			List<Move> moves = posn.findMoves(posn.getSideToMove());
			LOG.debug("min(): depth {}, currentLine: {}, min {}, max {}, moves: {}", depth, line, min, max, moves);
			for (Move move : moves) {
				Position newPosn = posn.move(move);
				line.addMove(move);
				LOG.debug("min(): depth {}, checking move {}, currentLine: {}, min {}, max {}", depth, move, line, min, max);
				SearchResult result = alphabeta(newPosn, depth - 1, min, max, line, true);
				if (result.getScore() < max) {
					max = result.getScore();
					if (result.getLine().isPresent()) {
						currentBestLine = new Line(result.getLine().get());
					}
					LOG.debug("min(): depth {}, saved new best line: {},min {}, max {}", depth, currentBestLine, min, max);
				}
				line.removeLastMove();
				if (max <= min) {
					LOG.debug("min(): alpha cut-off");
					break; /* alpha cut-off */
				}
			}
			if (moves.isEmpty()) /* no possible moves -- checkmate or statemate */ {
				// test for checkmate or stalemate
				if (posn.isInCheck()) {
					LOG.debug("min(): found mate at depth {}, currentLine: {}", depth, line);
					// favour a mate in 5 rather than mate in 3
					return new SearchResult(max - (10 - depth), line); // need to remain below MAX_INT
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(max, currentBestLine);
		}
	}

	//	/*
	//	 * if 'max' has found a move with evaluation +5, then a further move which evaluates to +3 can be immediately discarded.
	//	 */
	//	public SearchResult alphaBetaMax(Position posn,
	//			Line line,
	//			int alpha,
	//			int beta,
	//			int depthleft) {
	//		LOG.debug("in alphaBetaMax, line {}, alpha {}, beta {}, depth {}", line, alpha, beta, depthleft);
	//		if (depthleft == 0) {
	//			int eval = posn.evaluate();
	//			NBR_POSNS_EVALUATED++;
	//			LOG.debug("evaluated posn: {}, posn\n{}", eval, posn);
	//			return new SearchResult(eval, line);
	//		}
	//		List<Move> moves = posn.findMoves(posn.getSideToMove());
	//		Line currentBestLine = null;
	//		int currentBestScore = MIN_VAL;
	//		for (Move move : moves) {
	//			LOG.debug("(max) alpha {}, beta {}, depth {}, move {}", alpha, beta, depthleft, move);
	//			Position newPosn = posn.move(move);
	//			line.addMove(move);
	//			SearchResult result = alphaBetaMin(newPosn, line, alpha, beta, depthleft - 1);
	//			if (result.getScore() > currentBestScore) {
	//				currentBestScore = result.getScore();
	//				if (currentBestScore >= beta) {
	//					LOG.debug("depth {}, move {}: beta cutoff {}, {} for posn\n{}", depthleft, move, result, beta, newPosn);
	//					// 'undo' move
	//					line.removeLastMove();
	//					return result;//new SearchResult(beta); // fail hard beta-cutoff
	//				}
	//			}
	//			if (result.getScore() > alpha) {
	//				LOG.debug("depth {}, move {}: new alpha {}, {} for posn\n{}", depthleft, move, result, alpha, newPosn);
	//				alpha = result.getScore(); // alpha acts like max in MiniMax
	//				currentBestLine = new Line(result.getLine().get());
	//				// print best line (if at top-level depth ?)
	//				if (depthleft == 4) {
	//					result.printUCI(outputStream);
	//				}
	//			}
	//			// 'undo' move
	//			line.removeLastMove();
	//		}
	//		if (currentBestScore <= MIN_VAL) {
	//			// test for checkmate or stalemate
	//			System.out.println("max: test for checkmate or stalemate");
	//			if (posn.isInCheck()) {
	//				return new SearchResult(currentBestScore + depthleft, line);
	//			} else {
	//				// statemate: evaluate as 0
	//				return new SearchResult(0, line);
	//			}
	//		}
	//		return new SearchResult(currentBestScore, currentBestLine);
	//	}

	class SearchResult {
		private int score;
		private Optional<Line> line;

		public SearchResult(int score) {
			this(score, null);
		}

		public SearchResult(int score, Line line) {
			this.score = score;
			if (line == null) {
				this.line = Optional.empty();
			} else {
				if (line.getMoves().size() > 5) {
					throw new RuntimeException("line too long: " + line);
				}
				Line clonedLine = new Line(line);
				this.line = Optional.of(clonedLine);
			}
		}

		public int getScore() {
			return score;
		}

		public Optional<Line> getLine() {
			return line;
		}

		@Override
		public String toString() {
			return score + ":" + line;
		}

		// create output for UCI
		public void printUCI(PrintStream outputStream) {
			outputStream.print("info pv ");
			for (Move m : line.get().getMoves()) {
				outputStream.print(m.toUCIString() + " ");
			}
			outputStream.println("score cp " + score);
		}
	}

	class Line {
		private Deque<Move> moves;

		public Line() {
			this.moves = new ArrayDeque<>();
		}

		public Line(Move m) {
			this();
			addMove(m);
		}

		// copy constructor
		public Line(Line line) {
			this();
			moves.addAll(line.moves);
		}

		public void addMove(Move m) {
			moves.add(m);
			if (moves.size() > 5) {
				throw new RuntimeException("moves too long: " + moves);
			}
		}

		public void removeLastMove() {
			moves.removeLast();
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
