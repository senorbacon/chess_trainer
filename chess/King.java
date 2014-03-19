package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class King extends Piece {
	public final static int MAX_KING_MOVES = 8;


/**
 * King constructor comment.
 * @param color int
 * @param file int
 * @param rank int
 */
protected King(Board board, int color, int file, int rank) {
	super(board, color, file, rank, KING);

	moves = new Move[MAX_KING_MOVES];
}
	//
	// For a given move, determine pinned/immobile/discovered check/defends attributes
	protected static boolean addKingMove(King king, int count, Move move, boolean attackMove)
		throws IllegalMoveException, PoolFullException
	{
		// make sure move is on the board
		if (move.moveToFile() <= 0              ||
		    move.moveToFile() > Board.NUM_FILES ||
		    move.moveToRank() <= 0              ||
		    move.moveToRank() > Board.NUM_RANKS)
			return false;
			
		Board board = king.getBoard();
		Piece attackedPiece = move.getAttackedPiece();
		Piece checkingPieceAfterMove = board.placesKingInCheck(move, (Piece) null);

		boolean thisColorsTurn = (king.getColor() == board.getColorToMove());
		Piece discoveredCheckPiece = null;
		
		if (thisColorsTurn)
			discoveredCheckPiece = board.placesOpposingKingInCheck(move, (Piece) null);
		
		move.setFlags(attackMove?0:Move.NON_ATTACK, null, checkingPieceAfterMove);

		if (attackedPiece != null && attackedPiece.getColor() == king.getColor())
			move.setFlag(Move.ATTACK_ILLEGAL);
			
		if (discoveredCheckPiece != null)
			move.setFlag(Move.DISCOVERED_CHECK);
		
		king.moves[count] = move;
		return true;
	}
	protected void generateMoves()
		throws PoolFullException, IllegalMoveException
	{
		generateMoves(this);
	}
	protected static void generateMoves(King king)
		throws PoolFullException, IllegalMoveException
	{
		Board board = king.getBoard();
		MovePool movePool = board.getMovePool();
		Move move;
		int color = king.getColor();
		int toFile, file = king.getFile();
		int toRank, rank = king.getRank();
		int count = 0;
		int i;

		int castlingRank = (color == WHITE)?1:8;
		
		int oppColor = (color == WHITE)?BLACK:WHITE;
		Piece oppPiece;

		CheckingPieces checkingPieces = ((CheckingPieces) board.getCheckingPiecePool().obtain()).reset();
		board.kingInCheck(color, checkingPieces);

		// cycle through the eight moves
		for (i=0; i<Piece.KING_MOVE_TABLE_SIZE; i++)
		{
			toFile = file + Piece.KingMoveTable_file[i];
			toRank = rank + Piece.KingMoveTable_rank[i];

			move = movePool.obtainMove(king, toFile, toRank, board.getPiece(toFile, toRank));
			if (addKingMove(king, count, move, true))
				count++;
			else
				movePool.release(move);
		}

		// castling
		if (checkingPieces.checkingPiece1 == null && !king.hasBeenMoved())
		{
			// kingside-castle
			Piece rook = board.getPiece(8, castlingRank);
			if (rook != null && !rook.hasBeenMoved() &&  
			    board.getPiece(6, castlingRank) == null  &&  
			    board.getPiece(7, castlingRank) == null )
			{
				checkingPieces.reset();				
				move = movePool.obtainMove(king, 6, castlingRank, null);
				board.placesKingInCheck(move, checkingPieces);
				movePool.release(move);

				// make sure spaces king moves to are not attacked
				if (checkingPieces.checkingPiece1 == null)
				{
					checkingPieces.reset();				
					move = movePool.obtainMove(king, 7, castlingRank, null);
					board.placesKingInCheck(move, checkingPieces);
					
					if (checkingPieces.checkingPiece1 == null)
					{
						move.setSpecialMove(Move.KINGSIDE_CASTLE);
						move.setFlag(Move.NON_ATTACK);
						king.moves[count++] = move;
					}
					else
						movePool.release(move);
				}
			}

			// queenside-castle
			rook = board.getPiece(1, castlingRank);
			if (rook != null && !rook.hasBeenMoved() && 
			    board.getPiece(4, castlingRank) == null && 
			    board.getPiece(3, castlingRank) == null && 
			    board.getPiece(2, castlingRank) == null)
			{
				checkingPieces.reset();
				move = movePool.obtainMove(king, 4, castlingRank, null);
				board.placesKingInCheck(move, checkingPieces);
				movePool.release(move);

				// make sure spaces king moves to are not attacked
				if (checkingPieces.checkingPiece1 == null)
				{
					checkingPieces.reset();
					move = movePool.obtainMove(king, 3, castlingRank, null);
					board.placesKingInCheck(move, checkingPieces);
					
					if (checkingPieces.checkingPiece1 == null)
					{
						move.setSpecialMove(Move.QUEENSIDE_CASTLE);
						move.setFlag(Move.NON_ATTACK);
						king.moves[count++] = move;
					}
					else
						movePool.release(move);
				}
			}
		}
		
		board.getCheckingPiecePool().release(checkingPieces);
	}
}
