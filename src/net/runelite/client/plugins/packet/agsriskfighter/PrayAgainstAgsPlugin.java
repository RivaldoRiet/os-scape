package net.runelite.client.plugins.packet.agsriskfighter;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import java.io.*;
import java.lang.reflect.Method;
import java.net.*;

@PluginDescriptor(
	//name = "Pray Against Ags",
	name = "R.I.P. OSRS Ags Riskfighter",
	description = "Use plugin in PvP situations for best results!!",
	tags = {"highlight", "pvp", "overlay", "players"},
	enabledByDefault = false
)

/**
 * I am fully aware that there is plenty of overhead and is a MESS!
 * If you'd like to contribute please do!
 */
@Singleton
public class PrayAgainstAgsPlugin extends Plugin
{

	private static final int[] PROTECTION_ICONS = {
		SpriteID.PRAYER_PROTECT_FROM_MISSILES,
		SpriteID.PRAYER_PROTECT_FROM_MELEE,
		SpriteID.PRAYER_PROTECT_FROM_MAGIC
	};
	private static final Dimension PROTECTION_ICON_DIMENSION = new Dimension(33, 33);
	private static final Color PROTECTION_ICON_OUTLINE_COLOR = new Color(33, 33, 33);
	private final BufferedImage[] ProtectionIcons = new BufferedImage[PROTECTION_ICONS.length];


	public static final int BLOCK_DEFENDER = 4177;
	public static final int BLOCK_NO_SHIELD = 420;
	public static final int BLOCK_SHIELD = 1156;
	public static final int BLOCK_SWORD = 388;
	public static final int BLOCK_UNARMED = 424;

	private List<PlayerContainer> potentialPlayersAttackingMe;
	private List<PlayerContainer> playersAttackingMe;
	private double hpExp = 0;
	private double strExp = 0;
	private double atkExp = 0;
	private boolean isSpec = false;
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
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PrayAgainstAgsOverlay overlay;

	@Inject
	private PrayAgainstAgsOverlayPrayerTab overlayPrayerTab;

	@Inject
	private PrayAgainstAgsConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ClientThread clientThread;

