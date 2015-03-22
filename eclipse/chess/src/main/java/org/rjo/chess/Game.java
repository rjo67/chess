package org.rjo.chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

import org.rjo.chess.pieces.Piece;
import org.rjo.chess.pieces.PieceType;

/**
 * Encapsulates the board, the moves, castling rights, etc (clocks?).
 *
 * @author rich
 */
public class Game {

   private Chessboard chessboard;
   /** stores the moves (ply) */
   private Deque<Move> moves;
   /** move number of the next move. Not calculated from size of 'moves' since we don't have to start at move 1 */
   private int moveNbr;
   private EnumSet<CastlingRights>[] castling;
   /** which side is to move */
   private Colour sideToMove;
   /**
    * if the king (of the sideToMove) is currently in check. Normally deduced from the last move but can be set
    * delibarately for tests.
    */
   private boolean inCheck;

   /**
    * Constructs a game with the default start position.
    */
   public Game() {
      chessboard = new Chessboard();
      init(EnumSet.allOf(CastlingRights.class), EnumSet.allOf(CastlingRights.class));
   }

   /**
    * Inits a game with the given chessboard. Castling rights are set to 'empty'.
    *
    * @param chessboard
    *           the chessboard
    */
   public Game(Chessboard chessboard) {
      this.chessboard = chessboard;
      init(EnumSet.noneOf(CastlingRights.class), EnumSet.noneOf(CastlingRights.class));
   }

   private void init(EnumSet<CastlingRights> whiteCastlingRights, EnumSet<CastlingRights> blackCastlingRights) {
      moves = new ArrayDeque<>();
      castling = new EnumSet[Colour.values().length];
      castling[Colour.WHITE.ordinal()] = whiteCastlingRights;
      castling[Colour.BLACK.ordinal()] = blackCastlingRights;
      sideToMove = Colour.WHITE;
      moveNbr = 1;
   }

   public int getMoveNumber() {
      return moveNbr;
   }

   /**
    * Sets the move number.
    */
   public void setMoveNumber(int moveNbr) {
      this.moveNbr = moveNbr;
   }

   public boolean canCastle(Colour colour, CastlingRights rights) {
      return castling[colour.ordinal()].contains(rights);
   }

   public void setCastlingRights(Colour colour, CastlingRights... rights) {
      castling[colour.ordinal()].clear();
      for (CastlingRights right : rights) {
         if (right != null) {
            castling[colour.ordinal()].add(right);
         }
      }
   }

   /**
    * Just for tests: indicate that in this position the king of the side to move is in check.
    *
    * @param inCheck
    *           true when the king is in check.
    */
   public void setInCheck(boolean inCheck) {
      this.inCheck = inCheck;
   }

   public Chessboard getChessboard() {
      return chessboard;
   }

   public Colour getSideToMove() {
      return sideToMove;
   }

   public void setSideToMove(Colour sideToMove) {
      this.sideToMove = sideToMove;
   }

   /**
    * Find all moves for the given colour from the current position.
    *
    * @param colour
    *           the required colour
    * @return all moves for this colour.
    */
   public List<Move> findMoves(Colour colour) {
      List<Move> moves = new ArrayList<>(60);
      for (PieceType type : PieceType.getPieceTypes()) {
         Piece p = chessboard.getPieces(colour).get(type);
         moves.addAll(p.findMoves(this, inCheck));
      }
      return moves;
   }

   /**
    * Find the number of possible moves at the given depth, starting at the current position, i.e. for a depth of 2 and
    * start colour white, all of black's moves will be returned for each of the possible white moves.
    *
    * NB: this is not the same as counting all possible moves from a given position for a given depth. Only leaf nodes
    * are counted.
    *
    * @param sideToMove
    *           the starting colour
    * @param depth
    *           the required depth to search
    * @param debug
    *           if true, debug-info will be printed to stdout
    *
    * @return the list of possible moves at the given depth.
    */
   public List<Move> findMoves(Colour sideToMove, int depth, boolean debug) {
      return findMovesInternal(sideToMove, depth, new ArrayDeque<Move>(), new ArrayList<Move>(), debug);
   }

   /**
    * Internal method for finding the number of possible moves at the given depth.
    *
    * @param sideToMove
    *           the starting colour
    * @param depth
    *           the required depth to search
    * @param movesSoFar
    *           for debugging purposes: the moves up to this point
    * @param totalMoves
    *           stores all moves found
    * @param debug
    *           if true, debug-info will be printed to stdout
    *
    * @return the list of possible moves at the given depth.
    */
   private List<Move> findMovesInternal(Colour sideToMove, int depth, Deque<Move> movesSoFar, List<Move> totalMoves,
         boolean debug) {
      if (depth == 0) {
         return new ArrayList<Move>();
      }
      // movesAtThisLevel and movesSoFar are only used for "logging"
      List<Move> movesAtThisLevel = new ArrayList<>();
      for (Move move : findMoves(sideToMove)) {
         if (debug) {
            movesSoFar.addLast(move);
         }
         move(move);

         List<Move> movesFromThisPosn = findMovesInternal(Colour.oppositeColour(sideToMove), depth - 1, movesSoFar,
               totalMoves, debug);
         if (movesFromThisPosn.isEmpty()) {
            totalMoves.add(move);
            if (debug) {
               movesAtThisLevel.add(move);
            }
         }

         unmove(move);
         if (debug) {
            movesSoFar.removeLast();
         }
      }

      if (debug) {
         if (!movesAtThisLevel.isEmpty()) {
            boolean check = false;
            boolean capture = false;
            if (!movesSoFar.isEmpty()) {
               check = movesSoFar.peekLast().isCheck();
            }
            if (!movesSoFar.isEmpty()) {
               capture = movesSoFar.peekLast().isCapture();
            }
            System.out.println((check ? "CHECK" : "") + (capture ? "CAPTURE" : "") + " moves: " + movesSoFar + " -> "
                  + movesAtThisLevel.size() + ":" + movesAtThisLevel);
         }
      }
      return totalMoves;

   }

