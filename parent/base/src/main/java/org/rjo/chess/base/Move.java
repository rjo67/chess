package org.rjo.chess.base;

import org.rjo.chess.base.CastlingRightsSummary.CastlingRights;

/**
 * Represents a move.
 *
 * @author rich
 */
public class Move {

	private final Square from;
	private final Square to;

	/**
	 * which piece is moving
	 */
	private final PieceType piece;
	/**
	 * colour of the piece
	 */
	private final Colour colour;

	/**
	 * captured piece -- null if non-capture, otherwise if implies that this move was a capture
	 */
	private PieceType capturedPiece;

	/**
	 * whether this move was a check. Cannot be null.
	 */
	private boolean check;

	/**
	 * castling info -- if not null, implies that this move was 0-0 or 0-0-0
	 */
	private CastlingInfo castlingInfo;

	/**
	 * castling rights info
	 */
	private CastlingRightsInfo castlingRightsInfo;

	/**
	 * if promotion info -- if not null, implies that this move was a promotion
	 */
	private PromotionInfo promotionInfo;

	/**
	 * true if enpassant move
	 */
	private boolean enpassant;

	/**
	 * Set to the square where the pawn was, which has just been captured enpassant. See
	 * {@link Square#findMoveFromEnpassantSquare(Square)}.
	 */
	private Square pawnCapturedEnpassant;

	/**
	 * Constructor for normal non-capture non-check moves.
	 *
	 * @param piece which piece is moving
	 * @param colour colour of moving piece
	 * @param from start square
	 * @param to destination square
	 */
	public Move(PieceType piece, Colour colour, Square from, Square to) {
		this(piece, colour, from, to, null);
	}

	/**
	 * Constructor allowing specification of capture (non-check) moves.
	 *
	 * @param piece which piece is moving
	 * @param colour colour of moving piece
	 * @param from start square
	 * @param to destination square
	 * @param capturedPiece the captured piece (null if not a capture)
	 */
	public Move(PieceType piece, Colour colour, Square from, Square to, PieceType capturedPiece) {
		this(piece, colour, from, to, capturedPiece, false);
	}

	/**
	 * Constructor allowing specification of capture and check moves.
	 *
	 * @param piece which piece is moving
	 * @param colour colour of moving piece
	 * @param from start square
	 * @param to destination square
	 * @param capturedPiece info about a captured piece (can be null)
	 * @param check whether this move is a check
	 */
	public Move(PieceType piece, Colour colour, Square from, Square to, PieceType capturedPiece, boolean check) {
		this.piece = piece;
		this.colour = colour;
		this.from = from;
		this.to = to;
		this.capturedPiece = capturedPiece;
		this.check = check;
		this.castlingInfo = null;
		this.enpassant = false;
		this.castlingRightsInfo = new CastlingRightsInfo();
	}

	/**
	 * Sets the promotion piece i/c of a pawn promotion.
	 *
	 * @param type to which piece the pawn gets promoted
	 */
	public void setPromotionPiece(PieceType type) {
		if (piece != PieceType.PAWN) {
			throw new IllegalArgumentException("can only specify a promotion piece for a pawn move");
		}

		this.promotionInfo = new PromotionInfo(type);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(10);
		if (castlingInfo != null) {
			if (CastlingRights.KINGS_SIDE == castlingInfo.direction) {
				sb.append("O-O");
			} else {
				sb.append("O-O-O");
			}
		} else {
			sb.append(piece.getSymbol());
			sb.append(from);
			sb.append(isCapture() ? "x" : "-");
			sb.append(to);
			sb.append(isPromotion() ? "=" + promotionInfo.promotedPiece.getSymbol() : "");
		}
		sb.append(check ? "+" : "");
		return sb.toString();
	}

	public String toUCIString() {
		if (isCastleKingsSide()) {
			return (colour == Colour.WHITE ? "e1g1" : "e8g8");
		} else if (isCastleQueensSide()) {
			return (colour == Colour.WHITE ? "e1c1" : "e8c8");
		} else {
			return from().name() + to().name();
		}
	}

