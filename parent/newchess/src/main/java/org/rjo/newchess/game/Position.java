package org.rjo.newchess.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.board.Ray;
import org.rjo.newchess.move.IMove;
import org.rjo.newchess.move.MoveGenerator;
import org.rjo.newchess.move.MoveGeneratorI;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.Piece;
import org.rjo.newchess.piece.Pieces;

/**
 * Stores information about a position.
 * 
 * <h2>Checks</h2> Using {@link #isKingInCheck(int, Colour, boolean, int, int)} the squares of enemy pieces attacking the king will be found
 * and stored in the position.
 * 
 * If a position is 'cloned' using {@link Position#Position(Position)} objects will only be shallow copied, and must therefore be copied on
 * write.
 * 
 * @author rich
 * @since 2021
 */
public class Position {

   private final static byte UNOCCUPIED_SQUARE = (byte) 0;
   private final static MoveGeneratorI moveGenerator = new MoveGenerator();

   /** enables sanity checks during move processing */
   private static final boolean TEST_IF_VALID = false;

   /**
    * Stores information about a piece on a square.
    * 
    * The 'rayToKing' gets set during move processing, if the piece is checking the king.
    */
   public static class PieceSquareInfo {
      private byte piece;
      private int square;
      private Ray rayToKing;

      public PieceSquareInfo(byte piece, int square) {
         this.piece = piece;
         this.square = square;
      }

      public PieceSquareInfo(byte piece, Square square) {
         this(piece, square.index());
      }

      // mainly for tests
      public PieceSquareInfo(Piece piece, int square) {
         this(Pieces.fromPiece(piece, null), square);
      }

      // mainly for tests
      public PieceSquareInfo(Piece piece, Square square) {
         this(Pieces.fromPiece(piece, null), square);
      }

      /**
       * Set the ray to the king along which we are checking.
       * 
       * <B>Do not call for knight checks</B>, since there is no ray in this case.
       */
      public void setRayToKing(Ray rayToKing) {
         if (rayToKing == null) { throw new IllegalArgumentException("rayToKing cannot be null if the king is in check"); }
         this.rayToKing = rayToKing;
      }

      public Ray rayToKing() {
         return rayToKing;
      }

      public int square() {
         return square;
      }

      public byte piece() {
         return piece;
      }

      @Override
      public String toString() {
         return Pieces.toPiece(piece) + "@" + Square.toSquare(square) + (rayToKing == null ? "" : " on ray " + rayToKing);
      }
   }

   /**
    * Stores information about a piece (type, colour) on a particular square.
    */
   byte[] board;// package protected for tests

   // keeps track on who can still castle
   // 1st dimension: W/B, 2nd dimension: 0 - king's side, 1 - queen's side
   boolean[][] castlingRights; // package protected for tests

   private Square enpassantSquare; // set if previous move was a pawn moving 2 squares forward
   int[] kingsSquare; // keep track of where the kings are (package protected for tests)
   private Colour sideToMove;
   // if kingInCheck==TRUE, then either directCheckSquare or discoveredCheckSquare (or both) will be set
   private boolean kingInCheck; // TRUE if the king is now in check (i.e. the move leading to this posn has checked the king)
   private List<PieceSquareInfo> checkSquares; // set to the square(s) of the piece(s) delivering a check
   // debugging info
   private Position previousPosn; // stores the previous position
   private IMove currentMove; // stores the move made from the previous position to get to this position

   // mainly for tests
   public Position(Square whiteKingsSquare, Square blackKingsSquare) {
      this(new boolean[2][2], whiteKingsSquare, blackKingsSquare);
   }

   // mainly for tests
   public Position(boolean[][] castlingRights, Square whiteKingsSquare, Square blackKingsSquare) {
      this(castlingRights);
      addPiece(Pieces.generateKing(Colour.WHITE), whiteKingsSquare);
      addPiece(Pieces.generateKing(Colour.BLACK), blackKingsSquare);
   }

   public Position() {
      this(new boolean[2][2]);
   }

