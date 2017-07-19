package org.rjo.chess;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.util.Stopwatch;

/**
 * Test Position.
 *
 * @author rich
 */
public class PositionTest {

	@Test
	public void posnSpeedTest() throws InterruptedException {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();

		Stopwatch sw = new Stopwatch();
		sw.start();
		int nbrIter = 10000000;
		for (int i = 0; i < nbrIter; i++) {
			@SuppressWarnings("unused")
			Position p2 = new Position(p);
		}
		System.out.println(nbrIter + " new Positions in " + sw.read() + "ms");
	}

	@Test
	public void testToString() {
		Game game = Fen.decode("r3kr1Q/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ-q - 0 1");
		StringBuilder expected = new StringBuilder(80);

		// toString() always displays board from white POV

		expected.append("r...kr.Q   Black to move").append("\n");
		expected.append("p.ppqpb.   castlingRights: [QUEENS_SIDE, KINGS_SIDE], [QUEENS_SIDE]").append("\n");
		expected.append("bn..pnp.   enpassant square: null").append("\n");
		expected.append("...PN...").append("\n");
		expected.append(".p..P...").append("\n");
		expected.append("..N.....").append("\n");
		expected.append("PPPBBPPP").append("\n");
		expected.append("R...K..R").append("\n");

		assertEquals(expected.toString(), game.getPosition().toString());
	}

	@Test
	public void checkImmutable() {
		Position p = Position.startPosition();
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4);
		Position p2 = p.move(move);

