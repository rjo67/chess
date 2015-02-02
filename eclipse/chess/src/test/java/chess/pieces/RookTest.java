package chess.pieces;

import java.util.BitSet;

import org.junit.Test;

import chess.BitBoard;
import chess.Colour;

public class RookTest {

   private Rook whiteRook = new Rook(Colour.WHITE);

   @Test
   public void test() {
      BitBoard[][] bb = whiteRook.getMoveBitBoards();
      BitBoard b = bb[0][0];
      System.out.println(b.display());
      long lo = b.getBitSet().toLongArray()[0];

      BitSet bs2 = BitSet.valueOf(new long[] { (lo << 8) | 1 });
      System.out.println(BitBoard.display(bs2));

   }

}
