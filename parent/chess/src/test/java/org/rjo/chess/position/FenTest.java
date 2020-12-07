package org.rjo.chess.position;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FenTest {

	@Test
	public void startPosition() {
		assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", Fen.encode(new Game()));
	}

	@Test
	public void invalidDelimiters() {
		assertThrows(IllegalArgumentException.class,
				() -> Fen.decode("rnbqkbnr/ppp/ppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"));
	}

	@Test
	public void invalidEndOfRank() {
		assertThrows(IllegalArgumentException.class,
				() -> Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKB w - - 0 1")); // last rank not complete
	}

	@Test
	@Disabled
	public void fromStartPosition() {
		Fen.decode("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1").getPosition().debug();
	}

	@Test
	public void encodeDecode() {
		String fen = "4r3/p1p1p1p1/8/8/8/8/k1K5/4Qr2 w - - 0 1";
		assertEquals(fen, Fen.encode(Fen.decode(fen)));
	}

}
