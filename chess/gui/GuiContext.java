package chess.gui;

import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import chess.*;

/**
 */
public class GuiContext { 

	// board state
	// states 1 & 2 are for drag and drop
	public final static int NOTHING_SELECTED = 0;
	public final static int SQUARE_PRESELECTED = 1;
	public final static int PIECE_DRAGGING = 2;
	public final static int SQUARE_SELECTED = 3;

	// border constants
	public final static int BORDER_EMPTY = 0;
	public final static int BORDER_SELECTED = 1;
	public final static int BORDER_LEGAL = 2;
	public final static int BORDER_ILLEGAL = 3;

	// mouse modes
	public final static int MOUSE_MODE_PIECE_INFO  = 1;
	public final static int MOUSE_MODE_SQUARE_INFO = 2;
	public final static int NUM_MOUSE_MODES        = 2;
	
	protected int mouseMode;
	protected BoardActionHandler boardActionHandlers[] = new BoardActionHandler[NUM_MOUSE_MODES+1];
	
	protected int boardState;
	protected SquareGui selectedSquare;
	protected SquareInputBox selectedInputBox;
	protected boolean enforceLegalMoves;
	
	protected Board board;
	protected Game game;

	protected BoardGui boardGui;
	protected InfoBox infoBox;
	protected GameInfoBox gameInfoBox;

	protected PromotionDialog promotionDialog;

	protected MouseHandler mouseHandler;
	protected KeyHandler keyHandler;

	protected MouseEvent dragEvent;
	protected SquareGui draggedFromSquare;
	protected int draggedPieceType;
	protected int draggedPieceColor;

//debug
public ActionListener keyAction = new ActionListener() {
	public void actionPerformed(ActionEvent e)
	{
		infoBox.appendMessage("Action: " + e.getActionCommand());
	}
};

public FocusListener focusListener = new FocusListener() {
	public void focusGained(FocusEvent e) {
		infoBox.appendMessage("Focus gained:\n\t" + e);
//		System.out.println("Focus gained:\n\t" + e);
	}

	public void focusLost(FocusEvent e) {
		infoBox.appendMessage("Focus lost:\n\t" + e);
//		System.out.println("Focus lost:\n\t" + e);
	}
};

	// used when drag is released (hack)
	protected SquareGui lastSquareEntered;

	class KeyHandler implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
//			if (e.isActionKey())
//			{
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					infoBox.appendMessage("UP");
					break;
					
				case KeyEvent.VK_DOWN:
					infoBox.appendMessage("DOWN");
					break;
					
				case KeyEvent.VK_LEFT:
					infoBox.appendMessage("LEFT");
					break;
					
				case KeyEvent.VK_RIGHT:
					infoBox.appendMessage("RIGHT");
					break;
				}
