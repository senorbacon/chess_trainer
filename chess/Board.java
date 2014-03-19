package chess;


import java.util.*;

/**
 * Insert the type's description here.
 */
public class Board {

	public static final char[] fileChars = {' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	public static final int NUM_RANKS = 8;
	public static final int NUM_FILES = 8;
	protected static final int NUM_PIECES = NUM_FILES * 2 * 2;

	// number of plies to calculate
	protected static final int MAX_PLIES_LOOKAHEAD = 1;

	protected static final int MAX_STACK_DEPTH = MAX_PLIES_LOOKAHEAD + 1;

	// size of the move pool = 2 * (32 + 28 + 16 + 26 + 27 + 8) = 2 * 137 = 274
	protected static final int MAX_BOARD_MOVES = 2 * (Pawn.MAX_PAWN_MOVES * 8 +
	                                                  Rook.MAX_ROOK_MOVES * 2 + 
	                                                  Knight.MAX_KNIGHT_MOVES * 2 + 
	                                                  Bishop.MAX_BISHOP_MOVES * 2 + 
	                                                  Queen.MAX_QUEEN_MOVES + 
	                                                  King.MAX_KING_MOVES);

	protected static final int MOVE_POOL_SIZE = MAX_BOARD_MOVES * MAX_STACK_DEPTH;
	protected static final int CHECKING_PIECE_POOL_SIZE = 100;

	protected static final int MAX_ATTACKING_MOVES = 24;

	protected static final int MOVE_FREE = 0;
	protected static final int MOVE_SET  = 1;

	// stack implementation
	protected int stackDepth = 0;
	protected SingleBoard[] boardStack = new SingleBoard[MAX_STACK_DEPTH];
	protected PieceIterator pieceIterator[] = new PieceIterator[MAX_STACK_DEPTH];

	// pools
	protected PiecePool piecePool = new PiecePool(this, MAX_STACK_DEPTH);
	protected MovePool movePool = new MovePool("movePool", MOVE_POOL_SIZE);
	protected Pool checkingPiecePool = new Pool("checkingPiecePool", CHECKING_PIECE_POOL_SIZE,
		new PoolFactoryMethod() {
			public Poolable createPoolObject() {
				return new CheckingPieces();
			}
		}
	);

	// copy of the last move for computing en passant
	protected Move lastMove = null;

	// current move & board status
	protected Move currentMove = null;
	protected int pieceToMoveIx = -1;
	protected int pieceToMove_oldFile;
	protected int pieceToMove_oldRank;
	protected int pieceToCaptureIx = -1;
	// special moves
	protected Rook castlingRook = null;
	protected int castlingRook_oldFile;
	protected int castlingRook_oldRank;
	protected Pawn promotedPawn;
	protected Piece promotedPawnBecomes;

	class SingleBoard
	{
		protected Square board[][] = new Square[NUM_RANKS][NUM_FILES];
		protected int colorToMove = Piece.WHITE;

		// add room for 10 more pieces if we want to add them
		protected Piece[] pieces = new Piece[NUM_PIECES+10];

		protected King whiteKing, blackKing;

		public SingleBoard()
		{
			for (int rank=0; rank<NUM_RANKS; rank++)
			{
				for (int file=0; file<NUM_FILES; file++)
				{
					board[rank][file] = new Square(file+1, rank+1);
				}
			}
		}

		public King getKing(int color)
		{
			return (color == Piece.WHITE)?whiteKing:blackKing;
		}

		public void setKing(King king)
		{
			if (king == null)
			{
				whiteKing = null;
				blackKing = null;
			}
			else if (king.getColor() == Piece.WHITE)
				whiteKing = king;
			else 
				blackKing = king;
		}
	}

	class PieceIterator implements Iterator
	{
		Piece thePieces[];
		boolean colorMatters;
		int color;
		int count;
		int promotedCount;
		
		SingleBoard board;
		int initialStackDepth;

		PieceIterator(int depth)
		{
			initialStackDepth = depth;
			board = boardStack[depth];
			thePieces = board.pieces;
		}
			
		protected PieceIterator reset(boolean colorMatters)
		{
			this.colorMatters = colorMatters;

			count = 0;
			promotedCount = 0;
			return this;
		}
		
		public PieceIterator reset()
		{
			return reset(false);
		}
		
		public PieceIterator reset(int color)
		{
			this.color = color;
			return reset(true);
		}
		
		public boolean hasNext()
		{
			int i;
			boolean valid;

			if (initialStackDepth != stackDepth)
			{
				Debug.debugMsg(this, Debug.WARNING, "Iterator out of context; current depth:iterator depth (" 
				                                    + stackDepth + ":" + initialStackDepth + ")");
				return false;
			}
			
			for (i=count; i<thePieces.length; i++)
			{
				valid = true;
				Piece piece = thePieces[i];

				if (piece == null)
					valid = false;
				else
				if (colorMatters && (piece.getColor() != color))
					valid = false;

				if (valid)
					return true;
			}
	
			return false;				
		}

		public Object next()
		{
			int i;
			boolean valid;
			
			if (initialStackDepth != stackDepth)
			{
				Debug.debugMsg(this, Debug.WARNING, "Iterator out of context; current depth:iterator depth (" 
				                                    + stackDepth + ":" + initialStackDepth + ")");
				return null;
			}
			
			for (i=count; i<thePieces.length; i++)
			{
				valid = true;
				Piece piece = thePieces[i];

				if (piece == null)
					valid = false;
				else
				if (colorMatters && (piece.getColor() != color))
					valid = false;

				if (valid)
				{
					count = i+1;
					return piece;
				}
			}
						
			return null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException("remove not supported in Board.PieceIterator");
		}
	}

	public Board()
	{
		int i, j, k;
		for (i=0; i<MAX_STACK_DEPTH; i++) {
			boardStack[i] = new SingleBoard();
			pieceIterator[i] = new PieceIterator(i);
		}

		resetMoveStatus();
	}
	public void addKings()
		throws PoolFullException
	{
		if (boardStack[stackDepth].getKing(Piece.WHITE) == null &&
		    boardStack[stackDepth].getKing(Piece.WHITE) == null)
		{
			boardStack[stackDepth].setKing((King) addPiece(Piece.KING, Piece.WHITE, 5, 1));
			boardStack[stackDepth].setKing((King) addPiece(Piece.KING, Piece.BLACK, 5, 8));
		}
	}
	public Piece addPiece(int pieceType, int color, int file, int rank)
		throws PoolFullException
	{
		Piece[] pieces = boardStack[stackDepth].pieces;
		Piece piece;

		int i;
		for (i=0; i<pieces.length; i++)
			if (pieces[i] == null) break;

		if (i == pieces.length)
			throw new PoolFullException("Can't add piece to board");

		piece = piecePool.obtainPiece(pieceType, color, file, rank);
		pieces[i] = setSpace(piece, file, rank);

		return piece;
	}
	/**
	 ** calculate checks, attacked squares, etc.
	 */ 
	public void analyze()
		throws PoolFullException, IllegalMoveException
	{
		Debug.debugMsg(this, Debug.INFO, "Starting analyze... movePool size is " + movePool.getNumUsed());
		generatePieceMoves();
		Debug.debugMsg(this, Debug.INFO, "Generated moves...  movePool size is " + movePool.getNumUsed());
		updateSquares();		
	}
	public void clearBoard()
	{
		Piece pieces[] = boardStack[stackDepth].pieces;
		Piece piece;

		for (int i=0; i<pieces.length; i++)
		{
			if (pieces[i] != null)
			{
				piece = pieces[i];
				pieces[i] = null;
				piecePool.releasePiece(piece);
				setSpace(null, piece.getFile(), piece.getRank());
			}
		}
		boardStack[stackDepth].setKing(null);
		
		/* debug */
		/*
		int i, j;
		for (i=0; i<NUM_FILES; i++)
			for (j=0; j<NUM_RANKS; j++)
				if (boardStack[stackDepth].board[i][j].piece != null)
					Debug.debugMsg(this, Debug.ERROR, "board not clear! piece = " + boardStack[stackDepth].board[i][j].piece);
		*/
	}
	protected void commitMove()
		throws IllegalMoveException, PoolFullException, InvalidPieceException
	{
		Piece pieces[] = boardStack[stackDepth].pieces;
		
		if (currentMove == null)
			throw new IllegalMoveException("No move set to commit.");
			
		Piece piece = currentMove.getPiece();

		if (currentMove.isAttackingAPiece())
		{
			piecePool.releasePiece(pieces[pieceToCaptureIx]);
			pieces[pieceToCaptureIx] = null;
		}

		// update piece
		piece.setHasBeenMoved();

		// special moves
		if (castlingRook != null)
			castlingRook.setHasBeenMoved();

		if (promotedPawn != null)
		{
			// replace pawn with the promoted piece in the current board's piece array
			boardStack[stackDepth].pieces[pieceToMoveIx] = promotedPawnBecomes;
			piecePool.releasePiece(promotedPawn);
		}

		// update last move
		if (lastMove != null)
			movePool.release(lastMove);
		lastMove = movePool.cloneMove(currentMove);
		
		resetMoveStatus();
		boardStack[stackDepth].colorToMove = (piece.getColor() == Piece.WHITE)?Piece.BLACK:Piece.WHITE;
	}
	public void commitMove(Move move)
		throws IllegalMoveException, PoolFullException, InvalidPieceException
	{
		movePiece(move);
		commitMove();
	}
	/**
	 *
thoughts on square processing:
  - a good way to go is to run through all the pieces and for each piece iterate
	through all the moves, assigning moves to squares, with the following exceptions:
	non-ATTACK moves (pawn pushes and castling), and pinned+immobile moves

  - Need to take care with situations where move order is important.  Here, the attack
	values are different depending on who's turn it is.  p=white and P=black. If it's 
	white's turn, then p is free to attack P, which sets up a discovered check for B.
	However, if it's black's turn, than p may not defend r, because it becomes pinned.
	This is a discovered pin.  Unless of course, another piece is attacking r.  Yerg.
	
		 . . . .
		  . .Q. .
		 . . B .
		  . P r .
		 . . p .
		  . .k. .

  - If there's a DISCOVERED CHECK move, and it's the piece's color's turn to move,
	all opposing attacks are rendered null. What about
	multiple pieces that attack a square that has a DISCOVERED CHECK?  In this case,
	the discovered-check piece doesn't have to attack the square, so the opposing piece
	could contribute.  I think though for the sake of the drill, finding that discoverd
	check piece is more important - but what about for position analysis?
	Also, if one of the king's moves is to the space that the DISCOVERED CHECK moves to,
	it becomes a '?' move because now we can't know how many pieces are attacking.

	Finally, if a King move is a DISCOVERED CHECK move, and it is also an IMMOBILE move,
	then nothing special happens - why?  because for the king to contribute to that square
	all of the opponents pieces must be exhausted in the attack, which prtty much negates
	whatever advantage is gained from a discovered check.

  - PINNED moves may contribute if 1) removing all opposing pieces that attack the 
	move square doesn't leave king in check and 2) there's a legal/defends/xray(?) move of 
	the same color for every opposing piece removed. PINNED IMMOBILE moves don't contribute.
	What about multiple pins?  

			B . . .
		   . N . .
			. QR. .
		   . .r. .
			. . n .
		   . .r. k

  - IMMOBILE (non-king) moves don't contribute.

  - IMMOBILE (king) An immobile king move may contribute only after all 
	other pieces are calculated to move (turn dependant), the king may move onto the 
	square without being in check. There must be, not counting the
	king's moves, an equal or better balance of force in the king's color for the move 
	to count, reason being that the king needs to eventually be able to take the square.
	It does depend on whose turn it is to move, exactly how many attacking pieces are
	required to make this immobile move work.

  - DEFENDS moves contribute like normal legal moves - subject to pinning and immobile
	rules.  

  - XRAY moves require the most processing of all. Some pretty hackish stuff possibly.
	See the rules below.

  - SKEWER moves contribute like normal attack moves, but only if the opponent's only
	legal moves are king moves.  In addition, if the king could move to a space that
	attacks the skewered spot, it's a ? space.

  - Sometimes we have the old '?' for an answer, in the case of move-order-dependant
	situations like discovered checks or pins with extra attackers, and skewer moves
	where the king could possibly attack the skewered piece.

	*/ 
	protected void countContributingMoves(Square square)
	{
		Move move;
		Move pinnedMove;
		Piece piece;
		Piece pinnedPiece, pinningPiece;
		int pieceColor;
		int colorToMove = this.getColorToMove();
		
		for (int i=0; i<square.moveCount; i++)
		{
			move = square.moves[i];

			// we only count attacking moves
			if (!move.isAttack())
				continue;
			
			piece = move.getPiece();
			pieceColor = piece.getColor();

			// find pinned moves that can contribute; mark pinned moves that can't (as immobile?)
			if ((move.isPinned() != null) && ((move.getFlags() & Move.PINNED_PROCESSED) == 0) )
				processPinnedMove(square, move);
			
			
			// now that we know what moves can and can't contribute, we can look for
			// discovered pins
				
			/*
			if (checkDiscoveredPin(square.attackingMoves, i))
			{
				// todo: there's probably discovered pins that don't depend
				// on move order, let's not write them off this easily
				square.attackedByBlack = -1;
				square.attackedByWhite = -1;
				continue;
			}
			*/

			// at some point we need to process immobile king moves

			// plus we need a clever way to deal with move order
			// it may help in a couple ways here to be forward-looking here and
			// think about assigning points to material lost

			// because finally we need to process xray and skewer moves
			
			// non-king immobile moves don't contribute
			if (move.isImmobile() != null && (piece.getType() != Piece.KING) )
				continue;

			if (pieceColor == Piece.WHITE)
				square.attackedByWhite++;
			else	
				square.attackedByBlack++;
		}
	}
	/**
	 ** calculate legal moves & attack moves for each piece
	 */ 
	public void generatePieceMoves()
		throws PoolFullException, IllegalMoveException
	{
		Iterator iter = getPieces();
		Iterator moves;

		Piece piece;
		Move move;
		while (iter.hasNext())
		{
			piece = (Piece) iter.next();
			piece.generateAllMoves(); 
//debug
//Debug.debugMsg(this, Debug.INFO, "after " + piece + ", movePool is " + movePool.getNumUsed()); 
		}		
	}
	public Pool getCheckingPiecePool()
	{
		return checkingPiecePool;
	}
	public int getColorToMove()
	{
		return boardStack[stackDepth].colorToMove;
	}
	public static char getFileChar(int file)
	{
		if (file >= 1 && file <= NUM_FILES)
			return fileChars[file];
		else
			return ' ';
	}
	public King getKing(int color)
	{
		SingleBoard single = boardStack[stackDepth];
		return (color == Piece.WHITE)?single.whiteKing:single.blackKing;
	}
	public Move getLastMove()
	{
		return lastMove;
	}
	public MovePool getMovePool()
	{
		return movePool;
	}
	// file and rank are expressed as numbers 1-8
	public Piece getPiece(int file, int rank)
	{
		if (file < 1 || file > NUM_FILES ||
			rank < 1 || rank > NUM_RANKS)
			return null;
		return boardStack[stackDepth].board[rank-1][file-1].piece;
	}
	public Iterator getPieces()
	{
		return pieceIterator[stackDepth].reset();
	}
	public Iterator getPieces(int color)
	{
		return pieceIterator[stackDepth].reset(color);
	}
	public Square getSquare(int file, int rank)
	{
		return boardStack[stackDepth].board[rank-1][file-1];
	}
	protected void groupMovesBySquare()
	{
		Square square;
		Iterator iter = getPieces();
		Iterator moves;
		int i;

		Piece piece;
		Move move;
		while (iter.hasNext())
		{
			piece = (Piece) iter.next();
			moves = piece.getAllMoves();
	
			while (moves.hasNext())
			{
				move = (Move) moves.next();
	
				square = getSquare(move.moveToFile(), move.moveToRank());
				square.moves[square.moveCount++] = move;
			}
		}
	}
	protected CheckingPieces kingInCheck(int color, CheckingPieces pieces)
	{
		SingleBoard board = boardStack[stackDepth];
		King king = board.getKing(color);
		int kFile = king.getFile();
		int kRank = king.getRank();
		int opposingColor = (color == Piece.WHITE)?Piece.BLACK:Piece.WHITE;
		int oFile, oRank;
		int oFileDir, oRankDir;
		int i, j;

		Piece temp;

		// pawn check
		int opposingPawnDir = (color == Piece.WHITE)?1:-1;
		oRank = kRank + opposingPawnDir;

		if (squareContains(Piece.PAWN, opposingColor, kFile-1, oRank))
		{
			temp = getPiece(kFile-1, oRank);
			if (pieces.checkingPiece1 == null)
				pieces.checkingPiece1 = temp;
			else if (!pieces.checkingPiece1.equals(temp))
			{
				 pieces.checkingPiece2 = temp;
				 return pieces;
			}
		}
			
		if (squareContains(Piece.PAWN, opposingColor, kFile+1, oRank))
		{
			temp = getPiece(kFile+1, oRank);
			if (pieces.checkingPiece1 == null)
				pieces.checkingPiece1 = temp;
			else if (!pieces.checkingPiece1.equals(temp))
			{
				 pieces.checkingPiece2 = temp;
				 return pieces;
			}
		}

		// knight check
		for (i=0; i<Piece.KNIGHT_MOVE_TABLE_SIZE; i++)
		{
			oFile = kFile + Piece.KnightMoveTable_file[i];
			oRank = kRank + Piece.KnightMoveTable_rank[i];

			if (squareContains(Piece.KNIGHT, opposingColor, oFile, oRank))
			{
				temp = getPiece(oFile, oRank);
				if (pieces.checkingPiece1 == null)
					pieces.checkingPiece1 = temp;
				else if (!pieces.checkingPiece1.equals(temp))
				{
					 pieces.checkingPiece2 = temp;
					 return pieces;
				}
			}
		}

		// rook/queen check
		for (i=0; i<Piece.ROOK_DIR_TABLE_SIZE; i++)
		{
			oFileDir = Piece.RookDirTable_file[i];
			oRankDir = Piece.RookDirTable_rank[i];

			for (j=1; j<NUM_FILES; j++)
			{
				oFile = kFile + oFileDir*j;
				if (oFile < 1 || oFile > NUM_FILES)
					break;

				oRank = kRank + oRankDir*j;
				if (oRank < 1 || oRank > NUM_RANKS)
					break;

				if (squareContains(Piece.ROOK, opposingColor, oFile, oRank) ||
					squareContains(Piece.QUEEN, opposingColor, oFile, oRank) ||
				    (squareContains(Piece.KING, opposingColor, oFile, oRank) && j==1) )
				{
					temp = getPiece(oFile, oRank);
					if (pieces.checkingPiece1 == null)
						pieces.checkingPiece1 = temp;
					else if (!pieces.checkingPiece1.equals(temp))
					{
						 pieces.checkingPiece2 = temp;
						 return pieces;
					}
				}

				// stop if we get to another piece
				if (getPiece(oFile, oRank) != null)
					break;
			}
		}

		// bishop/queen check
		for (i=0; i<Piece.BISHOP_DIR_TABLE_SIZE; i++)
		{
			oFileDir = Piece.BishopDirTable_file[i];
			oRankDir = Piece.BishopDirTable_rank[i];

			for (j=1; j<NUM_FILES; j++)
			{
				oFile = kFile + oFileDir*j;
				if (oFile < 1 || oFile > NUM_FILES)
					break;

				oRank = kRank + oRankDir*j;
				if (oRank < 1 || oRank > NUM_RANKS)
					break;

				if (squareContains(Piece.BISHOP, opposingColor, oFile, oRank) ||
					squareContains(Piece.QUEEN, opposingColor, oFile, oRank) ||
				    (squareContains(Piece.KING, opposingColor, oFile, oRank) && j==1) )
				{
					temp = getPiece(oFile, oRank);
					if (pieces.checkingPiece1 == null)
						pieces.checkingPiece1 = temp;
					else if (!pieces.checkingPiece1.equals(temp))
					{
						 pieces.checkingPiece2 = temp;
						 return pieces;
					}
				}

				// stop if we get to another piece
				if (getPiece(oFile, oRank) != null)
					break;
			}
		}

		return pieces;
	}

	public Piece kingInCheck(int color, Piece tryAnotherPiece)
		throws PoolFullException
	{
		CheckingPieces pieces = ((CheckingPieces) checkingPiecePool.obtain()).reset();

		try {
			kingInCheck(color, pieces);

			if (tryAnotherPiece == null || pieces.checkingPiece2 == null)
				return pieces.checkingPiece1;
				
			if (tryAnotherPiece.equals(pieces.checkingPiece1))
				return pieces.checkingPiece2;
			else
				return pieces.checkingPiece1;
		} finally {
			checkingPiecePool.release(pieces);
		}
	}

	public void movePiece(Move move)
		throws IllegalMoveException, PoolFullException
	{
		// do we have an uncommitted move?  then rollback
		if (currentMove != null)
			rollbackMove();
	
		Piece piece = move.getPiece();

		if ((pieceToMoveIx = whichPiece(piece)) == -1)
			throw new IllegalMoveException("Piece to move (" + piece + ") is not correctly on the board.");

		pieceToMove_oldFile = piece.getFile();
		pieceToMove_oldRank = piece.getRank();
 
		if (move.isAttackingAPiece())
		{
			Piece captured = move.getAttackedPiece();
			if ((pieceToCaptureIx = whichPiece(captured)) == -1)
				throw new IllegalMoveException("Piece to capture (" + captured + ") is not correctly on the board.");
				
			setSpace(null, captured.getFile(), captured.getRank());
		}

		// update board
		setSpace(null, piece.getFile(), piece.getRank());
		setSpace(piece, move.moveToFile(), move.moveToRank());

		// special moves
		if (move.isKingsideCastle())
		{
			int rank = (piece.getColor() == Piece.WHITE)?1:8;
			castlingRook = (Rook) getPiece(8, rank);
			castlingRook_oldFile = 8;
			castlingRook_oldRank = rank;
			setSpace(null, 8, rank);
			setSpace(castlingRook, 6, rank);
		} else
		if (move.isQueensideCastle())
			{
			int rank = (piece.getColor() == Piece.WHITE)?1:8;
			castlingRook = (Rook) getPiece(1, rank);
			castlingRook_oldFile = 1;
			castlingRook_oldRank = rank;
			setSpace(null, 1, rank);
			setSpace(castlingRook, 4, rank);
		} else
		if (move.isPawnPromoted())
		{
			promotedPawn = (Pawn) move.getPiece();
			promotedPawnBecomes = piecePool.obtainPiece(move.pawnPromotedTo(), 
			                                            promotedPawn.getColor(), 
			                                            move.moveToFile(), 
			                                            move.moveToRank());

			setSpace(promotedPawnBecomes, move.moveToFile(), move.moveToRank());
		}

		currentMove = move;
	}
	public boolean pieceBetween(Piece piece, int file1, int rank1, int file2, int rank2)
	{
		int diffX = file1 - file2;
		int diffY = rank1 - rank2;
		int deltaX = (diffX<0)?-diffX:diffX;
		int deltaY = (diffY<0)?-diffY:diffY;

		int file = piece.getFile();
		int rank = piece.getRank();

		if (deltaX != 0 && deltaY != 0 && deltaX != deltaY)
		{
			Debug.debugMsg(this, Debug.ERROR, "pieceBetween(): not a line.");
			return false;
		}

		if (file == file2 &&
		    rank == rank2)
			return true;		

		diffX = (diffX==0)?0:(diffX<0)?-1:1;
		diffY = (diffY==0)?0:(diffY<0)?-1:1;

		for (; (file1 != file2 || rank1 != rank2); file1 -= diffX, rank1 -= diffY)
		{
			if (file == file1 &&
			    rank == rank1)
				return true;
		}

		return false;
	}
	public void placesKingInCheck(Move move, CheckingPieces pieces)
		throws IllegalMoveException, PoolFullException
	{
		movePiece(move);
		kingInCheck(move.getPiece().getColor(), pieces);
		rollbackMove();
	}
	public Piece placesKingInCheck(Move move, Piece tryAnotherPiece)
		throws IllegalMoveException, PoolFullException
	{
		movePiece(move);
		Piece checkingPiece = kingInCheck(move.getPiece().getColor(), tryAnotherPiece);
		rollbackMove();
		return checkingPiece;
	}
	public void placesOpposingKingInCheck(Move move, CheckingPieces pieces)
		throws IllegalMoveException, PoolFullException
	{
		int opposingColor = (move.getPiece().getColor() == Piece.WHITE)?Piece.BLACK:Piece.WHITE;
		movePiece(move);
		kingInCheck(opposingColor, pieces);
		rollbackMove();
	}
	public Piece placesOpposingKingInCheck(Move move, Piece tryAnotherPiece)
		throws IllegalMoveException, PoolFullException
	{
		int opposingColor = (move.getPiece().getColor() == Piece.WHITE)?Piece.BLACK:Piece.WHITE;
		movePiece(move);
		Piece checkingPiece = kingInCheck(opposingColor, tryAnotherPiece);
		rollbackMove();
		return checkingPiece;
	}
	public void popMove()
	{
		if (stackDepth == 0)
		{
			Debug.debugMsg(this, Debug.WARNING, "popping empty stack!");
			return;
		}

		stackDepth--;
	}
	/**
	 *  Check if this pinned move contributes to the square.
	 *  we do this by checking to see if the pinning piece also attacks the current
	 *  square; but we also need to check if the pinning piece is immobile/pinned
	 *   - this is a recursive process in which we determine if the pinning piece is
	 *     pinned by a pinning piece, and so on, until at the end of the chain, if the
	 *     last pinned piece's pinning piece can legally attack the given square, the
	 *     last pinned piece may contribute, and so on up the stack.
	 *   - however, at each step of the recursion, we must make sure that there isn't
	 *     more than one pinning piece (i.e. stacked up) and that any other pinning
	 *     pieces can't contribute, by following the same recursion.  In this case,
	 *     all pinning pieces (really a max of two) must contribute for the pinned piece
	 *     to contribute.
	 *   - as we go, we mark the move with a PINNED_IMMOBILE or a PINNED_CONTRIBUTES
	 *     that way, we don't duplicate the calculations for future recursions on this
	 *     square.
	 */
	public void processPinnedMove(Square square, Move move)
	{
		Piece pinningPiece = move.isPinned();
		Move pinningPieceMove = null;
		boolean pinningPieceImmobile = false;

		// determine if pinning piece also attacks square
		for (int i=0; i<square.moveCount; i++)
		{
			if (square.moves[i].getPiece().equals(pinningPiece))
			{
				pinningPieceMove = square.moves[i];
				break;
			}
		}

		if (pinningPieceMove == null || pinningPieceMove.isImmobile() != null)
			pinningPieceImmobile = true;

		// pinning piece does indeed attack the same square - but can it be moved?
		if (pinningPieceImmobile == false)
		{
			if (pinningPieceMove.isPinned() != null && 
			    (pinningPieceMove.getFlags() & Move.PINNED_PROCESSED) == 0)
				processPinnedMove(square, pinningPieceMove);

			pinningPieceImmobile = ((pinningPieceMove.getFlags() & Move.PINNED_CONTRIBUTES) == 0);

			// xray stuff? like is the pinningPieceMove is an xray move but we need to do xray
			// processing to make sure intermediate pieces aren't immobile sort of thing
		}
		
		move.setFlag(Move.PINNED_PROCESSED);

		if (pinningPieceImmobile = false)
			move.setFlag(Move.PINNED_CONTRIBUTES);
	}
	public void pushMove(Move move)
		throws BoardStackOverflowException, PoolFullException, IllegalMoveException, InvalidPieceException
	{
		if (stackDepth+1 == MAX_STACK_DEPTH)
			throw new BoardStackOverflowException();

		SingleBoard curBoard = boardStack[stackDepth];
		SingleBoard nextBoard = boardStack[++stackDepth];
		Piece piece;

		clearBoard();

		for (int i=0; i<NUM_RANKS; i++)
		{
			for (int j=0; j<NUM_FILES; j++)
			{
				if ((piece = curBoard.board[i][j].piece) != null)
					nextBoard.board[i][j].piece = piecePool.obtainPiece(piece.getType(), piece.getColor(), j, i);
			}
		}

		translateMove(move);
		movePiece(move);
	}
	public void removePiece(Piece piece)
		throws InvalidPieceException
	{
		// note this function is only for test purposes!
		// movePiece() & commitMove() represent the proper mechanism for removing pieces
		setSpace(null, piece.getFile(), piece.getRank());
		
		int index = whichPiece(piece);
		if (index != -1)
			boardStack[stackDepth].pieces[index] = null;
		else
			throw new InvalidPieceException();

		piecePool.releasePiece(piece);
	}
	public void resetBoard()
		throws PoolFullException
	{
		Piece[] pieces = boardStack[stackDepth].pieces;
		
		clearBoard();
		
		if (lastMove != null)
			movePool.release(lastMove);
		lastMove = null;
		
		// white pawns
		for (int i=1; i<=NUM_FILES; i++)
			addPiece(Piece.PAWN, Piece.WHITE, i, 2);
			
		// black pawns
		for (int i=1; i<=NUM_FILES; i++)
			addPiece(Piece.PAWN, Piece.BLACK, i, 7);
		
		addPiece(Piece.ROOK, Piece.WHITE, 1, 1);
		addPiece(Piece.ROOK, Piece.WHITE, 8, 1);
		addPiece(Piece.KNIGHT, Piece.WHITE, 2, 1);
		addPiece(Piece.KNIGHT, Piece.WHITE, 7, 1);
		addPiece(Piece.BISHOP, Piece.WHITE, 3, 1);
		addPiece(Piece.BISHOP, Piece.WHITE, 6, 1);
		addPiece(Piece.QUEEN, Piece.WHITE, 4, 1);	
		
		addPiece(Piece.ROOK, Piece.BLACK, 1, 8);
		addPiece(Piece.ROOK, Piece.BLACK, 8, 8);
		addPiece(Piece.KNIGHT, Piece.BLACK, 2, 8);
		addPiece(Piece.KNIGHT, Piece.BLACK, 7, 8);
		addPiece(Piece.BISHOP, Piece.BLACK, 3, 8);
		addPiece(Piece.BISHOP, Piece.BLACK, 6, 8);
		addPiece(Piece.QUEEN, Piece.BLACK, 4, 8);

		addKings();
	}
	protected void resetMoveStatus() 
	{
		// reset move status
		currentMove = null;
		pieceToMoveIx = -1;
		pieceToMove_oldFile = 0;
		pieceToMove_oldRank = 0;		
		pieceToCaptureIx = -1;
		castlingRook = null;
		castlingRook_oldFile = 0;
		castlingRook_oldRank = 0;
		promotedPawn = null;
		promotedPawnBecomes = null;
	}

	public void resetSquares()
	{
		// reset counters
		Square square;
		for (int i=1; i<=NUM_RANKS; i++)
		{
			for (int j=1; j<=NUM_FILES; j++)
			{
				square = getSquare(i, j);
				square.attackedByWhite = 0;
				square.attackedByBlack = 0;
				for (int k=0; k<square.moveCount; k++)
					square.moves[k] = null;
				square.moveCount = 0;
			}
		}
	}
	public void rollbackMove()
	{
		Piece pieces[] = boardStack[stackDepth].pieces;
		
		if (currentMove == null)
			return;
			
		Piece piece = currentMove.getPiece();

		// restore moved piece
		setSpace(piece, pieceToMove_oldFile, pieceToMove_oldRank);
		setSpace(null, currentMove.moveToFile(), currentMove.moveToRank());
		
		// restore captured piece
		if (currentMove.isAttackingAPiece())
		{
			Piece captured = pieces[this.pieceToCaptureIx];
			setSpace(captured, captured.getFile(), captured.getRank());
		}

		// special moves
		if (currentMove.isKingsideCastle() || currentMove.isQueensideCastle())
		{
			setSpace(null, castlingRook.getFile(), castlingRook.getRank());
			setSpace(castlingRook, castlingRook_oldFile, castlingRook_oldRank);
		} else
		if (currentMove.isPawnPromoted())
		{
			piecePool.releasePiece(promotedPawnBecomes);
		}

		resetMoveStatus();
	}
	public Piece setSpace(Piece piece, int file, int rank)
	{
		boardStack[stackDepth].board[rank-1][file-1].piece = piece;

		// update piece coordinates
		// NOTE: check all references to this function if we remove this
		if (piece != null)
			piece.move(file, rank);

		return piece;
	}
	public boolean squareContains(int type, int color, int file, int rank)
	{
		Piece piece = getPiece(file, rank);
		if ((piece != null) && (piece.getColor() == color) && (piece.getType() == type) )
			return true;
		else
			return false;
	}
	protected void translateMove(Move move)
		throws IllegalMoveException
	{
		Piece newPiece = null, piece = move.getPiece();
		Piece newCapturedPiece = null, capturedPiece = move.getAttackedPiece();

		newPiece = getPiece(piece.getFile(), piece.getRank());
		if (capturedPiece != null)
			newCapturedPiece = getPiece(capturedPiece.getFile(), capturedPiece.getRank());

		if (newPiece == null ||
			(capturedPiece != null && newCapturedPiece == null))
				throw new IllegalMoveException("Couldn't translate move to new board.");

		if (newPiece.getType() != piece.getType())
			throw new IllegalMoveException("Translate move: new piece isn't same type as move piece.");
			
		if (capturedPiece != null && newCapturedPiece.getType() != capturedPiece.getType())
			throw new IllegalMoveException("Translate move: new captured piece isn't same type as captured piece.");
			
		move.resetMove(newPiece, move.moveToFile(), move.moveToRank(), newCapturedPiece);
	}
	protected void updateSquares()
	{
		resetSquares();
		groupMovesBySquare();

		for (int j=1; j<=NUM_RANKS; j++)
			for (int i=1; i<=NUM_FILES; i++)
				countContributingMoves(getSquare(i, j));
	}
	// locate the piece in the pieces array
	public int whichPiece(Piece piece)
	{
		Piece[] pieces = boardStack[stackDepth].pieces;
		for (int i=0; i<pieces.length; i++)
			if (pieces[i] == piece)
				return i;

		Square square = getSquare(piece.getFile(), piece.getRank());
		Debug.debugMsg(this, Debug.WARNING, "Couldn't locate piece " + piece + " on current board.");
		Debug.debugMsg(this, Debug.WARNING, "Square contains: " + square.getPiece());
		return -1;
	}
}
