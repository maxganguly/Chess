package chess;

import chess.Control.Piecetype.Team;

public interface Chessbot {
	/**
	 * Gives an legal Move in Long Algorithmic Chess Notation
	 * @return the Move in Lacn
	 */
	public String getMoveLacn();
	/**
	 * Gives an legal Move
	 * @return a Move as Object
	 */
	public Move getMove();
	/**
	 * Gives a Move to the 
	 * @param m
	 */
	public void recieveMove(Move m);
	public void recieveMove(String move);
	public Team getTeam();
}
