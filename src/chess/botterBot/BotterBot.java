package chess.botterBot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import chess.Chessbot;
import chess.Control;
import chess.Move;
import chess.UCIMove;
import chess.Control.Piecetype;
import chess.Control.Piecetype.Team;

public class BotterBot implements Chessbot {
	private BotterBoard board;
	private static final int cachesize = 1_000_003; // Prime
	public static final short SHORTMIN = (Short.MIN_VALUE+1);	//Needed to allow inverting
	private HashMap<String, Short> fencache;
	// private long[] cache;
	private short[] cache;
	private int maxTimePerMove;
	private byte maxDepthperMove;
	private boolean stopCalculation;
	private boolean shouldcache;
	private boolean arraycache;
	private Timer tim;
	
	public BotterBot() {
		this(5000, (byte) 5);
	}

	public BotterBot(int maxTimePerMove, byte maxDepthPerMove) {
		this(maxTimePerMove, maxDepthPerMove, false);
	}

	public BotterBot(int maxTimePerMove, byte maxDepthPerMove, boolean shouldcache) {
		this(maxTimePerMove, maxDepthPerMove, shouldcache, true);
	}

	public BotterBot(int maxTimePerMove, byte maxDepthPerMove, boolean shouldcache, boolean arraycache) {
		this(null, maxTimePerMove, maxDepthPerMove, shouldcache, arraycache);
	}
	public BotterBot(String fen, int maxTimePerMove, byte maxDepthPerMove, boolean shouldcache, boolean arraycache) {
		if (maxDepthPerMove < 1)
			maxTimePerMove = -1;
		if(fen == null)
			this.board = new BotterBoard();
		else
			this.board = new BotterBoard(fen);
		if (Calculations.ZobristList == null)
			Calculations.initZobristTable(new PRNG(42));
		this.maxDepthperMove = maxDepthPerMove;
		this.maxTimePerMove = maxTimePerMove;
		this.shouldcache = shouldcache;
		this.arraycache = arraycache;
		if (shouldcache) {
			if (arraycache)
				this.cache = new short[this.cachesize];
			else
				this.fencache = new HashMap<>();
		}
	}

	@Override
	public String getMoveLacn() {
		return getBotterMove().getLacn();
	}

	@Override
	public Move getMove() {
		return new Move(getMoveLacn());
	}

	@Override
	public UCIMove getUCIMove() {
		return UCIMove.lacntoUCI(getMoveLacn());
	}

	public BotterMove getBotterMove() {
		this.stopCalculation = false;
		if (maxTimePerMove != -1) {
			this.tim = new Timer("Move timer", true);
			tim.schedule(new TimerTask() {

				@Override
				public void run() {
					System.out.println("Stop calc");
					stopCalculation = true;
				}
			}, maxTimePerMove);
		}
		chooseBest(maxDepthperMove, board.isCurrentPlayerWhite());
		if (maxTimePerMove != -1) {
			tim.cancel();
		}
		System.out.println("Given move: " + currentbest.getLacn());
		return currentbest;
	}

	@Override
	public void recieveMove(Move m) {
		this.board.makeMove(board.toBotterMove(m), true);
		System.out.println("The current Evaluation is: " + board.evalAll());
	}

	@Override
	public void recieveMove(String lacn) {
		this.board.makeMove(board.toBotterMove(lacn), true);
		System.out.println("The current Evaluation is: " + board.evalAll());
	}

	@Override
	public void recieveMove(UCIMove m) {
		this.board.makeMove(board.toBotterMove(m), true);
		System.out.println("The current Evaluation is: " + board.evalAll());
	}

	public void recieveBotterMove(BotterMove move) {
		System.out.println("Recieved a move");
		if (!this.board.makeMove(move, true)) {
			System.err.print("Illegal Move: " + move.getLacn());
		}
	}

	private BotterMove currentbest;
	private short currentbestEval;
	private int[] currentbestEvalSplit;
	private int collisionst;
	private int collisionsf;

