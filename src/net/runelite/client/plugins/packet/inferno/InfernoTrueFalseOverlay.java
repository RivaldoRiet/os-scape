package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class InfernoTrueFalseOverlay extends Overlay {
	private InfernoPlugin plugin;

	private Rectangle rectangle = new Rectangle(470, 350, 10, 10);

	private final PanelComponent panelComponent;

	private Color rectangleColor;

	@Inject
	private Client client;

	@Inject
	public InfernoTrueFalseOverlay(InfernoPlugin plugin, Client client) {
		setPriority(OverlayPriority.LOW);
		setPosition(OverlayPosition.BOTTOM_LEFT);
		this.client = client;
		this.plugin = plugin;
		this.panelComponent = new PanelComponent();
		this.panelComponent.setPreferredSize(new Dimension(150, 0));
		this.panelComponent.getChildren().add(LineComponent.builder().left("Inferno prayer ").build());
	}

	public Dimension render(Graphics2D graphics) {
		this.panelComponent.getChildren().clear();
		this.panelComponent.getChildren().add(LineComponent.builder().left("Inferno prayer " + this.plugin.getMaul()).build());
		return this.panelComponent.render(graphics);
	}
}
