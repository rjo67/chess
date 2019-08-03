package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;

/**
 * @author rich
 * @since 2016-10-18
 */
public class PieceManager {
	/**
	 * Stores the pieces in the game. The first dimension indicates the colour {white, black}.
	 */
	private Pieces[] pieces;

	/**
	 * whether this piece type has already been cloned. Dimensions as for {@link #pieces}.
	 */
	private boolean[][] alreadyCloned;

	/**
	 * Constructor. The pieces data structure will be initialised to null values.
	 */
	private PieceManager() {
		pieces = new Pieces[2];
		alreadyCloned = new boolean[2][PieceType.ALL_PIECE_TYPES.length]; // set to default FALSE
	}

	/**
	 * Constructor. The pieces map will be initialised to the values of the parameters.
	 *
	 * @param whitePieces the white pieces
	 * @param blackPieces the black pieces
	 */
	public PieceManager(List<Piece> whitePieces, List<Piece> blackPieces) {
		this();
		pieces[Colour.WHITE.ordinal()] = new Pieces(Colour.WHITE, whitePieces);
		pieces[Colour.BLACK.ordinal()] = new Pieces(Colour.BLACK, blackPieces);
	}

	/**
	 * Copy constructor. The new object references the same <code>Piece instances</code> as before. Need to clone iff these
	 * objects get changed.
	 *
	 * @param otherPieceManager the pieceManager that gets copied
	 */
	public PieceManager(final PieceManager otherPieceManager) {
		this();
		pieces[Colour.WHITE.ordinal()] = new Pieces(otherPieceManager.pieces[Colour.WHITE.ordinal()]);
		pieces[Colour.BLACK.ordinal()] = new Pieces(otherPieceManager.pieces[Colour.BLACK.ordinal()]);
	}

	/**
	 * Creates a copy of the Piece object for the given type at the given location and returns it.
	 * <p>
	 * The new object will replace the previous object in the appropriate data structures.
	 *
	 * @param colour the required colour
	 * @param pieceType the required piece type
	 * @param location location of the piece
	 * @return a Piece object
	 */
	public Piece getClonedPiece(Colour colour,
			PieceType pieceType,
			Square location) {
		//		if (alreadyCloned[colour.ordinal()][pieceType.ordinal()]) {
		//			return getPiece(colour, pieceType, location);
		//		}

		var piece = getPiece(colour, pieceType, location);
		//			alreadyCloned[colour.ordinal()][pieceType.ordinal()] = true;
		return pieces[colour.ordinal()].copyPiece(piece);
	}

	/**
	 * Adds a piece of the given type and colour to the appropriate data structures.
	 *
	 * @param colour colour
	 * @param pieceType type
	 * @param location location
	 * @return the new piece
	 */
	public Piece addPiece(Colour colour,
			PieceType pieceType,
			Square location) {
		return pieces[colour.ordinal()].addPiece(pieceType, location);
	}

	/**
	 * Removes a piece of the given type and colour from the appropriate data structures.
	 *
	 * @param colour colour
	 * @param pieceType type
	 * @param location location
	 */
	public void removePiece(Colour colour,
			PieceType pieceType,
			Square location) {
		pieces[colour.ordinal()].removePiece(pieceType, location);
	}

	/**
	 * returns the pieces for the given colour.
	 *
	 * @param colour the required colour
	 * @return all pieces for the given colour
	 */
	public Pieces getPiecesForColour(Colour colour) {
		return pieces[colour.ordinal()];
	}

	/**
	 * returns a particular Piece object.
	 *
	 * @param colour the required colour
	 * @param pieceType the required piece type
	 * @param location the location of the piece
	 * @return a Piece object
	 */
	public Piece getPiece(Colour colour,
			PieceType pieceType,
			Square location) {
		return pieces[colour.ordinal()].findPieceAt(location, pieceType);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(600);
		sb.append("PieceManager@").append(Integer.toHexString(System.identityHashCode(this)));
		sb.append("[");
		sb.append(pieces[Colour.WHITE.ordinal()]);
		sb.append(",");
		sb.append(pieces[Colour.BLACK.ordinal()]);
		sb.append("]");
		return sb.toString();
	}

	public static class Pieces {
		private King king;
		private List<Piece> rooks = new ArrayList<>();
		private List<Piece> knights = new ArrayList<>();
		private List<Piece> bishops = new ArrayList<>();
		private List<Piece> queens = new ArrayList<>();
		private Pawns pawns;
		private Colour colour;

