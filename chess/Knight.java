package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Knight extends Piece {
	public final static int MAX_KNIGHT_MOVES = 8;


/**
 * Knight constructor comment.
 * @param board chess.Board
 * @param color int
 * @param file int
 * @param rank int
 */
protected Knight(Board board, int color, int file, int rank) {
	super(board, color, file, rank, KNIGHT);

	moves = new Move[MAX_KNIGHT_MOVES];
}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		Knight.generateMoves(this);
	}
	protected static void generateMoves(Knight knight)
		throws PoolFullException, IllegalMoveException
	{
		Board board = knight.getBoard();
		MovePool movePool = board.getMovePool();
		Move move;
		int color = knight.getColor();
		int toFile, file = knight.getFile();
		int toRank, rank = knight.getRank();
		int count = 0;
		int i;
		
		int oppColor = (color == WHITE)?BLACK:WHITE;
		Piece oppPiece;

		CheckingPieces checkingPieces = ((CheckingPieces) board.getCheckingPiecePool().obtain()).reset();
		board.kingInCheck(color, checkingPieces);

		// cycle through the eight moves
		for (i=0; i<Piece.KNIGHT_MOVE_TABLE_SIZE; i++)
		{
			toFile = file + Piece.KnightMoveTable_file[i];
			toRank = rank + Piece.KnightMoveTable_rank[i];

			move = movePool.obtainMove(knight, toFile, toRank, board.getPiece(toFile, toRank));
			if (addMove(knight, count, move, true, checkingPieces))
				count++;
			else
				movePool.release(move);
		}
		
		board.getCheckingPiecePool().release(checkingPieces);
	}
}
