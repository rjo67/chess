package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.BitBoard;
import org.rjo.chess.CastlingRights;
import org.rjo.chess.CheckStates;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Move;
import org.rjo.chess.MoveDistance;
import org.rjo.chess.Position;
import org.rjo.chess.Square;
import org.rjo.chess.util.Stopwatch;

/**
 * Stores information about the king in the game.
 *
 * @author rich
 * @see http://chessprogramming.wikispaces.com/King+Pattern
 */
public class King extends AbstractPiece {
	private static final Logger LOG = LogManager.getLogger(King.class);

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 20000;

	/**
	 * stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function
	 */
	// Important: array value [0] corresponds to square a1; [63] == h8.
	private static final int[] SQUARE_VALUE_MIDDLEGAME =
		// @formatter:off
      new int[] {
         20, 30, 10,  0,  0, 10, 30, 20,
         20, 20,  0,  0,  0,  0, 20, 20,
         -10,-20,-20,-20,-20,-20,-20,-10,
         -20,-30,-30,-40,-40,-30,-30,-20,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
   };
   // @formatter:on
	private static final int[] SQUARE_VALUE_ENDGAME =
		// @formatter:off
      new int[] {
         -50,-30,-30,-30,-30,-30,-30,-50,
         -30,-30,  0,  0,  0,  0,-30,-30,
         -30,-10, 20, 30, 30, 20,-10,-30,
         -30,-10, 30, 40, 40, 30,-10,-30,
         -30,-10, 30, 40, 40, 30,-10,-30,
         -30,-10, 20, 30, 30, 20,-10,-30,
         -30,-20,-10,  0,  0,-10,-20,-30,
         -50,-40,-30,-20,-20,-30,-40,-50,
   }; // @formatter:on

	/**
	 * Which squares cannot be attacked when castling. <br>
	 * 1st dimension: Colour.<br>
	 * 2nd: CastlingRights:<br>
	 * 3rd: Squares
	 */
	private static final Square[][][] CASTLING_SQUARES_NOT_IN_CHECK;