	public static Move castleKingsSide(Colour colour) {
		Move move;
		if (Colour.WHITE == colour) {
			move = new Move(PieceType.KING, Colour.WHITE, Square.e1, Square.g1);
			move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(PieceType.ROOK, Colour.WHITE, Square.h1, Square.f1));
		} else {
			move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.g8);
			move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE, new Move(PieceType.ROOK, Colour.BLACK, Square.h8, Square.f8));
		}
		return move;
	}

	public static Move castleQueensSide(Colour colour) {
		Move move;
		if (Colour.WHITE == colour) {
			move = new Move(PieceType.KING, Colour.WHITE, Square.e1, Square.c1);
			move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(PieceType.ROOK, Colour.WHITE, Square.a1, Square.d1));
		} else {
			move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.c8);
			move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE, new Move(PieceType.ROOK, Colour.BLACK, Square.a8, Square.d8));
		}
		return move;
	}

	public static Move enpassant(Colour colour,
			Square from,
			Square to) {
		Move move = new Move(PieceType.PAWN, colour, from, to, PieceType.PAWN);
		move.enpassant = true;
		move.pawnCapturedEnpassant = Square.findMoveFromEnpassantSquare(to);
		return move;
	}

	public void setCheck(boolean checkInfo) {
		this.check = checkInfo;
	}

	public boolean isCheck() {
		return this.check;
	}

	public boolean isCapture() {
		return capturedPiece != null;
	}

	public boolean isEnpassant() {
		return enpassant;
	}

	public Square getPawnCapturedEnpassant() {
		return pawnCapturedEnpassant;
	}

	public PieceType getCapturedPiece() {
		if (isCapture()) {
			return capturedPiece;
		} else {
			throw new IllegalArgumentException("move was not a capture: " + toString());
		}
	}

	/**
	 * true if this move represents a pawn promotion.
	 */
	public boolean isPromotion() {
		return promotionInfo != null;
	}

	public boolean isCastleKingsSide() {
		return (castlingInfo != null && castlingInfo.direction == CastlingRights.KINGS_SIDE);
	}

	public boolean isCastleQueensSide() {
		return (castlingInfo != null && castlingInfo.direction == CastlingRights.QUEENS_SIDE);
	}

	public Move getRooksCastlingMove() {
		return (castlingInfo != null) ? castlingInfo.rooksMove : null;
	}

	/**
	 * which piece is moving.
	 *
	 * @return the moving piece.
	 */
	public PieceType getPiece() {
		return piece;
	}

	/**
	 * colour of moving piece.
	 *
	 * @return the colour of the moving piece.
	 */
	public Colour getColour() {
		return colour;
	}

	public PieceType getPromotedPiece() {
		return (promotionInfo != null) ? promotionInfo.promotedPiece : null;
	}

	public Square from() {
		return from;
	}

	public Square to() {
		return to;
	}

	/**
	 * Set the castling rights previous to this move. This should always be filled for a king or a rook move, and can be
	 * filled for other moves.
	 *
	 * @param previousCastlingRights
	 */
	public void setPreviousCastlingRights(CastlingRightsSummary previousCastlingRights) {
		this.castlingRightsInfo.setPreviousCastlingRights(previousCastlingRights);
	}

	public CastlingRightsSummary getPreviousCastlingRights() {
		return castlingRightsInfo.getPreviousCastlingRights();
	}

	/**
	 * Use this to find out if the previousCastlingRights have been set, prior to calling getPreviousCastlingRights.
	 *
	 * @return
	 */
	public boolean previousCastlingRightsWasSet() {
		return castlingRightsInfo.getPreviousCastlingRights() != null;
	}

	/**
	 * Set the castling rights FOR THE OPPONENT previous to this move. This should be filled for a move which affects a1,
	 * a8, h1, h8.
	 *
	 * @param previousCastlingRights
	 */
	public void setPreviousCastlingRightsOpponent(CastlingRightsSummary previousCastlingRights) {
		this.castlingRightsInfo.setPreviousCastlingRightsOpponent(previousCastlingRights);
	}

	public CastlingRightsSummary getPreviousCastlingRightsOpponent() {
		return castlingRightsInfo.getPreviousCastlingRightsOpponent();
	}

	/**
	 * Use this to find out if the previousCastlingRightsOpponent have been set, prior to calling
	 * getPreviousCastlingRightsOpponent.
	 *
	 * @return
	 */
	public boolean previousCastlingRightsOpponentWasSet() {
		return castlingRightsInfo.getPreviousCastlingRightsOpponent() != null;
	}

	/**
	 * Returns true if this move was a pawn move of two squares forward. This implies a potential enpassant move for the
	 * opponent.
	 *
	 * @return true if this move was a pawn move of two squares forward.
	 */
	public boolean isPawnMoveTwoSquaresForward() {
		if (PieceType.PAWN == piece) {
			return Math.abs(to.rank() - from.rank()) == 2;
		} else {
			return false;
		}
	}

	/**
	 * information about a castling move
	 */
	private static class CastlingInfo {
		private CastlingRights direction;
		private Move rooksMove;

		public CastlingInfo(CastlingRights direction, Move rooksMove) {
			this.direction = direction;
			this.rooksMove = rooksMove;
		}
	}

	/**
	 * Information about the 'castling rights' for each player before this move.
	 */
	private static class CastlingRightsInfo {
		/**
		 * Stores castling rights BEFORE this move. To enable unmove. Value gets set for each king and rook move. and for each
		 * (opponent's) move which has target square a1, a8, h1, or h8.
		 */
		private CastlingRightsSummary previousCastlingRights;

		/**
		 * Stores castling rights of the OPPONENT BEFORE this move. To enable unmove. Value gets set for moves such as Nb6xa8
		 * (target squares a1, a8, h1, or h8), when the opponent can no longer castle on this side.
		 */
		private CastlingRightsSummary previousCastlingRightsOpponent;

		public CastlingRightsInfo() {
			this.previousCastlingRights = null;
			this.previousCastlingRightsOpponent = null;
		}

		public void setPreviousCastlingRights(CastlingRightsSummary previousCastlingRights) {
			this.previousCastlingRights = new CastlingRightsSummary(previousCastlingRights);
		}

		public CastlingRightsSummary getPreviousCastlingRights() {
			return previousCastlingRights;
		}

		public void setPreviousCastlingRightsOpponent(CastlingRightsSummary previousCastlingRights) {
			this.previousCastlingRightsOpponent = new CastlingRightsSummary(previousCastlingRights);
		}

		public CastlingRightsSummary getPreviousCastlingRightsOpponent() {
			return previousCastlingRightsOpponent;
		}

	}

	private static class PromotionInfo {
		private PieceType promotedPiece;

		public PromotionInfo(PieceType promotedPiece) {
			if (promotedPiece == PieceType.PAWN || promotedPiece == PieceType.KING) {
				throw new IllegalArgumentException("cannot promote to a pawn or king!");
			}
			this.promotedPiece = promotedPiece;
		}
	}

}
