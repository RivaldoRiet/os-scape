package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoNamingDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoPrayerDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoSafespotDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoZukShieldDisplayMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Singleton
public class InfernoInfoBoxOverlay extends Overlay {
	private static final Color NOT_ACTIVATED_BACKGROUND_COLOR = new Color(150, 0, 0, 150);

	private final Client client;

	private final InfernoPlugin plugin;

	private final InfernoConfig config;

	private final SpriteManager spriteManager;

	private final PanelComponent imagePanelComponent = new PanelComponent();

	private BufferedImage prayMeleeSprite;

	private BufferedImage prayRangedSprite;

	private BufferedImage prayMagicSprite;

	@Inject
	private InfernoInfoBoxOverlay(Client client, InfernoPlugin plugin, InfernoConfig config, SpriteManager spriteManager) {
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
		determineLayer();
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.HIGH);
	}

	public Dimension render(Graphics2D graphics) {
		if (this.config.prayerDisplayMode() != InfernoPrayerDisplayMode.BOTTOM_RIGHT
				&& this.config.prayerDisplayMode() != InfernoPrayerDisplayMode.BOTH)
			return null;
		this.imagePanelComponent.getChildren().clear();
		if (this.plugin.getClosestAttack() != null) {
			BufferedImage prayerImage = getPrayerImage(this.plugin.getClosestAttack());
			this.imagePanelComponent.getChildren().add(new ImageComponent(prayerImage));
			this.imagePanelComponent.setBackgroundColor(this.client.isPrayerActive(this.plugin.getClosestAttack().getPrayer())
					? ComponentConstants.STANDARD_BACKGROUND_COLOR
					: NOT_ACTIVATED_BACKGROUND_COLOR);
		} else {
			this.imagePanelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
		}
		return this.imagePanelComponent.render(graphics);
	}

	private BufferedImage getPrayerImage(InfernoNPC.Attack attack) {
		if (this.prayMeleeSprite == null) this.prayMeleeSprite = this.spriteManager.getSprite(129, 0);
		if (this.prayRangedSprite == null) this.prayRangedSprite = this.spriteManager.getSprite(128, 0);
		if (this.prayMagicSprite == null) this.prayMagicSprite = this.spriteManager.getSprite(127, 0);
		switch (attack) {
			case MELEE:
				return this.prayMeleeSprite;
			case RANGED:
				return this.prayRangedSprite;
			case MAGIC:
				return this.prayMagicSprite;
		}
		return this.prayMagicSprite;
	}

	public void determineLayer() {

	}
}
