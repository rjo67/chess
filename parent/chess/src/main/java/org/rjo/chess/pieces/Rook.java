package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rjo.chess.SystemFlags;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.Move.CheckInformation;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.SquareCache;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.bits.BitValueCalculator;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.base.ray.RayUtils;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.check.BoardInfo;
import org.rjo.chess.position.check.CheckRestriction;
import org.rjo.chess.position.check.KingCheck;

/**
 * Stores information about the rooks in the game.
 *
 * @author rich
 */
public class Rook extends SlidingPiece {

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 500;

	private static int[] SQUARE_VALUE =
	// @formatter:off
      new int[] {
         0,  0,  0,  5,  5,  0,  0,  0,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
        -5,  0,  0,  0,  0,  0,  0, -5,
         5, 10, 10, 10, 10, 10, 10,  5,
         0,  0,  0,  0,  0,  0,  0,  0,
      };
      // @formatter:on

	/**
	 * stores possible moves from posn 'x' (first array dimension). x is the file (in bits from right, RHS==0). so
	 * moveMap[4] stores patterns where the rook is on the 4th file. In the map itself: the key is the value of the byte
	 * pattern (file 'x' is always 1). the value stores which moves are valid for this pattern. The maps are immutable.
	 */
	@SuppressWarnings("unchecked")
	private final static Map<Integer, MoveInfo>[] moveMap = new Map[8];
	@SuppressWarnings("unchecked")
	private final static Map<Integer, MoveInfo>[] vertMoveMap = new Map[8];