   /**
    * Execute the given move.
    *
    * @param move
    *           the move
    */
   public void move(Move move) {
      if (move.getColour() != sideToMove) {
         throw new IllegalArgumentException("move is for '" + move.getColour() + "' but sideToMove=" + sideToMove);
      }
      this.moves.add(move);
      PieceType movingPiece = move.getPiece();
      // double check that the move fits the data structures
      // if (!chessboard.getPieces(sideToMove).get(movingPiece).getBitBoard().getBitSet().get(move.from().bitPosn())) {
      // throw new IllegalArgumentException("no " + type + " found on square " + move.from() + ". Move=" + move);
      // }

      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         chessboard.getPieces(sideToMove).get(movingPiece).move(move);
         chessboard.getPieces(sideToMove).get(PieceType.ROOK).move(rooksMove);
         CastlingRights right = move.isCastleKingsSide() ? CastlingRights.KINGS_SIDE : CastlingRights.QUEENS_SIDE;
         castling[sideToMove.ordinal()].remove(right);
      } else {
         if (!move.isCapture() && !chessboard.getEmptySquares().getBitSet().get(move.to().bitIndex())) {
            throw new IllegalArgumentException("square " + move.to() + " is not empty. Move=" + move);
         }
         // update structures for the moving piece
         chessboard.getPieces(sideToMove).get(movingPiece).move(move);
         // capture: remove the captured piece
         if (move.isCapture()) {
            chessboard.getPieces(Colour.oppositeColour(sideToMove)).get(move.getCapturedPiece()).removePiece(move.to());
         }
         // promotion: add the promoted piece
         if (move.isPromotion()) {
            chessboard.getPieces(sideToMove).get(move.getPromotedPiece()).addPiece(move.to());
         }

         chessboard.updateStructures();
         // this doesn't take into a/c captures, promotions, castling, therefore using updateStructures for now
         // chessboard.getEmptySquares().getBitSet().set(move.from().bitIndex());
         // chessboard.getEmptySquares().getBitSet().clear(move.to().bitIndex());
         //
         // chessboard.getAllPieces(sideToMove).getBitSet().clear(move.from().bitIndex());
         // chessboard.getAllPieces(sideToMove).getBitSet().set(move.to().bitIndex());

         setSideToMove(Colour.oppositeColour(sideToMove));
         inCheck = move.isCheck();
         if (Colour.WHITE == sideToMove) {
            moveNbr++;
         }
      }
   }

   /**
    * Reverses the given move.
    *
    * @param move
    *           the move
    */
   public void unmove(Move move) {
      if (move.getColour() == sideToMove) {
         throw new IllegalArgumentException("unmove for '" + move.getColour() + "' was unexpected");
      }
      // pollLast instead of removeLast to avoid exception
      this.moves.pollLast();
      PieceType movingPiece = move.getPiece();

      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
         chessboard.getPieces(move.getColour()).get(PieceType.ROOK).unmove(rooksMove);
         CastlingRights right = move.isCastleKingsSide() ? CastlingRights.KINGS_SIDE : CastlingRights.QUEENS_SIDE;
         castling[move.getColour().ordinal()].add(right);
      } else {
         if (!move.isCapture() && !chessboard.getEmptySquares().getBitSet().get(move.from().bitIndex())) {
            throw new IllegalArgumentException("square " + move.from() + " is not empty. Unmove=" + move);
         }
         // update structures for the moving piece
         chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
         // capture: add the captured piece
         if (move.isCapture()) {
            chessboard.getPieces(Colour.oppositeColour(move.getColour())).get(move.getCapturedPiece())
                  .addPiece(move.to());
         }
         // promotion: remove the promoted piece
         if (move.isPromotion()) {
            chessboard.getPieces(move.getColour()).get(move.getPromotedPiece()).removePiece(move.to());
         }

         chessboard.updateStructures();
         // this doesn't take into a/c captures, promotions, castling, therefore using updateStructures for now
         // chessboard.getEmptySquares().getBitSet().clear(move.from().bitIndex());
         // chessboard.getEmptySquares().getBitSet().set(move.to().bitIndex());
         //
         // chessboard.getAllPieces(move.getColour()).getBitSet().set(move.from().bitIndex());
         // chessboard.getAllPieces(move.getColour()).getBitSet().clear(move.to().bitIndex());

         // undoing black's move means that black should now move
         setSideToMove(move.getColour());
         // check if the 'new' last move was a check
         Move lastMove = this.moves.peekLast();
         inCheck = (lastMove != null) ? lastMove.isCheck() : false;
         if (Colour.BLACK == sideToMove) {
            moveNbr--;
         }
      }

   }
}
