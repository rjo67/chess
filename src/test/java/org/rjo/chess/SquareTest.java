package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SquareTest {

	@Test
	public void enumCoverage() {
		// increase coverage of enum
		Square.values();
		Square.valueOf("a2");
	}

	@Test
	public void mappingToBitPosn() {
		assertEquals(0, Square.a1.bitIndex());
	}

	@Test
	public void mappingFromBitPosn() {
		assertEquals(Square.a8, Square.fromBitIndex(56));
	}

	@Test
	public void nameOfSquare() {
		assertEquals("h1", Square.h1.toString());
	}

	/**
	 * make sure the bit positions have been entered correctlry
	 */
	@Test
	public void bitPosns() {
		for (int i = 0; i < 64; i++) {
			assertEquals(i, Square.fromBitIndex(i).bitIndex());
		}
	}

	@Test
	public void badInput() {
		assertThrows(IllegalArgumentException.class, () -> Square.fromString("A92"));
	}

	@Test
	public void badRank() {
		assertThrows(IllegalArgumentException.class, () -> Square.fromString("A9"));
	}

	@Test
	public void badRank2() {
		assertThrows(IllegalArgumentException.class, () -> Square.fromString("A0"));
	}

	@Test
	public void badFile() {
		assertThrows(IllegalArgumentException.class, () -> Square.fromString("I6"));
	}

	@Test
	public void badFile2() {
		assertThrows(IllegalArgumentException.class, () -> Square.fromString("06"));
	}
}
