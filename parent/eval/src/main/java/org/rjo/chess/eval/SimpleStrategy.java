package org.rjo.chess.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.rjo.chess.base.Move;
import org.rjo.chess.base.eval.MoveInfo;
import org.rjo.chess.position.Position;

public class SimpleStrategy implements SearchStrategy {

	private static Random rand = new Random();

	@Override
	public int getCurrentDepth() {
		return 0;
	}

	@Override
	public void incrementDepth(int increment) {
		// no-op
	}

	@Override
	public MoveInfo findMove(Position posn) {
		MoveInfo moveInfo = new MoveInfo();
		List<Move> computerMoves = posn.findMoves(posn.getSideToMove());
		if (computerMoves.size() == 0) {
			return null;
		} else {
			List<MoveEval> evalList = new ArrayList<>();
			for (Move move : computerMoves) {
				evalList.add(new MoveEval(posn.evaluate(move), move));
			}
			int PLAY_BEST_MOVE = 95; // play 'best' move x% of the time
			evalList.sort((arg0, arg1) -> {
				return arg1.getValue() - arg0.getValue(); // to sort in 'best-first' order
			});
			System.out.println(evalList);
			for (MoveEval moveEval : evalList) {
				if ((rand.nextInt(100) + 1) <= PLAY_BEST_MOVE) {
					moveInfo.setMove(moveEval.getMove());
					break;
				}
			}
			// if still haven't chosen, take the first one ;-)
			if (moveInfo.getMove() == null) {
				moveInfo.setMove(evalList.get(0).getMove());
			}
		}
		return moveInfo;
	}

}