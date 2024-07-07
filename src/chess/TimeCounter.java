package chess;

import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JLabel;

import chess.Control.Piecetype.Team;

class TimeCounter extends Thread {
	private AtomicLong counter;
	private boolean stop;
	private Team team;
	private JLabel label;
	private Control c;
	private long increaseOnStart;

	public TimeCounter(AtomicLong counter, Team team, JLabel label, Control c, long increaseOnStart) {
		this.counter = counter;
		stop = true;
		this.team = team;
		this.label = label;
		this.c = c;
		this.increaseOnStart = increaseOnStart;
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
			wert = counter.get() / 1000;
			if (!stop) {
				wert = counter.addAndGet(-100) / 1000;
				if (wert <= 0) {
					wert = 0;
					c.gameOver(team == Team.WHITE ? Team.BLACK : Team.WHITE, true);
				}
			}
			// System.out.println(team.name() + ": " + (wert / 60) + ":" + (wert % 60));
			this.label.setText(team.name() + ": " + (wert / 60) + ":" + (wert % 60));
		}
	}

}
