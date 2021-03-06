package org.rjo.chess.pieces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.position.Position;

public class BishopMoveTest extends AbstractMoveTest {

	@Test
	public void locations() {
		Bishop b = new Bishop(Colour.WHITE, true);
		Square[] locn = b.getLocations();
		Set<Square> set = new HashSet<>(Arrays.asList(locn));
		assertTrue(set.contains(Square.c1));
		assertTrue(set.contains(Square.f1));
	}

	@Test
	public void startPosition() {
		setupGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0");
		TestUtil.checkMoves(findBishopMoves());
	}

	@Test
	public void moveFromMiddleOfBoard() {
		setupGame("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bd4-e5", "Bd4-f6", "Bd4xg7", "Bd4-c5", "Bd4-b6", "Bd4-a7", "Bd4-e3",
				"Bd4-f2", "Bd4-g1", "Bd4-c3", "Bd4-b2", "Bd4-a1");
	}

	@Test
	public void moveFromA1() {
		setupGame("4k3/8/8/8/8/8/8/B3K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba1-b2", "Ba1-c3", "Ba1-d4", "Ba1-e5", "Ba1-f6", "Ba1-g7", "Ba1-h8");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromA1WithBlockade() {
		setupGame("4k3/8/8/8/3P4/2K5/8/B7 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba1-b2");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromA1WithCapture() {
		setupGame("4k3/8/8/8/3b4/8/8/B3K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba1-b2", "Ba1-c3", "Ba1xd4");
	}

	@Test
	public void moveFromH1() {
		setupGame("4k3/8/8/8/8/8/8/4K2B w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh1-g2", "Bh1-f3", "Bh1-e4", "Bh1-d5", "Bh1-c6+", "Bh1-b7", "Bh1-a8");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromH1WithBlockade() {
		setupGame("4k3/8/8/8/4P3/8/8/4K2B w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh1-g2", "Bh1-f3");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromH1WithCapture() {
		setupGame("4k3/8/8/8/4p3/8/8/4K2B w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh1-g2", "Bh1-f3", "Bh1xe4");
	}

	@Test
	public void moveFromA8() {
		setupGame("B3k3/8/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba8-b7", "Ba8-c6+", "Ba8-d5", "Ba8-e4", "Ba8-f3", "Ba8-g2", "Ba8-h1");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromA8WithBlockade() {
		setupGame("B3k3/8/8/3P4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba8-b7", "Ba8-c6+");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromA8WithCapture() {
		setupGame("B3k3/8/8/3p4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Ba8-b7", "Ba8-c6+", "Ba8xd5");
	}

	@Test
	public void moveFromH8() {
		setupGame("4k2B/8/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh8-g7", "Bh8-f6", "Bh8-e5", "Bh8-d4", "Bh8-c3", "Bh8-b2", "Bh8-a1");
	}

	/**
	 * blocked by own pieces
	 */
	@Test
	public void moveFromH8WithBlockade() {
		setupGame("4k2B/8/8/4P3/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh8-g7", "Bh8-f6");
	}

	/**
	 * blocked by enemy pieces
	 */
	@Test
	public void moveFromH8WithCapture() {
		setupGame("4k2B/8/8/4p3/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findBishopMoves(), "Bh8-g7", "Bh8-f6", "Bh8xe5");
	}

	@Test
	public void attacksSquare() {
		setupGame("k2B1K2/8/8/4P3/8/8/8/8 w - - 0 4");
		Piece whiteBishop = game.getPosition().getPieces(Colour.WHITE)[PieceType.BISHOP.ordinal()];
		Position posn = game.getPosition();
		for (Square sq : new Square[] { Square.c7, Square.b6, Square.a5, Square.e7, Square.f6, Square.g5, Square.h4 }) {
			assertTrue(whiteBishop.attacksSquare(posn.getTotalPieces().flip(), sq), "square " + sq);
		}
		assertFalse(whiteBishop.attacksSquare(posn.getTotalPieces().flip(), Square.c4));
	}

	@Test
	public void pinnedBishops() {
		setupGame("rnbqkbnr/ppppppp1/8/b4r2/P3r3/1B6/2PBB3/2B1K3 w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), "c2-c3", "c2-c4", "Bb3-a2", "Bb3-c4", "Bb3-d5",
				"Bb3-e6", "Bb3xf7+", "Bc1-b2", "Bc1-a3", "Bd2-c3", "Bd2-b4", "Bd2xa5", "Ke1-d1");
	}

	/** same as pinnedBishops, but extra black bishop puts the king in check */
	@Test
	public void pinnedBishopsKingInCheck() {
		setupGame("rnbqkbnr/ppppppp1/8/b4r2/P3r2b/1B6/2PBB3/2B1K3 w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), "Ke1-d1");
	}

	@Test
	public void pinnedSpeed() {
		setupGame("rnbqkbnr/ppppppp1/8/b4r2/4r3/8/3BB3/4K3 w - - 0 4");
		var NBR_ITERS = 500000;
		var sw = StopWatch.createStarted();
		for (int i = 0; i < NBR_ITERS; i++) {
			TestUtil.checkMoves(findBishopMoves(), "Bd2-c3", "Bd2-b4", "Bd2xa5");
		}
		System.out.println("pinned bishops: " + sw.getTime());
		// takes ~: 8800ms for 1000000 times, not evaluating 'pinnedPieces'
		// takes ~5300ms for 1000000 times,  evaluating 'pinnedPieces'
	}

	@Test
	public void speedTest() {
		setupGame("r3k2r/pp3p2/8/4B3/2p1b3/8/PPPPB2P/R3K2R w KQkq - 0 0");
		// init
		assertEquals(17, findBishopMoves().size());
		int repeats = 10;
		long start = System.currentTimeMillis();
		for (int loop = 0; loop < repeats; loop++) {
			for (int i = 0; i < 10000; i++) {
				assertEquals(17, findBishopMoves().size());
			}
		}
		long timeTaken = System.currentTimeMillis() - start;
		assertTrue((timeTaken / repeats) < 250, "took longer than 250ms: " + timeTaken);
		System.out.println(timeTaken);
	}
}
