package org.rjo.chess.util;

import java.util.Arrays;
import java.util.stream.Stream;

import org.rjo.chess.Square;

/**
 * A simple cache to map squares to values.
 * 
 * @param <T> the type of the information which is stored for each square
 */
public class SquareCache<T> {

	// the cache
	private T[] cache;
	// the default value for each entry (can be null)
	private T defaultValue;

	// can't create generic array
	@SuppressWarnings("unchecked")
	public SquareCache(T defaultValue) {
		this.cache = (T[]) new Object[Square.values().length];
		this.defaultValue = defaultValue;
		reset();
	}

	public SquareCache(SquareCache<T> copy) {
		cache = Arrays.copyOf(copy.cache, copy.cache.length);
		defaultValue = copy.defaultValue;
	}

	/**
	 * Reset the cache to the default value.
	 */
	public void reset() {
		Arrays.fill(cache, defaultValue);
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

	final public Stream<T> stream() {
		return Arrays.stream(cache);
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