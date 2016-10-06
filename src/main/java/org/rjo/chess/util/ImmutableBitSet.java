package org.rjo.chess.util;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * An immutable bitset. Prevents (accidental) modification of the underlying bitset by using for example the methods and
 * or xor.
 *
 * @author rich
 */
public class ImmutableBitSet implements Cloneable {

   private BitSet bs;

   public ImmutableBitSet(BitSet bs) {
      this.bs = bs;
   }

   public byte[] toByteArray() {
      return bs.toByteArray();
   }

   public long[] toLongArray() {
      return bs.toLongArray();
   }

   public void flip(@SuppressWarnings("unused") int bitIndex) {
      throw new UnsupportedOperationException();
   }

   public void flip(@SuppressWarnings("unused") int fromIndex, @SuppressWarnings("unused") int toIndex) {
      throw new UnsupportedOperationException();
   }

   public void set(@SuppressWarnings("unused") int bitIndex) {
      throw new UnsupportedOperationException();
   }

   public void set(@SuppressWarnings("unused") int bitIndex, @SuppressWarnings("unused") boolean value) {
      throw new UnsupportedOperationException();
   }

   public void set(@SuppressWarnings("unused") int fromIndex, @SuppressWarnings("unused") int toIndex) {
      throw new UnsupportedOperationException();
   }

   public void set(@SuppressWarnings("unused") int fromIndex, @SuppressWarnings("unused") int toIndex,
         @SuppressWarnings("unused") boolean value) {
      throw new UnsupportedOperationException();
   }

   public void clear(@SuppressWarnings("unused") int bitIndex) {
      throw new UnsupportedOperationException();
   }

   public void clear(@SuppressWarnings("unused") int fromIndex, @SuppressWarnings("unused") int toIndex) {
      throw new UnsupportedOperationException();
   }

   public void clear() {
      throw new UnsupportedOperationException();
   }

   public boolean get(int bitIndex) {
      return bs.get(bitIndex);
   }

   public BitSet get(int fromIndex, int toIndex) {
      return bs.get(fromIndex, toIndex);
   }

   public int nextSetBit(int fromIndex) {
      return bs.nextSetBit(fromIndex);
   }

   public int nextClearBit(int fromIndex) {
      return bs.nextClearBit(fromIndex);
   }

   public int previousSetBit(int fromIndex) {
      return bs.previousSetBit(fromIndex);
   }

   public int previousClearBit(int fromIndex) {
      return bs.previousClearBit(fromIndex);
   }

   public int length() {
      return bs.length();
   }

   public boolean isEmpty() {
      return bs.isEmpty();
   }

   public boolean intersects(BitSet set) {
      return bs.intersects(set);
   }

   public int cardinality() {
      return bs.cardinality();
   }

   public void and(@SuppressWarnings("unused") BitSet set) {
      throw new UnsupportedOperationException();
   }

   public void or(@SuppressWarnings("unused") BitSet set) {
      throw new UnsupportedOperationException();
   }

   public void xor(@SuppressWarnings("unused") BitSet set) {
      throw new UnsupportedOperationException();
   }

   public void andNot(@SuppressWarnings("unused") BitSet set) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int hashCode() {
      return bs.hashCode();
   }

   public int size() {
      return bs.size();
   }

   @Override
   public boolean equals(Object obj) {
      return bs.equals(obj);
   }

   @Override
   public Object clone() {
      return new ImmutableBitSet((BitSet) bs.clone());
   }

   @Override
   public String toString() {
      return bs.toString();
   }

   public IntStream stream() {
      return bs.stream();
   }

}
