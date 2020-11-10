package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ParameterizedSquareToBitsetPosnTest {

	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { "A1", 0 }, { "b1", 1 }, { "c1", 2 }, { "d1", 3 }, { "e1", 4 },
				{ "f1", 5 }, { "g1", 6 }, { "H1", 7 }, { "A2", 8 }, { "b3", 17 }, { "c4", 26 }, { "d5", 35 },
				{ "e6", 44 }, { "f7", 53 }, { "g8", 62 }, { "h8", 63 }, });
	}

	@ParameterizedTest
	@MethodSource("data")
	public void coordToBitPosn(String coord, int bitsetPosn) {
		assertEquals(Square.fromString(coord).bitIndex(), bitsetPosn, "bad mapping for coord " + coord);
	}

}
