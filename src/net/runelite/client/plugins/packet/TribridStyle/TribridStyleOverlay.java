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

package net.runelite.client.plugins.packet.TribridStyle;

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
class TribridStyleOverlay extends Overlay
{

	private final TribridStylePlugin plugin;
	private final TribridStyleConfig config;
	private final Client client;
	private int lastId = 0;
	private int lastWep = 0;
	private boolean shouldSpec = true;
	private boolean shouldEat = true;
	private boolean shouldTripleEat = true;
	private boolean shouldDrinkPrayer = true;
	private long lastTime = 0;
	private long lastEatTime = 0;
	private long lastTripleEatTime = 0;
	private long lastPrayerTime = 0;
	private long lastSurge = 0;
	@Inject
	private TribridStyleOverlay(final TribridStylePlugin plugin, final TribridStyleConfig config, final Client client)
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
		if (config.autoSwitchPrayer() && getPercentagePrayer() > 0 && client.getRealSkillLevel(Skill.PRAYER) >= 43) {
			readPrayer();
		}
		renderPotentialPlayers(g);
		renderAttackingPlayers(g);
		renderAvailablePlayers(g);
		//handleFood();
		g.setColor(Color.WHITE);
		if (config.useAutoeat()) {
			handleFood();
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

	private void handleFood(){
		Player p = getCurrPlayer();
		if (p != null)
		{
			if(p.getAnimation() == 1667 && shouldTripleEat ||  p.getAnimation() == 7644 && shouldTripleEat)
			{
				//plugin.doubleEat();
				lastTripleEatTime = System.currentTimeMillis();
				plugin.tripleEat();
				shouldTripleEat = false;
			}

		}
		if (System.currentTimeMillis() - lastEatTime > 3000)
		{
			shouldEat = true;
		}

		if (System.currentTimeMillis() - lastTripleEatTime > 3000)
		{
			shouldTripleEat = true;
		}
	}

	private void handlePray(int id)
	{
		if (id == 1 && lastId != id)
		{
		//	click(717, 4);
			plugin.invoke("Activate", "<col=ff9040>Protect from Melee</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MELEE.getId());
		//	click(717, 4);
			lastId = id;
		}

		if (id == 2 && lastId != id)
		{
			//click(717, 4);
			plugin.invoke("Activate", "<col=ff9040>Protect from Magic</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId() );
			//click(717, 4);
			lastId = id;
		}

		if (id == 3 && lastId != id)
		{
		//	click(717, 4);
			plugin.invoke("Activate", "<col=ff9040>Protect from Missiles</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId() );
		//	click(717, 4);
			lastId = id;
		}
	}

	private void renderAvailablePlayers(Graphics2D graphics){
		for (Player p : client.getPlayers()) {
			if (config.showAvailable() && !p.equals(client.getLocalPlayer())) {
				int localLvl = client.getLocalPlayer().getCombatLevel();
				int difference = localLvl - p.getCombatLevel();
				if (difference >= -5 && difference <= 15) {
					try {
						OverlayUtil.renderPolygon(graphics, p.getConvexHull(), Color.BLUE);
					} catch (NullPointerException ignored) {
					}
				}
			}
		}
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

	private int getPercentagePrayer()
	{
		//returns 50 for example
		if (client.getBoostedSkillLevel(Skill.PRAYER) == 0)
		{
			return 0;
		}
		float perc = ((float) client.getBoostedSkillLevel(Skill.PRAYER) / client.getRealSkillLevel(Skill.PRAYER));
		float perc1 = (perc * 100);
		return (int) perc1;
	}

	private void handlePray()
	{
		if (!opponentIsSpeccing() && getPercentagePrayer() <= 50 && shouldDrinkPrayer) {
			plugin.DrinkByName("uper restore");
			plugin.DrinkByName("rayer potion");
			shouldDrinkPrayer = false;
			lastPrayerTime = System.currentTimeMillis();
		}

		if (System.currentTimeMillis() - lastPrayerTime > 3000) {
			shouldDrinkPrayer = true;
		}
	}

	private boolean opponentIsSpeccing()
	{
		Player p = getCurrPlayer();
		if (p != null) {
			if (p.getAnimation() == 1667 || p.getAnimation() == 1062 || p.getAnimation() == 7644) {
				return true;
			}
		}

		return false;
	}

	private void handleSurge()
	{
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
							renderTileUnderPlayer(graphics, container.getPlayer(), config.potentialPlayerColor());
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
							renderTileUnderPlayer(graphics, container.getPlayer(), config.attackerPlayerColor());
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

	private void invokeMenuAction(String a, String b, int c, int d, int e, int f)
	{
		plugin.invoke(a, b, c, d, e, f);
	}

	private void attackCurrentPlayer()
	{
		String name = "";
		int level = 0;
		int idx = 0;
		if (plugin.getPlayersAttackingMe() != null && plugin.getPlayersAttackingMe().size() > 0)
		{
			name = plugin.getPlayersAttackingMe().get(0).getPlayer().getName();
			level = plugin.getPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = plugin.getIndex(plugin.getPlayersAttackingMe().get(0).getPlayer());
		}
		if (plugin.getPlayersAttackingMe() != null && plugin.getPlayersAttackingMe().size() <= 0 && plugin.getPotentialPlayersAttackingMe() != null && plugin.getPotentialPlayersAttackingMe().size() > 0)
		{
			name = plugin.getPotentialPlayersAttackingMe().get(0).getPlayer().getName();
			level = plugin.getPotentialPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = plugin.getIndex(plugin.getPotentialPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invokeMenuAction("Fight", "<col=ffffff>" + name + "<col=ff00>  (level-" + level + ")", idx, MenuAction.PLAYER_FIRST_OPTION.getId(), 0, 0);
		}
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
		setLayer(OverlayLayer.ABOVE_SCENE);

	}
}
