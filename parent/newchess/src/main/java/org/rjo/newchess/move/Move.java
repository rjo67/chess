package org.rjo.newchess.move;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Pieces;

public class Move implements IMove {

   private static enum SpecialMove {
      KINGS_SIDE_CASTLING, QUEENS_SIDE_CASTLING, ENPASSANT, PAWN_TWO_SQUARES_FORWARD;
   }

   private final byte originPiece; // which piece is moving...
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
    * @param originPiece                   info about piece on origin square
    * @param targetSq                      target square
    * @param capture                       true if capture
    * @param enpassant                     true if enpassant
    * @param squareOfPawnCapturedEnpassant enpassant square (only set if enpassant=true)
    * @param kingsSideCastling             true if king's-side castling
    * @param queensSideCastling            true if queen's-side castling
    * @param pawnTwoSquaresForward         true if pawn move two-squares forward
    */
   private Move(int originSq, byte originPiece, int targetSq, boolean capture, byte promotedPiece, boolean enpassant, int squareOfPawnCapturedEnpassant,
         boolean kingsSideCastling, boolean queensSideCastling, boolean pawnTwoSquaresForward) {
      this.originSq = originSq;
      this.originPiece = originPiece;
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
    * @param originPiece   info about piece on origin square
    * @param targetSq      target square
    * @param targetPiece   info about piece on target square; 0 (empty square) == no piece == not a capture
    * @param promotedPiece promoted piece, !=0 if promotion
    */
   public Move(int originSq, byte originPiece, int targetSq, byte targetPiece, byte promotedPiece) {
      this(originSq, originPiece, targetSq, targetPiece != 0, promotedPiece, false, (byte) 0, false, false, false);
   }

   /**
    * Constructor for special moves like castling or enpassant.
    * <p>
    * 
    * @param originSq    origin square
    * @param originPiece info about piece on origin square
    * @param targetSq    target square
    * @param specialMove determines the type of special move
    */
   private static Move createMove(int originSq, byte originPiece, int targetSq, SpecialMove specialMove) {
      switch (specialMove) {
      case KINGS_SIDE_CASTLING:
      case QUEENS_SIDE_CASTLING:
         return new Move(originSq, originPiece, targetSq, false, (byte) 0, false, (byte) 0, specialMove == SpecialMove.KINGS_SIDE_CASTLING,
               specialMove == SpecialMove.QUEENS_SIDE_CASTLING, false);
      case ENPASSANT:
         // explicitly set 'capture', since e.p. moves are captures although the target square is 'empty'
         return new Move(originSq, originPiece, targetSq, true, (byte) 0, true, targetSq + (Pieces.isWhitePiece(originPiece) ? 8 : -8), false, false, false);
      case PAWN_TWO_SQUARES_FORWARD:
         return new Move(originSq, originPiece, targetSq, false, (byte) 0, false, 0, false, false, true);
      default:
         throw new IllegalStateException("unhandled value of specialMove: " + specialMove);
      }
   }

   /**
    * Normal move.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @return the new move object
    */
   public static Move createMove(int origin, byte originPiece, int target) {
      return new Move(origin, originPiece, target, (byte) 0, (byte) 0);
   }

   /**
    * Normal move. Helper method using Square objects.
    * 
    * @param origin           origin square
    * @param originSquareInfo info about origin square
    * @param target           target square
    * @return the new move object
    */
   public static Move createMove(Square origin, byte originPiece, Square target) {
      return new Move(origin.index(), originPiece, target.index(), (byte) 0, (byte) 0);
   }

   /**
    * Capture move.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @param targetPiece info about piece on target square; cannot be zero (empty square)
    * @return the new move object
    */
   public static Move createCapture(int origin, byte originPiece, int target, byte targetPiece) {
      if (targetPiece == 0) { throw new IllegalStateException("cannot call createCapture with empty 'targetPiece'"); }
      return new Move(origin, originPiece, target, targetPiece, (byte) 0);
   }

   /**
    * Capture move. Helper method using Square objects.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @param targetPiece info about piece on target square; cannot be zero (empty square)
    * @return the new move object
    */
   public static Move createCapture(Square origin, byte originPiece, Square target, byte targetPiece) {
      return createCapture(origin.index(), originPiece, target.index(), targetPiece);
   }

   /**
    * Promotion (without capture).
    * 
    * @param origin        origin square
    * @param originPiece   info about piece on origin square
    * @param target        target square
    * @param promotedPiece the promoted piece
    * @return the new move object
    */
   public static Move createPromotionMove(int origin, byte originPiece, int target, byte promotedPiece) {
      return new Move(origin, originPiece, target, (byte) 0, promotedPiece);
   }

   /**
    * Promotion (without capture). Helper-Method using Square objects.
    * 
    * @param origin        origin square
    * @param originPiece   info about piece on origin square
    * @param target        target square
    * @param promotedPiece the promoted piece
    * @return the new move object
    */
   public static Move createPromotionMove(Square origin, byte originPiece, Square target, byte promotedPiece) {
      return new Move(origin.index(), originPiece, target.index(), (byte) 0, promotedPiece);
   }

   /**
    * Promotion, optionally with capture.
    * 
    * @param origin        origin square
    * @param originPiece   info about piece on origin square
    * @param target        target square
    * @param targetPiece   info about piece on target square/captured piece, 0 if not a capture
    * @param promotedPiece the promoted piece
    * @return the new move object
    */
   public static Move createPromotionCaptureMove(int origin, byte originPiece, int target, byte targetPiece, byte promotedPiece) {
      return new Move(origin, originPiece, target, targetPiece, promotedPiece);
   }

   public static Move createPromotionCaptureMove(Square origin, byte originPiece, Square target, byte targetPiece, byte promotedPiece) {
      return new Move(origin.index(), originPiece, target.index(), targetPiece, promotedPiece);
   }

   /**
    * Kingsside castling. Origin and target squares are set to those of the king.
    * <p>
    * Cannot use a static Move object here, since the 'check' attribute is not set at construction time.
    * 
    * @param colour colour of moving side
    * @return the new move object
    */
   public static Move createKingssideCastlingMove(Colour colour) {
      return createMove(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], Pieces.generateKing(colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][0], SpecialMove.KINGS_SIDE_CASTLING);
   }

