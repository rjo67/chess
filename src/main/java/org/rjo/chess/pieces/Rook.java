package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.BitBoard;
import org.rjo.chess.Colour;
import org.rjo.chess.Move;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.ray.RayFactory;
import org.rjo.chess.ray.RayType;
import org.rjo.chess.util.BitValueCalculator;
import org.rjo.chess.util.Stopwatch;

/**
 * Stores information about the rooks (still) in the game.
 *
 * @author rich
 */
public class Rook extends SlidingPiece {

	private static final Logger LOG = LogManager.getLogger(Rook.class);
	/*
	 * if useMovemap is defined, we'll use the data structures moveMap, vertMoveMap. Otherwise the
	 * ray algorithm will be used.
	 */
	private static final boolean USE_MOVE_MAP;

	static {
		String val = System.getProperty("useMovemap");
		USE_MOVE_MAP = (val != null);
		LOG.debug("using moveMap algorithm: " + USE_MOVE_MAP);
	}

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
	 * stores possible moves from posn 'x' (first array dimension). x is the file (in bits from
	 * right, RHS==0). so moveMap[4] stores patterns where the rook is on the 4th file. In the map
	 * itself: the key is the value of the byte pattern (file 'x' is always 1). the value stores
	 * which moves are valid for this pattern. The maps are immutable.
	 */
	private final static Map<Integer, MoveInfo>[] moveMap = new Map[8];
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
		 * general algorithm: 'tmpMoveMap' stores the bitmaps to the 'left' and the 'right' of
		 * piecePosn. In the 3rd for loop, these get concatenated into 'concatMoveMap' -- one value
		 * for both the LHS and the RHS. These values are then stored as immutable maps. 'vertMoveMap'
		 * is then generated from the horizontal 'moveMap'.
		 */

