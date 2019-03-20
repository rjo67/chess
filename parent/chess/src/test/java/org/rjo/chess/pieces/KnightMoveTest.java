package org.rjo.chess.pieces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;

public class KnightMoveTest extends AbstractMoveTest {

	Piece whiteKnight;

	@Test
	public void startPosition() {
		setupGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nb1-a3", "Nb1-c3", "Ng1-f3", "Ng1-h3");
	}

	@Test
	public void moveFromMiddleOfBoard() {
		setupGame("4k3/8/8/8/3N4/5p2/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nd4-e6", "Nd4-f5", "Nd4xf3", "Nd4-e2", "Nd4-c2", "Nd4-b3", "Nd4-c6",
				"Nd4-b5");
	}

	@Test
	public void moveFromA1() {
		setupGame("4k3/8/8/8/8/8/8/N3K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na1-b3", "Na1-c2");
	}

	@Test
	public void pinnedSpeed() {
		setupGame("2K1N1r1/8/8/8/8/8/8/3k4 w - -");
		var sw = new StopWatch();
		sw.start();
		var NBR_ITERS = 100000;
		for (int i = 0; i < NBR_ITERS; i++) {
			TestUtil.checkMoves(findKnightMoves());
		}
		System.out.println("pinned knights:" + sw.getTime());
		// takes ~5889ms for a million times, not evaluating 'pinnedPieces'
		// takes ~4700ms for a million times,  evaluating 'pinnedPieces'
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromA1WithBlockade() {
		setupGame("4k3/8/8/8/8/8/2K5/N7 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na1-b3");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromA1WithCapture() {
		setupGame("4k3/8/8/8/8/8/2p5/N3K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na1-b3", "Na1xc2");
	}

	@Test
	public void moveFromH1() {
		setupGame("4k3/8/8/8/8/8/8/4K2N w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh1-g3", "Nh1-f2");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromH1WithBlockade() {
		setupGame("4k3/8/8/8/8/8/5K2/7N w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh1-g3");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromH1WithCapture() {
		setupGame("4k3/8/8/8/8/6p1/5K2/7N w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh1xg3");
	}

	@Test
	public void moveFromA8() {
		setupGame("N3k3/8/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na8-b6", "Na8-c7+");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromA8WithBlockade() {
		setupGame("N3k3/2K5/8/8/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na8-b6");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromA8WithCapture() {
		setupGame("N3k3/2p5/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Na8-b6", "Na8xc7+");
	}

	@Test
	public void moveFromH8() {
		setupGame("4k2N/8/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh8-g6", "Nh8-f7");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromH8WithBlockade() {
		setupGame("4k2N/5K2/8/8/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh8-g6");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromH8WithCapture() {
		setupGame("4k2N/5p2/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findKnightMoves(), "Nh8-g6", "Nh8xf7");
	}

	@Test
	public void discoveredCheck() {
		setupGame("8/8/8/1k4NR/8/4K3/8/8 w - - 0 10");
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		TestUtil.checkMoves(moves, "Rh5-h6", "Rh5-h7", "Rh5-h8", "Rh5-h4", "Rh5-h3", "Rh5-h2", "Rh5-h1", "Ke3-d2", "Ke3-d3", "Ke3-d4",
				"Ke3-e2", "Ke3-e4", "Ke3-f2", "Ke3-f3", "Ke3-f4", "Ng5-h3+", "Ng5-f3+", "Ng5-e4+", "Ng5-e6+", "Ng5-f7+", "Ng5-h7+");
	}

	@Test
	public void kingInCheckFollowedByDiscoveredCheck() {
		setupGame("8/4r3/8/1k4NR/8/4K3/8/8 w - - 0 10");
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		TestUtil.checkMoves(moves, "Ke3-d2", "Ke3-d3", "Ke3-d4", "Ke3-f2", "Ke3-f3", "Ke3-f4", "Ng5-e4+", "Ng5-e6+");
	}

	@Test
	public void attacksSquare() {
		setupGame("4k3/8/8/8/3N4/5p2/8/4K3 w - - 0 0");
		whiteKnight = game.getPosition().getPieces(Colour.WHITE)[PieceType.KNIGHT.ordinal()];
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.c2));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.b3));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.b5));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.c6));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.e6));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.f5));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.f3));
		assertTrue(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.e2));
		assertFalse(whiteKnight.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.c5));
	}

}