   /**
    * Queensside castling. Origin and target squares are set to those of the king.
    * 
    * @param colour colour of moving side
    * @return the new move object
    */
   public static Move createQueenssideCastlingMove(Colour colour) {
      return createMove(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], Pieces.generateKing(colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][1], SpecialMove.QUEENS_SIDE_CASTLING);
   }

   /**
    * Enpassant.
    * 
    * @param originSq    origin square
    * @param originPiece info about piece on origin square
    * @param epSquare    e.p. square
    * @return the new move object
    */
   public static Move createEnpassantMove(int originSq, byte originPiece, int epSquare) {
      return createMove(originSq, originPiece, epSquare, SpecialMove.ENPASSANT);
   }

   /**
    * Enpassant. Helper method using Square objects.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @return the new move object
    */
   public static Move createEnpassantMove(Square origin, byte originPiece, Square epSquare) {
      return createMove(origin.index(), originPiece, epSquare.index(), SpecialMove.ENPASSANT);
   }

   /**
    * Special method for two square pawn moves, in order to set the flag {@link #pawnTwoSquaresForward}.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @return the new move object
    */
   public static Move createPawnTwoSquaresForwardMove(int origin, byte originPiece, int target) {
      return createMove(origin, originPiece, target, SpecialMove.PAWN_TWO_SQUARES_FORWARD);
   }

   /**
    * Special method for two square pawn moves, in order to set the flag {@link #pawnTwoSquaresForward}.
    * 
    * Helper method using Square objects.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @return the new move object
    */
   public static Move createPawnTwoSquaresForwardMove(Square origin, byte originPiece, Square target) {
      return createMove(origin.index(), originPiece, target.index(), SpecialMove.PAWN_TWO_SQUARES_FORWARD);
   }

   /**
    * Does this move instance represent a capture of a piece at 'captureSquare'? Also copes with enpassant, where the pawn taken e.p. is on
    * 'captureSquare'.
    * 
    * @param captureSquare the square to inspect
    * @return true if piece captured.on 'captureSquare'
    */
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
         sb.append(Pieces.symbol(originPiece));
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
   public byte getMovingPiece() { return originPiece; }

   @Override
   public Colour getColourOfMovingPiece() { return Pieces.colourOf(originPiece); }

   @Override
   public int getOrigin() { return originSq; }

   @Override
   public int getTarget() { return targetSq; }

   @Override
   public boolean isPawnTwoSquaresForward() { return pawnTwoSquaresForward; }

   @Override
   public byte getPromotedPiece() { return promotedPiece; }

}
