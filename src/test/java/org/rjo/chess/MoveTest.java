package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.chess.pieces.PieceType;

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

	@Test
	public void fromUCIStringNormal() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = Move.fromUCIString("e8e6", g);
		assertEquals("Re8-e6", m.toString());
	}

	@Test
	public void fromUCIStringCapture() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = Move.fromUCIString("e8e5", g);
		assertEquals("Re8xe5", m.toString());
	}

	@Test
	public void fromUCIStringPromotionBlack() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = Move.fromUCIString("c2c1q", g);
		assertEquals("c2-c1=Q", m.toString());
	}

	@Test
	public void fromUCIStringPromotionCaptureBlack() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/3N4 b - - 67 34");
		Move m = Move.fromUCIString("c2d1q", g);
		assertEquals("c2xd1=Q", m.toString());
	}

	@Test
	public void fromUCIStringPromotionWhite() {
		Game g = Fen.decode("4r1k1/3P4/8/8/6PK/r7/8/8 w - - 67 34");
		Move m = Move.fromUCIString("d7d8n", g);
		assertEquals("d7-d8=N", m.toString());
		g.makeMove(m);
		assertEquals("3Nr1k1/8/8/8/6PK/r7/8/8 b - - 68 34", Fen.encode(g));
	}

	@Test
	public void fromUCIStringPromotionCaptureWhite() {
		Game g = Fen.decode("4r1k1/3P4/8/8/6PK/r7/8/8 w - - 67 34");
		Move m = Move.fromUCIString("d7e8r", g);
		assertEquals("d7xe8=R", m.toString());
		g.makeMove(m);
		assertEquals("4R1k1/8/8/8/6PK/r7/8/8 b - - 68 34", Fen.encode(g));
	}
}
