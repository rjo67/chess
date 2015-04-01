package org.rjo.chess;

import java.io.IOException;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Perft, (PERFormance Test, move path enumeration) is a debugging function to walk the move generation tree of strictly
 * legal moves to count all the leaf nodes of a certain depth,
 *
 * @author rich
 * @see http://chessprogramming.wikispaces.com/Perft
 * @see http://chessprogramming.wikispaces.com/Perft+Results
 * @see http://wismuth.com/chess/statistics-games.html
 * @see http://www.chessprogramming.net/perfect-perft/
 * @see http://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=508921&t=47318
 * @see http://www.rocechess.ch/perft.html
 */
public class PerftTest {

   /**
    * performs perft(n) for n = 1..size of expectedNbrOfMoves.
    * Fill expectedNbrOfMoves with -1 to avoid calling for this depth.
    *
    * @param testname
    *           for output purposes
    * @param fenString
    *           the starting position
    * @param sideToMove
    *           which side to move
    * @param expectedNbrOfMoves
    *           int array with expected number of moves.
    */
   private void doTest(String testname, String fenString, Colour sideToMove, int[] expectedNbrOfMoves) {
      for (int depth = 0; depth < expectedNbrOfMoves.length; depth++) {
         if (expectedNbrOfMoves[depth] != -1) {
            Game game = Fen.decode(fenString);
            long start = System.currentTimeMillis();
            int moves = Perft.countMoves(Perft.findMoves(game, sideToMove, depth + 1));
            long time = System.currentTimeMillis() - start;
            System.out.println(String.format(Locale.GERMANY, "%s, %2dply: %,12d moves (%,10d ms) (%8.1f moves/ms)",
                  testname, depth + 1, moves, time, ((moves * 1.0) / time)));
            assertEquals(expectedNbrOfMoves[depth], moves);
         }
      }
   }

