package org.rjo.chess.pieces;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

public class QueenMoveTest {

	private Piece whiteQueen;
	private Game game;

	private void setupGame(String fen) {
		game = Fen.decode(fen);
		whiteQueen = game.getPosition().getPieces(Colour.WHITE)[PieceType.QUEEN.ordinal()];
	}

	@Test
	public void startPosition() {
		Game game = new Game();
		Queen whiteQueen = new Queen(Colour.WHITE);
		TestUtil.checkMoves(whiteQueen.findMoves(game.getPosition()));
	}

	@Test
	public void moveFromMiddleOfBoard() {
		setupGame("4k3/8/8/8/3Q4/8/8/4K3  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER, "Qd4-d5", "Qd4-d6",
				"Qd4-d7+", "Qd4-d8+", "Qd4-e4+", "Qd4-f4", "Qd4-g4", "Qd4-h4", "Qd4-d3", "Qd4-d2", "Qd4-d1", "Qd4-c4",
				"Qd4-b4", "Qd4-a4+", "Qd4-e5+", "Qd4-f6", "Qd4-g7", "Qd4-h8+", "Qd4-c5", "Qd4-b6", "Qd4-a7", "Qd4-e3+",
				"Qd4-f2", "Qd4-g1", "Qd4-c3", "Qd4-b2", "Qd4-a1");
	}

	@Test
	public void moveFromA1() {
		setupGame("4k3/8/8/8/8/8/2K5/Q7  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER, "Qa1-a2", "Qa1-a3",
				"Qa1-a4+", "Qa1-a5", "Qa1-a6", "Qa1-a7", "Qa1-a8+", "Qa1-b1", "Qa1-c1", "Qa1-d1", "Qa1-e1+", "Qa1-f1",
				"Qa1-g1", "Qa1-h1", "Qa1-b2", "Qa1-c3", "Qa1-d4", "Qa1-e5+", "Qa1-f6", "Qa1-g7", "Qa1-h8+");
	}

	/**
	 * queen gets blocked by own pieces
	 */
	@Test
	public void moveFromA1WithBlockade() {
		setupGame("4k3/8/8/8/P7/2P5/8/Q1K5  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER.or(TestUtil.PAWN_FILTER),
				"Qa1-a2", "Qa1-a3", "Qa1-b1", "Qa1-b2");
	}

	/**
	 * queen gets blocked by enemy pieces
	 */
	@Test
	public void moveFromA1WithCapture() {
		setupGame("4k3/8/8/4p3/p7/8/8/Q1K5  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER, "Qa1-a2", "Qa1-a3",
				"Qa1xa4+", "Qa1-b1", "Qa1-b2", "Qa1-c3", "Qa1-d4", "Qa1xe5+");
	}

	@Test
	public void moveFromA8() {
		setupGame("Q7/8/8/2k5/8/8/2K5/8  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER, "Qa8-a7+", "Qa8-a6",
				"Qa8-a5+", "Qa8-a4", "Qa8-a3+", "Qa8-a2", "Qa8-a1", "Qa8-b8", "Qa8-c8+", "Qa8-d8", "Qa8-e8", "Qa8-f8+",
				"Qa8-g8", "Qa8-h8", "Qa8-b7", "Qa8-c6+", "Qa8-d5+", "Qa8-e4", "Qa8-f3", "Qa8-g2", "Qa8-h1");
	}

	/**
	 * queen gets blocked by own pieces
	 */
	@Test
	public void moveFromA8WithBlockade() {
		setupGame("Q1K5/8/2P5/P5k1/8/8/8/8  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER.or(TestUtil.PAWN_FILTER),
				"Qa8-a7", "Qa8-a6", "Qa8-b8", "Qa8-b7");
	}

	/**
	 * queen gets blocked by enemy pieces
	 */
	@Test
	public void moveFromA8WithCapture() {
		setupGame("Q1K5/8/8/p1kp4/8/8/8/8  w - - 0 0");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), TestUtil.KING_FILTER, "Qa8-a7+", "Qa8-a6",
				"Qa8xa5+", "Qa8-b8", "Qa8-b7", "Qa8-c6+", "Qa8xd5+");
	}

	@Test
	public void attacksSquareStraight() {
		setupGame("5K2/4Q3/8/4p3/8/8/k7/8  w - - 0 0");
		for (Square sq : new Square[] { Square.e8, Square.e6, Square.d7, Square.c7, Square.b7, Square.a7, Square.f7,
				Square.g7, Square.h7 }) {
			assertTrue(whiteQueen.attacksSquare(game.getPosition().getTotalPieces().flip(), sq), "square " + sq);
		}
		assertFalse(whiteQueen.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.c4));
	}

	@Test
	public void attacksSquareDiag() {
		setupGame("3Q1K2/8/2k5/4p3/8/8/8/8  w - - 0 0");
		for (Square sq : new Square[] { Square.c7, Square.b6, Square.a5, Square.e7, Square.f6, Square.g5, Square.h4 }) {
			assertTrue(whiteQueen.attacksSquare(game.getPosition().getTotalPieces().flip(), sq), "square " + sq);
		}
		assertFalse(whiteQueen.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.c4));
	}

}
