package org.rjo.chess;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.BiPredicate;

import org.junit.Test;
import org.rjo.chess.pieces.PieceType;
import org.rjo.chess.ray.RayType;

/**
 * Tests of {@link PositionCheckState}.
 *
 * @author rich
 * @since 2017-08-06
 */
public class PositionCheckStateTest {

	private PositionCheckState state;

	private void assertSquareHasCorrectStatus(Square sq,
			RayType rayType,
			BiPredicate<Square, RayType> checkfn) {
		assertTrue("square " + sq + " has incorrect status: " + state.getCheckState(sq.bitIndex(), rayType) + "\n" + state,
				checkfn.test(sq, rayType));
	}

	/**
	 * after white's moves have been evaluated, checks that the correct squares are marked as 'check'.
	 */
	@Test
	public void simple() {
		Position posn = Fen.decode("5k2/8/p3p3/R7/3Q2n1/8/K7/8 w - - 2 4").getPosition();
		posn.findMoves(Colour.WHITE);
		Position posn2 = posn.move(new Move(PieceType.QUEEN, Colour.WHITE, Square.d4, Square.f4, null, true));
		state = posn2.getCheckState()[Colour.WHITE.ordinal()];

		// are correct moves listed?
		Arrays.stream(new Square[] { Square.f4, Square.f5, Square.f6, Square.f7 })
				.forEach(sq -> assertSquareHasCorrectStatus(sq, RayType.NORTH, (square,
						raytype) -> state.squareHasCheckStatus(square, raytype)));

		// now make black move and check if state is correctly updated
		Position posn3 = posn2.move(new Move(PieceType.KNIGHT, Colour.BLACK, Square.g4, Square.f6));
		state = posn3.getCheckState()[Colour.WHITE.ordinal()];
		Arrays.stream(new Square[] { Square.f2, Square.f3, Square.f4, Square.f5 })
				.forEach(sq -> assertSquareHasCorrectStatus(sq, RayType.NORTH, (square,
						raytype) -> !state.squareHasCheckStatus(square, raytype)));
		Arrays.stream(new Square[] { Square.f6 }).forEach(sq -> assertSquareHasCorrectStatus(sq, RayType.NORTH, (square,
				raytype) -> state.squareHasCheckIfCaptureStatus(sq, raytype)));
	}

	/**
	 * after white's moves have been evaluated, checks that the correct squares are marked as 'check'.
	 */
	@Test
	public void promotion() {
		Position posn = Fen.decode("5k2/N1P5/pp2p3/8/8/8/8/K7 w - - 2 4").getPosition();
		posn.findMoves(Colour.WHITE);
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.c7, Square.c8, null, true);
		move.setPromotionPiece(PieceType.ROOK);
		Position posn2 = posn.move(move);
		state = posn2.getCheckState()[Colour.WHITE.ordinal()];
		// are correct moves listed?
		Arrays.stream(new Square[] { Square.c8, Square.d8, Square.e8 }).forEach(sq -> assertSquareHasCorrectStatus(sq, RayType.EAST, (square,
				raytype) -> state.squareHasCheckStatus(square, raytype)));
		// now make black move and check if state is correctly updated
	}

}
