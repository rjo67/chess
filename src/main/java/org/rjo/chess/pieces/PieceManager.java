package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.rjo.chess.Colour;

/**
 * @author rich
 * @since 2016-10-18
 */
public class PieceManager {
	/**
	 * Stores the pieces in the game. The first dimension indicates the colour {white, black}. The
	 * second dimension corresponds to {@link PieceType#ALL_PIECE_TYPES}.
	 */
	private Piece[][] pieces;

	/**
	 * whether this piece type has already been cloned. Dimensions as for {@link #pieces}.
	 */
	private Boolean[][] alreadyCloned;

	/**
	 * Constructor. The pieces data structure will be initialised to null values.
	 */
	public PieceManager() {
		pieces = new Piece[2][PieceType.ALL_PIECE_TYPES.length];
		initAlreadyCloned();
	}

	private void initAlreadyCloned() {
		alreadyCloned = new Boolean[2][PieceType.ALL_PIECE_TYPES.length];
		for (int i = 0; i < 2; i++) {
			for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
				alreadyCloned[i][pt.ordinal()] = Boolean.FALSE;
			}
		}
	}

	/**
	 * Constructor. The pieces map will be initialised to the values of the parameters.
	 * 
	 * @param whitePieces the white pieces
	 * @param blackPieces the black pieces
	 */
	public PieceManager(Set<Piece> whitePieces, Set<Piece> blackPieces) {
		this();
		whitePieces.stream().forEach(p -> pieces[Colour.WHITE.ordinal()][p.getType().ordinal()] = p);
		blackPieces.stream().forEach(p -> pieces[Colour.BLACK.ordinal()][p.getType().ordinal()] = p);
	}

	/**
	 * Copy constructor. Copies the contents of the hashmap into a new hashmap. This references the
	 * same 'pieces' as before. Need to clone iff these objects get changed.
	 * 
	 * @param pieceManager the pieceManager that gets copied
	 */
	public PieceManager(final PieceManager pieceManager) {
		this();
		for (Colour col : Colour.ALL_COLOURS) {
			for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
				pieces[col.ordinal()][pt.ordinal()] = pieceManager.getPiece(col, pt);
			}
		}
	}

	/**
	 * returns a particular Piece object from the map. The Piece object <b>will be cloned</b> and
	 * re-inserted into the 'pieces' hashmap the first time.
	 * 
	 * @param colour the required colour
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
			alreadyCloned[colour.ordinal()][pieceType.ordinal()] = Boolean.TRUE;
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
	public Piece[] getPiecesForColour2(Colour colour) {
		return pieces[colour.ordinal()];
	}

	/**
	 * returns a particular Piece object.
	 * 
	 * @param colour the required colour
	 * @param pieceType the required piece type
	 * @return a Piece object
	 */
	public Piece getPiece(Colour colour, PieceType pieceType) {
		return pieces[colour.ordinal()][pieceType.ordinal()];
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(600);
		sb.append("PieceManager@").append(Integer.toHexString(System.identityHashCode(this)));
		sb.append("[");
		List<String> tempList = new ArrayList<>();
		for (Colour col : Colour.ALL_COLOURS) {
			StringBuffer sb2 = new StringBuffer(300);
			boolean first = true;
			for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
				if (first) {
					first = false;
				} else {
					sb2.append(",");
				}
				sb2.append(pieces[col.ordinal()][pt.ordinal()]);
			}
			tempList.add(sb2.toString());
		}
		sb.append(tempList.stream().collect(Collectors.joining(",", "{", "}")));
		sb.append("]");
		return sb.toString();
	}
}
