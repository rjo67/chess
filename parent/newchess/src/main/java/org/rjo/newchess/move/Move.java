package org.rjo.newchess.move;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Pieces;

public class Move implements IMove {

   public static final IMove[] KINGS_CASTLING_MOVE = { //
         new Move(MoveGenerator.kingsCastlingSquareIndex[Colour.WHITE.ordinal()], MoveGenerator.kingsSquareAfterCastling[Colour.WHITE.ordinal()][0], false,
               (byte) 0, false, (byte) 0, true, false, false), //
         new Move(MoveGenerator.kingsCastlingSquareIndex[Colour.BLACK.ordinal()], MoveGenerator.kingsSquareAfterCastling[Colour.BLACK.ordinal()][0], false,
               (byte) 0, false, (byte) 0, true, false, false) };

   public static final IMove[] QUEENS_CASTLING_MOVE = { //
         new Move(MoveGenerator.kingsCastlingSquareIndex[Colour.WHITE.ordinal()], MoveGenerator.kingsSquareAfterCastling[Colour.WHITE.ordinal()][1], false,
               (byte) 0, false, (byte) 0, false, true, false), //
         new Move(MoveGenerator.kingsCastlingSquareIndex[Colour.BLACK.ordinal()], MoveGenerator.kingsSquareAfterCastling[Colour.BLACK.ordinal()][1], false,
               (byte) 0, false, (byte) 0, false, true, false) };

   private final int originSq; // ... and where it's moving from
   private final int targetSq; // where piece is moving to
   private final boolean capture;
   private final byte promotedPiece; // !=0 if a promotion
   private final boolean enpassant; // whether this move is an enpassant capture
   private final int squareOfPawnCapturedEnpassant; // the square of the pawn which was captured enpassant, **defaults to 0**
   private final boolean kingsSideCastling;
   private final boolean queensSideCastling;
   private final boolean pawnTwoSquaresForward; // marker field to indicate a pawn move of two squares (used in Position when performing a move)

   /**
    * Base Constructor.
    * 
    * @param originSq                      origin square
    * @param targetSq                      target square
    * @param capture                       true if capture
    * @param enpassant                     true if enpassant
    * @param squareOfPawnCapturedEnpassant enpassant square (only set if enpassant=true)
    * @param kingsSideCastling             true if king's-side castling
    * @param queensSideCastling            true if queen's-side castling
    * @param pawnTwoSquaresForward         true if pawn move two-squares forward
    */
   /* package */ Move(int originSq, int targetSq, boolean capture, byte promotedPiece, boolean enpassant, int squareOfPawnCapturedEnpassant,
         boolean kingsSideCastling, boolean queensSideCastling, boolean pawnTwoSquaresForward) {
      this.originSq = originSq;
      this.targetSq = targetSq;
      this.capture = capture;
      this.promotedPiece = promotedPiece;
      this.enpassant = enpassant;
      this.squareOfPawnCapturedEnpassant = squareOfPawnCapturedEnpassant;
      this.kingsSideCastling = kingsSideCastling;
      this.queensSideCastling = queensSideCastling;
      this.pawnTwoSquaresForward = pawnTwoSquaresForward;
   }

   /**
    * Constructor for most moves (but NOT enpassant). A 'capture' is recognised when targetPiece!=0.
    * 
    * @param originSq      origin square
    * @param targetSq      target square
    * @param targetPiece   info about piece on target square; 0 (empty square) == no piece == not a capture
    * @param promotedPiece promoted piece, !=0 if promotion
    */
   public Move(int originSq, int targetSq, byte targetPiece, byte promotedPiece) {
      this(originSq, targetSq, targetPiece != 0, promotedPiece, false, (byte) 0, false, false, false);
   }

   /**
    * Normal move.
    * 
    * @param origin origin square
    * @param target target square
    * @return the new move object
    */
   public static Move createMove(int origin, int target) {
      return new Move(origin, target, (byte) 0, (byte) 0);
   }

   /**
    * Normal move. Helper method using Square objects.
    * 
    * @param origin origin square
    * @param target target square
    * @return the new move object
    */
   public static Move createMove(Square origin, Square target) {
      return new Move(origin.index(), target.index(), (byte) 0, (byte) 0);
   }

