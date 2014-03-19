package chess;

import java.util.*;
/**
 * Insert the type's description here.
 */
public class MovePool extends Pool {

/**
 * MovePool constructor comment.
 * @param name java.lang.String
 * @param size int
 * @param createMethod chess.PoolFactoryMethod
 */
public MovePool(String name, int size) {
	super(name, size, 
		new PoolFactoryMethod() {
			public Poolable createPoolObject() {
				return new Move(null, 0, 0, null);
			}
		}
	);
}
	public Move cloneMove(Move source)
		throws PoolFullException
	{
		Move move = (Move) obtain();
		if (move != null)
		{
			move.copyMove(source);
		}
		return move;
	}
	public Move obtainMove(Piece piece, int file, int rank, Piece captured)
		throws PoolFullException
	{
		Move move = (Move) obtain();
		if (move != null)
		{
			move.resetMove(piece, file, rank, captured);
		}
		return move;
	}
}
