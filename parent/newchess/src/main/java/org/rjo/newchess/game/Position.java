package org.rjo.newchess.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.board.Ray;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.move.MoveGenerator;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

/**
 * Stores information about a position.
 * 
 * <h2>Checks</h2> Using {@link #isKingInCheck(int, Colour, boolean, int, int)} the squares of enemy pieces attacking
 * the king will be found and stored in the position.
 * 
 * If a position is 'cloned' using {@link Position#Position(Position)} objects will only be shallow copied, and must
 * therefore be copied on write.
 * 
 * @author rich
 * @since  2021
 */
public class Position {

   private final static SquareInfo UNOCCUPIED_SQUARE = new SquareInfo(null, Colour.UNOCCUPIED);
   /** enables sanity checks during move processing */
   private static final boolean TEST_IF_VALID = true;

   /**
    * Stores information about a particular square.
    */
   public static record SquareInfo(PieceType pieceType, Colour colour) {
   }

   SquareInfo[] board;// package protected for tests

   // keeps track on who can still castle
   // 1st dimension: W/B, 2nd dimension: 0 - king's side, 1 - queen's side
   boolean[][] castlingRights; // package protected for tests

   private Square enpassantSquare; // set if previous move was a pawn moving 2 squares forward
   int[] kingsSquare; // keep track of where the kings are (package protected for tests)
   private Colour sideToMove;
   // if kingInCheck==TRUE, then either directCheckSquare or discoveredCheckSquare (or both) will be set
   private boolean kingInCheck; // TRUE if the king is now in check (i.e. the move leading to this posn has checked the king)
   private List<Integer> checkSquares; // set to the square(s) of the piece(s) delivering a check

   // mainly for tests
   public Position(Square whiteKingsSquare, Square blackKingsSquare) {
      this(new boolean[2][2], whiteKingsSquare, blackKingsSquare);
   }

   // mainly for tests
   public Position(boolean[][] castlingRights, Square whiteKingsSquare, Square blackKingsSquare) {
      this(castlingRights);
      addPiece(Colour.WHITE, PieceType.KING, whiteKingsSquare);
      addPiece(Colour.BLACK, PieceType.KING, blackKingsSquare);
   }

   public Position() {
      this(new boolean[2][2]);
   }

   public Position(boolean[][] castlingRights) {
      this.board = new SquareInfo[64];
      this.kingsSquare = new int[] { -1, -1 };
      for (int i = 0; i < 64; i++) {
         board[i] = UNOCCUPIED_SQUARE;
      }
      this.castlingRights = castlingRights;
      setSideToMove(Colour.WHITE);
   }

   /**
    * Copy constructor.
    * 
    * All information is "shallow" cloned. If it gets changed later the appropriate data structures must be fully cloned.
    * 
    * @param prevPosn position to copy
    */
   public Position(Position prevPosn) {
      this.castlingRights = prevPosn.castlingRights;
      this.enpassantSquare = prevPosn.enpassantSquare;
      this.kingsSquare = prevPosn.kingsSquare;
      this.sideToMove = prevPosn.sideToMove;
      this.kingInCheck = prevPosn.kingInCheck;
      this.board = prevPosn.board.clone();
   }

   public void addPiece(Colour colour, PieceType pieceType, int square) {
      if (!isEmpty(square)) { throw new IllegalStateException("there is already a " + pieceAt(square) + " at square " + Square.toSquare(square)); }
      if (pieceType == PieceType.KING) {
         if (kingsSquare[colour.ordinal()] != -1) {
            throw new IllegalStateException("a " + colour + " king has already been added at square " + Square.toSquare(kingsSquare[colour.ordinal()]));
         }
         kingsSquare[colour.ordinal()] = square;
      }
      board[square] = new SquareInfo(pieceType, colour);
   }

   public void addPiece(Colour colour, PieceType pieceType, Square square) {
      this.addPiece(colour, pieceType, square.index());
   }

   public boolean isEmpty(int square) {
      return colourOfPieceAt(square) == Colour.UNOCCUPIED;
   }

