package org.rjo.chess.pieces;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.util.Stopwatch;

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

		checkPieceObjects(p, p2, new HashSet<PieceType>(Arrays.asList(PieceType.PAWN)), new HashSet<PieceType>());
	}

	@Test
	public void pieceMgrBlackKnightMove() {
		Position p = Position.startPosition();
		p.setSideToMove(Colour.BLACK);
		Move move = new Move(PieceType.KNIGHT, Colour.BLACK, Square.b8, Square.a6);
		Position p2 = p.move(move);

		checkPieceObjects(p, p2, new HashSet<PieceType>(), new HashSet<PieceType>(Arrays.asList(PieceType.KNIGHT)));
	}

	@Test
	public void pieceMgrWhiteBishopCapture() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();
		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.d4, Square.g7, PieceType.PAWN);
		Position p2 = p.move(move);

		checkPieceObjects(p, p2, new HashSet<PieceType>(Arrays.asList(PieceType.BISHOP)),
				new HashSet<PieceType>(Arrays.asList(PieceType.PAWN)));
	}

	@Test
	public void pieceMgrWhiteCastling() {
		Position p = Fen.decode("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 0").getPosition();
		Move move = Move.castleKingsSide(Colour.WHITE);
		Position p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<PieceType>(Arrays.asList(PieceType.KING, PieceType.ROOK)),
				new HashSet<PieceType>());

		move = Move.castleQueensSide(Colour.WHITE);
		p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<PieceType>(Arrays.asList(PieceType.KING, PieceType.ROOK)),
				new HashSet<PieceType>());
	}

	@Test
	public void pieceMgrBlackCastling() {
		Position p = Fen.decode("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 0").getPosition();
		Move move = Move.castleKingsSide(Colour.BLACK);
		Position p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<PieceType>(),
				new HashSet<PieceType>(Arrays.asList(PieceType.KING, PieceType.ROOK)));

		move = Move.castleQueensSide(Colour.BLACK);
		p2 = p.move(move);
		checkPieceObjects(p, p2, new HashSet<PieceType>(),
				new HashSet<PieceType>(Arrays.asList(PieceType.KING, PieceType.ROOK)));
	}

	@Test
	public void pieceManagerSpeedTest() {
		Position p = Fen.decode("4k3/6p1/8/8/3B4/8/8/4K3 w - - 0 0").getPosition();
		PieceManager pm = p.getPieceManager();

		Stopwatch sw = new Stopwatch();
		sw.start();
		for (int i = 0; i < 1000000; i++) {
			PieceManager pm2 = new PieceManager(pm);
		}
		System.out.println(sw.read());
	}

	/**
	 * helper method to check that the objects stored in Position.pieceMgr get cloned as required
	 * after a move.
	 * 
	 * @param before previous position
	 * @param after position after move
	 * @param allowedChangesWhite which white pieces should have been cloned
	 * @param allowedChangesBlack which black pieces should have been cloned
	 */
	private void checkPieceObjects(Position before, Position after, Set<PieceType> allowedChangesWhite,
			Set<PieceType> allowedChangesBlack) {
		Piece[] whitePiecesBefore = before.getPieceManager().getPiecesForColour2(Colour.WHITE);
		Piece[] whitePiecesAfter = after.getPieceManager().getPiecesForColour2(Colour.WHITE);
		Piece[] blackPiecesBefore = before.getPieceManager().getPiecesForColour2(Colour.BLACK);
		Piece[] blackPiecesAfter = after.getPieceManager().getPiecesForColour2(Colour.BLACK);

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
