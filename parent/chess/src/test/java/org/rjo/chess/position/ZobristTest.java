package org.rjo.chess.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.Zobrist;

public class ZobristTest {

	@Test
	public void multipleCallsSameHash() {
		Zobrist zob = new Zobrist(10);
		Position posn = Position.startPosition();
		assertEquals(zob.hash(posn), zob.hash(posn));
	}

	@Test
	public void sameSeedGeneratesSameHash() {
		Zobrist zob1 = new Zobrist(10);
		Zobrist zob2 = new Zobrist(10);

		Position posn = Position.startPosition();
		assertEquals(zob1.hash(posn), zob2.hash(posn));
	}

	@Test
	public void differentSeedGeneratesDifferentHash() {
		Zobrist zob1 = new Zobrist(3);
		Zobrist zob2 = new Zobrist(4);

		Position posn = Position.startPosition();
		assertNotEquals(zob1.hash(posn), zob2.hash(posn));
	}

	@Test
	public void incrementalSimpleMove() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a3));// avoid enpassant
	}

	@Test
	public void incrementalSimpleMoveBlack() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.BLACK, Square.a7, Square.a6));// avoid enpassant
	}

	@Test
	public void incrementalRookMoveBlack() {
		// a rook move can affect castling rights
		checkMove(Fen.decode("rnbqkbnr/1ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
				new Move(PieceType.ROOK, Colour.BLACK, Square.a8, Square.a5));
	}

	@Test
	public void incrementalRookMoveWhite() {
		// a rook move can affect castling rights
		checkMove(Fen.decode("rnbqkbnr/1ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
				new Move(PieceType.ROOK, Colour.BLACK, Square.a8, Square.a5));
	}

	@Test
	public void incrementalKingMove() {
		// a king move can affect castling rights
		checkMove(Fen.decode("rnbqk1nr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
				new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.f8));
	}

	@Test
	public void incrementalCaptureAffectingOpponentsCastlingRights() {
		Move move = new Move(PieceType.QUEEN, Colour.WHITE, Square.c6, Square.a8, PieceType.ROOK);
		checkMove(Fen.decode("rnbqkbnr/8/2Q5/8/8/8/P1P1P1P1/RNB1KBNR w Kkq - 0 0").getPosition(), move);

	}

	@Test
	public void incrementalPromotionWhite() {
		// avoid capturing on a8, since this affects castling rights
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.b7, Square.b8);
		move.setPromotionPiece(PieceType.QUEEN);
		checkMove(Fen.decode("r1bqkbnr/pPpppppp/8/8/8/8/1PPPPPPP/RNBQKBNR w KQkq - 0 0").getPosition(), move);
	}

	@Test
	public void incrementalPromotionCaptureWhite() {
		// avoid capturing on a1, since this affects castling rights
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.b7, Square.c8, PieceType.BISHOP);
		move.setPromotionPiece(PieceType.QUEEN);
		checkMove(Fen.decode("rnbqkbnr/pPpppppp/8/8/8/8/1PPPPPPP/RNBQKBNR w KQkq - 0 0").getPosition(), move);
	}

	@Test
	public void incrementalPromotionBlack() {
		// avoid capturing on a1, since this affects castling rights
		Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.c2, Square.c1);
		move.setPromotionPiece(PieceType.BISHOP);
		checkMove(Fen.decode("rnbqkbnr/p1pppppp/8/8/8/8/PPpPPPPP/RN1QKBNR b KQkq - 0 0").getPosition(), move);
	}

	@Test
	public void incrementalPromotionCaptureBlack() {
		// avoid capturing on a1, since this affects castling rights
		Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.c2, Square.d1, PieceType.QUEEN);
		move.setPromotionPiece(PieceType.BISHOP);
		checkMove(Fen.decode("rnbqkbnr/p1pppppp/8/8/8/8/PPpPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(), move);
	}

	@Test
	public void incrementalPawnCaptureEnpassant() {
		Move move = Move.enpassant(Colour.BLACK, Square.c4, Square.d3);
		checkMove(Fen.decode("rnbqkbnr/p1pppppp/8/8/2pP4/8/PP2PPPP/RNBQKBNR b KQkq d5 0 0").getPosition(), move);
	}

	@Test
	public void incrementalCapture() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/P7/8/8/8/1PPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a6, Square.b7, PieceType.PAWN));
	}

	@Test
	public void incrementalSettingEnpassantSquare() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4));// possible enpassant
	}

	@Test
	public void incrementalWithEnpassantSquareSet() {
		// enpassant square is set before this (non e.p.) move
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b - d3 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.BLACK, Square.d7, Square.d5));// possible enpassant
	}

	@Test
	public void incrementalCastleShort() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQK2R w KQ - 0 0").getPosition(), Move.castleKingsSide(Colour.WHITE));
	}

	@Test
	public void incrementalCastleLong() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQ - 0 0").getPosition(), Move.castleQueensSide(Colour.WHITE));
	}

	private void checkMove(Position posn1,
			Move move) {
		Zobrist zob = new Zobrist(10);
		Position posn2 = posn1.move(move);
		assertEquals(zob.update(zob.hash(posn1), move, posn1.getCastlingRights(), posn1.getEnpassantSquare()), zob.hash(posn2));
	}
}