   /**
    * Capture move.
    * 
    * @param origin      origin square
    * @param target      target square
    * @param targetPiece info about piece on target square; cannot be zero (empty square)
    * @return the new move object
    */
   public static Move createCapture(int origin, int target, byte targetPiece) {
      if (targetPiece == 0) { throw new IllegalStateException("cannot call createCapture with empty 'targetPiece'"); }
      return new Move(origin, target, targetPiece, (byte) 0);
   }

   /**
    * Capture move. Helper method using Square objects.
    * 
    * @param origin      origin square
    * @param target      target square
    * @param targetPiece info about piece on target square; cannot be zero (empty square)
    * @return the new move object
    */
   public static Move createCapture(Square origin, Square target, byte targetPiece) {
      return createCapture(origin.index(), target.index(), targetPiece);
   }

   /**
    * Promotion (without capture).
    * 
    * @param origin        origin square
    * @param target        target square
    * @param promotedPiece the promoted piece
    * @return the new move object
    */
   public static Move createPromotionMove(int origin, int target, byte promotedPiece) {
      return new Move(origin, target, (byte) 0, promotedPiece);
   }

   /**
    * Promotion, optionally with capture.
    * 
    * @param origin        origin square
    * @param target        target square
    * @param targetPiece   info about piece on target square/captured piece, 0 if not a capture
    * @param promotedPiece the promoted piece
    * @return the new move object
    */
   public static Move createPromotionCaptureMove(int origin, int target, byte targetPiece, byte promotedPiece) {
      return new Move(origin, target, targetPiece, promotedPiece);
   }

   /**
    * Create an Enpassant move. Purely for game setup.
    * 
    * @param originSq            origin square
    * @param epSquare            e.p. square
    * @param colourOfMovingPiece colour of the moving pawn
    * @return the new move object
    */
   public static Move createEnpassantMove(int originSq, int epSquare, Colour colourOfMovingPiece) {
      return new Move(originSq, epSquare, true, (byte) 0, true, epSquare + (colourOfMovingPiece == Colour.WHITE ? 8 : -8), false, false, false);
   }

   /**
    * Does this move instance represent a capture of a piece at 'captureSquare'? Also copes with enpassant, where the pawn taken e.p. is on
    * 'captureSquare'.
    * 
    * @param captureSquare the square to inspect
    * @return true if piece captured.on 'captureSquare'
    */
   @Override
   public boolean moveCapturesPiece(int captureSquare) {
      if (isCapture()) {
         if (getTarget() == captureSquare) { return true; }
         // enpassant
         if (isEnpassant() && getSquareOfPawnCapturedEnpassant() == captureSquare) { return true; }
      }
      return false;
   }

   @Override
   public String toString() {
      if (isKingssideCastling()) {
         return "O-O";
      } else if (isQueenssideCastling()) {
         return "O-O-O";
      } else {
         StringBuilder sb = new StringBuilder(10);
         sb.append(Square.toSquare(originSq));
         sb.append(isCapture() ? "x" : "-");
         sb.append(Square.toSquare(targetSq));
         if (promotedPiece != 0) { sb.append("=").append(Pieces.symbol(promotedPiece)); }
         if (enpassant) { sb.append(" ep"); }
         return sb.toString();
      }
   }

   @Override
   public boolean isCapture() { return capture; }

   @Override
   public boolean isPromotion() { return promotedPiece != 0; }

   @Override
   public boolean isEnpassant() { return enpassant; }

   @Override
   public int getSquareOfPawnCapturedEnpassant() { return squareOfPawnCapturedEnpassant; }

   @Override
   public boolean isKingssideCastling() { return kingsSideCastling; }

   @Override
   public boolean isQueenssideCastling() { return queensSideCastling; }

   @Override
   public int getOrigin() { return originSq; }

   @Override
   public int getTarget() { return targetSq; }

   @Override
   public boolean isPawnTwoSquaresForward() { return pawnTwoSquaresForward; }

   @Override
   public byte getPromotedPiece() { return promotedPiece; }

}
