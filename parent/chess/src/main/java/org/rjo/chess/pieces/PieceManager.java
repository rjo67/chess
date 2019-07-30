package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;
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
		pieces[Colour.WHITE.ordinal()] = new Pieces(whitePieces);
		pieces[Colour.BLACK.ordinal()] = new Pieces(blackPieces);
	}

	/**
	 * Copy constructor. The new object references the same <code>pieces</code> as before. Need to clone iff these objects
	 * get changed.
	 *
	 * @param otherPieceManager the pieceManager that gets copied
	 */
	public PieceManager(final PieceManager otherPieceManager) {
		this();
		pieces[Colour.WHITE.ordinal()] = otherPieceManager.pieces[Colour.WHITE.ordinal()];
		pieces[Colour.BLACK.ordinal()] = otherPieceManager.pieces[Colour.BLACK.ordinal()];
	}

	//	/**
	//	 * returns a particular Piece object from the map. The Piece object <b>will be cloned</b> and re-inserted into the
	//	 * <code>pieces</code> hashmap the first time.
	//	 *
	//	 * @param colour the required colour
	//	 * @param pieceType the required piece type
	//	 * @return a Piece object
	//	 */
	//	public Piece getClonedPiece(Colour colour,
	//			PieceType pieceType) {
	//		if (alreadyCloned[colour.ordinal()][pieceType.ordinal()]) {
	//			return getPiece(colour, pieceType);
	//		}
	//		try {
	//			Piece cloned = (Piece) getPiece(colour, pieceType).clone();
	//			pieces[colour.ordinal()][pieceType.ordinal()] = cloned;
	//			alreadyCloned[colour.ordinal()][pieceType.ordinal()] = true;
	//			return cloned;
	//		} catch (CloneNotSupportedException e) {
	//			throw new RuntimeException("could not clone piece!?");
	//		}
	//	}

	/**
	 * returns the pieces for the given colour.
	 *
	 * @param colour the required colour
	 * @return all pieces for the given colour
	 */
	public Pieces getPiecesForColour(Colour colour) {
		return pieces[colour.ordinal()];
	}

	//	/**
	//	 * returns a particular Piece object.
	//	 *
	//	 * @param colour the required colour
	//	 * @param pieceType the required piece type
	//	 * @return a Piece object
	//	 */
	//	public Piece getPiece(Colour colour,
	//			PieceType pieceType) {
	//		return pieces[colour.ordinal()][pieceType.ordinal()];
	//	}

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

		public Pieces(List<Piece> pieces) {
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

		/** return a bitboard of all piece types */
		public BitBoard createBitBoard() {
			BitBoard bb = new BitBoard();
			stream().forEach(p -> bb.set(p.getLocation()));
			return bb;
		}

		/** return a bitboard of the given piece types */
		public BitBoard createBitBoard(PieceType... types) {
			BitBoard bb = new BitBoard();
			stream(types).forEach(p -> bb.set(p.getLocation()));
			return bb;
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
			return "Pieces [king=" + king + ", rooks=" + rooks + ", knights=" + knights + ", bishops=" + bishops + ", queens=" + queens
					+ ", pawns=" + pawns + "]";
		}

	}
}
