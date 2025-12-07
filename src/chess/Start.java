package chess;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import chess.botterBot.BotterBot;


public class Start {


	public static void main(String[] args){
		String fen = Control.startFEN;
		fen = getM3FEN();
		
		System.out.println(fen);
		Model m = Model.loadfromFen(fen);
		View v = new View();
		AtomicLong alw = new AtomicLong(900_000);
		AtomicLong alb = new AtomicLong(900_000);
		
		
		
		/*
		Chessbot cb = new RandomChessbot(new Model(m,false));
		Chessbot rand = new RandomChessbot(m);
		Control c = new Control(m,v,alw,alb,10_000,cb,rand);
		*/
		Chessbot cbw = new BotterBot(fen, 10_000, (byte) 6, true, true);
		Chessbot cbb = new BotterBot(fen, 10_000, (byte) 6, false, false);
		Control c = new Control(m,v,alw,alb,10_000, cbw, cbb);
		
		//Tournament t = new Tournament(cb, cb, new long[] {900_000,900_000,0}, 200,false);
		
	}
	
	public static String getM2FEN() {
		return getRandomLine("m2.txt");
	}
	
	public static String getM3FEN() {
		return getRandomLine("m3.txt");
	}
	
	public static String getM4FEN() {
		return getRandomLine("m4.txt");
	}
	
	public static String getRandomLine(String file) {
		String line = null;
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
		    long size = raf.length();
		    linepicker:
		    while (line == null) {
		        raf.seek(ThreadLocalRandom.current().nextLong(size));
		        int b = raf.read();
		        if (b == -1) continue;
		        while (((b = raf.read()) & (0xFF)) != '\r' && (b  & (0xFF)) != '\n') {
		        	if(b == -1)
		        		continue linepicker;
		        }
		        line = raf.readLine();
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return line;
	}
}
