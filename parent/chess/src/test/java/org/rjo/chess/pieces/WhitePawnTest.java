package org.rjo.chess.pieces;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;

public class WhitePawnTest extends AbstractMoveTest {

	@Test
	public void startPosition() {
		setupGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a2-a3", "a2-a4",
				"b2-b3", "b2-b4", "c2-c3", "c2-c4", "d2-d3", "d2-d4", "e2-e3", "e2-e4", "f2-f3", "f2-f4", "g2-g3", "g2-g4", "h2-h3", "h2-h4");
	}

	@Test
	public void blockedPawn() {
		setupGame("4k3/8/8/8/8/P7/P7/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), TestUtil.KING_FILTER, "a3-a4");
	}

	@Test
	public void captureLeft() {
		setupGame("4k3/8/8/8/8/pp6/1P6/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), TestUtil.KING_FILTER, "b2xa3");
	}

	@Test
	public void captureLeftPromotion() {
		setupGame("rk6/1P6/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), TestUtil.KING_FILTER, "b7xa8=R+", "b7xa8=N", "b7xa8=B", "b7xa8=Q+");
	}

	@Test
	public void captureRight() {
		setupGame("4k3/8/8/8/8/pp6/P7/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a2xb3");
	}

	@Test
	public void captureRightPromotion() {
		setupGame("kr6/P7/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a7xb8=R+", "a7xb8=N", "a7xb8=B", "a7xb8=Q+");
	}

	@Test
	public void promotionCheckKnight() {
		setupGame("8/4k1P1/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "g7-g8=Q", "g7-g8=B", "g7-g8=N+", "g7-g8=R");
	}

	@Test
	public void promotionCheckBishop() {
		setupGame("8/6P1/4k3/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "g7-g8=Q+", "g7-g8=B+", "g7-g8=N", "g7-g8=R");
	}

	@Test
	public void promotionCheckRook() {
		setupGame("1k6/6P1/8/8/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "g7-g8=Q+", "g7-g8=B", "g7-g8=N", "g7-g8=R+");
	}

	@Test
	public void enpassantRight() {
		setupGame("4k3/8/8/Pp6/8/8/8/4K3 w - b6 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a5-a6", "a5xb6");
	}

	@Test
	public void enpassantLeft() {
		setupGame("4k3/8/8/pP6/8/8/8/4K3 w - a6 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b5-b6", "b5xa6");
	}

	@Test
	public void enpassantWithTwoCandidatePawns() {
		setupGame("4k3/8/8/1PpP4/8/8/8/4K3 w - c6 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b5-b6", "b5xc6", "d5-d6", "d5xc6");
	}

	@Test
	public void checkRight() {
		setupGame("8/4k3/8/3P4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d6+");
	}

	@Test
	public void checkLeft() {
		setupGame("8/2k5/8/3P4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d6+");
	}

	@Test
	public void checkCaptureLeft() {
		setupGame("8/1k6/2p5/3P4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d6", "d5xc6+");
	}

	@Test
	public void checkCaptureRight() {
		setupGame("8/5k2/3pp3/3P4/8/8/8/4K3 w - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5xe6+");
	}

	@Test
	public void pinnedSpeed() {
		setupGame("4k3/7b/8/1q3P2/2P5/3K1P1q/8/8 w - - 0 0");
		var NBR_ITERS = 100000;
		var sw = StopWatch.createStarted();
		for (int i = 0; i < NBR_ITERS; i++) {
			TestUtil.checkMoves(findPawnMoves(), "c4xb5");
		}
		System.out.println("pinned pawn: " + sw.getTime());
		// takes ~7300ms for a million times, not evaluating 'pinnedPieces'
		// takes ~xms for a million times,  evaluating 'pinnedPieces'
	}

	@Test
	public void attacksSquare() {
		setupGame("8/5k2/8/3P4/8/8/8/4K3 w - - 0 0");
		var pawn = game.getPosition().getPieces(Colour.WHITE)[PieceType.PAWN.ordinal()];
		assertTrue(pawn.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.e6));
		assertFalse(pawn.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.d6));
		assertFalse(pawn.attacksSquare(game.getPosition().getTotalPieces().flip(), Square.e7));
	}
}
