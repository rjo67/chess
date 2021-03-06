package org.rjo.chess.pieces;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Position;

/**
 * @author rich
 * @since 2016-10-18
 */
public class PieceManagerTest {

	@Test
	public void pieceMgrWhitePawnMove() {
		Position p = Position.startPosition();
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.a2, Square.a4);
		Position p2 = p.move(move);

		// System.out.println(p.getPieceManager());
		// System.out.println("----");
		// System.out.println(p2.getPieceManager());

		checkPieceObjects(p, p2, new HashSet<>(Arrays.asList(PieceType.PAWN)), new HashSet<>());
	}

	@Test
	public void pieceMgrBlackKnightMove() {
		Position p = new Position(Colour.BLACK);
		Move move = new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8, Square.a6);
		Position p2 = p.move(move);

		checkPieceObjects(p, p2, new HashSet<>(), new HashSet<>(Arrays.asList(PieceType.KNIGHT)));
	}

	@Test
	public void pieceMgrWhiteBishopCapture() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();
		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.d4, Square.g7, PieceType.PAWN);
		Position p2 = p.move(move);

		checkPieceObjects(p, p2, new HashSet<>(Arrays.asList(PieceType.BISHOP)),
				new HashSet<>(Arrays.asList(PieceType.PAWN)));
	}

	@Test
	public void pieceMgrWhiteCastling() {
		Position p = Fen.decode("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 0").getPosition();
		Move move = Move.castleKingsSide(Colour.WHITE);
		Position p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<>(Arrays.asList(PieceType.KING, PieceType.ROOK)), new HashSet<>());

		move = Move.castleQueensSide(Colour.WHITE);
		p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<>(Arrays.asList(PieceType.KING, PieceType.ROOK)), new HashSet<>());
	}

	@Test
	public void pieceMgrBlackCastling() {
		Position p = Fen.decode("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 0").getPosition();
		Move move = Move.castleKingsSide(Colour.BLACK);
		Position p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<>(), new HashSet<>(Arrays.asList(PieceType.KING, PieceType.ROOK)));

		move = Move.castleQueensSide(Colour.BLACK);
		p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<>(), new HashSet<>(Arrays.asList(PieceType.KING, PieceType.ROOK)));
	}

	@Test
	public void pieceManagerSpeedTest() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();
		PieceManager pm = p.getPieceManager();

		StopWatch sw = new StopWatch();
		sw.start();
		for (int i = 0; i < 1000000; i++) {
			@SuppressWarnings("unused")
			PieceManager pm2 = new PieceManager(pm);
		}
		System.out.println("pieceManagerSpeedTest: " + sw.getTime());
	}

	@Test
	public void pieceManagerToString() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();
		System.out.println(p.getPieceManager().toString());
	}

	/**
	 * helper method to check that the objects stored in Position.pieceMgr get
	 * cloned as required after a move.
	 *
	 * @param before              previous position
	 * @param after               position after move
	 * @param allowedChangesWhite which white pieces should have been cloned
	 * @param allowedChangesBlack which black pieces should have been cloned
	 */
	private void checkPieceObjects(Position before, Position after, Set<PieceType> allowedChangesWhite,
			Set<PieceType> allowedChangesBlack) {
		Piece[] whitePiecesBefore = before.getPieceManager().getPiecesForColour(Colour.WHITE);
		Piece[] whitePiecesAfter = after.getPieceManager().getPiecesForColour(Colour.WHITE);
		Piece[] blackPiecesBefore = before.getPieceManager().getPiecesForColour(Colour.BLACK);
		Piece[] blackPiecesAfter = after.getPieceManager().getPiecesForColour(Colour.BLACK);

		for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
			if (System.identityHashCode(whitePiecesBefore[pt.ordinal()]) != System
					.identityHashCode(whitePiecesAfter[pt.ordinal()])) {
				if (!allowedChangesWhite.contains(pt)) {
					fail("white " + pt + " changed incorrectly");
				}
			} else {
				if (allowedChangesWhite.contains(pt)) {
					fail("white " + pt + " NOT changed as expected");
				}
			}
			if (System.identityHashCode(blackPiecesBefore[pt.ordinal()]) != System
					.identityHashCode(blackPiecesAfter[pt.ordinal()])) {
				if (!allowedChangesBlack.contains(pt)) {
					fail("black " + pt + " changed incorrectly");
				}
			} else {
				if (allowedChangesBlack.contains(pt)) {
					fail("black " + pt + " NOT changed as expected");
				}
			}
		}
	}

}
