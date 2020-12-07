package org.rjo.chess.pieces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
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
	 * Constructor. The pieces data structure will be initialised to null values.
	 */
	private PieceManager() {
		pieces = new Pieces[2];
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

		private static final Logger LOG = LogManager.getLogger(Pieces.class);

		private King king;
		private Pawns pawns;
		private Colour colour;

		/** rooks, knights, bishops, queens are stored in these maps, index PieceType. NOT for king or pawns! */
		private Map<Square, Piece>[] _pieces;

		/**
		 * whether this piece type has already been copied. Dimensions as for {@link #pieces}.
		 */
		private boolean[] alreadyCloned;

		/**
		 * Constructor with a list of pieces of any types.
		 *
		 * @param colour the piece colour
		 * @param pieces the required pieces
		 */
		public Pieces(Colour colour, List<Piece> pieces) {
			this.colour = colour;
			alreadyCloned = new boolean[PieceType.ALL_PIECE_TYPES.length]; // set to default FALSE
			this._pieces = new Map[PieceType.ALL_PIECE_TYPES.length];
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				_pieces[pt.ordinal()] = new HashMap<>();
			}
			for (Piece p : pieces) {
				switch (p.getType()) {
				case KING:
					if (king != null) {
						throw new IllegalStateException("cannot define more than one king");
					}
					king = (King) p;
					break;
				case ROOK:
				case KNIGHT:
				case BISHOP:
				case QUEEN:
					_pieces[p.getType().ordinal()].put(p.getLocation(), p);
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
		 * to be copied).
		 *
		 * @param other the piece object to copy
		 */
		public Pieces(Pieces other) {
			this.colour = other.colour;
			this.alreadyCloned = new boolean[PieceType.ALL_PIECE_TYPES.length]; // set to default FALSE
			this._pieces = new Map[PieceType.ALL_PIECE_TYPES.length];
			this.king = other.king; // not copied, since will never be removing a king
			this.pawns = new Pawns(other.pawns);

			//TODO don't copy the maps here, but instead on demand--using 'alreadyCloned' to keep track
			// since there will only ever be one piece 'moved' for a given position. It's therefore better to just copy the relevant map and not all of them.
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				_pieces[pt.ordinal()] = new HashMap<>(other._pieces[pt.ordinal()]);
			}

			//			debug("copy constructor called with other: " + other);
		}

		public Piece getKing() {
			return king;
		}

		public Piece getPawns() {
			return pawns;
		}

		/**
		 * Performs the 'move' for the given pieceType at the given location.
		 * <p>
		 * Do not call piece.move() directly, since then the internal structures in this object will not be updated. e.g. the
		 * hashmap of locations will be wrong.
		 *
		 * @param pieceType
		 * @param location
		 * @param move
		 */
		public void move(Move move) {
			var oldPiece = findPieceAt(move.from(), move.getPiece());
			var newPiece = copyPiece(oldPiece);
			newPiece.move(move);

			// update internal structures
			switch (move.getPiece()) {
			case KING:
			case PAWN:
				// nothing to do
				break;
			case ROOK:
			case KNIGHT:
			case BISHOP:
			case QUEEN:
				replace(oldPiece, newPiece, _pieces[move.getPiece().ordinal()]);
				break;
			default:
				throw new IllegalArgumentException("unknown piece type: " + move.getPiece());
			}
		}

		/**
		 * Creates a new piece instance. Cannot create new kings or pawns.
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
		 * Creates a new piece instance (not pawn or king) and adds it to the appropriate data structure.
		 *
		 * @param pieceType type
		 * @param location location
		 * @return the new piece
		 */
		public Piece addPiece(PieceType pieceType,
				Square location) {
			return this.addPiece(createPiece(pieceType, location));
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
			case ROOK:
			case KNIGHT:
			case BISHOP:
			case QUEEN:
				this._pieces[piece.getType().ordinal()].put(piece.getLocation(), piece);
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
			case ROOK:
			case KNIGHT:
			case BISHOP:
			case QUEEN:
				removeSuccessful = this._pieces[pieceType.ordinal()].remove(piece.getLocation(), piece);
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
				replace(piece, newPiece, this._pieces[PieceType.QUEEN.ordinal()]);
				break;
			case BISHOP:
				newPiece = new Bishop(piece);
				replace(piece, newPiece, this._pieces[PieceType.BISHOP.ordinal()]);
				break;
			case KNIGHT:
				newPiece = new Knight(piece);
				replace(piece, newPiece, this._pieces[PieceType.KNIGHT.ordinal()]);
				break;
			case ROOK:
				newPiece = new Rook(piece);
				replace(piece, newPiece, this._pieces[PieceType.ROOK.ordinal()]);
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
		 * Removes 'oldPiece' from 'pieceMap' and adds 'newPiece'.
		 */
		private void replace(Piece oldPiece,
				Piece newPiece,
				Map<Square, Piece> pieceMap) {
			//			debug(", removing " + oldPiece + " from map " + pieceMap + ", locn:" + oldPiece.getLocation());
			var removed = pieceMap.remove(oldPiece.getLocation());
			if (removed == null) {
				throw new IllegalArgumentException("no piece " + oldPiece + " at location: " + oldPiece.getLocation() + " in map:" + pieceMap);
			}
			//			debug(", adding " + newPiece + " to map " + pieceMap + ", locn:" + newPiece.getLocation());
			pieceMap.put(newPiece.getLocation(), newPiece);
		}

		/** return a bitboard of all piece types */
		public BitBoard createBitBoard() {
			BitBoard bb = new BitBoard();
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				for (Square sq : _pieces[pt.ordinal()].keySet()) {
					bb.set(sq);
				}
			}
			bb.set(king.getLocation());
			bb.getBitSet().or(pawns.getLocationBitBoard().getBitSet());

			//			stream().forEach(p -> bb.getBitSet().or(p.getLocationBitBoard().getBitSet()));

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
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				_pieces[pt.ordinal()].values().stream().forEach(p -> builder.accept(p));
			}
			builder.accept(pawns);

			return builder.build();
		}

		/** return a stream of the given piece types */
		public Stream<Piece> stream(PieceType... types) {
			Builder<Piece> builder = Stream.builder();
			for (PieceType pt : types) {
				switch (pt) {
				case KING:
					builder.accept(king);
					break;
				case ROOK:
				case KNIGHT:
				case BISHOP:
				case QUEEN:
					_pieces[pt.ordinal()].values().stream().forEach(p -> builder.accept(p));
					break;
				case PAWN:
					builder.accept(pawns);
					break;
				default:
					throw new IllegalArgumentException("unknown type " + pt);
				}
			}
			return builder.build();
		}

		/**
		 * Find the piece of the given type occupying the given square.
		 *
		 * @param square the required square
		 * @param pieceType the required type
		 * @return the piece, or throws NoSuchElementException if the square is not occupied
		 */
		public Piece findPieceAt(Square square,
				PieceType pieceType) {

			Piece piece = null;
			switch (pieceType) {
			case KING:
				if (king.getLocation() == square) {
					piece = king;
				}
				break;
			case ROOK:
			case KNIGHT:
			case BISHOP:
			case QUEEN:
				piece = _pieces[pieceType.ordinal()].get(square);
				break;
			case PAWN:
				if (pawns.pieceAt(square)) {
					piece = pawns;
				}
				break;
			default:
				throw new IllegalArgumentException("unknown type " + pieceType);
			}

			if (piece != null) {
				return piece;
			} else {
				throw new NoSuchElementException("no piece at square: " + square);
			}
		}

		/**
		 * Find the piece occupying the given square.
		 *
		 * @param square the required square
		 * @return the piece, or throws NoSuchElementException if the square is not occupied
		 */
		public Piece findPieceAt(Square square) {
			if (king.getLocation() == square) {
				return king;
			}
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				var piece = _pieces[pt.ordinal()].get(square);
				if (piece != null) {
					return piece;
				}
			}

			if (pawns.pieceAt(square)) {
				return pawns;
			}
			throw new NoSuchElementException("no piece at square: " + square);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(200);
			sb.append("Pieces@").append(System.identityHashCode(this))
					.append(" [").append(colour).append(": ");
			if (king != null) {
				sb.append(", king=").append(king);
			}
			for (PieceType pt : new PieceType[] { PieceType.QUEEN, PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP }) {
				if (!_pieces[pt.ordinal()].isEmpty()) {
					sb.append(pt + "=" + _pieces[pt.ordinal()]);
				}
			}
			if (pawns != null) {
				sb.append(", pawns=").append(pawns);
			}

			sb.append("]");
			return sb.toString();
		}

		private void debug(String message) {
			if (this.colour == Colour.BLACK && LOG.isDebugEnabled()) {
				LOG.debug("Pieces@" + System.identityHashCode(this) + " " + message);
			}
		}
	}
}