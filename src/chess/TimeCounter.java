package chess;

import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JLabel;

import chess.Control.Piecetype.Team;

class TimeCounter extends Thread {
	private AtomicLong counter;
	private boolean stop;
	private Team team;
	private JLabel label;
	private OnGameOver ogo;
	private long increaseOnStart;

	public TimeCounter(AtomicLong counter, Team team, JLabel label, OnGameOver ogo, long increaseOnStart) {
		this.counter = counter;
		stop = true;
		this.team = team;
		this.label = label;
		this.increaseOnStart = increaseOnStart;
		this.ogo = ogo;
	}
	public void setTime(long time) {
		this.counter.set(time);
	}
	public void startCounter() {
		stop = false;
		counter.addAndGet(increaseOnStart);
		// this.notify(); Need to see how this works
	}

	public void stopCounter() {
		stop = true;
	}

	public boolean isRunning() {
		return !stop;
	}

	public long getValue() {
		return counter.get();
	}

	@Override
	public void run() {
		long wert = 0;
		while (true) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (!stop) {
				wert = counter.addAndGet(-100);
				if (wert <= 0) {
					wert = 0;
					ogo.gameOver(team == Team.WHITE ? Team.BLACK : Team.WHITE);
				}
			}
			if(this.label != null) {
				wert = counter.get() / 1000;
				this.label.setText(team.name() + ": " + (wert / 60) + ":" + (wert % 60));
			}
		}
	}

}
interface OnGameOver{
	public void gameOver(Team t);
}
