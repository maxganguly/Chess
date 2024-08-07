package chess;


import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JLabel;

import chess.Control.Piecetype.Team;

public class Control {
	private Model m;
	private View v;
	private List<Move> lm;
	private Team currentPlayer;
	private Chessbot aiWhite, aiDark;
	private boolean allowPLayer;
	private boolean end;
	public static final String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	private boolean playonTime;
	private TimeCounter timeWhite, timeBlack;
	private long[] time;

	public enum Piecetype {
		INVALID(-1, "Invalid", "\u26A0", (char) 0, Team.NONE, false, false, false),
		EMPTY(0, "Empty Space", "", ' ', Team.NONE, false, false, false),
		WHITE_KING(1, "White King", "\u2654", 'K', Team.WHITE, false, false, true),
		WHITE_QUEEN(2, "White Queen", "\u2655", 'Q', Team.WHITE, true, true, false),
		WHITE_ROOK(3, "White Rook", "\u2656", 'R', Team.WHITE, false, true, false),
		WHITE_BISHOP(4, "White Bishop", "\u2657", 'B', Team.WHITE, true, false, false),
		WHITE_KNIGHT(5, "White Knight", "\u2658", 'N', Team.WHITE, false, false, true),
		WHITE_PAWN(6, "White Pawn", "\u2659", 'P', Team.WHITE, false, false, true),
		DARK_KING(7, "Dark King", "\u265A", 'k', Team.BLACK, false, false, true),
		DARK_QUEEN(8, "Dark Queen", "\u265B", 'q', Team.BLACK, true, true, false),
		DARK_ROOK(9, "Dark Rook", "\u265C", 'r', Team.BLACK, false, true, false),
		DARK_BISHOP(10, "Dark Bishop", "\u265D", 'b', Team.BLACK, true, false, false),
		DARK_KNIGHT(11, "Dark Knight", "\u265E", 'n', Team.BLACK, false, false, true),
		DARK_PAWN(12, "Dark Pawn", "\u265F", 'p', Team.BLACK, false, false, true);

		public final int value;
		public final String name;
		public final String symbol;
		public final boolean diagonal, orthogonal, special;
		public final char letter;

		public enum Team {
			BLACK, WHITE, NONE;
		}

		public final Team team;

		private Piecetype(int value, String name, String symbol, char letter, Team team, boolean diagonal,
				boolean orthogonal, boolean special) {
			this.value = value;
			this.name = name;
			this.symbol = symbol;
			this.letter = letter;
			this.team = team;
			this.diagonal = diagonal;
			this.orthogonal = orthogonal;
			this.special = special;
		}

	};

	public Control(Model m, View v) {
		this(m, v, null, null, 0, null, null);
	}

	public Control(Model m, View v, AtomicLong timeWhite, AtomicLong timeBlack, int addTimePerMove) {
		this(m, v, timeWhite, timeBlack, addTimePerMove, null, null);
	}

	public Control(Model m, View v, Chessbot aiWhite, Chessbot aiDark) {
		this(m, v, null, null, 0, aiWhite, aiDark);
	}