   public boolean isEmpty(Square sq) {
      return isEmpty(sq.index());
   }

   public Colour colourOfPieceAt(int square) {
      return board[square].colour();
   }

   public Object colourOfPieceAt(Square square) {
      return colourOfPieceAt(square.index());
   }

   public PieceType pieceAt(int square) {
      return board[square].pieceType();
   }

   public PieceType pieceAt(Square square) {
      return pieceAt(square.index());
   }

   public boolean canCastleKingsside(Colour col) {
      return castlingRights[col.ordinal()][0];
   }

   public boolean canCastleQueensside(Colour col) {
      return castlingRights[col.ordinal()][1];
   }

   public void setEnpassantSquare(Square sq) {
      this.enpassantSquare = sq;
   }

   public Square getEnpassantSquare() {
      return enpassantSquare;
   }

   public int getKingsSquare(Colour col) {
      return kingsSquare[col.ordinal()];
   }

   // delivers the 'raw' value of the square
   public SquareInfo raw(int square) {
      return board[square];
   }

   // delivers the 'raw' value of the square
   public SquareInfo raw(Square square) {
      return this.raw(square.index());
   }

   public Colour getSideToMove() {
      return sideToMove;
   }

   public void setSideToMove(Colour sideToMove) {
      this.sideToMove = sideToMove;
   }

   public void setCastlingRights(boolean[][] castlingRights) {
      this.castlingRights = castlingRights;
   }

   // displays the board (always from white POV, a1 in bottom LHS)
   @Override
   public String toString() {
      String[][] board = new String[8][8];

      // init
      for (int rank = 7; rank >= 0; rank--) {
         for (int file = 0; file < 8; file++) {
            int sq = ((7 - rank) * 8) + file;
            if (this.isEmpty(sq)) {
               board[rank][file] = ".";
            } else {
               PieceType pt = this.pieceAt(sq);
               Colour col = this.colourOfPieceAt(sq);
               board[rank][file] = pt.fenSymbol(col);
            }
         }
      }

      StringBuilder sb = new StringBuilder(150);
      for (int rank = 7; rank >= 0; rank--) {
         for (int file = 0; file < 8; file++) {
            sb.append(board[rank][file]);
         }
         switch (rank) {
         case 7:
            sb.append("   ").append(sideToMove).append(" to move");
            break;
         case 6:
            sb.append("   castlingRights: ").append(castlingRights[0]).append(", ").append(castlingRights[1]);
            break;
         case 5:
            sb.append("   enpassant square: ").append(enpassantSquare);
            break;
         case 4:
            sb.append("   hash (zobrist): ").append(hashCode());
            break;
         default:
            break;
         }
         sb.append("\n");
      }
      return sb.toString();
   }

   public Position move(Move move) {
      Position newPosn = new Position(this); // clone current position
      newPosn.processMove(move);
      return newPosn;
   }