//			}
		}
		
		public void keyReleased(KeyEvent e)
		{
		}
		
		public void keyTyped(KeyEvent e)
		{
			char keyChar = e.getKeyChar();

			if (Character.isDigit(keyChar))
				selectedInputBox.setValue(Character.digit(keyChar, 10));
//debug
else
infoBox.appendMessage("keyTyped: " + keyChar);
		}		
	}

	class MouseHandler implements MouseListener, MouseMotionListener
	{
		public void mouseEntered(MouseEvent e)
		{
			Component c = e.getComponent();
			if (c instanceof SquareGui)
			{
				SquareGui squareGui = (SquareGui) c;
				boardActionHandlers[mouseMode].mouseEnteredSquare(squareGui, selectedSquare);
				lastSquareEntered = squareGui;
			}
			else
			{
				System.out.println("Caught mouse enter event in Component: " + c);
			}
		}
		
		public void mouseMoved(MouseEvent e)
		{
		}
		
		public void mouseDragged(MouseEvent e)
		{
			Component c = e.getComponent();
			if (c instanceof SquareGui)
			{
				SquareGui squareGui = (SquareGui) c;
				Square square = squareGui.getSquare();

				// piece bank
				if (square == null)
				{
					dragEvent = SwingUtilities.convertMouseEvent(c, e, boardGui.getContentPane());
					draggedFromSquare = squareGui;
					draggedPieceType = squareGui.getPieceType();
					draggedPieceColor = squareGui.getPieceColor();					
				}
				// board
				else
				{
					Piece piece = square.getPiece();

					if (piece != null)
					{
						dragEvent = SwingUtilities.convertMouseEvent(c, e, boardGui.getContentPane());
						draggedFromSquare = squareGui;
						draggedPieceType = piece.getType();
						draggedPieceColor = piece.getColor();

						// make source square's piece disappear
						squareGui.setDragging(true);
					}
				}
			}
			
			boardGui.getGlassPane().repaint();
		}
		
		public void mouseReleased(MouseEvent e)
		{
			if (dragEvent != null)
			{
//debug
//Point p = new Point(0, 0);
//p = SwingUtilities.convertPoint(boardGui.getSquareGui(1, 8), p, boardGui);

//debug
//infoBox.setMessage("orig x: " + e.getX());
//infoBox.appendMessage("orig y: " + e.getY());
				MouseEvent frameEvent = SwingUtilities.convertMouseEvent(e.getComponent(), e, boardGui);
//debug
//infoBox.appendMessage("translated x: " + frameEvent.getX());
//infoBox.appendMessage("translated y: " + frameEvent.getY());
//infoBox.appendMessage("upper left of board x: " + p.x);
//infoBox.appendMessage("upper left of board y: " + p.y);
//infoBox.appendMessage("trans rel. to board x: " + (frameEvent.getX() - p.x));
//infoBox.appendMessage("trans rel. to board y: " + (frameEvent.getY() - p.y));

				Component dest = SwingUtilities.getDeepestComponentAt(boardGui.getContentPane(), frameEvent.getX(), frameEvent.getY());

				if (dest instanceof PieceGui)
					dest = ((PieceGui)dest).getParent();
//debug						
//infoBox.appendMessage("found component: " + dest);
				if (dest instanceof SquareGui)
				{
//					SquareGui destSquare = (SquareGui) dest;
					// hack to determine square to drop in
					SquareGui destSquare = lastSquareEntered;
					// abandon if it didn't work
					if (destSquare == null)
						destSquare = (SquareGui) dest;
					
					boardActionHandlers[mouseMode].pieceDragged(draggedFromSquare, destSquare);
				}
				
				dragEvent = null;
				lastSquareEntered = null;
				draggedFromSquare.setDragging(false);
				draggedFromSquare = null;
				draggedPieceType = 0;
				draggedPieceColor = 0;
				boardGui.getGlassPane().repaint();			
			}
		}
		
		public void mousePressed(MouseEvent e)
		{
			Component c = e.getComponent();
			if (c instanceof SquareGui)
			{
				SquareGui squareGui = (SquareGui) c;
				boardActionHandlers[mouseMode].squareSelected(squareGui, selectedSquare);
			}
			else
			{
				System.out.println("Caught mouse click in Component: " + c);
			}
		}
		
		public void mouseClicked(MouseEvent e)
		{
		}
		
		public void mouseExited(MouseEvent e)
		{
		}		
	}

	//public final static int MOUSE_MODE_MOVE_PIECES = 1;
	abstract class BasicActionHandler implements BoardActionHandler
	{
		GuiContext guiContext;

		public BasicActionHandler(GuiContext context)
		{
			this.guiContext = context;
		}
		
		public void mouseEnteredSquare(SquareGui entered, SquareGui currentSelection)
		{
			Move move = entered.getHighlightedMove();
			String moveInfo = entered.getHighlightedMoveInfo();

			if (moveInfo == null && move != null)
			{
				moveInfo = move.generateMoveInfo();
				entered.setHighlightedMoveInfo(moveInfo);
			}
			
			infoBox.displayMoveInfo(moveInfo);
		}
		
		public void pieceDragged(SquareGui currentSelection, SquareGui dest)
		{
			try {
				deselect(currentSelection);

				// if user clicks on selected square, turn off selection and return
				if (dest == currentSelection)
				{
//debug
infoBox.setMessage("same square!");
					return;
				}

				if (currentSelection != null)
				{
					Square square = currentSelection.getSquare();

					// user click on the board?
					if (square != null)
					{
						// selected square have a piece to move/remove?
						if (square.getPiece() != null)
						{
							// is target the board or the piecebank?
							if (dest.getSquare() == null)
							{
								board.removePiece(square.getPiece());
								board.analyze();
								currentSelection.updateSquare();
								return;
							}
							else
							{
								movePiece(square.getPiece(), dest);
								currentSelection.updateSquare();
								// update the whole board because of special moves (castling)
								boardGui.updateBoard();
								return;
							}
						}
						else
							currentSelection = null;
					}
					// currentSelection is in the piece bank, so add piece to the board
					else
					{
						// but first make sure target is on the board with no piece
						square = dest.getSquare();
						if (square != null)
						{
							Piece piece = square.getPiece();
							if (piece == null)
							{
								board.addPiece(currentSelection.getPieceType(),
								               currentSelection.getPieceColor(), 
								               square.getFile(), square.getRank());
								board.analyze();
								dest.updateSquare();
							}
							return;
						}
						else
							currentSelection = null;
					}
				}
			} catch (ChessException e) {
				boardGui.dialogMsg(e.getMessage());
			}
		}

		public void deselect(SquareGui squareGui)
		{
			if (squareGui != null)
			{
				// if selection isn't in the piece bank
				if (squareGui.getSquare() != null)
				{
					highlightMoveSquares(squareGui, false);
				}
				squareGui.setBorder(BORDER_EMPTY);
				guiContext.setSelectedSquare(null);
			}
		}
	}
	
	//public final static int MOUSE_MODE_PIECE_INFO  = 2;
	class ActionHandler_pieceInfo extends BasicActionHandler implements BoardActionHandler
	{
		public ActionHandler_pieceInfo(GuiContext context)
		{
			super(context);
		}
		
		public void squareSelected(SquareGui toSelect, SquareGui currentSelection)
		{
			if (toSelect == currentSelection || toSelect.getSquare() == null)
				return;
			
			if (currentSelection != null)
			{
				deselect(currentSelection);
				currentSelection = null;
			}
			
			currentSelection = toSelect;
			toSelect.setBorder(BORDER_SELECTED);

			highlightMoveSquares(toSelect, true);
//			System.out.println("Selecting square: " + squareGui.getSquare());
//			System.out.println("      square GUI: " + squareGui);

			guiContext.setSelectedSquare(currentSelection);
		}

		public void highlightMoveSquares(SquareGui source, boolean on)
		{
			Piece piece = source.getSquare().getPiece();
			if (piece != null)
			{
				Iterator moves = piece.getAllMoves();

				Move move;
				SquareGui squareGui;
				
				while (moves.hasNext())
				{ 
					move = (Move) moves.next();
					squareGui = getBoardGui().getSquareGui(move.moveToFile(), move.moveToRank());

					if (on)
					{
						if (move.isLegal())
							squareGui.setBorder(BORDER_LEGAL);
						else
							squareGui.setBorder(BORDER_ILLEGAL);
							
						squareGui.setHighlightedMove(move);
					}
					else
					{
						squareGui.setBorder(BORDER_EMPTY);
						squareGui.setHighlightedMove(null);
					}			
				}
			}
		}
	}
	
	//public final static int MOUSE_MODE_SQUARE_INFO = 3;
	class ActionHandler_squareInfo extends BasicActionHandler implements BoardActionHandler
	{
		public ActionHandler_squareInfo(GuiContext context)
		{
			super(context);
		}
		
		public void squareSelected(SquareGui toSelect, SquareGui currentSelection)
		{
			if (toSelect == currentSelection || toSelect.getSquare() == null)
				return;
			
			if (currentSelection != null)
			{
				deselect(currentSelection);
				currentSelection = null;
			}
			
			currentSelection = toSelect;
			toSelect.setBorder(BORDER_SELECTED);

			highlightMoveSquares(toSelect, true);
			guiContext.setSelectedSquare(currentSelection);
		}

		public void highlightMoveSquares(SquareGui squareGui, boolean on)
		{
			Square square = squareGui.getSquare();
			Move squareMoves[] = square.getSquareMoves();

			SquareGui moveFrom;
			Move move;
			Piece piece;
			
			for (int i=0; i<squareMoves.length; i++)
			{ 
				move = squareMoves[i];
				if (move == null)
					break;

				piece = move.getPiece();
				moveFrom = getBoardGui().getSquareGui(piece.getFile(), piece.getRank());

				if (on)
				{
					if (move.isLegal())
						moveFrom.setBorder(BORDER_LEGAL);
					else
						moveFrom.setBorder(BORDER_ILLEGAL);
						
					moveFrom.setHighlightedMove(move);
				}
				else
				{
					moveFrom.setBorder(BORDER_EMPTY);
					moveFrom.setHighlightedMove(null);
				}				
			}
		}
	}	
	public GuiContext(BoardGui boardGui, Board board)
	{
		this.boardGui = boardGui;
		this.board = board;
		this.game = new Game(board);

		this.mouseHandler = new MouseHandler();
		this.keyHandler = new KeyHandler();
		this.mouseMode = MOUSE_MODE_PIECE_INFO;

		boardActionHandlers[MOUSE_MODE_PIECE_INFO] = new ActionHandler_pieceInfo(this);	
		boardActionHandlers[MOUSE_MODE_SQUARE_INFO] = new ActionHandler_squareInfo(this);

		promotionDialog = new PromotionDialog(boardGui);
	}
	public void clearBoard()
	{
		try {
			boardActionHandlers[mouseMode].deselect(this.selectedSquare);
			board.clearBoard();
			board.addKings();
			board.analyze();
			boardGui.updateBoard();
		} catch (ChessException e) {
			boardGui.dialogMsg(e.getMessage());
		}
	}
	public void debug() {
		int a = 4+5;
	}
	public Board getBoard() {
		return board;
	}
	public BoardGui getBoardGui() {
		return boardGui;
	}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @return int
 */