	static {
		/* @formatter:off
       * **************** piecePosn=4 ****************
       *  1 00010000 x10  HI: 0001 x1   -
       *  2 00110000 x30  HI: 0011 x3   -
       *  3 01010000 x50  HI: 0101 x5   -
       *  4 01110000 x70  HI: 0111 x7   -
       *  5 10010000 x90  HI: 1001 x9   -
       *  6 10110000 xB0  HI: 1011 xB   -
       *  7 11010000 xD0  HI: 1101 xD   -
       *  8 11110000 xF0  HI: 1111 xF   -
       *
       *
       *  piecePosn==3
       *  1 00001000 x8  LO: 00000000 x0
       *  2 00001001 x9  LO: 00000001 x1
       *  3 00001010 xA  LO: 00000010 x2
       *  4 00001011 xB  LO: 00000011 x3
       *  5 00001100 xC  LO: 00000100 x4
       *  6 00001101 xD  LO: 00000101 x5
       *  7 00001110 xE  LO: 00000110 x6
       *  8 00001111 xF  LO: 00000111 x7
       *  [0, 1, 2, 3, 4, 5, 6, 7]
       *
       * @formatter:on
       */

		/*
		 * general algorithm: 'tmpMoveMap' stores the bitmaps to the 'left' and the 'right' of piecePosn. In the 3rd for loop,
		 * these get concatenated into 'concatMoveMap' -- one value for both the LHS and the RHS. These values are then stored
		 * as immutable maps. 'vertMoveMap' is then generated from the horizontal 'moveMap'.
		 */

		// achtung! In this array the index starts from the right!
		// in the 'real' moveMap [0] is the leftmost file, e.g. A1
		@SuppressWarnings("unchecked")
		Map<Integer, MoveInfo>[][] tmpMoveMap = new HashMap[8][2];
		// add squares to left of posn
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			BitValueCalculator t = new BitValueCalculator();
			int maskStart = 1;
			t.generate(piecePosn);
			List<Integer> moves = new ArrayList<>();
			int fieldIncr = 1; // moves are stored as +/- 1 from start posn
			tmpMoveMap[piecePosn][0] = new HashMap<>(10);
			for (int posnToCheck = piecePosn + 1; posnToCheck < 8; posnToCheck++) {
				maskStart = maskStart << 1;
				int mask = maskStart + 1;// b11, b101, b1001,
				fieldIncr--; // since processing LHS, need to store -1, -2, -3, ...
				if (fieldIncr != 0) {
					moves.add(fieldIncr);
				}

				MoveInfo moveInfo = new MoveInfo(moves.toArray(new Integer[moves.size()]), (fieldIncr - 1));

				Iterator<Integer> iter = t.getCacheHI().iterator();
				while (iter.hasNext()) {
					int value = iter.next();
					if ((value & mask) == mask) { // set
						tmpMoveMap[piecePosn][0].put(value, moveInfo);
						iter.remove();
					}
				}
			}
			// don't forget "1", i.e. all values to LHS
			if (piecePosn != 7) {
				moves.add(--fieldIncr);
				tmpMoveMap[piecePosn][0].put(1, new MoveInfo(moves.toArray(new Integer[moves.size()])));
			}
		}
		// add squares to right of piecePosn
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			BitValueCalculator t = new BitValueCalculator();
			int mask = 2 << (piecePosn - 1);
			t.generate(piecePosn);
			List<Integer> moves = new ArrayList<>();
			int fieldIncr = -1;
			tmpMoveMap[piecePosn][1] = new HashMap<>(10);
			for (int posnToCheck = piecePosn - 1; posnToCheck >= 0; posnToCheck--) {
				mask = mask >> 1; // b100, b10, b1
				fieldIncr++; // since processing RHS, need 1, 2, 3, ...
				if (fieldIncr != 0) {
					moves.add(fieldIncr);
				}

				MoveInfo moveInfo = new MoveInfo(moves.toArray(new Integer[moves.size()]), (fieldIncr + 1));

				Iterator<Integer> iter = t.getCacheLO().iterator();
				while (iter.hasNext()) {
					int value = iter.next();
					if ((value & mask) == mask) { // set
						tmpMoveMap[piecePosn][1].put(value, moveInfo);
						iter.remove();
					}
				}
			}
			// don't forget "0", i.e. all values to RHS
			if (piecePosn != 0) {
				moves.add(++fieldIncr);
				tmpMoveMap[piecePosn][1].put(0, new MoveInfo(moves.toArray(new Integer[moves.size()])));
			}
		}

		// concat the two dimensions together to get the definitive moveMap
		// Achtung! concatMoveMap[0] is the leftmost file, e.g. A1. this is the opposite of tmpMoveMap.
		@SuppressWarnings("unchecked")
		Map<Integer, MoveInfo>[] concatMoveMap = new HashMap[8];
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			int translatedPiecePosn = 7 - piecePosn;
			concatMoveMap[translatedPiecePosn] = new HashMap<>(30);
			if (piecePosn == 7) {
				// special case: from extreme LHS, take all of map[1]
				// but need to add 128 to each key to represent the piece at [7]
				for (Integer key : tmpMoveMap[piecePosn][1].keySet()) {
					concatMoveMap[translatedPiecePosn].put(key + 128, tmpMoveMap[piecePosn][1].get(key));
				}
			} else if (piecePosn == 0) {
				// special case: from extreme RHS, take all of map[0]
				concatMoveMap[translatedPiecePosn] = tmpMoveMap[piecePosn][0];
			} else {
				for (int hi : tmpMoveMap[piecePosn][0].keySet()) {
					for (int lo : tmpMoveMap[piecePosn][1].keySet()) {
						int key = (hi << piecePosn) + lo;
						// System.out.println(String.format("piecePosn: %d, hi:
						// %s, lo: %s, key: %s", piecePosn,
						// Integer.toBinaryString(hi),
						// Integer.toBinaryString(lo),
						// Integer.toBinaryString(key)));
						concatMoveMap[translatedPiecePosn].put(key,
								MoveInfo.concat(tmpMoveMap[piecePosn][0].get(hi), tmpMoveMap[piecePosn][1].get(lo)));
					}
				}
			}
		}
		// make unmodifiable
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			moveMap[piecePosn] = Collections.unmodifiableMap(concatMoveMap[piecePosn]);
		}
		// copy moveMap to the vertical
		@SuppressWarnings("unchecked")
		Map<Integer, MoveInfo>[] tmpVertMoveMap = new HashMap[8];
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			tmpVertMoveMap[piecePosn] = new HashMap<>(30);
			for (Integer key : moveMap[piecePosn].keySet()) {
				MoveInfo mi = moveMap[piecePosn].get(key);
				tmpVertMoveMap[piecePosn].put(key, MoveInfo.copyWithOffsetMultiplier(mi, 8));
			}
		}
		// make unmodifiable
		for (int piecePosn = 0; piecePosn < 8; piecePosn++) {
			vertMoveMap[piecePosn] = Collections.unmodifiableMap(tmpVertMoveMap[piecePosn]);
		}
	}

	@Override
	public int calculatePieceSquareValue() {
		return AbstractBitBoardPiece.pieceSquareValue(pieces, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Rook class -- with no pieces on the board. Delegates to Rook(Colour, boolean) with parameter false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public Rook(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the Rook class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public Rook(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the Rook class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Rook(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Rook class with the required squares (can be null) or the default start squares. Setting
	 * 'startPosition' true has precedence over <code>startSquares</code>.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
	 *           ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Rook(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.ROOK);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = null;
		requiredSquares = getColour() == Colour.WHITE ? new Square[] { Square.a1, Square.h1 } : new Square[] { Square.a8, Square.h8 };
		initPosition(requiredSquares);
	}

	/** uses the moveMap and vertMoveMap structures to find possible moves */
	private List<Move> findMovesUsingMoveMap(Position posn) {
		List<Move> moves = new ArrayList<>();
		Colour opponentsColour = Colour.oppositeColour(getColour());
		BitBoard allPieces = posn.getTotalPieces();
		BitSetUnifier opponentsPieces = posn.getAllPieces(opponentsColour).getBitSet();

		// rank, getValueForRank()
		Map<Integer, Integer> rankValueCache = new HashMap<>();
		Map<Integer, Integer> fileValueCache = new HashMap<>();
		for (int bitIndex = pieces.getBitSet().nextSetBit(0); bitIndex >= 0; bitIndex = pieces.getBitSet().nextSetBit(bitIndex + 1)) {
			Square fromSquareIndex = Square.fromBitIndex(bitIndex);
			// System.out.println("on square " + fromSquareIndex);
			int file = fromSquareIndex.file();
			int rank = fromSquareIndex.rank();
			int rankVal;
			if (rankValueCache.containsKey(rank)) {
				rankVal = rankValueCache.get(rank);
			} else {
				rankVal = allPieces.getValueForRank(rank);
				rankValueCache.put(rank, rankVal);
			}

			addMoves(posn, moves, opponentsColour, opponentsPieces, bitIndex, fromSquareIndex, moveMap[file].get(rankVal));

			int fileVal;
			if (fileValueCache.containsKey(file)) {
				fileVal = fileValueCache.get(file);
			} else {
				fileVal = allPieces.getValueForFile(file);
				fileValueCache.put(file, fileVal);
			}
			addMoves(posn, moves, opponentsColour, opponentsPieces, bitIndex, fromSquareIndex, vertMoveMap[rank].get(fileVal));
		}

		return moves;
	}

	/**
	 * internal method to find moves/captures.
	 *
	 * @param chessboard the current board
	 * @param moves possible moves will be returned in this list
	 * @param opponentsColour opponent's colour
	 * @param opponentsPieces bitset of opponent's pieces
	 * @param bitIndexOfPiece bit index of our piece for which the moves are currently being calculated
	 * @param fromSquareIndex square corresponding to bitIndexOfPiece
	 * @param moveinfo the MoveInfo object corresponding to the file (or rank) which we're currently looking at.
	 */
	private void addMoves(Position chessboard,
			List<Move> moves,
			Colour opponentsColour,
			BitSetUnifier opponentsPieces,
			int bitIndexOfPiece,
			Square fromSquareIndex,
			MoveInfo moveinfo) {

		// System.out.printf("val=%d, map entry=%s%n", val, moveinfo);
		for (int sqOffset : moveinfo.getMoveOffsets()) {
			moves.add(new Move(this.getType(), getColour(), fromSquareIndex, Square.fromBitIndex(bitIndexOfPiece + sqOffset)));
		}

		for (int sqOffset : moveinfo.getPossibleCapturesOffset()) {
			int sqIndex = bitIndexOfPiece + sqOffset;
			if (opponentsPieces.get(sqIndex)) {
				Square targetSquare = Square.fromBitIndex(sqIndex);
				moves.add(
						new Move(this.getType(), getColour(), fromSquareIndex, targetSquare, chessboard.pieceAt(targetSquare, opponentsColour)));
			}
		}
	}

	@Override
	public List<Move> findMoves(Position posn,
			BoardInfo boardInfo) {
		List<Move> moves = new ArrayList<>(30);

		for (RayType rayType : new RayType[] { RayType.NORTH, RayType.EAST, RayType.SOUTH, RayType.WEST }) {
			moves.addAll(search(posn, RayUtils.getRay(rayType), boardInfo.getCheckRestrictedSquares(), boardInfo.isKingInCheck()));
		}
		// make sure my king is not/no longer in check
		Square myKing = posn.getKingPosition(colour);
		Colour opponentsColour = Colour.oppositeColour(colour);
		moves.removeIf(move -> KingCheck.isKingInCheck(posn, move, opponentsColour, myKing, boardInfo.isKingInCheck()));

		return moves;
	}

	@Override
	public List<Move> findMoves(Position posn,
			CheckInformation kingInCheck,
			CheckRestriction checkRestriction) {

		List<Move> moves = findPotentialMoves(posn, checkRestriction);

		// make sure king is not/no longer in check
		Square myKing = posn.getKingPosition(colour);
		Colour opponentsColour = Colour.oppositeColour(colour);
		moves.removeIf(move -> KingCheck.isKingInCheck(posn, move, opponentsColour, myKing, kingInCheck.isCheck()));
		return moves;
	}

	@Override
	public List<Move> findPotentialMoves(Position posn,
			CheckRestriction checkRestriction) {
		List<Move> moves = new ArrayList<>(30);

		// search for moves
		if (SystemFlags.USE_MOVE_MAP) {
			moves = findMovesUsingMoveMap(posn);
		} else {
			for (RayType rayType : new RayType[] { RayType.NORTH, RayType.EAST, RayType.SOUTH, RayType.WEST }) {
				moves.addAll(search(posn, RayUtils.getRay(rayType), checkRestriction.getSquareRestriction(), checkRestriction.isInCheck()));
			}
		}
		return moves;
	}

	@Override
	public CheckInformation isOpponentsKingInCheckAfterMove(Position posn,
			Move move,
			Square opponentsKing,
			BitSetUnifier emptySquares,
			PositionCheckState checkCache,
			SquareCache<Boolean> discoveredCheckCache) {

		if (findRankOrFileCheck(posn, emptySquares, move, opponentsKing, checkCache)) {
			return new CheckInformation(move.getPiece(), move.to());
		}
		// if it's already check, don't need to calculate discovered check

		/*
		 * most moves have the same starting square. If we've already checked for discovered check for this square, then can use
		 * the cached result. (Discovered check only looks along one ray from move.from() to the opponent's king.)
		 */
		boolean isCheck;
		Boolean lookup = discoveredCheckCache.lookup(move.from());
		if (lookup != null) {
			isCheck = lookup;
		} else {
			isCheck = Position.checkForDiscoveredCheck(posn, move, getColour(), opponentsKing);
			discoveredCheckCache.store(move.from(), isCheck);
		}
		if (isCheck) {
			return new CheckInformation(true);
		} else {
			return CheckInformation.NOT_CHECK;
		}
	}

	@Override
	public boolean attacksSquare(BitSetUnifier emptySquares,
			Square targetSq,
			PositionCheckState checkCache) {
		for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
			if (attacksSquare(emptySquares, Square.fromBitIndex(i), targetSq, checkCache, false /* TODO */
					, false)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * static version of {@link #attacksSquare(BitSetUnifier, Square, PositionCheckState)}, for use from Pawn.
	 *
	 * @param emptySquares the empty squares
	 * @param startSquare start square (i.e. where the rook is)
	 * @param targetSquare square being attacked (i.e. where the king is)
	 * @param checkCache cache of previously found results
	 * @param isCapture if the move is a capture
	 * @param isPromotion if the move is a promotion
	 * @return true if targetSquare is attacked from startSquare, otherwise false.
	 */
	public static boolean attacksSquare(BitSetUnifier emptySquares,
			Square startSquare,
			Square targetSquare,
			PositionCheckState checkCache,
			boolean isCapture,
			boolean isPromotion) {
		return attacksSquareRankOrFile(emptySquares, startSquare, targetSquare, checkCache, isCapture, isPromotion);
	}
}

class MoveInfo {
	/** stores possible moves as bitindex offsets from current square */
	private Integer[] moveOffsets;
	/**
	 * this offset is either an enemy piece (i.e. a capture move) or a friendly piece (i.e. no move)
	 */
	private Integer[] possibleCapturesOffset;

	MoveInfo(Integer[] moves) {
		this(moves, new Integer[0]);
	}

	public Integer[] getMoveOffsets() {
		return moveOffsets;
	}

	public Integer[] getPossibleCapturesOffset() {
		return possibleCapturesOffset;
	}

	// replaces the move offsets by the given offsetMultiplier and returns a new MoveInfo
	public static MoveInfo copyWithOffsetMultiplier(MoveInfo m1,
			int offsetMultiplier) {
		Integer[] newMoveOffsets = m1.getMoveOffsets().clone();
		Integer[] newCaptureOffsets = m1.getPossibleCapturesOffset().clone();
		for (int i = 0; i < newMoveOffsets.length; i++) {
			newMoveOffsets[i] *= offsetMultiplier;
		}
		for (int i = 0; i < newCaptureOffsets.length; i++) {
			newCaptureOffsets[i] *= offsetMultiplier;
		}
		return new MoveInfo(newMoveOffsets, newCaptureOffsets);
	}

	// concats the info in m1 and m2 and returns a new MoveInfo
	public static MoveInfo concat(MoveInfo m1,
			MoveInfo m2) {
		List<Integer> moves = new ArrayList<>();
		Collections.addAll(moves, m1.moveOffsets);
		Collections.addAll(moves, m2.moveOffsets);

		List<Integer> captures = new ArrayList<>();
		Collections.addAll(captures, m1.possibleCapturesOffset);
		Collections.addAll(captures, m2.possibleCapturesOffset);

		MoveInfo mi = new MoveInfo(moves.toArray(new Integer[0]), captures.toArray(new Integer[0]));
		// System.out.println(String.format("m1: %s, m2: %s, concat: %s", m1,
		// m2, mi));
		return mi;
	}

	MoveInfo(Integer[] moveOffsets, Integer... possibleCapturesOffset) {
		this.moveOffsets = moveOffsets;
		this.possibleCapturesOffset = possibleCapturesOffset;
	}

	@Override
	public String toString() {
		String result = "{";
		StringBuilder sb = new StringBuilder();
		for (Integer m : moveOffsets) {
			sb.append(m).append(" ");
		}
		if (sb.length() != 0) {
			result += "m=" + sb.toString();
		}
		if (possibleCapturesOffset.length > 0) {
			sb = new StringBuilder();
			for (Integer captures : possibleCapturesOffset) {
				sb.append(captures).append(" ");
			}
			result += "c=" + sb.toString();
		}
		return result + "}";
	}

}
