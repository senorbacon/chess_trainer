package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public abstract class Piece implements Poolable  {
	public final static int WHITE = 1;
	public final static int BLACK = 2;

	public final static int PAWN   = 1;
	public final static int ROOK   = 2;
	public final static int KNIGHT = 3;
	public final static int BISHOP = 4;
	public final static int QUEEN  = 5;
	public final static int KING   = 6;

	protected final static String pieceNames[] = { "",
	                                               "pawn",
	                                               "rook",
	                                               "knight",
	                                               "bishop",
	                                               "queen",
	                                               "king"     };

	protected final static char pieceLetter[] = {' ', 'P', 'R', 'N', 'B', 'Q', 'K'};

	public static final int KNIGHT_MOVE_TABLE_SIZE = 8;
	public static final int KnightMoveTable_file[] = {2, 1, -1, -2, -2, -1,  1,  2};
	public static final int KnightMoveTable_rank[] = {1, 2,  2,  1, -1, -2, -2, -1};
	
	public static final int ROOK_DIR_TABLE_SIZE = 4;
	public static final int RookDirTable_file[] = {1, 0, -1,  0};
	public static final int RookDirTable_rank[] = {0, 1,  0, -1};

	public static final int BISHOP_DIR_TABLE_SIZE = 4;
	public static final int BishopDirTable_file[] = {1, -1, -1,  1};
	public static final int BishopDirTable_rank[] = {1,  1, -1, -1};

	public static final int QUEEN_DIR_TABLE_SIZE = 8;
	public static final int QueenDirTable_file[] = {1, 1, 0, -1, -1, -1,  0,  1};
	public static final int QueenDirTable_rank[] = {0, 1, 1,  1,  0, -1, -1, -1};

	public static final int KING_MOVE_TABLE_SIZE = 8;
	public static final int KingMoveTable_file[] = {1, 1, 0, -1, -1, -1,  0,  1};
	public static final int KingMoveTable_rank[] = {0, 1, 1,  1,  0, -1, -1, -1};
	
	protected Board board;

	protected int type;
	protected int color;
	protected int file;
	protected int rank;

	protected int poolID;

	protected boolean hasBeenMoved;

	protected Move moves[];
	protected MovesIterator movesIterator = new MovesIterator();

	class MovesIterator implements Iterator
	{
		int count;
		boolean wantLegal;
		boolean wantAttack;
		boolean wantAll;
		
		public MovesIterator reset(boolean wantLegal, boolean wantAttack)
		{
			count = 0;
			this.wantLegal = wantLegal;
			this.wantAttack = wantAttack;
			this.wantAll = false;
			return this;
		}
		
		public MovesIterator reset()
		{
			count = 0;
			this.wantLegal = false;
			this.wantAttack = false;
			this.wantAll = true;
			return this;
		}
		
		public boolean hasNext()
		{
			int i;
			boolean valid;
			
			for (i=count; i<moves.length; i++)
			{
				// end of moves list?
				if (moves[i] == null)
					return false;

				if (wantAll)
					return true;
					
				if (wantLegal && moves[i].isLegal())
					return true;

				if (wantAttack && moves[i].isAttack())
					return true;
			}
	
			return false;				
		}

		public Object next()
		{
			int i;
			boolean valid;
			Move move;
			
			for (i=count; i<moves.length; i++)
			{
				move = moves[i];

				if (move == null)
					return null;
				
				if (wantAll)
				{
					count = i+1;
					return move;
				}
					
				if (wantLegal && moves[i].isLegal())
				{
					count = i+1;
					return move;
				}

				if (wantAttack && moves[i].isAttack())
				{
					count = i+1;
					return move;
				}
			}
	
			return null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException("remove not supported in Board.PieceIterator");
		}
	}
	protected Piece(Board board, int color, int file, int rank, int type)
	{
		this.board = board;
		this.color = color;
		this.file = file;
		this.rank = rank;
		this.type = type;
	}
	//
	// For a given move, determine pinned/immobile/discovered check/defends attributes
	protected static boolean addMove(Piece piece, int count, 
	                                 Move move, boolean isAttackMove,
	                                 CheckingPieces checkingPieces)
		throws IllegalMoveException, PoolFullException
	{
		// make sure move is on the board
		if (move.moveToFile() <= 0              ||
		    move.moveToFile() > Board.NUM_FILES ||
		    move.moveToRank() <= 0              ||
		    move.moveToRank() > Board.NUM_RANKS)
			return false;
			
		Board board = piece.getBoard();
		Piece attackedPiece = move.getAttackedPiece();
		Piece pinningPiece = null;
		Piece checkingPieceAfterMove = null;

		// if the king is in check from 2 pieces, there's no need to check for pin
		if (checkingPieces.checkingPiece2 == null)
		{
			checkingPieceAfterMove = board.placesKingInCheck(move, checkingPieces.checkingPiece1);

			// was king in check before move?	
			if (checkingPieces.checkingPiece1 != null)
			{
				if (checkingPieceAfterMove != null)
				{
					// if a move uncovers a different checking piece, it's a pin
					if (!checkingPieces.checkingPiece1.equals(checkingPieceAfterMove))
						pinningPiece = checkingPieceAfterMove;

					// but it's not immobile if it attacks the original checking piece
					if (attackedPiece != null && attackedPiece.equals(checkingPieces.checkingPiece1))
						checkingPieceAfterMove = null;
				}
			}
			else
			if (checkingPieceAfterMove != null) {
				pinningPiece = checkingPieceAfterMove;
				checkingPieceAfterMove = null;
			}
		}
		else
		{
			// king in check from two different pieces; make sure the checking piece
			// is one we're not attacking
			if (attackedPiece != null && attackedPiece.equals(checkingPieces.checkingPiece1))
				checkingPieceAfterMove = checkingPieces.checkingPiece2;
			else
				checkingPieceAfterMove = checkingPieces.checkingPiece1;
		}


		move.setFlags(isAttackMove?0:Move.NON_ATTACK, pinningPiece, checkingPieceAfterMove);

		// discovered check logic
		// is it this piece's color's turn to move?
		if ((piece.getColor() == board.getColorToMove()))
		{
			Piece discoveredCheckPiece = board.placesOpposingKingInCheck(move, piece);

			// if this move places the king in check, but it's not this piece checking...
			if (discoveredCheckPiece != null && !discoveredCheckPiece.equals(piece))
				move.setFlag(Move.DISCOVERED_CHECK);
		}

		// are we attacking a piece on our side?  then we're defending
		if (attackedPiece != null && attackedPiece.getColor() == piece.getColor())
			move.setFlag(Move.ATTACK_ILLEGAL);
			
		piece.moves[count] = move;
		return true;
	}
	public static Piece createPiece(Board board, int type)
	{
		return createPiece(board, type, Piece.WHITE, 0, 0);
	}
	public static Piece createPiece(Board board, int type, int color, int file, int rank)
	{
		Piece piece = null;
		
		switch (type)
		{
			case PAWN:
				piece = new Pawn(board, color, file, rank);
				break;

			case ROOK:
				piece = new Rook(board, color, file, rank);
				break;

			case KNIGHT:
				piece = new Knight(board, color, file, rank);
				break;

			case BISHOP:
				piece = new Bishop(board, color, file, rank);
				break;

			case QUEEN:
				piece = new Queen(board, color, file, rank);
				break;

			case KING:
				piece = new King(board, color, file, rank);
				break;
		}

		return piece;
	}
	public boolean equals(Object obj)
	{
		// need a stronger equals?
		if (obj == null || !(obj instanceof Piece))
			return false;

		Piece that = (Piece) obj;
		if (that.type != this.type ||
			that.file != this.file ||
			that.rank != this.rank)
			return false;

		return true;
	}
	public void generateAllMoves()
		throws PoolFullException, IllegalMoveException
	{
		releaseMoves();
		generateMoves();
	}
	protected abstract void generateMoves()
		throws PoolFullException, IllegalMoveException;
public Iterator getAllMoves()
{
	return movesIterator.reset();
}
public Iterator getAttackMoves()
{
	return movesIterator.reset(false, true);
}
	public Board getBoard()
	{
		return board;
	}
	public int getColor()
	{
		return color;
	}
	public int getFile()
	{
		return file;
	}
public Iterator getLegalMoves()
{
	return movesIterator.reset(true, false);
}
	public char getLetter()
	{
		char letter = pieceLetter[type];
		if (color == Piece.WHITE)
			letter = Character.toLowerCase(letter);

		return letter;
	}
	public String getName()
	{
		return pieceNames[type];
	}
	public int getPoolID() {
		return poolID;
	}
	public int getRank()
	{
		return rank;
	}
	public int getType()
	{
		return type;
	}
	public boolean hasBeenMoved()
	{
		return hasBeenMoved;
	}
	public void move(int file, int rank)
	{
		this.file = file;
		this.rank = rank;
	}
	public void move(Move move)
	{
		this.file = move.moveToFile();
		this.rank = move.moveToRank();
	}
	public void releaseMoves()
	{
		Move move;
		for (int i=0; i<moves.length; i++)
		{
			move = moves[i];
			if (move == null)
				break;
				
			board.getMovePool().release(move);
			moves[i] = null;
		}
	}
	public void resetPiece(int color, int file, int rank) {
		this.color = color;
		this.file = file;
		this.rank = rank;

		this.hasBeenMoved = false;
	}
	public void setHasBeenMoved()
	{
		hasBeenMoved = true;
	}
	public void setPoolID(int poolID) {
		this.poolID = poolID;
	}
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(this.getLetter());
		buf.append(" at ");
		buf.append(Board.getFileChar(file));
		buf.append(rank);
		return buf.toString();
	}
}
