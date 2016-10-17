package org.rjo.chess.pieces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Square;
import org.rjo.chess.TestUtil;

public class RookMoveTest {

	private Piece whiteRook;
	private Game game;

	private void setupGame(String fen) {
		game = Fen.decode(fen);
		whiteRook = game.getChessboard().getPieces(Colour.WHITE).get(PieceType.ROOK);
	}

	@Test
	public void startPosition() {
		Game game = new Game();
		Rook whiteRook = (Rook) game.getChessboard().getPieces(Colour.WHITE).get(PieceType.ROOK);
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), new HashSet<>());
	}

	@Test
	public void moveFromMiddleOfBoard() {
		setupGame("4k3/8/8/8/3R4/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rd4-d5", "Rd4-d6", "Rd4-d7", "Rd4-d8+",
				"Rd4-e4+", "Rd4-f4", "Rd4-g4", "Rd4-h4", "Rd4-d3", "Rd4-d2", "Rd4-d1", "Rd4-c4", "Rd4-b4", "Rd4-a4");
	}

	@Test
	public void moveFromA1() {
		setupGame("4k3/8/8/8/8/2K5/8/R7 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra1-a2", "Ra1-a3", "Ra1-a4", "Ra1-a5", "Ra1-a6",
				"Ra1-a7", "Ra1-a8+", "Ra1-b1", "Ra1-c1", "Ra1-d1", "Ra1-e1+", "Ra1-f1", "Ra1-g1", "Ra1-h1");
	}

	/**
	 * rook gets blocked by own pieces in north and east direction
	 */
	@Test
	public void moveFromA1WithBlockade() {
		setupGame("4k3/8/8/8/P7/8/8/R1K5 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra1-a2", "Ra1-a3", "Ra1-b1");
	}

	/**
	 * rook gets blocked by enemy pieces
	 */
	@Test
	public void moveFromA1WithCapture() {
		setupGame("4k3/8/8/8/p7/8/8/R1K5 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra1-a2", "Ra1-a3", "Ra1xa4", "Ra1-b1");
	}

	@Test
	public void moveFromH1() {
		setupGame("4k3/1K6/8/8/8/8/8/7R w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh1-h2", "Rh1-h3", "Rh1-h4", "Rh1-h5", "Rh1-h6",
				"Rh1-h7", "Rh1-h8+", "Rh1-g1", "Rh1-f1", "Rh1-e1+", "Rh1-d1", "Rh1-c1", "Rh1-b1", "Rh1-a1");
	}

	/**
	 * rook gets blocked by own pieces in north and west direction
	 */
	@Test
	public void moveFromH1WithBlockade() {
		setupGame("4k3/8/8/8/7P/8/8/5K1R w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh1-h2", "Rh1-h3", "Rh1-g1");
	}

	/**
	 * rook gets blocked by enemy pieces
	 */
	@Test
	public void moveFromH1WithCapture() {
		setupGame("4k3/8/8/8/7p/8/8/5K1R w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh1-h2", "Rh1-h3", "Rh1xh4", "Rh1-g1");
	}

	@Test
	public void moveFromA8() {
		setupGame("R7/8/8/1k6/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra8-a7", "Ra8-a6", "Ra8-a5+", "Ra8-a4",
				"Ra8-a3", "Ra8-a2", "Ra8-a1", "Ra8-b8+", "Ra8-c8", "Ra8-d8", "Ra8-e8", "Ra8-f8", "Ra8-g8", "Ra8-h8");
	}

	/**
	 * rook gets blocked by own pieces in south and east direction
	 */
	@Test
	public void moveFromA8WithBlockade() {
		setupGame("R1K5/8/8/P1k5/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra8-a7", "Ra8-a6", "Ra8-b8");
	}

	/**
	 * rook gets blocked by enemy pieces
	 */
	@Test
	public void moveFromA8WithCapture() {
		setupGame("R1K5/8/8/p7/3k4/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Ra8-a7", "Ra8-a6", "Ra8xa5", "Ra8-b8");
	}

	@Test
	public void moveFromH8() {
		setupGame("7R/8/8/1k6/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh8-h7", "Rh8-h6", "Rh8-h5+", "Rh8-h4",
				"Rh8-h3", "Rh8-h2", "Rh8-h1", "Rh8-g8", "Rh8-f8", "Rh8-e8", "Rh8-d8", "Rh8-c8", "Rh8-b8+", "Rh8-a8");
	}

	/**
	 * rook gets blocked by own pieces in south and west direction
	 */
	@Test
	public void moveFromH8WithBlockade() {
		setupGame("5K1R/8/8/6kP/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh8-h7", "Rh8-h6", "Rh8-g8+");
	}

	/**
	 * rook gets blocked by enemy pieces
	 */
	@Test
	public void moveFromH8WithCapture() {
		setupGame("5K1R/8/8/6kp/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteRook.findMoves(game.getChessboard()), "Rh8-h7", "Rh8-h6", "Rh8xh5+", "Rh8-g8+");
	}

	@Test
	public void attacksSquare() {
		setupGame("5K2/4R3/2k5/4p3/8/8/8/8 w - - 0 0");
		for (Square sq : new Square[] { Square.e8, Square.e6, Square.d7, Square.c7, Square.b7, Square.a7, Square.f7,
				Square.g7, Square.h7 }) {
			assertTrue("square " + sq, whiteRook.attacksSquare(game.getChessboard().getEmptySquares().getBitSet(), sq));
		}
		assertFalse(whiteRook.attacksSquare(game.getChessboard().getEmptySquares().getBitSet(), Square.c4));
	}

}
