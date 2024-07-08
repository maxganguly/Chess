package chess;

import java.util.concurrent.atomic.AtomicLong;

import chess.Control.Piecetype.Team;

public class Start {


	public static void main(String[] args) {
		
		Model m = new Model();
		//View v = new View();
		AtomicLong alw = new AtomicLong(900_000);
		AtomicLong alb = new AtomicLong(900_000);
		Chessbot cb = new RandomChessbot(new Model(m,false));
		//Control c = new Control(m,v,alw,alb,10_000,cb,cb);
		
		Tournament t = new Tournament(cb, cb, new long[] {900_000,900_000,0}, 200,false);
	}

}
