package org.rjo.chess.util;

import java.util.Arrays;

import org.rjo.chess.Square;

/**
 * A simple cache to map values to squares.
 */
public class SquareCache<T> {
	// can't create generic array
	private T[] cache;

	@SuppressWarnings("unchecked")
	public SquareCache(T defaultValue) {
		cache = (T[]) new Object[Square.values().length];
		for (int i = 0; i < Square.values().length; i++) {
			cache[i] = defaultValue;
		}
	}

	public SquareCache(SquareCache<T> copy) {
		cache = Arrays.copyOf(copy.cache, copy.cache.length);
	}

	final public T lookup(Square square) {
		return lookup(square.bitIndex());
	}

	final public T lookup(int squareBitIndex) {
		return cache[squareBitIndex];
	}

	final public void store(Square square,
			T value) {
		store(square.bitIndex(), value);
	}

	final public void store(int squareBitIndex,
			T value) {
		cache[squareBitIndex] = value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		for (Square sq : Square.values()) {
			if (this.lookup(sq) != null) {
				sb.append("(" + sq + ":" + this.lookup(sq) + ")");
			}
		}
		return sb.toString();
	}
}