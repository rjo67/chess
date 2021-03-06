package org.rjo.chess.base.eval;

import org.rjo.chess.base.Move;

public class MoveInfo {
	private Move move;
	private Line line;
	private boolean checkmate;
	private boolean stalemate;

	public void setCheckmate(boolean b) {
		this.checkmate = b;
	}

	public boolean isCheckmate() {
		return checkmate;
	}

	public boolean isStalemate() {
		return stalemate;
	}

	public void setStalemate(boolean b) {
		this.stalemate = b;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public Line getLine() {
		return line;
	}

	public void setLine(Line line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return move + "(" + line + ")";
	}

}