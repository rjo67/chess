package org.rjo.chess;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MoveDistanceTest {

	@Parameter(0)
	public Square start;

	@Parameter(1)
	public Square finish;

	@Parameter(2)
	public int distance;

	@Parameters
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { Square.a1, Square.a1, 0 }, { Square.a1, Square.b1, 1 }, { Square.a1, Square.b2, 1 },
				{ Square.a1, Square.b3, 2 }, { Square.a1, Square.b4, 3 }, { Square.b2, Square.a7, 5 }, { Square.c1, Square.a8, 7 },
				{ Square.a1, Square.h8, 7 }, { Square.a1, Square.h8, 7 } });
	}

	@Test
	public void distance() {
		assertEquals("bad distance for squares " + start + ", " + finish, distance, MoveDistance.calculateDistance(start, finish));
	}

}
