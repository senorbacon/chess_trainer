package chess.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import chess.*;
import chess.parser.pgn.*;

/**
 */
public class GameInfoBox extends JPanel {
	public static String TAG_EVENT  = "event";
	public static String TAG_SITE   = "site";
	public static String TAG_DATE   = "date";
	public static String TAG_ROUND  = "round";
	public static String TAG_WHITE  = "white";
	public static String TAG_BLACK  = "black";
	public static String TAG_RESULT = "result";
	public static String TAG_ECO    = "eco";
	
	GuiContext guiContext;

	JLabel lPlayers;
	JLabel lDate;
	JLabel lEvent;
	JLabel lSite;
	JLabel lResult;

	HashMap infoMap;
	public GameInfoBox(GuiContext context)
	{
		super();
		this.guiContext = context;
		context.setGameInfoBox(this);

		lPlayers = new JLabel();
		lDate = new JLabel();
		lEvent = new JLabel();
		lSite = new JLabel();
		lResult = new JLabel();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(lEvent);
		add(lSite);
		add(lPlayers);
		add(lDate);
		add(lResult);

		this.infoMap = new HashMap();
		setGame(null);
		//this.setBorder(new CompoundBorder(new EmptyBorder(20, 10, 20, 10), new LineBorder(Color.black)));
	}
	public void setGame(PGN_Game gameNode)
	{
		Node node;
		PGN_TagPair pairNode;

		if (gameNode != null)
		{
			int numNodes = gameNode.jjtGetNumChildren();
			infoMap.clear();
			
			for (int i=0; i<numNodes; i++)
			{
				node = gameNode.jjtGetChild(i);
				if (node instanceof PGN_TagPair)
				{
					pairNode = (PGN_TagPair) node;
					infoMap.put(pairNode.getName().toLowerCase(), pairNode.getValue());
				}
			}
		}

		String white  = (String) infoMap.get(TAG_WHITE);
		String black  = (String) infoMap.get(TAG_BLACK);
		String event  = (String) infoMap.get(TAG_EVENT);
		String site   = (String) infoMap.get(TAG_SITE);
		String round  = (String) infoMap.get(TAG_ROUND);
		String date   = (String) infoMap.get(TAG_DATE);
		String result = (String) infoMap.get(TAG_RESULT);

		white  = (white == null)?"?":white;
		black  = (black == null)?"?":black;
		event  = (event == null)?"?":event;
		site   = (site  == null)?"?":site;
		round  = (round == null)?"?":round;
		date   = (date  == null)?"?":date;
		result = (result== null)?"?":result;

		StringBuffer buf = new StringBuffer();
		buf.append("Event: ").append(event).append(", round ").append(round);
		lEvent.setText(buf.toString());

		buf.setLength(0);
		buf.append("Players: ").append(white).append(" - ").append(black);
		lPlayers.setText(buf.toString());

		buf.setLength(0);
		buf.append("Site: ").append(site);
		lSite.setText(buf.toString());

		buf.setLength(0);
		buf.append("Date: ").append(date);
		lDate.setText(buf.toString());

		buf.setLength(0);
		buf.append("Result: ").append(result);
		lResult.setText(buf.toString());
	}
}
