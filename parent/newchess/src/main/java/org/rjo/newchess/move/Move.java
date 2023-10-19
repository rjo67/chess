package org.rjo.newchess.move;

import java.util.Arrays;
import java.util.List;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.PieceSquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Pieces;

public class Move {
   private final byte originPiece; // which piece is moving...
   private final int originSq; // ... and where it's moving from
   private final int targetSq; // where piece is moving to
   private final byte targetPiece; // !=0 for captures
   private final boolean capture;
   private final boolean promotion;
   private final byte promotedPiece;
   private final boolean enpassant; // whether this move is an enpassant capture
   private final int squareOfPawnCapturedEnpassant; // the square of the pawn which was captured enpassant, **defaults to 0**

   // following fields are set after constructor call
   private boolean kingsSideCastling;
   private boolean queensSideCastling;
   private boolean check; // whether this move is a check
   private List<PieceSquareInfo> checkSquares; // set to the square(s) of the piece(s) delivering a check
   private boolean pawnTwoSquaresForward; // marker field to indicate a pawn move of two squares (used in Position when performing a move)

   /**
    * Constructor for most moves (but NOT enpassant). A 'capture' will be automatically recognised.
    * 
    * @param origin        origin square
    * @param originPiece   info about piece on origin square
    * @param target        target square
    * @param targetPiece   info about piece on target square; 0 (empty square) == no piece == not a capture
    * @param promotedPiece promoted piece, set if promotion
    */
   private Move(int origin, byte originPiece, int target, byte targetPiece, byte promotedPiece) {
      this.originSq = origin;
      this.targetSq = target;
      this.originPiece = originPiece;
      this.capture = targetPiece != 0;
      this.targetPiece = targetPiece;
      this.promotion = promotedPiece != 0;
      this.promotedPiece = promotedPiece;
      this.enpassant = false;
      this.squareOfPawnCapturedEnpassant = 0;
   }

   /**
    * Constructor for enpassant moves. Explicitly sets 'capture' (required since e.p. moves are captures but the target square is 'empty').
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    */
   private Move(int origin, byte originPiece, int target) {
      this.originSq = origin;
      this.targetSq = target;
      this.originPiece = originPiece;
      this.targetPiece = 0;
      this.promotion = false;
      this.promotedPiece = 0;
      this.capture = true;
      this.enpassant = true;
      this.squareOfPawnCapturedEnpassant = targetSq + (Pieces.isWhitePiece(originPiece) ? 8 : -8);
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
      return createMove(origin.index(), originPiece, target.index());
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
      return createPromotionMove(origin.index(), originPiece, target.index(), promotedPiece);
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
      return createPromotionCaptureMove(origin.index(), originPiece, target.index(), targetPiece, promotedPiece);
   }

   /**
    * Kingsside castling. Origin and target squares are set to those of the king.
    * 
    * @param colour colour of moving side
    * @return the new move object
    */
   public static Move createKingssideCastlingMove(Colour colour) {
      Move move = new Move(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], Pieces.generateKing(colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][0], (byte) 0, (byte) 0);
      move.kingsSideCastling = true;
      return move;
   }

   /**
    * Queensside castling. Origin and target squares are set to those of the king.
    * 
    * @param colour colour of moving side
    * @return the new move object
    */
   public static Move createQueenssideCastlingMove(Colour colour) {
      Move move = new Move(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], Pieces.generateKing(colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][1], (byte) 0, (byte) 0);
      move.queensSideCastling = true;
      return move;
   }

   /**
    * Enpassant.
    * 
    * @param origin      origin square
    * @param originPiece info about piece on origin square
    * @param target      target square
    * @return the new move object
    */
   public static Move createEnpassantMove(int origin, byte originPiece, int epSquare) {
      return new Move(origin, originPiece, epSquare);
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
      return createEnpassantMove(origin.index(), originPiece, epSquare.index());
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
      Move move = createMove(origin, originPiece, target);
      move.pawnTwoSquaresForward = true;
      return move;
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
      return createPawnTwoSquaresForwardMove(origin.index(), originPiece, target.index());
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
         if (promotion) { sb.append("=").append(Pieces.symbol(promotedPiece)); }
         if (enpassant) { sb.append(" ep"); }
         if (isCheck()) { sb.append("+"); }
         return sb.toString();
      }
   }

   public boolean isCapture() { return capture; }

   public boolean isPromotion() { return promotion; }

   public boolean isEnpassant() { return enpassant; }

   public int getSquareOfPawnCapturedEnpassant() { return squareOfPawnCapturedEnpassant; }

   public boolean isKingssideCastling() { return kingsSideCastling; }

   public boolean isQueenssideCastling() { return queensSideCastling; }

   public byte getMovingPiece() { return originPiece; }

   public Colour getColourOfMovingPiece() { return Pieces.colourOf(originPiece); }

   public int getOrigin() { return originSq; }

   public int getTarget() { return targetSq; }

   public void setCheck(List<PieceSquareInfo> checkSquares) {
      check = true;
      this.checkSquares = checkSquares;
   }

   public void setCheck(PieceSquareInfo... checkSquares) {
      setCheck(Arrays.asList(checkSquares));
   }

   public boolean isCheck() { return check; }

   public List<PieceSquareInfo> getCheckSquares() { return checkSquares; }

   public boolean isPawnTwoSquaresForward() { return pawnTwoSquaresForward; }

   public byte getPromotedPiece() { return promotedPiece; }

}
