package org.rjo.chess.eval;

import org.rjo.chess.Position;

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