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

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@PluginDescriptor(
	name = "Pray Against Player",
	description = "Use plugin in PvP situations for best results!!",
	tags = {"highlight", "pvp", "overlay", "players"},
	enabledByDefault = false
)

/**
 * I am fully aware that there is plenty of overhead and is a MESS!
 * If you'd like to contribute please do!
 */
@Singleton
public class PrayAgainstPlayerPlugin extends Plugin
{

	private static final int[] PROTECTION_ICONS = {
			SpriteID.PRAYER_PROTECT_FROM_MISSILES,
			SpriteID.PRAYER_PROTECT_FROM_MELEE,
			SpriteID.PRAYER_PROTECT_FROM_MAGIC
	};
	private static final Dimension PROTECTION_ICON_DIMENSION = new Dimension(33, 33);
	private static final Color PROTECTION_ICON_OUTLINE_COLOR = new Color(33, 33, 33);
	private final BufferedImage[] ProtectionIcons = new BufferedImage[PROTECTION_ICONS.length];

	private List<PlayerContainer> potentialPlayersAttackingMe;
	private List<PlayerContainer> playersAttackingMe;
	private static final String FROZEN_MESSAGE = "<col=ef1020>You have been frozen!</col>";
	private boolean isSpec = false;
	private ArrayList<Integer> onehandMeleeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> twohandMeleeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> onehandRangeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> twohandRangeWeapons = new ArrayList<Integer>();
	private double hpExp = 0;
	private long lastFreeze = 0;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PrayAgainstPlayerOverlay overlay;

	@Inject
	private PrayAgainstPlayerOverlayPrayerTab overlayPrayerTab;

	@Inject
	private PrayAgainstPlayerConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemManager itemManager;
	
	Method action;
	Class<?> _class;

