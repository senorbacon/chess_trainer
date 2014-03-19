Ideas for chessdrills.com


- opening moves drills
- guess the result of the game 5/10 moves before it ends (less points for predicting draws)


Money makin'
 - To play, you need to create an account (very simple)

 - To be ranked, you need to pay for a membership account. $1/month $10/year $25/permanent (or something)

 - Members are ranked by efficiency, which is calculated by (avg. time) + (avg. error)*error_penalty
    - There are two ranking lists: normal and blitz.  In normal, error_penalty is 30 seconds. (or something)
                                                    In blitz, error_penalty is 5 seconds.
    - This should encourage fast thinking in blitz and more deliberate thinking in normal mode.
    - Members select which ranking list they want - both, normal, blitz, none.  A default is saved for the 
      member, which can be customized. The default is both.

 - You can reset your record at any time.

 - Members may also practice (no ranking). In fact, this is the only option available to non-members.

 - Anyone may download the practice application.  This application comes with a zipped archive of .pgn
   games, but players may supply their own games.  Application also allows players to create their own
   positions by dragging them.  This might be handy to develop early because of the testing potential.

 - The practice application is part sales tool.  Include tantalizing links to join membership, so
   player can be ranked.  
    - If player joins from the application, it uploads any practice results to the
      account.  It also gets a key from the application which transforms the sales portion 
      to a customized user portion.  Ability to continue to upload games (whic hrequires passwd)
    - A key can be provided from a link on the member's home page, which can be used to 'personalize'
      the app.  
    - If a login attempt fails 3 times, it turns off the registration.  
    - The personalization thing is really nothing more than a reason to turn off the 'become a member
      to be ranked' link, and to provide a handy interface to their account (which only helps if
      they've got an internet connection).
  
Security
  - None needed!  Player ranking discourages people from sharing accounts.  The software that gets 
    released is free and encourages people to pay for accounts.

Picture of me getting beat by puppy.


implement scheme in which we cache some number of random games (100?) from the 
PGN file then throw away the parse nodes.  reload the pgn file as nec. 

test push, pop

do square processing!  owww!
  - for each square, collect all the attack moves - DONE
  - for each square, determine whether each move contributes


Capturing king is a legal move, if it's out of turn. Not a problem, but it points
  to the blurriness of the concept of legal. The question is whether the move.isLegal() 
  should contain the logic to tell whether the move is out of turn, by referncing 
  piece.getBoard().getColorToMove() or something.

move.toString() - do clarifying letter

Now, moves have status - LEGAL, PINNED, IMMOBILE, XRAY, DEFENDS
  - PINNED simply means that moving the piece places the king in check. 
    A pinned move can only contribute to an attacking square if all sources
    of the pin also attack the same square. 
  - IMMOBILE means that moving the piece leaves the king in check. IMMOBILE 
    moves can't contribute to attacking a square.
  - DEFENDS are for illegal moves that either attack pieces of the same color,
    or are pawn attack moves to empty spaces.  Also, king moves to squares that
    are attacked by opponent pieces are DEFENDS moves.
  - XRAY means that, say, a rook has a queen in front of it; they both
    attack squares beyond the queen... xray moves continue on, through any
    pieces that	attack in the same direction as the piece in question (of 
    either color), including pawns (on diagonals only) and opposing kings.  

	What happens at x?

      -A-          -B-         -C-
     . . r .     . P . .    .rr .q.
	RQ. Kx.       x . R.
     . . . .     R p . k
    . . .q.       . . . .
                 . . q . 
                  . . b .

 	A) XRAY and DEFENDS moves are predicated on the spaces in
       question being the site of a combinational bloodbath.  Since the King would
       need to be devoured for the Rook or Queen to come into play, those moves can't
       contribute to x.  However, if the opposing rook and queen didn't attack x, the
       King could in fact move to x, so the XRAY moves are created.  Later, when they're
       processed, the King's ability to attack x will determine whether or not the XRAY
       moves contribute.

    B) Here, q and b moves to the site of p are DEFENDS moves.  But moves to x are indeed
       XRAY moves.  Moves beyond x are not, since the pawn can only move the one space...
       OR CAN IT???  here, the pawn is pinned by the opposing Rook.  Therefore, the XRAY
       moves don't contribute.

    C) If we have rrq, than q is a DEFENDS move for the first rook, and 
       the first rook is a DEFENDS move for q.


A thought about skewer moves:
    
    Q. .k.r.    We can say that the Queen here is attacking the rook iff there are no
     ^^^        no legal moves except king moves.  If we do this (and we will), then we
                also need to check if the king is then in a position to defend the rook.
   Therefore XRAY moves that go beyond a king are SKEWER moves.  Can we do this with XRAY?

A thought about discovered checks:

    Q.N.k. .    Here, the Knight capture of the pawn opens a discovered check on the pawn.
    . . . .     A call to placesKingInCheck(opposite color) sets this as a DISCOVERED_CHECK
     p . .B.    move if the checking piece is different from the Knight.  Jesus, do we have 
    . b . .     enough different move flags?  Might be time to implement the old bitfield.
                Anyway, a DISCOVERED_CHECK move renders all defense of the attacked piece 
   to be nullified, as the next move must remove check, which can't be done by attacking the
   Knight.  Nice!

How about regular checking moves:

    . . r k     Here, if Q or B takes p, the king is in check.  So what?  
     . q . .
    Q . p .
     . B . .





    create move controls, forward & backward
    new game button
    pgn tag display

	improve appearance - how?

	drag and drop
		debug problem with y coordinate being off

	check/number boxes in SquareGuis - keyboard control