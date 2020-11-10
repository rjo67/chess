package org.rjo.chess;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MoveDistanceTest {

	public static Iterable<Object[]> data() {
		return Arrays.asList(
				new Object[][] { { Square.a1, Square.a1, 0 }, { Square.a1, Square.b1, 1 }, { Square.a1, Square.b2, 1 },
						{ Square.a1, Square.b3, 2 }, { Square.a1, Square.b4, 3 }, { Square.b2, Square.a7, 5 },
						{ Square.c1, Square.a8, 7 }, { Square.a1, Square.h8, 7 }, { Square.a1, Square.h8, 7 } });
	}

	@ParameterizedTest
	@MethodSource("data")
	public void distance(Square start, Square finish, int distance) {
		assertEquals(distance, MoveDistance.calculateDistance(start, finish),
				"bad distance for squares " + start + ", " + finish);
	}

}
