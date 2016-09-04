package org.rjo.chess2;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Square;
import org.rjo.chess.pieces.PieceType;

public class PositionTest {

	@Test
	public void checkImmutable() {
		Position p = Position.startPosition();
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4);
		Position p2 = p.calculateNewPosition(move);

		// System.out.println(p);
		// System.out.println(p2);
		assertFalse("positions are the same -- objects not immutable!?", p.toString().equals(p2.toString()));
	}
}