   public Position(boolean[][] castlingRights) {
      this.board = new byte[64];
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
    * @param move     move just been played in "prevPosn". Can be null.
    */
   public Position(Position prevPosn, IMove move) {
      this.castlingRights = prevPosn.castlingRights;
      this.enpassantSquare = prevPosn.enpassantSquare;
      this.kingsSquare = prevPosn.kingsSquare;
      this.sideToMove = prevPosn.sideToMove;
      this.kingInCheck = prevPosn.kingInCheck;
      this.checkSquares = prevPosn.checkSquares;
      this.board = prevPosn.board.clone();
      this.previousPosn = prevPosn;
      this.currentMove = move;
   }

   public void addPiece(byte piece, int square) {
      Colour colour = Pieces.isWhitePiece(piece) ? Colour.WHITE : Colour.BLACK;
      if (!squareIsEmpty(square)) { throw new IllegalStateException("there is already a " + pieceAt(square) + " at square " + Square.toSquare(square)); }
      if (Pieces.isKing(piece)) {
         if (kingsSquare[colour.ordinal()] != -1) {
            throw new IllegalStateException("a " + colour + " king has already been added at square " + Square.toSquare(kingsSquare[colour.ordinal()]));
         }
         kingsSquare[colour.ordinal()] = square;
      }
      board[square] = piece;
   }

   // convert from Piece to byte
   public void addPiece(Colour col, Piece pt, int sq) {
      this.addPiece(Pieces.generatePiece(pt, col), sq);
   }

   public void addPiece(byte piece, Square square) {
      this.addPiece(piece, square.index());
   }

   // convert from Piece to byte
   public void addPiece(Colour col, Piece pt, Square square) {
      this.addPiece(Pieces.generatePiece(pt, col), square.index());
   }

   public boolean squareIsEmpty(int square) {
      return board[square] == UNOCCUPIED_SQUARE;
   }

   public boolean squareIsEmpty(Square sq) {
      return squareIsEmpty(sq.index());
   }

   public Colour colourOfPieceAt(int square) {
      return Pieces.colourOf(board[square]);
   }

   public Colour colourOfPieceAt(Square square) {
      return colourOfPieceAt(square.index());
   }

   public byte pieceAt(int square) {
      return board[square];
   }

   public byte pieceAt(Square square) {
      return pieceAt(square.index());
   }

   /**
    * Does the piece at square 'sq' match the supplied piece (same piece type and same colour)?
    * 
    * @param sq     square to check
    * @param piece  required piece
    * @param colour
    * @return true if matches
    */
   public boolean matchesPieceTypeAndColour(int sq, byte piece) {
      return pieceAt(sq) == piece;
   }

   public boolean canCastleKingsside(Colour col) {
      return castlingRights[col.ordinal()][0];
   }

   public boolean canCastleQueensside(Colour col) {
      return castlingRights[col.ordinal()][1];
   }

   public void setEnpassantSquare(Square sq) { this.enpassantSquare = sq; }

   public Square getEnpassantSquare() { return enpassantSquare; }

   public int getKingsSquare(Colour col) {
      return kingsSquare[col.ordinal()];
   }

   public Colour getSideToMove() { return sideToMove; }

   public void setSideToMove(Colour sideToMove) { this.sideToMove = sideToMove; }

   public void setCastlingRights(boolean[][] castlingRights) { this.castlingRights = castlingRights; }

   // displays the board (always from white POV, a1 in bottom LHS)
   @Override
   public String toString() {
      String[][] board = new String[8][8];

      // init
      for (int rank = 7; rank >= 0; rank--) {
         for (int file = 0; file < 8; file++) {
            int sq = ((7 - rank) * 8) + file;
            if (this.squareIsEmpty(sq)) {
               board[rank][file] = ".";
            } else {
               byte pt = this.pieceAt(sq);
               board[rank][file] = Pieces.fenSymbol(pt);
            }
         }
      }

      StringBuilder sb = new StringBuilder(150);
      for (int rank = 7; rank >= 0; rank--) {
         for (int file = 0; file < 8; file++) {
            sb.append(board[rank][file]);
         }
         switch (rank) {
         case 7 -> sb.append("   ").append(sideToMove).append(" to move");
         case 6 -> sb.append("   castlingRights: ").append(castlingRightsToString());
         case 5 -> sb.append("   enpassant square: ").append(enpassantSquare);
         case 4 -> sb.append("   hash (zobrist): ").append(hashCode());
         case 3 -> {
            if (kingInCheck) {
               sb.append("   king in check: ").append(checkSquares);
            } else {
               sb.append("   king not in check");
            }
         }
         case 1 -> sb.append("   fen ").append(Fen.encode(this));
         }
         sb.append("\n");
      }
      return sb.toString();
   }

   public Position move(IMove move) {
      Position newPosn = new Position(this, move); // clone current position
      newPosn.processMove(move);
      return newPosn;
   }

   // process the given move, updating internal structures
   private void processMove(IMove move) {
      int sideToMoveOrdinal = this.sideToMove.ordinal();
      byte movingPiece = pieceAt(move.getOrigin());

      if (TEST_IF_VALID) {
         if (move.isCapture()) {
            if (move.isEnpassant()) {
               if (!squareIsEmpty(move.getTarget())) {
                  throw new IllegalStateException(String.format("invalid enpassant move %s, target square is not empty", move));
               } else if (squareIsEmpty(move.getSquareOfPawnCapturedEnpassant())) {
                  throw new IllegalStateException(
                        String.format("invalid enpassant move %s, square %s is empty", move, Square.toSquare(move.getSquareOfPawnCapturedEnpassant())));
               }
            } else if (squareIsEmpty(move.getTarget()))
               throw new IllegalStateException(String.format("invalid capture move %s, target square is empty", move));
         }
         if (!move.isCapture() && !squareIsEmpty(move.getTarget())) {
            throw new IllegalStateException(String.format("invalid non-capture move %s, target square is occupied with: %s %s", move,
                  colourOfPieceAt(move.getTarget()), pieceAt(move.getTarget())));
         }
         if (Pieces.colourOf(pieceAt(move.getOrigin())) != this.sideToMove) {
            throw new IllegalStateException(String.format("invalid move %s, sideToMove is %s", move, sideToMove));
         }
      }
      // remove piece at move.origin, place piece at move.target (implicitly removing piece at move.target)
      board[move.getOrigin()] = UNOCCUPIED_SQUARE;
      board[move.getTarget()] = move.isPromotion() ? move.getPromotedPiece() : movingPiece;
      if (move.isEnpassant()) { board[move.getSquareOfPawnCapturedEnpassant()] = UNOCCUPIED_SQUARE; }

      // move rook too if castling
      if (move.isKingssideCastling() || move.isQueenssideCastling()) {
         int sideOfBoard = move.isKingssideCastling() ? 0 : 1;
         int rookOriginSq = MoveGenerator.rooksCastlingSquareIndex[sideToMove.ordinal()][sideOfBoard];
         int rookTargetSq = MoveGenerator.rooksSquareAfterCastling[sideToMove.ordinal()][sideOfBoard];
         if (TEST_IF_VALID) {
            if (!Pieces.isRook(pieceAt(rookOriginSq))) {
               throw new IllegalStateException(String.format("invalid castling move %s, no rook at %s", move, Square.toSquare(rookOriginSq)));
            }
            if (!squareIsEmpty(rookTargetSq)) {
               throw new IllegalStateException(
                     String.format("invalid castling move %s, rook's target sq %s is not empty", move, Square.toSquare(rookTargetSq)));
            }
            if (!this.castlingRights[sideToMoveOrdinal][sideOfBoard]) {
               throw new IllegalStateException(String.format("invalid move %s, castling no longer allowed", move));
            }
         }
         board[rookOriginSq] = UNOCCUPIED_SQUARE;
         board[rookTargetSq] = Pieces.generateRook(Pieces.colourOf(movingPiece));
      }

      // update enpassantSquare if pawn moved
      if (Pieces.isPawn(movingPiece) && move.isPawnTwoSquaresForward()) {
         this.enpassantSquare = Square.findEnpassantSquareFromMove(Square.toSquare(move.getTarget()));
      } else {
         this.enpassantSquare = null;
      }

      int opponentsSideOrdinal = this.sideToMove.opposite().ordinal();
      boolean castlingRightsChanged = false;
      boolean opponentsCastlingRightsChanged = false;
      boolean kingsCastling = this.castlingRights[sideToMoveOrdinal][0];
      boolean queensCastling = this.castlingRights[sideToMoveOrdinal][1];
      boolean opponentsKingsCastling = this.castlingRights[opponentsSideOrdinal][0];
      boolean opponentsQueensCastling = this.castlingRights[opponentsSideOrdinal][1];

      // update kingsSquare && castling rights if king moved
      if (Pieces.isKing(movingPiece)) {
         this.kingsSquare = this.kingsSquare.clone();
         this.kingsSquare[sideToMoveOrdinal] = move.getTarget();
         castlingRightsChanged = true;
         kingsCastling = false;
         queensCastling = false;
      }

      // check if a rook moved from its starting square, therefore invalidating castling rights
      if (Pieces.isRook(movingPiece)) {
         if (move.getOrigin() == MoveGenerator.rooksCastlingSquareIndex[sideToMoveOrdinal][0] && canCastleKingsside(sideToMove)) {
            castlingRightsChanged = true;
            kingsCastling = false;
         } else if (move.getOrigin() == MoveGenerator.rooksCastlingSquareIndex[sideToMoveOrdinal][1] && canCastleQueensside(sideToMove)) {
            castlingRightsChanged = true;
            queensCastling = false;
         }
      }

      // if a piece captured something on a1/h1 or a8/h8, then opponent can't castle anymore
      if (move.getTarget() == MoveGenerator.rooksCastlingSquareIndex[opponentsSideOrdinal][0] && canCastleKingsside(sideToMove.opposite())) {
         opponentsCastlingRightsChanged = true;
         opponentsKingsCastling = false;
      } else if (move.getTarget() == MoveGenerator.rooksCastlingSquareIndex[opponentsSideOrdinal][1] && canCastleQueensside(sideToMove.opposite())) {
         opponentsCastlingRightsChanged = true;
         opponentsQueensCastling = false;
      }

      // clone and set new castling rights
      if (castlingRightsChanged || opponentsCastlingRightsChanged) {
         this.castlingRights = this.castlingRights.clone();
         if (castlingRightsChanged) { this.castlingRights[sideToMoveOrdinal] = new boolean[] { kingsCastling, queensCastling }; }
         if (opponentsCastlingRightsChanged) { this.castlingRights[opponentsSideOrdinal] = new boolean[] { opponentsKingsCastling, opponentsQueensCastling }; }
      }

      this.sideToMove = this.sideToMove.opposite();
      this.setKingInCheck(move.getCheckSquares());
   }

   /**
    * Determines whether in <b>this position</b> the king of side 'colour' is currently in check. Does not take move info into account.
    * 
    * This method is required when setting up a new position (e.g. from {@link Fen#decode(String)} where we don't have any previous move info.
    * 
    * @param kingsSquare king's square
    * @param colour      king's colour
    * @return an empty list if king is not in check; otherwise, the squares with pieces which give check
    */
   public List<PieceSquareInfo> isKingInCheck(int kingsSquare, Colour colour) {
      Colour opponentsColour = colour.opposite();
      List<PieceSquareInfo> checkSquares = new ArrayList<>(2);
      final byte opponentsColorPawn = Pieces.generatePawn(opponentsColour);
      // *our* colour used to index pawnCaptures, because we want the 'inverse', i.e. squares which attack the given square
      for (int sq : MoveGenerator.pawnCaptures[colour.ordinal()][kingsSquare]) {
         if (matchesPieceTypeAndColour(sq, opponentsColorPawn)) {
            checkSquares.add(new PieceSquareInfo(Pieces.generatePawn(opponentsColour), sq));
            break;
         }
      }

      // a pawn giving check ==> a knight cannot also be giving check
      if (checkSquares.isEmpty()) {
         final byte opponentsColorKnight = Pieces.generateKnight(opponentsColour);
         for (int sq : MoveGenerator.knightMoves[kingsSquare]) {
            if (matchesPieceTypeAndColour(sq, opponentsColorKnight)) {
               checkSquares.add(new PieceSquareInfo(Pieces.generateKnight(opponentsColour), sq));
               break;
            }
         }
      }

      for (Ray ray : Ray.values()) {
         PieceSquareInfo enemyPieceInfo = opponentsPieceOnRay(colour, kingsSquare, ray);
         if (enemyPieceInfo.piece() != 0 && Pieces.canSlideAlongRay(enemyPieceInfo.piece, ray)) {
            checkSquares.add(enemyPieceInfo);
            if (checkSquares.size() == 2) { break; }
         }
      }

      return checkSquares;
   }

   /**
    * Returns the square and type of an opponent's piece on the given ray, starting from (but not including) startSq. If an intervening piece
    * of my colour is found first, returns (null, -1).
    * 
    * @param myColour my colour
    * @param startSq  where to start
    * @param ray      direction
    * @return the piece-type and square of the enemy piece, if found. If no piece was found, returns PieceSquareInfo(null,-1). If piece of my
    *         colour was found, returns PieceSquareInfo(null,square).
    * @see #opponentsPieceOnRay(Colour, int, Ray, int)
    */
   public PieceSquareInfo opponentsPieceOnRay(Colour myColour, int startSq, Ray ray) {
      return opponentsPieceOnRay(myColour, startSq, ray, -1);
   }

   /**
    * Returns the square and type of an opponent's piece on the given ray, starting from (but not including) startSq. If an intervening piece
    * of my colour is found first, returns (null, square).
    * 
    * The 'squareToIgnore' will be ignored, if set.
    * 
    * @param myColour       my colour
    * @param startSq        where to start
    * @param ray            direction
    * @param squareToIgnore square to ignore (used in enpassant calculations). Set to -1 if not required.
    * @return the piece-type and square of the enemy piece, if found. If no piece was found, returns PieceSquareInfo(null,-1). If piece of my
    *         colour was found, returns PieceSquareInfo(null,square).
    */
   public PieceSquareInfo opponentsPieceOnRay(Colour myColour, int startSq, Ray ray, int squareToIgnore) {
      int squareOfInterest = -1;
      byte enemyPiece = (byte) 0;
      for (int potentialEnemySq : Ray.raysList[startSq][ray.ordinal()]) {
         if (potentialEnemySq == squareToIgnore || squareIsEmpty(potentialEnemySq)) { continue; }
         byte pieceAtSquare = pieceAt(potentialEnemySq);
         squareOfInterest = potentialEnemySq;
         if (myColour.opposes(Pieces.colourOf(pieceAtSquare))) { enemyPiece = pieceAtSquare; }
         break; // can stop in any case, having found a piece
      }
      return new PieceSquareInfo(enemyPiece, squareOfInterest);
   }

   public List<IMove> findMoves(Colour sideToMove) {
      return moveGenerator.findMoves(this, sideToMove);
   }

   public boolean isKingInCheck() { return kingInCheck; }

   public void setKingInCheck(List<PieceSquareInfo> checkSquares) {
      if (checkSquares == null || checkSquares.isEmpty()) {
         this.kingInCheck = false;
         this.checkSquares = null;
      } else {
         this.kingInCheck = true;
         this.checkSquares = checkSquares;
      }
   }

   public void setKingInCheck(PieceSquareInfo... checkSquares) {
      setKingInCheck(Arrays.asList(checkSquares));
   }

   public List<PieceSquareInfo> getCheckSquares() { return checkSquares; }

   /**
    * @return a FEN string for this position (FEN is incomplete, missing half moves and clock info).
    * @see {@link Game#getFen()}.
    */
   public String getFen() { return Fen.encode(this); }

   /** returns the FEN representation of the current castling rights */
   public String castlingRightsToString() {
      StringBuilder sb = new StringBuilder(4);
      if (this.canCastleKingsside(Colour.WHITE)) { sb.append('K'); }
      if (this.canCastleQueensside(Colour.WHITE)) { sb.append('Q'); }
      if (this.canCastleKingsside(Colour.BLACK)) { sb.append('k'); }
      if (this.canCastleQueensside(Colour.BLACK)) { sb.append('q'); }
      if (sb.length() == 0) {
         return "-";
      } else {
         return sb.toString();
      }
   }

}
