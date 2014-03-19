package chess;

/**
 * if checkingPiece2 != null, checkingPiece != null && !checkingPiece.equals(checkingPiece2)
 */
public class CheckingPieces implements Poolable {
	Piece checkingPiece1;
	Piece checkingPiece2;

	int poolId;

/**
 * getPoolID method comment.
 */
public int getPoolID() {
	return poolId;
}
	public CheckingPieces reset()
	{
		checkingPiece1 = null;
		checkingPiece2 = null;
		return this;
	}
/**
 * setPoolID method comment.
 */
public void setPoolID(int poolID) 
{
	this.poolId = poolID;
}
}