	public void chooseBest(byte depth, boolean iswhite) {
		BotterMove[] moves = this.board.getAllMoves(iswhite, true);
		currentbest = moves[0];
		short best = iswhite ? SHORTMIN : Short.MAX_VALUE;
		currentbestEval = best;
		// *
		// Should or not clear cache for each position
		if (this.shouldcache) {
			cache = new short[this.cachesize];
			this.fencache = new HashMap<String, Short>();
			// System.gc(); // Questionable
		}
		// */
		collisionst = 0;
		collisionsf = 0;
		short score;
		short localbest = best;
		BotterMove localbestMove = moves[0];
		depth:
		for (byte i = 1; i < this.maxDepthperMove && !stopCalculation; i++) {
			System.out.println("Depth: " + i);
			if (this.shouldcache) {
				if (arraycache)
					cache = new short[this.cachesize];
				else
					this.fencache = new HashMap<String, Short>();
				System.gc(); // Questionable
			}
			localbest = best;// reset the localbest to find the acutal best and
																	// not be biased be shallow moves
			int[][] temp = new int[1][];
			for (BotterMove bm : moves) {
				if(!board.makeMove(bm, true)) {
					System.out.println("Something is very wrong");
				}
				if (stopCalculation) {
					board.unmakeMove();
					
					break depth;
				}

				score = (short)negamax(board, SHORTMIN, Short.MAX_VALUE, i, !iswhite, temp);// (short)(iswhite?board.evalAll():-board.evalAll());//alphaBeta(Short.MIN_VALUE,
				if(i%2 == 1) {
					System.out.print("Inverted: ("+score+") \t ");
					score = (short)-score;
				}
				System.out.println("Depth: " + i + " Move: " + bm.toString() + " with score: " + score +"("+ Arrays.toString(temp[0]) +")");
				
				
				board.unmakeMove();
				if ((score > localbest && iswhite) || (score < localbest && !iswhite)) {
					if ((score > 20_000 && iswhite) || (score < -20_000 && !iswhite)) {//if there is an checkmate found
						currentbest = bm;
						currentbestEval = score;
						break depth;
					}
					localbest = score;
					localbestMove = bm;
				}
			}
			int occupied = 0;
			if (this.cache != null) { // Is the cache is used
				for (int j = 0; j < cachesize; j++) {
					if (cache[j] != 0)
						occupied++;
				}
				System.out.println(occupied + " Spaces used, " + collisionst + " collisions true and " + collisionsf
						+ " collisions false");
			}
			// System.out.println("Current best move is: " + localbestMove.getLacn() + "
			// with an evaluation of: " + currentbestEval);
			System.out.println("Current best move is: "+ localbestMove + " with an evaluation of: " + localbest);
			currentbest = localbestMove;
			currentbestEval = localbest;
			localbest = best;
			if ((currentbestEval > 10_000 && iswhite) || (currentbestEval < -10_000 && !iswhite)) {//if there is an checkmate found
				break depth;
			}
			
		}
		System.out.println(
				"The Chosen best move is: " + localbestMove.getLacn() + " with an evaluation of: " + currentbestEval);
	}

	/**
	 * A method to evaluate a Position using Minimax
	 * 
	 * @param b         the Position to be evaluated
	 * @param alpha     the current best result (When called use Short.MAX_VALUE)
	 * @param beta      the current worst result (best for enemy) (When called use
	 *                  Short.MIN_VALUE)
	 * @param depthLeft depth to look (depth 0 results in bord.evalAll();)
	 * @param iswhite   given turn for Player white or not
	 * @return
	 */
	public short eval_deep(BotterBoard b, short alpha, short beta, byte depthLeft, boolean isWhite) {
		if (depthLeft == 0)
			return (short) (isWhite ? b.evalAll() : -b.evalAll());
		short score = alpha;
		BotterMove[] moves = b.getAllMoves(isWhite, true);
		//order(moves);
		long zobrist;
		for (BotterMove bm : moves) {
			b.makeMove(bm, false);
			score = (short) -eval_deep(b, (short) -beta, (short) -alpha, (byte) (depthLeft - 1), !isWhite);
			// System.out.println(b.zobristhash());
			b.unmakeMove();
			if (score > alpha) {
				alpha = score;
				if (score > alpha)
					alpha = score; // alpha acts like max in MiniMax
			}
			if (score >= beta)
				return alpha; // fail soft beta-cutoff, existing the loop here is also fine
		}
		return score;
	}

