package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.List;

import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.bits.BitBoard;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetHelper;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.base.ray.RayType;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;
import org.rjo.chess.position.PositionInfo;
import org.rjo.chess.position.check.KingCheck;

/**
 * Stores information about the pawns (still) in the game.
 *
 * @author rich
 */
public class Pawn extends AbstractBitBoardPiece {

	/**
	 * piece value in centipawns
	 */
	private static final int PIECE_VALUE = 100;

	/**
	 * Stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function. These values
	 * (mirrored for black) should be added to VALUE to get a piece-square value for each pawn. Important: array value [0]
	 * corresponds to square a1; [63] == h8. For black, the position as given below corresponds to the actual board, i.e. a1
	 * is bottom RHS [63]
	 */
	private static int[] SQUARE_VALUE =
	// @formatter:off
            new int[]{
                    0, 0, 0, 0, 0, 0, 0, 0,
                    5, 10, 10, -20, -20, 10, 10, 5,
                    5, -5, -10, 0, 0, -10, -5, 5,
                    0, 0, 0, 20, 20, 0, 0, 0,
                    5, 5, 10, 25, 25, 10, 5, 5,
                    10, 10, 20, 30, 30, 20, 10, 10,
                    50, 50, 50, 50, 50, 50, 50, 50,
                    0, 0, 0, 0, 0, 0, 0, 0};
    // @formatter:on

	private static MoveHelper[] helper;

	static {
		helper = new MoveHelper[Colour.values().length];
		helper[Colour.WHITE.ordinal()] = new WhiteMoveHelper();
		helper[Colour.BLACK.ordinal()] = new BlackMoveHelper();
	}

	@Override
	public int calculatePieceSquareValue() {
		return AbstractBitBoardPiece.pieceSquareValue(pieces, getColour(), PIECE_VALUE, SQUARE_VALUE);
	}

