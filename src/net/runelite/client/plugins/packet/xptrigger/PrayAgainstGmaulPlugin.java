package net.runelite.client.plugins.packet.xptrigger;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
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
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import simple.robot.api.ClientContext;
import net.runelite.client.plugins.PluginDependency;
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
import java.util.List;

@PluginDescriptor(
	name = "R.I.P OSRS - XP trigger Gmaul",
	//name = "Pray against gmaul",
	description = "Use plugin in PvP situations for best results!!",
	tags = {"highlight", "pvp", "overlay", "players"},
	enabledByDefault = false
)

/**
 * I am fully aware that there is plenty of overhead and is a MESS!
 * If you'd like to contribute please do!
 */
@Singleton
public class PrayAgainstGmaulPlugin extends Plugin
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
	public List<PlayerContainer> playersAttackingMe;
	private double hpExp = 0;
	private double strExp = 0;
	private double atkExp = 0;
	private double mageExp = 0;
	private boolean isSpec = false;
	private int lastMove = 0;
	private long anchorTime = 0;
	LocalPoint previouslp1 = null;
	LocalPoint previouslp2 = null;
	int dharokAxe = 4718;
	private static Class<?> _class;
	private static Method action;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PrayAgainstGmaulOverlay overlay;

	@Inject
	private PrayAgainstGmaulOverlayPrayerTab overlayPrayerTab;

	@Inject
	private PrayAgainstGmaulConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemManager itemManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ClientThread clientThread;

	@Provides
	PrayAgainstGmaulConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PrayAgainstGmaulConfig.class);
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

	public HotkeyListener[] get() {

		return new HotkeyListener[] {whipper, singleEat, doubleEat, tripleEat, spell, ballistaspec };
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
			registerKey();
			loadProtectionIcons();
			return;
		}
		unregisterKey();
	}

	@Override
	protected void startUp()
	{
		potentialPlayersAttackingMe = new ArrayList<>();
		playersAttackingMe = new ArrayList<>();
		overlayManager.add(overlay);
		overlayManager.add(overlayPrayerTab);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			registerKey();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(overlayPrayerTab);
		unregisterKey();
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
	public void singleEat()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
	//	EatByName("karambwan");
	}

	public void doubleEatNoCheck()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		EatByName("karambwan");
	}

	private final HotkeyListener singleEat = new HotkeyListener(() -> config.singeEatKey()) {
		@Override
		public void hotkeyPressed() {
			client.getLogger().debug("eat");
			singleEat();
		}
	};

	private final HotkeyListener doubleEat = new HotkeyListener(() -> config.doubleEatKey()) {
		@Override
		public void hotkeyPressed() {
			client.getLogger().debug("eat1");
			doubleEatNoCheck();
		}
	};

	private final HotkeyListener tripleEat = new HotkeyListener(() -> config.tripleEatKey()) {
		@Override
		public void hotkeyPressed() {
			tripleEatNoCheck();
		}
	};

	public void tripleEatNoCheck()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		DrinkByName("brew");
		EatByName("karambwan");
	}


	@Subscribe
	private void onStatChanged(StatChanged c)
	{
		final double oldExp = hpExp;
		final double oldstrExp = strExp;
		final double oldatkExp = atkExp;
		final double oldmageExp = mageExp;
		hpExp = client.getSkillExperience(Skill.RANGED);
		strExp = client.getSkillExperience(Skill.STRENGTH);
		atkExp = client.getSkillExperience(Skill.ATTACK);
		mageExp = client.getSkillExperience(Skill.MAGIC);
		
		if (c.getSkill().equals(Skill.RANGED) || c.getSkill().equals(Skill.STRENGTH) || c.getSkill().equals(Skill.ATTACK) || c.getSkill().equals(Skill.MAGIC))
		{
			if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) >= 500) {
				final double diff = hpExp - oldExp;
				final double diffstr = strExp - oldstrExp;
				final double diffatk = atkExp - oldatkExp;
				final double diffmage = mageExp - oldmageExp;

				ClientContext.instance().log("XP: " + diff);

				double dmg = (int) (diff / 4.3);
				double dmgstr = (int) (diffstr / 4.3);
				double dmgatk = (int) (diffatk / 4.3);
				double dmgmage = (int) ((diffmage - 50) / 2);

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

				if (dmgmage > 100 || dmgmage > 0 && ClientContext.instance().getClient().getVarbitValue(client.getVarps(), 4070) == 2) {
					// return on crazy xp drops
					return;
				}


				ClientContext.instance().log("dmg: " + dmg);
				ClientContext.instance().log("dmgstr: " + dmgstr);
			//	client.getLogger().debug("dmgmage: " + dmgmage);

				if (dmg >= this.config.damageNeeded() || dmgstr >= this.config.damageNeeded() || dmgatk >= this.config.damageNeeded() || dmgmage >= this.config.damageNeeded()) {
				//if (dmg >= this.config.damageNeeded() || dmgstr >= this.config.damageNeeded()) {
					//readBook();
					usePiety();
					// using packets to spec with fake clicks
					switchByName("of strength");
					switchByName("ire cape");
					switchByName("infernal cape");
					switchByName("melee helm");
					//wielding gmaul
					switchByName("ranite maul");
					attackCurrentPlayer();
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
					invokeMenuAction("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
				//	readBook();
				}
			}
		}
	}

	public void overEat(){
		EatByName("hark");
		EatByName("anta ray");
		EatByName("nglerfish");
		DrinkByName("brew");
	}


	public void tripleEat()
	{
		EatByName("hark");
		DrinkByName("brew");
		EatByName("karambwan");
	}


	public void doubleEat()
	{
		EatByName("hark");
		EatByName("karambwan");
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

	private final HotkeyListener ballistaspec = new HotkeyListener(() -> config.ballistaSpecKey()) {
		@Override
		public void hotkeyPressed() {
			useRigour();
			switchByName("book");
			switchByName("chaps");
			switchByName("ava");
			switchByName("hoenix");
			switchByName("ranger helm");
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
				}
			}
		});
	}


	private final HotkeyListener whipper = new HotkeyListener(() -> config.specKey())
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);

			final String firstslot = getItemDefinition(getItem(0));
			final String secslot = getItemDefinition(getItem(1));
