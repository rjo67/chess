package org.rjo.chess2;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.Move;

/**
 * The game object stores the moves and positions.
 *
 * @author rich
 * @since 2016-09-04
 */
public class Game {

	private List<MovePosition> gameProgress;
	private int moveNbr; // half-moves -- also index into the gameProgress list

	public Game() {
		gameProgress = new ArrayList<>();
		gameProgress.add(new MovePosition(null, Position.startPosition()));
		moveNbr = 0;
	}

	public void makeMove(Move move) {
		gameProgress.add(new MovePosition(move, gameProgress.get(moveNbr).getPosition().calculateNewPosition(move)));
		moveNbr++;
	}

	/**
	 * undo the current move
	 */
	public void undoMove() {
		if (moveNbr > 1) {
			gameProgress.remove(moveNbr);
			moveNbr--;
		}
	}

	public void displayGame() {
		for (int move = 0; move <= moveNbr; move++) {
			System.out.println("halfmove: " + move);
			System.out.println(gameProgress.get(move));
			System.out.println("\n");
		}
	}
}
