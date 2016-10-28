package org.rjo.chess.pieces;

import java.util.BitSet;
import java.util.List;

import org.rjo.chess.BitBoard;
import org.rjo.chess.CheckStates;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.pieces.AbstractPiece.SquareCache;

/**
 * The interface for a chess piece.
 *
 * @author rich
 */
public interface Piece extends Cloneable {

	public Object clone() throws CloneNotSupportedException;

	/**
	 * @return the symbol for this piece.
	 */
	public String getSymbol();

	/**
	 * Initialises data structures to the starting position of the pieces.
	 *
	 * @see #initPosition(Square...)
	 */
	public void initPosition();

	/**
	 * Sets the start squares for this piece type to the parameter(s).
	 *
	 * @param requiredSquares all required squares.
	 */
	public void initPosition(Square... requiredSquares);

	/**
	 * Finds all possible moves for this piece type in the given position. Delegates to {@link #findMoves(Game, boolean)} with 2nd parameter FALSE.
	 * <b>Moves returned are legal. However, this method does not check to see if the opponent's king is in check after the move.</b>
	 *
	 * @param position current game state.
	 * @return a list of all possible moves.
	 */
	public List<Move> findMoves(Position position);

	/**
	 * Finds all possible moves for this piece type in the given position. <b>Moves returned are legal. However, this method does not check to see if
	 * the opponent's king is in check after the move.</b>
	 *
	 * @param position current position.
	 * @param kingInCheck indicates if the king is currently in check. This limits the available moves.
	 * @return a list of all possible moves.
	 */
	public List<Move> findMoves(Position position, boolean kingInCheck);

	/**
	 * Does the given move leave the opponent's king in check?
	 * <p>
	 * Currently two parts to this:<br>
	 * (a) does the piece check the king at square move.to()?<br>
	 * (b) is there a discovered check having vacated the square move.from()?
	 *
	 * @param position current position
	 * @param move current move
	 * @param opponentsKing location of opponent's king
	 * @param emptySquares bitset of empty squares (passed in as optimization)
	 * @param checkCache cache for checks
	 * @param discoveredCheckCache cache for discovered checks
	 * @return true when the move leaves the opponent's king in check
	 */
	public boolean isOpponentsKingInCheckAfterMove(Position position, Move move, Square opponentsKing, BitSet emptySquares,
			SquareCache<CheckStates> checkCache, SquareCache<Boolean> discoveredCheckCache);

	/**
	 * Checks to see if the given square is attacked by one or more pieces of this piece type.
	 *
	 * @param emptySquares empty square bitset
	 * @param targetSq the square to check.
	 * @param checkCache check cache
	 * @return true if it is attacked, otherwise false.
	 */
	public boolean attacksSquare(BitSet emptySquares, Square targetSq, SquareCache<CheckStates> checkCache);

	/**
	 * Checks to see if the given square is attacked by one or more pieces of this piece type. <B>without check cache -- usually just for tests</b>.
	 *
	 * @param emptySquares empty square bitset
	 * @param targetSq the square to check.
	 * @return true if it is attacked, otherwise false.
	 */
	public boolean attacksSquare(BitSet emptySquares, Square targetSq);

	/**
	 * Carries out the move for this piece type, i.e. updates internal structures. More complicated situations e.g. promotions, captures are dealt with
	 * in {@link Game#move(Move)}.
	 *
	 * @param move the move to make
	 */
	public void move(Move move);

	/**
	 * Removes the captured piece in a capture move from the internal data structures for that piece type.
	 *
	 * @param square from where to remove the piece
	 */
	public void removePiece(Square square);

	/**
	 * Adds a piece to the internal data structures at the given square. Mainly for promotions. No error checking is performed here.
	 *
	 * @param square where to add the piece
	 */
	public void addPiece(Square square);

	/**
	 * @return the colour of the piece.
	 */
	public Colour getColour();

	public BitBoard getBitBoard();

	/**
	 * Returns all the squares currently occupied by this piece type.
	 *
	 * @return the squares currently occupied by this piece type
	 */
	public Square[] getLocations();

	/**
	 * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
	 *
	 * @return the FEN symbol for this piece.
	 */
	public String getFenSymbol();

	public PieceType getType();

	/**
	 * Returns true if this piece type is present on the given square.
	 *
	 * @param targetSquare square of interest.
	 * @return true if this piece type is present, otherwise false.
	 */
	public boolean pieceAt(Square targetSquare);

	/**
	 * Calculates the piece-square value in centipawns. For each piece, its piece_value is added to the square_value of the square where it currently
	 * is.
	 *
	 * @return the piece-square value in centipawns (for all pieces of this type).
	 */
	public int calculatePieceSquareValue();

}
