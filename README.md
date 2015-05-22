chess
-----

explorations in the world of chess computer programming

current state: move generator seems to be bugfree. Now working on performance.

## TODO
* rewrite isKingInCheck.
* optimize King to not use bitboard? findKing().  Maybe other pieces too.

* iterative deepening
* thread for search, interrupt from uci controller

## 22.05.15

* use log4j
* rays - static init
5ply:  162.934.523 moves (   66.558 ms) ( 2448,0 moves/ms)
5ply:  162.934.523 moves (   64.057 ms) ( 2543,6 moves/ms)

## 12.05.15
alpha-beta search


## 10.04.15
pawn optimisations:
5ply:  164.075.551 moves (   74.889 ms) ( 2190,9 moves/ms)
5ply:  164.075.551 moves (   72.033 ms) ( 2277,8 moves/ms)
5ply:  164.075.551 moves (   69.508 ms) ( 2360,5 moves/ms)

## 08.04.15
* Bug in castle move: need to check for check.
* if previous move was not a check, then only need to check if my move exposes the king (i.e. the piece was pinned).
This requires that the 'inCheck' flag always gets set properly.
5ply:  164.075.551 moves (   79.460 ms) ( 2064,9 moves/ms)
* pawn promotion optimisations
5ply:  164.075.551 moves (   74.889 ms) ( 2190,9 moves/ms)

## 06.04.15
Introdution of "Rays".
Rewrite of discoveredCheck to use RayUtils, i.e. only checking the ray between the opponent's king and the move.from() square.
5ply:  164.075.551 moves (  120.411 ms) ( 1362,6 moves/ms)

* Caching the results of 'discoveredCheck':
5ply:  164.075.551 moves (  117.081 ms) ( 1401,4 moves/ms)
* After rewriting bishop search to use Rays. Not a huge difference in speed but the code is a lot simpler.
5ply:  164.075.551 moves (  112.854 ms) ( 1453,9 moves/ms)
* After rewriting rook and queen search to use Rays.
5ply:  164.075.551 moves (  107.777 ms) ( 1522,4 moves/ms) -- 25% speed up compared with 03.04.15!

## 03.04.15 Teil 2
updateStructures now completely incremental.
Speed now:
5ply:  164.075.551 moves (   142.830 ms) (  1148,7 moves/ms)

Hotspots:
 attacksSquareOnRankOrFile, isKingInCheck, SlidingPiece.search, attacksSquareOnDiagonal

## 03.04.15
Further optimizations. Introduction of allRooksAndQueens and allBishopsAndQueens.
updateStructures is now incremental for non-captures.
Optimization of attacksSquareOnRankOrFile did not work, code is commented out (see canReachTargetSquare).

Speed now:
5ply:  164.075.551 moves (   171.302 ms) (   957,8 moves/ms)

Was up to ca. 1000moves/ms before allRooksAndQueens optimizations...

## 02.04.15
Optimizations e.g. updateStructures, Pawn.attacksSquare

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


