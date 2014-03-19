package chess;

import java.util.*;

/**
 * Insert the type's description here.
 */
public class Move implements Poolable {

	public final static int NON_ATTACK        = 0x01;
	public final static int XRAY              = 0x02;
	public final static int ATTACK_ILLEGAL    = 0x04;
	public final static int DISCOVERED_CHECK  = 0x08;
	public final static int SKEWER            = 0x10;

	// move status flags
	public final static int PINNED_CONTRIBUTES = 0x4000;
	public final static int PINNED_PROCESSED   = 0x8000;
	
	protected Piece piece;
	protected int moveFromFile;
	protected int moveFromRank;
	protected int moveToFile;
	protected int moveToRank;

	protected Piece attackedPiece;
	protected Piece pinningPiece;
	protected Piece checkingPiece;

	protected int flags;
	/*	
	protected boolean isCapture;
	protected boolean isAttack;
	protected boolean isLegal;
	protected boolean isXray;
	protected boolean defends;
	protected boolean isDiscoveredCheck;
	*/

	/*
	** Special moves
	*/
	public final static int EN_PASSANT       = 1;
	public final static int PAWN_PROMOTED    = 2;
	public final static int DOUBLE_PAWN_MOVE = 3;
	public final static int KINGSIDE_CASTLE  = 4;
	public final static int QUEENSIDE_CASTLE = 5;

	protected int specialMoveType;

	// what piece will the pawn become if it promotes?
	protected int pawnPromotedTo;

	protected int poolID;

	protected StringBuffer theString = new StringBuffer();

