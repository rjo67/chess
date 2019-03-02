package org.rjo.chess.pieces;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rjo.chess.CheckRestriction;
import org.rjo.chess.base.Colour;
import org.rjo.chess.base.Move;
import org.rjo.chess.base.MoveDistance;
import org.rjo.chess.base.PieceType;
import org.rjo.chess.base.Square;
import org.rjo.chess.base.SquareCache;
import org.rjo.chess.base.CastlingRightsSummary.CastlingRights;
import org.rjo.chess.base.Move.CheckInformation;
import org.rjo.chess.base.bits.BitSetFactory;
import org.rjo.chess.base.bits.BitSetUnifier;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.KingCheck;
import org.rjo.chess.position.Position;
import org.rjo.chess.position.PositionCheckState;

/**
 * Stores information about the king in the game.
 *
 * @author rich
 * @see 'http://chessprogramming.wikispaces.com/King+Pattern'
 */
public class King extends AbstractSetPiece {
    private static final Logger LOG = LogManager.getLogger(King.class);

    /**
     * piece value in centipawns
     */
    private static final int PIECE_VALUE = 20000;

    /**
     * set true when we're in the endgame
     */
    public static boolean IN_ENDGAME = false;

    /**
     * stores the piece-square values. http://chessprogramming.wikispaces.com/Simplified+evaluation+function
     */
    // Important: array value [0] corresponds to square a1; [63] == h8.
    private static final int[] SQUARE_VALUE_MIDDLEGAME =
            // @formatter:off
            new int[]{
                    20, 30, 10, 0, 0, 10, 30, 20,
                    20, 20, 0, 0, 0, 0, 20, 20,
                    -10, -20, -20, -20, -20, -20, -20, -10,
                    -20, -30, -30, -40, -40, -30, -30, -20,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
                    -30, -40, -40, -50, -50, -40, -40, -30,
            };
    // @formatter:on
    private static final int[] SQUARE_VALUE_ENDGAME =
            // @formatter:off
            new int[]{
                    -50, -30, -30, -30, -30, -30, -30, -50,
                    -30, -30, 0, 0, 0, 0, -30, -30,
                    -30, -10, 20, 30, 30, 20, -10, -30,
                    -30, -10, 30, 40, 40, 30, -10, -30,
                    -30, -10, 30, 40, 40, 30, -10, -30,
                    -30, -10, 20, 30, 30, 20, -10, -30,
                    -30, -20, -10, 0, 0, -10, -20, -30,
                    -50, -40, -30, -20, -20, -30, -40, -50,
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
        CASTLING_SQUARES_NOT_IN_CHECK[Colour.WHITE.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new Square[]{Square.f1, Square.g1};
        CASTLING_SQUARES_NOT_IN_CHECK[Colour.WHITE.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new Square[]{Square.c1, Square.d1};
        CASTLING_SQUARES_NOT_IN_CHECK[Colour.BLACK.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new Square[]{Square.f8, Square.g8};
        CASTLING_SQUARES_NOT_IN_CHECK[Colour.BLACK.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new Square[]{Square.c8, Square.d8};
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
        CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new int[]{Square.f1.bitIndex(),
                Square.g1.bitIndex()};
        CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.WHITE.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new int[]{Square.b1.bitIndex(),
                Square.c1.bitIndex(), Square.d1.bitIndex()};
        CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()][CastlingRights.KINGS_SIDE.ordinal()] = new int[]{Square.f8.bitIndex(),
                Square.g8.bitIndex()};
        CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[Colour.BLACK.ordinal()][CastlingRights.QUEENS_SIDE.ordinal()] = new int[]{Square.b8.bitIndex(),
                Square.c8.bitIndex(), Square.d8.bitIndex()};
    }

    /**
     * Valid squares to move to
     */
    private static final BitSetUnifier[] MOVES = new BitSetUnifier[64];

    static {
        for (int i = 0; i < 64; i++) {
            BitSetUnifier myBitSet = BitSetFactory.createBitSet(64);
            myBitSet.set(i);

            /*
             * calculate left and right attack then shift up and down one rank
             */
            BitSetUnifier combined = BitSetHelper.shiftOneWest(myBitSet);
            BitSetUnifier east = BitSetHelper.shiftOneEast(myBitSet);
            combined.or(east);

            // save the current state
            BitSetUnifier possibleMoves = (BitSetUnifier) combined.clone();
            // now add the king's position again and shift up and down one rank
            combined.or(myBitSet);
            BitSetUnifier north = BitSetHelper.shiftOneNorth(combined);
            BitSetUnifier south = BitSetHelper.shiftOneSouth(combined);
            // add to result
            possibleMoves.or(north);
            possibleMoves.or(south);

            MOVES[i] = possibleMoves;
        }
    }

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
     * @param colour        indicates the colour of the pieces
     * @param startPosition if true, the default start squares are assigned. If false, no pieces are placed on the board.
     */
    public King(Colour colour, boolean startPosition) {
        this(colour, startPosition, (Square[]) null);
    }

    /**
     * Constructs the King class, defining the start squares.
     *
     * @param colour       indicates the colour of the pieces
     * @param startSquares the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
     *                     the board.
     */
    public King(Colour colour, Square... startSquares) {
        this(colour, false, startSquares);
    }

    /**
     * Constructs the King class with the required squares (can be null) or the default start squares. Setting
     * <code>startPosition</code> true has precedence over <code>startSquares</code>.
     *
     * @param colour        indicates the colour of the pieces
     * @param startPosition if true, the default start squares are assigned. Value of <code>startSquares</code> will be
     *                      ignored.
     * @param startSquares  the required starting squares of the piece(s). Can be null, in which case no pieces are placed on
     *                      the board.
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
        requiredSquares = getColour() == Colour.WHITE ? new Square[]{Square.e1} : new Square[]{Square.e8};
        initPosition(requiredSquares);
    }

    @Override
    public void initPosition(Square... requiredSquares) {
        if (requiredSquares != null) {
            if (requiredSquares.length > 1) {
                throw new IllegalArgumentException("king cannot have more than one start square");
            }
            pieces = new HashSet<>(1);
            pieces.add(requiredSquares[0]);
        }
    }

    @Override
    public int calculatePieceSquareValue() {

        int bitIndex = pieces.iterator().next().bitIndex();

        int[] values = IN_ENDGAME ? SQUARE_VALUE_ENDGAME : SQUARE_VALUE_MIDDLEGAME;
        int offset = getColour() == Colour.WHITE ? bitIndex : 63 - bitIndex;

        return PIECE_VALUE + values[offset];
    }

    @Override
    public void addPiece(@SuppressWarnings("unused") Square square) {
        throw new IllegalStateException("cannot add king!?");
    }

    @Override
    public void removePiece(@SuppressWarnings("unused") Square square) {
        throw new IllegalStateException("cannot remove king!?");
    }

    /**
     * checks if castling is possible. the approriate squares must be empty and not in check from other pieces.
     *
     * @param posn           the current position
     * @param myColour       my colour
     * @param oppositeColour opponent's colour
     * @param castlingRights which way to castle
     * @return true if castling is possible
     */
    public static boolean isCastlingLegal(Position posn,
                                          Colour myColour,
                                          Colour oppositeColour,
                                          CastlingRights castlingRights) {
        boolean canCastle = true;
        // check squares are empty
        BitSetUnifier totalPiecesBitSet = posn.getTotalPieces().getBitSet();
        for (int bitIndex : CASTLING_SQUARES_WHICH_MUST_BE_EMPTY[myColour.ordinal()][castlingRights.ordinal()]) {
            if (canCastle) {
                canCastle = !totalPiecesBitSet.get(bitIndex);
            } else {
                break;
            }
        }
        if (!canCastle) {
            return false;
        }
        // check squares are not attacked by an enemy piece
        for (Square sq : CASTLING_SQUARES_NOT_IN_CHECK[myColour.ordinal()][castlingRights.ordinal()]) {
            if (canCastle) {
                canCastle = !posn.squareIsAttacked(sq, oppositeColour);
            } else {
                break;
            }
        }
        return canCastle;
    }

    @Override
    public List<Move> findMoves(Position posn,
                                CheckInformation kingInCheck,
                                CheckRestriction checkRestriction) {
        StopWatch stopwatch = new StopWatch();
        List<Move> moves = findPotentialMoves(posn, checkRestriction);

        long time1 = stopwatch.getTime();

        final Colour oppositeColour = Colour.oppositeColour(getColour());

        // make sure king is not/no longer in check
        Iterator<Move> iter = moves.listIterator();
        while (iter.hasNext()) {
            Move move = iter.next();
            // castling -- can't castle out of check or over a square in check
            if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
                if (kingInCheck.isCheck()) {
                    iter.remove();
                } else {
                    if (move.isCastleKingsSide() && !isCastlingLegal(posn, getColour(), oppositeColour, CastlingRights.KINGS_SIDE)) {
                        iter.remove();
                    } else if (move.isCastleQueensSide() && !isCastlingLegal(posn, getColour(), oppositeColour, CastlingRights.QUEENS_SIDE)) {
                        iter.remove();
                    }
                }
            } else {
                Square myKing = move.to();
                if (KingCheck.isKingInCheck(posn, move, Colour.oppositeColour(getColour()), myKing, kingInCheck.isCheck())) {
                    iter.remove();
                }
            }
        }

        long time3 = stopwatch.getTime();
        if (time3 != 0) {
            LOG.debug("found " + moves.size() + " moves in " + time1 + "," + time3 + ", fen: " + Fen.encode(posn));
        }
        return moves;
    }

    @Override
    public List<Move> findPotentialMoves(Position posn,
                                         @SuppressWarnings("unused") CheckRestriction checkRestriction) {
        List<Move> moves = new ArrayList<>();

        Square kingsSquare = pieces.iterator().next();
        final Colour oppositeColour = Colour.oppositeColour(colour);
        Square opponentsKingSquare = posn.getKingPosition(oppositeColour);

        BitSetUnifier possibleMoves = (BitSetUnifier) MOVES[kingsSquare.bitIndex()].clone();

        // move can't be to a square with a piece of the same colour on it
        possibleMoves.andNot(posn.getAllPieces(colour).getBitSet());

        // can't move adjacent to opponent's king
        possibleMoves.andNot(MOVES[opponentsKingSquare.bitIndex()]);

        /*
         * possibleMoves now contains the possible moves apart from castling. (Moving the king to an attacked square has not
         * been checked yet.)
         */

        BitSetUnifier opponentsPieces = posn.getAllPieces(oppositeColour).getBitSet();
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

        // castling added if possible (without checking if legal)
        if (posn.canCastle(getColour(), CastlingRights.KINGS_SIDE)) {
            moves.add(Move.castleKingsSide(getColour()));
        }
        if (posn.canCastle(getColour(), CastlingRights.QUEENS_SIDE)) {
            moves.add(Move.castleQueensSide(getColour()));
        }

        return moves;
    }

    @Override
    public CheckInformation isOpponentsKingInCheckAfterMove(Position posn,
                                                            Move move,
                                                            Square opponentsKing,
                                                            @SuppressWarnings("unused") BitSetUnifier emptySquares,
                                                            PositionCheckState checkCache,
                                                            SquareCache<Boolean> discoveredCheckCache) {
        // checks: a king move can only give check if (a) castled with check or (b) discovered check
        /*
         * all king moves have the same starting square. If we've already checked for discovered check for this square, then can
         * use the cached result. (Discovered check only looks along one ray from move.from() to the opponent's king.)
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
            CheckInformation checkInfo = CheckInformation.NOT_CHECK;
            if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
                if (SlidingPiece.attacksSquareRankOrFile(posn.getEmptySquares(), move.getRooksCastlingMove().to(), opponentsKing,
                        checkCache, move.isCapture(), move.isPromotion())) {
                    checkInfo = new CheckInformation(PieceType.ROOK, move.getRooksCastlingMove().to());
                }
            }
            return checkInfo;
        }
    }

    @Override
    public boolean attacksSquare(@SuppressWarnings("unused") BitSetUnifier emptySquares,
                                 Square sq,
                                 @SuppressWarnings("unused") PositionCheckState checkCache) {
        return MoveDistance.calculateDistance(pieces.iterator().next(), sq) == 1;
    }
}
