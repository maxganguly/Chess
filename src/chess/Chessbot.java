package chess;

import chess.Control.Piecetype.Team;

public interface Chessbot {
	/**
	 * Gives an legal Move in Long Algorithmic Chess Notation
	 * @return the Move in Lacn
	 */
	public String getMoveLacn();
	/**
	 * Gives the Model the Bot decided as best
	 * @returns the best Move the Bot found
	 */
	public Move getMove();
	/**
	 * Gives the Bot a move to compute (Move as Move Object)
	 * @param m the Move the other player made
	 */
	public void recieveMove(Move m);
	/**
	 * Gives the Bot the move the other player made to compute(Move as Long Algorithmic Chess Notation)
	 * @param move the Move the other Player made
	 */
	public void recieveMove(String move);
	/**
	 * Gives the Team the Bot is
	 * @return either Team.BLACK or Team.WHITE
	 */
	public Team getTeam();
}
