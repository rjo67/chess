package org.rjo.chess.position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.position.PositionInfo.PieceInfo;

/**
 * Tests for {@link PositionAnalyser}.
 *
 * @author rich
 */
public class PositionAnalyserTest {

	/**
	 * Test PositionAnalyser::analysePosition for checks.
	 */
	@ParameterizedTest
	@MethodSource("data")
	public void simpleChecks(String fenString, Colour kingsColour, PieceInfo[] expectedChecks,
			PieceInfo[] expectedPins) {
		var game = Fen.decode(fenString);
		var friendlyPieces = setupBitsets(game.getPosition(), kingsColour);
		var enemyPieces = setupBitsets(game.getPosition(), kingsColour.oppositeColour());
		var kingsSquare = game.getPosition().getKingPosition(kingsColour);
		var posnInfo = PositionAnalyser.analysePosition(kingsSquare, kingsColour,
				getAllPieces(game.getPosition(), kingsColour), friendlyPieces, enemyPieces, null, true);
		var pins = posnInfo.getPinnedPieces();
		if (expectedChecks != null) {
			var checks = posnInfo.getCheckers();
			var nbrExpectedChecks = expectedChecks.length;
			assertEquals(nbrExpectedChecks, checks.size(), "bad nbr of checks");
			for (PieceInfo expectedCheck : expectedChecks) {
				Optional<PieceInfo> result = checks.stream().filter(c -> c.equals(expectedCheck)).findFirst();
				assertFalse(result.isEmpty(), String.format("expected check: %s, got %s", expectedCheck, checks));
			}
		}
		if (expectedPins != null) {
			var nbrExpectedPins = expectedPins.length;
			assertEquals(nbrExpectedPins, pins.size(), "bad nbr of pins");
			for (PieceInfo expectedPin : expectedPins) {
				Optional<PieceInfo> result = pins.stream().filter(c -> c.equals(expectedPin)).findFirst();
				assertFalse(result.isEmpty(), String.format("expected pin: %s, got %s", expectedPin, pins));
			}
		}
	}

	/**
	 * Data for the tests. Format is:
	 * <ul>
	 * <li>FEN</li>
	 * <li>king's colour</li>
	 * <li>pieces checking king</li>
	 * <li>pinned pieces</li>
	 * </ul>
	 */
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				{ "8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10", Colour.WHITE, new PieceInfo[] {}, null },
				{ "3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10", Colour.WHITE, new PieceInfo[] {}, null },
				{ "8/4k3/8/8/2p5/3K4/8/8 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(null, PieceType.PAWN, Square.c4) }, null },
				{ "8/4k3/8/1b6/4P3/3K4/8/8 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.NORTHWEST, PieceType.BISHOP, Square.b5) }, null },
				{ "8/4k3/8/8/4P3/3K3q/8/6q1 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.EAST, PieceType.QUEEN, Square.h3) }, null },
				{ "8/4k3/8/8/4P3/3K4/8/5q2 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.SOUTHEAST, PieceType.QUEEN, Square.f1) }, null },
				{ "3r4/4k3/8/r7/4P3/3K4/8/8 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.NORTH, PieceType.ROOK, Square.d8) }, null },
				{ "8/4k3/8/8/4P3/3K4/8/4n3 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(null, PieceType.KNIGHT, Square.e1) }, null },
				{ "8/4k3/b7/8/4P3/3K4/8/4n3 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(null, PieceType.KNIGHT, Square.e1),
								new PieceInfo(RayType.NORTHWEST, PieceType.BISHOP, Square.a6) },
						null },
				{ "8/4k3/8/8/4P3/3K2q1/8/4n3 w - - 10 10", Colour.WHITE,
						new PieceInfo[] { new PieceInfo(null, PieceType.KNIGHT, Square.e1),
								new PieceInfo(RayType.EAST, PieceType.QUEEN, Square.g3) },
						null },

				// ************ ab hier pins

				{ "8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10", Colour.WHITE, null,
						new PieceInfo[] { new PieceInfo(RayType.NORTHWEST, PieceType.BISHOP, Square.c3) } },
				{ "3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10", Colour.WHITE, null,
						new PieceInfo[] { new PieceInfo(RayType.NORTHEAST, PieceType.PAWN, Square.e4),
								new PieceInfo(RayType.EAST, PieceType.PAWN, Square.f3) } },
				{ "3r4/4k3/6b1/3N1P2/8/q1RK4/8/8 w - - 10 10", Colour.WHITE, null,
						new PieceInfo[] { new PieceInfo(RayType.WEST, PieceType.ROOK, Square.c3),
								new PieceInfo(RayType.NORTH, PieceType.KNIGHT, Square.d5),
								new PieceInfo(RayType.NORTHEAST, PieceType.PAWN, Square.f5) } }, });
	}

	private BitSetUnifier getAllPieces(Position posn, Colour colour) {
		return posn.getAllPieces(colour).getBitSet();
	}

	private BitSetUnifier[] setupBitsets(Position posn, Colour colour) {
		BitSetUnifier[] pieces = new BitSetUnifier[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			pieces[type.ordinal()] = posn.getPieces(colour)[type.ordinal()].getBitBoard().getBitSet();
		}
		return pieces;
	}
}