	public short negamax(BotterBoard b, short alpha, short beta, int depthleft, boolean whitesTurn, int[][] evalSplit) {
		// System.out.println("Depth: "+depthleft+"\nalpha: "+alpha+"\nbeta: "+beta);
		if (depthleft == 0) {
			evalSplit[0] = b.evalAllSplit();
			return (short) b.evalAll();
		}

		BotterMove[] moves = b.getAllMoves(whitesTurn, true);
		order(moves);
		short score = SHORTMIN;
		long zobrist;
		int zobristmodcachesize = 0;
		for (BotterMove bm : moves) {
			b.makeMove(bm, false);
			// not negating the result of the function as the evaluation is based on white
			int[][] temp = new int[1][];
			short tempscore = (short) -negamax(b, (short) -beta, (short) -alpha, depthleft - 1, !whitesTurn, temp);
			
			score = max(score, tempscore);
			if(score == tempscore) {
				evalSplit[0] = temp[0];
			}
			alpha = max(alpha, score);
			b.unmakeMove();
			if (alpha >= beta)// if alpha is greater than beta that means at an earlier calculation a more
								// optimal move for the current player/moveset has been found
				break;
		}
		if(moves == null || moves.length == 0) {
			evalSplit[0] = b.evalAllSplit();
			return (short) -b.evalAll();
		}
		return score;
	}


	public short minimax(BotterBoard b, int depthleft, boolean whitesTurn) {
		// System.out.println("Depth: "+depthleft+"\nalpha: "+alpha+"\nbeta: "+beta);
		if (depthleft == 0)
			return (short) b.evalAll();

		short bestValue = SHORTMIN;
		BotterMove[] moves = b.getAllMoves(whitesTurn, true);
		order(moves);
		short score;
		long zobrist;
		int zobristmodcachesize = 0;
		for (BotterMove bm : moves) {
			b.makeMove(bm, false);
			if (shouldcache) {
				zobrist = b.zobristhash();
				zobristmodcachesize = (int) (zobrist % cachesize);
				while (zobristmodcachesize < 0) { // maybe an if wouldwork instead, java makesnegative numbers to
													// negative modulo
					zobristmodcachesize = (zobristmodcachesize + cachesize) % cachesize;
				}
				if (cache[zobristmodcachesize] != 0) {
					System.out.println("Cached");
					b.unmakeMove();
					return cache[zobristmodcachesize];
				}
			}
			score = (short) -minimax(b, depthleft - 1, !whitesTurn);

			b.unmakeMove();
			if (score > bestValue) {
				
				bestValue = score;
			}
		}
		return bestValue;
	}

	/*
	 * Old minimax with pruning public short alphaBeta(BotterBoard b, short alpha,
	 * short beta, int depthleft, boolean whitesTurn, boolean shouldcache) { //
	 * System.out.println("Depth: "+depthleft+"\nalpha: "+alpha+"\nbeta: "+beta); if
	 * (depthleft == 0) return (short) b.evalAll(); short bestValue = whitesTurn ?
	 * Short.MIN_VALUE : Short.MAX_VALUE; BotterMove[] moves =
	 * b.getAllMoves(whitesTurn, true); order(moves); short score; long zobrist; int
	 * zobristmodcachesize = 0; for (BotterMove bm : moves) { b.makeMove(bm, false);
	 * if (shouldcache) { if (arraycache) { zobrist = b.zobristhash();
	 * zobristmodcachesize = ((int)(zobrist%cachesize)+cachesize)%cachesize;
	 * 
	 * if (cache[zobristmodcachesize] != 0) { short tempscore = (short) alphaBeta(b,
	 * alpha, beta, depthleft - 1, !whitesTurn, shouldcache);
	 * if(cache[zobristmodcachesize] != tempscore) { //System.out.println("Cached "
	 * + cache[zobristmodcachesize] + " at " + zobristmodcachesize +
	 * " not equals to real " + tempscore); collisionsf++; }else collisionst++;
	 * b.unmakeMove(); return cache[zobristmodcachesize]; } } else { String pos =
	 * b.getFen(); pos = pos.substring(0, pos.indexOf(' ')); if
	 * (this.fencache.containsKey(pos)) { // Cache breaks higher depth
	 * b.unmakeMove(); return fencache.get(pos); } }
	 * 
	 * } score = (short) alphaBeta(b, alpha, beta, depthleft - 1, !whitesTurn,
	 * shouldcache); if (shouldcache) { if (arraycache) { zobrist = b.zobristhash();
	 * cache[zobristmodcachesize] = (short) score; } else { String pos = b.getFen();
	 * pos = pos.substring(0, pos.indexOf(' ')); fencache.put(pos, score); } }
	 * b.unmakeMove(); if (whitesTurn) { bestValue = max(bestValue, score); alpha =
	 * max(alpha, score);
	 * 
	 * } else { bestValue = min(bestValue, score); beta = min(beta, score); } if
	 * (beta <= alpha) // failsoft cutoff, alpha, beta pruning break; } //
	 * System.out.println("Best: " + bestValue); return bestValue; } //
	 */

