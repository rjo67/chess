package org.rjo.chess.base.bits;

import org.junit.Test;
import org.rjo.chess.base.Square;

public class BitBoardTest {

	@Test
	public void test() {

		BitBoard bb = new BitBoard();
		bb.setBitsAt(Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2, Square.h2);

		// System.out.println(bb.display());

		long lo = bb.toLong();

		BitSetUnifier bs2 = BitSetFactory.createBitSet(new long[] { (lo << 9) }); // a2->b3 // need to blank file 8
		// System.out.println(BitBoard.display(bs2));

		bs2 = BitSetFactory.createBitSet(new long[] { (lo << 8) }); // a2->a3
		// System.out.println(BitBoard.display(bs2));

		BitSetUnifier bs = bb.cloneBitSet();
		bs.and(BitBoard.RANK[1]);
		lo = bs.toLongArray()[0];
		bs2 = BitSetFactory.createBitSet(new long[] { (lo << 16) }); // c2->c4; need to blank everything except rank 2 since only
		// these pawns can move 2
		// System.out.println(BitBoard.display(bs2));

		bs = bb.cloneBitSet();
		bs.and(BitBoard.EXCEPT_FILE[0]);
		lo = bs.toLongArray()[0];
		bs2 = BitSetFactory.createBitSet(new long[] { (lo << 7) }); // c2->b3; need to blank file 1
		// System.out.println(BitBoard.display(bs2));

		bs = bb.cloneBitSet();
		bs.and(BitBoard.EXCEPT_FILE[0]);
		lo = bs.toLongArray()[0];
		bs2 = BitSetFactory.createBitSet(new long[] { (lo >>> 9) }); // b2->c1; need to blank file 1
		BitBoard.display(bs2);
		// System.out.println(BitBoard.display(bs2));

	}

}
