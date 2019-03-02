package org.rjo.chess.uci;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.rjo.chess.base.Move;
import org.rjo.chess.position.Fen;
import org.rjo.chess.position.Game;

public class UciTest {
	@Test
	public void fromUCIStringNormal() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = UCI.fromUCIString("e8e6", g);
		assertEquals("Re8-e6", m.toString());
	}

	@Test
	public void fromUCIStringCapture() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = UCI.fromUCIString("e8e5", g);
		assertEquals("Re8xe5", m.toString());
	}

	@Test
	public void fromUCIStringPromotionBlack() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/8 b - - 67 34");
		Move m = UCI.fromUCIString("c2c1q", g);
		assertEquals("c2-c1=Q", m.toString());
	}

	@Test
	public void fromUCIStringPromotionCaptureBlack() {
		Game g = Fen.decode("4r1k1/3R2pp/2N3p1/2p1P3/6PK/r7/2p3P1/3N4 b - - 67 34");
		Move m = UCI.fromUCIString("c2d1q", g);
		assertEquals("c2xd1=Q", m.toString());
	}

	@Test
	public void fromUCIStringPromotionWhite() {
		Game g = Fen.decode("4r1k1/3P4/8/8/6PK/r7/8/8 w - - 67 34");
		Move m = UCI.fromUCIString("d7d8n", g);
		assertEquals("d7-d8=N", m.toString());
		g.makeMove(m);
		assertEquals("3Nr1k1/8/8/8/6PK/r7/8/8 b - - 68 34", Fen.encode(g));
	}

	@Test
	public void fromUCIStringPromotionCaptureWhite() {
		Game g = Fen.decode("4r1k1/3P4/8/8/6PK/r7/8/8 w - - 67 34");
		Move m = UCI.fromUCIString("d7e8r", g);
		assertEquals("d7xe8=R", m.toString());
		g.makeMove(m);
		assertEquals("4R1k1/8/8/8/6PK/r7/8/8 b - - 68 34", Fen.encode(g));
	}
}
