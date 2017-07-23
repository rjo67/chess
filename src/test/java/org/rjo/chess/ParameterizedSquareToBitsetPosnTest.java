package org.rjo.chess;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ParameterizedSquareToBitsetPosnTest {

	@Parameter(0)
	public String coord;

	@Parameter(1)
	public int bitsetPosn;

	@Parameters
	public static Iterable<Object[]> data() {
		return Arrays.asList(new Object[][] { { "A1", 0 }, { "b1", 1 }, { "c1", 2 }, { "d1", 3 }, { "e1", 4 }, { "f1", 5 }, { "g1", 6 },
				{ "H1", 7 }, { "A2", 8 }, { "b3", 17 }, { "c4", 26 }, { "d5", 35 }, { "e6", 44 }, { "f7", 53 }, { "g8", 62 }, { "h8", 63 }, });
	}

	@Test
	public void coordToBitPosn() {
		assertThat("bad mapping for coord " + coord, Square.fromString(coord).bitIndex(), is(equalTo(bitsetPosn)));
	}

}
