package org.rjo.chess.position.check;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;

/**
 * Move generation where the king is in check.
 *
 * @author rich
 */
public class CheckTest {

	@Test
	public void checkRook() {
		Game game = Fen.decode("8/3n4/ppkpb3/1p2r3/P1R2r2/b2qq3/1n4K1/8 b - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.BLACK), "Kc6-b7", "Kc6-d5", "Nd7-c5", "Nb2xc4",
				"Re5-c5", "Rf4xc4", "Qd3xc4", "Qe3-c5", "Ba3-c5", "Be6xc4", "b5xc4");
	}

	@Test
	public void checkPromotion() {
		Game game = Fen.decode("r4K2/1PB1Q3/2N5/8/6k1/8/8/R2R4 w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), "Kf8-f7", "Kf8-g7", "Nc6-b8", "Nc6-d8",
				"Bc7-b8", "Bc7-d8", "Ra1xa8", "Rd1-d8", "Qe7-d8", "Qe7-e8", "b7xa8=R", "b7xa8=N", "b7xa8=B", "b7xa8=Q",
				"b7-b8=Q", "b7-b8=R", "b7-b8=B", "b7-b8=N");
	}

	@Test
	public void doubleCheck() {
		Game game = Fen.decode("5R2/3r1K2/1N6/8/2b5/8/4Q3/7k w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), "Kf7-e8", "Kf7-f6", "Kf7-g6");
	}

	@Test
	public void doubleCheckAdjacentPiece() {
		Game game = Fen.decode("5R2/4rK2/1N6/8/2b5/8/4Q3/7k w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE), "Kf7xe7", "Kf7-f6", "Kf7-g6");
	}

	@Test
	public void pin() {
		Game game = Fen.decode("rnbqkbnr/ppppppp1/8/7Q/4P3/8/PPPP1PPP/RNB1KBNR b - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.BLACK), "a7-a6", "b7-b6", "c7-c6", "d7-d6", "e7-e6",
				"g7-g6", "a7-a5", "b7-b5", "c7-c5", "d7-d5", "e7-e5", "g7-g5", "Rh8-h7", "Rh8-h6", "Rh8xh5", "Nb8-a6",
				"Nb8-c6", "Ng8-f6", "Ng8-h6");
	}

	@Test
	public void checkmate() {
		Game game = Fen.decode("4QR2/3r1K2/1N3PP1/8/2b5/8/8/7k w - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.WHITE));
	}

	@Test
	public void saragossaOpening() {
		Game game = Fen.decode("rnbqkbnr/ppp1pppp/3p4/8/Q7/2P5/PP1PPPPP/RNB1KBNR b - - 0 4");
		TestUtil.checkMoves(game.getPosition().findMoves(Colour.BLACK), "b7-b5", "c7-c6", "Nb8-c6", "Nb8-d7", "Bc8-d7",
				"Qd8-d7");
	}

	@Test
	public void checkAfterQueensideCastles() {
		Game game = Fen.decode("3k4/p1p1p1p1/8/8/8/p3p2p/P3P2P/R3K2R w KQ - 0 4");
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		TestUtil.checkMoves(moves, "O-O", "O-O-O+", "Ra1-b1", "Ra1-c1", "Ra1-d1+", "Rh1-g1", "Rh1-f1", "Ke1-d1",
				"Ke1-f1");
		Move castlingmove = moves.stream().filter(move -> move.isCastleQueensSide()).findAny()
				.orElseThrow(IllegalStateException::new);
		assertTrue(castlingmove.isCheck());
	}

	@Test
	public void checkAfterKingsSideCastles() {
		Game game = Fen.decode("5k2/p1p1p1p1/8/8/8/p3p2p/P3P2P/R3K2R w KQ - 0 4");
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		TestUtil.checkMoves(moves, "O-O+", "O-O-O", "Ra1-b1", "Ra1-c1", "Ra1-d1", "Rh1-g1", "Rh1-f1+", "Ke1-d1",
				"Ke1-f1");
		Move castlingmove = moves.stream().filter(move -> move.isCastleKingsSide()).findAny()
				.orElseThrow(IllegalStateException::new);
		assertTrue(castlingmove.isCheck());
	}
}