/*
			if (containsMeleeName(firstslot)) {
				usePiety();
				switchByName("of strength");
				switchByName("ire cape");
				switchItem(getItem(0), 0);
				attackCurrentPlayer();
				return;
			}

			if (containsRangeName(firstslot)) {
				useRigour();
				switchByName("glory");
				switchItem(getItem(0), 0);
				return;
			}

 */
			if (contains(dharokAxe)) {
				switchByName("of strength");
				switchByName("ire cape");
				switchByName("greataxe");
				attackCurrentPlayer();
				return;
			}
			if (contains(4151)) {
				//whip
				switchByName("of strength");
				switchByName("ire cape");
				switchByName("defender");
				switchByName("whip");
				attackCurrentPlayer();
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

			if (firstslot.contains("of fire"))
			{
				useAugury();
				switchItem(getItem(0), 0);
				switchItem(getItem(1), 1);
				switchItem(getItem(4), 4);
				switchItem(getItem(5), 5);
				castAttackCurrentPlayer();
			}

			if (firstslot.contains("staff"))
			{
				useAugury();
				switchItem(getItem(0), 0);
				switchItem(getItem(1), 1);
				switchItem(getItem(4), 4);
				switchItem(getItem(5), 5);
				castAttackCurrentPlayer();
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
				switchByName("erserker necklace");
				switchItem(getItem(0), 0);
				attackCurrentPlayer();
			}
		}
	};

	private void readBook()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (contains(2167)) {
			invokeMenuAction("Read", "Read", 2167, 33, 23, 9764864);
		}
	}

	private void useAugury(){
		clientThread.invoke(() -> {
			int praylvl = getPrayerLevel();
			if (praylvl >= 45) {
				if (!client.isPrayerActive(Prayer.MYSTIC_MIGHT))
				{
					invokeMenuAction("Activate", "<col=ff9040>Mystic MIGHT</col>", 1 , 57, -1, WidgetInfo.PRAYER_MYSTIC_MIGHT.getId() );
				}
			}else if (praylvl >= 27) {
				if (!client.isPrayerActive(Prayer.MYSTIC_LORE))
				{
					invokeMenuAction("Activate", "<col=ff9040>Mystic Lore</col>", 1 , 57, -1, WidgetInfo.PRAYER_MYSTIC_LORE.getId() );
				}
			}else if(praylvl >= 9){
				if (!client.isPrayerActive(Prayer.MYSTIC_WILL))
				{
					invokeMenuAction("Activate", "<col=ff9040>Mystic Will</col>", 1 , 57, -1, WidgetInfo.PRAYER_MYSTIC_WILL.getId() );
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

	private void dance(){

	}

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

	public void castVenge()
	{
		invokeMenuAction("Cast", "<col=00ff00>Vengeance</col>", 1, 57, -1, 14286986);
	}


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
			if (getPrayerLevel() >= 44) {
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

	public int getIndex(Player p)
	{
		for (int i = 0; i < client.getCachedPlayers().length; i++) {
			if (client.getCachedPlayers()[i] != null && client.getCachedPlayers()[i].equals(p)) {
				return i;
			}
		}
		return -1;
	}
	
	private void castAttackCurrentPlayer()
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
			invokeMenuAction("Cast", "<col=00ff00>Fire Surge</col>", 0, 25, -1, 14286922);
			invokeMenuAction("Cast", "<col=00ff00>Fire Surge</col><col=ffffff> -> <col=ffffff>" + name + "<col=40ff00>  (level-" + level + ")", idx, MenuAction.SPELL_CAST_ON_PLAYER.getId(), 0, 0);
		}
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
		}
		if (getPlayersAttackingMe() != null && getPlayersAttackingMe().size() <= 0 && getPotentialPlayersAttackingMe() != null && getPotentialPlayersAttackingMe().size() > 0)
		{
			name = getPotentialPlayersAttackingMe().get(0).getPlayer().getName();
			level = getPotentialPlayersAttackingMe().get(0).getPlayer().getCombatLevel();
			idx = getIndex(getPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invoke("Fight", "<col=ffffff>" + name + "<col=ff00>  (level-" + level + ")", idx, MenuAction.PLAYER_FIRST_OPTION.getId(), 0, 0);
		}
	}

	private void invokeMenuAction(String a, String b, int c, int d, int e, int f)
	{
		clientThread.invoke(() -> {
			this.invoke(a, b, c, d, e, f);
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

	private boolean containsMeleeName(String name)
	{
		for (String s : meleeWeaponNames) {
			if (name.contains(s)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsRangeName(String name)
	{
		for (String s : rangedWeaponNames) {
			if (name.contains(s)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsMagicName(String name)
	{
		for (String s : magicWeaponNames) {
			if (name.contains(s)) {
				return true;
			}
		}
		return false;
	}

	private static final String[] meleeWeaponNames = {
			"sword",
			"scimitar",
			"dagger",
			"spear",
			"mace",
			"axe",
			"whip",
			"tentacle",
			"-ket-",
			"-xil-",
			"warhammer",
			"halberd",
			"claws",
			"hasta",
			"scythe",
			"maul",
			"anchor",
			"sabre",
			"excalibur",
			"machete",
			"dragon hunter lance",
			"event rpg",
			"silverlight",
			"darklight",
			"arclight",
			"flail",
			"granite hammer",
			"rapier",
			"bulwark"
	};

	private static final String[] rangedWeaponNames = {
			"bow",
			"blowpipe",
			"xil-ul",
			"knife",
			"dart",
			"thrownaxe",
			"chinchompa",
			"ballista",
			"javelin"
	};

	private static final String[] magicWeaponNames = {
			"staff",
			"trident",
			"wand",
			"dawnbringer"
	};
}