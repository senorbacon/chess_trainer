package chess.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import chess.*;
/**
 *
 */
public class BoardCanvas extends JComponent {
	GuiContext guiContext;
	Board board;
	
	SquareGui squares[][] = new SquareGui[8][8];

	protected static final int CANVAS_SIZE_Y = 360;
	protected final static double ASPECT_RATIO = 1.03;

	protected final static Border border = LineBorder.createBlackLineBorder();

	public BoardCanvas(GuiContext context)
	{
		this.guiContext = context;

		JPanel center = new JPanel();
		center.setLayout(null);
		
		int size_y = CANVAS_SIZE_Y;
		int size_x = (int) ((float) size_y * ASPECT_RATIO);
//		this.setPreferredSize(new Dimension(size_x, size_y));
		this.setSize(new Dimension(size_x, size_y+20));
		center.setSize(new Dimension(size_x, size_y+20));

		int square_x = size_x/8;
		int square_y = size_y/8;

		SquareGui square;
		for (int rank=1; rank<=8; rank++)
		{
			for (int file=1; file<=8; file++)
			{
				square = new SquareGui(context, file, rank);
				squares[rank-1][file-1] = square;

				square.setBounds(square_x * (file-1) + 5, 
				                 square_y * (8-rank) + 5, 
				                 square_x, square_y);
				square.addChildren();
				center.add(square);
			}
		}

		add(center, BorderLayout.CENTER);

		setupFocusManagement();
	}
/**
 */
public SquareGui getSquareGui(int file, int rank) {
	if (file < 1 || file > Board.NUM_FILES ||
		rank < 1 || rank > Board.NUM_RANKS)
		return null;
		
	return squares[rank-1][file-1];
}
	public boolean isFocusCycleRoot()
	{
		return true;
	}
	public boolean isFocusTraversable()
	{
		return false;
	}
	public boolean isManagingFocus()
	{
		return true;
	}
	public void setupFocusManagement()
	{
/*
		registerKeyboardAction(guiContext.keyAction, "UP",
		                       KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), 
		                       JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(guiContext.keyAction, "DOWN", 
		                       KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), 
		                       JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(guiContext.keyAction, "LEFT", 
		                       KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), 
		                       JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(guiContext.keyAction, "RIGHT", 
		                       KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), 
		                       JComponent.WHEN_IN_FOCUSED_WINDOW);
*/

//		currentlySelectedBox = 

		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e)
			{
				System.out.println("key pressed: " + e.getKeyCode());
			}
			public void keyReleased(KeyEvent e)
			{
			}
			public void keyTyped(KeyEvent e)
			{
			}
		});
	}
	public void updateBoard()
	{
		for (int j=0; j<Board.NUM_RANKS; j++) {
			for (int i=0; i<Board.NUM_FILES; i++)
			{
				squares[j][i].updateSquare();
			}
		}
	}
}
