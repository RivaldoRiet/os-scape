package net.runelite.client.plugins.packet.inferno.displaymodes;

public enum InfernoWaveDisplayMode {
	CURRENT("Current wave"),
	NEXT("Next wave"),
	BOTH("Both"),
	NONE("None");

	InfernoWaveDisplayMode(String name) {
		this.name = name;
	}

	private final String name;

	public String toString() {
		return this.name;
	}
}
