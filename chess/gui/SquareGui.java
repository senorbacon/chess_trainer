package chess.gui;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

import chess.*;

/**
 *
 */
public class SquareGui extends JPanel {
	protected static Color whiteColor = new Color(190, 190, 180);
	protected static Color blackColor = new Color(128, 128, 110);
	protected static Color lightBlueColor = new Color(96, 96, 240);
	protected static Color lightGreenColor = new Color(128, 255, 128);
	protected static Color redColor = new Color(200, 60, 60);
	
	protected static Border lightBlueBorder = BorderFactory.createLineBorder(lightBlueColor, 2);
	protected static Border lightGreenBorder = BorderFactory.createLineBorder(lightGreenColor, 2);
	protected static Border redBorder = BorderFactory.createLineBorder(redColor, 2);
	protected static Border whiteBorder = BorderFactory.createLineBorder(whiteColor, 2);
	protected static Border blackBorder = BorderFactory.createLineBorder(blackColor, 2);

	protected final boolean bankSquare;	
	protected final int color;
	protected int size;

	// bank squares
	protected int pieceType;
	protected int pieceColor;

	// board squares
	protected int file;
	protected int rank;

	protected Border backgroundBorder;

	protected GuiContext guiContext;
	protected Square square;
	protected PieceGui pieceGui;

	protected SquareInputBox whiteInput, blackInput;

	protected Move highlightedMove;
	protected String highlightedMoveInfo;

	protected boolean isDragging = false;

	public SquareGui(int pieceType, int color, GuiContext context)
	{
		this.guiContext = context;
		this.square = null;
		this.pieceType = pieceType;
		this.pieceColor = color;
		this.bankSquare = true;
		this.color = 0;
				
		pieceGui = new PieceGui(pieceType, color, guiContext);
		add(pieceGui);

		setBackground(whiteColor);
		setBorder(whiteBorder);

		addMouseListener(guiContext.getMouseHandler());
		addMouseMotionListener(guiContext.getMouseHandler());
	}
	public SquareGui(GuiContext context, int file, int rank)
	{
		this.guiContext = context;
		this.bankSquare = false;
		this.square = context.getBoard().getSquare(file, rank);
		this.file = file;
		this.rank = rank;
		this.color = (file + rank + 1)%2;
		if (color==Piece.WHITE)
		{
			setBackground(whiteColor);
			backgroundBorder = whiteBorder;
		} else
		{
			setBackground(blackColor);
			backgroundBorder = blackBorder;
		}
		setBorder(backgroundBorder);

		addMouseListener(guiContext.getMouseHandler());
		addMouseMotionListener(guiContext.getMouseHandler());
	}
	public void addChildren()
	{
		this.setLayout(null);
		
		pieceGui = new PieceGui(guiContext, square);
		pieceGui.setBounds(8, 6, 35, 35);
		add(pieceGui);

		blackInput = new SquareInputBox(this, getBackground());
		blackInput.setBounds(0, 0, 9, 11);
		add(blackInput);

		whiteInput = new SquareInputBox(this, getBackground());
		whiteInput.setBounds(0, getBounds().height-11, 9, 11);
		add(whiteInput);
		updateSquare();
	}
/**
 * Insert the method's description here.
 * Creation date: (7/7/02 7:14:51 PM)
 * @return chess.Move
 */
public chess.Move getHighlightedMove() {
	return highlightedMove;
}
/**
 * Insert the method's description here.
 * Creation date: (7/9/02 10:07:44 AM)
 * @return java.lang.String
 */
public java.lang.String getHighlightedMoveInfo() {
	return highlightedMoveInfo;
}
	public SquareInputBox getInputBox(int color)
	{
		if (color == Piece.WHITE)
			return whiteInput;
		else if (color == Piece.BLACK)
			return blackInput;
		return null;
	}
	public int getPieceColor()
	{
		return pieceColor;
	}
	public PieceGui getPieceGui()
	{
		return pieceGui;
	}
	public int getPieceType()
	{
		return pieceType;
	}
	public Square getSquare() {
		return square;
	}
	public boolean isFocusTraversable()
	{
		return false;
	}
	public void setBorder(int borderType)
	{
		switch (borderType)
		{
			case GuiContext.BORDER_SELECTED:
				setBorder(lightGreenBorder);
				break;
				
			case GuiContext.BORDER_LEGAL:
				setBorder(lightBlueBorder);
				break;
				
			case GuiContext.BORDER_ILLEGAL:
				setBorder(redBorder);
				break;
				
			default:
				setBorder(backgroundBorder);
				break;
		}				
	}
	public void setDragging(boolean dragging)
	{
		if (dragging != isDragging)
		{
			isDragging = dragging;
			pieceGui.setVisible(!dragging);
		}
	}
/**
 * Insert the method's description here.
 * Creation date: (7/7/02 7:14:51 PM)
 * @param newHighlightedMove chess.Move
 */
public void setHighlightedMove(chess.Move newHighlightedMove) {
	highlightedMove = newHighlightedMove;
	highlightedMoveInfo = null;
}
/**
 * Insert the method's description here.
 * Creation date: (7/9/02 10:07:44 AM)
 * @param newHighlightedMoveInfo java.lang.String
 */
public void setHighlightedMoveInfo(java.lang.String newHighlightedMoveInfo) {
	highlightedMoveInfo = newHighlightedMoveInfo;
}
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(Board.getFileChar(file));
		buf.append(rank);
		return buf.toString();
	}
	public void updateSquare()
	{
		pieceGui.updateSquare();

		whiteInput.setValue(square.getAttackedByWhite());
		blackInput.setValue(square.getAttackedByBlack());
	}
}
