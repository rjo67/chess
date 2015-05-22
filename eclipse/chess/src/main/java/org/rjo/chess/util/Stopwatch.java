package org.rjo.chess.util;

public class Stopwatch {

   private long start;

   public Stopwatch() {
      start();
   }

   public void start() {
      this.start = System.currentTimeMillis();
   }

   public long read() {
      return System.currentTimeMillis() - start;
   }

}
