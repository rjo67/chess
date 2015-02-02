package chess.pieces;

import java.util.List;

import chess.BitBoard;
import chess.Chessboard;
import chess.Colour;
import chess.Move;
import chess.Square;

/**
 * Stores information about the rooks (still) in the game.
 * 
 * @author rich
 */
public class Rook extends BasePiece {

   /**
    * Creates the data structures for the starting position of the rooks.
    * The rooks will be placed according to the default start position.
    * 
    * @param side
    *           used to determine the starting position for the pieces
    */
   public Rook(Colour side) {
      this(side, new Square[0]);
   }

   /**
    * Creates the data structures for the starting position of the rooks.
    * 
    * @param colour
    *           indicates the colour of the pieces
    * @param requiredSquares
    *           required starting position of the pieces (if empty, the standard default positions will be used)
    */
   public Rook(Colour colour, Square... requiredSquares) {

      super(colour, colour.toString() + " Rook");

      if (requiredSquares.length == 0) {
         // use default positions
         switch (colour) {
         case White:
            requiredSquares = new Square[] { Square.a1, Square.h1 };
            break;
         case Black:
            requiredSquares = new Square[] { Square.a8, Square.h8 };
            break;
         }
      }
      pieces = new BitBoard();
      pieces.setBitsAt(requiredSquares);

      initMoveBitBoards();
   }

   @Override
   public String getSymbol() {
      return "R";
   }

   /** @formatter:off */
   byte[][][] b =
      {//a1..h1
       { //a1
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b1
         new byte[] { (byte)0b1011_1111, (byte)0b0100_0000, (byte)0b0100_0000,  (byte)0b0100_0000, (byte)0b0100_0000, (byte)0b0100_0000, (byte)0b0100_0000, (byte)0b0100_0000,},
         //c1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h1
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
       },
       //a2..h2
       { //a2
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h2
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a3..h3
      {  //a3
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h3
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a4..h4
      {  //a4
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h4
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a5..h5
      {  //a5
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h5
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a6..h6
      {  //a6
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h6
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a7..h7
      {  //a7
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h7
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },
      //a8..h8
      {  //a8
         new byte[] { (byte)0b0111_1111, (byte)0b1000_0000, (byte)0b1000_0000,  (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000, (byte)0b1000_0000,},
         //b8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //c8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //d8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //e8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //f8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //g8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 },
         //h8
         new byte[] { 0, (byte) 255, 0, 0, 0, 0, 0, 0 } 
      },

   };
   /** @formatter:on */

   /**
    * Creates for each square a bitboard containing all possible moves.
    */
   // TODO should be static, since is the same for white or black
   private void initMoveBitBoards() {
      moveBitBoards = new BitBoard[8][8];
      for (int rank = 0; rank < 8; rank++) {
         for (int file = 0; file < 8; file++) {
            moveBitBoards[rank][file] = new BitBoard(b[rank][file]);
         }
      }
   }

   @Override
   public List<Move> findMoves(Chessboard chessboard) {
      // TODO Auto-generated method stub
      return null;
   }

}
