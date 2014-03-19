package chess;

import java.util.*;
/**
 * Insert the type's description here.
 */
public class GameTextView implements GameListener {
	Game game;
	Board board;

	public GameTextView(Game game)
	{
		this.game = game;
		this.board = game.getBoard();
		game.registerListener(this);
	}
/**
 * boardUpdated method comment.
 *
		 !---!---!---!---!---!---!---!---!
	  8  ! r ! n ! b ! q ! k ! b ! n ! r ! 
		 !---!---!---!---!---!---!---!---!
	  7  ! p ! p ! p ! p ! p ! p ! p ! p !
		 !---!---!---!---!---!---!---!---!
	  6  !   ! . !   ! . !   ! . !   ! . !
		 !---!---!---!---!---!---!---!---!
	  5  ! . !   ! . !   ! . !   ! . !   !
		 !---!---!---!---!---!---!---!---!
	  4  !   ! . !   ! . !   ! . !   ! . !
		 !---!---!---!---!---!---!---!---!
	  3  ! . !   ! . !   ! . !   ! . !   !
		 !---!---!---!---!---!---!---!---!
	  2  ! P ! P ! P ! P ! P ! P ! P ! P !
		 !---!---!---!---!---!---!---!---!
	  1  ! R ! N ! B ! Q ! K ! B ! N ! R !
		 !---!---!---!---!---!---!---!---!
		   a   b   c   d   e   f   g   h
 */
public boolean boardUpdated() {
	//String boundaryLine = "		 !---!---!---!---!---!---!---!---!";
	int rank, file;
	int bottomRank;
	Piece piece;
	int attackedByBlack, attackedByWhite;

	System.out.println();
	//System.out.println(boundaryLine);

	bottomRank = 0;
	
	for (rank=8; rank>=0; rank--)
	{
		// draw boundary line with attack numbers
		System.out.print("	     !");
		for (file=1; file<=8; file++)
		{
			if (rank > 0)
			{
				attackedByBlack = board.getSquare(file, rank).attackedByBlack;
				if (attackedByBlack == -1)
					System.out.print("?-");
				else if (attackedByBlack == 0)
					System.out.print("--");
				else
				{
					System.out.print(attackedByBlack);
					if (attackedByBlack < 10)
						System.out.print('-');
				}
			}
			else
				System.out.print("--");

			if (bottomRank > 0)
			{
				attackedByWhite = board.getSquare(file, bottomRank).attackedByWhite;
				if (attackedByWhite == -1)
					System.out.print("?!");
				else if (attackedByWhite == 0)
					System.out.print("-!");
				else
				{
					System.out.print(attackedByWhite);
					if (attackedByWhite < 10)
						System.out.print('!');
				}
			}
			else
				System.out.print("-!");
		}
		System.out.println();

		// draw piece line
		if (rank > 0)
		{
			System.out.print("	  " + rank + "  ! ");
			for (file=1; file<=8; file++)
			{
				piece = board.getPiece(file, rank);
				if (piece != null)
					System.out.print(piece.getLetter() + " ! ");
				else 
				if ((rank + file)%2 == 0)
					System.out.print(". ! ");
				else
					System.out.print("  ! ");			
			}
			System.out.print("\n");
		}

		bottomRank = rank;
	}
	
	System.out.println("		   a   b   c   d   e   f   g   h");
	System.out.println();
	return true;
}
/**
 * gameOver method comment.
 */
public boolean gameOver() {
	return false;
}
/**
 * gameReset method comment.
 */
public boolean gameReset() {
	return false;
}
}
