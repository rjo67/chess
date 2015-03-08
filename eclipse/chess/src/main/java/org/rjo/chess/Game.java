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
         moves.addAll(p.findMoves(this));
      }
      return moves;
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

         chessboard.getEmptySquares().getBitSet().set(move.from().bitIndex());
         chessboard.getEmptySquares().getBitSet().clear(move.to().bitIndex());

         setSideToMove(Colour.oppositeColour(sideToMove));
         if (Colour.WHITE == sideToMove) {
            moveNbr++;
         }
      }

   }
}
