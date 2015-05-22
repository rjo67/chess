package org.rjo.chess.pieces;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rjo.chess.BitBoard;
import org.rjo.chess.Chessboard;
import org.rjo.chess.Colour;
import org.rjo.chess.Game;
import org.rjo.chess.Move;
import org.rjo.chess.Square;

public abstract class Piece implements Cloneable {

   // type of this piece
   private PieceType type;

   // stores position of the piece(s) of a particular kind (queen, pawns, ...)
   protected BitBoard pieces;

   // stores the colour of the piece
   protected Colour colour;

   @Override
   public Object clone() throws CloneNotSupportedException {
      Piece piece = (Piece) super.clone();
      piece.pieces = new BitBoard(pieces.cloneBitSet());
      return piece;
   }

   /**
    * @return the symbol for this piece.
    */
   public String getSymbol() {
      return type.getSymbol();
   }

   /**
    * Initialises data structures to the starting position of the pieces.
    *
    * @see #initPosition(Square...).
    */
   abstract public void initPosition();

   /**
    * Finds all possible moves for this piece type in the given game.
    * Delegates to {@link #findMoves(Game, boolean)} with 2nd parameter FALSE.
    *
    * @param game
    *           current game state.
    * @return a list of all possible moves.
    */
   public final List<Move> findMoves(Game game) {
      return findMoves(game, false);
   }

   /**
    * Finds all possible moves for this piece type in the given game.
    *
    * @param game
    *           current game state.
    * @param kingInCheck
    *           indicates if the king is currently in check. This limits the available moves.
    * @return a list of all possible moves.
    */
   abstract public List<Move> findMoves(Game game, boolean kingInCheck);

   /**
    * Checks to see if the given square is attacked by one or more pieces of this piece type.
    *
    * @param chessboard
    *           the board
    * @param targetSq
    *           the square to check.
    * @return true if it is attacked, otherwise false.
    */
   abstract public boolean attacksSquare(Chessboard chessboard, Square targetSq);

   /**
    * Carries out the move for this piece type, i.e. updates internal structures.
    * More complicated situations e.g. promotions, captures are dealt with in {@link Game#move(Move)}.
    *
    * @param move
    *           the move to make
    */
   public void move(Move move) {
      if (!pieces.getBitSet().get(move.from().bitIndex())) {
         throw new IllegalArgumentException("no " + type + " found on square " + move.from() + ". Move=" + move);
      }
      pieces.getBitSet().clear(move.from().bitIndex());
      if (!move.isPromotion()) {
         pieces.getBitSet().set(move.to().bitIndex());
      }
   }

   /**
    * Reverses the move for this piece type, i.e. updates internal structures.
    * More complicated situations e.g. promotions, captures are dealt with in {@link Game#unmove(Move)}.
    *
    * @param move
    *           the move to undo
    */
   public void unmove(Move move) {
      if (!move.isPromotion()) {
         if (!pieces.getBitSet().get(move.to().bitIndex())) {
            throw new IllegalArgumentException("no " + type + " found on square " + move.to() + ". Unmove=" + move);
         }
         pieces.getBitSet().clear(move.to().bitIndex());
      }
      pieces.getBitSet().set(move.from().bitIndex());
   }

   /**
    * Removes the captured piece in a capture move from the internal data structures for that piece type.
    *
    * @param square
    *           from where to remove the piece
    */
   public void removePiece(Square square) {
      if (!pieces.getBitSet().get(square.bitIndex())) {
         throw new IllegalArgumentException("no " + type + " found on square " + square);
      }
      pieces.getBitSet().clear(square.bitIndex());
   }

   /**
    * Adds a piece to the internal data structures at the given square. Mainly for promotions.
    * No error checking is performed here.
    *
    * @param square
    *           where to add the piece
    */
   public void addPiece(Square square) {
      pieces.getBitSet().set(square.bitIndex());
   }

   protected Piece(Colour colour, PieceType type) {
      this.colour = colour;
      this.type = type;
   }

   public Colour getColour() {
      return colour;
   }

   public BitBoard getBitBoard() {
      return pieces;
   }

   /**
    * Sets the start squares for this piece type to the parameter(s).
    *
    * @param requiredSquares
    *           all required squares.
    */
   public void initPosition(Square... requiredSquares) {
      pieces = new BitBoard();
      if (requiredSquares != null) {
         pieces.setBitsAt(requiredSquares);
      }
   }

   /**
    * Returns all the squares currently occupied by this piece type.
    *
    * @return the squares currently occupied by this piece type
    */
   public Square[] getLocations() {
      Set<Square> set = new HashSet<>();
      for (int i = pieces.getBitSet().nextSetBit(0); i >= 0; i = pieces.getBitSet().nextSetBit(i + 1)) {
         set.add(Square.fromBitIndex(i));
      }
      return set.toArray(new Square[set.size()]);
   }

   /**
    * Returns the FEN symbol for this piece. Delegates to {@link PieceType#getFenSymbol(Colour)}.
    *
    * @return the FEN symbol for this piece.
    */
   public String getFenSymbol() {
      return type.getFenSymbol(colour);
   }

   @Override
   public String toString() {
      return colour.toString() + " " + type.toString();
   }

   public PieceType getType() {
      return type;
   }

   /**
    * Returns true if this piece type is present on the given square.
    *
    * @param targetSquare
    *           square of interest.
    * @return true if this piece type is present, otherwise false.
    */
   public boolean pieceAt(Square targetSquare) {
      return pieces.getBitSet().get(targetSquare.bitIndex());
   }

   /**
    * Calculates the piece-square value in centipawns.
    * For each piece, its piece_value is added to the square_value of the square where it currently is.
    *
    * @return the piece-square value in centipawns (for all pieces of this type).
    */
   public abstract int calculatePieceSquareValue();

   public static int pieceSquareValue(final BitSet piecesBitSet, final Colour colour, final int pieceValue,
         final int[] squareValue) {
      int value = 0;
      for (int i = piecesBitSet.nextSetBit(0); i >= 0; i = piecesBitSet.nextSetBit(i + 1)) {
         int sqValue;
         if (colour == Colour.WHITE) {
            sqValue = squareValue[i];
         } else {
            sqValue = squareValue[63 - i];
         }
         value += pieceValue + sqValue;
      }
      return value;
   }

}
