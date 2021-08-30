/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.packet.prayagainstplayer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
class PrayAgainstPlayerOverlay extends Overlay
{

	private final PrayAgainstPlayerPlugin plugin;
	private final PrayAgainstPlayerConfig config;
	private final Client client;
	private int lastId = 0;
	private int lastWep = 0;
	@Inject
	private PrayAgainstPlayerOverlay(final PrayAgainstPlayerPlugin plugin, final PrayAgainstPlayerConfig config, final Client client)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		determineLayer();
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D g)
	{
		readPrayer();
		renderPotentialPlayers(g);
		renderAttackingPlayers(g);
		g.drawString("Debug", 7, 245);
		g.drawString("Isreachable: " + plugin.isOpponentReachable() + " - frozen: " + plugin.isFrozen() + " - distance: " + plugin.enemyDistance(), 7, 260);
		//handleSpec();
		return null;
	}

	private void renderPotentialPlayers(Graphics2D graphics)
	{
		if (plugin.getPotentialPlayersAttackingMe() == null || !plugin.getPotentialPlayersAttackingMe().isEmpty())
		{
			try
			{
				if (plugin.getPotentialPlayersAttackingMe() != null)
				{
					for (PlayerContainer container : plugin.getPotentialPlayersAttackingMe())
					{
						if ((System.currentTimeMillis() > (container.getWhenTheyAttackedMe() + container.getMillisToExpireHighlight())) && (container.getPlayer().getInteracting() != client.getLocalPlayer()))
						{
							plugin.removePlayerFromPotentialContainer(container);
						}
						if (config.drawPotentialTargetsName())
						{
							renderNameAboveHead(graphics, container.getPlayer(), config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetHighlight())
						{
							renderHighlightedPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetTile())
						{
						//	renderTileUnderPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
						}
						if (config.drawPotentialTargetPrayAgainst())
						{
							if (plugin.getPlayersAttackingMe() == null || plugin.getPlayersAttackingMe().isEmpty())
							{
								renderPrayAgainstOnPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
							}
						}
					}
				}
			}
			catch (ConcurrentModificationException ignored)
			{
			}
		}
	}

	private void renderAttackingPlayers(Graphics2D graphics)
	{
		if (plugin.getPlayersAttackingMe() == null || !plugin.getPlayersAttackingMe().isEmpty())
		{
			try
			{
				if (plugin.getPlayersAttackingMe() != null)
				{
					for (PlayerContainer container : plugin.getPlayersAttackingMe())
					{
						if ((System.currentTimeMillis() > (container.getWhenTheyAttackedMe() + container.getMillisToExpireHighlight())) && (container.getPlayer().getInteracting() != client.getLocalPlayer()))
						{
							plugin.removePlayerFromAttackerContainer(container);
						}

						if (config.drawTargetsName())
						{
							renderNameAboveHead(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
						if (config.drawTargetHighlight())
						{
							renderHighlightedPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
						if (config.drawTargetTile())
						{
							//renderTileUnderPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
						if (config.drawTargetPrayAgainst())
						{
							renderPrayAgainstOnPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
						}
					}
				}
			}
			catch (ConcurrentModificationException ignored)
			{
			}
		}
	}

	private void renderNameAboveHead(Graphics2D graphics, Player player, Color color)
	{
		final String name = Text.sanitize(player.getName());
		final int offset = player.getLogicalHeight() + 40;
		Point textLocation = player.getCanvasTextLocation(graphics, name, offset);
		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, name, color);
		}
	}

	private void renderHighlightedPlayer(Graphics2D graphics, Player player, Color color)
	{
		try
		{
			OverlayUtil.renderPolygon(graphics, player.getConvexHull(), color);
		}
		catch (NullPointerException ignored)
		{
		}
	}

	private void renderTileUnderPlayer(Graphics2D graphics, Player player, Color color)
	{
		Polygon poly = player.getCanvasTilePoly();
		OverlayUtil.renderPolygon(graphics, poly, color);
	}

	private Player getCurrPlayer()
	{
		if (plugin.getPlayersAttackingMe() != null && plugin.getPlayersAttackingMe().size() > 0)
		{
			return plugin.getPlayersAttackingMe().get(0).getPlayer();
		}
		if (plugin.getPlayersAttackingMe() != null && plugin.getPlayersAttackingMe().size() <= 0 && plugin.getPotentialPlayersAttackingMe() != null && plugin.getPotentialPlayersAttackingMe().size() > 0)
		{
			return plugin.getPotentialPlayersAttackingMe().get(0).getPlayer();
		}
		return null;
	}


	private void readPrayer()
	{
		Player player = getCurrPlayer();
		if (player != null) {
			switch (WeaponType.checkWeaponOnPlayer(client, player)) {
				case WEAPON_MELEE:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
						handlePray(1);
					}
					break;
				case WEAPON_MAGIC:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
						handlePray(2);
					}
					break;
				case WEAPON_RANGED:
					if (!client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
						handlePray(3);
					}
					break;
				default:
					break;
			}
		}
	}

	private void handlePray(int id)
	{
		if (id == 1 && lastId != id)
		{
			plugin.invoke("Activate", "<col=ff9040>Protect from Melee</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MELEE.getId());
			lastId = id;
		}

		if (id == 2 && lastId != id)
		{
			plugin.invoke("Activate", "<col=ff9040>Protect from Magic</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId() );
			lastId = id;
		}

		if (id == 3 && lastId != id)
		{
			plugin.invoke("Activate", "<col=ff9040>Protect from Missiles</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId() );
			lastId = id;
		}
	}

	private void renderPrayAgainstOnPlayer(Graphics2D graphics, Player player, Color color)
	{
		final int offset = (player.getLogicalHeight() / 2) + 75;
		BufferedImage icon;

		switch (WeaponType.checkWeaponOnPlayer(client, player))
		{
			case WEAPON_MELEE:
				//handlePray(1);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_MELEE);
				graphics.setColor(Color.RED);
				graphics.fillRect(0, 230, 15, 15);
				break;
			case WEAPON_MAGIC:
				//handlePray(2);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_MAGIC);
				graphics.setColor(Color.BLUE);
				graphics.fillRect(0, 230, 15, 15);
				break;
			case WEAPON_RANGED:
				//handlePray(3);
				icon = plugin.getProtectionIcon(WeaponType.WEAPON_RANGED);
				graphics.setColor(Color.GREEN);
				graphics.fillRect(0, 230, 15, 15);
				break;
			default:
				icon = null;
				break;
		}
		try
		{
			if (icon != null)
			{
				Point point = player.getCanvasImageLocation(icon, offset);
				OverlayUtil.renderImageLocation(graphics, point, icon);
			}
			else
			{
				if (config.drawUnknownWeapons())
				{
					int itemId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
					ItemComposition itemComposition = client.getItemDefinition(itemId);

					final String str = itemComposition.getName().toUpperCase();
					Point point = player.getCanvasTextLocation(graphics, str, offset);
					OverlayUtil.renderTextLocation(graphics, point, str, color);
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private void handleSpec()
	{
		final ItemContainer ic = client.getItemContainer(InventoryID.EQUIPMENT);
		int weaponId = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
		final String name = client.getItemDefinition(weaponId).getName().toLowerCase();
		if (client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED) == 0 && name.contains("godsword") || name.contains("claws") || name.contains("dagger") || name.contains("javelin") || name.contains("longsword"))
		{
			if (lastWep != weaponId)
			{
				plugin.invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
			}
		}
		lastWep = weaponId;
	}

	private void click(int x, int y)
	{
		//MouseEvent mousePressed = new MouseEvent(client.getCanvas(), 501, System.currentTimeMillis(), 0, (int) (x), (int) (y), 1, false, 1);
		//client.getCanvas().dispatchEvent(mousePressed);

		MouseEvent mouseReleased = new MouseEvent(client.getCanvas(), 502, System.currentTimeMillis(), 0, (int) (x), (int) (y), 1, false, 1);
		client.getCanvas().dispatchEvent(mouseReleased);

		//MouseEvent mouseClicked = new MouseEvent(client.getCanvas(), 500, System.currentTimeMillis(), 0, (int) (x), (int) (y), 1, false, 1);
		//client.getCanvas().dispatchEvent(mouseClicked);
	}

	public void determineLayer()
	{
		if (config.mirrorMode())
		{
	
		}
		if (!config.mirrorMode())
		{
			setLayer(OverlayLayer.ABOVE_SCENE);
		}
	}
}