		/**
		 * Constructor with a list of pieces of any types.
		 *
		 * @param colour the piece colour
		 * @param pieces the required pieces
		 */
		public Pieces(Colour colour, List<Piece> pieces) {
			this.colour = colour;
			for (Piece p : pieces) {
				switch (p.getType()) {
				case KING:
					if (king != null) {
						throw new IllegalStateException("cannot define more than one king");
					}
					king = (King) p;
					break;
				case QUEEN:
					queens.add(p);
					break;
				case BISHOP:
					bishops.add(p);
					break;
				case KNIGHT:
					knights.add(p);
					break;
				case ROOK:
					rooks.add(p);
					break;
				case PAWN:
					pawns = (Pawns) p;
					break;
				default:
					throw new IllegalArgumentException("unknown piece type: " + p.getType());
				}
			}
		}

		/**
		 * Copy constructor. The lists are copied (their contents are not duplicated, if the entries are changed they first need
		 * to be cloned).
		 *
		 * @param other the piece object to copy
		 */
		public Pieces(Pieces other) {
			this.colour = other.colour;
			this.king = other.king; // not copied, since will never be removing a king
			this.rooks = new ArrayList<>(other.rooks);
			this.knights = new ArrayList<>(other.knights);
			this.bishops = new ArrayList<>(other.bishops);
			this.queens = new ArrayList<>(other.queens);
			this.pawns = new Pawns(other.pawns);
		}

		public Piece getKing() {
			return king;
		}

		public List<Piece> getRooks() {
			return rooks;
		}

		public List<Piece> getKnights() {
			return knights;
		}

		public List<Piece> getBishops() {
			return bishops;
		}

		public List<Piece> getQueens() {
			return queens;
		}

		public Piece getPawns() {
			return pawns;
		}

		/**
		 * Creates a new piece instance.
		 *
		 * @param pieceType type
		 * @param location location
		 * @return the new piece
		 */
		public Piece createPiece(PieceType pieceType,
				Square location) {
			Piece newPiece;
			switch (pieceType) {
			case KING:
				throw new IllegalArgumentException("cannot create a king!");
			case QUEEN:
				newPiece = new Queen(this.colour, location);
				break;
			case BISHOP:
				newPiece = new Bishop(this.colour, location);
				break;
			case KNIGHT:
				newPiece = new Knight(this.colour, location);
				break;
			case ROOK:
				newPiece = new Rook(this.colour, location);
				break;
			case PAWN:
				throw new IllegalArgumentException("cannot create a pawn!");
			default:
				throw new IllegalArgumentException("unknown piece type: " + pieceType);
			}
			return newPiece;
		}

		/**
		 * Creats a new piece instance and adds it to the appropriate data structure.
		 *
		 * @param pieceType type
		 * @param location location
		 * @return the new piece
		 */
		public Piece addPiece(PieceType pieceType,
				Square location) {
			Piece newPiece = createPiece(pieceType, location);
			return this.addPiece(newPiece);
		}

		/**
		 * Adds the given piece to the appropriate data structure.
		 *
		 * @param piece the piece to add
		 * @return the piece
		 */
		public Piece addPiece(Piece piece) {
			switch (piece.getType()) {
			case KING:
				throw new IllegalArgumentException("cannot add a king!");
			case QUEEN:
				this.queens.add(piece);
				break;
			case BISHOP:
				this.bishops.add(piece);
				break;
			case KNIGHT:
				this.knights.add(piece);
				break;
			case ROOK:
				this.rooks.add(piece);
				break;
			case PAWN:
				throw new IllegalArgumentException("cannot add a pawn!");
			default:
				throw new IllegalArgumentException("unknown piece type: " + piece.getType());
			}
			return piece;
		}

		/**
		 * Removes a piece of the given type at the given location from the appropriate data structures.
		 *
		 * @param pieceType type
		 * @param location location
		 */
		public void removePiece(PieceType pieceType,
				Square location) {
			var piece = findPieceAt(location, pieceType);
			boolean removeSuccessful;
			switch (pieceType) {
			case KING:
				throw new IllegalArgumentException("cannot remove a king!");
			case QUEEN:
				removeSuccessful = this.queens.remove(piece);
				break;
			case BISHOP:
				removeSuccessful = this.bishops.remove(piece);
				break;
			case KNIGHT:
				removeSuccessful = this.knights.remove(piece);
				break;
			case ROOK:
				removeSuccessful = this.rooks.remove(piece);
				break;
			case PAWN:
				pawns.removePiece(location);
				removeSuccessful = true;
				break;
			default:
				throw new IllegalArgumentException("unknown piece type: " + pieceType);
			}
			if (!removeSuccessful) {
				throw new IllegalStateException("could not remove " + piece);
			}
		}

