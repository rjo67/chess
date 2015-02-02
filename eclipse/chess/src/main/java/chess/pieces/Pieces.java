package chess.pieces;

import chess.Colour;

public enum Pieces {

   PAWN {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new Pawn(side);
      }
   },
   ROOK {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new Rook(side);
      }
   },
   KNIGHT {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new Knight(side);
      }
   },
   BISHOP {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new Bishop(side);
      }
   },
   QUEEN {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new Queen(side);
      }
   },
   KING {
      @Override
      public Piece getPieceImpl(Colour side) {
         return new King(side);
      }
   };

   abstract public Piece getPieceImpl(Colour side);

}