	/**
	 * Constructs the Pawn class -- with no pawns on the board. Delegates to Pawn(Colour, boolean) with parameter false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public Pawn(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the Pawn class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public Pawn(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the Pawn class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
	 *           the board.
	 */
	public Pawn(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the Pawn class with the required squares (can be null) or the default start squares. Setting
	 * <code>startPosition</code> true has precedence over <code>startSquares</code>.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
	 *           ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pawns are placed on
	 *           the board.
	 */
	public Pawn(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.PAWN);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition(Square... requiredSquares) {
		if (requiredSquares != null) {
			for (Square square : requiredSquares) {
				if (square.rank() == 0 || square.rank() == 7) {
					throw new RuntimeException("cannot init Pawn on square: " + square);
				}
			}
		}
		super.initPosition(requiredSquares);
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = null;
		// @formatter:off
        requiredSquares = getColour() == Colour.WHITE
                ? new Square[]{Square.a2, Square.b2, Square.c2, Square.d2, Square.e2, Square.f2, Square.g2,
                Square.h2}
                : new Square[]{Square.a7, Square.b7, Square.c7, Square.d7, Square.e7, Square.f7, Square.g7,
                Square.h7};
        // @formatter:on
		initPosition(requiredSquares);
	}

	@Override
	public List<Move> findMoves(Position posn,
			boolean kingInCheck,
			PositionInfo posnInfo) {
		List<Move> moves = _findPotentialMoves(posn, posnInfo, posnInfo.getSquaresToBlockCheck(), posnInfo.isKingInCheck());

		final Square myKing = posn.getKingPosition(colour);
		final Colour opponentsColour = Colour.oppositeColour(colour);
		// make sure my king is not/no longer in check
		moves.removeIf(move -> KingCheck.isKingInCheck(posn, move, opponentsColour, myKing, kingInCheck));
		return moves;
	}

	private List<Move> _findPotentialMoves(Position posn,
			PositionInfo posnInfo,
			BitBoard checkRestriction,
			boolean isInCheck) {
		/*
		 * The pawn move is complicated by the different directions for white and black pawns. This is the only piece to have
		 * this complication. This difference is catered for by the <code>MoveHelper</code> implementations.
		 */

		List<Move> moves = new ArrayList<>();
		//
		// 1) one square forward
		// 2) two squares forward
		// 3) capture left
		// 4) capture right
		// 5) enpassant
		// 6) promotion
		//
		moves.addAll(calculateForwardMoves(posn, posnInfo, helper[getColour().ordinal()], checkRestriction, isInCheck));
		moves.addAll(captureLeft(posn, posnInfo, helper[getColour().ordinal()], checkRestriction, isInCheck));
		moves.addAll(captureRight(posn, posnInfo, helper[getColour().ordinal()], checkRestriction, isInCheck));

		return moves;
	}

	@Override
	public boolean doesMoveLeaveOpponentInCheck(Move move,
			Piece[] pieces,
			Square opponentsKing,
			BitBoard[] checkingBitboards) {
		if (move.isPromotion()) {
			return pieces[move.getPromotedPiece().ordinal()].doesMoveLeaveOpponentInCheck(move, pieces, opponentsKing, checkingBitboards);
		} else {
			return helper[getColour().ordinal()].doesPawnAttackSquare(opponentsKing, move.to());
		}
	}

	private List<Move> calculateForwardMoves(Position posn,
			PositionInfo posnInfo,
			MoveHelper helper,
			BitBoard checkRestriction,
			boolean isInCheck) {

		BitSetUnifier pawnsMinusPinnedPawns = pieces.cloneBitSet();
		// remove pinned pawns -- pinned if the pin ray is not north/south
		posnInfo.getPinnedPieces().stream()
				.filter(pi -> pi.getPiece() == PieceType.PAWN)
				.filter(pi -> !pi.getRay().isVertical())
				.forEach(pi -> pawnsMinusPinnedPawns.clear(pi.getBitIndex()));

		BitSetUnifier tmpBits = helper.moveOneRank(pawnsMinusPinnedPawns);
		tmpBits.andNot(posn.getTotalPieces().getBitSet()); // move must be to an empty square
		// clone tmpBits, to be able to carry on using it for 2 squares forward
		var oneSquareForward = ((BitSetUnifier) tmpBits.clone());
		if (isInCheck) {
			oneSquareForward.and(checkRestriction.getBitSet()); // respect square restrictions
		}

		var twoSquaresForward = helper.moveOneRank(tmpBits);
		twoSquaresForward.andNot(posn.getTotalPieces().getBitSet()); // move must be to an empty square
		// just take the pawns now on the 4th rank (relative to colour), since only these can have moved two squares
		twoSquaresForward.and(helper.fourthRank());
		if (isInCheck) {
			twoSquaresForward.and(checkRestriction.getBitSet()); // respect square restrictions
		}

		List<Move> moves = new ArrayList<>();
		moves.addAll(generateOneSquareForwardMoves(oneSquareForward, helper));
		moves.addAll(generateTwoSquareForwardMoves(twoSquaresForward, helper));
		return moves;
	}

	/**
	 * generates pawn moves from the given bitset. This only contains pawns which have moved one square forward to a
	 * non-empty square.
	 *
	 * @param oneSquareForward the bitset containing the pawns
	 * @param helper distinguishes between white and black sides, since the pawns move in different directions
	 * @return the moves (including promotion if applicable)
	 */
	private List<Move> generateOneSquareForwardMoves(BitSetUnifier oneSquareForward,
			MoveHelper helper) {
		List<Move> moves = new ArrayList<>(10);
		int offset = helper.getColour() == Colour.WHITE ? -8 : 8;
		for (int i = oneSquareForward.nextSetBit(0); i >= 0; i = oneSquareForward.nextSetBit(i + 1)) {
			Square from = Square.fromBitIndex(i + offset);
			Square to = Square.fromBitIndex(i);
			// promotion: extra check for pawns on the 8th rank
			if (helper.onLastRank(i)) {
				for (PieceType type : PieceType.getPieceTypesForPromotion()) {
					Move move = new Move(PieceType.PAWN, getColour(), from, to);
					move.setPromotionPiece(type);
					moves.add(move);
				}
			} else {
				moves.add(new Move(PieceType.PAWN, getColour(), from, to));
			}
		}
		return moves;
	}

	/**
	 * generates pawn moves from the given bitset. This should only contain pawns which have moved two squares forward to a
	 * non-empty square.
	 *
	 * @param twoSquaresForward the bitset containing the pawns
	 * @param helper distinguishes between white and black sides, since the pawns move in different directions
	 * @return the moves
	 */
	private List<Move> generateTwoSquareForwardMoves(BitSetUnifier twoSquaresForward,
			MoveHelper helper) {
		List<Move> moves = new ArrayList<>(10);
		int offset = helper.getColour() == Colour.WHITE ? -16 : 16;
		for (int i = twoSquaresForward.nextSetBit(0); i >= 0; i = twoSquaresForward.nextSetBit(i + 1)) {
			moves.add(new Move(PieceType.PAWN, getColour(), Square.fromBitIndex(i + offset), Square.fromBitIndex(i)));
		}
		return moves;
	}

	/**
	 * Helper method to check for captures 'left' or 'right'.
	 *
	 * @param position state of the board
	 * @param clonedPieces an already cloned bitset of the pawns (pinned pawns could already be removed here)
	 * @param helper distinguishes between white and black sides, since the pawns move in different directions
	 * @param captureLeft if true, check for captures 'left'. Otherwise, 'right'.
	 * @param checkRestriction info about the squares which come into consideration (e.g. when in check). Normally all are
	 *           allowed.
	 * @return list of moves found by this method
	 */
	private List<Move> capture(Position position,
			BitSetUnifier clonedPieces,
			MoveHelper helper,
			boolean captureLeft,
			BitBoard checkRestriction,
			boolean isInCheck) {

		BitSetUnifier captures;
		// generate bitset of pawn captures
		if (captureLeft) {
			captures = helper.pawnCaptureLeft(clonedPieces);
		} else {
			captures = helper.pawnCaptureRight(clonedPieces);
		}

		BitBoard opponentsPieces = position.getAllPieces(Colour.oppositeColour(helper.getColour()));
		// enpassant: add in enpassant square if necessary (cloning first)
		BitBoard squareRestrictionCloned = checkRestriction;
		if (position.getEnpassantSquare() != null) {
			opponentsPieces = new BitBoard(opponentsPieces); // clone
			opponentsPieces.set(position.getEnpassantSquare().bitIndex());
			// no need to set enpassant square (and therefore clone) if all squares are allowed already
			if (isInCheck) {
				squareRestrictionCloned = new BitBoard(checkRestriction); // clone
				squareRestrictionCloned.set(position.getEnpassantSquare());
			}
		}

		// move must be a capture, therefore AND with opponent's pieces (plus enpassant square)
		captures.and(opponentsPieces.getBitSet());

		// remove square restrictions (no point if not in check, since then we're 'and'ing with a completely set bitset)
		if (isInCheck) {
			captures.and(squareRestrictionCloned.getBitSet());

		}

		int offset = captureLeft ? helper.captureLeftOffset() : helper.captureRightOffset();

		List<Move> moves = new ArrayList<>();
		Colour oppositeColour = Colour.oppositeColour(getColour());
		for (int i = captures.nextSetBit(0); i >= 0; i = captures.nextSetBit(i + 1)) {
			Square targetSquare = Square.fromBitIndex(i);
			if (helper.onLastRank(i)) {
				// capture with promotion
				Square fromSquare = Square.fromBitIndex(i + offset);
				PieceType capturedPiece = position.pieceAt(targetSquare, oppositeColour);
				for (PieceType type : PieceType.getPieceTypesForPromotion()) {
					Move move = new Move(PieceType.PAWN, getColour(), fromSquare, targetSquare, capturedPiece);
					move.setPromotionPiece(type);
					moves.add(move);
				}
			} else {
				PieceType capturedPiece;
				boolean enpassant = false;
				if (targetSquare == position.getEnpassantSquare()) {
					capturedPiece = PieceType.PAWN;
					enpassant = true;
				} else {
					capturedPiece = position.pieceAt(targetSquare, oppositeColour);
				}
				Move move;
				if (enpassant) {
					move = Move.enpassant(getColour(), Square.fromBitIndex(i + offset), targetSquare);
				} else {
					move = new Move(PieceType.PAWN, getColour(), Square.fromBitIndex(i + offset), targetSquare, capturedPiece);
				}
				moves.add(move);
			}
		}
		return moves;
	}

	/**
	 * Captures 'left' from white's POV e.g. b3xa4 or for a black move e.g. b6xa5.
	 *
	 * @param chessboard state of the board
	 * @param helper distinguishes between white and black sides, since the pawns move in different directions
	 * @param checkRestriction a bitboard of squares which come into consideration (e.g. when in check). Normally all are
	 *           allowed.
	 * @return list of moves found by this method
	 */
	private List<Move> captureLeft(Position chessboard,
			PositionInfo posnInfo,
			MoveHelper helper,
			BitBoard checkRestriction,
			boolean isInCheck) {
		BitSetUnifier pawnsMinusPinnedPawns = pieces.cloneBitSet();
		// remove pinned pawns -- pinned if not pinned along the direction of the capture or its opposite
		posnInfo.getPinnedPieces().stream()
				.filter(pi -> pi.getPiece() == PieceType.PAWN)
				.filter(pi -> !helper.isCaptureLeftRayOrOpposite(pi.getRay()))
				.forEach(pi -> pawnsMinusPinnedPawns.clear(pi.getBitIndex()));

		return capture(chessboard, pawnsMinusPinnedPawns, helper, true, checkRestriction, isInCheck);
	}

	/**
	 * Captures 'right' from white's POV e.g. b3xc4 or for a black move e.g. b6xc5.
	 *
	 * @param chessboard state of the board
	 * @param helper distinguishes between white and black sides, since the pawns move in different directions
	 * @param checkRestriction a bitboard of squares which come into consideration (e.g. when in check). Normally all are
	 *           allowed.
	 * @return list of moves found by this method
	 */
	private List<Move> captureRight(Position chessboard,
			PositionInfo posnInfo,
			MoveHelper helper,
			BitBoard checkRestriction,
			boolean isInCheck) {

		BitSetUnifier pawnsMinusPinnedPawns = pieces.cloneBitSet();
		// remove pinned pawns -- pinned if not pinned along the direction of the capture or its opposite
		posnInfo.getPinnedPieces().stream()
				.filter(pi -> pi.getPiece() == PieceType.PAWN)
				.filter(pi -> !helper.isCaptureRightRayOrOpposite(pi.getRay()))
				.forEach(pi -> pawnsMinusPinnedPawns.clear(pi.getBitIndex()));

		return capture(chessboard, pawnsMinusPinnedPawns, helper, false, checkRestriction, isInCheck);
	}

	@Override
	public boolean attacksSquare(@SuppressWarnings("unused") BitSetUnifier emptySquares,
			Square targetSq,
			@SuppressWarnings("unused") PositionCheckState checkCache) {
		return helper[getColour().ordinal()].doPawnsAttackSquare(targetSq, pieces.getBitSet()) != -1;
	}

	/**
	 * Whether one or more of the pawns described in 'pawns' attack the square 'targetSq'.
	 *
	 * @param targetSq square to be attacked
	 * @param colour colour of the pawns in <code>pawns</code>.
	 * @param pawns bitset describing where the pawns are
	 * @return bit index of the pawn which attacks <code>targetSq</code>, or -1.
	 */
	public static int attacksSquare(Square targetSq,
			Colour colour,
			BitSetUnifier pawns) {
		return helper[colour.ordinal()].doPawnsAttackSquare(targetSq, pawns);
	}

	/**
	 * Factors out the differences between white pawn moves (going up the board) and black pawn moves (going down).
	 */
	private interface MoveHelper {
		/**
		 * Shifts the given bitset one rank north or south.
		 *
		 * @param bs start bitset
		 * @return shifted bitset
		 */
		BitSetUnifier moveOneRank(BitSetUnifier bs);

		/**
		 * The fourth rank (4th or 5th depending on the colour). Used when calculating pawn moves 2 squares forward.
		 *
		 * @return The fourth rank
		 */
		BitSetUnifier fourthRank();

		/**
		 * returns true if the given bitIndex is on the 'last rank' of the board.
		 *
		 * @param bitIndex the bitIndex
		 * @return true if on last rank.
		 */
		boolean onLastRank(int bitIndex);

		/**
		 * Given the starting bitset, returns a new bitset representing the pawn capture 'to the right' as seen from white's
		 * POV, e.g. b3xc4 or for a black move e.g. b6xc5.
		 *
		 * @param startPosn starting bitset
		 * @return the shifted bitset
		 */
		BitSetUnifier pawnCaptureRight(BitSetUnifier startPosn);

		/**
		 * Given the starting bitset, returns a new bitset representing the pawn capture 'to the left' as seen from white's POV,
		 * e.g. b3xa4 or for a black move e.g. b6xa5.
		 *
		 * @param captureLeft starting bitset
		 * @return the shifted bitset
		 */
		BitSetUnifier pawnCaptureLeft(BitSetUnifier captureLeft);

		/**
		 * @return the colour represented by this helper class.
		 */
		Colour getColour();

		/**
		 * The last rank (1st or 8th depending on the colour).
		 *
		 * @return The last rank
		 */
		BitSetUnifier lastRank();

		/**
		 * All ranks apart from the last rank (1st or 8th depending on the colour).
		 *
		 * @return The last rank flipped, i.e. all ranks apart from the last rank.
		 */
		BitSetUnifier lastRankFlipped();

		/**
		 * The starting rank for the pawns (2nd or 6th) depending on the colour.
		 *
		 * @return The starting rank
		 */
		BitSetUnifier startRank();

		/**
		 * Returns true if at least one pawn attacks the given square.
		 *
		 * @param targetSq square to be considered
		 * @param pawns the pawn bitset
		 * @return bit index of the pawn which attacks the given square, or -1
		 */
		int doPawnsAttackSquare(Square targetSq,
				BitSetUnifier pawns);

		/**
		 * Returns true if a pawn on 'pawnSq' attacks 'targetSq'.
		 *
		 * @param targetSq square to be considered
		 * @param pawnSq the square where the pawn is
		 * @return true if the pawn attacks the target square
		 */
		boolean doesPawnAttackSquare(Square targetSq,
				Square pawnSq);

		/**
		 * Returns the bitset offset for captures 'right' relative to colour.
		 *
		 * @return the bitset offset, i.e. the difference between the target square and the pawn's square.
		 */
		int captureRightOffset();

		/**
		 * Returns the bitset offset for captures 'left' relative to colour.
		 *
		 * @return the bitset offset, i.e. the difference between the target square and the pawn's square.
		 */
		int captureLeftOffset();

		/**
		 * Returns true if the given ray matches the ray reflecting a capture 'left' or its opposite.
		 *
		 * @param true if the ray is the capture left ray (or its opposite)
		 */
		boolean isCaptureLeftRayOrOpposite(RayType ray);

		/**
		 * Returns true if the given ray matches the ray reflecting a capture 'right' or its opposite.
		 *
		 * @param true if the ray is the capture right ray (or its opposite)
		 */
		boolean isCaptureRightRayOrOpposite(RayType ray);
	}

	/**
	 * Implements the MoveHelper interface from white's POV.
	 */
	static class WhiteMoveHelper implements MoveHelper {

		@Override
		public BitSetUnifier moveOneRank(BitSetUnifier bs) {
			return BitSetHelper.shiftOneNorth(bs);
		}

		@Override
		public BitSetUnifier lastRank() {
			return BitBoard.RANK[7];
		}

		@Override
		public BitSetUnifier lastRankFlipped() {
			return BitBoard.EXCEPT_RANK[7];
		}

		@Override
		public Colour getColour() {
			return Colour.WHITE;
		}

		@Override
		public BitSetUnifier startRank() {
			return BitBoard.RANK[1];
		}

		@Override
		public BitSetUnifier fourthRank() {
			return BitBoard.RANK[3];
		}

		@Override
		public BitSetUnifier pawnCaptureLeft(BitSetUnifier startPosn) {
			if (startPosn.isEmpty()) {
				return startPosn;
			}
			startPosn.and(BitBoard.EXCEPT_FILE[0]); // only the pawns on the 2nd to 8th files (to avoid wraparound)
			long[] longArray = startPosn.toLongArray();
			if (longArray.length == 0) {
				return BitSetFactory.createBitSet(64);
			}
			return BitSetFactory.createBitSet(new long[] { longArray[0] << 7 });
		}

		@Override
		public BitSetUnifier pawnCaptureRight(BitSetUnifier startPosn) {
			if (startPosn.isEmpty()) {
				return startPosn;
			}
			startPosn.and(BitBoard.EXCEPT_FILE[7]); // only the pawns on the 1st to 7th files (to avoid wraparound)
			long[] longArray = startPosn.toLongArray();
			if (longArray.length == 0) {
				return BitSetFactory.createBitSet(64);
			}
			return BitSetFactory.createBitSet(new long[] { longArray[0] << 9 });
		}

		@Override
		public boolean onLastRank(int bitIndex) {
			return bitIndex > 55;
		}

		@Override
		public int doPawnsAttackSquare(Square targetSq,
				BitSetUnifier pawns) {
			if (targetSq.rank() < 2) {
				return -1;
			}
			final int index = targetSq.bitIndex();
			// attack from left
			if (targetSq.file() > 0 && pawns.get(index - 9)) {
				return index - 9;
			}
			// attack from right
			if (targetSq.file() < 7 && pawns.get(index - 7)) {
				return index - 7;
			}
			return -1;
		}

		@Override
		public boolean doesPawnAttackSquare(Square targetSq,
				Square pawnSq) {
			if (targetSq.rank() < 2) {
				return false;
			}
			final int index = targetSq.bitIndex();
			// attack from left
			if (targetSq.file() > 0 && pawnSq.bitIndex() == (index - 9)) {
				return true;
			}
			// attack from right
			return targetSq.file() < 7 && pawnSq.bitIndex() == (index - 7);
		}

		@Override
		public int captureRightOffset() {
			return -9;
		}

		@Override
		public int captureLeftOffset() {
			return -7;
		}

		@Override
		public boolean isCaptureLeftRayOrOpposite(RayType ray) {
			return ray == RayType.NORTHWEST || ray == RayType.SOUTHEAST;
		}

		@Override
		public boolean isCaptureRightRayOrOpposite(RayType ray) {
			return ray == RayType.NORTHEAST || ray == RayType.SOUTHWEST;
		}

	}

	/**
	 * Implements the MoveHelper interface from black's POV.
	 */
	static class BlackMoveHelper implements MoveHelper {

		@Override
		public BitSetUnifier moveOneRank(BitSetUnifier bs) {
			return BitSetHelper.shiftOneSouth(bs);
		}

		@Override
		public BitSetUnifier pawnCaptureRight(BitSetUnifier startPosn) {
			if (startPosn.isEmpty()) {
				return startPosn;
			}
			startPosn.and(BitBoard.EXCEPT_FILE[7]); // only the pawns on the 1st to 7th files (to avoid wraparound)
			long[] longArray = startPosn.toLongArray();
			if (longArray.length == 0) {
				return BitSetFactory.createBitSet(64);
			}
			return BitSetFactory.createBitSet(new long[] { longArray[0] >>> 7 });
		}

		@Override
		public BitSetUnifier pawnCaptureLeft(BitSetUnifier startPosn) {
			if (startPosn.isEmpty()) {
				return startPosn;
			}
			startPosn.and(BitBoard.EXCEPT_FILE[0]); // only the pawns on the 2nd to 8th files (to avoid wraparound)
			long[] longArray = startPosn.toLongArray();
			if (longArray.length == 0) {
				return BitSetFactory.createBitSet(64);
			}
			return BitSetFactory.createBitSet(new long[] { longArray[0] >>> 9 });
		}

		@Override
		public Colour getColour() {
			return Colour.BLACK;
		}

		@Override
		public BitSetUnifier lastRank() {
			return BitBoard.RANK[0];
		}

		@Override
		public BitSetUnifier lastRankFlipped() {
			return BitBoard.EXCEPT_RANK[0];
		}

		@Override
		public BitSetUnifier startRank() {
			return BitBoard.RANK[6];
		}

		@Override
		public BitSetUnifier fourthRank() {
			return BitBoard.RANK[4];
		}

		@Override
		public boolean onLastRank(int bitIndex) {
			return bitIndex < 8;
		}

		@Override
		public int doPawnsAttackSquare(Square targetSq,
				BitSetUnifier pawns) {
			if (targetSq.rank() > 5) {
				return -1;
			}
			final int index = targetSq.bitIndex();
			// attack from left
			if (targetSq.file() > 0 && pawns.get(index + 7)) {
				return index + 7;
			}
			// attack from right
			if (targetSq.file() < 7 && pawns.get(index + 9)) {
				return index + 9;
			}
			return -1;
		}

		@Override
		public boolean doesPawnAttackSquare(Square targetSq,
				Square pawnSq) {
			if (targetSq.rank() > 5) {
				return false;
			}
			final int index = targetSq.bitIndex();
			// attack from left
			if (targetSq.file() > 0 && pawnSq.bitIndex() == (index + 7)) {
				return true;
			}
			// attack from right
			return targetSq.file() < 7 && pawnSq.bitIndex() == (index + 9);
		}

		@Override
		public int captureRightOffset() {
			return 7;
		}

		@Override
		public int captureLeftOffset() {
			return 9;
		}

		@Override
		public boolean isCaptureLeftRayOrOpposite(RayType ray) {
			return ray == RayType.SOUTHWEST || ray == RayType.NORTHEAST;
		}

		@Override
		public boolean isCaptureRightRayOrOpposite(RayType ray) {
			return ray == RayType.SOUTHEAST || ray == RayType.NORTHWEST;
		}
	}

}
