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
 * TODO merge with PositionCheckState? Normally all squares come into consideration for a move. If the king is in check
 * however, the available squares are greatly reduced. In this case either the checking piece must be captured, a piece
 * interposed on the checking ray, or the king must move.
 * <p>
 * (Merged with CheckRestriction March 2019.)
 *
 * @author rich
 */
public class PositionInfo {

	private Square kingsSquare;
	private List<PieceInfo> checkInfo;
	private List<PieceInfo> pinInfo;
	private BitBoard restrictedSquares = BitBoard.allSet(); // default is no restriction
	private BitBoard restrictedSquaresForKing = BitBoard.empty(); // default is no restriction

	public PositionInfo(Square kingsSquare) {
		this.kingsSquare = kingsSquare;
		this.checkInfo = new ArrayList<>();
		this.pinInfo = new ArrayList<>();
	}

	public void addChecker(RayType ray,
			PieceType piece,
			Square square) {
		checkInfo.add(new PieceInfo(ray, piece, square));
	}

	public void addChecker(RayType ray,
			PieceType piece,
			int bitIndex) {
		checkInfo.add(new PieceInfo(ray, piece, bitIndex));
	}

	public void addChecker(PieceType piece,
			int bitIndex) {
		checkInfo.add(new PieceInfo(null, piece, bitIndex));
	}

	public void addPinnedPiece(RayType ray,
			PieceType piece,
			int bitIndex) {
		pinInfo.add(new PieceInfo(ray, piece, bitIndex));
	}

	public List<PieceInfo> getCheckInfo() {
		return checkInfo;
	}

	public List<PieceInfo> getPinInfo() {
		return pinInfo;
	}

	public boolean isKingInCheck() {
		return checkInfo.size() >= 1;
	}

	public boolean isDoubleCheck() {
		return checkInfo.size() >= 2;
	}

	/**
	 * Calculates the 'restricted squares', all squares from the ones from the checking piece to the king.
	 * <p>
	 * If not in check, sets an empty bitset.
	 */
	public void calculateCheckRestrictedSquares() {
		restrictedSquares = BitBoard.empty();
		restrictedSquaresForKing = BitBoard.empty();
		if (isKingInCheck()) {
			for (PieceInfo pieceInfo : checkInfo) {
				if (pieceInfo.ray != null) {
					// 'restrictedSquares' includes square of checking piece itself
					restrictedSquares.set(pieceInfo.getBitIndex());
					// 'restrictedSquaresForKing' excludes this square if adjacent to king since the king can move there and take the piece
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
						restrictedSquares.set(sq);
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
		}
	}

	/**
	 * A bitset of all the squares where pieces <B>must</B> interpose to block a check / take the attacking piece.
	 * <p>
	 * Usage: possibleMoves.and(boardInfo.getCheckRestrictedSquares().getBitSet());
	 */
	public BitBoard getCheckRestrictedSquares() {
		return restrictedSquares;
	}

	/**
	 * A bitset of all the squares where the king <B>cannot</B> move to, since he'll still be in check. This differs from
	 * {@link #getCheckRestrictedSquares()} in that
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