		/**
		 * The given piece will be copied and returned. The copy replaces the given piece in the appropriate data structure.
		 *
		 * @param piece the piece to copy
		 * @return the copied piece
		 */
		public Piece copyPiece(Piece piece) {
			Piece newPiece;
			switch (piece.getType()) {
			case KING:
				newPiece = new King(piece);
				this.king = (King) newPiece;
				break;
			case QUEEN:
				newPiece = new Queen(piece);
				replace(piece, newPiece, this.queens);
				break;
			case BISHOP:
				newPiece = new Bishop(piece);
				replace(piece, newPiece, this.bishops);
				break;
			case KNIGHT:
				newPiece = new Knight(piece);
				replace(piece, newPiece, this.knights);
				break;
			case ROOK:
				newPiece = new Rook(piece);
				replace(piece, newPiece, this.rooks);
				break;
			case PAWN:
				newPiece = new Pawns((Pawns) piece);
				this.pawns = (Pawns) newPiece; //TODO need to keep track of having being cloned, otherwise will create new object every time ??
				break;
			default:
				throw new IllegalArgumentException("unknown piece type: " + piece.getType());
			}
			return newPiece;
		}

		/**
		 * Removes 'oldPiece' from 'pieceList' and adds 'newPiece'.
		 */
		private void replace(Piece oldPiece,
				Piece newPiece,
				List<Piece> pieceList) {
			var removed = pieceList.remove(oldPiece);
			if (!removed) {
				throw new IllegalArgumentException("could not find piece " + oldPiece + " in list: " + pieceList);
			}
			pieceList.add(newPiece);
		}

		/** return a bitboard of all piece types */
		public BitBoard createBitBoard() {
			BitBoard bb = new BitBoard();
			stream().forEach(p -> bb.getBitSet().or(p.getLocationBitBoard().getBitSet()));
			return bb;
		}

		/** return a bitboard of the given piece types */
		public BitBoard createBitBoard(PieceType... types) {
			BitBoard bb = new BitBoard();
			stream(types).forEach(p -> bb.set(p.getLocation()));
			return bb;
		}

		/**
		 * Creates a map of all squares occupied by pieces and the corresponding piece type.
		 */
		public Map<Square, PieceType> createPieceMap() {
			Map<Square, PieceType> map = new HashMap<>(40);
			this.stream(PieceType.ALL_PIECE_TYPES_EXCEPT_PAWN)
					.forEach(p -> map.put(p.getLocation(), p.getType()));
			this.pawns.getLocationBitBoard().stream().forEach(bitIndex -> map.put(Square.fromBitIndex(bitIndex), PieceType.PAWN));

			return map;
		}

		/** return a stream of all piece types */
		public Stream<Piece> stream() {
			Builder<Piece> builder = Stream.builder();
			builder.accept(king);
			rooks.stream().forEach(p -> builder.accept(p));
			knights.stream().forEach(p -> builder.accept(p));
			bishops.stream().forEach(p -> builder.accept(p));
			queens.stream().forEach(p -> builder.accept(p));
			builder.accept(pawns);

			return builder.build();
		}

		/** return a stream of the given piece types */
		public Stream<Piece> stream(PieceType... types) {
			Builder<Piece> builder = Stream.builder();
			for (PieceType t : types) {
				switch (t) {
				case KING:
					builder.accept(king);
					break;
				case ROOK:
					rooks.stream().forEach(p -> builder.accept(p));
					break;
				case KNIGHT:
					knights.stream().forEach(p -> builder.accept(p));
					break;
				case BISHOP:
					bishops.stream().forEach(p -> builder.accept(p));
					break;
				case QUEEN:
					queens.stream().forEach(p -> builder.accept(p));
					break;
				case PAWN:
					builder.accept(pawns);
					break;
				default:
					throw new IllegalArgumentException("unknown type " + t);
				}
			}
			return builder.build();
		}

		/**
		 * Find the piece of the given type occupying the given square.
		 *
		 * @param square the required square
		 * @param pieceTypes the required type(s)
		 * @return the piece, or throws NoSuchElementException if the square is not occupied
		 */
		public Piece findPieceAt(Square square,
				PieceType... pieceTypes) {
			return this.stream(pieceTypes).filter(p -> p.pieceAt(square)).findAny().orElseThrow();
		}

		/**
		 * Find the piece occupying the given square.
		 *
		 * @param square the required square
		 * @return the piece, or throws NoSuchElementException if the square is not occupied
		 */
		public Piece findPieceAt(Square square) {
			return this.stream().filter(p -> p.pieceAt(square)).findAny().orElseThrow();
		}

		@Override
		public String toString() {
			return "Pieces@" + System.identityHashCode(this)
					+ " [" + colour + ": "
					+ (king != null ? "king=" + king : "")
					+ (rooks.isEmpty() ? "" : ", rooks=" + rooks)
					+ (knights.isEmpty() ? "" : ", knights=" + knights)
					+ (bishops.isEmpty() ? "" : ", bishops=" + bishops)
					+ (queens.isEmpty() ? "" : ", queens=" + queens)
					+ (pawns != null ? ", pawns=" + pawns : "")
					+ "]";
		}

	}
}
