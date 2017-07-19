package org.rjo.chess.pieces;

import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.TestUtil;

/**
 * Test movement of the king. Tests are repeated for white and black kings, although there shouldn't be any difference.
 * NB: For these tests the position of the opponent's king must always be specified.
 *
 * @author rich
 */
public class KingMoveTest {
	private Piece whiteKing;
	private Piece blackKing;
	private Game game;

	private void setupGame(
			String fen) {
		game = Fen.decode(fen);
		setupKings();
	}

	private void setupKings() {
		whiteKing = game.getPosition().getPieces(Colour.WHITE)[PieceType.KING.ordinal()];
		blackKing = game.getPosition().getPieces(Colour.BLACK)[PieceType.KING.ordinal()];
	}

	@Before
	public void init() {
		// NB: initialised without any starting squares
		whiteKing = new King(Colour.WHITE);
		blackKing = new King(Colour.BLACK);
	}

	@Test
	public void startPosition() {
		Game game = new Game();
		whiteKing = new King(Colour.WHITE, true);
		blackKing = new King(Colour.BLACK, true);
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), new HashSet<>());
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), new HashSet<>());
	}

	@Test
	public void middleOfBoard() {
		setupGame("8/8/8/2K2k2/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Kc5-c4", "Kc5-c6", "Kc5-b4", "Kc5-b5", "Kc5-b6", "Kc5-d4", "Kc5-d5",
				"Kc5-d6");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Kf5-f4", "Kf5-f6", "Kf5-e4", "Kf5-e5", "Kf5-e6", "Kf5-g4", "Kf5-g5",
				"Kf5-g6");
	}

	@Test
	public void onRank1() {
		setupGame("8/8/8/8/8/8/8/2K2k2 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Kc1-c2", "Kc1-b1", "Kc1-b2", "Kc1-d1", "Kc1-d2");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Kf1-f2", "Kf1-e1", "Kf1-e2", "Kf1-g1", "Kf1-g2");
	}

	@Test
	public void onRank8() {
		setupGame("2K3k1/8/8/8/8/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Kc8-c7", "Kc8-b8", "Kc8-b7", "Kc8-d8", "Kc8-d7");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Kg8-g7", "Kg8-f8", "Kg8-f7", "Kg8-h8", "Kg8-h7");
	}

	@Test
	public void onFile1() {
		setupGame("8/k7/8/8/K7/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Ka4-a5", "Ka4-a3", "Ka4-b3", "Ka4-b4", "Ka4-b5");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Ka7-a8", "Ka7-a6", "Ka7-b6", "Ka7-b7", "Ka7-b8");
	}

	@Test
	public void onFile8() {
		setupGame("8/7k/8/8/7K/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Kh4-h5", "Kh4-h3", "Kh4-g3", "Kh4-g4", "Kh4-g5");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Kh7-h8", "Kh7-h6", "Kh7-g6", "Kh7-g7", "Kh7-g8");
	}

	@Test
	public void kingsAdjacent() {
		setupGame("8/8/8/4k3/2K5/8/8/8 w - - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Kc4-b3", "Kc4-b4", "Kc4-b5", "Kc4-c3", "Kc4-c5", "Kc4-d3"
		// not possible because of adjacent king: "Kc4-d4", "Kc4-d5"
		);
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Ke5-f4", "Ke5-f5", "Ke5-f6", "Ke5-e6", "Ke5-e4", "Ke5-d6"
		// not possible because of adjacent king: "Ke5-d5", "Ke5-d4"
		);
	}

	@Test
	public void castleKingsSide() {
		setupGame("8/6k1/8/8/8/8/3PPP2/4K2R w K - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Ke1-d1", "Ke1-f1", "O-O");
	}

	@Test
	public void castleKingsSideWithCheck() {
		setupGame("r3k2r/p6p/8/B7/8/8/P4K1P/R6R b kq - 0 0");
		List<Move> moves = game.getPosition().findMoves(Colour.BLACK);
		TestUtil.checkMoves(moves, "a7-a6", "h7-h6", "h7-h5", "Ra8-b8", "Ra8-c8", "Ra8-d8", "Rh8-g8", "Rh8-f8+", "Ke8-d7", "Ke8-e7", "Ke8-f7",
				"Ke8-f8", "O-O+");
	}

	@Test
	public void castleKingsSideInCheck() {
		setupGame("8/6k1/8/8/8/8/3PPP1b/4K2R w K - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Ke1-d1", "Ke1-f1");
	}

	@Test
	public void castleKingsSideBlack() {
		setupGame("4k2r/3ppp2/8/8/8/8/8/6K1 b k - 0 0");
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Ke8-d8", "Ke8-f8", "O-O");
	}

	@Test
	public void castleKingsSideInCheckBlack() {
		Game game = Fen.decode("4k2r/3ppp1B/8/8/8/8/8/6K1 b k - 0 0");
		Piece blackKing = game.getPosition().getPieces(Colour.BLACK)[PieceType.KING.ordinal()];
		TestUtil.checkMoves(blackKing.findMoves(game.getPosition()), "Ke8-d8", "Ke8-f8");
	}

	@Test
	public void castleQueensSide() {
		setupGame("8/6k1/8/8/8/8/8/R3K2R w Q - 0 0");
		TestUtil.checkMoves(whiteKing.findMoves(game.getPosition()), "Ke1-d1", "Ke1-f1", "Ke1-d2", "Ke1-e2", "Ke1-f2", "O-O-O");
	}

}
