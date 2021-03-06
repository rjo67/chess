package org.rjo.chess.position;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rjo.chess.Perft;
import org.rjo.chess.base.CastlingRightsSummary.CastlingRights;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayInfo;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;

/**
 * Test Position.
 *
 * @author rich
 */
public class PositionTest {

	@Test
	public void posnSpeedTest() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();

		StopWatch sw = new StopWatch();
		sw.start();
		int nbrIter = 10000000;
		for (int i = 0; i < nbrIter; i++) {
			@SuppressWarnings("unused")
			Position p2 = new Position(p);
		}
		long duration = sw.getTime();
		System.out.println(nbrIter + " new Positions in " + duration + "ms "
				+ String.format("%9.7f", ((1.0 * duration) / nbrIter)) + "/posn");
	}

	@Test
	public void checkImmutable() {
		Position p = Position.startPosition();
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4);
		Position p2 = p.move(move);

		// System.out.println(p);
		// System.out.println(p2);
		assertNotEquals("positions are the same -- objects not immutable!?", p.toString(), p2.toString());
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
		List<Move> moves = game.getPosition().findMoves(Colour.WHITE);
		assertEquals(6, moves.size(), "found moves: " + moves);
	}

	@Test
	@Disabled
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
	public void bishopCapture() {
		Game game = Fen.decode("r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R w KQkq - 0 10");
		Position posn = game.getPosition();

		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.f4, Square.h6, PieceType.PAWN);

		InternalState prevState = new InternalState(posn);
		Position posnAfterMove = posn.move(move);
		InternalState newState = new InternalState(posnAfterMove);

		BitSetUnifier expectedBishopsAndQueens = BitSetFactory.createBitSet(64);
		expectedBishopsAndQueens.set(14);
		expectedBishopsAndQueens.set(47);

		BitSetUnifier expectedEmptySquares = (BitSetUnifier) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.f4.bitIndex()));
		expectedEmptySquares.set(Square.f4.bitIndex());

		BitSetUnifier expectedTotalPieces = (BitSetUnifier) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.f4.bitIndex()));
		expectedTotalPieces.clear(Square.f4.bitIndex());

		BitSetUnifier expectedAllPiecesWhite = (BitSetUnifier) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.f4.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.h6.bitIndex()));
		expectedAllPiecesWhite.clear(Square.f4.bitIndex());
		expectedAllPiecesWhite.set(Square.h6.bitIndex());

		BitSetUnifier expectedAllPiecesBlack = (BitSetUnifier) prevState.allPiecesBlack.clone();
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

		BitSetUnifier expectedEmptySquares = (BitSetUnifier) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.e4.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.d5.bitIndex()));
		expectedEmptySquares.set(Square.e4.bitIndex());

		BitSetUnifier expectedTotalPieces = (BitSetUnifier) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.e4.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.d5.bitIndex()));
		expectedTotalPieces.clear(Square.e4.bitIndex());

		BitSetUnifier expectedAllPiecesWhite = (BitSetUnifier) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.e4.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.d5.bitIndex()));
		expectedAllPiecesWhite.clear(Square.e4.bitIndex());
		expectedAllPiecesWhite.set(Square.d5.bitIndex());

		BitSetUnifier expectedAllPiecesBlack = (BitSetUnifier) prevState.allPiecesBlack.clone();
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

		BitSetUnifier expectedRooksAndQueensWhite = BitSetFactory.createBitSet(64);
		expectedRooksAndQueensWhite.set(Square.a1.bitIndex());
		expectedRooksAndQueensWhite.set(Square.h1.bitIndex());
		expectedRooksAndQueensWhite.set(Square.a8.bitIndex());
		BitSetUnifier expectedRooksAndQueensBlack = BitSetFactory.createBitSet(64);
		expectedRooksAndQueensBlack.set(Square.h8.bitIndex());

		BitSetUnifier expectedBishopsAndQueensWhite = BitSetFactory.createBitSet(64);
		expectedBishopsAndQueensWhite.set(Square.g2.bitIndex());
		expectedBishopsAndQueensWhite.set(Square.f4.bitIndex());
		expectedBishopsAndQueensWhite.set(Square.a8.bitIndex());

		BitSetUnifier expectedEmptySquares = (BitSetUnifier) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.b7.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.a8.bitIndex()));
		expectedEmptySquares.set(Square.b7.bitIndex());

		BitSetUnifier expectedTotalPieces = (BitSetUnifier) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.b7.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.a8.bitIndex()));
		expectedTotalPieces.clear(Square.b7.bitIndex());

		BitSetUnifier expectedAllPiecesWhite = (BitSetUnifier) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.b7.bitIndex()));
		assertFalse(expectedAllPiecesWhite.get(Square.a8.bitIndex()));
		expectedAllPiecesWhite.clear(Square.b7.bitIndex());
		expectedAllPiecesWhite.set(Square.a8.bitIndex());

		BitSetUnifier expectedAllPiecesBlack = (BitSetUnifier) prevState.allPiecesBlack.clone();
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

		BitSetUnifier expectedEmptySquares = (BitSetUnifier) prevState.emptySquares.clone();
		assertFalse(expectedEmptySquares.get(Square.b4.bitIndex()));
		assertFalse(expectedEmptySquares.get(Square.a4.bitIndex()));
		expectedEmptySquares.set(Square.b4.bitIndex());
		expectedEmptySquares.set(Square.a4.bitIndex());
		expectedEmptySquares.clear(Square.a3.bitIndex());

		BitSetUnifier expectedTotalPieces = (BitSetUnifier) prevState.totalPieces.clone();
		assertTrue(expectedTotalPieces.get(Square.b4.bitIndex()));
		assertTrue(expectedTotalPieces.get(Square.a4.bitIndex()));
		expectedTotalPieces.clear(Square.b4.bitIndex());
		expectedTotalPieces.clear(Square.a4.bitIndex());
		expectedTotalPieces.set(Square.a3.bitIndex());

		BitSetUnifier expectedAllPiecesWhite = (BitSetUnifier) prevState.allPiecesWhite.clone();
		assertTrue(expectedAllPiecesWhite.get(Square.a4.bitIndex()));
		expectedAllPiecesWhite.clear(Square.a4.bitIndex());

		BitSetUnifier expectedAllPiecesBlack = (BitSetUnifier) prevState.allPiecesBlack.clone();
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
		Position newPosn = game.getPosition()
				.move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.d5, PieceType.PAWN));
		assertEmptySquare(newPosn, Square.c4);
		assertPieceAt(newPosn, Square.d5, PieceType.PAWN, Colour.WHITE);
		assertTrue(newPosn.getPieces(Colour.BLACK)[PieceType.PAWN.ordinal()].getBitBoard().isEmpty());
	}

	@Test
	public void noPieceToCapture() {
		Game game = Fen.decode("k1K5/8/8/3p4/2P5/8/8/8 w - - 0 1");
		assertThrows(IllegalArgumentException.class, () -> game.getPosition()
				.move(new Move(PieceType.PAWN, Colour.WHITE, Square.c4, Square.b5, PieceType.PAWN)));
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
		Position newPosn = game.getPosition()
				.move(new Move(PieceType.KNIGHT, Colour.WHITE, Square.e5, Square.g6, PieceType.PAWN));
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
		assertArrayEquals(new CastlingRights[] { CastlingRights.QUEENS_SIDE, CastlingRights.KINGS_SIDE },
				CastlingRights.values());
		CastlingRights.valueOf("QUEENS_SIDE");
	}

	@Test
	public void testCheck() {
		Game game = Fen.decode("r3k2r/5p2/8/8/1Q6/BBBB4/8/R3K2R w KkQq - 0 1");
		System.setProperty("CHECK-DEBUG", "true");
		Map<String, Integer> moveMap = Perft.findMoves(game.getPosition(), Colour.WHITE, 1, 1);
		System.out.println(moveMap);
	}

	@Test
	public void emptySquares() {
		Game game = Fen.decode("r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w - - 0 0");
		int[] expectedEmptySquares = new int[] { 9, 5, 11, 10, 12, 11, 13, 4, 10, 14, 11, 12, 11, 19, 15, 17, 5, 7, 10,
				18, 14, 15, 17, 13, 7, 15, 15, 14, 16, 19, 18, 11, 13, 13, 17, 15, 18, 19, 22, 15, 10, 17, 15, 19, 17,
				23, 17, 17, 14, 12, 15, 17, 14, 17, 19, 13, 6, 10, 12, 12, 15, 13, 11, 8 };
		long start = System.currentTimeMillis();
		for (int i = 0; i < expectedEmptySquares.length; i++) {
			int nbrEmptySquares = 0;
			for (RayType rayType : RayType.values()) {
				RayInfo info = RayUtils.findFirstPieceOnRay(Colour.BLACK, game.getPosition().getTotalPieces().flip(),
						game.getPosition().getAllPieces(Colour.WHITE).getBitSet(), RayUtils.getRay(rayType), i);
				// System.out.println(ray + " " + info);
				nbrEmptySquares += info.getEmptySquares().size();
			}
			assertEquals(expectedEmptySquares[i], nbrEmptySquares, "bad sq " + Square.fromBitIndex(i));
		}
		System.out.println("emptySquares: " + (System.currentTimeMillis() - start));

	}

	@Test
	public void discoveredCheck() {
		Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
		// now move: Be4-f5 (discovered check)
		// need to manipulate chessboard to remove the bishop at e4
		BitSetUnifier emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.e4.bitIndex());
		BitSetUnifier myPieces = game.getPosition().getAllPieces(Colour.BLACK).getBitSet();
		myPieces.clear(Square.e4.bitIndex());
		assertTrue(Position.discoveredCheck(Colour.BLACK, game.getPosition(), emptySquares, myPieces, Square.e1,
				Square.e4));
	}

	@Test
	public void discoveredCheck2() {
		Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R w - - 0 0");
		// now move: b5-b6 (discovered check)
		// need to manipulate chessboard to remove the pawn at b5
		BitSetUnifier emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.b5.bitIndex());
		BitSetUnifier myPieces = game.getPosition().getAllPieces(Colour.WHITE).getBitSet();
		myPieces.clear(Square.b5.bitIndex());
		assertTrue(Position.discoveredCheck(Colour.WHITE, game.getPosition(), emptySquares, myPieces, Square.e8,
				Square.b5));
	}

	@Test
	public void noDiscoveredCheck() {
		Game game = Fen.decode("r3k2r/p3n2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
		// now move: Be4-f5 (not discovered check)
		// need to manipulate chessboard to remove the bishop at e4
		BitSetUnifier emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.e4.bitIndex());
		BitSetUnifier myPieces = game.getPosition().getAllPieces(Colour.BLACK).getBitSet();
		myPieces.clear(Square.e4.bitIndex());
		assertFalse(Position.discoveredCheck(Colour.BLACK, game.getPosition(), emptySquares, myPieces, Square.e1,
				Square.e4));
	}

	private void assertPieceAt(Position cb, Square sq, PieceType expectedPiece, Colour expectedColour) {
		assertEquals(expectedPiece, cb.pieceAt(sq, expectedColour));
	}

	private void assertMoveNotPresent(List<Move> moves, String requiredMove) {
		for (Move move : moves) {
			if (requiredMove.equals(move.toString())) {
				throw new AssertionError("move '" + requiredMove + "' was found in " + moves);
			}
		}
	}

	private void assertMovePresent(List<Move> moves, String requiredMove) {
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

	private void assertEmptySquare(Position cb, Square sq) {
		try {
			cb.pieceAt(sq, null);
			fail("expected exception");
		} catch (IllegalArgumentException x) {
			// ok
		}
	}

	class InternalState {
		BitSetUnifier emptySquares;
		BitSetUnifier allPiecesWhite;
		BitSetUnifier allPiecesBlack;
		BitSetUnifier totalPieces;

		InternalState(Position posn) {
			emptySquares = posn.getTotalPieces().flip();
			allPiecesWhite = posn.getAllPieces(Colour.WHITE).cloneBitSet();
			allPiecesBlack = posn.getAllPieces(Colour.BLACK).cloneBitSet();
			totalPieces = posn.getTotalPieces().cloneBitSet();
		}

		@Override
		public boolean equals(Object obj) {
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
			return this.totalPieces.equals(other.totalPieces);
		}
	}
}
