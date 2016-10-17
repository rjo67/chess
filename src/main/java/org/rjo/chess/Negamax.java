package org.rjo.chess;

import java.util.List;

public class Negamax implements SearchStrategy {
	private static final int MIN_INT = Integer.MIN_VALUE + 5;

	private int nbrNodesEvaluated;

	@Override
	public MoveInfo findMove(Position posn) {
		final int depth = 4;
		nbrNodesEvaluated = 0;
		int max = MIN_INT;
		MoveInfo moveInfo = new MoveInfo();
		long overallStartTime = System.currentTimeMillis();
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		for (Move move : moves) {
			long startTime = System.currentTimeMillis();
			Position posnAfterMove = posn.move(move);
			int score = -negaMax(depth - 1, posnAfterMove);
			// System.out.println(Fen.encode(game) + ", score=" + score +
			// ",depth=" + depth + ",max=" + max);
			if (score > max) {
				max = score;
				moveInfo.setMove(move);
				System.out.println("******   (" + move + ": " + score + ")");
			}
			System.out.println(String.format("(%7s,%5d,%7d,%5dms)", move, score, nbrNodesEvaluated,
					(System.currentTimeMillis() - startTime)));
		}
		long overallStopTime = System.currentTimeMillis();
		System.out.println(String.format("time: %7.2fs, %9.2f nodes/s", (overallStopTime - overallStartTime) / 1000.0,
				(1.0 * nbrNodesEvaluated / (overallStopTime - overallStartTime)) * 1000));
		return moveInfo;
	}

	private int negaMax(int depth, Position posn) {
		if (depth == 0) {
			nbrNodesEvaluated++;
			return posn.evaluate();
		}
		int max = MIN_INT;
		List<Move> moves = posn.findMoves(posn.getSideToMove());
		for (Move move : moves) {
			Position posnAfterMove = posn.move(move);
			int score = -negaMax(depth - 1, posnAfterMove);
			// System.out
			// .println(game.getSideToMove() + ": " + move + ", score=" + score
			// + ",depth=" + depth + ",max=" + max);
			if (score > max) {
				max = score;
			}
		}
		return max;
	}
}
