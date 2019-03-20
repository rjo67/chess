package org.rjo.chess.position.check;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.rjo.chess.TestUtil;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;
import org.rjo.chess.position.Position;

/**
 * Test determination of whether the king is in check.
 *
 * @author rich
 */
public class KingCheckTest {
	private Game game;
	private BitSetUnifier[] whitePieces;
	private BitSetUnifier[] blackPieces;

	private void setup(String fen) {
		game = Fen.decode(fen);
		whitePieces = setupBitsets(game.getPosition(), Colour.WHITE);
		blackPieces = setupBitsets(game.getPosition(), Colour.BLACK);
	}

	/**
	 * moving the rook away from the file leaves the king in check
	 */
	@Test
	public void pinnedOnFileMovesAway() {
		setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
		Move move = new Move(PieceType.ROOK, Colour.WHITE, Square.d4, Square.c4);
		assertTrue(KingCheck.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
	}

	/**
	 * moving the pawn away from the diagonal leaves the king in check
	 */
	@Test
	public void pinnedOnDiagonalMovesAway() {
		setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
		assertTrue(KingCheck.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
	}

	/**
	 * moving the rook along from the file does not leave the king in check
	 */
	@Test
	public void pinnedOnFileStaysOnFile() {
		setup("3r4/4k3/6b1/8/3RP3/3K4/8/8 w - - 10 10");
		Move move = new Move(PieceType.ROOK, Colour.WHITE, Square.d4, Square.d5);
		assertFalse(KingCheck.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
	}

	/**
	 * capturing the checking piece does not leave the king in check
	 */
	@Test
	public void checkingPieceGetsCaptured() {
		setup("8/4k3/6b1/8/4P3/3K3r/8/5Q2 w - - 10 10");
		Move move = new Move(PieceType.QUEEN, Colour.WHITE, Square.f1, Square.h3, PieceType.ROOK);
		assertFalse(KingCheck.isKingInCheck(game.getPosition(), move, Colour.BLACK, Square.d3, false));
	}

	/**
	 * the 'friendlyPieces' bitset must not get changed by the call to
	 * {@link KingCheck#isKingInCheckAfterMove_PreviouslyNotInCheck(Square, Colour, BitSetUnifier, BitSetUnifier[], Move)}.
	 */
	@Test
	public void friendlyPiecesDoesNotGetChangedAfterMove() {
		setup("3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10");
		BitSetUnifier friendlyPieces = getWhitePieces(game.getPosition());
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE, friendlyPieces, blackPieces, move));
		assertEquals(friendlyPieces, getWhitePieces(game.getPosition()));
	}

	/**
	 * the 'enemyPieces' bitset must not get changed by the call to
	 * {@link KingCheck#isKingInCheckAfterMove_PreviouslyNotInCheck(Square, Colour, BitSetUnifier, BitSetUnifier[], Move)}.
	 */
	@Test
	public void enemyPiecesNotChangedAfterCaptureMove() {
		setup("3r4/4k3/8/r7/4P3/8/2Kb4/8 w - - 10 10");
		BitSetUnifier bishopBitSet = game.getPosition().getPieces(Colour.BLACK)[PieceType.BISHOP.ordinal()].getBitBoard().getBitSet();
		Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2, PieceType.BISHOP);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
		// should be same object
		assertSame(bishopBitSet, game.getPosition().getPieces(Colour.BLACK)[PieceType.BISHOP.ordinal()].getBitBoard().getBitSet());
	}

	//
	// ********* pawn moves
	//

	@Test
	public void checkAfterNonCaptureMove() {
		setup("3bq3/pp2k3/8/rn3b2/4P3/3K1Pr1/8/8 w - - 10 10");
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.e4, Square.e5);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	@Test
	public void pawnCaptureIntoCheck() {
		setup("3r4/4k3/8/r7/4q3/3P4/3K4/8 w - - 10 10");
		Move move = new Move(PieceType.PAWN, Colour.WHITE, Square.d3, Square.e4, PieceType.QUEEN);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	@Test
	public void enpassantCaptureIntoCheck() {
		setup("3r4/4k3/8/2pP4/8/3K4/8/8 w - - 10 10");
		Move move = Move.enpassant(Colour.WHITE, Square.d5, Square.c6);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d3, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	//
	// ********* king moves
	//

	@Test
	public void kingMovesIntoCheck() {
		setup("3r4/4k3/8/r7/4P3/8/2K5/8 w - - 10 10");
		Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	@Test
	public void kingCapturesIntoCheck() {
		setup("3r4/4k3/8/r7/4P3/8/2Kb4/8 w - - 10 10");
		Move move = new Move(PieceType.KING, Colour.WHITE, Square.c2, Square.d2, PieceType.BISHOP);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	@Test
	public void kingCastlesIntoCheck() {
		setup("8/6r1/8/8/8/k7/8/4K2R w K - 10 10");
		Move move = Move.castleKingsSide(Colour.WHITE);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.g1, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	//
	// ********* bishop moves
	//

	@Test
	public void discoveredCheck() {
		setup("8/4k3/8/b7/8/2BP4/3K4/8 w - - 10 10");
		Move move = new Move(PieceType.BISHOP, Colour.WHITE, Square.c3, Square.d4);
		assertTrue(KingCheck.isKingInCheckAfterMove_PreviouslyNotInCheck(Square.d2, Colour.WHITE, getWhitePieces(game.getPosition()),
				blackPieces, move));
	}

	@Test
	public void speed() {
		setup("3r4/R1b1k3/6b1/8/3RP3/3K4/8/8 b - - 10 10");
		int NBR_ITERATIONS = 50000;
		for (int q = 0; q < 10; q++) {
			var sw = StopWatch.createStarted();
			for (int i = 0; i < NBR_ITERATIONS; i++) {
				TestUtil.checkMoves(game.getPosition().findMoves(Colour.BLACK), "Rd8-e8", "Rd8-f8", "Rd8-g8", "Rd8-h8", "Rd8-d7", "Rd8-d6",
						"Rd8-d5", "Rd8xd4+", "Rd8-c8", "Rd8-b8", "Rd8-a8", "Bg6-h5", "Bg6-f5", "Bg6xe4+", "Bg6-h7", "Bg6-f7", "Bg6-e8", "Ke7-e6",
						"Ke7-f6", "Ke7-f7", "Ke7-e8", "Ke7-f8");
			}
			sw.stop();
			System.out.println(String.format("%d iterations in %d ms, %2.3f/iter", NBR_ITERATIONS, sw.getTime(),
					1.0 * sw.getTime() / NBR_ITERATIONS));
			// ~400ms
		}
	}

	private BitSetUnifier getWhitePieces(Position chessboard) {
		return chessboard.getAllPieces(Colour.WHITE).getBitSet();
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
