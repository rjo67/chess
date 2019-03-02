package org.rjo.chess;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.position.MovePosition;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.ZobristMap;

/**
 * Encapsulates the board, the moves, castling rights, etc (clocks?).
 *
 * @author rich
 */
public class Game {

	/** stores the moves and resulting positions (ply) */
	private List<MovePosition> gameProgress;

	/** half-moves */
	private int halfmoveClock;

	/**
	 * move number of the next move. Not calculated from size of <code>gameProgress</code> since we don't have to start at
	 * move 1.
	 */
	private int moveNbr;

	// this points to the last position stored in 'gameProgress'.
	private int currentMoveOffset;

	private ZobristMap zobristMap;

	/**
	 * Constructs a game with the default start position.
	 */
	public Game() {
		this(Position.startPosition());
	}

	/**
	 * Inits a game with the given position.
	 *
	 * @param position the position
	 */
	public Game(Position position) {
		gameProgress = new ArrayList<>();
		gameProgress.add(new MovePosition(null, position));
		moveNbr = 1;
		currentMoveOffset = 0;
		zobristMap = new ZobristMap();
	}

	public void makeMove(Move move) {
		Position posnAfterMove = gameProgress.get(currentMoveOffset).getPosition().move(move);
		gameProgress.add(new MovePosition(move, posnAfterMove));
		currentMoveOffset++;
		halfmoveClock++;
		if (Colour.BLACK == move.getColour()) {
			moveNbr++;
		}
	}

	public ZobristMap getZobristMap() {
		return zobristMap;
	}

	public Position getPosition() {
		return gameProgress.get(currentMoveOffset).getPosition();
	}

	public int getHalfmoveClock() {
		return halfmoveClock;
	}

	public void setHalfmoveClock(int halfmoveClock) {
		this.halfmoveClock = halfmoveClock;
	}

	public int getMoveNumber() {
		return moveNbr;
	}

	/**
	 * Sets the move number.
	 */
	public void setMoveNumber(int moveNbr) {
		this.moveNbr = moveNbr;
	}

	/**
	 * prints out all the moves so far.
	 */
	public void displayGame() {
		for (int move = 0; move <= moveNbr; move++) {
			System.out.println("halfmove: " + move);
			System.out.println(gameProgress.get(move));
			System.out.println("\n");
		}
	}

}