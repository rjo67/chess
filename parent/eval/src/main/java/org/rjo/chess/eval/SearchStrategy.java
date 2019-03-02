package org.rjo.chess.eval;

import org.rjo.chess.base.eval.MoveInfo;
import org.rjo.chess.position.Position;

public interface SearchStrategy {

	MoveInfo findMove(Position posn);

	int getCurrentDepth();

	void incrementDepth(int increment);

	/**
	 * @return current number of nodes that have been searched
	 */
	default int getCurrentNbrNodesSearched() {
		return 0;
	}

}