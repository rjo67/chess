package org.rjo.chess.base.ray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;

public class RayTest {

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
		BitSetUnifier emptySquares = BitSetFactory.createBitSet(new long[] { -1 });
		BitSetUnifier myPieces = BitSetFactory.createBitSet(new long[] { 0 });
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