	/*
	 * older minimax public short minimax(BotterBoard b, int alpha, int beta, int
	 * depthleft, boolean whitesTurn, boolean shouldcache) { //
	 * System.out.println("Depth: "+depthleft+"\nalpha: "+alpha+"\nbeta: "+beta); if
	 * (depthleft == 0) return (short) b.evalAll();
	 * 
	 * short bestValue = Short.MIN_VALUE; BotterMove[] moves =
	 * b.getAllMoves(whitesTurn, true); order(moves); short score; long zobrist; int
	 * zobristmodcachesize = 0; for (BotterMove bm : moves) { b.makeMove(bm, false);
	 * if (shouldcache) { zobrist = b.zobristhash(); zobristmodcachesize = (int)
	 * (zobrist % cachesize); while (zobristmodcachesize < 0) { // maybe an if
	 * wouldwork instead, java makesnegative numbers to // negative modulo
	 * zobristmodcachesize = (zobristmodcachesize + cachesize) % cachesize; } if
	 * (cache[zobristmodcachesize] != 0) { System.out.println("Cached");
	 * b.unmakeMove(); return cache[zobristmodcachesize]; } } score = (short)
	 * -minimax(b, -beta, -alpha, depthleft - 1, !whitesTurn, shouldcache); if
	 * (shouldcache) { zobrist = b.zobristhash(); cache[zobristmodcachesize] =
	 * (short) score; } b.unmakeMove(); if (score > bestValue) { bestValue = score;
	 * if (score > alpha) alpha = score; // alpha acts like max in MiniMax } if
	 * (score >= beta) return bestValue; // fail soft beta-cutoff, existing the loop
	 * here is also fine } return bestValue; } //
	 */

	public int quiesce(int alpha, int beta, BotterBoard b, boolean whitesTurn, byte depth) {
		int stand_pat = b.evalAll();
		if (depth == 0)
			return stand_pat;
		if (stand_pat >= beta)
			return beta;
		if (alpha < stand_pat)
			alpha = stand_pat;
		BotterMove[] bm = b.getAllMoves(whitesTurn, true);
		int score;
		for (BotterMove m : bm) {
			if (!m.captures())
				continue;
			b.makeMove(m, false);
			score = -quiesce(-beta, -alpha, b, !whitesTurn, (byte) (depth - 1));
			b.unmakeMove();

			if (score >= beta)
				return beta;
			if (score > alpha)
				alpha = score;
		}
		return alpha;
	}

	/*
	 * Orders by sorting for captures
	 */
	public void order(BotterMove[] unordered) {
		BotterMove temp;
		byte insert = (byte) 0;

		for (int i = 0; i < unordered.length; i++) {
			if (unordered[i].captures()) {
				temp = unordered[i];
				unordered[i] = unordered[insert];
				unordered[insert] = temp;
				insert++;
			}
		}

	}

	@Override
	public void loadfromFen(String fen) {
		board = new BotterBoard(fen);
		System.out.println("loaded: " + fen);
	}

	public void printhash() {
		System.out.println(this.board.zobristhash());
	}

	class MoveChoice {
		public BotterMove m;
		public short eval;

		public MoveChoice(BotterMove m, short eval) {
			this.m = m;
			this.eval = eval;
		}
	}

	public short max(short a, short b) {
		return (a > b) ? a : b;
	}

	public short min(short a, short b) {
		return (a < b) ? a : b;
	}
}
