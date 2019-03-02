package org.rjo.chess;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.rjo.chess.base.Colour;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;

/**
 * Perft, (PERFormance Test, move path enumeration) is a debugging function to walk the move generation tree of strictly
 * legal moves to count all the leaf nodes of a certain depth. PERFT tests are usually pretty slow, therefore the
 * majority of tests do not run automatically.
 * <p>
 * Results 08.04.2015:
 *
 * <pre>
 * Running all tests
 *                          initialPosition  5ply:    4.865.609 moves (    4.142 ms) ( 1174,7 moves/ms)
 *                                    posn2  4ply:    4.085.603 moves (    2.547 ms) ( 1604,1 moves/ms)
 *                                    posn3  5ply:      674.624 moves (      547 ms) ( 1233,3 moves/ms)
 *                                    posn6  4ply:    3.894.594 moves (    1.766 ms) ( 2205,3 moves/ms)
 *                                  numpty2  7ply:      966.152 moves (      953 ms) ( 1013,8 moves/ms)
 *                                  numpty3  5ply:    3.186.478 moves (    2.016 ms) ( 1580,6 moves/ms)
 *                                  numpty4  6ply:      703.851 moves (      625 ms) ( 1126,2 moves/ms)
 *                                  numpty5  4ply:      909.807 moves (      515 ms) ( 1766,6 moves/ms)
 *                           illegalEpMove1  6ply:    1.134.888 moves (    1.141 ms) (  994,6 moves/ms)
 *                           illegalEpMove1  6ply:    1.134.888 moves (    1.126 ms) ( 1007,9 moves/ms)
 *                           illegalEpMove2  6ply:    1.015.133 moves (      923 ms) ( 1099,8 moves/ms)
 *                           illegalEpMove2  6ply:    1.015.133 moves (      922 ms) ( 1101,0 moves/ms)
 *                  epCaptureChecksOpponent  6ply:    1.440.467 moves (    1.329 ms) ( 1083,9 moves/ms)
 *                  epCaptureChecksOpponent  6ply:    1.440.467 moves (    1.329 ms) ( 1083,9 moves/ms)
 *              shortCastlingChecksOpponent  6ply:      661.072 moves (      626 ms) ( 1056,0 moves/ms)
 *              shortCastlingChecksOpponent  6ply:      661.072 moves (      625 ms) ( 1057,7 moves/ms)
 *               longCastlingChecksOpponent  6ply:      803.711 moves (      736 ms) ( 1092,0 moves/ms)
 *               longCastlingChecksOpponent  6ply:      803.711 moves (      781 ms) ( 1029,1 moves/ms)
 *     castlingIncludingLosingOrRookCapture  4ply:    1.274.206 moves (      548 ms) ( 2325,2 moves/ms)
 *     castlingIncludingLosingOrRookCapture  4ply:    1.274.206 moves (      547 ms) ( 2329,4 moves/ms)
 *                        castlingPrevented  4ply:    1.720.476 moves (      861 ms) ( 1998,2 moves/ms)
 *                        castlingPrevented  4ply:    1.720.476 moves (      859 ms) ( 2002,9 moves/ms)
 *                        promoteOutOfCheck  6ply:    3.821.001 moves (    2.173 ms) ( 1758,4 moves/ms)
 *                        promoteOutOfCheck  6ply:    3.821.001 moves (    2.189 ms) ( 1745,5 moves/ms)
 *    promotion www.rocechess.ch/perft.html  5ply:    3.605.103 moves (    2.328 ms) ( 1548,6 moves/ms)
 * Finished in     35 s
 * </pre>
 * <p>
 * <h1>Debugging tips</h1> If the Perft does not return the correct number of moves, one way of attacking the problem is
 * to use the excellent "JetChess" to calculate the perft scores. Then see which move(s) return the wrong number of
 * moves, and take it from there (at depth-1).
 *
 * @author rich
 * @see 'http://chessprogramming.wikispaces.com/Perft'
 * @see 'http://chessprogramming.wikispaces.com/Perft+Results'
 * @see 'http://wismuth.com/chess/statistics-games.html'
 * @see 'http://www.chessprogramming.net/perfect-perft/'
 * @see 'http://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=508921&t=47318'
 * @see 'http://www.rocechess.ch/perft.html'
 * @see 'https://sites.google.com/site/numptychess/perft'
 */
@RunWith(Parameterized.class)
public class PerftTest {

	private String testname;
	private String fenString;
	private Colour sideToMove;
	private int[] expectedNbrOfMoves;

	public PerftTest(String testname,
			String fenString,
			Colour sideToMove,
			int[] expectedNbrOfMoves) {
		this.testname = testname;
		this.fenString = fenString;
		this.sideToMove = sideToMove;
		this.expectedNbrOfMoves = expectedNbrOfMoves;
	}

	/**
	 * performs perft(n) for n = 1..size of expectedNbrOfMoves. Fill expectedNbrOfMoves with -1 to avoid calling for this
	 * depth.
	 */
	@Test
	public void test() {
		for (int depth = 0; depth < expectedNbrOfMoves.length; depth++) {
			if (expectedNbrOfMoves[depth] != -1) {
				Game game = Fen.decode(fenString);
				long start = System.currentTimeMillis();
				// tests run with threads=3 to finish in 24 seconds instead of 40+
				Map<String, Integer> moveMap = Perft.findMoves(game.getPosition(), sideToMove, depth + 1, 1);
				int moves = Perft.countMoves(moveMap);
				long time = System.currentTimeMillis() - start;
				// no printout if took less than 500ms
				System.out.println(String.format(Locale.GERMANY, "%40s %2dply: %,12d moves (%,8d ms) (%6.1f moves/ms)", testname, depth + 1,
						moves, time, ((moves * 1.0) / time)));
				assertEquals("error in test '" + testname + "': wrong nbr of moves at depth " + (depth + 1) + "\nfen: " + fenString
						+ "\nmoveMap: " + moveMap + "\n", expectedNbrOfMoves[depth], moves);
			}
		}
	}