public int getBoardState() {
	return boardState;
}
	public int getDraggedMousePos_x()
	{
		if (dragEvent == null)
			return 0;
		return dragEvent.getX();
	}
	public int getDraggedMousePos_y()
	{
		if (dragEvent == null)
			return 0;
		return dragEvent.getY();
	}
	public int getDraggedPieceColor()
	{
		return draggedPieceColor;
	}
	public int getDraggedPieceType()
	{
		return draggedPieceType;
	}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @return boolean
 */
public boolean getEnforceLegalMoves() {
	return enforceLegalMoves;
}
	public KeyHandler getKeyHandler()
	{
		return keyHandler;
	}
	public MouseHandler getMouseHandler() {
		return mouseHandler;
	}
/**
 * Insert the method's description here.
 * Creation date: (6/25/02 9:40:18 AM)
 * @return int
 */
public int getMouseMode() {
	return mouseMode;
}
/**
 * Insert the method's description here.
 * Creation date: (7/29/02 9:07:43 AM)
 * @return chess.gui.SquareInputBox
 */
public SquareInputBox getSelectedInputBox() {
	return selectedInputBox;
}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @return chess.gui.SquareGui
 */
public SquareGui getSelectedSquare() {
	return selectedSquare;
}
	public boolean isMouseDragging()
	{
		return (dragEvent != null);
	}
	public void loadPGNFile()
		throws ChessException
	{
		System.out.print("Loading PGN File...");
		game.loadPGNFile("example4.pgn");
		System.out.println("done.");

/*
		System.out.println("Loading random position...");
		game.loadRandomPosition();
*/
	}
	public void loadRandomPosition()
	{
		try {
			System.out.println("Loading random position...");
			game.loadRandomPosition();
			gameInfoBox.setGame(game.getGameNode());
			boardGui.updateBoard();			
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	protected void movePiece(Piece piece, SquareGui dest)
	{
		Square destSquare = dest.getSquare();
		//System.out.println("Attempting to move " + piece + " to " + destSquare);

		Iterator moves = piece.getAllMoves();
		Move move = null;
		boolean legal = false;
		boolean found = false;
		boolean created = false;
		
		while (moves.hasNext())
		{
			move = (Move) moves.next();
			if (move.moveToFile() == destSquare.getFile() &&
			    move.moveToRank() == destSquare.getRank() )
			{
				if (move.isLegal())
					legal = true;

				found = true;
				break;
			}
		}


		if (found)
			infoBox.setMessage("Found move in piece's moves" + (legal?" (legal)":""));
		else
			infoBox.setMessage("Couldn't locate move.");


		try {
			if (!found && !enforceLegalMoves)
			{
				move = board.getMovePool().obtainMove(piece, destSquare.getFile(), 
				                                      destSquare.getRank(), destSquare.getPiece());
				infoBox.appendMessage("Creating new move.");
				created = true;
			}

			if (move.getSpecialMove() == Move.PAWN_PROMOTED)
			{
				promotionDialog.setLocationRelativeTo(boardGui);
				int promotesTo = promotionDialog.displayDialog();

				if (promotesTo == 0)
					return;
					
				move.setPawnPromotesTo(promotesTo);
			}
				
			if (!enforceLegalMoves || legal)
			{
				infoBox.appendMessage("GuiContext.movePiece(): " + move);
				board.commitMove(move);
				board.analyze();
			}
			else
				infoBox.appendMessage("GuiContext.movePiece(): canceling move");			
		} catch (ChessException e) {
			System.out.println("GuiContext.movePiece(): " + e);
		} finally {
			if (created)
				board.getMovePool().release(move);
		}
	}
	public void resetBoard()
	{
		try {
			boardActionHandlers[mouseMode].deselect(this.selectedSquare);
			board.resetBoard();
			board.analyze();
			boardGui.updateBoard();
		} catch (ChessException e) {
			boardGui.dialogMsg(e.getMessage());
		}
	}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @param newBoardState int
 */
public void setBoardState(int newBoardState) {
	boardState = newBoardState;
}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @param newEnforceLegalMoves boolean
 */
public void setEnforceLegalMoves(boolean newEnforceLegalMoves) {
	enforceLegalMoves = newEnforceLegalMoves;
}
/**
 */
public void setGameInfoBox(GameInfoBox gameInfoBox) {
	this.gameInfoBox = gameInfoBox;
}
/**
 */
public void setInfoBox(InfoBox infoBox) {
	this.infoBox = infoBox;
}
/**
 * Insert the method's description here.
 * Creation date: (6/25/02 9:40:18 AM)
 * @param newMouseMode int
 */
public void setMouseMode(int newMouseMode) {
	boardActionHandlers[mouseMode].deselect(selectedSquare);
	mouseMode = newMouseMode;
}
/**
 * Insert the method's description here.
 * Creation date: (7/29/02 9:07:43 AM)
 * @param newSelectedInputBox chess.gui.SquareInputBox
 */
public void setSelectedInputBox(SquareInputBox newSelectedInputBox) {
	if (selectedInputBox != null)
		selectedInputBox.setSelected(false);
		
	selectedInputBox = newSelectedInputBox;
	selectedInputBox.setSelected(true);
	selectedInputBox.requestFocus();
}
/**
 * Insert the method's description here.
 * Creation date: (6/14/02 8:06:12 AM)
 * @param newOriginatingSquare chess.gui.SquareGui
 */
public void setSelectedSquare(SquareGui newSelectedSquare) {
	selectedSquare = newSelectedSquare;
}
}
