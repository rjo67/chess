package org.rjo.chess.base.bits;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.impl.ChessBitSetUnifier;
import org.rjo.chess.base.bits.impl.JavaUtilBitSet;
import org.rjo.chess.base.bits.impl.LuceneBitSet;
import org.rjo.chess.base.ray.RayUtils;

public class BitSetTest {

	@Test
	public void speedOfChessBitset() {
		BitSetUnifier bs = new ChessBitSetUnifier(64);
		BitSetUnifier bs2 = new ChessBitSetUnifier(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(bs::set);
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(bs2::set);

		System.out.println("ChessBitSet");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, BitSetUnifier::cardinality);
	}

	@Test
	public void speedOfJavaUtilBitset() {
		BitSetUnifier bs = new JavaUtilBitSet(64);
		BitSetUnifier bs2 = new JavaUtilBitSet(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(bs::set);
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(bs2::set);

		System.out.println("javautil");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, BitSetUnifier::cardinality);
	}

	@Test
	public void speedOfLuceneBitset() {
		BitSetUnifier bs = new LuceneBitSet(64);
		BitSetUnifier bs2 = new LuceneBitSet(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(bs::set);
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(bs2::set);

		System.out.println("lucene");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, BitSetUnifier::cardinality);
	}

	@Test
//	public void speedOfJavolutionBitset() {
//		BitSetUnifier bs = new JavolutionBitSet(64);
//		BitSetUnifier bs2 = new JavolutionBitSet(64);
//
//		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(bs::set);
//		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(bs2::set);
//
//		System.out.println("javolution");
//		repeat("flip", bs, (a) -> a.flip(0, 63));
//		repeat("get", bs, (a) -> a.get(18));
//		repeat("set", bs, (a) -> a.set(42));
//		repeat("and", bs, (a) -> a.and(bs2));
//		repeat("or", bs, (a) -> a.or(bs2));
//		repeat("xor", bs, (a) -> a.xor(bs2));
//		repeat("cardinality", bs, BitSetUnifier::cardinality);
//	}

	private void repeat(String name, BitSetUnifier bs, Consumer<BitSetUnifier> fn) {
		long nbrIters = 10000000;
		StopWatch sw = new StopWatch();
		sw.start();
		for (int i = 0; i < nbrIters; i++) {
			fn.accept(bs);
		}
		long duration = sw.getTime();
		System.out.println("operation: " + name + ", time: " + duration);
	}

	@Test
	public void test2() {

		// can a piece on b2 attack g2?

		BitBoard emptySquares = new BitBoard();
		emptySquares.setBitsAt(Square.d1, Square.c2, Square.d2, Square.e2, Square.f2, Square.d3, Square.d8, Square.f5);

		long start = System.currentTimeMillis();
		int nbrTimes = 10000000;
		int sq1 = Square.b2.bitIndex();
		int sq2 = Square.g2.bitIndex();
		for (int i = 0; i < nbrTimes; i++) {
			assertTrue(checkUsingBitSet((BitSetUnifier) RayUtils.getBitSetOfSquaresBetween(sq1, sq2).clone(),
					emptySquares.getBitSet()));
			// assertTrue(checkUsingIterator(EastRay.instance(), Square.b2, Square.c2,
			// emptySquares.getBitSet()));
		}
		long end = System.currentTimeMillis();
		System.out.println(String.format("%9.6f", 1.0 * (end - start) / nbrTimes));
	}

	private boolean checkUsingBitSet(BitSetUnifier bs, BitSetUnifier emptySquares) {
		int nbrSquaresBetweenStartAndEnd = bs.cardinality();
		bs.and(emptySquares);
		return bs.cardinality() == nbrSquaresBetweenStartAndEnd;
	}

}
