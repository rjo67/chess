package org.rjo.chess.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Calculates HI and LO values for 8-bit values, where the nth bit is set. e.g. for the 4th bit, HI will be the values
 * of bits 4 to 8, LO will be 1 to 3.
 *
 * @author rich
 */
public class BitValueCalculator {

	private Set<Integer> cacheHI = new HashSet<>(128);
	private Set<Integer> cacheLO = new HashSet<>(128);
	private static int[] MASKS = { 0b00000000, 0b00000001, 0b00000011, 0b00000111, 0b00001111, 0b00011111, 0b00111111, 0b01111111, };

	public static void main(String[] args) {
		BitValueCalculator t = new BitValueCalculator();
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			t.generate(piecePosn);
		}
	}

	public Set<Integer> getCacheHI() {
		return cacheHI;
	}

	public Set<Integer> getCacheLO() {
		return cacheLO;
	}

	public void generate(int piecePosn) {

		// System.out.println("**************** piecePosn=" + piecePosn + " ****************");
		StringBuilder sbHI = new StringBuilder(1000);
		StringBuilder sbLO = new StringBuilder(1000);
		cacheHI.clear();
		cacheLO.clear();
		BitSetUnifier posnOfPiece = BitSetFactory.createBitSet(8);
		posnOfPiece.set(piecePosn);

		int countHI = 0;
		int countLO = 0;
		for (int i = 0; i < 256; i++) {
			BitSetUnifier bs = BitSetFactory.createBitSet(new long[] { i });
			bs.or(posnOfPiece);
			int val = getVal(bs);
			// since we're always setting "posnOfPiece", we get some duplicates
			int hinibble = val >> piecePosn; // discard lowest x bits
			int lonibble = val & MASKS[piecePosn]; // blank highest x bits
			if (piecePosn != 7) {
				if (!cacheHI.contains(hinibble)) {
					sbHI.append(String.format("%3d %s  HI: %s x%s%n", ++countHI, display(val), toBinary(hinibble),
							Integer.toHexString(hinibble).toUpperCase()));
					cacheHI.add(hinibble);
				}
			}
			if (piecePosn != 0) {
				if (!cacheLO.contains(lonibble)) {
					sbLO.append(String.format("%3d %s  LO: %s x%s%n", ++countLO, display(val), toBinary(lonibble),
							Integer.toHexString(lonibble).toUpperCase()));
					cacheLO.add(lonibble);
				}
			}
		}

		// System.out.println(sbHI.toString());
		// System.out.println(cacheHI);
		// postProcess(piecePosn, cacheHI);

		// System.out.println(sbLO.toString());
		// System.out.println("total: " + (countHI + countLO) + "\n\n");
		// System.out.println(cacheLO);
	}

	private static void postProcess(int piecePosn,
			Set<Integer> cache) {
		/*
		 * post process HI nibbles (going left from bit 0): remove instances where the next bit is set (since these are all the
		 * same from our POV) eg. 00011 and 01011 are the same, since bit 1 is set in both
		 */

		// value for 'mask' here is wrong, should be:
		// 11
		// 101
		// 1001
		// 10001
		// etc (3, 5, 9, 17
		int maskStart = 1;
		for (int posnToCheck = piecePosn + 1; posnToCheck < 8; posnToCheck++) {
			maskStart = maskStart << 1;
			int mask = maskStart + 1;
			StringBuilder sb = new StringBuilder();
			sb.append("mask=0b").append(Integer.toBinaryString(mask));

			Iterator<Integer> iter = cache.iterator();
			while (iter.hasNext()) {
				int value = iter.next();
				if ((value & mask) == mask) { // set
					iter.remove();
					sb.append(", ").append(value);
				}
			}
			System.out.println(sb.toString());
		}
		if (!cache.isEmpty()) {
			System.out.println(cache);
		}
	}

	private int getVal(BitSetUnifier bs) {
		int val = 0;
		if (bs.toLongArray().length >= 1) {
			val = (int) bs.toLongArray()[0];
		}
		return val;
	}

	private String toBinary(int val) {
		return String.format("%8s", Integer.toBinaryString(val)).replace(" ", "0");
	}

	private String display(int val) {
		return String.format("%s x%s", toBinary(val), Integer.toHexString(val).toUpperCase());
	}
}
