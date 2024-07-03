package chess;

import java.util.Arrays;

import chess.Control.Piecetype.Team;

public class Start {


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Model m = new Model();
		View v = new View();
		Chessbot cbb = new RandomChessbot(m, Team.BLACK);
		Chessbot cbw = new RandomChessbot(m, Team.WHITE);
		Control c = new Control(m,v,cbw,cbb);
	}

}