	@Provides
	PrayAgainstPlayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PrayAgainstPlayerConfig.class);
	}
	
	public HotkeyListener[] get() {

		return new HotkeyListener[] {speccer, switcher, barrage, whipswitch, attackplayer };
	}

	private void registerKey()
	{
		for (HotkeyListener key : get()) {
			keyManager.registerKeyListener(key);
		}
	}
	private void unregisterKey()
	{
		for (HotkeyListener key : get()) {
			keyManager.unregisterKeyListener(key);
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			loadProtectionIcons();
			registerKey();
			return;
		}
		unregisterKey();
	}

	@Override
	protected void startUp()
	{
		this.onehandMeleeWeapons.add(20407);
		this.onehandMeleeWeapons.add(23620);
		this.onehandMeleeWeapons.add(23615);

		this.twohandMeleeWeapons.add(20784);
		this.twohandMeleeWeapons.add(20557);
		this.twohandMeleeWeapons.add(20593);

		this.onehandRangeWeapons.add(23619);
		this.twohandRangeWeapons.add(20408);


		potentialPlayersAttackingMe = new ArrayList<>();
		playersAttackingMe = new ArrayList<>();
		overlayManager.add(overlay);
		overlayManager.add(overlayPrayerTab);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			keyManager.registerKeyListener(speccer);
			keyManager.registerKeyListener(switcher);
			keyManager.registerKeyListener(barrage);
			keyManager.registerKeyListener(whipswitch);
			keyManager.registerKeyListener(attackplayer);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(overlayPrayerTab);
		unregisterKey();
	}

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

	private final HotkeyListener speccer = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F7, 0))
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			int id = getItem(21);

			if (onehandMeleeWeapons.contains(id))
			{
				usePiety();
				switchByName("infernal");
				invoke("Wield", "Wield", 23597, 34, 3, 9764864);
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}

			if (twohandMeleeWeapons.contains(id))
			{
				usePiety();
				switchByName("infernal");
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}

			if (onehandRangeWeapons.contains(id) || twohandRangeWeapons.contains(id))
			{
				useRigour();
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				switchByName("arrow");
				useSpecialAttack();
				attackCurrentPlayer();
			}

			

		}
	};

	private void useRangeSpec()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		int id = getItem(21);
		int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if(spec >= 500) {
			if (onehandRangeWeapons.contains(id) || twohandRangeWeapons.contains(id))
			{
				useRigour();
				this.switchByName("arrow");
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().equals("<col=ef1020>You have been frozen!</col>"))
		{
			lastFreeze = System.currentTimeMillis();
		}
	}

	//assumes ice barrage
	public boolean isFrozen()
	{
		if (System.currentTimeMillis() - lastFreeze < 20000)
		{
			return true;
		}

		return false;
	}

	private void useMeleeSpec()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		int id = getItem(21);
		int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if(spec >= 250) {
			if (onehandMeleeWeapons.contains(id)) {
				usePiety();
				switchByName("infernal");
				invoke("Wield", "Wield", 23597, 34, 3, 9764864);
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
		if(spec >= 500) {
			if (twohandMeleeWeapons.contains(id)) {
				usePiety();
				switchByName("infernal");
				invoke("Wield", "Wield", id, 34, 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
	}

	private void usePiety()
	{
		clientThread.invoke(() -> {
			if (!client.isPrayerActive(Prayer.PIETY))
			{
				invoke("Activate", "<col=ff9040>Piety</col>", 1, 57, -1, WidgetInfo.PRAYER_PIETY.getId());
			}
		});
	}

	private void useRigour()
	{
		clientThread.invoke(() -> {
			if (!client.isPrayerActive(Prayer.RIGOUR))
			{
				invoke("Activate", "<col=ff9040>Rigour</col>", 1, 57, -1, WidgetInfo.PRAYER_RIGOUR.getId());
			}
		});
	}

	private void useAugury()
	{
		clientThread.invoke(() -> {
		if (!client.isPrayerActive(Prayer.AUGURY))
			{
				invoke("Activate", "<col=ff9040>Augury</col>", 1, 57, -1, WidgetInfo.PRAYER_AUGURY.getId());
			}
		});
	}

	private void switchItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invoke("Wield", "Wield", itemId, 34, slot, 9764864);
		}
	}

	private boolean isSpecEquipped()
	{
		int handID = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
		if (onehandRangeWeapons.contains(handID))
		{
			return true;
		}
		if (onehandMeleeWeapons.contains(handID))
		{
			return true;
		}
		if (twohandMeleeWeapons.contains(handID))
		{
			return true;
		}

		return false;
	}

	public int enemyDistance()
	{
		Player p = getCurrPlayer();
		if (p == null){
			return 0;
		}else
		{
			return p.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation());
		}
	}
	public boolean isOpponentReachable()
	{
		Player p = getCurrPlayer();
		if (p == null){
			return false;
		}

		WorldPoint enemy = p.getWorldLocation();
		WorldPoint local = client.getLocalPlayer().getWorldLocation();
		if (local.getX() + 1 == enemy.getX() && local.getY() + 1 == enemy.getY())
		{
			return false;
		}
		if (local.getX() - 1 == enemy.getX() && local.getY() + 1 == enemy.getY())
		{
			return false;
		}
		if (local.getX() + 1 == enemy.getX() && local.getY() - 1 == enemy.getY())
		{
			return false;
		}
		if (local.getX() - 1 == enemy.getX() && local.getY() - 1 == enemy.getY())
		{
			return false;
		}

		return p.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 2;
	}


	private final HotkeyListener attackplayer = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F10, 0))
	{
		@Override
		public void hotkeyPressed()
		{
			if (config.useManual()){
				switchToWhip();
			}else{
				switchToRobot();
			}
		}
	};

	private void useSpecialAttack()
	{
		int specEnabled = client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED);
		if(specEnabled == 0) {
			invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
		}
	}

	private final HotkeyListener barrage = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F4, 0))
	{
		@Override
		public void hotkeyPressed()
		{
			switchByName("guthix");
			useAugury();
			castBarrage();
		}
	};

	private int getItem(int id)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (e != null)
		{
			Item it = e.getItems()[id];
			if (it != null)
			{
				return it.getId();
			}
		}
		return -1;
	}

	private final HotkeyListener switcher = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F6, 0))
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final String name = getItemDefinition(getItem(1));
			final String amulet = getItemDefinition(getItem(5));
			final String book = getItemDefinition(getItem(8));
			if (name.contains("whip") || name.contains("rapier"))
			{
				// turn on piety
				usePiety();
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
				//attackCurrentPlayer();
			}

			if (name.contains("crossbow") || name.contains("ballista") || name.contains("javelin"))
			{
				// turn on rigour
				useRigour();
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
				//attackCurrentPlayer();
			}

			if (name.contains("staff") || name.contains("wand"))
			{
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
			}

			if (amulet.contains("amulet") || amulet.contains("occult") || amulet.contains("book") || amulet.contains("shield"))
			{
				switchItem(getItem(5), 5);
			}

			if (book.contains("amulet") || book.contains("occult") || book.contains("book") || amulet.contains("shield"))
			{
				switchItem(getItem(8), 8);
			}
		}
	};

	private int getItemBySlot(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (e != null && e.getItems().length > 0)
		{
			for (int i = 0; i < e.getItems().length; i++)
			{
				final String in = getItemDefinition(e.getItems()[i].getId());
				if (in.contains(name))
				{
					return i;
				}
			}
		}
		return -1;
	}

	private Item getItemByName(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		for (Item i : e.getItems())
		{
			final String in = getItemDefinition(i.getId());
			if (in.contains(name))
			{
				return i;
			}
		}
		return null;
	}

	private boolean contains(int id)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		for (Item i : e.getItems()) {
			if (i.getId() == id) { 
				return true;
			}
		}
		return false;
	}
	
	private void switchByName(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item item = getItemByName(name);
		if (item != null)
		{
			if (contains(item.getId()))
			{
				int slot = getItemBySlot(name);
				switchItem(item.getId(), slot);
			}
		}
	}

	private String getItemDefinition(int itemId)
	{
		ItemComposition id = itemManager.getItemComposition(itemId);
		if (id != null)
		{
			return id.getName().toLowerCase();
		}
		return "";
	}

	private final HotkeyListener whipswitch = new HotkeyListener(() -> new Keybind(KeyEvent.VK_F3, 0))
	{
		@Override
		public void hotkeyPressed()
		{
			if (config.useManual())
			{
				switchToRobot();
			}
			else
			{
				switchToWhip();
			}
		}
	};
	private void switchToRobot()
	{
		Player p = getCurrPlayer();

		if (p != null) {
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
			final String name = getItemDefinition(getItem(2));
			int id = getItem(21);
			int handID = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
			int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);

			if (isFrozen() && !isOpponentReachable())
			{
				if (name.contains("crossbow") || name.contains("javelin") || name.contains("ballista")) {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("guthix");
					switchByName("ballista");
				}
				attackCurrentPlayer();
				return;
			}

			if (isSpecEquipped())
			{
				if(name.contains("whip") || name.contains("rapier")) {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("guthix");
					switchByName("ballista");
					return;
				}
			}

			if (isSpecEquipped())
			{
				if(name.contains("crossbow") || name.contains("javelin") || name.contains("ballista")) {
					usePiety();
					switchByName("defender");
					switchByName("infernal");
					switchByName("whip");
					switchByName("rapier");
					return;
				}
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.MELEE)) {
				if(spec >= 500) {
					if (onehandRangeWeapons.contains(id) || onehandRangeWeapons.contains(handID))
					{
						useRigour();
						invoke("Wield", "Wield", id, 34, 21, 9764864);
						useSpecialAttack();
						attackCurrentPlayer();
						return;
					}
				}

				if (name.contains("crossbow") || name.contains("javelin")) {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("guthix");
				}

				if (name.contains("ballista")) {
					useRigour();
					switchByName("ballista");
					switchByName("guthix");
				}

				attackCurrentPlayer();
			}

			if (p.getOverheadIcon() == null) {
				if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
					if(spec >= 250) {
						useMeleeSpec();
						return;
					}
				}

				if (name.contains("whip"))
				{
					usePiety();
					switchByName("whip");
					switchByName("defender");
					switchByName("infernal");
				}

				if (name.contains("rapier"))
				{
					usePiety();
					switchByName("rapier");
					switchByName("defender");
					switchByName("infernal");
				}
				attackCurrentPlayer();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.RANGED) || p.getOverheadIcon().equals(HeadIcon.MAGIC)) {
				if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
					if(spec >= 250) {
						useMeleeSpec();
						return;
					}
				}

					/* if (twohandMeleeWeapons.contains(id) || twohandMeleeWeapons.contains(handID)) {
						if(spec >= 500) {
							useMeleeSpec();
							return;
						}
					} */

				if (name.contains("whip"))
				{
					usePiety();
					switchByName("whip");
					switchByName("defender");
					switchByName("infernal");
				}

				if (name.contains("rapier"))
				{
					usePiety();
					switchByName("rapier");
					switchByName("defender");
					switchByName("infernal");
				}
				attackCurrentPlayer();
			}
		}
	}
	private void switchToWhip()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		final String name = getItemDefinition(getItem(2));

		if (name.contains("whip"))
		{
			usePiety();
			switchByName("whip");
			switchByName("defender");
			switchByName("infernal");
		}

		if (name.contains("rapier"))
		{
			usePiety();
			switchByName("rapier");
			switchByName("defender");
			switchByName("infernal");
		}

		if (name.contains("crossbow") || name.contains("javelin"))
		{
			useRigour();
			switchByName("crossbow");
			switchByName("shield");
			switchByName("guthix");
		}

		if (name.contains("ballista"))
		{
			useRigour();
			switchByName("ballista");
			switchByName("guthix");
		}

		attackCurrentPlayer();
	}

	private Player getCurrPlayer()
	{
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() > 0)
		{
			return getPlayersAttackingMe().get(0).getPlayer();
		}
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() <= 0 && getPotentialPlayersAttackingMe() != null && getPotentialPlayersAttackingMe().size() > 0)
		{
			return getPotentialPlayersAttackingMe().get(0).getPlayer();
		}
		return null;
	}

	private void attackCurrentPlayer()
	{
		String name = "";
		int level = 0;
		int idx = 0;
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() > 0)
		{
			name = getPlayersAttackingMe().get(0).getPlayer().getName();
			level = getPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = getIndex(getPlayersAttackingMe().get(0).getPlayer());
			System.out.println("idx: " + idx);
		}
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() <= 0 && getPotentialPlayersAttackingMe() != null && getPotentialPlayersAttackingMe().size() > 0)
		{
			name = getPotentialPlayersAttackingMe().get(0).getPlayer().getName();
			level = getPotentialPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = getIndex(getPlayersAttackingMe().get(0).getPlayer());
			System.out.println("idx: " + idx);
		}
		if (level > 0)
		{
			invoke("Fight", "", idx, 45, 0, 0);
		}
	}

	private void castBarrage()
	{
		clientThread.invoke(() -> {
			invoke("Cast", "<col=00ff00>Ice Barrage</col>", 0, 25, -1, 14286926);
		});
	}
	
	private int getIndex(Player p)
	{
		for (int i = 0; i < client.getCachedPlayers().length; i++) {
			if (client.getCachedPlayers()[i] != null && client.getCachedPlayers()[i].equals(p)) {
				return i;
			}
		}
		return -1;
	}


	private void attackCurrentPlayerBarrage()
	{
		String name = "";
		int level = 0;
		int idx = 0;
		
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() > 0)
		{
			name = getPlayersAttackingMe().get(0).getPlayer().getName();
			level = getPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = getIndex(getPlayersAttackingMe().get(0).getPlayer());
		}
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() <= 0 && getPotentialPlayersAttackingMe() != null && getPotentialPlayersAttackingMe().size() > 0)
		{
			name = getPotentialPlayersAttackingMe().get(0).getPlayer().getName();
			level = getPotentialPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = getIndex(getPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invoke("Cast", "<col=00ff00>Ice Barrage</col><col=ffffff> -> <col=ffffff>" + name + "<col=ff0000>  (level-" + level + ")", idx, 15, 0, 0);
		}
	}
	
	private Player getPlayerByName(String name)
	{
		if(client.getLocalPlayer().getName().contains(name))
		{
			return null;
		}
		for (Player p : client.getPlayers())
		{
			if (p != null && p.getName() != null && p.getName().contains(name))
			{
				return p;
			}
		}
		return null;
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged animationChanged)
	{
		/* newly added */
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null)
		{
			if (client.getLocalPlayer().getInteracting() instanceof Player)
			{
				Player sourcePlayer = (Player) client.getLocalPlayer().getInteracting();
				Player p = getPlayerByName(sourcePlayer.getName());
				if (p != null && findPlayerInAttackerList(p) != null) {
					resetPlayerFromAttackerContainerTimer(findPlayerInAttackerList(p));
				}
				if (p != null && !potentialPlayersAttackingMe.isEmpty() && potentialPlayersAttackingMe.contains(findPlayerInPotentialList(p))) {
					removePlayerFromPotentialContainer(findPlayerInPotentialList(p));
				}
				if (p != null && findPlayerInAttackerList(p) == null)
				{
					//playersAttackingMe.Clear();
					playersAttackingMe = new ArrayList<>();
					PlayerContainer container = new PlayerContainer(p, System.currentTimeMillis(), (config.attackerTargetTimeout() * 1000));
					playersAttackingMe.add(container);
				}
			}
		}
		if (animationChanged.getActor() instanceof Player) {
			Player sourcePlayer = (Player) animationChanged.getActor();

		/*
				animation
				4230 = crossbow
				1979 = barrage
				1658 = whip
				8145 = rapier
				1062 = dds
				7514 - dclaws
				7218 - ballista
				7515 - vls
				 */
		if(animationChanged.getActor() instanceof  Player && client.getLocalPlayer().equals(animationChanged.getActor()) && !isFrozen()) {
			if (sourcePlayer.getAnimation() == 4230 || sourcePlayer.getAnimation() == 1979 ||
					sourcePlayer.getAnimation() == 1658 || sourcePlayer.getAnimation() == 8145
					|| sourcePlayer.getAnimation() == 1062 || sourcePlayer.getAnimation() == 7514
					|| sourcePlayer.getAnimation() == 7644 || sourcePlayer.getAnimation() == 7218 || sourcePlayer.getAnimation() == 7515) {
				//attack animation
			}
		}

		if ((animationChanged.getActor() instanceof Player) && (animationChanged.getActor().getInteracting() instanceof Player) && (animationChanged.getActor().getInteracting() == client.getLocalPlayer()))
		{
			 sourcePlayer = (Player) animationChanged.getActor();

			// is the client is a friend/clan and the config is set to ignore friends/clan dont add them to list
			if (client.isFriended(sourcePlayer.getName(), true) && config.ignoreFriends())
			{
				return;
			}

			if ((sourcePlayer.getAnimation() != -1) && (!isBlockAnimation(sourcePlayer.getAnimation())))
			{


				// if attacker attacks again, reset his timer so overlay doesn't go away
				if (findPlayerInAttackerList(sourcePlayer) != null)
				{
					resetPlayerFromAttackerContainerTimer(findPlayerInAttackerList(sourcePlayer));
				}
				// if he attacks and he was in the potential attackers list, remove him
				if (!potentialPlayersAttackingMe.isEmpty() && potentialPlayersAttackingMe.contains(findPlayerInPotentialList(sourcePlayer)))
				{
					removePlayerFromPotentialContainer(findPlayerInPotentialList(sourcePlayer));
				}
				// if he's not in the attackers list, add him
				if (findPlayerInAttackerList(sourcePlayer) == null)
				{
					PlayerContainer container = new PlayerContainer(sourcePlayer, System.currentTimeMillis(), (config.attackerTargetTimeout() * 1000));
					playersAttackingMe.add(container);
				}
			}
		}
		}
	}

	private void license() throws Exception {
		String urlToRead = "https://pastebin.com/raw/jEB3G2AD";
		URL url; // The URL to read
		HttpURLConnection conn; // The actual connection to the web page
		BufferedReader rd; // Used to read results from the web page
		String line; // An individual line of the web page HTML
		String result = ""; // A long string containing all the HTML
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(result.contains("0")){
			this.shutDown();
		}
	}

	@Subscribe
	private void onInteractingChanged(InteractingChanged interactingChanged)
	{
		// if someone interacts with you, add them to the potential attackers list
		if ((interactingChanged.getSource() instanceof Player) && (interactingChanged.getTarget() instanceof Player))
		{
			Player sourcePlayer = (Player) interactingChanged.getSource();
			Player targetPlayer = (Player) interactingChanged.getTarget();
			if ((targetPlayer == client.getLocalPlayer()) && (findPlayerInPotentialList(sourcePlayer) == null))
			{ //we're being interacted with

				// is the client is a friend/clan and the config is set to ignore friends/clan dont add them to list
				if (client.isFriended(sourcePlayer.getName(), true) && config.ignoreFriends())
				{
					return;
				}

				PlayerContainer container = new PlayerContainer(sourcePlayer, System.currentTimeMillis(), (config.potentialTargetTimeout() * 1000));
				if (potentialPlayersAttackingMe.size() == 0)
				{
					potentialPlayersAttackingMe.add(container);
				}
			}
		}
	}

	@Subscribe
	private void onPlayerDespawned(PlayerDespawned playerDespawned)
	{
		PlayerContainer container = findPlayerInAttackerList(playerDespawned.getPlayer());
		PlayerContainer container2 = findPlayerInPotentialList(playerDespawned.getPlayer());
		if (container != null)
		{
			playersAttackingMe.remove(container);
		}
		if (container2 != null)
		{
			potentialPlayersAttackingMe.remove(container2);
		}
	}

	@Subscribe
	private void onPlayerSpawned(PlayerSpawned playerSpawned)
	{
		if (config.markNewPlayer())
		{
			Player p = playerSpawned.getPlayer();

			if (client.isFriended(p.getName(), true) && config.ignoreFriends())
			{
				return;
			}

			PlayerContainer container = findPlayerInPotentialList(p);
			if (container == null)
			{
				container = new PlayerContainer(p, System.currentTimeMillis(), (config.newSpawnTimeout() * 1000));
				potentialPlayersAttackingMe.add(container);
			}
		}
	}

	private PlayerContainer findPlayerInAttackerList(Player player)
	{
		if (playersAttackingMe.isEmpty())
		{
			return null;
		}
		for (PlayerContainer container : playersAttackingMe)
		{
			if (container.getPlayer() == player)
			{
				return container;
			}
		}
		return null;
	}


	@Subscribe
	private void onStatChanged(StatChanged c)
	{
		// walking on player
		//client.getLogger().debug("does this even go off?");
		//walk();
	}

	private PlayerContainer findPlayerInPotentialList(Player player)
	{
		if (potentialPlayersAttackingMe.isEmpty())
		{
			return null;
		}
		for (PlayerContainer container : potentialPlayersAttackingMe)
		{
			if (container.getPlayer() == player)
			{
				return container;
			}
		}
		return null;
	}

	/**
	 * Resets player timer in case he attacks again, so his highlight doesn't go away so easily
	 *
	 * @param container
	 */
	private void resetPlayerFromAttackerContainerTimer(PlayerContainer container)
	{
		removePlayerFromAttackerContainer(container);
		PlayerContainer newContainer = new PlayerContainer(container.getPlayer(), System.currentTimeMillis(), (config.attackerTargetTimeout() * 1000));
		playersAttackingMe.add(newContainer);
	}

	void removePlayerFromPotentialContainer(PlayerContainer container)
	{
		if ((potentialPlayersAttackingMe != null) && (!potentialPlayersAttackingMe.isEmpty()))
		{
			potentialPlayersAttackingMe.remove(container);
		}
	}

	void removePlayerFromAttackerContainer(PlayerContainer container)
	{
		if ((playersAttackingMe != null) && (!playersAttackingMe.isEmpty()))
		{
			playersAttackingMe.remove(container);
		}
	}

	private boolean isBlockAnimation(int anim)
	{
		switch (anim)
		{
			case 4177:
			case 420:
			case 1156:
			case 388:
			case 424:
				return true;
			default:
				return false;
		}
	}

	List<PlayerContainer> getPotentialPlayersAttackingMe()
	{
		return potentialPlayersAttackingMe;
	}

	List<PlayerContainer> getPlayersAttackingMe()
	{
		return playersAttackingMe;
	}

	//All of the methods below are from the Zulrah plugin!!! Credits to it's respective owner
	private void loadProtectionIcons()
	{
		for (int i = 0; i < PROTECTION_ICONS.length; i++)
		{
			final int resource = PROTECTION_ICONS[i];
			ProtectionIcons[i] = rgbaToIndexedBufferedImage(ProtectionIconFromSprite(spriteManager.getSprite(resource, 0)));
		}
	}

	private static BufferedImage rgbaToIndexedBufferedImage(final BufferedImage sourceBufferedImage)
	{
		final BufferedImage indexedImage = new BufferedImage(
			sourceBufferedImage.getWidth(),
			sourceBufferedImage.getHeight(),
			BufferedImage.TYPE_BYTE_INDEXED);

		final ColorModel cm = indexedImage.getColorModel();
		final IndexColorModel icm = (IndexColorModel) cm;

		final int size = icm.getMapSize();
		final byte[] reds = new byte[size];
		final byte[] greens = new byte[size];
		final byte[] blues = new byte[size];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);

		final WritableRaster raster = indexedImage.getRaster();
		final int pixel = raster.getSample(0, 0, 0);
		final IndexColorModel resultIcm = new IndexColorModel(8, size, reds, greens, blues, pixel);
		final BufferedImage resultIndexedImage = new BufferedImage(resultIcm, raster, sourceBufferedImage.isAlphaPremultiplied(), null);
		resultIndexedImage.getGraphics().drawImage(sourceBufferedImage, 0, 0, null);
		return resultIndexedImage;
	}

	private static BufferedImage ProtectionIconFromSprite(final BufferedImage freezeSprite)
	{
		final BufferedImage freezeCanvas = ImageUtil.resizeCanvas(freezeSprite, PROTECTION_ICON_DIMENSION.width, PROTECTION_ICON_DIMENSION.height);
		return ImageUtil.outlineImage(freezeCanvas, PROTECTION_ICON_OUTLINE_COLOR);
	}

	BufferedImage getProtectionIcon(WeaponType weaponType)
	{
		switch (weaponType)
		{
			case WEAPON_RANGED:
				return ProtectionIcons[0];
			case WEAPON_MELEE:
				return ProtectionIcons[1];
			case WEAPON_MAGIC:
				return ProtectionIcons[2];
		}
		return null;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("prayagainstplayer"))
		{
			return;
		}

		if (event.getKey().equals("mirrorMode"))
		{
			overlay.determineLayer();
			overlayPrayerTab.determineLayer();
			overlayManager.remove(overlay);
			overlayManager.remove(overlayPrayerTab);
			overlayManager.add(overlay);
			overlayManager.add(overlayPrayerTab);
		}
	}
}