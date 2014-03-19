package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Bishop extends Piece {
	public final static int MAX_BISHOP_MOVES = 13;


/**
 * Bishop constructor comment.
 * @param board chess.Board
 * @param color int
 * @param file int
 * @param rank int
 */
protected Bishop(Board board, int color, int file, int rank) {
	super(board, color, file, rank, BISHOP);

	moves = new Move[MAX_BISHOP_MOVES];
}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		generateMoves(this, 0);
	}
	/**
	 * We'll pass in a Piece here because queen.generateMove() calls this as well
	 */
	protected static void generateMoves(Piece bishop, int count)
		throws PoolFullException, IllegalMoveException
	{
		Board board = bishop.getBoard();
		MovePool movePool = board.getMovePool();
		Move move;
		int color = bishop.getColor();
		int fileDir, toFile, file = bishop.getFile();
		int rankDir, toRank, rank = bishop.getRank();
		int i, j;
		boolean xray = false;
		boolean singleXray = false;
		boolean skewer = false;
		
		int oppColor = (color == WHITE)?BLACK:WHITE;
		int oppType;
		Piece oppPiece;
		Piece pinningPiece, king;

		CheckingPieces checkingPieces = ((CheckingPieces) board.getCheckingPiecePool().obtain()).reset();
		board.kingInCheck(color, checkingPieces);

		// cycle through the different directions
		for (i=0; i<Piece.BISHOP_DIR_TABLE_SIZE; i++)
		{
			fileDir = BishopDirTable_file[i];
			rankDir = BishopDirTable_rank[i];
			xray = false;
			singleXray = false;
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
				
				move = movePool.obtainMove(bishop, toFile, toRank, oppPiece);
				if (addMove(bishop, count, move, true, checkingPieces))
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
							king = board.getKing(bishop.getColor());
							if (board.pieceBetween(pinningPiece, file, rank, king.getFile(), king.getRank()))
								move.setFlags(move.getFlags(), null, move.isImmobile());
						}
					}						

					count++;
				}
				else
					movePool.release(move);

				// if we've already skewered a piece, or xray-ed through a pawn,
				// end processing in this direction
				if (singleXray || skewer)
					break;
						
				// if the piece on the given space is a rook, queen, or same-color pawn,
				// turn the xray flag on.  If there's a space and it doesn't allow
				// xray moves, break out
				if (oppPiece != null)
				{
					oppType = oppPiece.getType();
					if (oppType == Piece.BISHOP ||
					    oppType == Piece.QUEEN)
					{
						xray = true;
					}
					else
					if (oppType == Piece.PAWN)
					{
						// we can only xray through white pawns if we're "below" then
						// and through black pawns if we're "above" them
						boolean pawnWhite = (oppPiece.getColor()==WHITE)?true:false;
						if (( pawnWhite && rank < toRank) || 
						    (!pawnWhite && rank > toRank) )
						{
							xray = true;
							singleXray = true;
						}

						// if we can't xray, end processing for this direction
						if (!xray)
							break;
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
	}
}
