chess
-----

explorations in the world of chess computer programming



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


