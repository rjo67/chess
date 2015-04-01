chess
-----

explorations in the world of chess computer programming

current state: move generator seems to be bugfree.

## TODO
* optimization of the move generator, since it maxes on ca. 300-350 moves/ms.
Hotspots:
    org.rjo.chess.Chessboard.updateStructures()
    org.rjo.chess.pieces.Pawn.captureRight()
    org.rjo.chess.pieces.Pawn.captureLeft()
    org.rjo.chess.pieces.SlidingPiece.attacksSquareOnRankOrFile()
    org.rjo.chess.pieces.SlidingPiece.attacksSquareOnDiagonal()
    org.rjo.chess.Chessboard.isKingInCheck()


## 31.03.15

* pawn capture did not check for possible promotion
* added a lot more perft tests. Good results.

* Stalemate position led to "off-by-one" problem, therefore rewrote perft routine
to just use ints instead of storing the moves. Also solves memory problems if not the time problem.


## 30.03.15
* fixed enpassant bug (posn3):
 f4xe3 (e.p.) not allowed here since would be check: e2-e4 -> 17:f4-f3, d6-d5, c7-c6, c7-c5, f4xe3, Rh5-h6, Rh5-h7, Rh5-h8, Rh5-g5, Rh5-f5, Rh5-e5, Rh5-d5, Rh5-c5, Rh5xb5+, Kh4-g3, Kh4-g4, Kh4-g5


## 29.03.15
perft2
1ply finished in 23
2ply finished in 50
3ply finished in 734
4ply finished in 22629 (with debug info)

  4ply: java.lang.AssertionError: wrong number of moves expected:<4085603> but was:<4079729>

perft6:
2ply finished in 45
3ply finished in 629
4ply finished in 16518

* Fixed bug in attacksSquare.  startSquare==targetSquare was being counted as the target being attacked.
  Found by posn5: king capture bug after Qd1xd7+ -> Nb8xd7, Bc8xd7, Qd8xd7. Missing move Ke8xd7.

## 24.03.15
Bugs in castling rights move/unmove

## 22.03.15
move/unmove finished. Perft 4 from start position works. Corrected bugs in check evaluation and pinned pieces.

## 08.03.15
first steps to playing a move. Move.java expanded to hold further details e.g. which piece was captured,
whether an en passant move, castling, in preparation for the 'unmove' funtion.

## 04.03.15 
Handling of discovered checks.

## 26.02.15 
Finalised routines to check if a square is attacked by an opposing piece.
Improved King castling.

## 22.02.15 
Routines to check if a square is attacked by an opposing piece.

## 16.02.15 
queen moves.
perft (performance testing) to test move generation. Looks good! (Only 1-ply a.t.m.)
King: castling implemented (w/o regard for squares in check).
Main class is now 'Game', containing a 'Chessboard'.

Still TODO: identification of checks. 

## 14.02.15 
rook and bishop moves.

## 11.02.15 
rewrite pawn moves to accommodate black moves.

## 08.02.15 
finalized pawn moves and added king moves.
Both only for white!

## 06.02.15
added pawn moves


