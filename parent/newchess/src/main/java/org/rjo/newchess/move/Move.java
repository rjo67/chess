package org.rjo.newchess.move;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.SquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class Move {
   // which piece is moving...
   private final SquareInfo originSquareInfo;
   // ... and where it's moving from
   private final int originSq;

   // where piece is moving to
   private final int targetSq;
   private final SquareInfo targetSquareInfo; // !null for captures

   private final boolean capture;
   private final boolean promotion;
   private final boolean enPassant;
   private final PieceType promotedPiece;
   // following fields are set after constructor call
   private boolean kingsSideCastling;
   private boolean queensSideCastling;
   private boolean check; // whether this move is a check
   private boolean pawnTwoSquaresForward; // marker field to indicate a pawn move of two squares (used in Position when performing a move)

   /**
    * @param origin           origin square
    * @param originSquareInfo 'raw' info of origin square
    * @param target           target square
    * @param targetSquareInfo 'raw' info of target square; null if not a capture
    * @param promotedPiece    promoted piece, set if promotion
    * @param castling         set if a castling move (kings/queensside)
    * @param enPassant        set if enpassant
    */
   private Move(int origin, SquareInfo originSquareInfo, int target, SquareInfo targetSquareInfo, PieceType promotedPiece, boolean enPassant) {
      this.originSq = origin;
      this.targetSq = target;
      this.originSquareInfo = originSquareInfo;
      this.capture = targetSquareInfo != null;
      this.targetSquareInfo = targetSquareInfo;
      this.promotion = promotedPiece != null;
      this.promotedPiece = promotedPiece;
      this.enPassant = enPassant;
   }

   /**
    * Normal move.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @return                  the new move object
    */
   public static Move createMove(int origin, SquareInfo originSquareInfo, int target) {
      return new Move(origin, originSquareInfo, target, null, null, false);
   }

   /**
    * Normal move. Helper method using Square objects.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @return                  the new move object
    */
   public static Move createMove(Square origin, SquareInfo originSquareInfo, Square target) {
      return new Move(origin.index(), originSquareInfo, target.index(), null, null, false);
   }

   /**
    * Capture move.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square (capture)
    * @return                  the new move object
    */
   public static Move createCapture(int origin, SquareInfo originSquareInfo, int target, SquareInfo targetSquareInfo) {
      return new Move(origin, originSquareInfo, target, targetSquareInfo, null, false);
   }

   /**
    * Capture move. Helper method using Square objects.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square (capture)
    * @return                  the new move object
    */
   public static Move createCapture(Square origin, SquareInfo originSquareInfo, Square target, SquareInfo targetSquareInfo) {
      return new Move(origin.index(), originSquareInfo, target.index(), targetSquareInfo, null, false);
   }

   /**
    * Promotion (without capture).
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  promotedPiece    the promoted piece
    * @return                  the new move object
    */
   public static Move createPromotionMove(int origin, SquareInfo originSquareInfo, int target, PieceType promotedPiece) {
      return new Move(origin, originSquareInfo, target, null, promotedPiece, false);
   }

   /**
    * Promotion with capture.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square/captured piece, null if not a capture
    * @param  promotedPiece    the promoted piece
    * @return                  the new move object
    */
   public static Move createPromotionCaptureMove(int origin, SquareInfo originSquareInfo, int target, SquareInfo targetSquareInfo, PieceType promotedPiece) {
      return new Move(origin, originSquareInfo, target, targetSquareInfo, promotedPiece, false);
   }

   /**
    * Kingsside castling.
    * 
    * @param  origin           origin square (of king)
    * @param  originSquareInfo info about origin square
    * @param  colour           colour of moving side
    * @return                  the new move object
    */
   public static Move createKingssideCastlingMove(int origin, SquareInfo originSquareInfo, Colour colour) {
      // origin and target are start and end square of the king
      Move move = new Move(origin, originSquareInfo, MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][0], null, null, false);
      move.kingsSideCastling = true;
      return move;
   }

   /**
    * Queensside castling.
    * 
    * @param  origin           origin square (of king)
    * @param  originSquareInfo info about origin square
    * @param  colour           colour of moving side
    * @return                  the new move object
    */
   public static Move createQueenssideCastlingMove(int origin, SquareInfo originSquareInfo, Colour colour) {
      // origin and target are start and end square of the king
      Move move = new Move(origin, originSquareInfo, MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][1], null, null, false);
      move.queensSideCastling = true;
      return move;
   }

   /**
    * Enpassant (with optional capture).??
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square/captured piece, ??null if not a capture??
    * @return                  the new move object
    */
   public static Move createEnpassantMove(int origin, SquareInfo originSquareInfo, int epSquare, SquareInfo targetSquareInfo) {
      return new Move(origin, originSquareInfo, epSquare, targetSquareInfo, null, true);
   }

   /**
    * Enpassant. Helper method using Square objects.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square/captured piece
    * @return                  the new move object
    */
   public static Move createEnpassantMove(Square origin, SquareInfo originSquareInfo, Square epSquare, SquareInfo targetSquareInfo) {
      return new Move(origin.index(), originSquareInfo, epSquare.index(), targetSquareInfo, null, true);
   }

   /**
    * Special method for two square pawn moves, in order to set the flag {@link #pawnTwoSquaresForward}.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @return                  the new move object
    */
   public static Move createPawnTwoSquaresForwardMove(int origin, SquareInfo originSquareInfo, int target) {
      Move move = createMove(origin, originSquareInfo, target);
      move.pawnTwoSquaresForward = true;
      return move;
   }

   /**
    * Special method for two square pawn moves, in order to set the flag {@link #pawnTwoSquaresForward}.
    * 
    * Helper method using Square objects.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @return                  the new move object
    */
   public static Move createPawnTwoSquaresForwardMove(Square origin, SquareInfo originSquareInfo, Square target) {
      Move move = createMove(origin, originSquareInfo, target);
      move.pawnTwoSquaresForward = true;
      return move;
   }

   @Override
   public String toString() {
      if (isKingssideCastling()) {
         return "O-O";
      } else if (isQueenssideCastling()) {
         return "O-O-O";
      } else {
         StringBuilder sb = new StringBuilder(10);
         sb.append(originSquareInfo.pieceType().symbol(originSquareInfo.colour()));
         sb.append(Square.toSquare(originSq));
         sb.append(isCapture() ? "x" : "-");
         sb.append(Square.toSquare(targetSq));
         if (promotion) { sb.append("=").append(promotedPiece.symbol(originSquareInfo.colour())); }
         if (enPassant) { sb.append(" ep"); }
         if (check) { sb.append("+"); }
         return sb.toString();
      }
   }

   public boolean isCapture() {
      return capture;
   }

   public boolean isPromotion() {
      return promotion;
   }

   public boolean isEnPassant() {
      return enPassant;
   }

   public boolean isKingssideCastling() {
      return kingsSideCastling;
   }

   public boolean isQueenssideCastling() {
      return queensSideCastling;
   }

   public PieceType getMovingPiece() {
      return originSquareInfo.pieceType();
   }

   public Colour getColourOfMovingPiece() {
      return originSquareInfo.colour();
   }

   public int getOrigin() {
      return originSq;
   }

   public int getTarget() {
      return targetSq;
   }

   public void setCheck() {
      check = true;
   }

   public boolean isCheck() {
      return check;
   }

   public boolean isPawnTwoSquaresForward() {
      return pawnTwoSquaresForward;
   }

   public PieceType getPromotedPiece() {
      return promotedPiece;
   }

}
