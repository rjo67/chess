package org.rjo.chess.position.check;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.check.BoardInfo.PieceInfo;

/**
 * Test determination of whether pieces are pinned.
 *
 * @author rich
 */
public class KingPinTest {
	private Game game;
	private BitSetUnifier[] whitePieces;
	private BitSetUnifier[] blackPieces;

	private void setup(String fen) {
		game = Fen.decode(fen);
		whitePieces = setupBitsets(game.getPosition(), Colour.WHITE);
		blackPieces = setupBitsets(game.getPosition(), Colour.BLACK);
	}

	/**
	 * Calling the isKingInCheck method directly and inspecting the results.
	 */
	@Test
	public void simplePins() {
		var data = Arrays.asList(new Object[][] {
				{ "8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10", Square.d2, Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.NORTHWEST, PieceType.BISHOP, Square.c3) } },
				{ "3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10", Square.d3, Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.NORTHEAST, PieceType.PAWN, Square.e4),
								new PieceInfo(RayType.EAST, PieceType.PAWN, Square.f3) } },
				{ "3r4/4k3/6b1/3N1P2/8/q1RK4/8/8 w - - 10 10", Square.d3, Colour.WHITE,
						new PieceInfo[] { new PieceInfo(RayType.WEST, PieceType.ROOK, Square.c3),
								new PieceInfo(RayType.NORTH, PieceType.KNIGHT, Square.d5),
								new PieceInfo(RayType.NORTHEAST, PieceType.PAWN, Square.f5) } }, });

		for (Object[] d : data) {
			setup((String) d[0]);
			var pins = KingCheck.isKingInCheck((Square) d[1], (Colour) d[2], getWhitePieces(game.getPosition()),
					whitePieces, blackPieces, null, true).getPinInfo();
			if (d[3] != null) {
				var expectedChecks = ((PieceInfo[]) d[3]).length;
				assertEquals(expectedChecks, pins.size(), String.format("bad nbr of pins for posn %s", d[0]));
				for (PieceInfo expectedPin : (PieceInfo[]) d[3]) {
					Optional<PieceInfo> result = pins.stream().filter(c -> c.equals(expectedPin)).findFirst();
					assertFalse(result.isEmpty(), String.format("expected pin: %s, got %s", expectedPin, pins));
				}
			}
		}
	}

	private BitSetUnifier getWhitePieces(Position chessboard) {
		return chessboard.getAllPieces(Colour.WHITE).getBitSet();
	}

	private BitSetUnifier[] setupBitsets(Position posn, Colour colour) {
		BitSetUnifier[] pieces = new BitSetUnifier[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			pieces[type.ordinal()] = posn.getPieces(colour)[type.ordinal()].getBitBoard().getBitSet();
		}
		return pieces;
	}
}
