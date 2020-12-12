package org.rjo.chess.pieces;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.bits.BitSetUnifier;

/**
 * @author rich
 * @since 2016-10-18
 */
public class PieceManager {
	/**
	 * Stores the pieces in the game. The first dimension indicates the colour
	 * {white, black}. The second dimension corresponds to
	 * {@link PieceType#ALL_PIECE_TYPES}.
	 */
	private Piece[][] pieces;

	/**
	 * whether this piece type has already been cloned. Dimensions as for
	 * {@link #pieces}.
	 */
	private boolean[][] alreadyCloned;

	/**
	 * Constructor. The pieces data structure will be initialised to null values.
	 */
	private PieceManager() {
		pieces = new Piece[2][PieceType.ALL_PIECE_TYPES.length];
		alreadyCloned = new boolean[2][PieceType.ALL_PIECE_TYPES.length]; // set to default FALSE
	}

	/**
	 * Constructor. The pieces map will be initialised to the values of the
	 * parameters.
	 *
	 * @param whitePieces the white pieces
	 * @param blackPieces the black pieces
	 */
	public PieceManager(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		this();
		whitePieces.forEach(p -> pieces[Colour.WHITE.ordinal()][p.getType().ordinal()] = p);
		blackPieces.forEach(p -> pieces[Colour.BLACK.ordinal()][p.getType().ordinal()] = p);
	}

	/**
	 * Copy constructor. The new object references the same <code>pieces</code> as
	 * before. Need to clone iff these objects get changed.
	 *
	 * @param otherPieceManager the pieceManager that gets copied
	 */
	public PieceManager(final PieceManager otherPieceManager) {
		this();
		for (Colour col : Colour.ALL_COLOURS) {
			pieces[col.ordinal()] = Arrays.copyOf(otherPieceManager.pieces[col.ordinal()],
					pieces[col.ordinal()].length);
		}
	}

	/**
	 * returns a particular Piece object from the map. The Piece object <b>will be
	 * cloned</b> and re-inserted into the <code>pieces</code> hashmap the first
	 * time.
	 *
	 * @param colour    the required colour
	 * @param pieceType the required piece type
	 * @return a Piece object
	 */
	public Piece getClonedPiece(Colour colour, PieceType pieceType) {
		if (alreadyCloned[colour.ordinal()][pieceType.ordinal()]) {
			return getPiece(colour, pieceType);
		}
		try {
			Piece cloned = (Piece) getPiece(colour, pieceType).clone();
			pieces[colour.ordinal()][pieceType.ordinal()] = cloned;
			alreadyCloned[colour.ordinal()][pieceType.ordinal()] = true;
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("could not clone piece!?");
		}
	}

	/**
	 * returns the pieces for the given colour.
	 *
	 * @param colour the required colour
	 * @return all pieces for the given colour
	 */
	public Piece[] getPiecesForColour(Colour colour) {
		return pieces[colour.ordinal()];
	}

	/**
	 * Returns an array of all the bitsets of all of the pieces for the given
	 * colour.
	 *
	 * @param colour the required colour
	 * @return an array containing all the bitsets of all of the pieces
	 */
	public BitSetUnifier[] setupBitsets(Colour colour) {
		BitSetUnifier[] piecesBitsets = new BitSetUnifier[PieceType.ALL_PIECE_TYPES.length];
		for (PieceType type : PieceType.ALL_PIECE_TYPES) {
			piecesBitsets[type.ordinal()] = pieces[colour.ordinal()][type.ordinal()].getBitBoard().getBitSet();
		}
		return piecesBitsets;
	}

	/**
	 * returns a particular Piece object.
	 *
	 * @param colour    the required colour
	 * @param pieceType the required piece type
	 * @return a Piece object
	 */
	public Piece getPiece(Colour colour, PieceType pieceType) {
		return pieces[colour.ordinal()][pieceType.ordinal()];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(600);
		sb.append("PieceManager@").append(Integer.toHexString(System.identityHashCode(this))).append("[");
		for (Colour col : Colour.ALL_COLOURS) {
			sb.append(Arrays.stream(PieceType.ALL_PIECE_TYPES).map(pt -> pieces[col.ordinal()][pt.ordinal()].toString())
					.collect(Collectors.joining(",", "{", "}")));
		}
		sb.append("]");
		return sb.toString();
	}
}
