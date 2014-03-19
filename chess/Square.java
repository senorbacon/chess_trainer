package chess;

import java.util.*;
/**
 * Insert the type's description here.
 * Creation date: (5/28/02 8:41:41 AM)
 * @author: 
 */
public class Square {

	int attackedByWhite;
	int attackedByBlack;

	int file;
	int rank;

	Piece piece;

	int moveCount;
	Move moves[] = new Move[Board.MAX_ATTACKING_MOVES];

	public Square(int file, int rank)
	{
		this.file = file;
		this.rank = rank;
	}
	public int getAttackedByBlack()
	{
		return attackedByBlack;
	}
	public int getAttackedByWhite()
	{
		return attackedByWhite;
	}
	public int getFile() { return file; }
	public Piece getPiece()
	{
		return piece;
	}
	public int getRank() { return rank; }
	public Move[] getSquareMoves()
	{
		return moves;
	}
	public String toString()
	{
		if (piece == null)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(Board.getFileChar(file));
			buf.append(rank);
			return buf.toString();
		}
		else
			return piece.toString();
	}
}
