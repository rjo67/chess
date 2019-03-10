package org.rjo.chess.position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;

/**
 * Stores information about a position:
 * <ul>
 * <li>is king in check and if so, from where and which pieces?</li>
 * <li>which pieces are pinned</li>
 * </ul>
 * TODO merge with PositionCheckState?
 * <p>
 * Normally all squares come into consideration for a move. If the king is in check however, the available squares are
 * greatly reduced. In this case either the checking piece must be captured, a piece interposed on the checking ray, or
 * the king must move.
 * <p>
 * (Merged with CheckRestriction March 2019.)
 *
 * @author rich
 */
public class PositionInfo {

	private Square kingsSquare;
	private List<PieceInfo> checkers;
	private List<PieceInfo> pinnedPieces;
	/**
	 * bitboard of all squares from the checking piece(s) to the king. This represents the squares where a piece could
	 * interpose (or take the attacker) to block the check. Default is all-set, i.e. all squares are allowed.
	 */
	private BitBoard squaresToBlockCheck;
	/**
	 * squares in this bitset represent the squares where the king <B>cannot</B> move since it would still be in check.
	 * Default is empty, i.e. no restriction.
	 */
	private BitBoard restrictedSquaresForKing; // default is no restriction

	public PositionInfo(Square kingsSquare) {
		this.kingsSquare = kingsSquare;
		this.checkers = new ArrayList<>();
		this.pinnedPieces = new ArrayList<>();
	}

	public void addChecker(RayType ray,
			PieceType piece,
			Square square) {
		checkers.add(new PieceInfo(ray, piece, square));
	}

	public void addChecker(RayType ray,
			PieceType piece,
			int bitIndex) {
		checkers.add(new PieceInfo(ray, piece, bitIndex));
	}

	public void addChecker(PieceType piece,
			int bitIndex) {
		checkers.add(new PieceInfo(null, piece, bitIndex));
	}

	public void addPinnedPiece(RayType ray,
			PieceType piece,
			int bitIndex) {
		pinnedPieces.add(new PieceInfo(ray, piece, bitIndex));
	}

	public List<PieceInfo> getCheckers() {
		return checkers;
	}

	public List<PieceInfo> getPinnedPieces() {
		return pinnedPieces;
	}

	public boolean isKingInCheck() {
		return checkers.size() >= 1;
	}

	public boolean isDoubleCheck() {
		return checkers.size() >= 2;
	}

	/**
	 * Calculates the 'restricted squares', e.g. all squares from the ones from the checking piece to the king.
	 * <p>
	 * This must be called after the check- and pin-info has been set.
	 */
	public void calculateRestrictedSquares() {
		if (isKingInCheck()) {
			squaresToBlockCheck = BitBoard.empty();
			restrictedSquaresForKing = BitBoard.empty();
			for (PieceInfo pieceInfo : checkers) {
				if (pieceInfo.ray == null) {
					// knight or pawn giving check
					squaresToBlockCheck.set(pieceInfo.getBitIndex());
				} else {
					// normally the square of the checking piece itself is included in both bitsets.
					// 'restrictedSquaresForKing' excludes this square if adjacent to king since the king could move there and take the piece
					squaresToBlockCheck.set(pieceInfo.getBitIndex());
					if (!Square.fromBitIndex(pieceInfo.getBitIndex()).isAdjacentTo(kingsSquare)) {
						restrictedSquaresForKing.set(pieceInfo.getBitIndex());
					}
					Iterator<Integer> iter = RayUtils.getRay(pieceInfo.ray.getOpposite()).squaresFrom(pieceInfo.getBitIndex());
					boolean reachedKingsSquare = false;
					while (iter.hasNext()) {
						int sq = iter.next();
						if (sq == kingsSquare.bitIndex()) {
							reachedKingsSquare = true;
							break;
						}
						squaresToBlockCheck.set(sq);
						restrictedSquaresForKing.set(sq);
					}
					if (reachedKingsSquare) {
						// process squares 'after' the king
						while (iter.hasNext()) {
							restrictedSquaresForKing.set(iter.next());
						}
					}
				}
			}
		} else {
			squaresToBlockCheck = BitBoard.allSet();
			restrictedSquaresForKing = BitBoard.empty();
		}
	}

	/**
	 * A bitset of all the squares where pieces <B>must</B> interpose to block a check (or take the attacking piece).
	 * <p>
	 * Usage: possibleMoves.and(boardInfo.getSquaresToBlockCheck().getBitSet());
	 */
	public BitBoard getSquaresToBlockCheck() {
		return squaresToBlockCheck;
	}

	/**
	 * A bitset of all the squares where the king <B>cannot</B> move to, since he'll still be in check. This differs from
	 * {@link #getSquaresToBlockCheck()} in that
	 * <ul>
	 * <li>the rays from checking-piece to king are continued past the king up to the end of the ray, since the king cannot
	 * move in that direction either.</li>
	 * <li>squares of checking pieces directly next to the king are NOT included, since the king could move there and take
	 * the attacker.</li>
	 * </ul>
	 * <p>
	 * Usage: possibleSquares.andNot(boardInfo.getCheckRestrictedSquaresForKing().getBitSet());
	 */
	public BitBoard getCheckRestrictedSquaresForKing() {
		return restrictedSquaresForKing;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PositionInfo [kingsSquare=").append(kingsSquare);
		sb.append(", checkers: ").append(checkers).append(", pins: ").append(pinnedPieces);
		sb.append(", restrictedSquaresForKing:\n").append(restrictedSquaresForKing);
		sb.append(", squaresToBlockCheck:\n").append(squaresToBlockCheck);
		sb.append("]");
		return sb.toString();
	}

	public static class PieceInfo {
		private RayType ray;
		private PieceType piece;
		private int bitIndex;

		public PieceInfo(RayType ray, PieceType piece, Square square) {
			this(ray, piece, square.bitIndex());
		}

		public PieceInfo(RayType ray, PieceType piece, int bitIndex) {
			this.ray = ray;
			this.piece = piece;
			this.bitIndex = bitIndex;
		}

		public RayType getRay() {
			return ray;
		}

		public PieceType getPiece() {
			return piece;
		}

		public int getBitIndex() {
			return bitIndex;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			PieceInfo other = (PieceInfo) obj;
			if (bitIndex != other.bitIndex) {
				return false;
			}
			if (piece != other.piece) {
				return false;
			}
			if (ray != other.ray) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("PieceInfo [");
			if (ray != null) {
				sb.append("ray=").append(ray).append(",");
			}
			sb.append(piece);
			sb.append("/").append(Square.fromBitIndex(bitIndex)).append("(").append(bitIndex).append(")");
			sb.append("]");
			return sb.toString();
		}
	}
}
