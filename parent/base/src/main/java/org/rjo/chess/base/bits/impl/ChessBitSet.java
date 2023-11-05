package org.rjo.chess.base.bits.impl;

/**
 * Fixed length 64bit bitset, based on apache lucene implementation FixedBitSet.
 */
public final class ChessBitSet implements Cloneable {

   private static final int numBits = 64; // The number of bits in use
   private long bits; // Array of longs holding the bits

   /**
    * Creates a new (empty) ChessBitSet of size 64 bits.
    */
   public ChessBitSet() {
      this(0L);
   }

   /**
    * Creates a new ChessBitSet of size 64 bits.
    */
   public ChessBitSet(long bits) {
      this.bits = bits;
   }

   public int length() {
      return numBits;
   }

   /** Expert. */
   public long getBits() { return bits; }

   /**
    * Returns number of set bits.
    */
   public int cardinality() {
      return Long.bitCount(bits);
   }

   public boolean get(int index) {
      assert index >= 0 && index < numBits : "index=" + index;
      // signed shift will keep a negative index and force an
      // array-index-out-of-bounds-exception, removing the need for an explicit check.
      long bitmask = 1L << index;
      return (bits & bitmask) != 0;
   }

   public void set(int index) {
      assert index >= 0 && index < numBits : "index=" + index;
      long bitmask = 1L << index;
      bits |= bitmask;
   }

   public void clear(int index) {
      assert index >= 0 && index < numBits : "index=" + index;
      long bitmask = 1L << index;
      bits &= ~bitmask;
   }

   public int nextSetBit(int index) {
      assert index >= 0 && index < numBits : "index=" + index;
      long word = bits >> index; // skip all the bits to the right of index
      if (word != 0) { return index + Long.numberOfTrailingZeros(word); }
      return -1;
   }

   public int prevSetBit(int index) {
      assert index >= 0 && index < numBits : "index=" + index;
      int i = index >> 6;
      final int subIndex = index & 0x3f; // index within the word
      long word = (bits << (63 - subIndex)); // skip all the bits to the left of index

      if (word != 0) { return (i << 6) + subIndex - Long.numberOfLeadingZeros(word); }

      return -1;
   }

   /** this = this OR other */
   public void or(ChessBitSet other) {
      this.bits |= other.bits;
   }

   /** this = this XOR other */
   public void xor(ChessBitSet other) {
      this.bits ^= other.bits;
   }

   /** returns true if the sets have any elements in common */
   public boolean intersects(ChessBitSet other) {
      return ((bits & other.bits) != 0);
   }

   /** this = this AND other */
   public void and(ChessBitSet other) {
      this.bits &= other.bits;
   }

   /** this = this AND NOT other */
   public void andNot(ChessBitSet other) {
      this.bits &= ~other.bits;
   }

   /**
    * @return true if all bits are clear.
    */
   public boolean isEmpty() { return (bits == 0L); }

   /**
    * Flips a range of bits
    *
    * @param startIndex lower index
    * @param endIndex   one-past the last bit to flip
    */
   public void flip(int startIndex, int endIndex) {
      assert startIndex >= 0 && startIndex < numBits;
      assert endIndex >= 0 && endIndex <= numBits;
      if (endIndex <= startIndex) { return; }

      /*
       * ** Grrr, java shifting uses only the lower 6 bits of the count so -1L>>>64 == -1 for that reason, make sure not to use endmask if the
       * bits to flip will be zero in the last word (redefine endWord to be the last changed...) long startmask = -1L << (startIndex & 0x3f); //
       * example: 11111...111000 long endmask = -1L >>> (64-(endIndex & 0x3f)); // example: 00111...111111
       */

      long startmask = -1L << startIndex;
      long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as -endIndex since only the lowest 6 bits are used

      bits ^= (startmask & endmask);
   }

   /** Flip the bit at the provided index. */
   public void flip(int index) {
      assert index >= 0 && index < numBits : "index=" + index + " numBits=" + numBits;
      long bitmask = 1L << index; // mod 64 is implicit
      bits ^= bitmask;
   }

   /**
    * Sets a range of bits
    *
    * @param startIndex lower index
    * @param endIndex   one-past the last bit to set
    */
   public void set(int startIndex, int endIndex) {
      assert startIndex >= 0 && startIndex < numBits : "startIndex=" + startIndex + ", numBits=" + numBits;
      assert endIndex >= 0 && endIndex <= numBits : "endIndex=" + endIndex + ", numBits=" + numBits;
      if (endIndex <= startIndex) { return; }

      long startmask = -1L << startIndex;
      long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as -endIndex since only the lowest 6 bits are used

      bits |= (startmask & endmask);
   }

   public void clear(int startIndex, int endIndex) {
      assert startIndex >= 0 && startIndex < numBits : "startIndex=" + startIndex + ", numBits=" + numBits;
      assert endIndex >= 0 && endIndex <= numBits : "endIndex=" + endIndex + ", numBits=" + numBits;
      if (endIndex <= startIndex) { return; }

      long startmask = -1L << startIndex;
      long endmask = -1L >>> -endIndex; // 64-(endIndex&0x3f) is the same as -endIndex since only the lowest 6 bits are used

      // invert masks since we are clearing
      startmask = ~startmask;
      endmask = ~endmask;

      bits &= (startmask | endmask);
   }

   @Override
   public ChessBitSet clone() {
      return new ChessBitSet(bits);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) { return true; }
      if (!(o instanceof ChessBitSet)) { return false; }
      ChessBitSet other = (ChessBitSet) o;
      return this.bits == other.bits;
   }

   @Override
   public int hashCode() {
      long h = 0;
      h ^= bits;
      h = (h << 1) | (h >>> 63); // rotate left
      // fold leftmost bits into right and add a constant to prevent
      // empty sets from returning 0, which is too common.
      return (int) ((h >> 32) ^ h) + 0x98761234;
   }

   @Override
   public String toString() {

      final int MAX_INITIAL_CAPACITY = Integer.MAX_VALUE - 8;
      int numBits = 64;
      StringBuilder b = new StringBuilder(6 * numBits + 2);
      b.append('{');
      boolean first = true;
      for (int i = 0; i < 64; i++) {
         if (get(i)) {
            if (first) {
               first = false;
            } else {
               b.append(",");
            }
            b.append(i);
         }
      }
      b.append('}');
      return b.toString();
   }
}
