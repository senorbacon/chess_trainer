package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Queen extends Piece {
	public final static int MAX_QUEEN_MOVES = 27;

/**
 * Queen constructor comment.
 * @param board chess.Board
 * @param color int
 * @param file int
 * @param rank int
 */
protected Queen(Board board, int color, int file, int rank) {
	super(board, color, file, rank, QUEEN);

	moves = new Move[MAX_QUEEN_MOVES];
}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		Bishop.generateMoves(this, Rook.generateMoves(this));
	}
}
