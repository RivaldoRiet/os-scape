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

package net.runelite.client.plugins.packet.agsriskfighter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
class PrayAgainstAgsOverlay extends Overlay
{

	private final PrayAgainstAgsPlugin plugin;
	private final PrayAgainstAgsConfig config;
	private final Client client;
	private int lastId = 0;
	private int lastWep = 0;
	private boolean shouldSpec = true;
	private boolean shouldVenge = true;
	private boolean shouldEat = true;
	private boolean shouldTripleEat = true;
	private long lastTime = 0;
	private long lastEatTime = 0;
	private long lastVengeTime = 0;
	private long lastTripleEatTime = 0;
	private static Class<?> _class;
	private static Method action;
	
	public void invoke(String option, String target, int id, int menuAction, int action, int widgetId) {
		MenuOptionClicked menu = new MenuOptionClicked();
		menu.setActionParam(action);
		menu.setMenuOption(option);
		menu.setMenuTarget(target);
		menu.setMenuAction(MenuAction.of(menuAction));
		menu.setId(id);
		menu.setWidgetId(widgetId);
		invoke(menu);
	}
	
	public void invoke(MenuOptionClicked option) {
		try {
			if (_class == null) _class = Class.forName("class67");
			
			if (action == null) {
				action = _class.getDeclaredMethod("method1808", int.class, int.class, int.class, int.class, java.lang.String.class,
						java.lang.String.class, int.class, int.class, int.class);
				action.setAccessible(true);
			}

			action.invoke(_class, option.getActionParam(), option.getWidgetId(), option.getMenuAction().getId(),
					option.getId(), option.getMenuOption(), option.getMenuTarget(), 640, 432, 1191285249);
		} catch (Exception e1) {
			System.out.println(option);
			e1.printStackTrace();
		}
	}
	
	@Inject
	private PrayAgainstAgsOverlay(PrayAgainstAgsPlugin plugin, final PrayAgainstAgsConfig config, final Client client)
	{
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		determineLayer();
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		renderPotentialPlayers(graphics);
		renderAttackingPlayers(graphics);
		//handleSpec();
		if (config.renderLowerCombat())
			renderAvailablePlayers(graphics);
		if (config.useVengeOnAgs())
			vengeOnAnimation();
		handleFood();
		return null;
	}

	private void renderAvailablePlayers(Graphics2D graphics){
		for (Player p : client.getPlayers()) {
			if (!p.equals(client.getLocalPlayer())) {
				int localLvl = client.getLocalPlayer().getCombatLevel();
				int difference = localLvl - p.getCombatLevel();
				if (difference >= -5 && difference <= 15) {
					try {
						OverlayUtil.renderPolygon(graphics, p.getConvexHull(), Color.BLUE);
					} catch (NullPointerException ignored) {
					}
					//	client.getLogger().debug("difference: " + difference);
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

	public int getPercentageHp()
	{
		//returns 50 for example
		if (client.getBoostedSkillLevel(Skill.HITPOINTS) == 0)
		{
			return 0;
		}
		float perc = ((float) client.getBoostedSkillLevel(Skill.HITPOINTS) / client.getRealSkillLevel(Skill.HITPOINTS));
		float perc1 = (perc * 100);
		return (int) perc1;
	}

	private void vengeOnAnimation()
	{
		Player p = getCurrPlayer();
		if (p != null) {
			if (isVengable(p.getAnimation()) && shouldVenge) {
				plugin.castVenge();
				shouldVenge = false;
				lastVengeTime = System.currentTimeMillis();
				return;
			}
		}

		if (System.currentTimeMillis() - lastVengeTime > 5000)
		{
			shouldVenge = true;
		}
	}

	boolean isVengable(int animation)
	{
		if (animation == 7644) {
			return true; // ags spec
		}
		if (animation == 7045) {
			return true; // ags
		}
		if (animation == 7516) {
			return true; // elder
		}
		if (animation == 7218) {
			return true; // ballista
		}
		if (animation == 7222) {
			return true; // ballista spec
		}
		return false;
	}

	private void handleFood(){
		 Player p = getCurrPlayer();
		if (config.autoEat() && p != null)
		{
			if(p.getAnimation() == 1667  && shouldTripleEat ||
					p.getAnimation() == 7644 && shouldTripleEat)
			{
				//plugin.doubleEat();
				lastTripleEatTime = System.currentTimeMillis();
				plugin.tripleEat();
				shouldTripleEat = false;
			}

		}
		if (config.eatOnLowHp() > 0 && getPercentageHp() <= config.eatOnLowHp() && shouldTripleEat) {
			lastTripleEatTime = System.currentTimeMillis();
			plugin.tripleEat();
			shouldTripleEat = false;
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

	private void handlePray(int id)
	{
		if (id == 1 && lastId != id)
		{
			invoke("Activate", "<col=ff9040>Protect from Melee</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MELEE.getId());
			lastId = id;
		}

		if (id == 2 && lastId != id)
		{
			invoke("Activate", "<col=ff9040>Protect from Magic</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId() );
			lastId = id;
		}

		if (id == 3 && lastId != id)
		{
			invoke("Activate", "<col=ff9040>Protect from Missiles</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId() );
			lastId = id;
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

				invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);

			}
		}
		lastWep = weaponId;
	}

	public void determineLayer()
	{
			setLayer(OverlayLayer.ABOVE_SCENE);
	}
}
