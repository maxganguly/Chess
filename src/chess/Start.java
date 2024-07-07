package chess;

import java.util.concurrent.atomic.AtomicLong;

import chess.Control.Piecetype.Team;

public class Start {


	public static void main(String[] args) {
		Model m = new Model();
		View v = new View();
		AtomicLong alw = new AtomicLong(900_000);
		AtomicLong alb = new AtomicLong(900_000);
		Chessbot cbb = new RandomChessbot(new Model(m,false), Team.BLACK);
		Chessbot cbw = new RandomChessbot(new Model(m,false), Team.WHITE);
		Control c = new Control(m,v,alw,alb,10_000,null,cbb);
	}

}
