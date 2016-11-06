package org.rjo.chess.pieces;

import java.util.BitSet;
import java.util.List;

import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;

/**
 * Stores the type and colour of a piece.
 *
 * @author rich
 * @since 2016-10-20
 */
/**
 * @author rich
 * @since 2016-10-21
 */
public abstract class AbstractPiece implements Piece {

	// type of this piece
	private PieceType type;

	// stores the colour of the piece
	private Colour colour;

	protected AbstractPiece(Colour colour, PieceType type) {
		this.colour = colour;
		this.type = type;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return the symbol for this piece.
	 */
	@Override
	public String getSymbol() {
		return type.getSymbol();
	}

	/**
	 * Finds all possible moves for this piece type in the given position. Delegates to {@link #findMoves(Game, boolean)} with 2nd parameter FALSE.
	 *
	 * @param position current game state.
	 * @return a list of all possible moves.
	 */
	@Override
	public final List<Move> findMoves(Position position) {
		return findMoves(position, false);
	}

	@Override
	public Colour getColour() {
		return colour;
	}

	/**
	 * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
	 *
	 * @return the FEN symbol for this piece.
	 */
	@Override
	public String getFenSymbol() {
		return type.getFenSymbol(colour);
	}

	@Override
	public String toString() {
		return colour.toString() + "-" + type.toString() + "@" + Integer.toHexString(System.identityHashCode(this));
	}

	@Override
	public PieceType getType() {
		return type;
	}

	@Override
	public final boolean attacksSquare(BitSet emptySquares, Square targetSq) {
		return attacksSquare(emptySquares, targetSq, new SquareCache<>());
	}

	/**
	 * A simple cache to map values to squares.
	 */
	public static class SquareCache<T> {
		// can't create generic array
		private Object[] cache = new Object[Square.values().length];

		final public T lookup(Square square) {
			return lookup(square.bitIndex());
		}

		@SuppressWarnings("unchecked")
		public T lookup(int squareBitIndex) {
			return (T) cache[squareBitIndex];
		}

		final public void store(Square square, T value) {
			store(square.bitIndex(), value);
		}

		public void store(int squareBitIndex, T value) {
			cache[squareBitIndex] = value;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(100);
			for (Square sq : Square.values()) {
				if (this.lookup(sq) != null) {
					sb.append("(" + sq + ":" + this.lookup(sq) + ")");
				}
			}
			return sb.toString();
		}
	}
}
