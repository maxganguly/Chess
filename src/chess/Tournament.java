package chess;

import java.util.concurrent.atomic.AtomicLong;

import chess.Control.Piecetype.Team;

public class Tournament implements OnGameOver {
	private int win, draw, loss;
	private boolean ended;
	private long[] time;
	private boolean switchedsides;
	private TimeCounter timeWhite, timeBlack;

	public Tournament(Chessbot cha, Chessbot chb, long[] time, int matches, boolean printprogress) {
		Move move = null;
		int result;
		this.time = time;
		Chessbot currentai;
		Chessbot[] bots = new Chessbot[3];
		bots[0] = cha;
		bots[1] = chb;
		TimeCounter currenttimer;
		timeWhite = new TimeCounter(new AtomicLong(time[0]), Team.WHITE, null, this, time[2]);
		timeBlack = new TimeCounter(new AtomicLong(time[1]), Team.BLACK, null, this, time[2]);
		Team currentPlayer = Team.WHITE;
		Model m = new Model();

		while (matches > 0) {

			ended = false;

			while (!ended) {
				currentai = (currentPlayer == Team.WHITE) ? bots[0] : bots[1];
				currenttimer = (currentPlayer == Team.WHITE) ? timeWhite : timeBlack;
				currenttimer.startCounter();
				// Give Move to other Bot if the bot is not playing both sides
				if (move != null && bots[0] != bots[1]) {
					currentai.recieveMove(move);
				}

				move = currentai.getMove();
				result = m.isCheckmate(currentPlayer);
				if (move == null) {
					if (result == -1)
						result = m.isCheckmate(currentPlayer == Team.WHITE ? Team.BLACK : Team.WHITE);

					// System.out.println(m.getFen());
				}
				if (m.draw())
					result = 0;

				if (result != -1) {
					if (result == 0) {
						gameOver(Team.NONE);
					} else {
						if ((currentPlayer == Team.BLACK))
							gameOver(Team.WHITE);
						else
							gameOver(Team.BLACK);
					}
					break;
				}
				// MOVEOUTPUT
				// System.out.println(move.getLacn());
				m.move(move);
				currentai.recieveMove(move);
				currenttimer.stopCounter();
			}
			switchedsides = !switchedsides;
			bots[2] = bots[0];
			bots[0] = bots[1];
			bots[1] = bots[2];
			if (printprogress) {
				System.out.println(m.getFen());
				System.out.println("Wins: " + win + ", Draws: " + draw + " Losses: " + loss);
			}
			bots[0].loadfromFen(Control.startFEN);
			bots[1].loadfromFen(Control.startFEN);
			m.load(Control.startFEN);
			matches--;

		}
		System.out.println("Wins: " + win + ", Draws: " + draw + " Losses: " + loss);
	}

	public void gameOver(Team t) {
		ended = true;
		if (time != null) {
			timeWhite.stopCounter();
			timeBlack.stopCounter();
			timeWhite.setTime(this.time[0]);
			timeBlack.setTime(this.time[1]);
		}
		if (!switchedsides) {
			if (t == Team.WHITE) {
				win++;
			} else if (t == Team.BLACK) {
				loss++;
			} else {
				draw++;
			}
		} else {
			if (t == Team.WHITE) {
				loss++;
			} else if (t == Team.BLACK) {
				win++;
			} else {
				draw++;
			}
		}
	}
}
