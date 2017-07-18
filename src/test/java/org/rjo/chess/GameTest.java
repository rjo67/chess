package org.rjo.chess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

public class GameTest {

	@Test
	public void colourTest() {
		// increase coverage of enum
		assertArrayEquals(new Colour[] { Colour.WHITE, Colour.BLACK }, Colour.ALL_COLOURS);
		Colour.valueOf("WHITE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void wrongSideToMove() {
		Game game = new Game();
		game.getPosition().move(new Move(PieceType.PAWN, Colour.BLACK, Square.b7, Square.b5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void noPieceOnStartSquare() {
		Game game = new Game();
		game.getPosition().move(new Move(PieceType.PAWN, Colour.WHITE, Square.b5, Square.b6));
	}

	@Test
	public void moveNbr() {
		Game game = new Game();
		game.makeMove(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));
		assertEquals(1, game.getMoveNumber());
		game.makeMove(new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8, Square.a6));
		assertEquals(2, game.getMoveNumber());
	}

	@Test(expected = IllegalArgumentException.class)
	public void illegalMoveToNonEmptySquare() {
		Game game = Fen.decode("k1K5/8/8/3p4/2P5/8/8/8 w - - 0 1");
		game.getPosition().move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5));
	}

	/////////////////////////////////////////////////////
	// unmove tests auskommentiert...
	/////////////////////////////////////////////////////

	// @Test(expected = IllegalArgumentException.class)
	// public void unmoveWrongSideToMove() {
	// Game game = new Game();
	// game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.b7,
	// Square.b5));
	// }
	//
	// @Test
	// public void unmoveNbr() {
	// Game game = Fen.decode("r1bqkbnr/pppppppp/n7/8/1P6/8/P1PPPPPP/RNBQKBNR w
	// KQkq - 0 2");
	// game.unmove(new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8,
	// Square.a6));
	// assertEquals(1, game.getMoveNumber());
	// game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.b2,
	// Square.b4));
	// assertEquals(1, game.getMoveNumber());
	// }
	//
	// @Test(expected = IllegalArgumentException.class)
	// public void unmoveNoPieceOnEndSquare() {
	// Game game = Fen.decode("8/8/8/8/8/8/5p2/8 w - - 0 1");
	// game.unmove(new Move(PieceType.PAWN, Colour.BLACK, Square.f4,
	// Square.f3));
	// }
	//
	// @Test
	// public void testUnmove() {
	// Game game = Fen.decode("4k2r/5p2/p7/3r4/8/8/6P1/R3K2R w KkQ - 0 1");
	// game.unmove(new Move(PieceType.ROOK, Colour.BLACK, Square.d7,
	// Square.d5));
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.d5);
	// assertPieceAt(cb, Square.d7, PieceType.ROOK);
	// }
	//
	// @Test(expected = IllegalArgumentException.class)
	// public void illegalUnmoveToNonEmptySquare() {
	// Game game = Fen.decode("8/8/8/3p4/2P5/2P5/8/8 b - - 0 1");
	// game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c3,
	// Square.c4));
	// }
	//
	// @Test
	// public void unmoveKingsCastlingWhite() {
	// Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w kKQq - 0
	// 1");
	// Move move = Move.castleKingsSide(Colour.WHITE);
	// game.getPosition().move(move);
	// game.unmove(move);
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.f1);
	// assertEmptySquare(cb, Square.g1);
	// assertPieceAt(cb, Square.e1, PieceType.KING);
	// assertPieceAt(cb, Square.h1, PieceType.ROOK);
	// assertTrue(game.getPosition().canCastle(Colour.WHITE,
	// CastlingRights.KINGS_SIDE));
	// }
	//
	// @Test
	// public void unmoveKingsCastlingBlack() {
	// Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R4RK1 b kQq - 0
	// 1");
	// Move move = Move.castleKingsSide(Colour.BLACK);
	// game.getPosition().move(move);
	// game.unmove(move);
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.f8);
	// assertEmptySquare(cb, Square.g8);
	// assertPieceAt(cb, Square.e8, PieceType.KING);
	// assertPieceAt(cb, Square.h8, PieceType.ROOK);
	// assertTrue(game.getPosition().canCastle(Colour.BLACK,
	// CastlingRights.KINGS_SIDE));
	// }
	//
	// @Test
	// public void unmoveQueensCastlingWhite() {
	// Game game = Fen.decode("2kr4/pppppppp/8/8/8/8/PPPPPPPP/R3K3 w Q - 0 1");
	// Move move = Move.castleQueensSide(Colour.WHITE);
	// game.getPosition().move(move);
	// game.unmove(move);
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.c1);
	// assertEmptySquare(cb, Square.d1);
	// assertPieceAt(cb, Square.a1, PieceType.ROOK);
	// assertPieceAt(cb, Square.e1, PieceType.KING);
	// assertTrue(game.getPosition().canCastle(Colour.WHITE,
	// CastlingRights.QUEENS_SIDE));
	// }
	//
	// @Test
	// public void unmoveQueensCastlingBlack() {
	// Game game = Fen.decode("r3k3/pppppppp/8/8/8/8/PPPPPPPP/2KR4 b q - 0 1");
	// Move move = Move.castleQueensSide(Colour.BLACK);
	// game.getPosition().move(move);
	// game.unmove(move);
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.c8);
	// assertEmptySquare(cb, Square.d8);
	// assertPieceAt(cb, Square.a8, PieceType.ROOK);
	// assertPieceAt(cb, Square.e8, PieceType.KING);
	// assertTrue(game.getPosition().canCastle(Colour.BLACK,
	// CastlingRights.QUEENS_SIDE));
	// }
	//
	// @Test
	// public void unmoveCapture() {
	// Game game = Fen.decode("8/8/8/3P4/8/8/8/8 b - - 0 1");
	// game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5,
	// PieceType.PAWN));
	// Position cb = game.getPosition();
	// assertPieceAt(cb, Square.c4, PieceType.PAWN);
	// assertTrue(cb.getPieces(Colour.BLACK).get(PieceType.PAWN).pieceAt(Square.d5));
	// }
	//
	// @Test(expected = IllegalArgumentException.class)
	// public void unmoveNoPieceToCapture() {
	// Game game = Fen.decode("8/8/8/3P4/8/8/8/8 b - - 0 1");
	// game.unmove(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.b5,
	// PieceType.PAWN));
	// }
	//
	// @Test
	// public void unmovePromotion() {
	// Game game = Fen.decode("8/8/8/8/8/8/8/5q2 w - - 0 1");
	// Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.f2, Square.f1);
	// move.setPromotionPiece(PieceType.QUEEN);
	// game.unmove(move);
	// Position cb = game.getPosition();
	// assertEmptySquare(cb, Square.f1);
	// assertPieceAt(cb, Square.f2, PieceType.PAWN);
	// }

}
