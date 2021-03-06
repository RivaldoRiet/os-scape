package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class InfernoWaveOverlay extends Overlay {
	private final InfernoPlugin plugin;

	private final InfernoConfig config;

	private final PanelComponent panelComponent;

	private Color waveHeaderColor;

	private Color waveTextColor;

	private InfernoWaveDisplayMode displayMode;

	void setWaveHeaderColor(Color waveHeaderColor) {
		this.waveHeaderColor = waveHeaderColor;
	}

	void setWaveTextColor(Color waveTextColor) {
		this.waveTextColor = waveTextColor;
	}

	void setDisplayMode(InfernoWaveDisplayMode displayMode) {
		this.displayMode = displayMode;
	}

	@Inject
	InfernoWaveOverlay(InfernoPlugin plugin, InfernoConfig config) {
		this.plugin = plugin;
		this.config = config;
		this.panelComponent = new PanelComponent();
		determineLayer();
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority(OverlayPriority.HIGH);
		this.panelComponent.setPreferredSize(new Dimension(160, 0));
	}

	public Dimension render(Graphics2D graphics) {
		this.panelComponent.getChildren().clear();
		if (this.displayMode == InfernoWaveDisplayMode.CURRENT || this.displayMode == InfernoWaveDisplayMode.BOTH)
			InfernoWaveMappings.addWaveComponent(this.config, this.panelComponent, "Current Wave (Wave " + this.plugin

					.getCurrentWaveNumber() + ")", this.plugin.getCurrentWaveNumber(), this.waveHeaderColor, this.waveTextColor);
		if (this.displayMode == InfernoWaveDisplayMode.NEXT || this.displayMode == InfernoWaveDisplayMode.BOTH)
			InfernoWaveMappings.addWaveComponent(this.config, this.panelComponent, "Next Wave (Wave " + this.plugin

					.getNextWaveNumber() + ")", this.plugin.getNextWaveNumber(), this.waveHeaderColor, this.waveTextColor);
		return this.panelComponent.render(graphics);
	}

	public void determineLayer() {
	}
}
