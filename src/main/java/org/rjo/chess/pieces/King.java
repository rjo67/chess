package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.CastlingRights;
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
public class King extends AbstractBitBoardPiece {
	private static final Logger LOG = LogManager.getLogger(King.class);

	/** piece value in centipawns */
	private static final int PIECE_VALUE = 20000;

	/**
	 * stores the piece-square values.
	 * http://chessprogramming.wikispaces.com/Simplified+evaluation+function
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

	@Override
	public int calculatePieceSquareValue() {
		return AbstractBitBoardPiece.pieceSquareValue(pieces.getBitSet(), getColour(), PIECE_VALUE,
				SQUARE_VALUE_MIDDLEGAME);
		// TODO ENDGAME
	}

	/**
	 * Which squares cannot be attacked when castling.
	 */
	private static final Map<Colour, Map<CastlingRights, Square[]>> CASTLING_SQUARES_NOT_IN_CHECK;

	static {
		CASTLING_SQUARES_NOT_IN_CHECK = new HashMap<>();
		Map<CastlingRights, Square[]> tmp = new HashMap<>();
		tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f1, Square.g1 });
		tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.c1, Square.d1 });
		CASTLING_SQUARES_NOT_IN_CHECK.put(Colour.WHITE, tmp);
		tmp = new HashMap<>();
		tmp.put(CastlingRights.KINGS_SIDE, new Square[] { Square.f8, Square.g8 });
		tmp.put(CastlingRights.QUEENS_SIDE, new Square[] { Square.c8, Square.d8 });
		CASTLING_SQUARES_NOT_IN_CHECK.put(Colour.BLACK, tmp);
	}

	/**
	 * Which squares need to be empty when castling.
	 */
	private static final Map<CastlingRights, BitSet>[] CASTLING_SQUARES_WHICH_MUST_BE_EMPTY;

	static {
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY = new Map[2];
		BitSet bs = new BitSet(64);
		bs.set(Square.f1.bitIndex());
		bs.set(Square.g1.bitIndex());
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()] = new HashMap<>(2);
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()].put(CastlingRights.KINGS_SIDE, bs);
		bs = new BitSet(64);
		bs.set(Square.b1.bitIndex());
		bs.set(Square.c1.bitIndex());
		bs.set(Square.d1.bitIndex());
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()].put(CastlingRights.QUEENS_SIDE, bs);
		bs = new BitSet(64);
		bs.set(Square.f8.bitIndex());
		bs.set(Square.g8.bitIndex());
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()] = new HashMap<>(2);
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()].put(CastlingRights.KINGS_SIDE, bs);
		bs = new BitSet(64);
		bs.set(Square.b8.bitIndex());
		bs.set(Square.c8.bitIndex());
		bs.set(Square.d8.bitIndex());
		CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()].put(CastlingRights.QUEENS_SIDE, bs);
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
	 * Constructs the King class -- with no pieces on the board. Delegates to King(Colour, boolean)
	 * with parameter false.
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
	 * @param startPosition if true, the default start squares are assigned. If false, no pieces are
	 *           placed on the board.
	 */
	public King(Colour colour, boolean startPosition) {
		this(colour, startPosition, (Square[]) null);
	}

	/**
	 * Constructs the King class, defining the start squares.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case
	 *           no pieces are placed on the board.
	 */
	public King(Colour colour, Square... startSquares) {
		this(colour, false, startSquares);
	}

	/**
	 * Constructs the King class with the required squares (can be null) or the default start
	 * squares. Setting 'startPosition' true has precedence over 'startSquares'.
	 *
	 * @param colour indicates the colour of the pieces
	 * @param startPosition if true, the default start squares are assigned. Value of 'startSquares'
	 *           will be ignored.
	 * @param startSquares the required starting squares of the piece(s). Can be null, in which case
	 *           no pieces are placed on the board.
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
	public List<Move> findMoves(Position posn, boolean kingInCheck) {
		Stopwatch stopwatch = new Stopwatch();
		List<Move> moves = new ArrayList<>();

		Square kingPosn = Square.fromBitIndex(pieces.getBitSet().nextSetBit(0));
		Square opponentsKingSquare = findOpponentsKing(posn);

		BitSet possibleMoves = (BitSet) MOVES[kingPosn.bitIndex()].clone();

		// move can't be to a square with a piece of the same colour on it
		possibleMoves.andNot(posn.getAllPieces(getColour()).getBitSet());

		// can't move adjacent to opponent's king
		possibleMoves.andNot(MOVES[opponentsKingSquare.bitIndex()]);

		/*
		 * possibleMoves now contains the possible moves apart from castling. (Moving the king to an
		 * attacked square has not been checked yet.)
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
				moves.add(new Move(PieceType.KING, getColour(), kingPosn, targetSquare,
						posn.pieceAt(targetSquare, oppositeColour)));
			} else {
				moves.add(new Move(PieceType.KING, getColour(), kingPosn, targetSquare));
			}
		}
		long time1 = stopwatch.read();

		// castling -- can't castle out of check
		if (!kingInCheck && posn.canCastle(getColour(), CastlingRights.KINGS_SIDE)) {
			// check squares are empty
			BitSet bs = (BitSet) CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[getColour().ordinal()].get(CastlingRights.KINGS_SIDE)
					.clone();
			bs.and(posn.getTotalPieces().flip());
			boolean canCastle = bs.cardinality() == 2;
			if (canCastle) {
				// check squares are not attacked by an enemy piece
				for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK.get(getColour()).get(CastlingRights.KINGS_SIDE)) {
					if (canCastle) {
						canCastle = canCastle && !posn.squareIsAttacked(sq, oppositeColour);
					}
				}
			}
			if (canCastle) {
				moves.add(Move.castleKingsSide(getColour()));
			}
		}
		if (!kingInCheck && posn.canCastle(getColour(), CastlingRights.QUEENS_SIDE)) {
			// check squares are empty
			BitSet bs = (BitSet) CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[getColour().ordinal()]
					.get(CastlingRights.QUEENS_SIDE).clone();
			bs.and(posn.getTotalPieces().flip());
			boolean canCastle = bs.cardinality() == 3;
			if (canCastle) {
				// check squares are not attacked by an enemy piece
				for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK.get(getColour()).get(CastlingRights.QUEENS_SIDE)) {
					if (canCastle) {
						canCastle = canCastle && !posn.squareIsAttacked(sq, oppositeColour);
					}
				}
			}
			if (canCastle) {
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
		// checks: a king move can only give check if (a) castled with check or
		// (b) discovered check

		/*
		 * all king moves have the same starting square. If we've already checked for discovered check
		 * for this square, then can use the cached result. (Discovered check only looks along one ray
		 * from move.from() to the opponent's king.)
		 */
		Map<Square, Boolean> discoveredCheckCache = new HashMap<>(2);
		for (Move move : moves) {
			boolean isCheck;
			if (discoveredCheckCache.containsKey(move.from())) {
				isCheck = discoveredCheckCache.get(move.from());
			} else {
				isCheck = Position.checkForDiscoveredCheck(posn, move, getColour(), opponentsKingSquare);
				discoveredCheckCache.put(move.from(), isCheck);
			}
			if (!isCheck) {
				if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
					isCheck = SlidingPiece.attacksSquareRankOrFile(posn.getTotalPieces().flip(),
							move.getRooksCastlingMove().to(), opponentsKingSquare);
				}
			}
			move.setCheck(isCheck);
		}

		long time4 = stopwatch.read();
		if (time4 != 0) {
			LOG.debug("found " + moves.size() + " moves in " + time1 + "," + time2 + "," + time3 + "," + time4 + ", fen: "
					+ Fen.encode(posn));
		}
		return moves;
	}

	private Square findOpponentsKing(Position chessboard) {
		return findOpponentsKing(getColour(), chessboard);
	}

	/**
	 * Locates the enemy's king. TODO store this value in 'Game' after each move, to make this lookup
	 * quicker.
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
	 * @param chessboard the board
	 * @return location of this colour's king.
	 */
	public static Square findKing(Colour colour, Position chessboard) {
		return chessboard.getPieces2(colour)[PieceType.KING.ordinal()].getLocations()[0];
	}

	@Override
	public boolean attacksSquare(BitSet notused, Square sq) {
		return MoveDistance.calculateDistance(getLocations()[0], sq) == 1;
	}
}