	public Move(Piece piece, int moveToFile, int moveToRank, Piece capturedPiece)
	{
		resetMove(piece, moveToFile, moveToRank, capturedPiece);
	}
	public Move clearFlag(int flag)
	{
		flags = flags & (Integer.MAX_VALUE - flag);
		return this;
	}
	protected void clearFlags()
	{
		this.flags = 0;
		
		pinningPiece = null;
		checkingPiece = null;
		specialMoveType = 0;
		pawnPromotedTo = 0;		
	}
	public Move copyMove(Move source)
	{
		this.piece = source.piece;
		this.moveFromFile = source.moveFromFile;
		this.moveFromRank = source.moveFromRank;
		this.moveToFile = source.moveToFile;
		this.moveToRank = source.moveToRank;
		this.attackedPiece = source.attackedPiece;

		this.flags = source.flags;
		this.pinningPiece = source.pinningPiece;
		this.checkingPiece = source.checkingPiece;
		this.specialMoveType = source.specialMoveType;
		
		this.theString.setLength(0);

		return this;
	}
	public String generateMoveInfo()
	{
		StringBuffer buf = new StringBuffer();

		buf.append(Board.getFileChar(moveToFile())).append(moveToRank());
		String destString = buf.toString();
		
		buf.setLength(0);
		buf.append(toString());
		buf.append("\n\n");

/*
	// move status flags
	public final static int PINNED_CONTRIBUTES = 0x4000;
	public final static int PINNED_PROCESSED   = 0x8000;

*/
		Piece piece = getPiece();
		int color = piece.getColor();
		String colorString[] = { "", "white", "black" };

		// describe piece
		buf.append(colorString[color]).append("'s ").append(piece.getName()).append(' ');

		// verb
		if (isIllegalAttack())
			buf.append("ATTACKS ");
		else if (isAttack())
			buf.append("ATTACKS ");
		else
			buf.append("MOVES to ");

		// destination square
		Piece attackedPiece = getAttackedPiece();
		if (attackedPiece == null)
			buf.append(destString);
		else
		{
			buf.append(colorString[attackedPiece.getColor()]).append("'s ");
			buf.append(attackedPiece.getName()).append(" on ").append(destString);
		}
		buf.append(".\n\n");

		if (isEnPassant())
			buf.append("This is an EN PASSANT move.\n\n");

		if (isKingsideCastle())
			buf.append("This is a kingside CASTLING move.\n\n");

		if (isQueensideCastle())
			buf.append("This is a queenside CASTLING move.\n\n");

		if (isPawnPromoted())
			buf.append("Pawn promotes to a " + Piece.pieceNames[pawnPromotedTo()] + ".\n\n");

		if (isXray())
			buf.append("This is an XRAY move.\n\n");

		if (isSkewer())
			buf.append("This is a SKEWER move.\n\n");
		
		if (isDiscoveredCheck())
			buf.append("This move is a DISCOVERED CHECK.\n\n");

		boolean pinned = false;
		Piece pinningPiece = isPinned();
		if (pinningPiece != null)
		{
			buf.append("However, this piece is pinned by ");
			buf.append(colorString[pinningPiece.getColor()]).append("'s ");
			buf.append(pinningPiece.getName()).append(".\n\n");
			pinned = true;
		}

		Piece immobilizingPiece = isImmobile();	
		if (immobilizingPiece != null)
		{
			if (pinned)
				buf.append("And, ");
			else
				buf.append("However, ");
			buf.append("this move leaves the king in check (given by ");
			buf.append(colorString[immobilizingPiece.getColor()]).append("'s ");
			buf.append(immobilizingPiece.getName()).append(").\n\n");
		}

		return buf.toString();
	}
	public static void generateMoveString(Move move)
	{
		Piece piece = move.getPiece();
		if (piece == null)
		{
			move.theString.append("null piece!");
			return;
		}

		// mark illegal moves with an '!'
		if (!move.isLegal())
			move.theString.append('!');

		if (move.isKingsideCastle())
		{
			move.theString.append("OxO");
			return;
		}

		if (move.isQueensideCastle())
		{
			move.theString.append("OxOxO");
			return;
		}

		switch (piece.getType())
		{
			case Piece.PAWN:
				break;

			default:
				move.theString.append(Character.toUpperCase(piece.getLetter()));
		}

		// append clarifying letter if nec.
		// this means a pawn's original file if capture;
		if ((move.getAttackedPiece() != null || move.isIllegalAttack())  &&  piece.getType() == Piece.PAWN)
			move.theString.append(Board.fileChars[piece.getFile()]);

		// or, for all major pieces (due to promotion), if another piece
		// can arrive at the same square, need to specify either rank or file

		if (move.getAttackedPiece() != null || move.isIllegalAttack())
			move.theString.append('x');

		move.theString.append(Board.fileChars[move.moveToFile()]);
		move.theString.append(move.moveToRank());

		// en passant
		if (move.isEnPassant())
			move.theString.append(" e.p.");

		if (move.isPawnPromoted())
		{
			move.theString.append(" = ");
			move.theString.append(Piece.pieceLetter[move.pawnPromotedTo()]);
		}			
		
		Board board = piece.getBoard();
		
	}
	public Piece getAttackedPiece() {
		return attackedPiece;
	}
/**
 * getFlags method comment.
 */
public int getFlags() {
	return flags;
}
	public Piece getPiece()
	{
		return this.piece;
	}
/**
 * getPoolID method comment.
 */
public int getPoolID() {
	return poolID;
}
	public int getSpecialMove()
	{
		return specialMoveType;
	}
	public boolean isAttack()
	{
		return ((flags & this.NON_ATTACK) == 0);
	}
	public boolean isAttackingAPiece()
	{
		return (attackedPiece != null);
	}
	public boolean isDiscoveredCheck()
	{
		return ((flags & DISCOVERED_CHECK) != 0);
	}
	public boolean isDoublePawnMove()
	{
		return (specialMoveType == DOUBLE_PAWN_MOVE);
	}
	public boolean isEnPassant()
	{
		return (specialMoveType == EN_PASSANT);
	}
	public boolean isIllegalAttack()
	{
		return ((flags & ATTACK_ILLEGAL) != 0);
	}
	public Piece isImmobile()
	{
		return checkingPiece;
	}
	public boolean isKingsideCastle()
	{
		return (specialMoveType == KINGSIDE_CASTLE);
	}
	public boolean isLegal()
	{
		return !((pinningPiece != null)  || 
		         (checkingPiece != null) || 
		         ((flags & XRAY) != 0)   ||
		         ((flags & ATTACK_ILLEGAL) != 0)||
		         ((flags & SKEWER) != 0)   );
	}
	public boolean isPawnPromoted()
	{
		return (specialMoveType == PAWN_PROMOTED);
	}
	public Piece isPinned()
	{
		return pinningPiece;
	}
	public boolean isQueensideCastle()
	{
		return (specialMoveType == QUEENSIDE_CASTLE);
	}
	public boolean isSkewer()
	{
		return ((flags & SKEWER) != 0);
	}
	public boolean isXray()
	{
		return ((flags & XRAY) != 0);
	}
	public int moveFromFile()
	{
		return moveFromFile;
	}
	public int moveFromRank()
	{
		return moveFromRank;
	}
	public int moveToFile()
	{
		return this.moveToFile;
	}
	public int moveToRank()
	{
		return this.moveToRank;
	}
	public int pawnPromotedTo()
	{
		return pawnPromotedTo;
	}
	public Move resetMove(Piece piece, int moveToFile, int moveToRank, Piece attackedPiece)
	{
		this.piece = piece;

		if (piece != null)
		{
			this.moveFromFile = piece.getFile();
			this.moveFromRank = piece.getRank();
		}
		this.moveToFile = moveToFile;
		this.moveToRank = moveToRank;
		this.attackedPiece = attackedPiece;

		clearFlags();
		
		this.theString.setLength(0);

		return this;
	}
	public Move setFlag(int flag)
	{
		flags = flags | flag;
		return this;
	}
	public Move setFlags(int flags,
	                     Piece pinningPiece,
	                     Piece checkingPiece)
	{
		this.flags = flags;
		this.pinningPiece = pinningPiece;
		this.checkingPiece = checkingPiece;

		return this;
	}
	public Move setPawnPromotesTo(int promotesTo)
	{
		this.pawnPromotedTo = promotesTo;
		return this;
	}
	// not really a public method; called by code that needs to
	// figure out the piece from a PGN file.
	void setPiece(Piece piece)
	{
		this.piece = piece;
		this.moveFromFile = piece.getFile();
		this.moveFromRank = piece.getRank();		
	}
/**
 * setPoolID method comment.
 */
public void setPoolID(int poolID) {
	this.poolID = poolID;
}
	public Move setSpecialMove(int moveType)
	{
		specialMoveType = moveType;
		return this;
	}
	public String toString()
	{
		if (theString.length() == 0)
			generateMoveString(this);
			
		return theString.toString();
	}
}
