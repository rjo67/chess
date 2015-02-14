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
public class ParameterizedBitsetPosnToSquareTest {

   @Parameter(0)
   public int bitsetPosn;

   @Parameter(1)
   public String coord;

   @Parameters
   public static Iterable<Object[]> data() {
      return Arrays.asList(new Object[][] { { 0, "a1" }, { 1, "b1" }, { 2, "c1" }, { 3, "d1" }, { 4, "e1" },
            { 5, "f1" }, { 6, "g1" }, { 7, "h1" }, { 8, "a2" }, { 17, "b3" }, { 26, "c4" }, { 35, "d5" }, { 44, "e6" },
            { 53, "f7" }, { 62, "g8" }, { 63, "h8" }, });
   }

   @Test
   public void bitposnToCoord() {
      assertThat("bad mapping for bitposn " + bitsetPosn, Square.fromBitPosn(bitsetPosn).toString(), is(equalTo(coord)));
   }

}
