package org.rjo.chess.pieces;

import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.Move.CheckInformation;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.PositionInfo;

/**
 * The interface for a chess piece.
 *
 * @author rich
 */
public interface Piece extends Cloneable {

	Object clone() throws CloneNotSupportedException;

	/**
	 * @return the symbol for this piece.
	 */
	String getSymbol();

	/**
	 * Initialises data structures to the starting position of the pieces.
	 *
	 * @see #initPosition(Square...)
	 */
	void initPosition();

	/**
	 * Sets the start squares for this piece type to the parameter(s).
	 *
	 * @param requiredSquares all required squares.
	 */
	void initPosition(Square... requiredSquares);

	/**
	 * New version of findMoves.
	 * <p>
	 * Finds all possible moves for this piece type in the given position.
	 * <p>
	 * <b>Moves returned are legal. However, this method does not check to see if the _opponent_'s king is in check after
	 * the move.</b>
	 *
	 * @param position current position.
	 * @param kingInCheck indicates if the king is currently in check. This limits the available moves.
	 * @param posnInfo info about pieces checking the king, pinned pieces, etc. Normally all squares are allowed. If the
	 *           king is in check then this object contains the squares which will potentially get out of check.
	 * @return a list of all possible moves.
	 */
	List<Move> findMoves(Position position,
			CheckInformation kingInCheck,
			PositionInfo posnInfo);

	/**
	 * After the move, is the opponent's king in check. <b>DOES NOT look for discovered checks.</b>
	 *
	 * @param move the move
	 * @param pieces all my pieces (is required by the pawn implementation)
	 * @param opponentsKing where the opponent's king is
	 * @param checkingBitboards the bitboards of squares which check the opponent's king [0]=rook,[1]=bishop
	 * @return true if the opponent's king is in check as a direct result of this move (disregarding any possible discovered
	 *         checks)
	 */
	boolean doesMoveLeaveOpponentInCheck(Move move,
			Piece[] pieces,
			Square opponentsKing,
			BitBoard[] checkingBitboards);

	/**
	 * Checks to see if the given square is attacked by one or more pieces of this piece type.
	 *
	 * @param emptySquares empty square bitset
	 * @param targetSq the square to check.
	 * @param checkCache check cache
	 * @return true if it is attacked, otherwise false.
	 */
	boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache);

	/**
	 * Checks to see if the given square is attacked by one or more pieces of this piece type. <B>without check cache --
	 * usually just for tests</b>.
	 *
	 * @param emptySquares empty square bitset
	 * @param targetSq the square to check.
	 * @return true if it is attacked, otherwise false.
	 */
	boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq);

	/**
	 * Carries out the move for this piece type, i.e. updates internal structures. More complicated situations e.g.
	 * promotions, captures are dealt with in {@link Position#move(Move)}.
	 *
	 * @param move the move to make
	 */
	void move(Move move);

	/**
	 * Removes the captured piece in a capture move from the internal data structures for that piece type.
	 *
	 * @param square from where to remove the piece
	 */
	void removePiece(Square square);

	/**
	 * Adds a piece to the internal data structures at the given square. Mainly for promotions. No error checking is
	 * performed here.
	 *
	 * @param square where to add the piece
	 */
	void addPiece(Square square);

	/**
	 * @return the colour of the piece.
	 */
	Colour getColour();

	BitBoard getBitBoard();

	/**
	 * Returns all the squares currently occupied by this piece type.
	 *
	 * @return the squares currently occupied by this piece type
	 */
	Square[] getLocations();

	/**
	 * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
	 *
	 * @return the FEN symbol for this piece.
	 */
	String getFenSymbol();

	PieceType getType();

	/**
	 * Returns true if this piece type is present on the given square.
	 *
	 * @param targetSquare square of interest.
	 * @return true if this piece type is present, otherwise false.
	 */
	boolean pieceAt(Square targetSquare);

	/**
	 * Calculates the piece-square value in centipawns. For each piece, its piece_value is added to the square_value of the
	 * square where it currently is.
	 *
	 * @return the piece-square value in centipawns (for all pieces of this type).
	 */
	int calculatePieceSquareValue();

	/**
	 * returns the number of pieces of this type still on the board.
	 *
	 * @return number of pieces of this type.
	 */
	int numberOfPieces();

}
