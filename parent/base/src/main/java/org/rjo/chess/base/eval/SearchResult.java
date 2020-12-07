package org.rjo.chess.base.eval;

import java.io.PrintStream;
import java.util.Optional;

import org.rjo.chess.base.Move;

public class SearchResult {

	private static int count = 0;

	private int score;
	private Optional<Line> line;
	// if set, have found a mate in x ply
	private int mateIn;

	public SearchResult(int score, int startDepth) {
		this(score, startDepth, null);
	}

	public SearchResult(int score, int startDepth, Line line) {
		this(score, line, -1, startDepth);
	}

	public SearchResult(int score, Line line, int mateIn, int startDepth) {
		count++;
		this.score = score;
		this.mateIn = mateIn;
		if (this.mateIn != -1) {
			System.out.println(count + ", mateIn " + mateIn + ", " + line);
		}
		if (line == null) {
			this.line = Optional.empty();
		} else {
			if (line.getMoves().size() > startDepth + 1) {
				throw new RuntimeException("line too long (startDepth: " + startDepth + "): " + line);
			}
			Line clonedLine = new Line(line);
			this.line = Optional.of(clonedLine);
		}
	}

	public int getScore() {
		return score;
	}

	public int getMateIn() {
		return mateIn;
	}

	public Optional<Line> getLine() {
		return line;
	}

	public void setLine(Optional<Line> line) {
		this.line = line;
	}

	@Override
	public String toString() {
		return score + ":" + line;
	}

	// create output for UCI
	public void printUCI(PrintStream outputStream) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(count).append(" ");
		sb.append("info pv ");
		for (Move m : line.get().getMoves()) {
			sb.append(m.toUCIString()).append(" ");
		}

		sb.append("score ");
		if (mateIn != -1) {
			sb.append("mate ").append((mateIn + 1) / 2); // mate in x moves, not plies
		} else {
			sb.append("cp ").append(score);
		}

		outputStream.println(sb.toString());
	}
}
