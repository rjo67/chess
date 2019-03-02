package org.rjo.chess.position;

import org.rjo.chess.base.Move;

/**
 * A composite class containing a move object with the corresponding position after the move.
 *
 * @author rich
 * @since 2016-09-04
 */
public class MovePosition {

	private Move move; // can be null for the start position
	private Position position;

	public MovePosition(Move move, Position position) {
		this.move = move;
		this.position = position;
	}

	public Move getMove() {
		return move;
	}

	public Position getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return "move: " + move + "\n" + position;
	}
}
