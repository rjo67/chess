package org.rjo.chess.base.eval;

import java.util.ArrayDeque;
import java.util.Deque;

import org.rjo.chess.base.Move;

public class Line {
	private Deque<Move> moves;

	public Line() {
		this.moves = new ArrayDeque<>();
	}

	public Line(Move m, int startDepth) {
		this();
		addMove(m, startDepth);
	}

	// copy constructor
	public Line(Line line) {
		this();
		moves.addAll(line.moves);
	}

	public void addMove(Move m,
			int startDepth) {
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