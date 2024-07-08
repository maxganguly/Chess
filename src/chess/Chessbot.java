package chess;

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
	 * Load a new Board and Data from the given fen
	 * @param fen
	 */
	public void loadfromFen(String fen);
}
