package org.rjo.chess.eval;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Position;

public class AlphaBeta3 implements SearchStrategy {
	private static final Logger LOG = LogManager.getLogger(AlphaBeta3.class);

	private static final int MIN_VAL = -99999;
	private static final int MAX_VAL = -MIN_VAL;

	/** whether to order the moves or not -- mainly for tests */
	public static boolean ORDER_MOVES = true;

	private int startDepth = 4;

	private PrintStream outputStream;

	private static int NBR_POSNS_EVALUATED;

	public AlphaBeta3(PrintStream out) {
		this.outputStream = out;
	}

	@Override
	public MoveInfo findMove(Position posn) {
		NBR_POSNS_EVALUATED = 0;
		MoveTree moveTree = new MoveTree(null, null, startDepth, 0, 0);
		// if white currently to move, want to maximize. Otherwise minimize.
		MiniMax type = (posn.getSideToMove() == Colour.WHITE) ? MiniMax.MAX : MiniMax.MIN;
		SearchResult result = alphabeta(posn, startDepth, MIN_VAL, MAX_VAL, new Line(), moveTree, type);
		LOG.debug(moveTree.toString());
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
	 * @param moveTree
	 * @param evaluationType whether max or min
	 * @return best result
	 */
	private SearchResult alphabeta(Position posn,
			int depth,
			int min,
			int max,
			Line line,
			MoveTree moveTree,
			MiniMax evaluationType) {
		if (depth == 0) {
			NBR_POSNS_EVALUATED++;
			int score = posn.evaluate();
			LOG.debug("depth 0: evaluating posn currentLine: {}, score {}", line, score);
			return new SearchResult(score, line);
		}

		Line currentBestLine = null;
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		LOG.debug("{}: depth {}, currentLine: {}, min {}, max {}, moves: {}", evaluationType, depth, line, min, max, moves);
		if (ORDER_MOVES) {
			moves = orderMoves(posn, moves);
			LOG.debug("{}: sorted moves: {}", evaluationType, moves);
		}
		switch (evaluationType) {

		case MAX:
			for (Move move : moves) {
				MoveTree moveEntry = new MoveTree(MiniMax.MAX, move, depth, min, max);
				moveTree.addEntry(moveEntry);
				Position newPosn = posn.move(move);
				line.addMove(move);
				LOG.debug("max(): depth {}, checking move {}, currentLine: {}, min {}, max {}", depth, move, line, min, max);
				SearchResult result = alphabeta(newPosn, depth - 1, min, max, line, moveEntry, MiniMax.MIN);
				moveEntry.setScore(result.getScore());
				if (result.getScore() > min) {
					min = result.getScore();
					moveEntry.addEvaluation(EvalType.BESTSOFAR);
					if (result.getLine().isPresent()) {
						currentBestLine = new Line(result.getLine().get());
						if (depth == startDepth) {
							result.printUCI(outputStream);
						}
					}
					LOG.debug("max(): depth {}, saved new best line: {}, min {}, max {}", depth, currentBestLine, min, max);
				}
				line.removeLastMove();
				if (max <= min) {
					LOG.debug("max(): beta cut-off");
					moveEntry.addEvaluation(EvalType.BETA_CUTOFF);
					break; /* beta cut-off */
				}
			}
			if (moves.isEmpty()) {
				if (posn.isInCheck()) {
					LOG.debug("max(): found mate at depth {}, currentLine: {}", depth, line);
					// favour a mate in 5 rather than mate in 3
					return new SearchResult(MIN_VAL + (10 - depth), line);// need to remain above MIN_VAL (??)
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(min, currentBestLine);

		case MIN:
			for (Move move : moves) {
				MoveTree moveEntry = new MoveTree(MiniMax.MIN, move, depth, min, max);
				moveTree.addEntry(moveEntry);
				Position newPosn = posn.move(move);
				line.addMove(move);
				LOG.debug("min(): depth {}, checking move {}, currentLine: {}, min {}, max {}", depth, move, line, min, max);
				SearchResult result = alphabeta(newPosn, depth - 1, min, max, line, moveTree, MiniMax.MAX);
				moveEntry.setScore(result.getScore());
				if (result.getScore() < max) {
					max = result.getScore();
					moveEntry.addEvaluation(EvalType.BESTSOFAR);
					if (result.getLine().isPresent()) {
						currentBestLine = new Line(result.getLine().get());
						if (depth == startDepth) {
							result.printUCI(outputStream);
						}
					}
					LOG.debug("min(): depth {}, saved new best line: {},min {}, max {}", depth, currentBestLine, min, max);
				}
				line.removeLastMove();
				if (max <= min) {
					LOG.debug("min(): alpha cut-off");
					moveEntry.addEvaluation(EvalType.ALPHA_CUTOFF);

					break; /* alpha cut-off */
				}
			}
			if (moves.isEmpty()) /* no possible moves -- checkmate or statemate */ {
				// test for checkmate or stalemate
				if (posn.isInCheck()) {
					LOG.debug("min(): found mate at depth {}, currentLine: {}", depth, line);
					// favour a mate in 5 rather than mate in 3
					return new SearchResult(MAX_VAL - (10 - depth), line); // need to remain below MAX_VAL
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(max, currentBestLine);
		default:
			throw new RuntimeException("unexpected value for MIN/MAX ?");
		}
	}

	/**
	 * Sorts the given move lists according to various heuristics.
	 *
	 * @param posn the current position
	 * @param moves all available moves
	 * @return a sorted list of available moves (hopefully, better moves first)
	 */
	private List<Move> orderMoves(Position posn,
			List<Move> moves) {

		List<Move> captures = new ArrayList<>(moves.size());
		List<Move> nonCaptures = new ArrayList<>(moves.size());
		for (Move move : moves) {
			if (move.isCapture()) {
				captures.add(move);
			} else {
				nonCaptures.add(move);
			}
		}
		captures = orderCaptures(posn, captures);
		nonCaptures = orderNonCaptures(posn, nonCaptures);

		// ... and return
		captures.addAll(nonCaptures);
		return captures;
	}

	// for captures: Most Valuable Victim - Least Valuable Aggressor or Static Exchange Evaluation (SEE)
	// http://chessprogramming.wikispaces.com/MVV-LVA
	private List<Move> orderCaptures(Position posn,
			List<Move> moves) {
		return moves;
	}

	// for non-captures: history heuristic
	private List<Move> orderNonCaptures(Position posn,
			List<Move> moves) {
		return moves;
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

	enum MiniMax {
		MAX, MIN;
	}

	enum EvalType {
		BESTSOFAR, ALPHA_CUTOFF, BETA_CUTOFF, NORMAL;

		public static String print(Set<EvalType> evaluations) {
			StringBuilder sb = new StringBuilder();
			for (EvalType eval : evaluations) {
				if (eval != NORMAL) {
					sb.append(eval).append(" ");
				}
			}
			return sb.toString();
		}
	}

	class MoveTree {
		private MiniMax type;
		private Set<EvalType> evaluations;
		private Move move;
		private int score;
		private int depth;
		private int min; // min value at time of evaluation
		private int max;// max value at time of evaluation
		private List<MoveTree> followingMoves;

		public MoveTree(MiniMax type, Move move, int depth, int min, int max) {
			this.type = type;
			this.move = move;
			this.depth = depth;
			this.min = min;
			this.max = max;
			this.evaluations = new HashSet<>();
			this.evaluations.add(EvalType.NORMAL);
			this.followingMoves = new ArrayList<>();
		}

		public void setScore(int score) {
			this.score = score;
		}

		public void addEvaluation(EvalType evaluation) {
			this.evaluations.add(evaluation);
		}

		public void addEntry(MoveTree moveTree) {
			this.followingMoves.add(moveTree);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(500);
			// ignore dummy first entry
			if (type == null) {
				sb.append("\n");
			} else {
				sb.append(
						String.format("%s %s %d %s (min:%d max:%d) %d %s\n", type, "        ".substring(0, 9 - depth), depth, move, min, max,
								score, EvalType.print(evaluations)));
			}
			for (MoveTree entry : followingMoves) {
				sb.append(entry.toString());
			}
			return sb.toString();
		}
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
				if (line.getMoves().size() > startDepth + 1) {
					throw new RuntimeException("line too long (startDepth: " + startDepth + "): " + line);
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
			if (moves.size() > startDepth + 1) {
				throw new RuntimeException("moves too long (startDepth: " + startDepth + "): " + moves);
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