   // process the given move, updating internal structures
   private void processMove(Move move) {

      int sideToMoveOrdinal = this.sideToMove.ordinal();
      // gets set to the new castling rights if they change with this move
      boolean[] newCastlingRights = null;

      if (TEST_IF_VALID) {
         if (move.getMovingPiece() != pieceAt(move.getOrigin())) {
            throw new IllegalStateException(String.format("invalid move %s, piece at %s is %s %s", move, Square.toSquare(move.getOrigin()),
                  colourOfPieceAt(move.getOrigin()), pieceAt(move.getOrigin())));
         }
         if (move.isCapture()) {
            if (move.isEnpassant()) {
               if (!isEmpty(move.getTarget())) {
                  throw new IllegalStateException(String.format("invalid enpassant move %s, target square is not empty", move));
               } else if (isEmpty(move.getSquareOfPawnCapturedEnpassant())) {
                  throw new IllegalStateException(
                        String.format("invalid enpassant move %s, square %s is empty", move, Square.toSquare(move.getSquareOfPawnCapturedEnpassant())));
               }
            } else if (isEmpty(move.getTarget()))
               throw new IllegalStateException(String.format("invalid capture move %s, target square is empty", move));
         }
         if (!move.isCapture() && !isEmpty(move.getTarget())) {
            throw new IllegalStateException(String.format("invalid non-capture move %s, target square is occupied with: %s %s", move,
                  colourOfPieceAt(move.getTarget()), pieceAt(move.getTarget())));
         }
         if (move.getColourOfMovingPiece() != this.sideToMove) {
            throw new IllegalStateException(String.format("invalid move %s, sideToMove is %s", move, sideToMove));
         }
      }
      // remove piece at move.origin, place piece at move.target (implicitly removing piece at move.target)
      board[move.getOrigin()] = UNOCCUPIED_SQUARE;
      board[move.getTarget()] = new SquareInfo((move.isPromotion() ? move.getPromotedPiece() : move.getMovingPiece()), move.getColourOfMovingPiece());
      if (move.isEnpassant()) { board[move.getSquareOfPawnCapturedEnpassant()] = UNOCCUPIED_SQUARE; }

      // move rook too if castling
      if (move.isKingssideCastling() || move.isQueenssideCastling()) {
         int sideOfBoard = move.isKingssideCastling() ? 0 : 1;
         int rookOriginSq = MoveGenerator.rooksCastlingSquareIndex[sideToMove.ordinal()][sideOfBoard];
         int rookTargetSq = MoveGenerator.rooksSquareAfterCastling[sideToMove.ordinal()][sideOfBoard];
         if (TEST_IF_VALID) {
            if (PieceType.ROOK != pieceAt(rookOriginSq)) {
               throw new IllegalStateException(String.format("invalid castling move %s, no rook at %s", move, Square.toSquare(rookOriginSq)));
            }
            if (!isEmpty(rookTargetSq)) {
               throw new IllegalStateException(
                     String.format("invalid castling move %s, rook's target sq %s is not empty", move, Square.toSquare(rookTargetSq)));
            }
            if (!this.castlingRights[sideToMoveOrdinal][sideOfBoard]) {
               throw new IllegalStateException(String.format("invalid move %s, castling no longer allowed", move));
            }
         }
         board[rookOriginSq] = UNOCCUPIED_SQUARE;
         board[rookTargetSq] = new SquareInfo(PieceType.ROOK, move.getColourOfMovingPiece());

         // update castlingrights
         newCastlingRights = move.isKingssideCastling() ? new boolean[] { false, this.castlingRights[sideToMoveOrdinal][1] }
               : new boolean[] { this.castlingRights[sideToMoveOrdinal][0], false };
      }

      // update enpassantSquare if pawn moved
      if (PieceType.PAWN == move.getMovingPiece() && move.isPawnTwoSquaresForward()) {
         this.enpassantSquare = Square.findEnpassantSquareFromMove(Square.toSquare(move.getTarget()));
      } else {
         this.enpassantSquare = null;
      }

      // update kingsSquare if king moved
      if (PieceType.KING == move.getMovingPiece()) {
         this.kingsSquare = this.kingsSquare.clone();
         this.kingsSquare[sideToMoveOrdinal] = move.getTarget();
      }

      // check if a rook moved from its starting square, therefore invalidating castling rights
      if (PieceType.ROOK == move.getMovingPiece()) {
         if (move.getOrigin() == MoveGenerator.rooksCastlingSquareIndex[sideToMoveOrdinal][0]) {
            newCastlingRights = new boolean[] { false, this.castlingRights[sideToMoveOrdinal][1] };
         } else if (move.getOrigin() == MoveGenerator.rooksCastlingSquareIndex[sideToMoveOrdinal][1]) {
            newCastlingRights = new boolean[] { this.castlingRights[sideToMoveOrdinal][0], false };
         }
      }

      // set new castling rights
      if (newCastlingRights != null) {
         this.castlingRights = this.castlingRights.clone();
         this.castlingRights[sideToMoveOrdinal] = newCastlingRights;
      }

      this.sideToMove = this.sideToMove.opposite();
      if (move.isCheck()) { this.setKingInCheck(move.getCheckSquares()); }
   }

