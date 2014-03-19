package chess.gui;

import chess.*;
import java.util.*;

/**
 */
public interface BoardActionHandler {
	public void deselect(SquareGui currentSelection);
	public void highlightMoveSquares(SquareGui squareGui, boolean on);
	public void mouseEnteredSquare(SquareGui entered, SquareGui currentSelection);
	public void pieceDragged(SquareGui source, SquareGui dest);
	public void squareSelected(SquareGui toSelect, SquareGui currentSelection);
}
