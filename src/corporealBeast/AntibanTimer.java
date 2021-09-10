package corporealBeast;

public class AntibanTimer {
	private long ms;

	public AntibanTimer() {
		this.ms = System.currentTimeMillis();
	}

	public long getMs() {
		return ms;
	}

	public void setMs(long ms) {
		this.ms = ms;
	}

}
