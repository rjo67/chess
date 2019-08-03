package org.rjo.chess.pieces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;

/**
 * Tests for the methods in Piece.
 *
 * @author rich
 */
public class PieceTest {

	@Test
	public void move() {
		Queen queen = new Queen(Colour.WHITE, Square.b6);
		queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.b6, Square.a6));
		assertFalse(queen.getLocationBitBoard().get(Square.b6.bitIndex()));
		assertTrue(queen.getLocationBitBoard().get(Square.a6.bitIndex()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void badMoveNonExistingPiece() {
		Queen queen = new Queen(Colour.WHITE, Square.b6);
		queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.g6, Square.a6));
	}

	@Test
	public void promotionMove() {
		Pawns pawn = new Pawns(Colour.WHITE, Square.a7);
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a7, Square.a8);
		move.setPromotionPiece(PieceType.BISHOP);
		pawn.move(move);
		assertTrue(pawn.getLocationBitBoard().isEmpty());
	}

	@Test
	public void pawnSymbolWhite() {
		assertEquals("", new Pawns(Colour.WHITE, Square.e6).getSymbol());
	}

	@Test
	public void rookSymbolWhite() {
		assertEquals("R", new Rook(Colour.WHITE, Square.a1).getSymbol());
	}

	@Test
	public void knightSymbolWhite() {
		assertEquals("N", new Knight(Colour.WHITE, Square.a1).getSymbol());
	}

	@Test
	public void bishopSymbolWhite() {
		assertEquals("B", new Bishop(Colour.WHITE, Square.a1).getSymbol());
	}

	@Test
	public void queenSymbolWhite() {
		assertEquals("Q", new Queen(Colour.WHITE, Square.a1).getSymbol());
	}

	@Test
	public void kingSymbolWhite() {
		assertEquals("K", new King(Colour.WHITE, Square.a1).getSymbol());
	}

	@Test
	public void pawnSymbolBlack() {
		assertEquals("", new Pawns(Colour.BLACK, Square.d4).getSymbol());
	}

	@Test
	public void rookSymbolBlack() {
		assertEquals("R", new Rook(Colour.BLACK, Square.a1).getSymbol());
	}

	@Test
	public void knightSymbolBlack() {
		assertEquals("N", new Knight(Colour.BLACK, Square.a1).getSymbol());
	}

	@Test
	public void bishopSymbolBlack() {
		assertEquals("B", new Bishop(Colour.BLACK, Square.a1).getSymbol());
	}

	@Test
	public void queenSymbolBlack() {
		assertEquals("Q", new Queen(Colour.BLACK, Square.a1).getSymbol());
	}

	@Test
	public void kingSymbolBlack() {
		assertEquals("K", new King(Colour.BLACK, Square.a1).getSymbol());
	}

	@Test
	public void pawnFenSymbolWhite() {
		assertEquals("P", new Pawns(Colour.WHITE, Square.h4).getFenSymbol());
	}

	@Test
	public void rookFenSymbolWhite() {
		assertEquals("R", new Rook(Colour.WHITE, Square.a1).getFenSymbol());
	}

	@Test
	public void knightFenSymbolWhite() {
		assertEquals("N", new Knight(Colour.WHITE, Square.a1).getFenSymbol());
	}

	@Test
	public void bishopFenSymbolWhite() {
		assertEquals("B", new Bishop(Colour.WHITE, Square.a1).getFenSymbol());
	}

	@Test
	public void queenFenSymbolWhite() {
		assertEquals("Q", new Queen(Colour.WHITE, Square.a1).getFenSymbol());
	}

	@Test
	public void kingFenSymbolWhite() {
		assertEquals("K", new King(Colour.WHITE, Square.a1).getFenSymbol());
	}

	@Test
	public void pawnFenSymbolBlack() {
		assertEquals("p", new Pawns(Colour.BLACK, Square.a5).getFenSymbol());
	}

	@Test
	public void rookFenSymbolBlack() {
		assertEquals("r", new Rook(Colour.BLACK, Square.a1).getFenSymbol());
	}

	@Test
	public void knightFenSymbolBlack() {
		assertEquals("n", new Knight(Colour.BLACK, Square.a1).getFenSymbol());
	}

	@Test
	public void bishopFenSymbolBlack() {
		assertEquals("b", new Bishop(Colour.BLACK, Square.a1).getFenSymbol());
	}

	@Test
	public void queenFenSymbolBlack() {
		assertEquals("q", new Queen(Colour.BLACK, Square.a1).getFenSymbol());
	}

	@Test
	public void kingFenSymbolBlack() {
		assertEquals("k", new King(Colour.BLACK, Square.a1).getFenSymbol());
	}
}
