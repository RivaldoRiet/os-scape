package corporealBeast;

public class VengeTimer {
private long ms;

public VengeTimer() {
	this.ms = System.currentTimeMillis();
}

public long getMs() {
	return ms;
}

public void setMs(long ms) {
	this.ms = ms;
}

}
