package net.runelite.client.plugins.packet.inferno.displaymodes;

public enum InfernoZukShieldDisplayMode {
	OFF("Off"),
	LIVE("Live (follow shield)"),
	PREDICT("Predict"),
	LIVEPLUSPREDICT("Live and Predict");

	private final String name;

	String getName() {
		return this.name;
	}

	InfernoZukShieldDisplayMode(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