		// achtung! In this array the index starts from the right!
		// in the 'real' moveMap [0] is the leftmost file, e.g. A1
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
				fieldIncr--; // since processing LHS, need to store -1, -2, -3,
									// ...
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
		// Achtung! concatMoveMap[0] is the leftmost file, e.g. A1. this is the
		// opposite of tmpMoveMap.
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
				continue;
			} else if (piecePosn == 0) {
				// special case: from extreme RHS, take all of map[0]
				concatMoveMap[translatedPiecePosn] = tmpMoveMap[piecePosn][0];
				continue;
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
		return Piece.pieceSquareValue(pieces.getBitSet(), colour, PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Rook class -- with no pieces on the board. Delegates to Rook(Colour, boolean)
	 * with parameter false.
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
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are
	 *           placed on the board.
	 */
	public Rook(Colour colour, boolean startPosition) {
		this(colour, startPosition, null);
	}

	/**
	 * Constructs the Rook class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case
	 *           no pieces are placed on the board.
	 */
	public Rook(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Rook class with the required squares (can be null) or the default start
	 * squares. Setting 'startPosition' true has precedence over 'startSquares'.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of 'startSquares'
	 *           will be ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case
	 *           no pieces are placed on the board.
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
		requiredSquares = colour == Colour.WHITE ? new Square[] { Square.a1, Square.h1 }
				: new Square[] { Square.a8, Square.h8 };
		initPosition(requiredSquares);
	}

	/** uses the moveMap and vertMoveMap structures to find possible moves */
	private List<Move> findMovesUsingMoveMap(Position posn) {
		List<Move> moves = new ArrayList<>();
		Colour opponentsColour = Colour.oppositeColour(colour);
		BitBoard allPieces = posn.getTotalPieces();
		BitSet opponentsPieces = posn.getAllPieces(opponentsColour).getBitSet();

		// rank, getValueForRank()
		Map<Integer, Integer> rankValueCache = new HashMap<>();
		Map<Integer, Integer> fileValueCache = new HashMap<>();
		for (int bitIndex = pieces.getBitSet().nextSetBit(0); bitIndex >= 0; bitIndex = pieces.getBitSet()
				.nextSetBit(bitIndex + 1)) {
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
			addMoves(posn, moves, opponentsColour, opponentsPieces, bitIndex, fromSquareIndex,
					vertMoveMap[rank].get(fileVal));
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
	 * @param bitIndexOfPiece bit index of our piece for which the moves are currently being
	 *           calculated
	 * @param fromSquareIndex square corresponding to bitIndexOfPiece
	 * @param moveinfo the MoveInfo object corresponding to the file (or rank) which we're currently
	 *           looking at.
	 */
	private void addMoves(Position chessboard, List<Move> moves, Colour opponentsColour, BitSet opponentsPieces,
			int bitIndexOfPiece, Square fromSquareIndex, MoveInfo moveinfo) {

		// System.out.printf("val=%d, map entry=%s%n", val, moveinfo);
		for (int sqOffset : moveinfo.getMoveOffsets()) {
			moves.add(new Move(this.getType(), colour, fromSquareIndex, Square.fromBitIndex(bitIndexOfPiece + sqOffset)));
		}

		for (int sqOffset : moveinfo.getPossibleCapturesOffset()) {
			int sqIndex = bitIndexOfPiece + sqOffset;
			if (opponentsPieces.get(sqIndex)) {
				Square targetSquare = Square.fromBitIndex(sqIndex);
				moves.add(new Move(this.getType(), colour, fromSquareIndex, targetSquare,
						chessboard.pieceAt(targetSquare, opponentsColour)));
			}
		}
	}

	@Override
	public List<Move> findMoves(Position posn, boolean kingInCheck) {
		Stopwatch stopwatch = new Stopwatch();

		List<Move> moves = new ArrayList<>(30);

		// search for moves
		if (USE_MOVE_MAP) {
			moves = findMovesUsingMoveMap(posn);
		} else {
			for (RayType rayType : new RayType[] { RayType.NORTH, RayType.EAST, RayType.SOUTH, RayType.WEST }) {
				moves.addAll(search(posn, RayFactory.getRay(rayType)));
			}
		}
		// make sure king is not/no longer in check
		Square myKing = King.findKing(colour, posn);
		Iterator<Move> iter = moves.listIterator();
		Colour opponentsColour = Colour.oppositeColour(colour);
		while (iter.hasNext()) {
			Move move = iter.next();
			boolean inCheck = Position.isKingInCheck(posn, move, opponentsColour, myKing, kingInCheck);
			if (inCheck) {
				iter.remove();
			}
		}

		// checks
		Square opponentsKing = King.findOpponentsKing(colour, posn);
		/*
		 * most moves have the same starting square. If we've already checked for discovered check for
		 * this square, then can use the cached result. (Discovered check only looks along one ray
		 * from move.from() to the opponent's king.)
		 */
		Map<Square, Boolean> discoveredCheckCache = new HashMap<>(5);
		for (Move move : moves) {
			boolean isCheck = findRankOrFileCheck(posn, move, opponentsKing);
			// if it's already check, don't need to calculate discovered check
			if (!isCheck) {
				if (discoveredCheckCache.containsKey(move.from())) {
					isCheck = discoveredCheckCache.get(move.from());
				} else {
					isCheck = Position.checkForDiscoveredCheck(posn, move, colour, opponentsKing);
					discoveredCheckCache.put(move.from(), isCheck);
				}
			}
			move.setCheck(isCheck);
		}

		long time = stopwatch.read();
		if (time != 0) {
			LOG.debug("found " + moves.size() + " moves in " + time);
		}
		return moves;
	}

	@Override
	public boolean attacksSquare(BitSet emptySquares, Square targetSq) {
		for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
			if (attacksSquareRankOrFile(emptySquares, Square.fromBitIndex(i), targetSq)) {
				return true;
			}
		}
		return false;
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

	// replaces the move offsets by the given offsetMultiplier and returns a new
	// MoveInfo
	public static MoveInfo copyWithOffsetMultiplier(MoveInfo m1, int offsetMultiplier) {
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
	public static MoveInfo concat(MoveInfo m1, MoveInfo m2) {
		List<Integer> moves = new ArrayList<>();
		for (Integer i : m1.moveOffsets) {
			moves.add(i);
		}
		for (Integer i : m2.moveOffsets) {
			moves.add(i);
		}

		List<Integer> captures = new ArrayList<>();
		for (Integer i : m1.possibleCapturesOffset) {
			captures.add(i);
		}
		for (Integer i : m2.possibleCapturesOffset) {
			captures.add(i);
		}
		MoveInfo mi = new MoveInfo(moves.toArray(new Integer[moves.size()]),
				captures.toArray(new Integer[captures.size()]));
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