	public Control(Model m, View v, AtomicLong timeWhite, AtomicLong timeBlack, int addTimePerMove, Chessbot aiWhite,
			Chessbot aiDark) {

		this.m = m;
		this.v = v;
		if (timeWhite != null && timeBlack != null) {
			// System.out.println("Playing with time");
			playonTime = true;
			time = new long[] {timeWhite.get(),timeBlack.get(),addTimePerMove};
			JLabel[] temp = v.getTimedisplays();
			Control c = this;
			OnGameOver ogo = new OnGameOver() {
				
				@Override
				public void gameOver(Team t) {
					c.gameOver(t, true);
				}
			};
			this.timeWhite = new TimeCounter(timeWhite, Team.WHITE, temp[0], ogo, addTimePerMove);
			this.timeBlack = new TimeCounter(timeBlack, Team.BLACK, temp[1], ogo, addTimePerMove);
			this.timeWhite.start();
			this.timeBlack.start();
		}
		v.ConnectWithController(this);
		currentPlayer = Team.WHITE;
		allowPLayer = true;
		if (aiWhite != null)
			this.aiWhite = aiWhite;
		if (aiDark != null)
			this.aiDark = aiDark;
		v.playground(m.getBoard());
		if (aiWhite != null && aiDark != null) {
			allowPLayer = false;
			Move move = null;
			int result;
			Chessbot currentai = null;
			TimeCounter currenttimer =null;
			while (true) {
				if (end) {
					try {
						Thread.sleep(1000);
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				currentai = (currentPlayer == Team.WHITE) ? aiWhite : aiDark;
				if(playonTime) {
					currenttimer = (currentPlayer == Team.WHITE) ? this.timeWhite : this.timeBlack;
					currenttimer.startCounter();
				}
				if (move != null && aiWhite != aiDark) {
					currentai.recieveMove(move);
				}

				move = currentai.getMove();
				result = m.isCheckmate(currentPlayer);
				if (move == null) {
					System.out.println(result);
				}
				if (m.draw())
					result = 0;

				if (result != -1) {
					if (result == 0) {
						gameOver(Team.NONE, false);
					} else {
						if ((currentPlayer == Team.BLACK))
							gameOver(Team.WHITE, false);
						else
							gameOver(Team.BLACK, false);
					}
					continue;
				}
				// MOVEOUTPUT
				// System.out.println(move.getLacn());
				m.move(move);
				currentai.recieveMove(move);
				v.playground(m.getBoard());
				if(playonTime) {
					currenttimer.stopCounter();
				}
				currentPlayer = (currentPlayer == Team.BLACK) ? Team.WHITE : Team.BLACK;
				/*
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} //
					// */
			}
		}
	}

	public void clicked(int x, int y) {
		// System.out.println(x+":"+y);
		if (!allowPLayer || end)
			return;
		m.choose(x, y);
		// System.out.println(currentPlayer);
		if (m.getPieceOn(x, y).team != currentPlayer && !v.targetofMove(x, y))
			return;
		int[] temp;

		if (lm != null) {
			for (Move move : lm) {
				temp = move.to();
				if (temp[0] == x && temp[1] == y) {
					if (move.getPiece() == Piecetype.DARK_PAWN && temp[1] == 7 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					} else if (move.getPiece() == Piecetype.WHITE_PAWN && temp[1] == 0 && move.getPromotion() == null) {
						move.setPromotion(v.promote(currentPlayer));
					}

					if (!m.move(move))
						return;
					v.playground(m.getBoard());
					lm = null;
					v.showMoves(lm);
					if (playonTime)
						if (currentPlayer == Team.BLACK) {
							timeBlack.stopCounter();
							timeWhite.startCounter();
						} else {
							timeWhite.stopCounter();
							timeBlack.startCounter();
						}
					currentPlayer = (currentPlayer == Team.BLACK) ? Team.WHITE : Team.BLACK;
					// move.causesCheck(m.isCheck(currentPlayer));
					int result = m.isCheckmate(currentPlayer);
					if (result != -1) {
						if (result == 0) {
							gameOver(Team.NONE, false);
						} else {
							if ((currentPlayer == Team.BLACK))
								gameOver(Team.WHITE, false);
							else
								gameOver(Team.BLACK, false);
						}
						return;
					}
					// MOVEOUTPUT
					// System.out.println(move.getLacn());

					if (currentPlayer == Team.WHITE && aiWhite != null) {
						timeBlack.stopCounter();
						timeWhite.startCounter();
						// System.out.println("AI Move White");
						aiWhite.recieveMove(move);
						move = aiWhite.getMove();
						if (!m.move(move))
							return;
						aiWhite.recieveMove(move);
						currentPlayer = Team.BLACK;
						result = m.isCheckmate(currentPlayer);
						v.playground(m.getBoard());
						// MOVEOUTPUT
						// System.out.println(move.getLacn());
						timeWhite.stopCounter();
						timeBlack.startCounter();
					} else if (currentPlayer == Team.BLACK && aiDark != null) {
						timeWhite.stopCounter();
						timeBlack.startCounter();
						// System.out.println("AI Move Black");
						aiDark.recieveMove(move);
						move = aiDark.getMove();
						if (!m.move(move))
							return;
						aiDark.recieveMove(move);
						currentPlayer = Team.WHITE;
						result = m.isCheckmate(currentPlayer);
						v.playground(m.getBoard());
						// MOVEOUTPUT
						// System.out.println(move.getLacn());
						timeBlack.stopCounter();
						timeWhite.startCounter();
					}
					if (result != -1) {
						if (result == 0) {
							gameOver(Team.NONE, false);
						} else {
							if ((currentPlayer == Team.BLACK))
								gameOver(Team.WHITE, false);
							else
								gameOver(Team.BLACK, false);
						}
					}

					return;
				}
			}
		}
		lm = m.getLegalMoves();

		v.showMoves(lm);
	}

	public void restart() {
		m.load(startFEN);
		if (aiWhite != null)
			aiWhite.loadfromFen(startFEN);
		if (aiDark != null)
			aiDark.loadfromFen(startFEN);
		if (playonTime)
			v.playground(m.getBoard());
		currentPlayer = Team.WHITE;
		end = false;
	}

	// *
	public void undo() {
		m.undo();
		currentPlayer = (currentPlayer == Team.BLACK) ? Team.WHITE : Team.BLACK;
		v.playground(m.getBoard());
	}

	public void redo() {
		m.redo();
		currentPlayer = (currentPlayer == Team.BLACK) ? Team.WHITE : Team.BLACK;
		v.playground(m.getBoard());
	}

	public void printFen() {
		System.out.println(m.getFen());
	}

	public void gameOver(Team Winner, boolean wonOnTime) {
		end = true;
		if(playonTime) {
			timeWhite.stopCounter();
			timeBlack.stopCounter();
			timeWhite.setTime(this.time[0]);
			timeBlack.setTime(this.time[1]);
		}
		if (Winner == Team.NONE) {
			System.out.println("Draw");
			v.gameOver(Winner);
			return;
		}
		if (Winner == Team.BLACK) {
			if (wonOnTime)
				System.out.println("Black Won on Time");
			else
				System.out.println("Black won via Checkmate");
		} else {
			if (wonOnTime)
				System.out.println("White Won on Time");
			else
				System.out.println("White won via Checkmate");
		}
		v.gameOver(Winner);
		return;
	}
	// */
}


