package org.rjo.chess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;

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
	public void incrementalUpdateSimpleMove() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a3));// avoid enpassant
	}

	@Test
	public void incrementalUpdateRookMove() {
		// a rook move can affect castling rights
		//TODO test is red, doesn't work for a black move

		//		checkMove(Fen.decode("rnbqkbnr/1ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
		checkMove(Fen.decode("r3k3/8/8/8/8/8/8/4K3 b q - 0 0").getPosition(), new Move(PieceType.ROOK, Colour.BLACK, Square.a8, Square.a5));
	}

	@Test
	public void incrementalUpdateRookMoveWhite() {
		// a rook move can affect castling rights
		//		checkMove(Fen.decode("rnbqkbnr/1ppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
		checkMove(Fen.decode("4k3/8/8/8/8/8/8/R3K2R w KQ - 0 0").getPosition(), new Move(PieceType.ROOK, Colour.WHITE, Square.a1, Square.a5));
	}

	@Test
	public void incrementalUpdateKingMove() {
		// a king move can affect castling rights
		//TODO test is red, doesn't work for a black move
		checkMove(Fen.decode("rnbqk1nr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 0").getPosition(),
				new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.f8));
	}

	@Test
	public void incrementalUpdateCapture() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/P7/8/8/8/1PPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a6, Square.b7, PieceType.PAWN));
	}

	@Test
	public void incrementalUpdateEnpassant() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 0").getPosition(),
				new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4));// possible enpassant
	}

	@Test
	public void incrementalUpdateCastleShort() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQK2R w KQ - 0 0").getPosition(), Move.castleKingsSide(Colour.WHITE));
	}

	@Test
	public void incrementalUpdateCastleLong() {
		checkMove(Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQ - 0 0").getPosition(), Move.castleQueensSide(Colour.WHITE));
	}

	private void checkMove(
			Position posn1,
			Move move) {
		Zobrist zob = new Zobrist(10);
		Position posn2 = posn1.move(move);
		System.out.println("posn1, toMove: " + posn1.getSideToMove() + " white castling rights: " + posn1.getCastlingRights()[0]
				+ ", black castling rights: " + posn1.getCastlingRights()[1]);
		System.out.println("posn2, toMove: " + posn2.getSideToMove() + " white castling rights: " + posn2.getCastlingRights()[0]
				+ ", black castling rights: " + posn2.getCastlingRights()[1]);
		assertEquals(zob.update(zob.hash(posn1), move, posn1.getCastlingRights()), zob.hash(posn2));
	}
}
