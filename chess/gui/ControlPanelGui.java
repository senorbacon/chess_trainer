package chess.gui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 */
public class ControlPanelGui extends JPanel {

 	protected static String[] modeNames = new String[GuiContext.NUM_MOUSE_MODES+1];
 	static {
	 	modeNames[GuiContext.MOUSE_MODE_PIECE_INFO] = "Piece Info";
	 	modeNames[GuiContext.MOUSE_MODE_SQUARE_INFO] = "Square Info";
 	}
 	
	GuiContext guiContext;
	
	JComboBox mouseModeBox;
	JButton   clearButton;
	JButton   resetButton;
	JButton   debugButton;
	JButton   loadButton;

	class MouseModeItem
	{
	 	protected int value;

	 	public MouseModeItem(int val)
	 	{
		 	this.value = val;
	 	}

	 	public int getValue()
	 	{
		 	return value;
	 	}

	 	public String toString()
	 	{
		 	return modeNames[value];
	 	}
	}
	public ControlPanelGui(GuiContext context)
	{
		this.guiContext = context;

		MouseModeItem items[] = {new MouseModeItem(GuiContext.MOUSE_MODE_PIECE_INFO),
		                         new MouseModeItem(GuiContext.MOUSE_MODE_SQUARE_INFO)};
		mouseModeBox = new JComboBox(items);
		mouseModeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				MouseModeItem item = (MouseModeItem) cb.getSelectedItem();
				guiContext.setMouseMode(item.getValue());
			}
		});

		clearButton = new JButton("Clear Board");
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guiContext.clearBoard();
			}
		});

		resetButton = new JButton("Reset Board");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guiContext.resetBoard();
			}
		});

		debugButton = new JButton("Debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guiContext.debug();
			}
		});

		loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				guiContext.loadRandomPosition();
			}
		});

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(mouseModeBox);
		//add(Box.createRigidArea(new Dimension(0, 10)));
		add(resetButton);
		//add(Box.createRigidArea(new Dimension(0, 10)));
		add(clearButton);
		//add(Box.createRigidArea(new Dimension(0, 10)));
		add(debugButton);
		//add(Box.createRigidArea(new Dimension(0, 10)));
		add(loadButton);
		
		//setBorder(new SoftBevelBorder(BevelBorder.RAISED));
	}
}
