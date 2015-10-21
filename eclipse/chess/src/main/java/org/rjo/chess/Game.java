package org.rjo.chess;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
   /** half-moves. Not used as yet. */
   private int halfmoveClock;
   private EnumSet<CastlingRights>[] castling;
   /** which side is to move */
   private Colour sideToMove;
   /**
    * if the king (of the sideToMove) is currently in check. Normally deduced from the last move but can be set
    * delibarately for tests.
    */
   private boolean inCheck;

   // thread pool for findMove()
   private ExecutorService threadPool = Executors.newFixedThreadPool(PieceType.getPieceTypes().length);

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

   @SuppressWarnings("unchecked")
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

   public int getHalfmoveClock() {
      return halfmoveClock;
   }

   public void setHalfmoveClock(int halfmoveClock) {
      this.halfmoveClock = halfmoveClock;
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

   public boolean isInCheck() {
      return inCheck;
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
      // return findMovesParallel(colour);
      List<Move> moves = new ArrayList<>(60);
      for (PieceType type : PieceType.getPieceTypes()) {
         Piece p = chessboard.getPieces(colour).get(type);
         moves.addAll(p.findMoves(this, inCheck));
      }
      return moves;
   }

   public List<Move> findMovesParallel(Colour colour) {
      // set up tasks
      List<Callable<List<Move>>> tasks = new ArrayList<>();
      for (PieceType type : PieceType.getPieceTypes()) {
         tasks.add(new Callable<List<Move>>() {

            @Override
            public List<Move> call() throws Exception {
               Piece p = chessboard.getPieces(colour).get(type);
               return p.findMoves(Game.this, inCheck);
            }

         });
      }

      // and execute
      List<Move> moves = new ArrayList<>(60);
      try {
         List<Future<List<Move>>> results = threadPool.invokeAll(tasks);

         for (Future<List<Move>> f : results) {
            moves.addAll(f.get());
         }
      } catch (InterruptedException e) {
         // TODO Auto-generated catch block
         throw new RuntimeException("got InterruptedException from future (findMovesParallel)", e);
      } catch (ExecutionException e) {
         throw new RuntimeException("got ExecutionException from future (findMovesParallel)", e);
      }

      return moves;
   }

   /**
    * Execute the given move without debug.
    *
    * @param move
    *           the move
    */
   public void move(Move move) {
      move(move, null);
   }

   /**
    * Execute the given move.
    *
    * @param move
    *           the move
    * @param debugWriter
    *           if not null, debug info will be written here
    */
   public void move(Move move, Writer debugWriter) {
      if (move.getColour() != sideToMove) {
         throw new IllegalArgumentException("move is for '" + move.getColour() + "' but sideToMove=" + sideToMove);
      }
      this.moves.add(move);
      PieceType movingPiece = move.getPiece();

      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         chessboard.getPieces(sideToMove).get(movingPiece).move(move);
         chessboard.getPieces(sideToMove).get(PieceType.ROOK).move(rooksMove);
         // castling rights are reset later on
      } else {
         if (!move.isCapture() && !chessboard.getEmptySquares().getBitSet().get(move.to().bitIndex())) {
            throw new IllegalArgumentException("square " + move.to() + " is not empty. Move=" + move);
         }
         // update structures for the moving piece
         chessboard.getPieces(sideToMove).get(movingPiece).move(move);
         // capture: remove the captured piece
         if (move.isCapture()) {
            if (move.isEnpassant()) {
               chessboard.getPieces(Colour.oppositeColour(sideToMove)).get(move.getCapturedPiece())
                     .removePiece(Square.findMoveFromEnpassantSquare(move.to()));
            } else {
               chessboard.getPieces(Colour.oppositeColour(sideToMove)).get(move.getCapturedPiece())
                     .removePiece(move.to());
            }
         }
         // promotion: add the promoted piece
         if (move.isPromotion()) {
            chessboard.getPieces(sideToMove).get(move.getPromotedPiece()).addPiece(move.to());
         }
      }
      chessboard.updateStructures(move);

      updateCastlingRightsAfterMove(move, debugWriter);
      if (move.isPawnMoveTwoSquaresForward()) {
         chessboard.setEnpassantSquare(Square.findEnpassantSquareFromMove(move.to()));
      } else {
         chessboard.setEnpassantSquare(null);
      }
      setSideToMove(Colour.oppositeColour(sideToMove));
      inCheck = move.isCheck();
      if (Colour.WHITE == sideToMove) {
         moveNbr++;
      }
   }

   /**
    * Calculates a static value for the position after the given move.
    *
    * @param move
    *           the move
    * @return a value in centipawns
    */
   public int evaluate(Move move) {
      this.move(move);
      int value = evaluate();
      this.unmove(move);
      return value;
   }

   /**
    * Calculates a static value for the current position.
    * In order for NegaMax to work, it is important to return the score relative to the side being evaluated.
    *
    * @return a value in centipawns
    */
   public int evaluate() {
      /*
       * materialScore = kingWt * (wK-bK)
       * + queenWt * (wQ-bQ)
       * + rookWt * (wR-bR)
       * + knightWt* (wN-bN)
       * + bishopWt* (wB-bB)
       * + pawnWt * (wP-bP)
       * mobilityScore = mobilityWt * (wMobility-bMobility)
       */
      int materialScore = 0;
      for (PieceType type : PieceType.getPieceTypes()) {
         int pieceScore = 0;
         Piece piece = chessboard.getPieces(Colour.WHITE).get(type);
         if (piece != null) {
            pieceScore += piece.calculatePieceSquareValue();
         }
         piece = chessboard.getPieces(Colour.BLACK).get(type);
         if (piece != null) {
            pieceScore -= piece.calculatePieceSquareValue();
         }
         materialScore += pieceScore;
      }

      // mobility
      // the sidetomove could be in check; for simplicity this is assumed, i.e. 'kingInCheck'==TRUE
      // the other side (who has just moved) cannot be in check
      // if enpassant square is set, this can only apply to the sidetomove
      int whiteMobility, blackMobility;
      Square enpassantSquare = null;
      List<Move> moves = new ArrayList<>(60);
      if (getSideToMove() != Colour.WHITE) {
         enpassantSquare = getChessboard().getEnpassantSquare();
         getChessboard().setEnpassantSquare(null);
      }
      for (PieceType type : PieceType.getPieceTypes()) {
         Piece p = chessboard.getPieces(Colour.WHITE).get(type);
         moves.addAll(p.findMoves(this, (getSideToMove() == Colour.WHITE ? true : false)));
      }
      if (getSideToMove() != Colour.WHITE) {
         getChessboard().setEnpassantSquare(enpassantSquare);
      }
      whiteMobility = moves.size();
      moves = new ArrayList<>(60);
      if (getSideToMove() != Colour.BLACK) {
         enpassantSquare = getChessboard().getEnpassantSquare();
         getChessboard().setEnpassantSquare(null);
      }
      for (PieceType type : PieceType.getPieceTypes()) {
         Piece p = chessboard.getPieces(Colour.BLACK).get(type);
         moves.addAll(p.findMoves(this, (getSideToMove() == Colour.BLACK ? true : false)));
      }
      if (getSideToMove() != Colour.BLACK) {
         getChessboard().setEnpassantSquare(enpassantSquare);
      }
      blackMobility = moves.size();

      final int MOBILITY_WEIGHTING = 2;
      int mobilityScore = MOBILITY_WEIGHTING * (whiteMobility - blackMobility);
      return (mobilityScore + materialScore) * (getSideToMove() == Colour.WHITE ? 1 : -1);
   }

   private void updateCastlingRightsAfterMove(Move move, Writer debugWriter) {
      if (PieceType.KING == move.getPiece()) {
         move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
         castling[sideToMove.ordinal()].clear();
         // writeDebug(debugWriter,
         // "move: " + move + ", sideToMove: " + sideToMove + ", castling=" + castling[sideToMove.ordinal()]);
      } else if (PieceType.ROOK == move.getPiece()) {
         // remove castling rights if rook has moved
         move.setPreviousCastlingRights(castling[sideToMove.ordinal()]);
         if (castling[sideToMove.ordinal()].contains(CastlingRights.KINGS_SIDE)) {
            Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h1 : Square.h8;
            if (move.from() == targetSquare) {
               castling[sideToMove.ordinal()].remove(CastlingRights.KINGS_SIDE);
            }
         }
         if (castling[sideToMove.ordinal()].contains(CastlingRights.QUEENS_SIDE)) {
            Square targetSquare = (sideToMove == Colour.WHITE) ? Square.a1 : Square.a8;
            if (move.from() == targetSquare) {
               castling[sideToMove.ordinal()].remove(CastlingRights.QUEENS_SIDE);
            }
         }
         // writeDebug(debugWriter,
         // "move: " + move + ", sideToMove: " + sideToMove + ", castling=" + castling[sideToMove.ordinal()]);
      }
      // update OPPONENT's castling rights if necessary
      if (move.isCapture()) {
         final Colour opponentsColour = Colour.oppositeColour(sideToMove);
         Square targetSquare = (sideToMove == Colour.WHITE) ? Square.h8 : Square.h1;
         boolean processed = false;
         if (move.to().equals(targetSquare)) {
            move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
            castling[opponentsColour.ordinal()].remove(CastlingRights.KINGS_SIDE);
            processed = true;
            // writeDebug(debugWriter, "move: " + move + ", removed kings side castling for " + opponentsColour);
         }
         if (!processed) {
            targetSquare = (sideToMove == Colour.WHITE) ? Square.a8 : Square.a1;
            if (move.to().equals(targetSquare)) {
               move.setPreviousCastlingRightsOpponent(castling[opponentsColour.ordinal()]);
               castling[opponentsColour.ordinal()].remove(CastlingRights.QUEENS_SIDE);
               // writeDebug(debugWriter, "move: " + move + ", removed queens side castling for " + opponentsColour);
            }
         }
      }

   }

   private void writeDebug(Writer debugWriter, String string) {
      if (debugWriter != null) {
         try {
            debugWriter.write(string + System.lineSeparator());
         } catch (IOException e) {
            throw new RuntimeException("could not write debug info", e);
         }
      }
   }

   /**
    * Reverses the given move. Version without debug info.
    *
    * @param move
    *           the move
    */
   public void unmove(Move move) {
      unmove(move, null);
   }

   /**
    * Reverses the given move.
    *
    * @param move
    *           the move
    * @param debugWriter
    *           if not null, debug info will be written here
    */
   public void unmove(Move move, Writer debugWriter) {
      if (move.getColour() == sideToMove) {
         throw new IllegalArgumentException("unmove for '" + move.getColour() + "' was unexpected");
      }

      PieceType movingPiece = move.getPiece();

      if (move.isCastleKingsSide() || move.isCastleQueensSide()) {
         Move rooksMove = move.getRooksCastlingMove();
         chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
         chessboard.getPieces(move.getColour()).get(PieceType.ROOK).unmove(rooksMove);
         // castling rights are reset later on
      } else {
         if (!move.isCapture() && !chessboard.getEmptySquares().getBitSet().get(move.from().bitIndex())) {
            throw new IllegalArgumentException("square " + move.from() + " is not empty. Unmove=" + move);
         }
         // update structures for the moving piece
         chessboard.getPieces(move.getColour()).get(movingPiece).unmove(move);
         // capture: add the captured piece
         if (move.isCapture()) {
            if (move.isEnpassant()) {
               chessboard.getPieces(Colour.oppositeColour(move.getColour())).get(move.getCapturedPiece())
                     .addPiece(Square.findMoveFromEnpassantSquare(move.to()));
            } else {
               chessboard.getPieces(Colour.oppositeColour(move.getColour())).get(move.getCapturedPiece())
                     .addPiece(move.to());
            }
         }
         // promotion: remove the promoted piece
         if (move.isPromotion()) {
            chessboard.getPieces(move.getColour()).get(move.getPromotedPiece()).removePiece(move.to());
         }
      }

      chessboard.updateStructures(move);

      // reset castling rights if necessary
      // if ((PieceType.KING == move.getPiece()) || (PieceType.ROOK == move.getPiece())) {
      if (move.previousCastlingRightsWasSet()) {
         castling[move.getColour().ordinal()] = move.getPreviousCastlingRights();
         // writeDebug(debugWriter, "unmove: " + move + ", sideToMove: " + move.getColour() + ", castling="
         // + castling[move.getColour().ordinal()]);
      }
      if (move.previousCastlingRightsOpponentWasSet()) {
         castling[Colour.oppositeColour(move.getColour()).ordinal()] = move.getPreviousCastlingRightsOpponent();
         // writeDebug(debugWriter, "unmove: " + move + ", sideToMove: " + Colour.oppositeColour(move.getColour())
         // + ", opponents castling=" + castling[Colour.oppositeColour(move.getColour()).ordinal()]);
      }
      // undoing black's move means that black should now move
      setSideToMove(move.getColour());

      // pollLast instead of removeLast to avoid exception
      Move lastMove = this.moves.pollLast();
      // check if the 'new' last move was a check
      inCheck = (lastMove != null) ? lastMove.isCheck() : false;

      if (lastMove != null && lastMove.isEnpassant()) {
         chessboard.setEnpassantSquare(lastMove.to());
      } else {
         chessboard.setEnpassantSquare(null);
      }

      if (Colour.BLACK == sideToMove) {
         moveNbr--;
      }
   }
}
