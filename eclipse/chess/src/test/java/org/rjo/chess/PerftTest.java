package org.rjo.chess;

import java.io.IOException;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Perft, (PERFormance Test, move path enumeration) is a debugging function to walk the move generation tree of strictly
 * legal moves to count all the leaf nodes of a certain depth.
 *
 * PERFT tests are usually pretty slow, therefore the majority of tests do not run automatically.
 *
 * <p>
 * Results 06.04.2015:
 * 
 * <pre>
 * Running all tests
 * initialPosition,  1ply:           20 moves (       15 ms) (    1,3 moves/ms)
 * initialPosition,  2ply:          400 moves (       12 ms) (   33,3 moves/ms)
 * initialPosition,  3ply:        8.902 moves (       58 ms) (  153,5 moves/ms)
 * initialPosition,  4ply:      197.281 moves (      400 ms) (  493,2 moves/ms)
 * initialPosition,  5ply:    4.865.609 moves (    5.188 ms) (  937,9 moves/ms)
 * posn2,  1ply:           48 moves (        1 ms) (   48,0 moves/ms)
 * posn2,  2ply:        2.039 moves (        9 ms) (  226,6 moves/ms)
 * posn2,  3ply:       97.862 moves (      147 ms) (  665,7 moves/ms)
 * posn2,  4ply:    4.085.603 moves (    3.702 ms) ( 1103,6 moves/ms)
 * posn3,  1ply:           14 moves (        0 ms) (Infinity moves/ms)
 * posn3,  2ply:          191 moves (        0 ms) (Infinity moves/ms)
 * posn3,  3ply:        2.812 moves (        5 ms) (  562,4 moves/ms)
 * posn3,  4ply:       43.238 moves (       45 ms) (  960,8 moves/ms)
 * posn3,  5ply:      674.624 moves (      672 ms) ( 1003,9 moves/ms)
 * posn5,  1ply:           42 moves (        1 ms) (   42,0 moves/ms)
 * posn5,  2ply:        1.352 moves (        1 ms) ( 1352,0 moves/ms)
 * posn5,  3ply:       53.392 moves (       44 ms) ( 1213,5 moves/ms)
 * posn6,  1ply:           46 moves (        0 ms) (Infinity moves/ms)
 * posn6,  2ply:        2.079 moves (        2 ms) ( 1039,5 moves/ms)
 * posn6,  3ply:       89.890 moves (       73 ms) ( 1231,4 moves/ms)
 * posn6,  4ply:    3.894.594 moves (    3.002 ms) ( 1297,3 moves/ms)
 * numpty2,  1ply:            5 moves (        0 ms) (Infinity moves/ms)
 * numpty2,  2ply:           39 moves (        0 ms) (Infinity moves/ms)
 * numpty2,  3ply:          237 moves (        0 ms) (Infinity moves/ms)
 * numpty2,  4ply:        2.002 moves (        4 ms) (  500,5 moves/ms)
 * numpty2,  5ply:       14.062 moves (       27 ms) (  520,8 moves/ms)
 * numpty2,  6ply:      120.995 moves (      195 ms) (  620,5 moves/ms)
 * numpty2,  7ply:      966.152 moves (    1.242 ms) (  777,9 moves/ms)
 * numpty3,  1ply:           17 moves (        0 ms) (Infinity moves/ms)
 * numpty3,  2ply:          341 moves (        0 ms) (Infinity moves/ms)
 * numpty3,  3ply:        6.666 moves (        7 ms) (  952,3 moves/ms)
 * numpty3,  4ply:      150.072 moves (      152 ms) (  987,3 moves/ms)
 * numpty3,  5ply:    3.186.478 moves (    2.943 ms) ( 1082,7 moves/ms)
 * numpty4,  1ply:            9 moves (        0 ms) (Infinity moves/ms)
 * numpty4,  2ply:           85 moves (        0 ms) (Infinity moves/ms)
 * numpty4,  3ply:          795 moves (        1 ms) (  795,0 moves/ms)
 * numpty4,  4ply:        7.658 moves (        8 ms) (  957,3 moves/ms)
 * numpty4,  5ply:       72.120 moves (       99 ms) (  728,5 moves/ms)
 * numpty4,  6ply:      703.851 moves (      843 ms) (  834,9 moves/ms)
 * numpty5,  1ply:           29 moves (        0 ms) (Infinity moves/ms)
 * numpty5,  2ply:          953 moves (        1 ms) (  953,0 moves/ms)
 * numpty5,  3ply:       27.990 moves (       23 ms) ( 1217,0 moves/ms)
 * numpty5,  4ply:      909.807 moves (      726 ms) ( 1253,2 moves/ms)
 * illegalEpMove1,  6ply:    1.134.888 moves (    1.605 ms) (  707,1 moves/ms)
 * illegalEpMove1,  6ply:    1.134.888 moves (    1.598 ms) (  710,2 moves/ms)
 * illegalEpMove2,  6ply:    1.015.133 moves (    1.344 ms) (  755,3 moves/ms)
 * illegalEpMove2,  6ply:    1.015.133 moves (    1.335 ms) (  760,4 moves/ms)
 * epCaptureChecksOpponent,  6ply:    1.440.467 moves (    1.995 ms) (  722,0 moves/ms)
 * epCaptureChecksOpponent,  6ply:    1.440.467 moves (    2.011 ms) (  716,3 moves/ms)
 * shortCastlingChecksOpponent,  6ply:      661.072 moves (      989 ms) (  668,4 moves/ms)
 * shortCastlingChecksOpponent,  6ply:      661.072 moves (      936 ms) (  706,3 moves/ms)
 * longCastlingChecksOpponent,  6ply:      803.711 moves (    1.103 ms) (  728,7 moves/ms)
 * longCastlingChecksOpponent,  6ply:      803.711 moves (    1.123 ms) (  715,7 moves/ms)
 * castlingIncludingLosingOrRookCapture,  4ply:    1.274.206 moves (      892 ms) ( 1428,5 moves/ms)
 * castlingIncludingLosingOrRookCapture,  4ply:    1.274.206 moves (      910 ms) ( 1400,2 moves/ms)
 * castlingPrevented,  4ply:    1.720.476 moves (    1.370 ms) ( 1255,8 moves/ms)
 * castlingPrevented,  4ply:    1.720.476 moves (    1.388 ms) ( 1239,5 moves/ms)
 * promoteOutOfCheck,  6ply:    3.821.001 moves (    3.476 ms) ( 1099,3 moves/ms)
 * promoteOutOfCheck,  6ply:    3.821.001 moves (    3.407 ms) ( 1121,5 moves/ms)
 * discoveredCheck2,  5ply:    1.004.658 moves (      765 ms) ( 1313,3 moves/ms)
 * discoveredCheck2,  5ply:    1.004.658 moves (      753 ms) ( 1334,2 moves/ms)
 * selfStalemateCheckmate,  7ply:      567.584 moves (      545 ms) ( 1041,4 moves/ms)
 * selfStalemateCheckmate,  7ply:      567.584 moves (      541 ms) ( 1049,1 moves/ms)
 * promotion www.rocechess.ch/perft.html,  1ply:           24 moves (        1 ms) (   24,0 moves/ms)
 * promotion www.rocechess.ch/perft.html,  2ply:          496 moves (        0 ms) (Infinity moves/ms)
 * promotion www.rocechess.ch/perft.html,  3ply:        9.483 moves (        9 ms) ( 1053,7 moves/ms)
 * promotion www.rocechess.ch/perft.html,  4ply:      182.838 moves (      176 ms) ( 1038,9 moves/ms)
 * promotion www.rocechess.ch/perft.html,  5ply:    3.605.103 moves (    3.327 ms) ( 1083,6 moves/ms)
 * promoteToGiveCheck,  6ply:      217.342 moves (      309 ms) (  703,4 moves/ms)
 * promoteToGiveCheck,  6ply:      217.342 moves (      312 ms) (  696,6 moves/ms)
 * Finished in     51 s
 * </pre>
 *
 * @author rich
 * @see http://chessprogramming.wikispaces.com/Perft
 * @see http://chessprogramming.wikispaces.com/Perft+Results
 * @see http://wismuth.com/chess/statistics-games.html
 * @see http://www.chessprogramming.net/perfect-perft/
 * @see http://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=508921&t=47318
 * @see http://www.rocechess.ch/perft.html
 * @see https://sites.google.com/site/numptychess/perft
 */