	static {
		CASTLING_SQUARES_NOT_IN_CHECK = new Square[Colour.values().length][CastlingRights.values().length][];
		CASTLING_SQUARES_NOT_IN_CHECK[Colour.WHITE.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new Square[] { Square.f1, Square.g1 };
		CASTLING_SQUARES_NOT_IN_CHECK[Colour.WHITE.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new Square[] { Square.c1, Square.d1 };
		CASTLING_SQUARES_NOT_IN_CHECK[Colour.BLACK.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new Square[] { Square.f8, Square.g8 };
		CASTLING_SQUARES_NOT_IN_CHECK[Colour.BLACK.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new Square[] { Square.c8, Square.d8 };

	}

	/**
	 * Which squares need to be empty when castling.<br>
	 * 1st dimension: Colour.<br>
	 * 2nd: CastlingRights:<br>
	 * 3rd: bitindices of Squares
	 */
	private static final int[][][] CASTLING_SQUARES_WHICH_MUST_BE_EMPTY;

	static {
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY = new int[Colour.values().length][CastlingRights.values().length][];
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new int[] { Square.f1.bitIndex(),
				Square.g1.bitIndex() };
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new int[] { Square.b1.bitIndex(),
				Square.c1.bitIndex(), Square.d1.bitIndex() };
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new int[] { Square.f8.bitIndex(),
				Square.g8.bitIndex() };
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new int[] { Square.b8.bitIndex(),
				Square.c8.bitIndex(), Square.d8.bitIndex() };

	}

	/**
	 * Valid squares to move to
	 */
	private static final BitSet[] MOVES = new BitSet[64];

	static {
		for (int i = 0; i < 64; i++) {
			BitSet myBitSet = new BitSet(64);
			myBitSet.set(i);

			/*
			 * calculate left and right attack then shift up and down one rank
			 */
			BitSet combined = BitSetHelper.shiftOneWest(myBitSet);
			BitSet east = BitSetHelper.shiftOneEast(myBitSet);
			combined.or(east);

			// save the current state
			BitSet possibleMoves = (BitSet) combined.clone();
			// now add the king's position again and shift up and down one rank
			combined.or(myBitSet);
			BitSet north = BitSetHelper.shiftOneNorth(combined);
			BitSet south = BitSetHelper.shiftOneSouth(combined);
			// add to result
			possibleMoves.or(north);
			possibleMoves.or(south);

			MOVES[i] = possibleMoves;
		}
	}

	/**
	 * location of the king
	 */
	private Square kingsSquare;

	/**
	 * Constructs the King class -- with no pieces on the board. Delegates to King(Colour, boolean) with parameter false.
	 *
	 * @param colour indicates the colour of the pieces
	 */
	public King(Colour colour) {
		this(colour, false);
	}

	/**
	 * Constructs the King class.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
	 */
	public King(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the King class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the board.
	 */
	public King(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the King class with the required squares (can be null) or the default start squares. Setting 'startPosition' true has precedence over
	 * 'startSquares'.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of 'startSquares' will be ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on the board.
	 */
	public King(Colour colour, boolean startPosition, Square... startSquares) {
		super(colour, PieceType.KING);
		if (startPosition) {
			initPosition();
		} else {
			initPosition(startSquares);
		}
	}

	@Override
	public void initPosition() {
		Square[] requiredSquares = null;
		requiredSquares = getColour() == Colour.WHITE ? new Square[] { Square.e1 } : new Square[] { Square.e8 };
		initPosition(requiredSquares);
	}

	@Override
	public void initPosition(Square... requiredSquares) {
		if (requiredSquares != null) {
			if (requiredSquares.length > 1) {
				throw new IllegalArgumentException("king cannot have more than one start square");
			}
			kingsSquare = requiredSquares[0];
		}
	}

	@Override
	public boolean pieceAt(Square targetSquare) {
		return kingsSquare == targetSquare;
	}

	@Override
	public int calculatePieceSquareValue() {

		int bitIndex = kingsSquare.bitIndex();

		int sqValue;
		if (getColour() == Colour.WHITE) {
			// TODO ENDGAME
			sqValue = SQUARE_VALUE_MIDDLEGAME[bitIndex];
		} else {
			sqValue = SQUARE_VALUE_MIDDLEGAME[63 - bitIndex];
		}
		return PIECE_VALUE + sqValue;
	}

	@Override
	public void addPiece(Square square) {
		if (kingsSquare != null) {
			throw new IllegalStateException("cannot add more than one king");
		}
		kingsSquare = square;
	}

	@Override
	public Square[] getLocations() {
		return new Square[] { kingsSquare };
	}

	@Override
	public void removePiece(Square square) {
		throw new IllegalStateException("cannot remove king!?");
	}

	@Override
	public void move(Move move) {
		if (kingsSquare != move.from()) {
			throw new IllegalArgumentException("the king is not on square " + move.from() + ". Move=" + move);
		}
		kingsSquare = move.to();
	}

	@Override
	public BitBoard getBitBoard() {
		BitBoard bb = new BitBoard();
		if (kingsSquare != null) {
			bb.setBitsAt(kingsSquare);
		}
		return bb;
	}

	/**
	 * checks if castling is possible. the approriate squares must be empty and not in check from other pieces.
	 *
	 * @param posn the current position
	 * @param oppositeColour opponent's colour
	 * @param castlingRights which way to castle
	 * @return true if castling is possible
	 */
	private boolean isCastlingLegal(Position posn, Colour oppositeColour, CastlingRights castlingRights) {
		boolean canCastle = true;
		// check squares are empty
		for (int bitIndex : CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[getColour().ordinal()][castlingRights.ordinal()]) {
			if (canCastle) {
				canCastle = canCastle && !posn.getTotalPieces().getBitSet().get(bitIndex);
			}
		}
		if (!canCastle) {
			return false;
		}
		// check squares are not attacked by an enemy piece
		for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK[getColour().ordinal()][castlingRights.ordinal()]) {
			if (canCastle) {
				canCastle = canCastle && !posn.squareIsAttacked(sq, oppositeColour);
			}
		}
		return canCastle;
	}

	@Override
	public List<Move> findMoves(Position posn, boolean kingInCheck) {
		Stopwatch stopwatch = new Stopwatch();
		List<Move> moves = new ArrayList<>();

		Square opponentsKingSquare = findOpponentsKing(posn);

		BitSet possibleMoves = (BitSet) MOVES[kingsSquare.bitIndex()].clone();

		// move can't be to a square with a piece of the same colour on it
		possibleMoves.andNot(posn.getAllPieces(getColour()).getBitSet());

		// can't move adjacent to opponent's king
		possibleMoves.andNot(MOVES[opponentsKingSquare.bitIndex()]);

		/*
		 * possibleMoves now contains the possible moves apart from castling. (Moving the king to an attacked square has not been checked yet.)
		 */

		final Colour oppositeColour = Colour.oppositeColour(getColour());
		BitSet opponentsPieces = posn.getAllPieces(oppositeColour).getBitSet();
		// check the possibleMoves and store them as moves / captures.
		for (int i = possibleMoves.nextSetBit(0); i >= 0; i = possibleMoves.nextSetBit(i + 1)) {
			Square targetSquare = Square.fromBitIndex(i);
			/*
			 * store move as 'move' or 'capture'
			 */
			if (opponentsPieces.get(i)) {
				moves.add(new Move(PieceType.KING, getColour(), kingsSquare, targetSquare, posn.pieceAt(targetSquare, oppositeColour)));
			} else {
				moves.add(new Move(PieceType.KING, getColour(), kingsSquare, targetSquare));
			}
		}
		long time1 = stopwatch.read();

		// castling -- can't castle out of check
		if (!kingInCheck) {
			if (posn.canCastle(getColour(), CastlingRights.KINGS_SIDE) && isCastlingLegal(posn, oppositeColour, CastlingRights.KINGS_SIDE)) {
				moves.add(Move.castleKingsSide(getColour()));
			}
			if (posn.canCastle(getColour(), CastlingRights.QUEENS_SIDE) && isCastlingLegal(posn, oppositeColour, CastlingRights.QUEENS_SIDE)) {
				moves.add(Move.castleQueensSide(getColour()));
			}
		}
		long time2 = stopwatch.read();

		// make sure king is not/no longer in check
		Iterator<Move> iter = moves.listIterator();
		while (iter.hasNext()) {
			Move move = iter.next();
			Square myKing = move.to();
			if (Position.isKingInCheck(posn, move, Colour.oppositeColour(getColour()), myKing, kingInCheck)) {
				iter.remove();
			}
		}

		long time3 = stopwatch.read();
		if (time3 != 0) {
			LOG.debug("found " + moves.size() + " moves in " + time1 + "," + time2 + "," + time3 + ", fen: " + Fen.encode(posn));
		}
		return moves;
	}

	private Square findOpponentsKing(Position chessboard) {
		return findOpponentsKing(getColour(), chessboard);
	}

	@Override
	public boolean isOpponentsKingInCheckAfterMove(Position posn, Move move, Square opponentsKing, BitSet emptySquares,
			SquareCache<CheckStates> checkCache, SquareCache<Boolean> discoveredCheckCache) {
		// checks: a king move can only give check if (a) castled with check or (b) discovered check
		/*
		 * all king moves have the same starting square. If we've already checked for discovered check for this square, then can use the cached result.
		 * (Discovered check only looks along one ray from move.from() to the opponent's king.)
		 */
		boolean isCheck;
		Boolean lookup = discoveredCheckCache.lookup(move.from());
		if (lookup != null) {
			isCheck = lookup;
		} else {
			isCheck = Position.checkForDiscoveredCheck(posn, move, getColour(), opponentsKing);
			discoveredCheckCache.store(move.from(), isCheck);
		}
		if (!isCheck) {
			if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
				isCheck = SlidingPiece.attacksSquareRankOrFile(posn.getTotalPieces().flip(), move.getRooksCastlingMove().to(), opponentsKing);
			}
		}
		return isCheck;
	}

	/**
	 * Locates the enemy's king. TODO store this value in 'Game' after each move, to make this lookup quicker.
	 *
	 * @param myColour my colour
	 * @param chessboard the board
	 * @return location of the other colour's king.
	 */
	public static Square findOpponentsKing(Colour myColour, Position chessboard) {
		return findKing(Colour.oppositeColour(myColour), chessboard);
	}

	/**
	 * Locates the king (mine or the opponents).
	 *
	 * @param colour which colour king we want
	 * @param posn the board
	 * @return location of this colour's king.
	 */
	public static Square findKing(Colour colour, Position posn) {
		return posn.getPieces(colour)[PieceType.KING.ordinal()].getLocations()[0];
	}

	@Override
	public boolean attacksSquare(BitSet notused, Square sq, SquareCache<CheckStates> notused2) {
		return MoveDistance.calculateDistance(kingsSquare, sq) == 1;
	}
}
