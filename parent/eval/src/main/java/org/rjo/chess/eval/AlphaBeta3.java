package org.rjo.chess.eval;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.eval.Line;
import org.rjo.chess.base.eval.MoveInfo;
import org.rjo.chess.base.eval.SearchResult;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.ZobristMap;
import org.rjo.chess.position.ZobristMap.ZobristInfo;

public class AlphaBeta3 implements SearchStrategy {
	private static final Logger LOG = LogManager.getLogger(AlphaBeta3.class);

	private static final int MIN_VAL = -99999;
	private static final int MAX_VAL = -MIN_VAL;

	private static boolean USE_ZOBRIST = true;

	private static int count = 0;

	/** whether to order the moves or not -- mainly for tests */
	public static boolean ORDER_MOVES = true;

	private int startDepth = 4;

	private PrintStream outputStream;

	private ZobristMap zobristMap;

	// how many times moves were made, i.e. new positions created
	private static int NBR_NODES_SEARCHED;
	// how many times 'evaluate' was called
	private static int NBR_POSNS_EVALUATED;

	/** constructor for tests that don't want the zobrist map */
	public AlphaBeta3(PrintStream out) {
		this(out, new ZobristMap());
	}

	public AlphaBeta3(PrintStream out, ZobristMap zobristMap) {
		this.outputStream = out;
		this.zobristMap = zobristMap;
	}

	@Override
	public MoveInfo findMove(Position posn) {
		NBR_NODES_SEARCHED = 0;
		NBR_POSNS_EVALUATED = 0;
		MoveTree moveTree = new MoveTree(null, null, startDepth, 0, 0);
		// if white currently to move, want to maximize. Otherwise minimize.
		MiniMax type = (posn.getSideToMove() == Colour.WHITE) ? MiniMax.MAX : MiniMax.MIN;

		long start = System.currentTimeMillis();
		SearchResult result = alphabeta(posn, startDepth, MIN_VAL, MAX_VAL, new Line(), moveTree, type);
		long duration = System.currentTimeMillis() - start;
		LOG.debug(moveTree.toString());
		LOG.info("evaluated {} nodes, {} posns, time: {}, result: {}", NBR_NODES_SEARCHED, NBR_POSNS_EVALUATED, timeTaken(duration), result);
		MoveInfo moveInfo = new MoveInfo();
		moveInfo.setMove(result.getLine().get().getMoves().pop());
		return moveInfo;
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
			return new SearchResult(score, startDepth, line);
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
				line.addMove(move, startDepth);
				NBR_NODES_SEARCHED++;
				LOG.debug("max(): depth {}, checking move {}, currentLine: {}, min {}, max {}", depth, move, line, min, max);
				Optional<ZobristInfo> previouslyProcessedPosition = zobristMap.checkZobrist(newPosn);
				SearchResult result = null;
				boolean foundZobrist = false;
				if (USE_ZOBRIST && previouslyProcessedPosition.isPresent() && previouslyProcessedPosition.get().getDepth() >= depth) {
					// TODO need to check for sideToMove?
					LOG.debug("max(): depth {}, zobrist hit {}", newPosn.getZobristHash());
					result = previouslyProcessedPosition.get().getSearchResult();
					// use the current line, not the line stored in the zobrist map
					result.setLine(Optional.of(line));
					foundZobrist = true;
				} else {
					result = alphabeta(newPosn, depth - 1, min, max, line, moveEntry, MiniMax.MIN);
				}
				moveEntry.setScore(result.getScore());
				if (USE_ZOBRIST && !foundZobrist) {
					zobristMap.updateZobristMap(newPosn, depth, result);
				}
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
					return new SearchResult(MIN_VAL + (10 - depth), line, line.getMoves().size(), startDepth);// need to remain above MIN_VAL (??)
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, startDepth, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(min, startDepth, currentBestLine);

		case MIN:
			for (Move move : moves) {
				MoveTree moveEntry = new MoveTree(MiniMax.MIN, move, depth, min, max);
				moveTree.addEntry(moveEntry);
				Position newPosn = posn.move(move);
				line.addMove(move, startDepth);
				NBR_NODES_SEARCHED++;
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
					// return a higher score for a mate in 3 compared to a mate in 5
					return new SearchResult(MAX_VAL - (10 - depth), line, line.getMoves().size(), startDepth); // need to remain below MAX_VAL
				} else {
					// statemate: evaluate as 0
					return new SearchResult(0, startDepth, line);
				}
			}
			// is possible to get here without having set 'currentBestLine'
			// e.g. have tried all possibilities but they were all outside of the [min,max] range
			return new SearchResult(max, startDepth, currentBestLine);
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

	private String timeTaken(long durationInMs) {
		return String.format("%02d.%02d", durationInMs / 1000, durationInMs % 1000);
	}

	@Override
	public int getCurrentNbrNodesSearched() {
		return NBR_NODES_SEARCHED;
	}

	@Override
	public int getCurrentDepth() {
		return startDepth;
	}

	@Override
	public void incrementDepth(int increment) {
		startDepth += increment;
	}

	enum MiniMax {
		MAX, MIN
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

	static class MoveTree {
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
						String.format("%s %s %d %s (min:%d max:%d) %d %s%n", type, "        ".substring(0, 9 - depth), depth, move, min, max,
								score, EvalType.print(evaluations)));
			}
			for (MoveTree entry : followingMoves) {
				sb.append(entry.toString());
			}
			return sb.toString();
		}
	}

}
