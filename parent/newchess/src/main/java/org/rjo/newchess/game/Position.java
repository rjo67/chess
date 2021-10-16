package org.rjo.newchess.game;

import java.util.List;

import org.rjo.newchess.board.Board.Square;
import org.rjo.newchess.move.Move;
import org.rjo.newchess.piece.Colour;
import org.rjo.newchess.piece.PieceType;

public class Position {

   public final static SquareInfo UNOCCUPIED_SQUARE = SquareInfo.unoccupied();

   public static record SquareInfo(PieceType pieceType, Colour colour) {

      public static SquareInfo unoccupied() {
         return new SquareInfo(null, Colour.UNOCCUPIED);
      }

   }

   private SquareInfo[] board;
   private boolean[][] castlingRights; // 1st dimension: W/B, 2nd dimension: 0 - king's side, 1 - queen's side
   private Square enpassantSquare; // set if previous move was a pawn moving 2 squares forward
   private int[] kingsSquare; // keep track of where the kings are
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

   public Colour colourOfPieceAt(int square) {
      return board[square].colour();
   }

   public PieceType pieceAt(int square) {
      return board[square].pieceType();
   }

   public boolean canCastleKingsside(Colour col) {
      return castlingRights[col.ordinal()][0];
   }

   public boolean canCastleQueensside(Colour col) {
      return castlingRights[col.ordinal()][1];
   }

   public void castleKingsside(Colour col) {
      this.castlingRights[col.ordinal()][0] = false;
   }

   public void castleQueensside(Colour col) {
      this.castlingRights[col.ordinal()][1] = false;
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

   public Position move(Move newMove) {
      // TODO Auto-generated method stub
      return null;
   }

   public List<Move> findMoves(Colour sideToMove) {
      // TODO Auto-generated method stub
      return null;
   }

}
