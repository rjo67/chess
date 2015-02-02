package chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FenTest {

   @Test
   public void startPosition() {
      assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", Fen.encode(new Chessboard()));
   }

   @Test(expected = IllegalArgumentException.class)
   public void invalidDelimiters() {
      Fen.decode("rnbqkbnr/ppp/ppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 2");
   }

   @Test(expected = IllegalArgumentException.class)
   public void invalidEndOfRank() {
      Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKB w - - 0 2"); // last rank not complete
   }

   @Test
   public void fromStartPosition() {
      Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 2").debug();
   }

   @Test
   public void encodeDecode() {
      String fen = "4r3/p1p1p1p1/8/8/8/8/k1K5/4Qr2 w - - 0 2";
      assertEquals(fen, Fen.encode(Fen.decode(fen)));
   }

}
