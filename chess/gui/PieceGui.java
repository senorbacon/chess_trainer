package chess.gui;

import javax.swing.*;
import java.awt.*;

import chess.*;

/**
 * Insert the type's description here.
 * Creation date: (5/14/02 8:03:09 AM)
 * @author: 
 */
public class PieceGui extends JLabel {
	public final static int B_PAWN   = 1;
	public final static int B_ROOK   = 2;
	public final static int B_KNIGHT = 3;
	public final static int B_BISHOP = 4;
	public final static int B_QUEEN  = 5;
	public final static int B_KING   = 6;
	public final static int W_PAWN   = 7;
	public final static int W_ROOK   = 8;
	public final static int W_KNIGHT = 9;
	public final static int W_BISHOP = 10;
	public final static int W_QUEEN  = 11;
	public final static int W_KING   = 12;
	
	protected static ImageIcon imageArray[] = { null,
	                                            new ImageIcon("merida35/bp35.gif"),
	                                            new ImageIcon("merida35/br35.gif"),
	                                            new ImageIcon("merida35/bn35.gif"),
	                                            new ImageIcon("merida35/bb35.gif"),
	                                            new ImageIcon("merida35/bq35.gif"),
	                                            new ImageIcon("merida35/bk35.gif"),
	                                            new ImageIcon("merida35/wp35.gif"),
	                                            new ImageIcon("merida35/wr35.gif"),
	                                            new ImageIcon("merida35/wn35.gif"),
	                                            new ImageIcon("merida35/wb35.gif"),
	                                            new ImageIcon("merida35/wq35.gif"),
	                                            new ImageIcon("merida35/wk35.gif")
	                                          };
	protected GuiContext guiContext;
	protected Square square;

	// board squares
	int savedPieceType = 0;
	int savedPieceColor = 0;

	// bank squares
	int pieceType;
	int pieceColor;

/**
 * PieceGui constructor comment.
 */
public PieceGui(int pieceType, int pieceColor, GuiContext context) {
	this.guiContext = context;
	this.square = null;

	this.pieceType = pieceType;
	this.pieceColor = pieceColor;
	
	updateSquare();	
}
/**
 * PieceGui constructor comment.
 */
public PieceGui(GuiContext context, Square square) {
	this.guiContext = context;
	this.square = square;
	updateSquare();	
}
	public static Image getPieceImage(int pieceType, int pieceColor)
	{
		int tempType = pieceType + ((pieceColor==Piece.WHITE)?6:0);
		return imageArray[tempType].getImage();
	}
	protected void setPieceIcon(int pieceType, int pieceColor)
	{
		int tempType = pieceType + ((pieceColor==Piece.WHITE)?6:0);
		setIcon(imageArray[tempType]);
	}
	public void updateSquare()
	{
		if (square != null)
		{
			Piece piece = square.getPiece();

			if (piece == null)
			{
				if (savedPieceType != 0)
				{
					savedPieceType = 0;
					savedPieceColor = 0;
					setIcon(null);
				}
			}
			else
			{
				if (piece.getType() != savedPieceType || piece.getColor() != savedPieceColor)
				{
					savedPieceType = piece.getType();
					savedPieceColor = piece.getColor();
					setPieceIcon(savedPieceType, savedPieceColor);
				}
			}
		}
		else
			setPieceIcon(pieceType, pieceColor);
	}
}
