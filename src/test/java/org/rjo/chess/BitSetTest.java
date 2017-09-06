package org.rjo.chess;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.rjo.chess.ray.Ray;
import org.rjo.chess.ray.RayUtils;
import org.rjo.chess.util.BitSetUnifier;
import org.rjo.chess.util.ChessBitSetUnifier;
import org.rjo.chess.util.JavaUtilBitSet;
import org.rjo.chess.util.JavolutionBitSet;
import org.rjo.chess.util.LuceneBitSet;

public class BitSetTest {

	@Test
	public void speedOfChessBitset() {
		BitSetUnifier bs = new ChessBitSetUnifier(64);
		BitSetUnifier bs2 = new ChessBitSetUnifier(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(i -> bs.set(i));
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(i -> bs2.set(i));

		System.out.println("ChessBitSet");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, (a) -> a.cardinality());
	}

	@Test
	public void speedOfJavaUtilBitset() {
		BitSetUnifier bs = new JavaUtilBitSet(64);
		BitSetUnifier bs2 = new JavaUtilBitSet(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(i -> bs.set(i));
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(i -> bs2.set(i));

		System.out.println("javautil");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, (a) -> a.cardinality());
	}

	@Test
	public void speedOfLuceneBitset() {
		BitSetUnifier bs = new LuceneBitSet(64);
		BitSetUnifier bs2 = new LuceneBitSet(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(i -> bs.set(i));
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(i -> bs2.set(i));

		System.out.println("lucene");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, (a) -> a.cardinality());
	}

	@Test
	public void speedOfJavolutionBitset() {
		BitSetUnifier bs = new JavolutionBitSet(64);
		BitSetUnifier bs2 = new JavolutionBitSet(64);

		Arrays.stream(new int[] { 2, 7, 14, 23, 35, 37, 41, 46 }).forEach(i -> bs.set(i));
		Arrays.stream(new int[] { 1, 9, 21, 33, 45, 48, 56 }).forEach(i -> bs2.set(i));

		System.out.println("javolution");
		repeat("flip", bs, (a) -> a.flip(0, 63));
		repeat("get", bs, (a) -> a.get(18));
		repeat("set", bs, (a) -> a.set(42));
		repeat("and", bs, (a) -> a.and(bs2));
		repeat("or", bs, (a) -> a.or(bs2));
		repeat("xor", bs, (a) -> a.xor(bs2));
		repeat("cardinality", bs, (a) -> a.cardinality());
	}

	private void repeat(String name,
			BitSetUnifier bs,
			Consumer<BitSetUnifier> fn) {
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
			//			assertTrue(checkUsingIterator(EastRay.instance(), Square.b2, Square.c2, emptySquares.getBitSet()));
		}
		long end = System.currentTimeMillis();
		System.out.println(String.format("%9.6f", 1.0 * (end - start) / nbrTimes));
	}

	private boolean checkUsingBitSet(BitSetUnifier bs,
			BitSetUnifier emptySquares) {
		int nbrSquaresBetweenStartAndEnd = bs.cardinality();
		bs.and(emptySquares);
		return bs.cardinality() == nbrSquaresBetweenStartAndEnd;
	}

	private boolean checkUsingIterator(Ray ray,
			Square startSquare,
			Square targetSquare,
			BitSet emptySquares) {
		Iterator<Integer> squaresFrom = ray.squaresFrom(startSquare);
		while (squaresFrom.hasNext()) {
			int nextSquare = squaresFrom.next();
			if (nextSquare == targetSquare.bitIndex()) {
				return true;
			} else if (!emptySquares.get(nextSquare)) {
				return false;
			}
		}
		return false;
	}

}
