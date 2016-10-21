package org.rjo.chess.ray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Random;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.Square;

public class RayTest {

	@Test
	public void emptySquares() {
		Game game = Fen.decode("r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w - - 0 0");
		int[] expectedEmptySquares = new int[] { 9, 5, 11, 10, 12, 11, 13, 4, 10, 14, 11, 12, 11, 19, 15, 17, 5, 7, 10,
				18, 14, 15, 17, 13, 7, 15, 15, 14, 16, 19, 18, 11, 13, 13, 17, 15, 18, 19, 22, 15, 10, 17, 15, 19, 17, 23,
				17, 17, 14, 12, 15, 17, 14, 17, 19, 13, 6, 10, 12, 12, 15, 13, 11, 8 };
		long start = System.currentTimeMillis();
		for (int i = 0; i < expectedEmptySquares.length; i++) {
			int nbrEmptySquares = 0;
			for (RayType rayType : RayType.values()) {
				RayInfo info = RayUtils.findFirstPieceOnRay(Colour.BLACK, game.getPosition().getTotalPieces().flip(),
						game.getPosition().getAllPieces(Colour.WHITE).getBitSet(), RayFactory.getRay(rayType), i);
				// System.out.println(ray + " " + info);
				nbrEmptySquares += info.getEmptySquares().size();
			}
			assertEquals("bad sq " + Square.fromBitIndex(i), expectedEmptySquares[i], nbrEmptySquares);
		}
		System.out.println("emptySquares: " + (System.currentTimeMillis() - start));

	}

	@Test
	public void discoveredCheck() {
		Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
		// now move: Be4-f5 (discovered check)
		// need to manipulate chessboard to remove the bishop at e4
		BitSet emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.e4.bitIndex());
		BitSet myPieces = game.getPosition().getAllPieces(Colour.BLACK).getBitSet();
		myPieces.clear(Square.e4.bitIndex());
		assertTrue(
				RayUtils.discoveredCheck(Colour.BLACK, game.getPosition(), emptySquares, myPieces, Square.e1, Square.e4));
	}

	@Test
	public void discoveredCheck2() {
		Game game = Fen.decode("r3k2r/p3r2p/8/1P6/B1p1b3/8/P6P/R3K2R w - - 0 0");
		// now move: b5-b6 (discovered check)
		// need to manipulate chessboard to remove the pawn at b5
		BitSet emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.b5.bitIndex());
		BitSet myPieces = game.getPosition().getAllPieces(Colour.WHITE).getBitSet();
		myPieces.clear(Square.b5.bitIndex());
		assertTrue(
				RayUtils.discoveredCheck(Colour.WHITE, game.getPosition(), emptySquares, myPieces, Square.e8, Square.b5));
	}

	@Test
	public void noDiscoveredCheck() {
		Game game = Fen.decode("r3k2r/p3n2p/8/1P6/B1p1b3/8/P6P/R3K2R b - - 0 0");
		// now move: Be4-f5 (not discovered check)
		// need to manipulate chessboard to remove the bishop at e4
		BitSet emptySquares = game.getPosition().getTotalPieces().flip();
		emptySquares.set(Square.e4.bitIndex());
		BitSet myPieces = game.getPosition().getAllPieces(Colour.BLACK).getBitSet();
		myPieces.clear(Square.e4.bitIndex());
		assertFalse(
				RayUtils.discoveredCheck(Colour.BLACK, game.getPosition(), emptySquares, myPieces, Square.e1, Square.e4));
	}

	@Test
	public void rayFromA3ToC1() {
		Ray ray = RayUtils.getRay(Square.a3, Square.c1);
		assertNotNull(ray);
		assertEquals(RayType.SOUTHEAST, ray.getRayType());
	}

	@Test
	public void speedOfGetRay() {
		long start = System.currentTimeMillis();
		int cnt = 0;
		Random rnd = new Random();
		for (int i = 0; i < 1000000; i++) {
			Ray ray = RayUtils.getRay(Square.fromBitIndex(rnd.nextInt(64)), Square.fromBitIndex(rnd.nextInt(64)));
			if (ray != null) {
				cnt++;
			}
		}
		System.out.println("getRay: " + cnt + ", " + (System.currentTimeMillis() - start));
	}

	@Test
	public void speed() {
		BitSet emptySquares = BitSet.valueOf(new long[] { -1 });
		BitSet myPieces = BitSet.valueOf(new long[] { 0 });
		Square startSquare = Square.a4;
		Square targetSquare = Square.d7;
		long start = System.currentTimeMillis();
		Ray ray = RayUtils.getRay(startSquare, targetSquare);
		for (int i = 0; i < 1000000; i++) {
			RayUtils.findFirstPieceOnRay(Colour.WHITE, emptySquares, myPieces, ray, startSquare.bitIndex());
		}
		System.out.println("speed: " + (System.currentTimeMillis() - start));
	}
}
