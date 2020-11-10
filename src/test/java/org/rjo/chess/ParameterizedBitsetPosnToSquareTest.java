package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ParameterizedBitsetPosnToSquareTest {

	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0, "a1" }, { 1, "b1" }, { 2, "c1" }, { 3, "d1" }, { 4, "e1" },
				{ 5, "f1" }, { 6, "g1" }, { 7, "h1" }, { 8, "a2" }, { 17, "b3" }, { 26, "c4" }, { 35, "d5" },
				{ 44, "e6" }, { 53, "f7" }, { 62, "g8" }, { 63, "h8" }, });
	}

	@ParameterizedTest
	@MethodSource("data")
	public void bitposnToCoord(int bitsetPosn, String coord) {
		assertEquals(Square.fromBitIndex(bitsetPosn).toString(), coord, "bad mapping for bitposn " + bitsetPosn);
	}

}
