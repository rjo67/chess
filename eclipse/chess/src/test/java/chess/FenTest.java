package chess;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FenTest {

   @Test
   public void startPosition() {
      assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", Fen.encode(new Chessboard()));
   }

}
