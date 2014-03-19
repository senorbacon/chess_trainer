package chess.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import chess.*;

/**
 */
public class PieceBankGui extends JPanel 
{
	//TODO: something else than this
	protected static int SQUARE_SIZE = 45;
	
	GuiContext guiContext;

	public PieceBankGui(GuiContext context)
	{
		this.guiContext = context;

		JPanel squares = new JPanel();
		squares.setLayout(null);
		squares.setPreferredSize(new Dimension(6*SQUARE_SIZE, 2*SQUARE_SIZE));

		SquareGui square;
		square = new SquareGui(Piece.KING, Piece.BLACK, context);
		square.setBounds(0, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.QUEEN, Piece.BLACK, context);
		square.setBounds(SQUARE_SIZE, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.ROOK, Piece.BLACK, context);
		square.setBounds(2*SQUARE_SIZE, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.BISHOP, Piece.BLACK, context);
		square.setBounds(3*SQUARE_SIZE, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.KNIGHT, Piece.BLACK, context);
		square.setBounds(4*SQUARE_SIZE, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.PAWN, Piece.BLACK, context);
		square.setBounds(5*SQUARE_SIZE, 0, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		
		square = new SquareGui(Piece.KING, Piece.WHITE, context);
		square.setBounds(0, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.QUEEN, Piece.WHITE, context);
		square.setBounds(SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.ROOK, Piece.WHITE, context);
		square.setBounds(2*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.BISHOP, Piece.WHITE, context);
		square.setBounds(3*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.KNIGHT, Piece.WHITE, context);
		square.setBounds(4*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);
		square = new SquareGui(Piece.PAWN, Piece.WHITE, context);
		square.setBounds(5*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
		squares.add(square);

		this.setSize(300, 120);
		this.add(squares, BorderLayout.CENTER);
		this.setBorder(new TitledBorder(new LineBorder(Color.black), "Piece bank"));
	}
}
