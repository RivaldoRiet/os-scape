package api.utils;

import lombok.Getter;
import simple.robot.util.Time;

public class Timer {
	@Getter
	public long start;

	private long period;

	private long end;

	public String toElapsedString() {
		return Time.formatTime(getElapsed());
	}

	public long setEndIn(long l) {
		this.end = System.currentTimeMillis() + l;
		return this.end;
	}

	public String toRemainingString() {
		return Time.formatTime(getRemaining());
	}

	public void end() {
		this.end = System.currentTimeMillis();
	}

	public void reset() {
		this.end = System.currentTimeMillis() + this.period;
	}

	public Timer() {
		this.period = this.start = System.currentTimeMillis();
	}

	public void restart() {
		this.period = this.start = System.currentTimeMillis();
	}

	public Timer(long l) {
		this.period = l;
		this.start = System.currentTimeMillis();
		this.end = this.start + l;
	}

	public long getElapsed() {
		return System.currentTimeMillis() - this.start;
	}

	public long getRemaining() {
		if (isRunning()) return this.end - System.currentTimeMillis();
		return 0L;
	}

	public boolean isRunning() {
		return System.currentTimeMillis() < this.end;
	}
}