package net.runelite.client.plugins.packet.inferno.displaymodes;

public enum InfernoSafespotDisplayMode {
	OFF("Off"),
	INDIVIDUAL_TILES("Individual tiles"),
	AREA("Area (lower fps)");

	private final String name;

	String getName() {
		return this.name;
	}

	InfernoSafespotDisplayMode(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
