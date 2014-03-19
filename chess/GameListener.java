package chess;

import java.util.*;
/**
 * Insert the type's description here.
 */
public interface GameListener {
	// functions return false to 'absorb' the message,
	// meaning, no other listeners get called
	// is this actually useful?
	public boolean boardUpdated();
	public boolean gameOver();
	public boolean gameReset();
}
