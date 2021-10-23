package org.rjo.newchess.game;

import java.util.List;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.move.MoveGenerator;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

/**
 * Stores information about a position. All info is immutable, i.e. must be copied on write.
 * 
 * @author rich
 * @since  2021
 */
public class Position {

   private final static SquareInfo UNOCCUPIED_SQUARE = new SquareInfo(null, Colour.UNOCCUPIED);
   /** enables sanity checks during move processing */
   private static final boolean TEST_IF_VALID = true;

   public static record SquareInfo(PieceType pieceType, Colour colour) {
   }

   SquareInfo[] board;// package protected for tests

   // keeps track on who can still castle
   // 1st dimension: W/B, 2nd dimension: 0 - king's side, 1 - queen's side
   boolean[][] castlingRights; // package protected for tests

   private Square enpassantSquare; // set if previous move was a pawn moving 2 squares forward
   int[] kingsSquare; // keep track of where the kings are ( package protected for tests)
   private Colour sideToMove;

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

   public void setCastlingsRights(boolean[][] castlingRights) {
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

      // update kingssquare if king moved
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
   }

   public List<Move> findMoves(Colour sideToMove) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * @return a FEN string for this position (FEN is incomplete, missing half moves and clock info).
    * @see    {@link Game#getFen()}.
    */
   public String getFen() {
      return Fen.encode(this);
   }

}
