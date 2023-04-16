package org.rjo.newchess.board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BoardTest {

   @Test
	public void optimizedArraysContainCorrectValues() {
		int expectedValue;
		for (int i = 0; i < 64; i++) {
			expectedValue = (i < 16 || i > 55) ? -1 : Board.getMailboxSquare(i, -10);
			assertEquals(expectedValue, Board.getMailboxSquareForWhitePawnsOneSquareForward(i), "bad value for i=" + i);
		}
		for (int i = 0; i < 64; i++) {
			expectedValue = (i < 8 || i > 47) ? -1 : Board.getMailboxSquare(i, 10);
			assertEquals(expectedValue, Board.getMailboxSquareForBlackPawnsOneSquareForward(i), "bad value for i=" + i);
		}
   }



}
