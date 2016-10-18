package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.rjo.chess.Colour;

/**
 * @author rich
 * @since 2016-10-18
 */
public class PieceManager {
	/**
	 * Stores the pieces in the game. The dimension indicates the colour {white, black}.
	 */
	private Map<PieceType, Piece>[] pieces;
	/**
	 * whether this piece type has already been cloned
	 */
	private Map<PieceType, Boolean>[] alreadyCloned;

	/**
	 * Constructor. The pieces map will be initialised to null values.
	 */
	@SuppressWarnings("unchecked")
	public PieceManager() {
		pieces = new HashMap[2];
		alreadyCloned = new HashMap[2];

		for (int i = 0; i < 2; i++) {
			pieces[i] = new HashMap<>();
			alreadyCloned[i] = new HashMap<>();
			for (PieceType pt : PieceType.ALL_PIECE_TYPES) {
				alreadyCloned[i].put(pt, Boolean.FALSE);
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
		whitePieces.stream().forEach(p -> getPiecesForColour(Colour.WHITE).put(p.getType(), p));
		blackPieces.stream().forEach(p -> getPiecesForColour(Colour.BLACK).put(p.getType(), p));
	}

	// copy constructor
	// copy the contents of the hashmap into a new hashmap
	// This references the same 'pieces' as before. Need to clone iff these objects get changed
	public PieceManager(final PieceManager pieceManager) {
		this();
		for (Colour colour : Colour.values()) {
			for (Piece p : pieceManager.getPiecesForColour(colour).values()) {
				getPiecesForColour(colour).put(p.getType(), p);
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
		if (alreadyCloned[colour.ordinal()].get(pieceType)) {
			return getPiece(colour, pieceType);
		}
		try {
			Piece cloned = (Piece) getPiece(colour, pieceType).clone();
			pieces[colour.ordinal()].put(pieceType, cloned);
			alreadyCloned[colour.ordinal()].put(pieceType, Boolean.TRUE);
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("could not clone piece!?");
		}
	}

	/**
	 * returns the piece map for the given colour.
	 * 
	 * @param colour the required colour
	 * @return all pieces for the given colour
	 */
	public Map<PieceType, Piece> getPiecesForColour(Colour colour) {
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
		return getPiecesForColour(colour).get(pieceType);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(300);
		sb.append("PieceManager@").append(Integer.toHexString(System.identityHashCode(this)));
		sb.append("[");
		List<String> tempList = new ArrayList<>();
		for (Colour col : Colour.values()) {
			// sb.append(col).append("{");
			List<String> tempList1 = new ArrayList<>();
			pieces[col.ordinal()].entrySet().stream().forEach(p -> {
				tempList1.add(p.getValue().toString());
			});
			tempList.add(tempList1.stream().collect(Collectors.joining(",")));
		}
		sb.append(tempList.stream().collect(Collectors.joining(",", "{", "}")));
		sb.append("]");
		return sb.toString();
	}
}