   @Test
   public void initialPosition() {
      doTest("initialPosition", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0", Colour.WHITE, new int[] {
            20, 400, 8902, 197281, 4865609 });
   }

   @Test
   public void posn2() throws IOException {
      doTest("posn2", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0", Colour.WHITE, new int[] {
            48, 2039, 97862, 4085603 });
   }

   @Test
   public void posn3() throws IOException {
      doTest("posn3", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0", Colour.WHITE, new int[] { 14, 191, 2812, 43238,
            674624 });
   }

   @Test
   public void posn5() throws IOException {
      doTest("posn5", "rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6", Colour.WHITE, new int[] { 42,
            1352, 53392 });
   }

   @Test
   public void posn6() throws IOException {
      doTest("posn6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", Colour.WHITE,
            new int[] { 46, 2079, 89890, 3894594 });
   }

   @Test
   @Ignore
   // takes 510s
   public void posn6ply5() throws IOException {
      doTest("posn6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", Colour.WHITE,
            new int[] { -1, -1, -1, -1, 164075551 });
   }

   // https://sites.google.com/site/numptychess/perft/position-2
   @Test
   public void numpty2() throws IOException {
      doTest("numpty2", "8/p7/8/1P6/K1k3p1/6P1/7P/8 w - - 0 10", Colour.WHITE, new int[] { 5, 39, 237, 2002, 14062,
            120995, 966152 });
   }

   // https://sites.google.com/site/numptychess/perft/position-3
   @Test
   public void numpty3() throws IOException {
      doTest("numpty3", "r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w KQkq - 0 10", Colour.WHITE, new int[] { 17, 341, 6666,
            150072, 3186478 });
   }

   // https://sites.google.com/site/numptychess/perft/position-4
   @Test
   public void numpty4() throws IOException {
      doTest("numpty4", "8/5p2/8/2k3P1/p3K3/8/1P6/8 b - - 0 10", Colour.BLACK, new int[] { 9, 85, 795, 7658, 72120,
            703851 });
   }

   // https://sites.google.com/site/numptychess/perft/position-5
   @Test
   public void numpty5() throws IOException {
      doTest("numpty5", "r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10", Colour.BLACK, new int[] { 29,
            953, 27990, 909807 });
   }

   // http://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=508921&t=47318
   @Test
   public void illegalEpMove1() throws IOException {
      doTest("illegalEpMove1", "8/8/8/8/k1p4R/8/3P4/3K4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            1134888 });
      doTest("illegalEpMove1", "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            1134888 });
   }

   @Test
   public void illegalEpMove2() throws IOException {
      doTest("illegalEpMove2", "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            1015133 });
      doTest("illegalEpMove2", "8/b2p2k1/8/2P5/8/4K3/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            1015133 });
   }

   @Test
   public void enpassantCaptureChecksOpponent() throws IOException {
      doTest("enpassantCaptureChecksOpponent", "8/5k2/8/2Pp4/2B5/1K6/8/8 w - d6 0 1", Colour.WHITE, new int[] { -1, -1,
            -1, -1, -1, 1440467 });
      doTest("enpassantCaptureChecksOpponent", "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", Colour.BLACK, new int[] { -1, -1,
            -1, -1, -1, 1440467 });
   }

   @Test
   public void shortCastlingChecksOpponent() throws IOException {
      doTest("shortCastlingChecksOpponent", "5k2/8/8/8/8/8/8/4K2R w K - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1,
            -1, 661072 });
      doTest("shortCastlingChecksOpponent", "4k2r/8/8/8/8/8/8/5K2 b k - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1,
            -1, 661072 });
   }

   @Test
   public void longCastlingChecksOpponent() throws IOException {
      doTest("longCastlingChecksOpponent", "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1,
            -1, 803711 });
      doTest("longCastlingChecksOpponent", "r3k3/8/8/8/8/8/8/3K4 b q - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1,
            -1, 803711 });
   }

   @Test
   public void castlingIncludingLosingOrRookCapture() throws IOException {
      doTest("castlingIncludingLosingOrRookCapture", "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", Colour.WHITE,
            new int[] { -1, -1, -1, 1274206 });
      doTest("castlingIncludingLosingOrRookCapture", "r3k2r/7b/8/8/8/8/1B4BQ/R3K2R b KQkq - 0 1", Colour.BLACK,
            new int[] { -1, -1, -1, 1274206 });
   }

   @Test
   public void castlingPrevented() throws IOException {
      doTest("castlingPrevented", "r3k2r/8/5Q2/8/8/3q4/8/R3K2R w KQkq - 0 1", Colour.WHITE, new int[] { -1, -1, -1,
            1720476 });
      doTest("castlingPrevented", "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", Colour.BLACK, new int[] { -1, -1, -1,
            1720476 });
   }

   @Test
   public void promoteOutOfCheck() throws IOException {
      doTest("promoteOutOfCheck", "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            3821001 });
      doTest("promoteOutOfCheck", "3K4/8/8/8/8/8/4p3/2k2R2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            3821001 });
   }

   @Test
   public void discoveredCheck2() throws IOException {
      doTest("discoveredCheck2", "5K2/8/1Q6/2N5/8/1p2k3/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1,
            1004658 });
      doTest("discoveredCheck2", "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1,
            1004658 });
   }

   @Test
   public void promoteToGiveCheck() throws IOException {
      doTest("promoteToGiveCheck", "4k3/1P6/8/8/8/8/K7/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            217342 });
      doTest("promoteToGiveCheck", "8/k7/8/8/8/8/1p6/4K3 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            217342 });
   }

   @Test
   public void underpromoteToGiveCheck() throws IOException {
      doTest("underpromoteToGiveCheck", "8/P1k5/K7/8/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            92683 });
      doTest("underpromoteToGiveCheck", "8/8/8/8/8/k7/p1K5/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            92683 });
   }

   @Test
   public void selfStalemate() throws IOException {
      doTest("selfStalemate", "K1k5/8/P7/8/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 2217 });
      doTest("selfStalemate", "8/8/8/8/8/p7/8/k1K5 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 2217 });
   }

   @Test
   public void selfStalemateCheckmate() throws IOException {
      doTest("selfStalemateCheckmate", "8/k1P5/8/1K6/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,
            -1, 567584 });
      doTest("selfStalemateCheckmate", "8/8/8/8/1k6/8/K1p5/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,
            -1, 567584 });
   }

   @Test
   public void doubleCheck() throws IOException {
      doTest("doubleCheck", "8/5k2/8/5N2/5Q2/2K5/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 23527 });
      doTest("doubleCheck", "8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 23527 });
   }

   /**
    * various discovered checks.
    * Pawn move - discovered check from rook.
    * King move - discovered check from bishop.
    * Pawn capture - discovered check from queen.
    */
   @Test
   public void discoveredCheck() {
      doTest("discoveredCheck", "8/8/8/2k3PR/8/1p2K3/2P2B2/2Q5 w - - 0 10", Colour.WHITE, new int[] { 31 });
   }

   // http://www.rocechess.ch/perft.html
   @Test
   public void promotion() throws IOException {
      doTest("promotion", "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", Colour.BLACK, new int[] { 24, 496, 9483, 182838,
            3605103 /* , 71179139 */});
   }

}