   /**
    * Determines whether in <b>this position</b> the king of side 'colour' is currently in check. Does not take move info
    * into account.
    * 
    * This method is required when setting up a new position (e.g. from {@link Fen#decode(String)} where we don't have any
    * previous move info.
    * 
    * TODO remove 'captureSquare'
    * 
    * @param  kingsSquare   king's square
    * @param  colour        king's colour
    * @param  captureSquare if the move was a capture, this is the square where a piece was captured. Otherwise -1
    * @return               an empty list if king is not in check; otherwise, the squares with pieces which give check
    */
   public List<Integer> isKingInCheck(int kingsSquare, Colour colour, int captureSquare) {
      Colour opponentsColour = colour.opposite();
      List<Integer> checkSquares = new ArrayList<>(2);
      // *our* colour used to index pawnCaptures, because we want the 'inverse', i.e. squares which attack the given square
      for (int sq : MoveGenerator.pawnCaptures[colour.ordinal()][kingsSquare]) {
         if (sq != captureSquare && pieceAt(sq) == PieceType.PAWN && colourOfPieceAt(sq) == opponentsColour) {
            checkSquares.add(sq);
            break;
         }
      }

      // a pawn giving check ==> a knight cannot also be giving check
      if (checkSquares.isEmpty()) {
         for (int sq : MoveGenerator.knightMoves[kingsSquare]) {
            if (sq != captureSquare && pieceAt(sq) == PieceType.KNIGHT && colourOfPieceAt(sq) == opponentsColour) {
               checkSquares.add(sq);
               break;
            }
         }
      }

      for (Ray ray : Ray.values()) {
         Pair<PieceType, Integer> enemyPieceInfo = opponentsPieceOnRay(colour, kingsSquare, ray);
         PieceType enemyPiece = enemyPieceInfo.getLeft();
         int enemySquare = enemyPieceInfo.getRight();
         if (enemyPiece != null && enemySquare != captureSquare && enemyPiece.canSlideAlongRay(ray)) {
            checkSquares.add(enemySquare);
            if (checkSquares.size() == 2) { break; }
         }
      }

      return checkSquares;
   }

   /**
    * Returns the square and type of an opponent's piece on the given ray, starting from (but not including) startSq. If an
    * intervening piece of my colour is found first, returns (null, -1).
    * 
    * @param  myColour my colour
    * @param  startSq  where to start
    * @param  ray      direction
    * @return          the piece-type and square of the enemy piece, if found. If no piece or one of my pieces was found,
    *                  returns (null, -1)
    */
   public Pair<PieceType, Integer> opponentsPieceOnRay(Colour myColour, int startSq, Ray ray) {
      int enemySq = -1;
      PieceType enemyPiece = null;
      for (int potentialEnemySq : Ray.raysList[startSq][ray.ordinal()]) {
         Colour colourOfSq = colourOfPieceAt(potentialEnemySq);
         if (colourOfSq == Colour.UNOCCUPIED) { continue; }
         if (myColour.opposes(colourOfSq)) {
            enemyPiece = pieceAt(potentialEnemySq);
            enemySq = potentialEnemySq;
         }
         break; // can stop in any case, having found a piece
      }
      return Pair.of(enemyPiece, enemySq);
   }

   public List<Move> findMoves(Colour sideToMove) {
      return new MoveGenerator().findMoves(this, sideToMove);
   }

   public boolean isKingInCheck() {
      return kingInCheck;
   }

   public void setKingInCheck(List<Integer> checkSquares) {
      if (checkSquares == null || checkSquares.isEmpty()) {
         this.kingInCheck = false;
         this.checkSquares = null;
      } else {
         this.kingInCheck = true;
         this.checkSquares = checkSquares;
      }
   }

   public void setKingInCheck(Integer... checkSquares) {
      setKingInCheck(Arrays.asList(checkSquares));
   }

   /**
    * @return a FEN string for this position (FEN is incomplete, missing half moves and clock info).
    * @see    {@link Game#getFen()}.
    */
   public String getFen() {
      return Fen.encode(this);
   }

   public List<Integer> getCheckSquares() {
      return checkSquares;
   }

}