package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Rook extends Piece {
	public final static int MAX_ROOK_MOVES = 14;


/**
 * Rook constructor comment.
 * @param board chess.Board
 * @param color int
 * @param file int
 * @param rank int
 */
protected Rook(Board board, int color, int file, int rank) {
	super(board, color, file, rank, ROOK);

	moves = new Move[MAX_ROOK_MOVES];
}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		generateMoves(this);
	}
	/**
	 * We'll pass in a Piece here because queen.generateMove() calls this as well
	 */
	protected static int generateMoves(Piece rook)
		throws PoolFullException, IllegalMoveException
	{
		Board board = rook.getBoard();
		MovePool movePool = board.getMovePool();
		Move move;
		int color = rook.getColor();
		int fileDir, toFile, file = rook.getFile();
		int rankDir, toRank, rank = rook.getRank();
		int count = 0;
		int i, j;
		boolean xray = false;
		boolean skewer = false;
		
		int oppColor = (color == WHITE)?BLACK:WHITE;
		int oppType;
		Piece oppPiece;
		Piece pinningPiece, king;

		CheckingPieces checkingPieces = ((CheckingPieces) board.getCheckingPiecePool().obtain()).reset();
		board.kingInCheck(color, checkingPieces);

		// cycle through the different directions
		for (i=0; i<Piece.ROOK_DIR_TABLE_SIZE; i++)
		{
			fileDir = RookDirTable_file[i];
			rankDir = RookDirTable_rank[i];
			xray = false;
			skewer = false;
			
			for (j=1; j<Board.NUM_FILES; j++)
			{
				toFile = file + j*fileDir;
				if (toFile < 1 || toFile > Board.NUM_FILES)
					break;

				toRank = rank + j*rankDir;
				if (toRank < 1 || toRank > Board.NUM_RANKS)
					break;

				oppPiece = board.getPiece(toFile, toRank);
				
				move = movePool.obtainMove(rook, toFile, toRank, oppPiece);
				if (addMove(rook, count, move, true, checkingPieces))
				{
					if (skewer)
						move.setFlag(Move.SKEWER);
					else
					if (xray) 
					{
						move.setFlag(Move.XRAY);

						// if there's a pinning piece that's in the xray line,
						// we need to remove it... but it might not be in the xray line.
						pinningPiece = move.isPinned();
						if (pinningPiece != null)
						{
							king = board.getKing(rook.getColor());
							if (board.pieceBetween(pinningPiece, toFile, toRank, king.getFile(), king.getRank()))
								move.setFlags(move.getFlags(), null, move.isImmobile());
						}
					}

					count++;
				}
				else
					movePool.release(move);

				// if the piece on the given space is a rook, queen, or opposing king,
				// turn the xray flag on.  If there's a space and it doesn't allow
				// xray moves, break out
				if (oppPiece != null)
				{
					if (skewer)
						break;
						
					oppType = oppPiece.getType();
					if (oppType == Piece.ROOK  ||
					    oppType == Piece.QUEEN)
					{
						xray = true;
					}
					else
					if (!xray && oppType == Piece.KING &&
					    oppPiece.getColor() == oppColor)
					{
						skewer = true;
					}
					else
						break;
				}
			}
		}

		board.getCheckingPiecePool().release(checkingPieces);
		return count;
	}
}
