package org.rjo.chess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

public class ZobristTest {

	@Test
	public void multipleCallsSameHash() {
		Zobrist zob = new Zobrist(10);
		Position posn = Position.startPosition();
		assertEquals(zob.hash(posn), zob.hash(posn));
	}

	@Test
	public void sameSeedGeneratesSameHash() {
		Zobrist zob1 = new Zobrist(10);
		Zobrist zob2 = new Zobrist(10);

		Position posn = Position.startPosition();
		assertEquals(zob1.hash(posn), zob2.hash(posn));
	}

	@Test
	public void differentSeedGeneratesDifferentHash() {
		Zobrist zob1 = new Zobrist(3);
		Zobrist zob2 = new Zobrist(4);

		Position posn = Position.startPosition();
		assertNotEquals(zob1.hash(posn), zob2.hash(posn));
	}

	@Test
	public void incrementalUpdateSimpleMove() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a3));// avoid enpassant
	}

	@Test
	public void incrementalUpdateCapture() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/P7/8/8/8/1PPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a6, Square.b7, PieceType.PAWN));
	}

	@Test
	public void incrementalUpdateEnpassant() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4));// possible enpassant
	}

	private void checkMove(
			Position posn1,
			Move move) {
		Zobrist zob = new Zobrist(10);
		Position posn2 = posn1.move(move);

		assertEquals(zob.update(zob.hash(posn1), move), zob.hash(posn2));
	}
}
