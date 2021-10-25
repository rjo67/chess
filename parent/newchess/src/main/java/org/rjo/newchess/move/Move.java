package org.rjo.newchess.move;

import java.util.Arrays;
import java.util.List;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.game.Position.CheckInfo;
import org.rjo.newchess.game.Position.SquareInfo;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;

public class Move {
   private final SquareInfo originSquareInfo; // which piece is moving...
   private final int originSq; // ... and where it's moving from
   private final int targetSq; // where piece is moving to
   private final SquareInfo targetSquareInfo; // !null for captures
   private final boolean capture;
   private final boolean promotion;
   private final Piece promotedPiece;

   // following fields are set after constructor call
   private boolean kingsSideCastling;
   private boolean queensSideCastling;
   private boolean check; // whether this move is a check
   private List<CheckInfo> checkSquares; // set to the square(s) of the piece(s) delivering a check
   private boolean enpassant; // whether this move is an enpassant capture
   private int squareOfPawnCapturedEnpassant; // the square of the pawn which was captured enpassant, **defaults to 0**
   private boolean pawnTwoSquaresForward; // marker field to indicate a pawn move of two squares (used in Position when performing a move)

   /**
    * @param origin           origin square
    * @param originSquareInfo 'raw' info of origin square
    * @param target           target square
    * @param targetSquareInfo 'raw' info of target square; null if not a capture
    * @param promotedPiece    promoted piece, set if promotion
    * @param castling         set if a castling move (kings/queensside)
    */
   private Move(int origin, SquareInfo originSquareInfo, int target, SquareInfo targetSquareInfo, Piece promotedPiece) {
      this.originSq = origin;
      this.targetSq = target;
      this.originSquareInfo = originSquareInfo;
      this.capture = targetSquareInfo != null;
      this.targetSquareInfo = targetSquareInfo;
      this.promotion = promotedPiece != null;
      this.promotedPiece = promotedPiece;
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
      return new Move(origin, originSquareInfo, target, null, null);
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
      return createMove(origin.index(), originSquareInfo, target.index());
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
      return new Move(origin, originSquareInfo, target, targetSquareInfo, null);
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
      return createCapture(origin.index(), originSquareInfo, target.index(), targetSquareInfo);
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
   public static Move createPromotionMove(int origin, SquareInfo originSquareInfo, int target, Piece promotedPiece) {
      return new Move(origin, originSquareInfo, target, null, promotedPiece);
   }

   /**
    * Promotion (without capture). Helper-Method using Square objects.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  promotedPiece    the promoted piece
    * @return                  the new move object
    */
   public static Move createPromotionMove(Square origin, SquareInfo originSquareInfo, Square target, Piece promotedPiece) {
      return createPromotionMove(origin.index(), originSquareInfo, target.index(), promotedPiece);
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
   public static Move createPromotionCaptureMove(int origin, SquareInfo originSquareInfo, int target, SquareInfo targetSquareInfo, Piece promotedPiece) {
      return new Move(origin, originSquareInfo, target, targetSquareInfo, promotedPiece);
   }

   public static Move createPromotionCaptureMove(Square origin, SquareInfo originSquareInfo, Square target, SquareInfo targetSquareInfo,
         Piece promotedPiece) {
      return createPromotionCaptureMove(origin.index(), originSquareInfo, target.index(), targetSquareInfo, promotedPiece);
   }

   /**
    * Kingsside castling. Origin and target squares are set to those of the king.
    * 
    * @param  colour colour of moving side
    * @return        the new move object
    */
   public static Move createKingssideCastlingMove(Colour colour) {
      Move move = new Move(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], new SquareInfo(Piece.KING, colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][0], null, null);
      move.kingsSideCastling = true;
      return move;
   }

   /**
    * Queensside castling. Origin and target squares are set to those of the king.
    * 
    * @param  colour colour of moving side
    * @return        the new move object
    */
   public static Move createQueenssideCastlingMove(Colour colour) {
      Move move = new Move(MoveGenerator.kingsCastlingSquareIndex[colour.ordinal()], new SquareInfo(Piece.KING, colour),
            MoveGenerator.kingsSquareAfterCastling[colour.ordinal()][1], null, null);
      move.queensSideCastling = true;
      return move;
   }

   /**
    * Enpassant.
    * 
    * @param  origin           origin square
    * @param  originSquareInfo info about origin square
    * @param  target           target square
    * @param  targetSquareInfo info about target square/captured piece
    * @return                  the new move object
    */
   public static Move createEnpassantMove(int origin, SquareInfo originSquareInfo, int epSquare, SquareInfo targetSquareInfo) {
      Move move = new Move(origin, originSquareInfo, epSquare, targetSquareInfo, null);
      move.enpassant = true;
      move.squareOfPawnCapturedEnpassant = move.getTarget() + (originSquareInfo.colour() == Colour.WHITE ? 8 : -8);
      return move;
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
      return createEnpassantMove(origin.index(), originSquareInfo, epSquare.index(), targetSquareInfo);
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
      return createPawnTwoSquaresForwardMove(origin.index(), originSquareInfo, target.index());
   }

   @Override
   public String toString() {
      if (isKingssideCastling()) {
         return "O-O";
      } else if (isQueenssideCastling()) {
         return "O-O-O";
      } else {
         StringBuilder sb = new StringBuilder(10);
         sb.append(originSquareInfo.piece().symbol(originSquareInfo.colour()));
         sb.append(Square.toSquare(originSq));
         sb.append(isCapture() ? "x" : "-");
         sb.append(Square.toSquare(targetSq));
         if (promotion) { sb.append("=").append(promotedPiece.symbol(originSquareInfo.colour())); }
         if (enpassant) { sb.append(" ep"); }
         if (isCheck()) { sb.append("+"); }
         return sb.toString();
      }
   }

   public boolean isCapture() {
      return capture;
   }

   public boolean isPromotion() {
      return promotion;
   }

   public boolean isEnpassant() {
      return enpassant;
   }

   public int getSquareOfPawnCapturedEnpassant() {
      return squareOfPawnCapturedEnpassant;
   }

   public boolean isKingssideCastling() {
      return kingsSideCastling;
   }

   public boolean isQueenssideCastling() {
      return queensSideCastling;
   }

   public Piece getMovingPiece() {
      return originSquareInfo.piece();
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

   public void setCheck(List<CheckInfo> checkSquares) {
      check = true;
      this.checkSquares = checkSquares;
   }

   public void setCheck(CheckInfo... checkSquares) {
      check = true;
      this.checkSquares = Arrays.asList(checkSquares);
   }

   public boolean isCheck() {
      return check;
   }

   public List<CheckInfo> getCheckSquares() {
      return checkSquares;
   }

   public boolean isPawnTwoSquaresForward() {
      return pawnTwoSquaresForward;
   }

   public Piece getPromotedPiece() {
      return promotedPiece;
   }

}
