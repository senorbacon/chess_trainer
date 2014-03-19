package chess;

import java.util.*;
import chess.parser.pgn.*;

/**
 * Insert the type's description here.
 */
public class Game {

	int plies;
	int moveNum;
	int color;

	Board board;

	// moves are keyed by ply number
	List moves = new ArrayList();
	PGN_Start gamesRoot;
	PGN_Game gameNode;

	List gameListeners = new ArrayList();
	MoveSource moveSource;

	public Game(Board board)
	{
		this.board = board;
	}
	public void fireBoardUpdated()
	{
		Iterator listeners = gameListeners.iterator();
		while (listeners.hasNext())
			((GameListener)listeners.next()).boardUpdated();
	}
	public void fireGameOver()
	{
		Iterator listeners = gameListeners.iterator();
		while (listeners.hasNext())
			((GameListener)listeners.next()).gameOver();
	}
	public void fireGameReset()
	{
		Iterator listeners = gameListeners.iterator();
		while (listeners.hasNext())
			((GameListener)listeners.next()).gameReset();
	}
	public Board getBoard()
	{
		return board;
	}
	public PGN_Game getGameNode()
	{
		return gameNode;
	}
	// load & parse PGN file and set gamesRoot to the root node
	public void loadPGNFile(String filename)
		throws ChessException
	{
		try {
			PGN parser = new PGN(new java.io.FileInputStream(filename));
			gamesRoot = (PGN_Start) parser.Start();
		} catch (Exception e) {
			throw new ChessException(e);
		}
	}
	public void loadRandomPosition()
		throws ChessException
	{
		if (gamesRoot == null)
			throw new ChessException("PGN file not loaded.");

		// get random game node
		Random r = new Random();
		int gameNum = (int) (r.nextDouble() * gamesRoot.jjtGetNumChildren());
		
		gameNode = (PGN_Game)gamesRoot.jjtGetChild(gameNum);
		
		MovePool movePool = board.getMovePool();
		Move sourceMove;
		Move move;
		PGN_MoveDesc moveNode;
		Node node;

		// number of children, including tag pairs	
		int numNodes = gameNode.jjtGetNumChildren();

		// used for calculating number of moves
		int numMoves = gameNode.jjtGetNumChildren();
		int moveNum = 10000;

		// reset the board
		this.reset();

		for (int i=0, mc=0; mc<moveNum && i<numNodes; i++)
		{
			node = gameNode.jjtGetChild(i);
			if (node instanceof PGN_TagPair)
			{
				numMoves--;
			}
			else if (node instanceof PGN_MoveDesc)
			{
				// pick the random move
				// we wait until this point because we don't know how many
				// moves we have until we're done processing tag pairs.
				if (moveNum == 10000)
					moveNum = (int) (r.nextDouble() * numMoves);
					
				moveNode = (PGN_MoveDesc) node;
				int moveToFile = moveNode.getFile();
				int moveToRank = moveNode.getRank();

				sourceMove = null;
				move = null;

				// castling moves, we just need to look up the appropriate
				// color king, and find the castle move to the correct side
				if (moveNode.isCastle())
				{
					King king = board.getKing(this.color);
					Iterator kingMoves = king.getAllMoves();
					while (kingMoves.hasNext())
					{
						move = (Move) kingMoves.next();
						if ( (move.isKingsideCastle() && moveNode.isKingsideCastle()) ||
						     (move.isQueensideCastle() && moveNode.isQueensideCastle())  )
						{
							sourceMove = move;
							break;
						}
					}
					if (sourceMove == null)
						throw new IllegalMoveException("Couldn't find castling move (" + moveNode + ").");
				}
				else
				{
					// figure out which piece is moving;
					// we know where it's moving to.
					// We need to get the legal moves to the square and use
					// clarifying ranks/files to clear up any ambiguity
					int pieceType = moveNode.getPieceType();
					Square square = board.getSquare(moveToFile, moveToRank);
					Move squareMoves[] = square.getSquareMoves();
					Move squareMove;

					Move possibleMoves[] = new Move[4];
					int moveCtr = 0;

					// compile all legal moves that have piece type given by moveNode
					int j;
					for (j=0; j<square.moveCount; j++)
					{
						if (squareMoves[j].isLegal() &&
							squareMoves[j].getPiece().getType() == pieceType &&
							squareMoves[j].getPiece().getColor() == this.color)
							possibleMoves[moveCtr++] = squareMoves[j];
					}

					if (moveCtr == 0)
						throw new IllegalMoveException("PGN specifies illegal move (" + moveNode + ").");

					// only one move with this piece; choose this one
					else if (moveCtr == 1)
					{
						sourceMove = possibleMoves[0];
					}
					else
					{
						// multiple moves with this piece; loop through until we find
						// the move with that uses either the clarifying rank/file
						int cf = moveNode.getClarifyingFile();
						int cr = moveNode.getClarifyingRank();
						int validMoves = 0;
						boolean badFile = false, badRank = false;

						// mark invalid any moves that don't match clarifying rank or file (or both)
						for (j=0; j<moveCtr; j++)
						{
							if (cf != 0)
								badFile = (cf != possibleMoves[j].moveFromFile());
							if (cr != 0)
								badRank = (cr != possibleMoves[j].moveFromRank());
							
							if (badFile || badRank)
								possibleMoves[j] = null;
							else
								validMoves++;
						}

						if (validMoves != 1)
							throw new IllegalMoveException("Can not clarify move (" + moveNode + ").");

						for (j=0; j<moveCtr; j++)
						{
							if (possibleMoves[j] != null)
							{
								sourceMove = possibleMoves[j];
								break;
							}
						}
						
						if (sourceMove == null)
							throw new IllegalMoveException("Error clarifying move (" + moveNode + ").");
					}
				}

				move = (Move) movePool.obtain();
				move.copyMove(sourceMove);
				
				// set the pawn promotion piece
				if (moveNode.getPromotionPiece() != 0)
					move.setPawnPromotesTo(moveNode.getPromotionPiece());

				movePiece(move);

				mc++;
			}
		}		
	}
	public static void main(String[] args) {
		Board board = new Board();
		MovePool movePool = board.getMovePool();
		Game game = new Game(board);
		GameTextView gameTextView = new GameTextView(game);

		// set up Debug
		Debug.debugMsg("main: ", Debug.INFO, "Starting...");

//
//  WARNING - code below could now lead to problems since we're keeping track
//            of moves in the moves List; the code below frees moves as soon as
//            they're done, leading to possible garbage in the list.
//

		
/*
		// test rqRRQr
		try {
			Move move;
			game.reset();
			King king = (King) board.getPiece(5, 8);

			// move white queen and rooks
			move = movePool.obtainMove(board.getPiece(4, 1), 2, 4, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(board.getPiece(1, 1), 1, 4, null);
			game.movePiece(move);
			movePool.release(move);
			move = movePool.obtainMove(board.getPiece(8, 1), 8, 4, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black queen and rooks
			move = movePool.obtainMove(board.getPiece(4, 8), 6, 4, null);
			game.movePiece(move);
			movePool.release(move);
						
			move = movePool.obtainMove(board.getPiece(1, 8), 3, 4, null);
			game.movePiece(move);
			movePool.release(move);
			move = movePool.obtainMove(board.getPiece(8, 8), 5, 4, null);
			game.movePiece(move);
			movePool.release(move);				
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/

		// test king moves
		try {
			Move move;
			game.reset();
			King king = (King) board.getPiece(5, 8);

			// remove pawns
			board.removePiece(board.getPiece(5, 2));
			board.removePiece(board.getPiece(6, 2));
			board.removePiece(board.getPiece(5, 7));

			// move black king
			move = movePool.obtainMove(board.getPiece(5, 8), 5, 6, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black queen
			move = movePool.obtainMove(board.getPiece(4, 8), 5, 8, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move white pawn
			move = movePool.obtainMove(board.getPiece(1, 2), 1, 3, null);
			game.movePiece(move);
			movePool.release(move);
			
			king.toString();
				
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}

/*
		// test castling
		try {
			Move move;
			game.reset();
			King bKing = (King) board.getPiece(5, 8);

			// remove kingside bishop and knight & pawns
			board.removePiece(board.getPiece(6, 8));
			board.removePiece(board.getPiece(7, 8));
			board.removePiece(board.getPiece(6, 7));
			board.removePiece(board.getPiece(7, 7));

			// remove queenside bishop and knight and queen & pawns
			board.removePiece(board.getPiece(2, 8));
			board.removePiece(board.getPiece(3, 8));
			board.removePiece(board.getPiece(4, 8));
			board.removePiece(board.getPiece(3, 7));
			board.removePiece(board.getPiece(4, 7));

			// move white bishop
			move = movePool.obtainMove(board.getPiece(6, 1), 2, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			bKing.toString();
				
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/
/*
		// test skewers
		try {
			Move move;
			game.reset();
			King wKing = (King) board.getPiece(5, 1);

			// remove pawns
			board.removePiece(board.getPiece(5, 2));

			// move king
			move = movePool.obtainMove(wKing, 5, 5, null);
			game.movePiece(move);
			movePool.release(move);			
		
			// move black rook
			move = movePool.obtainMove(board.getPiece(8, 8), 1, 5, null);
			game.movePiece(move);
			movePool.release(move);
						
			// move white pawn
			move = movePool.obtainMove(board.getPiece(8, 2), 8, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black queen
			move = movePool.obtainMove(board.getPiece(4, 8), 3, 5, null);
			game.movePiece(move);
			movePool.release(move);

			// move white pawn
			move = movePool.obtainMove(board.getPiece(7, 2), 7, 5, null);
			game.movePiece(move);
			movePool.release(move);
	
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/
/*
		// test rook/bishop/queen moves, immobile, pinned, discovered
		try {
			Move move;
			game.reset();
			King wKing = (King) board.getPiece(5, 1);

			// remove pawns
			board.removePiece(board.getPiece(5, 2));
			board.removePiece(board.getPiece(5, 7));
			board.removePiece(board.getPiece(7, 2));
			board.removePiece(board.getPiece(2, 7));
			board.removePiece(board.getPiece(3, 2));

			// move pawns
			move = movePool.obtainMove(board.getPiece(4, 7), 4, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(board.getPiece(3, 7), 3, 4, null);
			game.movePiece(move);
			movePool.release(move);
			
			
			// move black rook
			move = movePool.obtainMove(board.getPiece(8, 8), 5, 7, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black bishop 
			move = movePool.obtainMove(board.getPiece(6, 8), 5, 6, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black queen 
			move = movePool.obtainMove(board.getPiece(4, 8), 6, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move white piece
			move = movePool.obtainMove(board.getPiece(1, 2), 1, 3, null);
			game.movePiece(move);
			movePool.release(move);
	
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/		
/*
		// test knight pinned, immobile, and discovered check
		//     Q..   .R    Q. .
		//     .n.  .n.    .N.
		//      .k   .k     .k

		try {
			Move move;
			game.reset();
			King wKing = (King) board.getPiece(5, 1);

			// remove pawn in front of king
			board.removePiece(board.getPiece(5, 2));
			board.removePiece(board.getPiece(4, 2));

			// move white knight to d2
			move = movePool.obtainMove(board.getPiece(2, 1), 4, 2, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black queen to c3	
			move = movePool.obtainMove(board.getPiece(4, 8), 3, 3, null);
			game.movePiece(move);
			movePool.release(move);

			// move f3
			move = movePool.obtainMove(board.getPiece(6, 2), 6, 3, null);
			game.movePiece(move);
			movePool.release(move);
			
			// move black bishop to h4
			move = movePool.obtainMove(board.getPiece(6, 8), 8, 4, null);
			game.movePiece(move);
			movePool.release(move);

			// move black rook to e4
			move = movePool.obtainMove(board.getPiece(8, 8), 5, 4, null);
			game.movePiece(move);
			movePool.release(move);

			// move a white piece to make it black's turn
			move = movePool.obtainMove(board.getPiece(1, 2), 1, 3, null);
			game.movePiece(move);
			movePool.release(move);
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/	

/*
		// test placesKingInCheck()
		try {
			Move move;
			game.reset();
			King wKing = (King) board.getPiece(5, 1);
			King bKing = (King) board.getPiece(5, 8);

			// move queens to same rank as opposite kings
			// while moving white king to 4th file
			move = movePool.obtainMove(board.getPiece(4, 1), 5, 4, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(wKing, 4, 1, null);
			game.movePiece(move);
			movePool.release(move);

			move = movePool.obtainMove(board.getPiece(4, 8), 4, 5, null);
			game.movePiece(move);
			movePool.release(move);

			// move enemy pieces to attacking positions for guarding pawn
			move = movePool.obtainMove(board.getPiece(2, 8), 3, 3, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(board.getPiece(2, 1), 4, 6, null);
			game.movePiece(move);
			movePool.release(move);
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/
/*
		// test kingInCheck()
		// try check with each piece, both kings
		try {
			Move move;
			game.reset();
			King wKing = (King) board.getPiece(5, 1);
			King bKing = (King) board.getPiece(5, 8);

			game.fireBoardUpdated();

			// first, try pawn check

			// put kings in the middle of the board, very realistic like
			move = movePool.obtainMove(wKing, 7, 4, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bKing, 2, 4, null);
			game.movePiece(move);
			movePool.release(move);

			Piece bp = board.getPiece(8, 7);
			move = movePool.obtainMove(bp, 6, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			Piece wp = board.getPiece(2, 2);
			move = movePool.obtainMove(wp, 1, 3, null);
			game.movePiece(move);
			movePool.release(move);

			Debug.debugMsg("main:", Debug.INFO, "Is white king in check: " + board.kingInCheck(Piece.WHITE));
			Debug.debugMsg("main:", Debug.INFO, "Is black king in check: " + board.kingInCheck(Piece.BLACK));

			// next, try knight check
			game.reset();
			game.fireBoardUpdated();
			wKing = (King) board.getPiece(5, 1);
			bKing = (King) board.getPiece(5, 8);

			move = movePool.obtainMove(wKing, 6, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bKing, 3, 4, null);
			game.movePiece(move);
			movePool.release(move);

			Piece bk = board.getPiece(2, 8);
			move = movePool.obtainMove(bk, 4, 6, null);
			game.movePiece(move);
			movePool.release(move);
			
			Piece wk = board.getPiece(2, 1);
			move = movePool.obtainMove(wk, 1, 5, null);
			game.movePiece(move);
			movePool.release(move);

			Debug.debugMsg("main:", Debug.INFO, "Is white king in check: " + board.kingInCheck(Piece.WHITE));
			Debug.debugMsg("main:", Debug.INFO, "Is black king in check: " + board.kingInCheck(Piece.BLACK));

			// next, try bishop/queen check
			game.reset();
			game.fireBoardUpdated();
			wKing = (King) board.getPiece(5, 1);
			bKing = (King) board.getPiece(5, 8);

			move = movePool.obtainMove(wKing, 8, 8, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bKing, 8, 1, null);
			game.movePiece(move);
			movePool.release(move);

			// remove pawns
			board.removePiece(board.getPiece(7, 2));
			board.removePiece(board.getPiece(7, 7));

			//Piece wb = board.getPiece(3, 1);
			Piece wb = board.getPiece(4, 1);
			move = movePool.obtainMove(wb, 2, 7, null);
			game.movePiece(move);
			movePool.release(move);
			
			//Piece bb = board.getPiece(3, 8);
			Piece bb = board.getPiece(4, 8);
			move = movePool.obtainMove(bb, 2, 2, null);
			game.movePiece(move);
			movePool.release(move);

			// block with pawns
			//move = movePool.obtainMove(board.getPiece(3, 2), 3, 3, null);
			//game.movePiece(move);
			//movePool.release(move);

			//move = movePool.obtainMove(board.getPiece(3, 7), 3, 6, null);
			//game.movePiece(move);
			//movePool.release(move);

			Debug.debugMsg("main:", Debug.INFO, "Is white king in check: " + board.kingInCheck(Piece.WHITE));
			Debug.debugMsg("main:", Debug.INFO, "Is black king in check: " + board.kingInCheck(Piece.BLACK));

			// next, try rook/queen check
			game.reset();
			game.fireBoardUpdated();
			wKing = (King) board.getPiece(5, 1);
			bKing = (King) board.getPiece(5, 8);

			move = movePool.obtainMove(wKing, 8, 3, null);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bKing, 8, 5, null);
			game.movePiece(move);
			movePool.release(move);

			//Piece wr = board.getPiece(1, 1);
			Piece wr = board.getPiece(4, 1);
			move = movePool.obtainMove(wr, 1, 5, null);
			game.movePiece(move);
			movePool.release(move);
			
			//Piece br = board.getPiece(8, 8);
			Piece br = board.getPiece(4, 8);
			move = movePool.obtainMove(br, 1, 3, null);
			game.movePiece(move);
			movePool.release(move);

			// block with pawns
			//move = movePool.obtainMove(board.getPiece(3, 2), 3, 3, null);
			//game.movePiece(move);
			//movePool.release(move);

			//move = movePool.obtainMove(board.getPiece(3, 7), 3, 5, null);
			//game.movePiece(move);
			//movePool.release(move);
			
			Debug.debugMsg("main:", Debug.INFO, "Is white king in check: " + board.kingInCheck(Piece.WHITE));
			Debug.debugMsg("main:", Debug.INFO, "Is black king in check: " + board.kingInCheck(Piece.BLACK));
			
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}
*/
/*
		// test en passant, pawn promotion, and castling all in one fell swoop
		// both sides, no less
		//for (int i=0; i<5000; i++) 
		
		try {
			game.reset();

			//movePool.isPoolEmpty();

			// test the hot-damn piece pools
			//board.resetBoard();
			//board.resetBoard();
			//board.resetBoard();

			// remove kingside knight and bishop
			//board.removePiece(board.getPiece(6, 1));
			board.removePiece(board.getPiece(7, 1));

			// remove queenside knight, bishop, and queen	
			board.removePiece(board.getPiece(2, 8));
			//board.removePiece(board.getPiece(3, 8));
			board.removePiece(board.getPiece(4, 8));

			// place a pawn to be ready for en passant	
			Piece wp = board.getPiece(2, 2);
			Move move = movePool.obtainMove(wp, 2, 5, null);
			game.movePiece(move);
			movePool.release(move);

			Piece bp = board.getPiece(7, 7);
			move = movePool.obtainMove(bp, 7, 4, null);
			game.movePiece(move);
			movePool.release(move);

			// set up pawn to be captured en-passant
			Piece wep = board.getPiece(1, 7);
			move = movePool.obtainMove(wep, 1, 5, null);
			move.setSpecialMove(Move.DOUBLE_PAWN_MOVE, 0);
			game.movePiece(move);
			movePool.release(move);

			Piece bep = board.getPiece(8, 2);
			move = movePool.obtainMove(bep, 8, 4, null);
			move.setSpecialMove(Move.DOUBLE_PAWN_MOVE, 0);
			game.movePiece(move);
			movePool.release(move);

			// do the en-passant
			move = movePool.obtainMove(wp, 1, 6, wep);
			move.setSpecialMove(Move.EN_PASSANT, 0);
			game.movePiece(move);
			movePool.release(move);

			move = movePool.obtainMove(bp, 8, 3, bep);
			move.setSpecialMove(Move.EN_PASSANT, 0);
			game.movePiece(move);
			movePool.release(move);

			// move the pawn to within promoting
			move = movePool.obtainMove(wp, 2, 7, board.getPiece(2, 7));
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bp, 7, 2, board.getPiece(7, 2));
			game.movePiece(move);
			movePool.release(move);


			// castle kingside
			Piece king = board.getPiece(5, 1);
			move = movePool.obtainMove(king, 7, 1, null);
			move.setSpecialMove(Move.KINGSIDE_CASTLE, 0);
			game.movePiece(move);
			movePool.release(move);

			// castle queenside
			king = board.getPiece(5, 8);
			move = movePool.obtainMove(king, 3, 8, null);
			move.setSpecialMove(Move.QUEENSIDE_CASTLE, 0);
			game.movePiece(move);
			movePool.release(move);

			// promote pawns	
			move = movePool.obtainMove(wp, 2, 8, null);
			move.setSpecialMove(Move.PAWN_PROMOTED, Piece.QUEEN);
			game.movePiece(move);
			movePool.release(move);
			
			move = movePool.obtainMove(bp, 7, 1, null);
			move.setSpecialMove(Move.PAWN_PROMOTED, Piece.KNIGHT);
			game.movePiece(move);
			movePool.release(move);
		}
		catch (Exception e)
		{
			Debug.debugMsg("main: ", Debug.ERROR, "caught exception: " + e);
		}

*/
	}
	public void movePiece(Move move)
		throws IllegalMoveException, PoolFullException, InvalidPieceException
	{
		board.commitMove(move);
		board.analyze();
		fireBoardUpdated();
		moves.add(move);
		
		plies++;
		color = ((plies%2)==0)?Piece.WHITE:Piece.BLACK;
		moveNum = (plies/2) + 1;
	}
	public void registerListener(GameListener gameListener)
	{
		gameListeners.add(gameListener);
	}
	public void registerMoveSource(MoveSource moveSource)
	{
		this.moveSource = moveSource;
	}
	public void reset()
		throws PoolFullException, IllegalMoveException
	{
		plies = 0;
		moveNum = 1;
		color = Piece.WHITE;

		// release & clear all moves in the moves list
		MovePool movePool = board.getMovePool();
		for (int i=0; i<moves.size(); i++)
			movePool.release((Move) moves.get(i));
		moves.clear();
		
		board.resetBoard();
		fireGameReset();
		board.analyze();
		fireBoardUpdated();
	}
	public void setGamesRootNode(PGN_Start root)
	{
		gamesRoot = root;
	}
}
