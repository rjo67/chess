package org.rjo.chess.eval;

import org.rjo.chess.Position;

public interface SearchStrategy {

	MoveInfo findMove(Position posn);

	int getCurrentDepth();

	void incrementDepth(int increment);

}