package chess;

import java.util.concurrent.atomic.AtomicLong;


public class Start {


	public static void main(String[] args){
		String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1";
		Model m = new Model();//Model.loadfromFen(fen);
		View v = new View();
		AtomicLong alw = new AtomicLong(900_000);
		AtomicLong alb = new AtomicLong(900_000);
		
		/*
		Chessbot cb = new RandomChessbot(new Model(m,false));
		Chessbot rand = new RandomChessbot(m);
		Control c = new Control(m,v,alw,alb,10_000,cb,rand);
		*/
		Control c = new Control(m,v,alw,alb,10_000,null,null);
		
		//Tournament t = new Tournament(cb, cb, new long[] {900_000,900_000,0}, 200,false);
		
	}
}
