package org.rjo.chess.pieces;

import org.junit.jupiter.api.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;

public class BlackPawnTest extends AbstractMoveTest {

	@Test
	public void startPosition() {
		setupGame("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a7-a6", "a7-a5",
				"b7-b6", "b7-b5", "c7-c6", "c7-c5", "d7-d6", "d7-d5", "e7-e6", "e7-e5", "f7-f6", "f7-f5", "g7-g6", "g7-g5", "h7-h6", "h7-h5");
	}

	@Test
	public void blockedPawn() {
		setupGame("4k3/p7/p7/8/8/8/8/4K3 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a6-a5");
	}

	@Test
	public void captureLeft() {
		setupGame("4k3/8/8/1p6/PP6/8/8/4K3 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b5xa4");
	}

	@Test
	public void captureLeftPromotion() {
		setupGame("4k3/8/8/8/8/8/1p6/RK6 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b2xa1=R+", "b2xa1=N", "b2xa1=B", "b2xa1=Q+");
	}

	@Test
	public void captureRight() {
		setupGame("4k3/8/8/8/p7/PP6/8/4K3 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a4xb3");
	}

	@Test
	public void captureRightPromotion() {
		setupGame("4k3/8/8/8/8/8/2p5/2KR4 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "c2xd1=R+", "c2xd1=N", "c2xd1=B", "c2xd1=Q+");
	}

	@Test
	public void promotion() {
		setupGame("4k3/8/8/8/8/8/p7/4K3 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a2-a1=Q+", "a2-a1=B", "a2-a1=N", "a2-a1=R+");
	}

	@Test
	public void promotionRoce() {
		setupGame("n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1");
		TestUtil.checkMoves(game.getPosition().findMoves(game.getPosition().getSideToMove()), "g2-g1=R", "g2-g1=N+", "g2-g1=B", "g2-g1=Q",
				"g2xf1=R", "g2xf1=N",
				"g2xf1=B+", "g2xf1=Q+", "g2xh1=R", "g2xh1=N", "g2xh1=B", "g2xh1=Q", "Na8-b6", "Na8xc7", "Nc8-b6", "Nc8-d6", "Nc8xa7", "Nc8-e7",
				"Kd7-c6", "Kd7-d6", "Kd7-e6", "Kd7xc7", "Kd7-e7", "Kd7-e8");

	}

	@Test
	public void enpassantRight() {
		setupGame("4k3/8/8/8/pP6/8/8/4K3 b - b3 0 0");
		TestUtil.checkMoves(findPawnMoves(), "a4-a3", "a4xb3");
	}

	@Test
	public void enpassantLeft() {
		setupGame("4k3/8/8/8/Pp6/8/8/4K3 b - a3 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b4-b3", "b4xa3");
	}

	@Test
	public void enpassantWithTwoCandidatePawns() {
		setupGame("4k3/8/8/8/1pPp4/8/8/4K3 b - c3 0 0");
		TestUtil.checkMoves(findPawnMoves(), "b4-b3", "b4xc3", "d4-d3", "d4xc3");
	}

	@Test
	public void checkLeft() {
		setupGame("4k3/8/8/3p4/8/2K5/8/8 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d4+");
	}

	@Test
	public void checkRight() {
		setupGame("4k3/8/8/3p4/8/4K3/8/8 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d4+");
	}

	@Test
	public void checkCaptureLeft() {
		setupGame("4k3/8/8/3p4/2P5/1K6/8/8 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5-d4", "d5xc4+");
	}

	@Test
	public void checkCaptureRight() {
		setupGame("4k3/8/8/3p4/3PP3/5K2/8/8 b - - 0 0");
		TestUtil.checkMoves(findPawnMoves(), "d5xe4+");
	}

	@Test
	public void enpassantCheck() {
		setupGame("8/3pk3/8/4P3/8/8/5K2/8 b - - 0 0");
		// make pawn move to setup enpassant possibility
		game.makeMove(new Move(PieceType.PAWN, Colour.BLACK, Square.d7, Square.d5));
		TestUtil.checkMoves(findPawnMoves(), "e5xd6+", "e5-e6");
	}

	@Test
	public void enpassantDiscoveredCheckHorizontally() {
		setupGame("8/3p4/8/2k1P2Q/8/8/5K2/8 b - - 0 0");
		// make pawn move to setup enpassant possibility
		game.makeMove(new Move(PieceType.PAWN, Colour.BLACK, Square.d7, Square.d5));
		TestUtil.checkMoves(findPawnMoves(), "e5xd6+", "e5-e6");
	}

	@Test
	public void enpassantDiscoveredCheckDiagonally() {
		setupGame("6B1/3p4/8/4P3/2k5/8/5K2/8 b - - 0 0");
		// make pawn move to setup enpassant possibility
		game.makeMove(new Move(PieceType.PAWN, Colour.BLACK, Square.d7, Square.d5));
		TestUtil.checkMoves(findPawnMoves(), "e5xd6+", "e5-e6");
	}
}
