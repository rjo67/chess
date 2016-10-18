package org.rjo.chess;

import java.util.EnumSet;

import org.rjo.chess.pieces.PieceType;

/**
 * Represents a move.
 *
 * @author rich
 */
public class Move {

	private Square from;
	private Square to;

	/** which piece is moving */
	private PieceType piece;
	/** colour of the piece */
	private Colour colour;

	/** capture info -- if not null, implies that this move was a capture */
	private CaptureInfo captureInfo;

	/** whether this move was a check */
	private boolean check;

	/** castling info -- if not null, implies that this move was 0-0 or 0-0-0 */
	private CastlingInfo castlingInfo;

	/** castling rights info */
	private CastlingRightsInfo castlingRightsInfo;

	/**
	 * if promotion info -- if not null, implies that this move was a promotion
	 */
	private PromotionInfo promotionInfo;

	/** true if enpassant move */
	private boolean enpassant;

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
	 * @param capture whether this move is a capture
	 * @param check whether this move is a check
	 */
	public Move(PieceType piece, Colour colour, Square from, Square to, PieceType capturedPiece, boolean check) {
		this.piece = piece;
		this.colour = colour;
		this.from = from;
		this.to = to;
		if (capturedPiece != null) {
			this.captureInfo = new CaptureInfo(capturedPiece);
		}
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
			move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE,
					new Move(PieceType.ROOK, Colour.WHITE, Square.h1, Square.f1));
		} else {
			move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.g8);
			move.castlingInfo = new CastlingInfo(CastlingRights.KINGS_SIDE,
					new Move(PieceType.ROOK, Colour.BLACK, Square.h8, Square.f8));
		}
		return move;
	}

	public static Move castleQueensSide(Colour colour) {
		Move move;
		if (Colour.WHITE == colour) {
			move = new Move(PieceType.KING, Colour.WHITE, Square.e1, Square.c1);
			move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE,
					new Move(PieceType.ROOK, Colour.WHITE, Square.a1, Square.d1));
		} else {
			move = new Move(PieceType.KING, Colour.BLACK, Square.e8, Square.c8);
			move.castlingInfo = new CastlingInfo(CastlingRights.QUEENS_SIDE,
					new Move(PieceType.ROOK, Colour.BLACK, Square.a8, Square.d8));
		}
		return move;
	}

	public static Move enpassant(Colour colour, Square from, Square to) {
		Move move = new Move(PieceType.PAWN, colour, from, to, PieceType.PAWN);
		move.enpassant = true;
		return move;
	}

	/**
	 * converts from uci style move to a move object
	 *
	 * @param moveStr uci move e.g. b7d5
	 * @return move object. Whether 'Check' is not examined!
	 */
	public static Move fromUCIString(String moveStr, final Game game) {
		Square from = Square.fromString(moveStr.substring(0, 2));
		Square to = Square.fromString(moveStr.substring(2, 4));
		PieceType piece = game.getPosition().pieceAt(from, game.getPosition().getSideToMove());
		boolean kingsMove = piece == PieceType.KING;
		Move m;
		if (kingsMove && from == Square.e1 && to == Square.g1) {
			m = Move.castleKingsSide(Colour.WHITE);
		} else if (kingsMove && from == Square.e8 && to == Square.g8) {
			m = Move.castleKingsSide(Colour.BLACK);
		} else if (kingsMove && from == Square.e1 && to == Square.c1) {
			m = Move.castleQueensSide(Colour.WHITE);
		} else if (kingsMove && from == Square.e8 && to == Square.c8) {
			m = Move.castleQueensSide(Colour.BLACK);
		} else {
			PieceType capture = null;
			try {
				capture = game.getPosition().pieceAt(to, Colour.oppositeColour(game.getPosition().getSideToMove()));
				m = new Move(piece, game.getPosition().getSideToMove(), from, to, capture);
			} catch (IllegalArgumentException x) {
				// not a capture -- unless enpassant
				Square enpassantSquare = game.getPosition().getEnpassantSquare();
				if (enpassantSquare != null && piece == PieceType.PAWN && to == enpassantSquare) {
					m = Move.enpassant(game.getPosition().getSideToMove(), from, to);
				} else {
					m = new Move(piece, game.getPosition().getSideToMove(), from, to);
				}
			}
			if (piece == PieceType.PAWN && to.rank() == 7) {
				PieceType promotedPiece = PieceType.getPieceTypeFromSymbol(moveStr.substring(4, 5));
				m.setPromotionPiece(promotedPiece);
			}
		}
		return m;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public boolean isCheck() {
		return check;
	}

	public boolean isCapture() {
		return captureInfo != null;
	}

	public boolean isEnpassant() {
		return enpassant;
	}

	public PieceType getCapturedPiece() {
		if (isCapture()) {
			return captureInfo.capturedPiece;
		} else {
			throw new IllegalArgumentException("move was not a capture: " + toString());
		}
	}

	public boolean isPromotion() {
		return promotionInfo != null;
	}

	public boolean isCastleKingsSide() {
		return ((castlingInfo != null) && (castlingInfo.direction == CastlingRights.KINGS_SIDE));
	}

	public boolean isCastleQueensSide() {
		return ((castlingInfo != null) && (castlingInfo.direction == CastlingRights.QUEENS_SIDE));
	}

	public Move getRooksCastlingMove() {
		return (castlingInfo != null) ? castlingInfo.rooksMove : null;
	}

	public PieceType getPiece() {
		return piece;
	}

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
	 * Set the castling rights previous to this move. This should always be filled for a king or a
	 * rook move, and can be filled for other moves.
	 *
	 * @param previousCastlingRights
	 */
	public void setPreviousCastlingRights(EnumSet<CastlingRights> previousCastlingRights) {
		this.castlingRightsInfo.setPreviousCastlingRights(previousCastlingRights.clone());
	}

	public EnumSet<CastlingRights> getPreviousCastlingRights() {
		return castlingRightsInfo.getPreviousCastlingRights();
	}

	/**
	 * Use this to find out if the previousCastlingRights have been set, prior to calling
	 * getPreviousCastlingRights.
	 *
	 * @return
	 */
	public boolean previousCastlingRightsWasSet() {
		return castlingRightsInfo.getPreviousCastlingRights() != null;
	}

	/**
	 * Set the castling rights FOR THE OPPONENT previous to this move. This should be filled for a
	 * move which affects a1, a8, h1, h8.
	 *
	 * @param previousCastlingRights
	 */
	public void setPreviousCastlingRightsOpponent(EnumSet<CastlingRights> previousCastlingRights) {
		this.castlingRightsInfo.setPreviousCastlingRightsOpponent(previousCastlingRights.clone());
	}

	public EnumSet<CastlingRights> getPreviousCastlingRightsOpponent() {
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
	 * Returns true if this move was a pawn move of two squares forward. This implies a potential
	 * enpassant move for the opponent.
	 *
	 * @return true if this move was a pawn move of two squares forward.
	 */
	public boolean isPawnMoveTwoSquaresForward() {
		if (PieceType.PAWN == piece) {
			return (Math.abs(to.rank() - from.rank()) == 2);
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
		 * Stores castling rights BEFORE this move. To enable unmove. Value gets set for each king and
		 * rook move. and for each (opponent's) move which has target square a1, a8, h1, or h8.
		 */
		private EnumSet<CastlingRights> previousCastlingRights;

		/**
		 * Stores castling rights of the OPPONENT BEFORE this move. To enable unmove. Value gets set
		 * for moves such as Nb6xa8 (target squares a1, a8, h1, or h8), when the opponent can no
		 * longer castle on this side.
		 */
		private EnumSet<CastlingRights> previousCastlingRightsOpponent;

		public CastlingRightsInfo() {
			this.previousCastlingRights = null;
			this.previousCastlingRightsOpponent = null;
		}

		public void setPreviousCastlingRights(EnumSet<CastlingRights> previousCastlingRights) {
			this.previousCastlingRights = previousCastlingRights.clone();
		}

		public EnumSet<CastlingRights> getPreviousCastlingRights() {
			return previousCastlingRights;
		}

		public void setPreviousCastlingRightsOpponent(EnumSet<CastlingRights> previousCastlingRights) {
			this.previousCastlingRightsOpponent = previousCastlingRights.clone();
		}

		public EnumSet<CastlingRights> getPreviousCastlingRightsOpponent() {
			return previousCastlingRightsOpponent;
		}

	}

	private static class CaptureInfo {
		private PieceType capturedPiece;

		public CaptureInfo(PieceType capturedPiece) {
			this.capturedPiece = capturedPiece;
		}
	}

	private static class PromotionInfo {
		private PieceType promotedPiece;

		public PromotionInfo(PieceType promotedPiece) {
			if ((promotedPiece == PieceType.PAWN) || (promotedPiece == PieceType.KING)) {
				throw new IllegalArgumentException("cannot promote to a pawn or king!");
			}
			this.promotedPiece = promotedPiece;
		}
	}

}