public class PerftTest {

   /**
    * Use this to run all the long-running PERFT tests (see {@link #allTests()}.
    * <ul>
    * <li>Took 124s prior to 02.04.2015.</li>
    * <li>Took 112s on 02.04.2015.</li>
    * <li>Took 57s on 03.04.2015.</li>
    * </ul>
    */
   public static void main(String[] args) {
      System.out.println("Running all tests");
      long start = System.currentTimeMillis();
      new PerftTest().allTests();
      System.out.println(String.format(Locale.GERMANY, "Finished in %,6d s",
            (System.currentTimeMillis() - start) / 1000));
   }

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
            System.out.println(String.format(Locale.GERMANY, "%s, %2dply: %,12d moves (%,9d ms) (%7.1f moves/ms)",
                  testname, depth + 1, moves, time, ((moves * 1.0) / time)));
            assertEquals("wrong nbr of moves for fen: " + fenString, expectedNbrOfMoves[depth], moves);
         }
      }
   }

   /**
    * Data for {@link #allTests()}.
    *
    * Format is:
    * <ul>
    * <li>string describing the test</li>
    * <li>FEN</li>
    * <li>whose move it is -- Colour enum</li>
    * <li>expected number of moves for each depth (-1 means do not perform search at this depth) -- int[]</li>
    * </ul>
    */
   //@formatter:off
   private Object[][] data = new Object[][] {
         {"initialPosition", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0", Colour.WHITE, new int[] { 20, 400, 8902, 197281, 4865609}},
         {"posn2", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0", Colour.WHITE, new int[] {48, 2039, 97862, 4085603}},
         {"posn3", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0", Colour.WHITE, new int[] { 14, 191, 2812, 43238, 674624}},
         {"posn5", "rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6", Colour.WHITE, new int[] { 42,1352, 53392}},
         {"posn6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", Colour.WHITE, new int[] { 46, 2079, 89890, 3894594}},
         {"numpty2", "8/p7/8/1P6/K1k3p1/6P1/7P/8 w - - 0 10", Colour.WHITE, new int[] { 5, 39, 237, 2002, 14062, 120995, 966152 }},
         {"numpty3", "r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w KQkq - 0 10", Colour.WHITE, new int[] { 17, 341, 6666, 150072, 3186478 }},
         {"numpty4", "8/5p2/8/2k3P1/p3K3/8/1P6/8 b - - 0 10", Colour.BLACK, new int[] { 9, 85, 795, 7658, 72120, 703851 }},
         {"numpty5", "r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10", Colour.BLACK, new int[] { 29, 953, 27990, 909807 }},
         {"illegalEpMove1", "8/8/8/8/k1p4R/8/3P4/3K4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,1134888 }},
         {"illegalEpMove1", "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1,1134888 }},
         {"illegalEpMove2", "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 1015133 }},
         {"illegalEpMove2", "8/b2p2k1/8/2P5/8/4K3/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 1015133 }},
         {"epCaptureChecksOpponent", "8/5k2/8/2Pp4/2B5/1K6/8/8 w - d6 0 1", Colour.WHITE, new int[] { -1, -1,-1, -1, -1, 1440467 }},
         {"epCaptureChecksOpponent", "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 1440467 }},
         {"shortCastlingChecksOpponent", "5k2/8/8/8/8/8/8/4K2R w K - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 661072 }},
         {"shortCastlingChecksOpponent", "4k2r/8/8/8/8/8/8/5K2 b k - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 661072 }},
         {"longCastlingChecksOpponent", "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 803711 }},
         {"longCastlingChecksOpponent", "r3k3/8/8/8/8/8/8/3K4 b q - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 803711 }},
         {"castlingIncludingLosingOrRookCapture", "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 1274206 }},
         {"castlingIncludingLosingOrRookCapture", "r3k2r/7b/8/8/8/8/1B4BQ/R3K2R b KQkq - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 1274206 }},
         {"castlingPrevented", "r3k2r/8/5Q2/8/8/3q4/8/R3K2R w KQkq - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 1720476 }},
         {"castlingPrevented", "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 1720476 }},
         {"promoteOutOfCheck", "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 3821001 }},
         {"promoteOutOfCheck", "3K4/8/8/8/8/8/4p3/2k2R2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 3821001 }},
         {"discoveredCheck2", "5K2/8/1Q6/2N5/8/1p2k3/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, 1004658 }},
         {"discoveredCheck2", "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, 1004658 }},
         {"selfStalemateCheckmate", "8/k1P5/8/1K6/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, -1, 567584 }},
         {"selfStalemateCheckmate", "8/8/8/8/1k6/8/K1p5/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, -1, 567584 }},
         {"promotion www.rocechess.ch/perft.html", "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", Colour.BLACK, new int[] { 24, 496, 9483, 182838, 3605103 /* , 71179139 */}},
         {"promoteToGiveCheck", "4k3/1P6/8/8/8/8/K7/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1,217342 }},
         {"promoteToGiveCheck", "8/k7/8/8/8/8/1p6/4K3 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 217342 }},
   };
   //@formatter:on

   @Test
   @Ignore("too slow for normal junit testing. See main method.")
   public void allTests() {
      for (Object[] d : data) {
         doTest((String) d[0], (String) d[1], (Colour) d[2], (int[]) d[3]);
      }
   }

   @Test
   @Ignore("too slow for normal junit tests. See Perft::main")
   // posn6, 5ply: 164.075.551 moves ( 192.403 ms) ( 852,8 moves/ms)
   public void posn6ply5() throws IOException {
      doTest("posn6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", Colour.WHITE,
            new int[] { -1, -1, -1, -1, 164075551 });
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

}
