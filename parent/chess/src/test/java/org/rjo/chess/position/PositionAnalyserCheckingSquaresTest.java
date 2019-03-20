package org.rjo.chess.position;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;

/**
 * Tests for {@link PositionAnalyser}.
 *
 * @author rich
 */
@RunWith(Parameterized.class)
public class PositionAnalyserCheckingSquaresTest {
	private String fenString;
	private Colour kingsColour;
	private Square[] expectedRookChecks;
	private Square[] expectedBishopChecks;

	public PositionAnalyserCheckingSquaresTest(String fenString,
			Colour kingsColour,
			Square[] expectedRookChecks,
			Square[] expectedBishopChecks) {
		this.fenString = fenString;
		this.kingsColour = kingsColour;
		this.expectedRookChecks = expectedRookChecks;
		this.expectedBishopChecks = expectedBishopChecks;
	}

	/**
	 * Test PositionAnalyser::analysePosition for checks.
	 */
	@Test
	public void findCheckingSquares() {
		var game = Fen.decode(fenString);
		var kingsSquare = game.getPosition().getKingPosition(kingsColour);
		var friendlyPieces = setupBitsets(game.getPosition(), kingsColour);
		var enemyPieces = setupBitsets(game.getPosition(), kingsColour.oppositeColour());
		BitBoard[] outputBitboards = PositionAnalyser.findCheckingSquares(kingsSquare,
				PositionAnalyser.createBitboardContainingAllPieces(friendlyPieces),
				PositionAnalyser.createBitboardContainingAllPieces(enemyPieces));
		BitSetUnifier allSquares = (BitSetUnifier) outputBitboards[0].getBitSet().clone();
		allSquares.or(outputBitboards[1].getBitSet());
		BitBoard allSquaresBB = new BitBoard(allSquares);
		for (Square expectedCheck : expectedRookChecks) {
			assertTrue(String.format("square %s is not rook-check\n%schecks found:\n%s)", expectedCheck,
					game.getPosition(),
					allSquaresBB),
					outputBitboards[0].get(expectedCheck));
		}
		for (Square expectedCheck : expectedBishopChecks) {
			assertTrue(String.format("square %s is not bishop-check\n%schecks found:\n%s", expectedCheck,
					game.getPosition(),
					allSquaresBB),
					outputBitboards[1].get(expectedCheck));
		}
	}

	/**
	 * Data for the tests. Format is:
	 * <ul>
	 * <li>FEN</li>
	 * <li>king's colour</li>
	 * <li>squares where a rook or queen would check the king</li>
	 * <li>squares where a bishop or queen would check the king</li>
	 * </ul>
	 */
	@Parameters(name = "data({0})")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10", Colour.WHITE, new Square[] { Square.b2, Square.d1 }, new Square[] { Square.e3 } },
				{ "3bq3/pp2k3/8/rN3b2/4P3/3K1Pr1/8/8 w - - 10 10", Colour.WHITE,
						new Square[] { Square.d7, Square.d6, Square.d5, Square.d4, Square.a3, Square.b3, Square.e3, Square.f3, Square.d1 },
						new Square[] { Square.f1, Square.e2, Square.e4, Square.c4, Square.b5 } },
				{ "8/4k3/8/8/2P5/3K4/8/8 w - - 10 10", Colour.WHITE,
						new Square[] { Square.a3, Square.b3, Square.e3, Square.f3 },
						new Square[] { Square.b1, Square.c2, Square.c4, Square.h7 } },
				{ "8/p7/8/1P6/K1k3p1/6P1/7P/8 w - - 0 10", Colour.WHITE,
						new Square[] { Square.a1, Square.a2, Square.a3, Square.a5, Square.a6, Square.b4 },
						new Square[] { Square.b3, Square.c2, Square.d1 } },
				{ "r3k2r/pb3p2/5npp/n2p4/1p1PPB2/6P1/P2N1PBP/R3K2R b KQkq - 0 10", Colour.BLACK,
						new Square[] { Square.h8, Square.g8, Square.f8, Square.d8, Square.c8, Square.b8, Square.a8, Square.e7, Square.e6,
								Square.e5 },
						new Square[] { Square.d7, Square.c6, Square.b5, Square.a4 } },
				{ "8/k1P5/p7/1K6/8/8/8/8 b - - 0 1", Colour.BLACK,
						new Square[] { Square.a8, Square.a6, Square.b7 },
						new Square[] { Square.b8, Square.b6, Square.c5, Square.d4, Square.e3, Square.f2, Square.g1 } },
				{ "r3kb2/pppppppp/8/8/8/8/K7/8 b - - 0 1", Colour.BLACK,
						new Square[] { Square.d8, Square.c8, Square.b8, Square.a8, },
						new Square[] {} },
		});
	}

	private BitSetUnifier[] setupBitsets(Position posn,
			Colour colour) {
		BitSetUnifier[] pieces = new BitSetUnifier[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			pieces[type.ordinal()] = posn.getPieces(colour)[type.ordinal()].getBitBoard().getBitSet();
		}
		return pieces;
	}
}
