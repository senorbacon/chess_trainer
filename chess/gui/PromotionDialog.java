package chess.gui;

import chess.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Insert the type's description here.
 * Creation date: (9/27/02 7:56:30 AM)
 * @author: 
 */
public class PromotionDialog extends JDialog {

	JList selectionList;
	boolean cancelled;

	class PromotionItem
	{
		String name;
		int value;
		
		PromotionItem(String name, int value)
		{
			this.name = name;
			this.value = value;
		}

		int getValue()
		{
			return value;
		}

		public String toString()
		{
			return name;
		}
	}
	PromotionDialog(Frame parent)
	{
		super(parent, true);

		PromotionItem items[] = {
			new PromotionItem("Queen", Piece.QUEEN),
			new PromotionItem("Rook", Piece.ROOK),
			new PromotionItem("Bishop", Piece.BISHOP),
			new PromotionItem("Knight", Piece.KNIGHT)
		};

		selectionList = new JList(items);
		selectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reset();

		JButton okButton = new JButton("  OK  ");
		JButton cancelButton = new JButton("Cancel");

		final PromotionDialog thisDialog = this;
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				thisDialog.hide();
			}
		});
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				cancelled = true;
				thisDialog.hide();
			}
		});

		Container contentPane = getContentPane();
		contentPane.add(selectionList, BorderLayout.CENTER);
		contentPane.add(new JLabel("Pawn promotes to:"), BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(cancelButton);

		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		pack();

		this.setDefaultCloseOperation(
		    JDialog.DO_NOTHING_ON_CLOSE);
	}
	/*
	** Do everything: reset the dialog, display it, and return the selected value
	** 
	*/
	public int displayDialog()
	{
		reset();
		show();

		if (cancelled)
			return 0;

		return ((PromotionItem) selectionList.getSelectedValue()).getValue();
	}
	void reset()
	{
		// select the queen by default
		selectionList.setSelectedIndex(0);
		cancelled = false;
	}
}
