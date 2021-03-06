package org.rjo.chess.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class MoveTest {

	@Test
	public void pawnMove() {
		assertEquals(new Move(PieceType.PAWN, Colour.WHITE, Square.c5, Square.c6).toString(), "c5-c6");
	}

	@Test
	public void rookMove() {
		assertEquals(new Move(PieceType.ROOK, Colour.WHITE, Square.a1, Square.a5).toString(), "Ra1-a5");
	}

	@Test
	public void knightMove() {
		assertEquals(new Move(PieceType.KNIGHT, Colour.WHITE, Square.d4, Square.e6).toString(), "Nd4-e6");
	}

	@Test
	public void bishopMove() {
		assertEquals(new Move(PieceType.BISHOP, Colour.WHITE, Square.c5, Square.d6).toString(), "Bc5-d6");
	}

	@Test
	public void queenMove() {
		assertEquals(new Move(PieceType.QUEEN, Colour.WHITE, Square.a1, Square.a8).toString(), "Qa1-a8");
	}

	@Test
	public void kingMove() {
		assertEquals(new Move(PieceType.KING, Colour.WHITE, Square.c5, Square.c6).toString(), "Kc5-c6");
	}

	@Test
	public void illegalPromotion() {
		Move m = new Move(PieceType.ROOK, Colour.WHITE, Square.c7, Square.c8);
		assertThrows(IllegalArgumentException.class, () -> m.setPromotionPiece(PieceType.QUEEN));
	}

	@Test
	public void illegalPromotion2() {
		Move m = new Move(PieceType.PAWN, Colour.WHITE, Square.c7, Square.c8);
		assertThrows(IllegalArgumentException.class, () -> m.setPromotionPiece(PieceType.PAWN));
	}

	@Test
	public void illegalPromotion3() {
		Move m = new Move(PieceType.PAWN, Colour.WHITE, Square.c7, Square.c8);
		assertThrows(IllegalArgumentException.class, () -> m.setPromotionPiece(PieceType.KING));
	}

	@Test
	public void promotion() {
		Move m = new Move(PieceType.PAWN, Colour.WHITE, Square.c7, Square.c8);
		m.setPromotionPiece(PieceType.QUEEN);
		assertEquals(PieceType.QUEEN, m.getPromotedPiece());
		assertNull(new Move(PieceType.KING, Colour.WHITE, Square.c5, Square.c6).getPromotedPiece());
	}

	@Test
	public void badCapture() {
		Move m = new Move(PieceType.ROOK, Colour.WHITE, Square.c7, Square.c8);
		assertThrows(IllegalArgumentException.class, () -> m.getCapturedPiece());
	}

	@Test
	public void castleKingsSide() {
		assertTrue(Move.castleKingsSide(Colour.WHITE).isCastleKingsSide());
		assertFalse(new Move(PieceType.KING, Colour.WHITE, Square.c5, Square.c6).isCastleKingsSide());
	}

	@Test
	public void castleQueensSide() {
		assertTrue(Move.castleQueensSide(Colour.BLACK).isCastleQueensSide());
		assertFalse(new Move(PieceType.KING, Colour.WHITE, Square.c5, Square.c6).isCastleQueensSide());
	}

}
