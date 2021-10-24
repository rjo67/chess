package org.rjo.newchess.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.rjo.newchess.board.Board.Square;

public class FenTest {

   @Test
   public void startPosition() {
      assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", Fen.encode(Game.create()));
   }

   @Test
   public void invalidDelimiters() {
      assertThrows(IllegalArgumentException.class, () -> Fen.decode("rnbqkbnr/ppp/ppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"));
   }

   @Test
   public void invalidEndOfRank() {
      assertThrows(IllegalArgumentException.class, () -> Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKB w - - 0 1")); // last rank not complete
   }

   @Test
//   @Disabled
   public void fromStartPosition() {
      Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1").getPosition().toString();
   }

   @Test
   public void encodeDecode() {
      String fen = "4r3/p1p1p1p1/8/8/8/8/k1K5/4Qr2 w - - 0 1";
      assertEquals(fen, Fen.encode(Fen.decode(fen)));
   }

   @Test
   public void kingInCheck() {
      Position posn = Fen.decode("2k1r3/p1p1p1p1/B7/8/8/8/2K5/4n3 w - - 0 1").getPosition();
      assertTrue(posn.isKingInCheck());
      assertEquals(1, posn.getCheckSquares().size());
      assertTrue(posn.getCheckSquares().contains(Square.e1.index()));

      // same again with black to move
      posn = Fen.decode("2k1r3/p1p1p1p1/B7/8/8/8/2K5/4n3 b - - 0 1").getPosition();
      assertTrue(posn.isKingInCheck());
      assertEquals(1, posn.getCheckSquares().size());
      assertTrue(posn.getCheckSquares().contains(Square.a6.index()));
   }

   @Test
   public void kingInDiscoveredCheck() {
      Position posn = Fen.decode("2k1r3/p1p1N1p1/2q5/8/6B1/4n3/2K5/8 w - - 0 1").getPosition();
      assertTrue(posn.isKingInCheck());
      // black pieces checking
      assertEquals(2, posn.getCheckSquares().size());
      assertTrue(posn.getCheckSquares().contains(Square.c6.index()));
      assertTrue(posn.getCheckSquares().contains(Square.e3.index()));

      // same again with black to move
      posn = Fen.decode("2k1r3/p1p1N1p1/2q5/8/6B1/4n3/2K5/8 b - - 0 1").getPosition();
      assertTrue(posn.isKingInCheck());
      assertEquals(2, posn.getCheckSquares().size());
      assertTrue(posn.getCheckSquares().contains(Square.e7.index()));
      assertTrue(posn.getCheckSquares().contains(Square.g4.index()));
   }

   @Test
   public void kingInDiscoveredCheckWithPawn() {
      // starting posn: 3k13/2p5/3P4/8/8/3R4/2K5/8 w - - 0 1
      // then d6xc7+
      Position posn = Fen.decode("3k13/2P5/8/8/8/3R4/2K5/8 b - - 0 1").getPosition();
      assertTrue(posn.isKingInCheck());
      // white pieces checking
      assertEquals(2, posn.getCheckSquares().size());
      assertTrue(posn.getCheckSquares().contains(Square.c7.index()));
      assertTrue(posn.getCheckSquares().contains(Square.d3.index()));
   }

}
