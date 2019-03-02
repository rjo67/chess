package org.rjo.chess.position;

public class PositionScore {

	private int score;
	private int depth;

	public PositionScore(int score, int depth) {
		this.score = score;
		this.depth = depth;
	}

	public int getScore() {
		return score;
	}

	public int getDepth() {
		return depth;
	}

	@Override
	public String toString() {
		return "[score=" + score + ", depth=" + depth + "]";
	}
}
