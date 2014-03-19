package chess.gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import chess.*;


/**
 * Insert the type's description here.
 */
public class BoardGui extends JFrame
	implements Runnable 
{
	public final static int DEFAULT_SIZE_X = 600;
	public final static int DEFAULT_SIZE_Y = 500;

	ControlPanelGui controlPanel;
	BoardCanvas boardCanvas;
	PieceBankGui pieceBank;
	InfoBox infoBox;
	GameInfoBox gameInfoBox;

	JMenu menu;
	JMenuBar menuBar;

	protected Board board;
	protected GuiContext guiContext;

	class DragImageGlassPane extends Component
	{
	    public void paint(Graphics g) 
	    {
			if (guiContext.isMouseDragging())
			{
				Image img = PieceGui.getPieceImage(guiContext.getDraggedPieceType(),
				                                   guiContext.getDraggedPieceColor());

				int imageHeight = img.getHeight(null);
				int imageWidth = img.getWidth(null);

				g.drawImage(img, guiContext.getDraggedMousePos_x() - imageWidth/2, 
				                 guiContext.getDraggedMousePos_y() - imageHeight/2, null);
			}
	    }
	}

/**
 * BoardGui constructor comment.
 */
public BoardGui(Board board) {
	super();

	this.board = board;
}
	void createMenuBar()
	{
		JMenu submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		//Build the first menu.
		menu = new JMenu("A Menu");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menu);

		//a group of JMenuItems
		menuItem = new JMenuItem("A text-only menu item",
		                         KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
		        "This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new JMenuItem("Both text and icon", 
		                         new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_D);
		menu.add(menuItem);

		//a group of radio button menu items
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_R);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Another one");
		rbMenuItem.setMnemonic(KeyEvent.VK_O);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		cbMenuItem.setMnemonic(KeyEvent.VK_H);
		menu.add(cbMenuItem);

		//a submenu
		menu.addSeparator();
		submenu = new JMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new JMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_2, ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menu.add(submenu);

		//Build second menu in the menu bar.
		menu = new JMenu("Another Menu");
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(
		        "This menu does nothing");
		menuBar.add(menu);
	}
	public void dialogMsg(String msg)
	{
		JOptionPane.showMessageDialog(getContentPane(), msg, "oww, my tits!", JOptionPane.ERROR_MESSAGE);
	}
/**
 */
public SquareGui getSquareGui(int file, int rank) {
	return boardCanvas.getSquareGui(file, rank);
}
	public void init()
		throws ChessException
	{
		guiContext = new GuiContext(this, board);
		setName("Hooooold my nuts");
		
		setSize(DEFAULT_SIZE_X, DEFAULT_SIZE_Y);

//debug
//this.addFocusListener(guiContext.focusListener);

		// set up glass pane for handling dragon drop images
		DragImageGlassPane gp = new DragImageGlassPane();
		setGlassPane(gp);
		gp.setVisible(true);
		
		boardCanvas = new BoardCanvas(guiContext);
		boardCanvas.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		controlPanel = new ControlPanelGui(guiContext);
//		pieceBank = new PieceBankGui(guiContext);
		infoBox = new InfoBox(guiContext);
		gameInfoBox = new GameInfoBox(guiContext);

/*
boardCanvas.setNextFocusableComponent(controlPanel.clearButton);
controlPanel.clearButton.setNextFocusableComponent(controlPanel.resetButton);
controlPanel.resetButton.setNextFocusableComponent(infoBox);
infoBox.setNextFocusableComponent(controlPanel.mouseModeBox);
controlPanel.mouseModeBox.setNextFocusableComponent(controlPanel.debugButton);
controlPanel.debugButton.setNextFocusableComponent(boardCanvas);

//debug
boardCanvas.addFocusListener(guiContext.focusListener);
controlPanel.mouseModeBox.addFocusListener(guiContext.focusListener);
controlPanel.resetButton.addFocusListener(guiContext.focusListener);
controlPanel.clearButton.addFocusListener(guiContext.focusListener);
controlPanel.debugButton.addFocusListener(guiContext.focusListener);
infoBox.addFocusListener(guiContext.focusListener);
*/
		// piecebank panel
		//JPanel pieceBankPanel = new JPanel();
		//pieceBankPanel.add(pieceBank, BorderLayout.CENTER);
		//pieceBankPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		// game info panel
		JPanel gameInfoPanel = new JPanel();
		gameInfoPanel.setLayout(new BoxLayout(gameInfoPanel, BoxLayout.X_AXIS));
		gameInfoPanel.add(gameInfoBox);

		// chessboard panel
		JPanel chessboardPanel = new JPanel();
		chessboardPanel.setLayout(new BoxLayout(chessboardPanel, BoxLayout.Y_AXIS));
		chessboardPanel.add(gameInfoPanel);
		chessboardPanel.add(boardCanvas);
		chessboardPanel.add(controlPanel);

		// info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.add(infoBox);

		Container contentPane = getContentPane();
		getContentPane().add(chessboardPanel, BorderLayout.EAST);
		getContentPane().add(infoPanel, BorderLayout.CENTER);

		final BoardGui me = this;
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) 
			{
				dispose();
				System.exit(0);
			}

			public void windowOpened(java.awt.event.WindowEvent e)
			{
				Thread t = new Thread(me);
				t.start();
			}
		});

		// create menu
		createMenuBar();

		// catch all key events and funnel them to GuiContext
		addKeyListener(guiContext.getKeyHandler());

		// select the upper-left-most input box
		guiContext.setSelectedInputBox(boardCanvas.getSquareGui(1, 8).getInputBox(Piece.BLACK));

		setVisible(true);
	}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	try {
		Board board = new Board();
		//board.resetBoard();
		//board.analyze();

		BoardGui boardGui = new BoardGui(board);
		boardGui.init();
	} catch (Exception e) {
		System.out.println(e);
	}
}
	public void paint(Graphics g)
	{
		super.paint(g);
/*
		if (guiContext.isMouseDragging())
		{
			Image img = PieceGui.getPieceImage(guiContext.getDraggedPieceType(),
			                                   guiContext.getDraggedPieceColor());
			
			g.drawImage(img, guiContext.getDraggedMousePos_x(), 
			                 guiContext.getDraggedMousePos_y(), null);
		}
*/
	}
/**
 * Executed after window opens
 */
public void run() 
{
	final JDialog dialog = new JDialog(this, true);
	JPanel p = new JPanel();
	p.add(new JLabel("Loading PGN file. Please wait."), BorderLayout.CENTER);
	dialog.setContentPane(p);
	dialog.pack();
	dialog.setLocationRelativeTo(this);

	Runnable r = new Runnable() {
		public void run() {
			// create info box
			dialog.show();
		}
	};


	try {
		// give window time to open
		Thread.sleep(2000);
		
		// display information box saying we're loading PGN file
		SwingUtilities.invokeLater(r);
		
		Thread.sleep(2000);
		
		// load the PGN file
		guiContext.loadPGNFile();

		// close info box
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				// close box
				dialog.hide();
			}
		} );
	} catch (Exception e) {
		System.out.println(e);
		e.printStackTrace();
	}
}
	public void updateBoard()
	{
		boardCanvas.updateBoard();
	}
}
