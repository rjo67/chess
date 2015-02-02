package chess.pieces;

import java.util.List;
import java.util.Set;

import chess.BitBoard;
import chess.Chessboard;
import chess.Move;
import chess.Square;

public interface Piece {

   public BitBoard getBitBoard();

   /**
    * @return a string representation of the piece, used when displaying a move.
    */
   public String getSymbol();

   /**
    * @return a string representation of the piece, used for the FEN representation.
    */
   public String getFenSymbol();

   /**
    * Returns the set of squares currently occupied by this type of piece.
    * 
    * @return a set of squares.
    */
   public Set<Square> getLocations();

   /**
    * Calculates the possible moves for this piece in the current game position.
    * No 'advanced' legality checks z.b. king in check.
    * 
    * @param chessboard
    *           game state
    * @return a list of moves.
    */
   List<Move> findMoves(Chessboard chessboard);
}