		// System.out.println(p);
		// System.out.println(p2);
		assertFalse("positions are the same -- objects not immutable!?", p.toString().equals(p2.toString()));
	}

	@Test
	public void updateStructuresNoncaptureMove() {
		Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10");
		Move move = new Move(PieceType.KNIGHT, Colour.WHITE, Square.d2, Square.b3);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			game.getPosition().updateStructures(move);
		}
		System.out.println("1E06 noncapture move: " + (System.currentTimeMillis() - start));
	}

	@Test
	public void updateStructuresCaptureMove() {
		Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10");
		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.f4, Square.h6, PieceType.PAWN);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			game.getPosition().updateStructures(move);
		}
		System.out.println("1E06 capture move:" + (System.currentTimeMillis() - start));
	}

	@Test
	public void testMove() {
		Position posn = Position.startPosition();
		Position newPosn = posn.move(new Move(PieceType.PAWN, Colour.WHITE, Square.b2, Square.b4));

		assertEmptySquare(newPosn, Square.b2);
		assertPieceAt(newPosn, Square.b4, PieceType.PAWN, Colour.WHITE);
	}

	@Test
	public void blockCheck() {
		Game game = Fen.decode("3r4/4k3/8/R7/4P3/3K4/1BN1P3/8 w - - 10 10");
		game.getPosition().setInCheck(true);
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		assertEquals("found moves: " + moves, 6, moves.size());
	}

	@Test
	@Ignore
	public void pinnedPiece() {
		Game game = Fen.decode("3r4/4k3/8/8/3RP3/3K4/8/8 w - - 10 10");
		long start = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			@SuppressWarnings("unused")
			List<Move> moves = game.getPosition().findMoves(Colour.WHITE);// 1344
		}
		System.out.println(System.currentTimeMillis() - start);
	}

	@Test
	public void posn2ply2() {
		Game game = Fen.decode("r3kr1Q/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N5/PPPBBPPP/R3K2R b KQ-q - 0 1");
		Map<String, Integer> moveMap = Perft.findMoves(game.getPosition(), Colour.BLACK, 1);
		int moves = Perft.countMoves(moveMap);
		assertEquals("found moves" + moveMap, 35, moves);
	}

	@Test
	public void bishopCapture() {
		Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
		Position posn = game.getPosition();

		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.f4, Square.h6, PieceType.PAWN);

		InternalState prevState = new InternalState(posn);
		Position posnAfterMove = posn.move(move);
		InternalState newState = new InternalState(posnAfterMove);

		BitSet expectedBishopsAndQueens = new BitSet(64);
		expectedBishopsAndQueens.set(14);
		expectedBishopsAndQueens.set(47);

		BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.f4.bitIndex()));
		expectedEmptySquares.set(Square.f4.bitIndex());

		BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.f4.bitIndex()));
		expectedTotalPieces.clear(Square.f4.bitIndex());

		BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.f4.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.h6.bitIndex()));
		expectedAllPiecesWhite.clear(Square.f4.bitIndex());
		expectedAllPiecesWhite.set(Square.h6.bitIndex());

		BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
		assertTrue(expectedAllPiecesBlack.get(Square.h6.bitIndex()));
		expectedAllPiecesBlack.clear(Square.h6.bitIndex());

		assertEquals(expectedEmptySquares, newState.emptySquares);
		assertEquals(expectedTotalPieces, newState.totalPieces);
		assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
		assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

		// checks that the initial state hasn't changed
		InternalState newState2 = new InternalState(posn);
		assertEquals(prevState, newState2);
	}

	@Test
	public void pawnCapture() {
		Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
		Position posn = game.getPosition();

		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.d5, PieceType.PAWN);

		InternalState prevState = new InternalState(posn);
		Position posnAfterMove = posn.move(move);
		InternalState newState = new InternalState(posnAfterMove);

		BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.e4.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.d5.bitIndex()));
		expectedEmptySquares.set(Square.e4.bitIndex());

		BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.e4.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.d5.bitIndex()));
		expectedTotalPieces.clear(Square.e4.bitIndex());

		BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.e4.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.d5.bitIndex()));
		expectedAllPiecesWhite.clear(Square.e4.bitIndex());
		expectedAllPiecesWhite.set(Square.d5.bitIndex());

		BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
		assertTrue(expectedAllPiecesBlack.get(Square.d5.bitIndex()));
		expectedAllPiecesBlack.clear(Square.d5.bitIndex());

		assertEquals(expectedEmptySquares, newState.emptySquares);
		assertEquals(expectedTotalPieces, newState.totalPieces);
		assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
		assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

		// checks that the initial state hasn't changed
		InternalState newState2 = new InternalState(posn);
		assertEquals(prevState, newState2);
	}

	@Test
	public void pawnPromotionToQueen() {
		Game game = Fen.decode("r3k2r/pP3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
		Position posn = game.getPosition();

		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.b7, Square.a8, PieceType.ROOK);
		move.setPromotionPiece(PieceType.QUEEN);

		InternalState prevState = new InternalState(posn);
		Position posnAfterMove = posn.move(move);
		InternalState newState = new InternalState(posnAfterMove);

		BitSet expectedRooksAndQueensWhite = new BitSet(64);
		expectedRooksAndQueensWhite.set(Square.a1.bitIndex());
		expectedRooksAndQueensWhite.set(Square.h1.bitIndex());
		expectedRooksAndQueensWhite.set(Square.a8.bitIndex());
		BitSet expectedRooksAndQueensBlack = new BitSet(64);
		expectedRooksAndQueensBlack.set(Square.h8.bitIndex());

		BitSet expectedBishopsAndQueensWhite = new BitSet(64);
		expectedBishopsAndQueensWhite.set(Square.g2.bitIndex());
		expectedBishopsAndQueensWhite.set(Square.f4.bitIndex());
		expectedBishopsAndQueensWhite.set(Square.a8.bitIndex());

		BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.b7.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.a8.bitIndex()));
		expectedEmptySquares.set(Square.b7.bitIndex());

		BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.b7.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.a8.bitIndex()));
		expectedTotalPieces.clear(Square.b7.bitIndex());

		BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.b7.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.a8.bitIndex()));
		expectedAllPiecesWhite.clear(Square.b7.bitIndex());
		expectedAllPiecesWhite.set(Square.a8.bitIndex());

		BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
		assertTrue(expectedAllPiecesBlack.get(Square.a8.bitIndex()));
		expectedAllPiecesBlack.clear(Square.a8.bitIndex());

		assertEquals(expectedEmptySquares, newState.emptySquares);
		assertEquals(expectedTotalPieces, newState.totalPieces);
		assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
		assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

		// checks that the initial state hasn't changed
		InternalState newState2 = new InternalState(posn);
		assertEquals(prevState, newState2);
	}

	@Test
	public void blackPawnCapturesEnpassant() {
		Game game = Fen.decode("r3k2r/pP3p2/5npp/n2p4/Pp1PPB2/6P1/3N1PBP/R3K2R b KQkq a3 0 10");
		Position posn = game.getPosition();

		Move move = Move.enpassant(Colour.BLACK, Square.b4, Square.a3);

		InternalState prevState = new InternalState(posn);
		Position posnAfterMove = posn.move(move);
		InternalState newState = new InternalState(posnAfterMove);

		BitSet expectedEmptySquares = (BitSet) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.b4.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.a4.bitIndex()));
		expectedEmptySquares.set(Square.b4.bitIndex());
		expectedEmptySquares.set(Square.a4.bitIndex());
		expectedEmptySquares.clear(Square.a3.bitIndex());

		BitSet expectedTotalPieces = (BitSet) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.b4.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.a4.bitIndex()));
		expectedTotalPieces.clear(Square.b4.bitIndex());
		expectedTotalPieces.clear(Square.a4.bitIndex());
		expectedTotalPieces.set(Square.a3.bitIndex());

		BitSet expectedAllPiecesWhite = (BitSet) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.a4.bitIndex()));
		expectedAllPiecesWhite.clear(Square.a4.bitIndex());

		BitSet expectedAllPiecesBlack = (BitSet) prevState.allPiecesBlack.clone();
		assertTrue(expectedAllPiecesBlack.get(Square.b4.bitIndex()));
		expectedAllPiecesBlack.clear(Square.b4.bitIndex());
		expectedAllPiecesBlack.set(Square.a3.bitIndex());

		assertEquals(expectedEmptySquares, newState.emptySquares);
		assertEquals(expectedTotalPieces, newState.totalPieces);
		assertEquals(expectedAllPiecesWhite, newState.allPiecesWhite);
		assertEquals(expectedAllPiecesBlack, newState.allPiecesBlack);

		// checks that the initial state hasn't changed
		InternalState newState2 = new InternalState(posn);
		assertEquals(prevState, newState2);
	}

	@Test
	public void kingsCastlingWhite() {
		Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KkQq - 0 1");
		Position newPosn = game.getPosition().move(Move.castleKingsSide(Colour.WHITE));
		assertEmptySquare(newPosn, Square.e1);
		assertPieceAt(newPosn, Square.g1, PieceType.KING, Colour.WHITE);
		assertEmptySquare(newPosn, Square.h1);
		assertPieceAt(newPosn, Square.f1, PieceType.ROOK, Colour.WHITE);
		assertFalse(newPosn.canCastle(Colour.WHITE, CastlingRights.KINGS_SIDE));
	}

	@Test
	public void kingsCastlingBlack() {
		Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KkQq - 0 1");
		Position newPosn = game.getPosition().move(Move.castleKingsSide(Colour.BLACK));
		assertEmptySquare(newPosn, Square.e8);
		assertPieceAt(newPosn, Square.g8, PieceType.KING, Colour.BLACK);
		assertEmptySquare(newPosn, Square.h8);
		assertPieceAt(newPosn, Square.f8, PieceType.ROOK, Colour.BLACK);
		assertFalse(newPosn.canCastle(Colour.BLACK, CastlingRights.KINGS_SIDE));
	}

	@Test
	public void queensCastlingWhite() {
		Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KkQq - 0 1");
		Position newPosn = game.getPosition().move(Move.castleQueensSide(Colour.WHITE));
		assertEmptySquare(newPosn, Square.e1);
		assertPieceAt(newPosn, Square.c1, PieceType.KING, Colour.WHITE);
		assertEmptySquare(newPosn, Square.a1);
		assertPieceAt(newPosn, Square.d1, PieceType.ROOK, Colour.WHITE);
		assertFalse(newPosn.canCastle(Colour.WHITE, CastlingRights.QUEENS_SIDE));
	}

	@Test
	public void queensCastlingBlack() {
		Game game = Fen.decode("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b KkQq - 0 1");
		Position newPosn = game.getPosition().move(Move.castleQueensSide(Colour.BLACK));
		assertEmptySquare(newPosn, Square.e8);
		assertPieceAt(newPosn, Square.c8, PieceType.KING, Colour.BLACK);
		assertEmptySquare(newPosn, Square.a8);
		assertPieceAt(newPosn, Square.d8, PieceType.ROOK, Colour.BLACK);
		assertFalse(newPosn.canCastle(Colour.BLACK, CastlingRights.QUEENS_SIDE));
	}

	@Test
	public void capture() {
		Game game = Fen.decode("k1K5/8/8/3p4/2P5/8/8/8 w - - 0 1");
		Position newPosn = game.getPosition().move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5, PieceType.PAWN));
		assertEmptySquare(newPosn, Square.c4);
		assertPieceAt(newPosn, Square.d5, PieceType.PAWN, Colour.WHITE);
		assertTrue(newPosn.getPieces(Colour.BLACK)[PieceType.PAWN.ordinal()].getBitBoard().getBitSet().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void noPieceToCapture() {
		Game game = Fen.decode("k1K5/8/8/3p4/2P5/8/8/8 w - - 0 1");
		game.getPosition().move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.b5, PieceType.PAWN));
	}

	@Test
	public void promotion() {
		Game game = Fen.decode("k1K5/8/8/8/8/8/5p2/8 b - - 0 1");
		Move move = new Move(PieceType.PAWN, Colour.BLACK, Square.f2, Square.f1);
		move.setPromotionPiece(PieceType.QUEEN);
		Position newPosn = game.getPosition().move(move);
		assertEmptySquare(newPosn, Square.f2);
		assertPieceAt(newPosn, Square.f1, PieceType.QUEEN, Colour.BLACK);
	}

	@Test
	public void castlingNotAllowedAfterRookCapture() {
		// this is 'posn2' from PerftTest
		// sequence of moves: Ne5xg6, b4-b3, Ng6xh8. O-O is then not allowed...
		Game game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 4");
		Position newPosn = game.getPosition().move(new Move(PieceType.KNIGHT, Colour.WHITE, Square.e5, Square.g6, PieceType.PAWN));
		newPosn = newPosn.move(new Move(PieceType.PAWN, Colour.BLACK, Square.b4, Square.b3));
		newPosn = newPosn.move(new Move(PieceType.KNIGHT, Colour.WHITE, Square.g6, Square.h8, PieceType.ROOK));
		List<Move> moves = newPosn.findMoves(Colour.BLACK);
		assertMovePresent(moves, "O-O-O");
		assertMoveNotPresent(moves, "O-O");
	}

	@Test
	public void eval() {
		Game game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KkQq - 0 1");
		System.out.println(game.getPosition().evaluate());
		game = Fen.decode("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R b KkQq - 0 1");
		System.out.println(game.getPosition().evaluate());
	}

	@Test
	public void castlingRightsTest() {
		// increase coverage of enum
		assertArrayEquals(new CastlingRights[] { CastlingRights.QUEENS_SIDE, CastlingRights.KINGS_SIDE }, CastlingRights.values());
		CastlingRights.valueOf("QUEENS_SIDE");
	}

	@Test
	public void testCheck() {
		Game game = Fen.decode("r3k2r/5p2/8/8/1Q6/BBBB4/8/R3K2R w KkQq - 0 1");
		System.setProperty("CHECK-DEBUG", "true");
		Map<String, Integer> moveMap = Perft.findMoves(game.getPosition(), Colour.WHITE, 1);
		System.out.println(moveMap);
	}

	private void assertPieceAt(
			Position cb,
			Square sq,
			PieceType expectedPiece,
			Colour expectedColour) {
		assertEquals(expectedPiece, cb.pieceAt(sq, expectedColour));
	}

	private void assertMoveNotPresent(
			List<Move> moves,
			String requiredMove) {
		for (Move move : moves) {
			if (requiredMove.equals(move.toString())) {
				throw new AssertionError("move '" + requiredMove + "' was found in " + moves);
			}
		}
	}

	private void assertMovePresent(
			List<Move> moves,
			String requiredMove) {
		boolean found = false;
		for (Move move : moves) {
			if (requiredMove.equals(move.toString())) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new AssertionError("move '" + requiredMove + "' not found in " + moves);
		}
	}

	private void assertEmptySquare(
			Position cb,
			Square sq) {
		try {
			cb.pieceAt(sq, null);
			fail("expected exception");
		} catch (IllegalArgumentException x) {
			// ok
		}
	}

	class InternalState {
		BitSet emptySquares;
		BitSet allPiecesWhite;
		BitSet allPiecesBlack;
		BitSet totalPieces;

		InternalState(Position posn) {
			emptySquares = posn.getTotalPieces().flip();
			allPiecesWhite = posn.getAllPieces(Colour.WHITE).cloneBitSet();
			allPiecesBlack = posn.getAllPieces(Colour.BLACK).cloneBitSet();
			totalPieces = posn.getTotalPieces().cloneBitSet();
		}

		@Override
		public boolean equals(
				Object obj) {
			if (!(obj instanceof InternalState)) {
				return false;
			}
			InternalState other = (InternalState) obj;
			if (!this.emptySquares.equals(other.emptySquares)) {
				return false;
			}
			if (!this.allPiecesWhite.equals(other.allPiecesWhite)) {
				return false;
			}
			if (!this.allPiecesBlack.equals(other.allPiecesBlack)) {
				return false;
			}
			if (!this.totalPieces.equals(other.totalPieces)) {
				return false;
			}
			return true;
		}
	}
}
