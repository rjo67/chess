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

public class AlphaBeta2 implements SearchStrategy {
	private static final Logger LOG = LogManager.getLogger(AlphaBeta2.class);

	private static final int MIN_VAL = -99999;
	private static final int MAX_VAL = -MIN_VAL;

	private int depth = 4;

	private PrintStream outputStream;

	private static int NBR_POSNS_EVALUATED;

	public AlphaBeta2(PrintStream out) {
		this.outputStream = out;
	}

	@Override
	public MoveInfo findMove(Position posn) {
		NBR_POSNS_EVALUATED = 0;
		SearchResult result = alphaBetaMax(posn, new Line(), MIN_VAL, MAX_VAL, depth);
		LOG.info("got result: {}, posns evaluated: {}", result, NBR_POSNS_EVALUATED);
		MoveInfo moveInfo = new MoveInfo();
		moveInfo.setMove(result.getLine().get().getMoves().pop());
		return moveInfo;
	}

	@Override
	public int getCurrentDepth() {
		return depth;
	}

	@Override
	public void incrementDepth(int increment) {
		depth += increment;
	}

	/*
	 * if 'max' has found a move with evaluation +5, then a further move which evaluates to +3 can be immediately discarded.
	 */
	public SearchResult alphaBetaMax(Position posn,
			Line line,
			int alpha,
			int beta,
			int depthleft) {
		LOG.debug("in alphaBetaMax, line {}, alpha {}, beta {}, depth {}", line, alpha, beta, depthleft);
		if (depthleft == 0) {
			int eval = posn.evaluate();
			NBR_POSNS_EVALUATED++;
			LOG.debug("evaluated posn: {}, posn\n{}", eval, posn);
			return new SearchResult(eval, line);
		}
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		Line currentBestLine = null;
		int currentBestScore = MIN_VAL;
		for (Move move : moves) {
			LOG.debug("(max) alpha {}, beta {}, depth {}, move {}", alpha, beta, depthleft, move);
			Position newPosn = posn.move(move);
			line.addMove(move);
			SearchResult result = alphaBetaMin(newPosn, line, alpha, beta, depthleft - 1);
			if (result.getScore() > currentBestScore) {
				currentBestScore = result.getScore();
				if (currentBestScore >= beta) {
					LOG.debug("depth {}, move {}: beta cutoff {}, {} for posn\n{}", depthleft, move, result, beta, newPosn);
					// 'undo' move
					line.removeLastMove();
					return result;//new SearchResult(beta); // fail hard beta-cutoff
				}
			}
			if (result.getScore() > alpha) {
				LOG.debug("depth {}, move {}: new alpha {}, {} for posn\n{}", depthleft, move, result, alpha, newPosn);
				alpha = result.getScore(); // alpha acts like max in MiniMax
				currentBestLine = new Line(result.getLine().get());
				// print best line (if at top-level depth ?)
				if (depthleft == 4) {
					result.printUCI(outputStream);
				}
			}
			// 'undo' move
			line.removeLastMove();
		}
		if (currentBestScore <= MIN_VAL) {
			// test for checkmate or stalemate
			System.out.println("max: test for checkmate or stalemate");
			if (posn.isInCheck()) {
				return new SearchResult(currentBestScore + depthleft, line);
			} else {
				// statemate: evaluate as 0
				return new SearchResult(0, line);
			}
		}
		return new SearchResult(currentBestScore, currentBestLine);
	}

	public SearchResult alphaBetaMin(Position posn,
			Line line,
			int alpha,
			int beta,
			int depthleft) {
		LOG.debug("(min) line {}, alpha {}, beta {}, depth {}", line, alpha, beta, depthleft);
		if (depthleft == 0) {
			int eval = -posn.evaluate();
			NBR_POSNS_EVALUATED++;
			LOG.debug("(min) evaluated posn: {}, posn\n{}", eval, posn);
			return new SearchResult(eval, line);
		}
		Line currentBestLine = null;
		int currentBestScore = MAX_VAL;
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		for (Move move : moves) {
			LOG.debug("(min) alpha {}, beta {}, depth {}, move {}", alpha, beta, depthleft, move);
			Position newPosn = posn.move(move);
			line.addMove(move);
			SearchResult result = alphaBetaMax(newPosn, line, alpha, beta, depthleft - 1);
			if (result.getScore() < currentBestScore) {
				currentBestScore = result.getScore();
				if (result.getScore() <= alpha) {
					LOG.debug("depth {}, move {}: alpha cutoff {}, {} for posn\n{}", depthleft, move, result, alpha, newPosn);
					// 'undo' move
					line.removeLastMove();
					return result;//new SearchResult(alpha); // fail hard alpha-cutoff
				}
			}
			if (result.getScore() < beta) {
				LOG.debug("depth {}, move {}: new beta {}, {} for posn\n{}", depthleft, move, result, beta, newPosn);
				beta = result.getScore(); // beta acts like min in MiniMax
				currentBestLine = new Line(result.getLine().get());
			}
			// 'undo' move
			line.removeLastMove();
		}
		if (currentBestScore >= MAX_VAL) {
			// test for checkmate or stalemate
			System.out.println("min: test for checkmate or stalemate");
			if (posn.isInCheck()) {
				return new SearchResult(currentBestScore + depthleft, line);
			} else {
				// statemate: evaluate as 0
				return new SearchResult(0, line);
			}
		}
		return new SearchResult(currentBestScore, currentBestLine);
	}

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
