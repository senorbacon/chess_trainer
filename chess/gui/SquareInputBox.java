package chess.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 */
public class SquareInputBox extends JComponent {

	protected final static Color purpleColor = new Color(130, 90, 130);
	protected final static Color ltGreenColor = new Color(190, 210, 190);
	
	protected final static Border border = LineBorder.createBlackLineBorder();

	SquareGui parent;
	
	Color bgColor;
	Color fgColor;

	int value;

	public SquareInputBox(SquareGui parent, Color bg)
	{
		this.parent = parent;

		// darken color
		float SCALE = .8F;
		float rgb[] = new float[3];
		bg.getColorComponents(rgb);
		this.bgColor = new Color(rgb[0] * SCALE, rgb[1] * SCALE, rgb[2] * SCALE);
		this.setBackground(this.bgColor);

		Font font = Font.decode("Rockwell Extra Bold");
		font = font.deriveFont(9F);
		this.setFont(font);
		this.fgColor = Color.darkGray;
		this.setForeground(fgColor);

		this.value = 0;

		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e)
			{
				setBorder(border);
			}
			
			public void focusLost(FocusEvent e)
			{
				setBorder(null);
			}
		});		
	}
	public boolean isOpaque()
	{
		return true;
	}
	public void paint(Graphics g)
	{
		super.paint(g);

		Rectangle r = g.getClipBounds();

		g.setColor(this.getBackground());
		g.fillRect(r.x, r.y, r.width, r.height);

		g.setColor(this.getForeground());
		if (value != 0)
			g.drawString(String.valueOf(value), 1, getBounds().height - 2);
	}
	public void setSelected(boolean selected)
	{
		if (selected)
		{
			setBackground(purpleColor);
			setForeground(ltGreenColor);
		}
		else
		{
			setBackground(bgColor);
			setForeground(fgColor);
		}
		repaint();
	}
	public void setValue(int value)
	{
		this.value = value;
		repaint();
	}
}
