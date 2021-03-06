package org.rjo.chess.pieces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
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
	public void addPiece() {
		Queen queen = new Queen(Colour.WHITE, Square.b6);
		queen.addPiece(Square.a6);
		assertEquals(2, queen.getBitBoard().cardinality());
	}

	@Test
	public void removePiece() {
		Pawn pawn = new Pawn(Colour.WHITE, Square.a6);
		pawn.removePiece(Square.a6);
		assertTrue(pawn.getBitBoard().isEmpty());
	}

	@Test
	public void removeNonExistentPiece() {
		Pawn pawn = new Pawn(Colour.WHITE, Square.a5);

		assertThrows(IllegalArgumentException.class, () -> pawn.removePiece(Square.a6));
	}

	@Test
	public void move() {
		Queen queen = new Queen(Colour.WHITE, Square.b6);
		queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.b6, Square.a6));
		assertFalse(queen.getBitBoard().get(Square.b6.bitIndex()));
		assertTrue(queen.getBitBoard().get(Square.a6.bitIndex()));
	}

	@Test
	public void badMoveNonExistingPiece() {
		Queen queen = new Queen(Colour.WHITE, Square.b6);
		assertThrows(IllegalArgumentException.class,
				() -> queen.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.g6, Square.a6)));
	}

	@Test
	public void promotionMove() {
		Pawn pawn = new Pawn(Colour.WHITE, Square.a7);
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a7, Square.a8);
		move.setPromotionPiece(PieceType.BISHOP);
		pawn.move(move);
		assertTrue(pawn.getBitBoard().isEmpty());
	}

	@Test
	public void pawnSymbolWhite() {
		assertEquals("", new Pawn(Colour.WHITE).getSymbol());
	}

	@Test
	public void rookSymbolWhite() {
		assertEquals("R", new Rook(Colour.WHITE).getSymbol());
	}

	@Test
	public void knightSymbolWhite() {
		assertEquals("N", new Knight(Colour.WHITE).getSymbol());
	}

	@Test
	public void bishopSymbolWhite() {
		assertEquals("B", new Bishop(Colour.WHITE).getSymbol());
	}

	@Test
	public void queenSymbolWhite() {
		assertEquals("Q", new Queen(Colour.WHITE).getSymbol());
	}

	@Test
	public void kingSymbolWhite() {
		assertEquals("K", new King(Colour.WHITE).getSymbol());
	}

	@Test
	public void pawnSymbolBlack() {
		assertEquals("", new Pawn(Colour.BLACK).getSymbol());
	}

	@Test
	public void rookSymbolBlack() {
		assertEquals("R", new Rook(Colour.BLACK).getSymbol());
	}

	@Test
	public void knightSymbolBlack() {
		assertEquals("N", new Knight(Colour.BLACK).getSymbol());
	}

	@Test
	public void bishopSymbolBlack() {
		assertEquals("B", new Bishop(Colour.BLACK).getSymbol());
	}

	@Test
	public void queenSymbolBlack() {
		assertEquals("Q", new Queen(Colour.BLACK).getSymbol());
	}

	@Test
	public void kingSymbolBlack() {
		assertEquals("K", new King(Colour.BLACK).getSymbol());
	}

	@Test
	public void pawnFenSymbolWhite() {
		assertEquals("P", new Pawn(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void rookFenSymbolWhite() {
		assertEquals("R", new Rook(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void knightFenSymbolWhite() {
		assertEquals("N", new Knight(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void bishopFenSymbolWhite() {
		assertEquals("B", new Bishop(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void queenFenSymbolWhite() {
		assertEquals("Q", new Queen(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void kingFenSymbolWhite() {
		assertEquals("K", new King(Colour.WHITE).getFenSymbol());
	}

	@Test
	public void pawnFenSymbolBlack() {
		assertEquals("p", new Pawn(Colour.BLACK).getFenSymbol());
	}

	@Test
	public void rookFenSymbolBlack() {
		assertEquals("r", new Rook(Colour.BLACK).getFenSymbol());
	}

	@Test
	public void knightFenSymbolBlack() {
		assertEquals("n", new Knight(Colour.BLACK).getFenSymbol());
	}

	@Test
	public void bishopFenSymbolBlack() {
		assertEquals("b", new Bishop(Colour.BLACK).getFenSymbol());
	}

	@Test
	public void queenFenSymbolBlack() {
		assertEquals("q", new Queen(Colour.BLACK).getFenSymbol());
	}

	@Test
	public void kingFenSymbolBlack() {
		assertEquals("k", new King(Colour.BLACK).getFenSymbol());
	}
}
