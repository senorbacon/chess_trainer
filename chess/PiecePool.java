package chess;

import java.util.*;
/**
 * Insert the type's description here.
 */
public class PiecePool {
	final Board board;
	
	Pool pawnPool;
	Pool rookPool;
	Pool knightPool;
	Pool bishopPool;
	Pool queenPool;
	Pool kingPool;

	public PiecePool(Board bored, int stackDepth)
	{
		this.board = bored;
		
		pawnPool = new Pool("pawnPool", stackDepth*2*8, 
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.PAWN);
				}
			}			
		);
		
		rookPool = new Pool("rookPool", stackDepth*2*3,
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.ROOK);
				}
			}			
		);
		
		knightPool = new Pool("knightPool", stackDepth*2*3,
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.KNIGHT);
				}
			}			
		);
		
		bishopPool = new Pool("bishopPool", stackDepth*2*3,
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.BISHOP);
				}
			}			
		);
		
		queenPool = new Pool("queenPool", stackDepth*2*3,
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.QUEEN);
				}
			}			
		);
		
		kingPool = new Pool("kingPool", stackDepth*2*1,
			new PoolFactoryMethod() {
				public Poolable createPoolObject() {
					return Piece.createPiece(board, Piece.KING);
				}
			}			
		);
	}
	public Piece obtainPiece(int type)
		throws PoolFullException
	{
		switch (type)
		{
			case Piece.PAWN:
				return (Piece) pawnPool.obtain();
				
			case Piece.ROOK:
				return (Piece) rookPool.obtain();
				
			case Piece.KNIGHT:
				return (Piece) knightPool.obtain();
				
			case Piece.BISHOP:
				return (Piece) bishopPool.obtain();
				
			case Piece.QUEEN:
				return (Piece) queenPool.obtain();
				
			case Piece.KING:
				return (Piece) kingPool.obtain();

			default:
				return null;
		}
	}
	public Piece obtainPiece(int type, int color, int file, int rank)
		throws PoolFullException
	{
		Piece piece = obtainPiece(type);
		if (piece != null)
		{
			piece.resetPiece(color, file, rank);
		}
		return piece;
	}
	public void releasePiece(Piece piece)
	{
		piece.releaseMoves();
		
		switch (piece.getType())
		{
			case Piece.PAWN:
				pawnPool.release(piece);
				break;

			case Piece.ROOK:
				rookPool.release(piece);
				break;

			case Piece.KNIGHT:
				knightPool.release(piece);
				break;

			case Piece.BISHOP:
				bishopPool.release(piece);
				break;

			case Piece.QUEEN:
				queenPool.release(piece);
				break;

			case Piece.KING:
				kingPool.release(piece);
				break;

			default:
				Debug.debugMsg(this, Debug.ERROR, "Piece type " + piece.getType() + " not defined.");
		}
	}
}
