package org.rjo.chess.pieces;

import org.junit.Test;
import org.rjo.chess.Colour;
import org.rjo.chess.Fen;
import org.rjo.chess.Game;
import org.rjo.chess.TestUtil;

public class BlackPawnTest {
   private Piece pawn;
   private Game game;

   private void setupGame(String fen) {
      game = Fen.decode(fen);
      pawn = game.getChessboard().getPieces(Colour.BLACK).get(PieceType.PAWN);
   }

   @Test
   public void startPosition() {
      Game game = new Game();
      Pawn pawn = new Pawn(Colour.BLACK, true);
      TestUtil.checkMoves(pawn.findMoves(game), "a7-a6", "a7-a5", "b7-b6", "b7-b5", "c7-c6", "c7-c5", "d7-d6", "d7-d5",
            "e7-e6", "e7-e5", "f7-f6", "f7-f5", "g7-g6", "g7-g5", "h7-h6", "h7-h5");
   }

   @Test
   public void blockedPawn() {
      setupGame("4k3/p7/p7/8/8/8/8/4K3 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "a6-a5");
   }

   @Test
   public void captureLeft() {
      setupGame("4k3/8/8/1p6/PP6/8/8/4K3 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "b5xa4");
   }

   @Test
   public void captureLeftPromotion() {
      setupGame("4k3/8/8/8/8/8/1p6/RK6 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "b2xa1=R+", "b2xa1=N", "b2xa1=B", "b2xa1=Q+");
   }

   @Test
   public void captureRight() {
      setupGame("4k3/8/8/8/p7/PP6/8/4K3 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "a4xb3");
   }

   @Test
   public void captureRightPromotion() {
      setupGame("4k3/8/8/8/8/8/2p5/2KR4 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "c2xd1=R+", "c2xd1=N", "c2xd1=B", "c2xd1=Q+");
   }

   @Test
   public void promotion() {
      setupGame("4k3/8/8/8/8/8/p7/4K3 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "a2-a1=Q+", "a2-a1=B", "a2-a1=N", "a2-a1=R+");
   }

   @Test
   public void enpassantRight() {
      setupGame("4k3/8/8/8/pP6/8/8/4K3 b - b3 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "a4-a3", "a4xb3");
   }

   @Test
   public void enpassantLeft() {
      setupGame("4k3/8/8/8/Pp6/8/8/4K3 b - a3 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "b4-b3", "b4xa3");
   }

   @Test
   public void enpassantWithTwoCandidatePawns() {
      setupGame("4k3/8/8/8/1pPp4/8/8/4K3 b - c3 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "b4-b3", "b4xc3", "d4-d3", "d4xc3");
   }

   @Test
   public void checkLeft() {
      setupGame("4k3/8/8/3p4/8/2K5/8/8 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "d5-d4+");
   }

   @Test
   public void checkRight() {
      setupGame("4k3/8/8/3p4/8/4K3/8/8 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "d5-d4+");
   }

   @Test
   public void checkCaptureLeft() {
      setupGame("4k3/8/8/3p4/2P5/1K6/8/8 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "d5-d4", "d5xc4+");
   }

   @Test
   public void checkCaptureRight() {
      setupGame("4k3/8/8/3p4/3PP3/5K2/8/8 b - - 0 0");
      TestUtil.checkMoves(pawn.findMoves(game), "d5xe4+");
   }

}