	/**
	 * Data for the tests. Format is:
	 * <ul>
	 * <li>string describing the test</li>
	 * <li>FEN</li>
	 * <li>whose move it is -- Colour enum</li>
	 * <li>expected number of moves for each depth (-1 means do not perform search at this depth) -- int[]</li>
	 * </ul>
	 */
	@Parameters(name = "perft({0})")
	public static Collection<Object[]> data() {
		// @formatter:off
		return Arrays.asList(new Object[][] {
			{ "initialPosition", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0", Colour.WHITE, new int[] { 20, 400, 8902, 197281, 4865609 }},
			{ "posn2", "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 0", Colour.WHITE, new int[] { 48, 2039, 97862, 4085603 } },
			{ "posn3", "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 0", Colour.WHITE, new int[] {14, 191, 2812, 43238, 674624 } },
			{ "posn5", "rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6", Colour.WHITE, new int[] { 42, 1352, 53392 } },
			{ "posn6", "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", Colour.WHITE,
					new int[] { 46, 2079, 89890, 3894594 /*, 164075551*/ } },
			{ "numpty2", "8/p7/8/1P6/K1k3p1/6P1/7P/8 w - - 0 10", Colour.WHITE, new int[] { 5, 39, 237, 2002, 14062, 120995, 966152 } },
			{ "numpty3", "r3k2r/p6p/8/B7/1pp1p3/3b4/P6P/R3K2R w KQkq - 0 10", Colour.WHITE, new int[] { 17, 341, 6666, 150072, 3186478 } },
			{ "numpty4", "8/5p2/8/2k3P1/p3K3/8/1P6/8 b - - 0 10", Colour.BLACK, new int[] { 9, 85, 795, 7658, 72120, 703851 } },
			{ "numpty5", "r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10", Colour.BLACK, new int[] {29, 953, 27990, 909807 } },
			{ "illegalEpMove1W", "8/8/8/8/k1p4R/8/3P4/3K4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 1134888 } },
			{ "illegalEpMove1B", "3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 1134888 } },
			{ "illegalEpMove2W", "8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 1015133 } },
			{ "illegalEpMove2B", "8/b2p2k1/8/2P5/8/4K3/8/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 1015133 } },
			{ "epCaptureChecksOpponentW", "8/5k2/8/2Pp4/2B5/1K6/8/8 w - d6 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 1440467 } },
			{ "epCaptureChecksOpponentB", "8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 1440467 } },
			{ "shortCastlingChecksOpponentW", "5k2/8/8/8/8/8/8/4K2R w K - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 661072 } },
			{ "shortCastlingChecksOpponentB", "4k2r/8/8/8/8/8/8/5K2 b k - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 661072 } },
			{ "longCastlingChecksOpponentW", "3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 803711 } },
			{ "longCastlingChecksOpponentB", "r3k3/8/8/8/8/8/8/3K4 b q - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 803711 } },
			{ "castlingIncludingLosingOrRookCaptureW", "r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 1274206 } },
			{ "castlingIncludingLosingOrRookCaptureB", "r3k2r/7b/8/8/8/8/1B4BQ/R3K2R b KQkq - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 1274206 } },
			{ "castlingPreventedW", "r3k2r/8/5Q2/8/8/3q4/8/R3K2R w KQkq - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 1720476 } },
			{ "castlingPreventedB", "r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 1720476 } },
			{ "promoteOutOfCheckW", "2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 3821001 } },
			{ "promoteOutOfCheckB", "3K4/8/8/8/8/8/4p3/2k2R2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 3821001 } },
			{ "discoveredCheck", "8/8/8/2k3PR/8/1p2K3/2P2B2/2Q5 w - - 0 10", Colour.WHITE, new int[] { 31 }},
			{ "discoveredCheck2W", "5K2/8/1Q6/2N5/8/1p2k3/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, 1004658 } },
			{ "discoveredCheck2B", "8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, 1004658 } },
			{ "selfStalemateCheckmate", "8/k1P5/8/1K6/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, -1, 567584 } },
			{ "selfStalemateCheckmate2", "8/8/8/8/1k6/8/K1p5/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, -1, 567584 } },
			{ "selfStalemateW", "K1k5/8/P7/8/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 2217 }},
			{ "selfStalemateB", "8/8/8/8/8/p7/8/k1K5 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 2217 }},
			{ "promotion www.rocechess.ch/perft.html", "n1n5/PPPk4/8/8/8/8/4Kppp/5N1N b - - 0 1", Colour.BLACK,
					new int[] {24, 496, 9483, 182838, 3605103 /* , 71179139 */ } },
			{ "promoteToGiveCheckW", "4k3/1P6/8/8/8/8/K7/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 217342 } },
			{ "promoteToGiveCheckB", "8/k7/8/8/8/8/1p6/4K3 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 217342 } },
	      { "underpromoteToGiveCheckW", "8/P1k5/K7/8/8/8/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, -1, -1, 92683 } },
			{ "underpromoteToGiveCheckB", "8/8/8/8/8/k7/p1K5/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, -1, -1, 92683 } },
			{ "doubleCheckW", "8/5k2/8/5N2/5Q2/2K5/8/8 w - - 0 1", Colour.WHITE, new int[] { -1, -1, -1, 23527 } },
			{ "doubleCheckB", "8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", Colour.BLACK, new int[] { -1, -1, -1, 23527 } }
		});
		// @formatter:on
	}

}
