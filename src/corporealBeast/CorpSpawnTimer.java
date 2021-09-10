package corporealBeast;

public class CorpSpawnTimer {
	private long ms;

	public CorpSpawnTimer() {
		this.ms = System.currentTimeMillis();
	}

	public long getMs() {
		return ms;
	}

	public void setMs(long ms) {
		this.ms = ms;
	}

}
