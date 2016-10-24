package org.rjo.chess.eval;

import org.rjo.chess.Position;

public interface SearchStrategy {

	MoveInfo findMove(Position posn);

}
