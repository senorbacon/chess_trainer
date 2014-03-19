package chess.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import chess.*;
/**
 */
public class InfoBox extends JScrollPane {
	GuiContext guiContext;

	JTextArea textArea;

	public InfoBox(GuiContext context)
	{
		super();
		this.guiContext = context;
		guiContext.setInfoBox(this);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

//debug
//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//Font fonts[] = ge.getAllFonts();
//for (int i=0; i<fonts.length; i++)
//	appendMessage(fonts[i].toString());
		
		this.setViewportView(textArea);
		this.setBorder(new CompoundBorder(new EmptyBorder(20, 10, 20, 10), new LineBorder(Color.black)));
	}
	public void appendMessage(String message)
	{
		StringBuffer buf = new StringBuffer(textArea.getText());
		buf.append(message).append('\n');
		textArea.setText(buf.toString());
	}
	public void displayMoveInfo(String moveInfo)
	{
		if (moveInfo == null)
			textArea.setText("");
		else
			textArea.setText(moveInfo);
	}
	public void setMessage(String message)
	{
		StringBuffer buf = new StringBuffer(message);
		buf.append('\n');
		textArea.setText(message);
	}
}
