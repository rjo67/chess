package org.rjo.chess2;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public class GameTest {

	@Test
	public void simple() {
		Game g = new Game();
		g.makeMove(new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4));
		g.makeMove(new Move(PieceType.PAWN, Colour.BLACK, Square.e7, Square.e5));
		g.makeMove(new Move(PieceType.KNIGHT, Colour.WHITE, Square.b1, Square.c3));
		g.makeMove(new Move(PieceType.QUEEN, Colour.BLACK, Square.d8, Square.f6));
		g.displayGame();
	}

}