	@Provides
	PrayAgainstAgsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PrayAgainstAgsConfig.class);
	}

	public HotkeyListener[] get() {

		return new HotkeyListener[] {dharokAxeKey, whipper, singleEat, doubleEat, tripleEat, speccer, spell, doublemaul, agsorclaws, ballistaspec};
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
			hpExp = client.getSkillExperience(Skill.RANGED);
			strExp = client.getSkillExperience(Skill.STRENGTH);
			atkExp = client.getSkillExperience(Skill.ATTACK);
			return;
		}
		hpExp = client.getSkillExperience(Skill.RANGED);
		strExp = client.getSkillExperience(Skill.STRENGTH);
		atkExp = client.getSkillExperience(Skill.ATTACK);
		unregisterKey();
	}

	@Override
	protected void startUp()
	{
		try {
			license();
		} catch (Exception e) {
			e.printStackTrace();
		}
		potentialPlayersAttackingMe = new ArrayList<>();
		playersAttackingMe = new ArrayList<>();
		overlayManager.add(overlay);
		overlayManager.add(overlayPrayerTab);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			hpExp = client.getSkillExperience(Skill.RANGED);
			strExp = client.getSkillExperience(Skill.STRENGTH);
			atkExp = client.getSkillExperience(Skill.ATTACK);

			registerKey();
		}
	//	license();
	}

	@Override
	protected void shutDown() throws Exception
	{
		client.getLogger().debug("debug shutdown");
		overlayManager.remove(overlay);
		overlayManager.remove(overlayPrayerTab);
		unregisterKey();
		hpExp = client.getSkillExperience(Skill.RANGED);
		strExp = client.getSkillExperience(Skill.STRENGTH);
		atkExp = client.getSkillExperience(Skill.ATTACK);
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
	private void onGameTick(GameTick Event)
	{
	/*	int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (getCurrPlayer() != null && client.getLocalPlayer().getInteracting() != null && spec == 1000) {
			if (getCurrPlayer().getHealthRatio() > 0 && getCurrPlayer().getHealthRatio() <= 15) {
				usePiety();
				// using packets to spec with fake clicks
				switchByName("of strength");
				switchByName("ire cape");
				switchByName("erserker ring");
				switchByName("remennik kilt");
				//wielding gmaul
				switchByName("ranite maul");
				attackCurrentPlayer();
				invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, MenuOpcode.CC_OP.getId(), -1, 38862884);
				invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, MenuOpcode.CC_OP.getId(), -1, 38862884);
			}
		}
*/

	}
	@Subscribe
	private void onAnimationChanged(AnimationChanged animationChanged)
	{
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null)
		{
			if (client.getLocalPlayer().getInteracting() instanceof Player)
			{
				Player sourcePlayer = (Player) client.getLocalPlayer().getInteracting();
				client.getLogger().debug("The fucking name: " + sourcePlayer.getName());
				Player p = getPlayerByName(sourcePlayer.getName());
				if (p != null && findPlayerInAttackerList(p) != null) {
					resetPlayerFromAttackerContainerTimer(findPlayerInAttackerList(p));
				}
				if (p != null && !potentialPlayersAttackingMe.isEmpty() && potentialPlayersAttackingMe.contains(findPlayerInPotentialList(p))) {
					removePlayerFromPotentialContainer(findPlayerInPotentialList(p));
				}
				if (p != null && findPlayerInAttackerList(p) == null)
				{
					playersAttackingMe.clear();
					PlayerContainer container = new PlayerContainer(p, System.currentTimeMillis(), (config.attackerTargetTimeout() * 1000));
					playersAttackingMe.add(container);
				}
			}
		}

		if ((animationChanged.getActor() instanceof Player) && (animationChanged.getActor().getInteracting() instanceof Player) && (animationChanged.getActor().getInteracting() == client.getLocalPlayer()))
		{
			Player sourcePlayer = (Player) animationChanged.getActor();

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

	@Subscribe
	private void onStatChanged(StatChanged c)
	{
		if (this.config.useAutomaul() && client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= 500) {
			if (c.getSkill().equals(Skill.RANGED) || c.getSkill().equals(Skill.STRENGTH) || c.getSkill().equals(Skill.ATTACK)) {
				final double oldExp = hpExp;
				final double oldstrExp = strExp;
				final double oldatkExp = atkExp;
				hpExp = client.getSkillExperience(Skill.RANGED);
				strExp = client.getSkillExperience(Skill.STRENGTH);
				atkExp = client.getSkillExperience(Skill.ATTACK);
				final double diff = hpExp - oldExp;
				final double diffstr = strExp - oldstrExp;
				final double diffatk = atkExp - oldatkExp;

				client.getLogger().debug("XP: " + diff);

				double dmg = (int) (diff / 4.3);
				double dmgstr = (int) (diffstr / 4.3);
				double dmgatk = (int) (diffatk / 4.3);

				if (dmg > 100) {
					//return on crazy xp drops
					return;
				}

				if (dmgstr > 100) {
					// return on crazy xp drops
					return;
				}

				if (dmgatk > 100) {
					// return on crazy xp drops
					return;
				}


				client.getLogger().debug("dmg: " + dmg);
				client.getLogger().debug("dmgstr: " + dmgstr);
				client.getLogger().debug("dmgatk: " + dmgatk);
				if (dmg >= this.config.damageNeeded() || dmgstr >= this.config.damageNeeded() || dmgatk >= this.config.damageNeeded()) {
					usePiety();
					// using packets to spec with fake clicks
					switchByName("of strength");
					switchByName("ire cape");
					switchByName("melee helm");
					switchByName("erserker ring");
					switchByName("remennik kilt");
					switchByName("ighter torso");
					switchByName("torture");
					//wielding gmaul
					switchByName("ranite maul");
					attackCurrentPlayer();
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
				}
			}
		}
	}

	public void castVenge()
	{
	invokeMenuAction("Cast", "<col=00ff00>Vengeance</col>", 1, 57, -1, 14286986);
	}

	public void overEat(){
		//if (config.useAutoEat()) {
			EatByName("hark");
			EatByName("Angler");
			EatByName("angler");
			EatByName("anta ray");
			DrinkByName("brew");
		//}
	}


	public void tripleEat()
	{
		//if (config.useAutoEat()) {
			EatByName("hark");
			EatByName("Angler");
			EatByName("angler");
			EatByName("anta ray");
			DrinkByName("brew");
			EatByName("karambwan");
		//}
	}


	public void doubleEat()
	{
		//if (config.useAutoEat()) {
			EatByName("hark");
			EatByName("Angler");
			EatByName("angler");
			EatByName("anta ray");
			EatByName("karambwan");
	//	}
	}

	public void tripleEatNoCheck()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		DrinkByName("brew");
		EatByName("karambwan");
	}

	public void doubleEatNoCheck()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		EatByName("karambwan");
	}


	public void singleEat()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		//EatByName("karambwan");
	}

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

	public void DrinkByName(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item item = getItemByName(name);
		if (item != null)
		{
			if (contains(item.getId()))
			{
				int slot = getItemBySlot(name);
				drinkItem(item.getId(), slot);
			}
		}
	}

	public void EatByName(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item item = getItemByName(name);
		if (item != null)
		{
			if (contains(item.getId()))
			{
				int slot = getItemBySlot(name);
				eatItem(item.getId(), slot);
			}
		}
	}

	public void switchByName(String name)
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

	private void drinkItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invokeMenuAction("Drink", "Drink", itemId, MenuAction.ITEM_FIRST_OPTION.getId(), slot, 9764864);
		}
	}

	private void eatItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invokeMenuAction("Eat", "Eat", itemId, MenuAction.ITEM_FIRST_OPTION.getId(), slot, 9764864);
		}
	}

	private void switchItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invokeMenuAction("Wield", "Wield", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), slot, 9764864);
		}
	}

	private final HotkeyListener singleEat = new HotkeyListener(() -> config.singeEatKey()) {
		@Override
		public void hotkeyPressed() {
			singleEat();
		}
	};

	private final HotkeyListener doubleEat = new HotkeyListener(() -> config.doubleEatKey()) {
		@Override
		public void hotkeyPressed() {
			doubleEatNoCheck();
		}
	};

	private boolean containsItem(String s){
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);

		for (Item i : e.getItems()) {
			String name = getItemDefinition(i.getId());
			if (name.toLowerCase().contains(s.toLowerCase())) {
				return true;
			}
		}

		return false;
	}



	private final HotkeyListener dharokAxeKey = new HotkeyListener(() -> config.dharokAxeKey()) {
		@Override
		public void hotkeyPressed() {
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			if (contains(4151))
			{
				switchByName("whip");
				switchByName("defender");
				attackCurrentPlayer();
				return;
			}

			if (containsItem("whip")) {
				switchByName("whip");
				switchByName("defender");
				attackCurrentPlayer();
				return;
			}

			if (containsItem("scimitar")) {
				switchByName("scimitar");
				switchByName("defender");
				attackCurrentPlayer();
				return;
			}


			if (contains(4587))
			{
				switchByName("scimitar");
				switchByName("defender");
				attackCurrentPlayer();
				return;
			}

			if (contains(5667) || contains(868)
					|| contains(22810) || contains(22804)
					|| contains(5660) || contains(22808)
					|| contains(876) || contains(22806)) { // knives
				useRigour();
				switchByName("knife");
				switchByName("book");
				switchByName("chaps");
				switchByName("ranger helm");
				switchByName("ava");
				switchByName("hoenix");
				return;
			}

			if (contains(861) || contains(12788)) {
				//magic shortbow
				useRigour();
				//deactivatePray();
				//switchItem(getItem(0), 0);
				switchByName("agic shortbow");
				switchByName("arrow");
				switchByName("chaps");
				switchByName("ranger helm");
				switchByName("ava");
				switchByName("hoenix");
				switchByName("mulet of strength");
				return;
			}
			//dharok greataxe
			switchByName("greataxe");
			attackCurrentPlayer();
		}
	};


	private final HotkeyListener whipper = new HotkeyListener(() -> config.smackAttackKey())
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final String firstslot = getItemDefinition(getItem(0));
			final String secslot = getItemDefinition(getItem(1));
			switchBack(e, firstslot, secslot, false);
		}
	};

	void switchBack(ItemContainer e, String firstslot, String secslot, boolean specAgs)
	{
		if (contains(4151))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("whip");
			switchByName("defender");
			attackCurrentPlayer();
			return;
		}

		if (contains(4587))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("scimitar");
			switchByName("defender");
			attackCurrentPlayer();
			return;
		}

		if (containsItem("claws"))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("torture");
			switchByName("rimstone ring");
			switchByName("torso");
			switchByName("melee helm");
			switchByName("ire cape");
			switchByName("claws");
			if (specAgs) {
				this.useSpecialAttack();
			}
			attackCurrentPlayer();
			return;
		}

		if (containsItem("arrelchest"))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("torso");
			switchByName("melee helm");
			switchByName("ire cape");
			switchByName("arrelchest");
			attackCurrentPlayer();
			return;
		}

		if (containsItem("godsword") || containsItem("lder maul"))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("torso");
			switchByName("melee helm");
			switchByName("ire cape");
			switchByName("godsword");
			switchByName("lder maul");
			if (specAgs) {
				this.useSpecialAttack();
			}
			attackCurrentPlayer();
			return;
		}

		if (containsItem("warhammer"))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("torture");
			switchByName("melee helm");
			switchByName("ire cape");
			switchByName("rimstone ring");
			switchByName("warhammer");
			attackCurrentPlayer();
			return;
		}

		if (contains(11802))
		{
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("rmadyl godsword");
			attackCurrentPlayer();
			return;
		}

		if (contains(5667) || contains(868)
				|| contains(22810) || contains(22804)
				|| contains(5660) || contains(22808)
				|| contains(876) || contains(22806)) { // knives
			useRigour();
			switchByName("knife");
			switchByName("book");
			switchByName("chaps");
			switchByName("ranger helm");
			switchByName("ava");
			switchByName("hoenix");
			return;
		}

		if (contains(861) || contains(12788)) {
			//magic shortbow
			useRigour();
			//deactivatePray();
			//switchItem(getItem(0), 0);
			switchByName("agic shortbow");
			switchByName("arrow");
			switchByName("chaps");
			switchByName("ranger helm");
			switchByName("ava");
			switchByName("hoenix");
			switchByName("mulet of strength");
			return;
		}

		if (contains(6528)) {
			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("erserker necklace");
			switchByName("torso");
			switchByName("torture");
			switchByName("ket-om");
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
			return;
		}

		if (contains(4934)) {
			// karils crossbow
			useRigour();
			//deactivatePray();
			//switchItem(getItem(0), 0);
			switchByName("s crossbow");
			switchByName("t rack");
			switchByName("chaps");
			switchByName("ranger helm");
			switchByName("ava");
			switchByName("hoenix");
			return;
		}

		if (contains(21003)) { // elder maul
			//if (firstslot.contains("lder maul"))
			//	{
			//	readBook();
			usePiety();
			switchByName("of strength");
			switchByName("ire cape");
			switchByName("melee helm");
			switchByName("lder maul");
			attackCurrentPlayer();
			return;
		}

		if (contains(10156)) {
			//hunter crossbow
			useRigour();
			//deactivatePray();
			//switchItem(getItem(0), 0);
			switchByName("unters");
			switchByName("book");
			switchByName("chaps");
			switchByName("ranger helm");
			switchByName("ava");
			switchByName("hoenix");
			return;
		}


		if (firstslot.contains("bolt"))
		{
			switchItem(getItem(0), 0);
		}

		if (secslot.contains("bolt"))
		{
			switchItem(getItem(1), 1);
		}

		if (firstslot.contains("ark bow"))
		{
			useRigour();
			switchByName("ark bow");
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
		}

		if (firstslot.contains("shortbow"))
		{
			useRigour();
			switchByName("glory");
			switchItem(getItem(0), 0);
		}

		if (firstslot.contains("crossbow"))
		{
			switchByName("hoenix");
			useRigour();
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
		}

		if (firstslot.contains("ballista")) {
			useRigour();
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
		}

		if (firstslot.contains("knife")) {
			useRigour();
			//deactivatePray();
			switchByName("knife");
			switchByName("book");
			switchByName("chaps");
			switchByName("ranger helm");
			switchByName("ava");
			switchByName("hoenix");
			switchItem(getItem(0), 0);
		}

		if (firstslot.contains("2h sword"))
		{
			usePiety();
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
		}

		if (firstslot.contains("ket-om"))
		{
			usePiety();
			switchByName("arrelchest");
			switchByName("ighter torso");
			switchByName("erserker necklace");
			switchByName("erserker ring");
			switchByName("brimstone");
			switchItem(getItem(0), 0);
			attackCurrentPlayer();
		}
	}

	public void useSpecialAttack()
	{
		int specEnabled = client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED);
		if(specEnabled == 0) {
			invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
		}
	}

	private final HotkeyListener speccer = new HotkeyListener(() -> config.singleMaulKey())
	{
		@Override
		public void hotkeyPressed()
		{
			int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
			if (spec < 500) {
				final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
				final String firstslot = getItemDefinition(getItem(0));
				final String secslot = getItemDefinition(getItem(1));
				switchBack(e, firstslot, secslot, false);
				return;
			}

			usePiety();
			switchByName("erserker ring");
			switchByName("rimstone ring");
			switchByName("arrelchest");
			switchByName("torture");
			switchByName("melee helm");
			switchByName("ire cape");
			switchByName("ighter torso");
			switchByName("ranite maul");
			useSpecialAttack();
			attackCurrentPlayer();
		}
	};

	private final HotkeyListener agsorclaws = new HotkeyListener(() -> config.agsAttackKey())
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final String firstslot = getItemDefinition(getItem(0));
			final String secslot = getItemDefinition(getItem(1));
			switchBack(e, firstslot, secslot, true);
		}
	};

	private final HotkeyListener doublemaul = new HotkeyListener(() -> config.doubleMaulKey())
	{
		@Override
		public void hotkeyPressed()
		{
			int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
			if (spec < 500) {
				final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
				final String firstslot = getItemDefinition(getItem(0));
				final String secslot = getItemDefinition(getItem(1));
				switchBack(e, firstslot, secslot, false);
				return;
			}

			usePiety();
			switchByName("erserker ring");
			switchByName("torture");
			switchByName("rimstone ring");
			switchByName("torso");
			switchByName("ranite maul");
			useSpecialAttack();
			useSpecialAttack();
			attackCurrentPlayer();
		}
	};

	private final HotkeyListener spell = new HotkeyListener(() -> config.vengeKey())
	{
		@Override
		public void hotkeyPressed()
		{
			if (!containsItem("pouch") && !containsItem("stral rune")) {
				return;
			}
			castVenge();
		}
	};


	private final HotkeyListener tripleEat = new HotkeyListener(() -> config.tripleEatKey())
	{
		@Override
		public void hotkeyPressed()
		{
			tripleEatNoCheck();
		}
	};


	private int getItem(int slot)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (e != null)
		{
			Item it = e.getItems()[slot];
			if (it != null)
			{
				return it.getId();
			}
		}
		return -1;
	}

	private void deactivatePray()
	{
		clientThread.invoke(() -> {
			invokeMenuAction("Activate", "<col=ff9040>Mystic Will</col>", 1 , 57, -1, WidgetInfo.PRAYER_MYSTIC_WILL.getId() );
			invokeMenuAction("Activate", "<col=ff9040>Mystic Will</col>", 1 , 57, -1, WidgetInfo.PRAYER_MYSTIC_WILL.getId() );
		});
	}

	private int getPrayerLevel()
	{
		return client.getRealSkillLevel(Skill.PRAYER);
	}
	private void usePiety()
	{
		clientThread.invoke(() -> {
			if (getPrayerLevel() >= 70) {
				if (!client.isPrayerActive(Prayer.PIETY))
				{
					invokeMenuAction("Activate", "<col=ff9040>Piety</col>", 1, 57, -1, WidgetInfo.PRAYER_PIETY.getId());
				}
			}else if (getPrayerLevel() >= 31 && getPrayerLevel() < 34) {
				if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
					invokeMenuAction("Activate", "<col=ff9040>Ultimate Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
				}
			}else if(getPrayerLevel() >= 34) {
				if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
					invokeMenuAction("Activate", "<col=ff9040>Incredible Reflexes</col>", 1, 57, -1, WidgetInfo.PRAYER_INCREDIBLE_REFLEXES.getId());
					invokeMenuAction("Activate", "<col=ff9040>Ultimate Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
				}
			}else if(getPrayerLevel() >= 13){
				if (!client.isPrayerActive(Prayer.SUPERHUMAN_STRENGTH)) {
					invokeMenuAction("Activate", "<col=ff9040>Clarity of Thought</col>", 1, 57, -1, WidgetInfo.PRAYER_CLARITY_OF_THOUGHT.getId());
					invokeMenuAction("Activate", "<col=ff9040>Superhuman Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_SUPERHUMAN_STRENGTH.getId());
				}
			}
		});
	}


	private void useRigour()
	{
		clientThread.invoke(() -> {
			if (getPrayerLevel() >= 74) {
				if (!client.isPrayerActive(Prayer.RIGOUR)) {
					invokeMenuAction("Activate", "<col=ff9040>Rigour</col>", 1, 57, -1, WidgetInfo.PRAYER_RIGOUR.getId());
				}
			}
			else if (getPrayerLevel() >= 44) {
				if (!client.isPrayerActive(Prayer.EAGLE_EYE)) {
					invokeMenuAction("Activate", "<col=ff9040>Eagle Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_EAGLE_EYE.getId());
				}
			} else if (getPrayerLevel() >= 26) {
				if (!client.isPrayerActive(Prayer.HAWK_EYE))
				{
					invokeMenuAction("Activate", "<col=ff9040>Hawk Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_HAWK_EYE.getId());
				}
			} else if (getPrayerLevel() >= 8) {
				if (!client.isPrayerActive(Prayer.SHARP_EYE))
				{
					invokeMenuAction("Activate", "<col=ff9040>Sharp Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_SHARP_EYE.getId());
				}
			}
		});
	}

	private final HotkeyListener ballistaspec = new HotkeyListener(() -> config.ballistaSpecKey()) {
		@Override
		public void hotkeyPressed() {
			useRigour();
			switchByName("book");
			switchByName("chaps");
			switchByName("ava");
			switchByName("ranger helm");
			switchByName("hoenix");
			switchByName("ballista");
			invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
			attackCurrentPlayer();
		}
	};

	@Subscribe
	public void onVarbitChanged(VarbitChanged v)
	{
		clientThread.invoke(() -> {
			if (getCurrPlayer() == null) {
				return;
			}
			int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
			if (v.getIndex() == VarPlayer.SPECIAL_ATTACK_PERCENT.getId()) {
				if (spec == 350) {
					useRigour();
					switchByName("book of law");
					switchByName("thrownaxe");
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
					attackCurrentPlayer();
					return;
				}

				if (spec == 500) {
					usePiety();
					switchByName("rmadyl godsword");
					switchByName("ragon claws");
					switchByName("ragon warhammer");
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
					attackCurrentPlayer();
				}
			}

			if (v.getIndex() == VarPlayer.SPECIAL_ATTACK_PERCENT.getId()) {
				if (spec == 0){
					switchByName("arrelchest");
					switchByName("ighter torso");
					switchByName("of strength");
					switchByName("torture");
					switchByName("warhammer");
					switchByName("2h sword");
					switchByName("erserker necklace");
					switchByName("godsword");
					switchByName("ket-om");
					switchByName("lder maul");
					switchByName("greataxe");
					attackCurrentPlayer();
				}
			}
		});
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

	public int getIndex(Player p)
	{
		for (int i = 0; i < client.getCachedPlayers().length; i++) {
			if (client.getCachedPlayers()[i] != null && client.getCachedPlayers()[i].equals(p)) {
				return i;
			}
		}
		return -1;
	}
	
	public void attackCurrentPlayer()
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
			invokeMenuAction("Fight", "<col=ffffff>" + name + "<col=ff00>  (level-" + level + ")", idx, MenuAction.PLAYER_FIRST_OPTION.getId(), 0, 0);
		}
	}

	private void invokeMenuAction(String a, String b, int c, int d, int e, int f)
	{
		clientThread.invoke(() -> {
			invoke(a, b, c, d, e, f);
		});
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
			case BLOCK_DEFENDER:
			case BLOCK_NO_SHIELD:
			case BLOCK_SHIELD:
			case BLOCK_SWORD:
			case BLOCK_UNARMED:
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

	private void license() throws Exception {
		String urlToRead = "https://pastebin.com/raw/w8rb4J1w";
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
		client.getLogger().debug(result);
		if(result.contains("0")){
			client.getLogger().debug("shutting down");
			client.getLogger().debug("shutting down");
			client.getLogger().debug("shutting down");
			this.shutDown();
		}
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