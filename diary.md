# Diary #

## TODOs ?

- store Rays in a byte?
- Move as int?


## Overview of commits and performance
date | description | commit | perft (posn6, 5ply: 164.075.551 moves)
---- | ----------- | ------ | -----
20.10.23 | Piece and Colour information stored in one byte, which allows bit operations and reduces object creation. | | average of 10 iterations: 3920,60 ms (41849,6 moves/ms)
16.04.23 | Ray and pawn move optimizations (precaching various values which were previously being calculated) | | average of 10 iterations: 6021,40 ms (27248,7 moves/ms)
12.04.23 | Now on faster PC, perft will now run with 6 threads per default. |  | For comparison: Perft from old design: 13.718 ms, 11960,6 moves/ms, 83,6 nanos/move. From "newchess" design: 7.299 ms, 22804,2 moves/ms, 43,9 nanos/move.
14.09.17 | SlidingPiece::searchNew uses bitmaps for all ray directions. | ?? | 2503,0 moves/ms; 399,5 nanos/move
06.09.17 | SlidingPiece::searchNew uses bitmaps for sliding-pieces. Added ChessBitSet (bitset optimized for 64 bits | ?? | 2302,3 moves/ms, 434,4 nanos/move
26.08.17 | check-cache deactivated. Many other changes, especially limiting move-generation if in-check. | 9c688f9 | 2019,9 moves/ms
17.07.16 | project subdirs removed. Added Rook::findMovesUsingMoveMap, introducing hashmaps. | ac7390a | 2069,0 moves/ms
13.02.16 | rookmoves | eb33026 | 2117,8 moves/ms
27.12.15 | added KingChecker | cb0c20c | 2135,1 moves/ms
24.10.15 | rays | 48188b9 | 2045,9 moves/ms
21.10.15 | | 1faec96 | 2266,6 moves/ms
10.04.15 | pawn optimisations | | 2360,5 moves/ms
06.04.15 | using rays, caching discovered check | | 1522,4 moves/ms
03.04.15 | | | 1148,7 moves/ms


## 12.04.23

Update (since Nov 2021): the 'newchess' project contains a new start using a mailbox representation of the board. No bitboards in sight.

## 26.08.17

Deactivated check-cache, since still buggy (see promotion() Test, ply7) and does not seem to bring any speed-up.

## 20.08.17

fixed bug in check-cache, where a pawn promotion on a north ray to the king would result in a square
being incorrectly marked as 'check'.

Merged checkcache work into master.

## 19.08.17

Added 'BitSetUnifier' interface and two further bitset implementations (lucene and javolution (snapshot version)).
Still work to do for Javolution.

http://lucene.apache.org/core/6_6_0/core/org/apache/lucene/util/FixedBitSet.html
http://javolution.org/apidocs/javolution/util/FastBitSet.html


javautil
operation: flip, time: 17, 0.00000170
operation: set, time: 52, 0.00000520
operation: and, time: 89, 0.00000890
operation: or, time: 68, 0.00000680
operation: xor, time: 107, 0.00001070
operation: cardinality, time: 44, 0.00000440


lucene
operation: flip, time: 14, 0.00000140
operation: set, time: 31, 0.00000310
operation: and, time: 95, 0.00000950
operation: or, time: 63, 0.00000630
operation: xor, time: 63, 0.00000630
operation: cardinality, time: 49, 0.00000490


javolution
operation: flip, time: 13, 0.00000130
operation: set, time: 37, 0.00000370
operation: and, time: 91, 0.00000910
operation: or, time: 67, 0.00000670
operation: xor, time: 66, 0.00000660
operation: cardinality, time: 45, 0.00000450



## 09.07.17
Branch moveswithcheck: Fixed error in posn2 ply4 (which has been wrong since commit f322b19 (introduction of check-cache
and optimizations in SlidingPiece::findDiagonalCheck).

## 28.10.16

findMoves no longer checks to see if the move leaves the opponent's king in check.
This is now done in another method, hopefully to allow for some optimizations. Perft: 1931,7.

## 26.10.16

pawn optimizations. Perft: 1896,7 moves/ms. 

## 22.10.16

Removed use of Hashmap in RayFactory, and then removed RayFactory to BaseRay.

Perft: 1843,2 moves/ms.

## 21.10.16

Removed internal use of HashMaps in favour of arrays where possible.

Current perft: 1763,3 moves/ms.

## 20.10.16

Position: removed 'emptySquares', since this is just totalPieces.flip
'Piece' is now an interface; introduced AbstractPiece and AbstractBitBoardPiece.
'King' no longer uses a BitBoard as data structure.

Current perft: 1563,1 moves/ms.


## 19.10.16

PieceManager: changed 'alreadyCloned' and 'pieces' data structure from HashMap to array.

Current perft: 1459,0 moves/ms.

## 17.10.16

after 'immutable' refactoring: 'Piece' is not yet immutable, therefore need to clone everything every time.

Current perft: 312,5 moves/ms.

## 21.10.15

Removed extra data structures rookAndQueen, bishopAndQueen.

Current perft: 2300,4 moves/ms.

## 22.05.15

* use log4j
* rays - static init

* Current perft:
(oops, regression here ...)
** 5ply:  162.934.523 moves (   66.558 ms) ( 2448,0 moves/ms)
** 5ply:  162.934.523 moves (   64.057 ms) ( 2543,6 moves/ms)

## 12.05.15
alpha-beta search


## 10.04.15
pawn optimisations.

* Current perft: 2360,5 moves/ms.

## 08.04.15
Bug in castle move: need to check for check.

if previous move was not a check, then only need to check if my move exposes the king (i.e. the piece was pinned).
This requires that the 'inCheck' flag always gets set properly.

pawn promotion optimisations.  Current perft: 2190,9 moves/ms.

## 06.04.15
Introdution of "Rays".

Rewrite of discoveredCheck to use RayUtils, i.e. only checking the ray between the opponent's king and the move.from() square.

* Current perft: 1362,6 moves/ms.
* Caching the results of 'discoveredCheck': 1401,4 moves/ms
* After rewriting bishop search to use Rays. Not a huge difference in speed but the code is a lot simpler: 1453,9 moves/ms.
* After rewriting rook and queen search to use Rays: 1522,4 moves/ms -- 25% speed up compared with 03.04.15!

## 03.04.15 Teil 2
updateStructures now completely incremental.
Current perft: 1148,7 moves/ms.

Hotspots:
 attacksSquareOnRankOrFile, isKingInCheck, SlidingPiece.search, attacksSquareOnDiagonal

## 03.04.15
Further optimizations. Introduction of allRooksAndQueens and allBishopsAndQueens.
updateStructures is now incremental for non-captures.
Optimization of attacksSquareOnRankOrFile did not work, code is commented out (see canReachTargetSquare).

Current perft: 957,8 moves/ms. Was up to ca. 1000moves/ms before allRooksAndQueens optimizations...

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


