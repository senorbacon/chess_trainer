package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Pawn extends Piece {
	public final static int MAX_PAWN_MOVES = 4;
/**
 * Pawn constructor comment.
 * @param color int
 * @param file int
 * @param rank int
 */
protected Pawn(Board board, int color, int file, int rank) {
	super(board, color, file, rank, PAWN);

	moves = new Move[MAX_PAWN_MOVES];
}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		Pawn.generateMoves(this);
	}
	protected static void generateMoves(Pawn pawn)
		throws PoolFullException, IllegalMoveException
	{
		Board board = pawn.getBoard();
		MovePool movePool = board.getMovePool();
		Move move;
		Move epMove = null;
		int color = pawn.getColor();
		int file = pawn.getFile();
		int rank = pawn.getRank();
		int count = 0;
		
		int oppColor;
		int pawnDir;
		int promoteRank;
		int enPassantRank;
		
		Piece oppPiece;

		CheckingPieces checkingPieces = ((CheckingPieces) board.getCheckingPiecePool().obtain()).reset();
		board.kingInCheck(color, checkingPieces);
		
		if (color == WHITE)
		{
			oppColor = BLACK;
			pawnDir = 1;
			promoteRank = 7;
			enPassantRank = 5;
		} else {
			oppColor = WHITE;
			pawnDir = -1;
			promoteRank = 2;
			enPassantRank = 4;
		}

		// check en passant
		if (rank == enPassantRank)
		{
			Move lastMove = board.getLastMove();
			if (lastMove.getSpecialMove() == Move.DOUBLE_PAWN_MOVE &&
				(lastMove.moveToFile() == file+1 ||
				 lastMove.moveToFile() == file-1)
			   )
			{
				move = movePool.obtainMove(pawn, lastMove.moveToFile(), rank+pawnDir, lastMove.getPiece());
				move.setSpecialMove(Move.EN_PASSANT);
				
				addMove(pawn, count++, move, true, checkingPieces);
				epMove = move;
			}
		}

		//
		// Check "normal" moves, checking to see if they're also promotion moves
		// If so, we need to generate a move for each promotion possibility
		//

		// pawn push, one space
		if (board.getPiece(file, rank+pawnDir) == null)
		{
			move = movePool.obtainMove(pawn, file, rank+pawnDir, null);

			// legal but not an attack move
			addMove(pawn, count, move, false, checkingPieces);

			if (rank == promoteRank)
				move.setSpecialMove(Move.PAWN_PROMOTED);
				
			count++;			
		}

		// attack to the right, assuming there isn't an e.p. move there
		if (epMove == null || epMove.moveToFile() == file-1)
		{
			oppPiece = board.getPiece(file+1, rank+pawnDir);
			move = movePool.obtainMove(pawn, file+1, rank+pawnDir, oppPiece);
			if (addMove(pawn, count, move, true, checkingPieces))
			{
				if (oppPiece != null && (oppPiece.getColor() == oppColor))
				{
					// if this is a legal move, check for pawn promotion
					if (rank == promoteRank)
						move.setSpecialMove(Move.PAWN_PROMOTED);
				}

				// special case for pawns
				if (oppPiece == null)
					move.setFlag(Move.ATTACK_ILLEGAL);
					
				count++;
			}
			else
				movePool.release(move);
		}
		
		// attack to the left, assuming there isn't an e.p. move there
		if (epMove == null || epMove.moveToFile() == file+1)
		{
			oppPiece = board.getPiece(file-1, rank+pawnDir);
			move = movePool.obtainMove(pawn, file-1, rank+pawnDir, oppPiece);
			if (addMove(pawn, count, move, true, checkingPieces))
			{
				if (oppPiece != null && (oppPiece.getColor() == oppColor))
				{
					// if this is a legal move, check for pawn promotion
					if (rank == promoteRank)
						move.setSpecialMove(Move.PAWN_PROMOTED);
				}

				// special case for pawns
				if (oppPiece == null)
					move.setFlag(Move.ATTACK_ILLEGAL);
					
				count++;
			}
			else
				movePool.release(move);
		}
		
		// check for initial double move
		if (!pawn.hasBeenMoved)
		{
			if (board.getPiece(file, rank+pawnDir) == null &&
				board.getPiece(file, rank+pawnDir*2) == null)
			{
				move = movePool.obtainMove(pawn, file, rank+pawnDir*2, null);
				move.setSpecialMove(Move.DOUBLE_PAWN_MOVE);

				// legal but not an attack move
				addMove(pawn, count, move, false, checkingPieces);
			}
		}

		board.getCheckingPiecePool().release(checkingPieces);		
	}
}
