package chess.botterBot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import chess.UCIMove;

public class MoveGenerator {
	public static byte SEARCHDEPTH = 40;
	private Process stockfish = null;
	private BufferedWriter bw = null;
	private BufferedReader br = null;
	private HashMap<String, BotterMove> moves = new HashMap<String, BotterMove>();

	public MoveGenerator(String[] stockfishPath) {
		moves = new HashMap<String, BotterMove>();
		Runtime rt = Runtime.getRuntime();
		try {
			stockfish = rt.exec(stockfishPath);
			br = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(stockfish.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void calculate(byte depth) {
		File f = new File("./precomputed.txt");
		FileWriter fw = null;
		try {

			fw = new FileWriter(f);
			String start = "position startpos moves";
			BotterBoard bb = new BotterBoard();
			allBest(bb, start, depth);
			String text;
			for (String key : moves.keySet()) {
				if (moves.get(key) == null) {
					System.out.println("What?");
				}
				text = "precomputed.put(\"" + key + "\"," + ((BotterMove) moves.get(key)).constructor() + ");\n";
				System.out.print(text);
				fw.write(text);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void close() {
		stockfish.destroy();

		try {
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void allBest(BotterBoard board, String position, byte depthLeft) throws IOException {
		if (depthLeft == 0)
			return;
		String best;
		String pos;
		int i = 1;
		BotterMove[] movelist = board.getAllMoves(board.isCurrentPlayerWhite(), false);
		for (BotterMove bm : movelist) {
			board.makeMove(bm, false);
			System.out.println("DepthLeft: " + depthLeft + " " + i + "/" + movelist.length);
			pos = position + ' ' + bm.fromTo();
			if (!moves.containsKey(board.getFenBoardOnly())) {
				best = getbest(pos);
				// System.out.println(best);
				moves.put(board.getFenBoardOnly(), board.toBotterMove(new UCIMove(best)));
				System.out.println("position: " + pos + " best response: " + best);
			}
			if (depthLeft != 0)
				allBest(board, pos, (byte) (depthLeft - 1));
			board.unmakeMove();
			i++;
		}
	}

	public String getbest(String position) throws IOException {
		boolean read = true;
		String text;
		bw.write(position);
		bw.newLine();
		bw.write("go depth " + SEARCHDEPTH);
		bw.newLine();
		bw.flush();
		while (read) {
			while (br.ready()) {
				text = br.readLine();
				if (text.substring(0, 8).equals("bestmove")) {
					return text.substring(9, 13);
				}
			}
			try {
				Thread.currentThread().sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

}
