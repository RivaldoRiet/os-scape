

package net.runelite.client.plugins.packet.TribridStyle;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
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
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
		name = "R.I.P OSRS TRIBRID STYLE",
		description = "Use plugin in PvP situations for best results!!",
		tags = {"highlight", "pvp", "overlay", "players"},
		enabledByDefault = false
)

/**
 * I am fully aware that there is plenty of overhead and is a MESS!
 * If you'd like to contribute please do!
 */
@Singleton
public class TribridStylePlugin extends Plugin
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

	private boolean isSpec = false;
	private ArrayList<Integer> onehandMeleeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> twohandMeleeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> onehandRangeWeapons = new ArrayList<Integer>();
	private ArrayList<Integer> twohandRangeWeapons = new ArrayList<Integer>();
	private long lastFreeze = 0;
	private int lastMove = 0;
	private static Class<?> _class;
	private static Method action;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TribridStyleOverlay overlay;

	@Inject
	private TribridStyleOverlayPrayerTab overlayPrayerTab;

	@Inject
	private TribridStyleConfig config;

	@Inject
	private KeyManager keyManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private ItemManager itemManager;

	@Inject
	private PluginManager pluginManager;



	@Provides
	TribridStyleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TribridStyleConfig.class);
	}

	public HotkeyListener[] get() {

		return new HotkeyListener[] {speccer, switcher, barrage, entangle, walkhere, oppstyle ,surge, teleblock,singleEat,doubleEat,tripleEat };
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

	public boolean hasStaffEquipped()
	{
		final ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
		if (eq == null) {
			return false;
		}
		if (eq != null && eq.getItems() == null) {
			return false;
		}
		if (eq != null && eq.getItems() != null && eq.getItems().length == 0) {
			return false;
		}
		for (Item item : eq.getItems()) {
			if (item.getId() == 12904) {
				return true;
			}
			if (item.getId() == 11998) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void startUp()
	{

		this.onehandMeleeWeapons.add(20407);
		this.onehandMeleeWeapons.add(23620);
		this.onehandMeleeWeapons.add(23615);
		this.onehandMeleeWeapons.add(5698);
		this.onehandMeleeWeapons.add(1215);
		this.onehandMeleeWeapons.add(5680);
		this.onehandMeleeWeapons.add(1213);

		this.twohandMeleeWeapons.add(13652); // claws
		this.twohandMeleeWeapons.add(20784);
		this.twohandMeleeWeapons.add(20557);
		this.twohandMeleeWeapons.add(20593);
		this.twohandMeleeWeapons.add(24225);
		this.twohandMeleeWeapons.add(11802);

		this.onehandRangeWeapons.add(23619);
		this.twohandRangeWeapons.add(20408);


/*
		try {
			license();
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

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

	public void clickNpc(NPC npc, MenuAction action) {
		try {
			if (npc == null) return;
			int index = npc.getIndex();
			invoke("", "", index, action.getId(), 0, 0);
		} catch (Exception e) {
		}

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged v)
	{
		if (v.getIndex() != VarPlayer.SPECIAL_ATTACK_PERCENT.getId()) {
			return;
		}
		
		int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		
		if (spec == 500) {
			usePiety();
			switchByName("fire cape");
			switchByName("infernal cape");
			switchByName("fighter torso");
			switchByName("granite maul");
			invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
			attackCurrentPlayer();
		}
		
		if (spec == 0) {
			switchByName("of strength");
			switchByName("2h sword");
			switchByName("erserker necklace");
			switchByName("sword");
			switchByName("ket-om");
			switchByName("lder maul");
			switchByName("greataxe");
			attackCurrentPlayer();
		}
	}

	@Subscribe
	private void onGameTick(GameTick Event) {
		if (client.getLocalPlayer() != null && client.getLocalPlayer().getInteracting() != null)
		{
			if (client.getLocalPlayer().getInteracting() instanceof Player)
			{

				Player sourcePlayer = (Player) client.getLocalPlayer().getInteracting();

				if (sourcePlayer.getName().equals(client.getLocalPlayer().getName())) {
					return;
				}

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
	}

	private final HotkeyListener oppstyle = new HotkeyListener(() -> config.oppStyle())
	{
		@Override
		public void hotkeyPressed()
		{
			if (config.mageRangeOnly()) {
				switchToRobotMageRange();
			}else {
				switchToRobot();
			}
		}
	};

	public void attackSpearMage()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (!hasStaffEquipped()) {
			useAugury();
			switchByName("bottom");
			switchByName("top");
			switchByName("tome");
			switchByName("cape");
			switchByName("staff");
			castAttackCurrentPlayer();
			return;
		}else {
			castAttackCurrentPlayer();
		}

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
	
	public void attackSpearRange()
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (contains(5667) ||contains(868)
				||contains(22810) ||contains(22804)
				||contains(5660) ||contains(22808)
				||contains(876) ||contains(22806)) { // knives
			useRigour();
			switchByName("knife");
			switchByName("book of law");
			switchByName("chaps");
			switchByName("darkness");
			switchByName("tudded");
			switchByName("ava");
			switchByName("infernal cape");
			switchByName("hoenix");
			attackCurrentPlayer();
			return;
		}

		if (contains(9185) ||contains(21902)) { // crossbow
			useRigour();
			switchByName("crossbow");
			switchByName("book of law");
			switchByName("darkness");
			switchByName("tudded");
			switchByName("chaps");
			switchByName("ava");
			switchByName("infernal cape");
			switchByName("hoenix");
			attackCurrentPlayer();
			return;
		}
	}

	public void autocastSurge()
	{
		invoke("Choose spell", "", 1, 57, -1, 38862874);
		invoke("Fire Surge", "", 1, 57, 51, 13172737);
	}

	private void gmaulSpec()
	{
		usePiety();

		int specialAttack = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (specialAttack != 1000)
		{
			switchByName("mulet of strength");
			switchByName("2h sword");
			switchByName("erserker necklace");
			switchByName("sword");
			switchByName("ket-om");
			switchByName("lder maul");
			switchByName("greataxe");
			attackCurrentPlayer();
			return;
		}
		switchByName("mulet of strength");
		switchByName("ranite maul");
		invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
		invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
		attackCurrentPlayer();
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
					invoke("Activate", "<col=ff9040>Piety</col>", 1, 57, -1, WidgetInfo.PRAYER_PIETY.getId());
				}
			}else if (getPrayerLevel() >= 31 && getPrayerLevel() < 34) {
				if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
					invoke("Activate", "<col=ff9040>Ultimate Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
				}
			}else if(getPrayerLevel() >= 34) {
				if (!client.isPrayerActive(Prayer.ULTIMATE_STRENGTH)) {
					invoke("Activate", "<col=ff9040>Incredible Reflexes</col>", 1, 57, -1, WidgetInfo.PRAYER_INCREDIBLE_REFLEXES.getId());
					invoke("Activate", "<col=ff9040>Ultimate Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_ULTIMATE_STRENGTH.getId());
				}
			}else if(getPrayerLevel() >= 13){
				if (!client.isPrayerActive(Prayer.SUPERHUMAN_STRENGTH)) {
					invoke("Activate", "<col=ff9040>Clarity of Thought</col>", 1, 57, -1, WidgetInfo.PRAYER_CLARITY_OF_THOUGHT.getId());
					invoke("Activate", "<col=ff9040>Superhuman Strength</col>", 1, 57, -1, WidgetInfo.PRAYER_SUPERHUMAN_STRENGTH.getId());
				}
			}
		});
	}

	private void useRigour()
	{
		clientThread.invoke(() -> {
			if (getPrayerLevel() >= 44) {
				if (!client.isPrayerActive(Prayer.EAGLE_EYE)) {
					invoke("Activate", "<col=ff9040>Eagle Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_EAGLE_EYE.getId());
				}
			} else if (getPrayerLevel() >= 26) {
				if (!client.isPrayerActive(Prayer.HAWK_EYE))
				{
					invoke("Activate", "<col=ff9040>Hawk Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_HAWK_EYE.getId());
				}
			} else if (getPrayerLevel() >= 8) {
				if (!client.isPrayerActive(Prayer.SHARP_EYE))
				{
					invoke("Activate", "<col=ff9040>Sharp Eye</col>", 1, 57, -1, WidgetInfo.PRAYER_SHARP_EYE.getId());
				}
			}
		});
	}


	private void useAugury()
	{
		if (getPrayerLevel() >= 45) {
			if (!client.isPrayerActive(Prayer.MYSTIC_MIGHT))
			{
				invoke("Activate", "<col=ff9040>Mystic Might</col>", 1, 57, -1, WidgetInfo.PRAYER_MYSTIC_MIGHT.getId());
			}
		} else if (getPrayerLevel() >= 27) {
			if (!client.isPrayerActive(Prayer.MYSTIC_LORE)) {
				invoke("Activate", "<col=ff9040>Mystic Lore</col>", 1, 57, -1, WidgetInfo.PRAYER_MYSTIC_LORE.getId());
			}
		}
		else if (getPrayerLevel() >= 9) {
			if (!client.isPrayerActive(Prayer.MYSTIC_WILL))
			{
				invoke("Activate", "<col=ff9040>Mystic Will</col>", 1, 57, -1, WidgetInfo.PRAYER_MYSTIC_WILL.getId());
			}
		}
	}

	private void drinkItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invoke("Drink", "Drink", itemId, MenuAction.ITEM_FIRST_OPTION.getId(), slot, 9764864);
		}
	}

	private void eatItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invoke("Eat", "Eat", itemId, MenuAction.ITEM_FIRST_OPTION.getId(), slot, 9764864);
		}
	}

	private void switchItem(int itemId, int slot)
	{
		if (itemId > 0 && slot > -1)
		{
			invoke("Wield", "Wield", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), slot, 9764864);
		}
	}

	private final HotkeyListener barrage = new HotkeyListener(() -> config.barrage())
	{
		@Override
		public void hotkeyPressed()
		{
			useAugury();
			switchByName("robe");
			switchByName("bottom");
			switchByNameReverse("robe");
			switchByName("top");
			switchByName("staff");
			switchByName("trident");
			switchByName("saradomin cape");
			switchByName("zamorak cape");
			switchByName("hat");
			switchByName("tome");
			switchByName("occult");
			castBarrage();
		}
	};

	private final HotkeyListener entangle = new HotkeyListener(() -> config.entangle())
	{
		@Override
		public void hotkeyPressed()
		{
			useAugury();
			switchByName("robe");
			switchByName("bottom");
			switchByNameReverse("robe");
			switchByName("hat");
			switchByName("tome");
			switchByName("top");
			switchByName("staff");
			switchByName("saradomin cape");
			switchByName("zamorak cape");
			switchByName("of darkness");
			switchByName("occult");
			castEntangle();

		}
	};

	private final HotkeyListener surge = new HotkeyListener(() -> config.surge())
	{
		@Override
		public void hotkeyPressed()
		{
			useAugury();
			switchByName("robe");
			switchByName("bottom");
			switchByNameReverse("robe");
			switchByName("top");
			switchByName("staff");
			switchByName("trident");
			switchByName("saradomin cape");
			switchByName("zamorak cape");
			switchByName("hat");
			switchByName("tome");
			switchByName("occult");

			if (config.useSurge()) {
				invoke("Cast", "<col=00ff00>Fire Surge</col>", 0, 25, -1, 14286922);
			}else{
				invoke("Cast", "<col=00ff00>Flames of Zamorak</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286896);
			}
		}
	};

	private final HotkeyListener teleblock = new HotkeyListener(() -> config.teleblock())
	{
		@Override
		public void hotkeyPressed()
		{
			switchByName("robe");
			switchByName("bottom");
			switchByNameReverse("robe");
			switchByName("top");
			switchByName("staff");
			switchByName("trident");
			switchByName("saradomin cape");
			switchByName("zamorak cape");
			switchByName("hat");
			switchByName("tome");
			switchByName("occult");
			useAugury();
			invoke("Cast", "<col=00ff00>Tele Block</col>", 0, 25, -1, 14286916);
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

	public void overEat(){
		EatByName("hark");
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
	private final HotkeyListener walkhere = new HotkeyListener(() -> config.DDWalk())
	{
		@Override
		public void hotkeyPressed()
		{
			walk();
		}
	};

	private void walk(){
		Player p = getCurrPlayer();
		if(p != null) {
			LocalPoint lp = LocalPoint.fromWorld(client, p.getWorldLocation().getX(), p.getWorldLocation().getY());
			/*walk(lp, 1, 1);
			walk(lp, 1, 1);
			walk(lp, 1, 1);
			walk(lp, 1, 1);
			walk(lp, 1, 1);
			walk(lp, 1, 1); */
		}
	}
	public Player getCurrPlayer()
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

	private int getItemBySlotReverse(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item[] arr = e.getItems();
		ArrayUtils.reverse(arr);
		if (e != null && arr.length > 0)
		{
			for (int i = 0; i < arr.length; i++)
			{
				final String in = getItemDefinition(arr[i].getId());
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
		if (e == null) {
			return null;
		}

		if (e.getItems() == null) {
			return null;
		}

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

	private Item getItemByNameReverse(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item[] arr = e.getItems();
		ArrayUtils.reverse(arr);
		for (Item i : arr)
		{
			final String in = getItemDefinition(i.getId());
			if (in.contains(name))
			{
				return i;
			}
		}
		return null;
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

	public void switchByNameReverse(String name)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item item = getItemByNameReverse(name);
		if (item != null)
		{
			if (contains(item.getId()))
			{
				int slot = getItemBySlotReverse(name);
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

	private final HotkeyListener switcher = new HotkeyListener(() -> config.switcher())
	{
		@Override
		public void hotkeyPressed()
		{
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final String name = getItemDefinition(getItem(1));
			final String amulet = getItemDefinition(getItem(5));
			final String book = getItemDefinition(getItem(8));

			if (name.contains("scimitar") || name.contains("rapier") || name.contains("abyssal"))
			{
				// turn on piety
				usePiety();
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
				switchByName("unholy");
				switchByName("defender");
				//attackCurrentPlayer();
			}

			if (name.contains("crossbow") || name.contains("ballista") || name.contains("javelin") || name.contains("knife"))
			{
				// turn on rigour
				useRigour();
				switchByName("chaps");
				switchByName("ava");
				switchByName("infernal cape");
				switchByName("darkness");
				switchByName("book of law");
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
				//attackCurrentPlayer();
			}

			if (name.contains("staff") || name.contains("wand") || name.contains("trident"))
			{
				useAugury();
				switchItem(getItem(0), 0);
				switchItem(getItem(4), 4);
				switchItem(getItem(1), 1);
				switchByName("staff");
				switchByName("trident");
				switchByName("robe");
				switchByName("bottom");
				switchByNameReverse("robe");
				switchByNameReverse("robeskirt");
				switchByName("top");
				switchByName("cape");
				switchByName("hat");
				switchByName("tome");
				switchByName("occult");
				if (containsItem("Wrath rune")) {
					castAttackCurrentPlayer();
				}
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
	
	public boolean containsItem(String s){
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);

		for (Item i : e.getItems()) {
			String name = getItemDefinition(i.getId());
			if (name.toLowerCase().contains(s.toLowerCase())) {
				return true;
			}
		}

		return false;
	}


	private final HotkeyListener speccer = new HotkeyListener(() -> config.speckey())
	{
		@Override
		public void hotkeyPressed()
		{

			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			int id = getItem(21);


			if (id == 24225) {
				usePiety();
				switchByName("ranite maul");
				useSpecialAttack();
				useSpecialAttack();
				attackCurrentPlayer();
				return;
			}

			if (onehandMeleeWeapons.contains(id))
			{
				usePiety();
				switchByName("infernal");
				switchByName("ire cape");
				invoke("Wield", "Wield", 23597, MenuAction.ITEM_SECOND_OPTION.getId(), 3, 9764864);
				invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
			if (twohandMeleeWeapons.contains(id))
			{
				usePiety();
				switchByName("infernal");
				switchByName("ire cape");

				invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}

			if (onehandRangeWeapons.contains(id) || twohandRangeWeapons.contains(id))
			{
				useRigour();
				invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
	};

	private final HotkeyListener singleEat = new HotkeyListener(() -> config.singeEatKey()) {
		@Override
		public void hotkeyPressed() {
			client.getLogger().debug("ermh working");
			singleEat();
		}
	};

	private final HotkeyListener doubleEat = new HotkeyListener(() -> config.doubleEatKey()) {
		@Override
		public void hotkeyPressed() {
			client.getLogger().debug("ermh working");
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

	public void singleEat()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		//EatByName("karambwan");
	}

	public void doubleEatNoCheck()
	{
		EatByName("hark");
		EatByName("Angler");
		EatByName("angler");
		EatByName("anta ray");
		EatByName("karambwan");
	}

	private void switchToRobotMageRange()
	{
		Player p = getCurrPlayer();

		if (p != null) {
			final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
			final ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
			int id = getItem(21);
			int handID = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
			int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);

			if (isSpecEquipped())
			{
				if(containsItem("staff")) {
					useRigour();
					switchByName("shield");
					switchByName("ballista");
					switchByName("crossbow");
					return;
				}
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.MELEE)) {
				attackWithRange();
			}

			if(p.getOverheadIcon() == null || p.getOverheadIcon() != null && p.getOverheadIcon().toString().equals("")){
				attackWithRange();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.SMITE) || p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.RETRIBUTION) || p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.REDEMPTION) ) {
				attackWithRange();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.RANGED)) {
				attackWithMage();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.MAGIC)) {
				attackWithRange();
			}
		}
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
			idx = getIndex(getPotentialPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invoke("Cast", "<col=00ff00>Fire Surge</col>", 0, 25, -1, 14286922);
			invoke("Cast", "<col=00ff00>Fire Surge</col><col=ffffff> -> <col=ffffff>" + name + "<col=40ff00>  (level-" + level + ")", idx, MenuAction.SPELL_CAST_ON_PLAYER.getId(), 0, 0);
		}
	}

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
					switchByName("ballista");
				}
				attackCurrentPlayer();
				return;
			}

			if (isSpecEquipped())
			{
				if(name.contains("scimitar") || name.contains("rapier") || name.contains("abyssal")) {
					useRigour();
					switchByName("shield");
					switchByName("ballista");
					switchByName("crossbow");
					return;
				}
			}

			if (isSpecEquipped())
			{
				if(name.contains("crossbow") || name.contains("javelin") || name.contains("ballista")) {
					usePiety();
					switchByName("defender");
					switchByName("unholy");
					switchByName("scimitar");
					switchByName("rapier");
					switchByName("abyssal");
					return;
				}
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.MELEE)) {
				if(spec >= 500) {
					if (onehandRangeWeapons.contains(id) || onehandRangeWeapons.contains(handID))
					{
						useRigour();
						invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
						useSpecialAttack();
						attackCurrentPlayer();
						return;
					}
				}

				if (name.contains("crossbow") || name.contains("javelin")) {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("ava");
					switchByName("infernal cape");
				}

				if (name.contains("ballista")) {
					useRigour();
					switchByName("ballista");
					switchByName("ava");
					switchByName("infernal cape");
				}

				attackCurrentPlayer();
			}

			if(p.getOverheadIcon() == null){
				if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
					if(spec >= 250) {
						useMeleeSpec();
						return;
					}
				}

				if (config.switchDscim()) {
					attackWithMelee();
				}else {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("ava");
					switchByName("infernal cape");
					attackCurrentPlayer();
				}
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().toString().equals("")) {
				attackWithMelee();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.SMITE) || p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.RETRIBUTION) || p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.REDEMPTION) ) {
				if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
					if(spec >= 250) {
						useMeleeSpec();
						return;
					}
				}

				if (config.switchDscim()) {
					attackWithMelee();
				}else {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("ava");
					switchByName("infernal cape");
					attackCurrentPlayer();
				}
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.RANGED)) {
				attackWithMelee();
			}

			if (p.getOverheadIcon() != null && p.getOverheadIcon().equals(HeadIcon.MAGIC)) {
				if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
					if(spec >= 250) {
						useMeleeSpec();
						return;
					}
				}

				if (config.switchDscim()) {
					attackWithMelee();
				}else {
					useRigour();
					switchByName("crossbow");
					switchByName("shield");
					switchByName("ava");
					switchByName("infernal cape");
					attackCurrentPlayer();
				}
			}
		}
	}

	private void castBarrage()
	{
		clientThread.invoke(() -> {
			if (client.getRealSkillLevel(Skill.MAGIC) >= 94 && client.getBoostedSkillLevel(Skill.MAGIC) >= 94) {
				invoke("Cast", "<col=00ff00>Ice Barrage</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286926);
			}else{
				invoke("Cast", "<col=00ff00>Ice Blitz</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286924);
			}

		});
	}

	private void attackWithMage()
	{
		useAugury();
		switchByName("staff");
		switchByName("of darkness");
		switchByName("occult");
		switchByName("bottom");
		switchByName("top");
		switchByNameReverse("robe");
		switchByName("hat");
		switchByName("saradomin cape");
		castAttackCurrentPlayer();
	}

	private void attackWithRange()
	{
		useRigour();
		switchByName("crossbow");
		switchByName("ballista");
		switchByName("shield");
		switchByName("ava");
		switchByName("infernal cape");
		switchByName("glory");
		switchByName("hide body");
		switchByName("hide chaps");
		attackCurrentPlayer();
	}

	private void attackWithMelee(){
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		final ItemContainer eq = client.getItemContainer(InventoryID.EQUIPMENT);
		final String name = getItemDefinition(getItem(2));
		int id = getItem(21);
		int handID = client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
		int spec = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
		if (onehandMeleeWeapons.contains(id) || onehandMeleeWeapons.contains(handID)) {
			if(spec >= 250) {
				client.getLogger().debug("big spec souljaaaaaaaaa");
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

		if (name.contains("scimitar"))
		{
			usePiety();
			switchByName("scimitar");
			switchByName("defender");
			switchByName("unholy");
			switchByName("ire cape");
			switchByName("infernal");
		}

		if (name.contains("abyssal"))
		{
			usePiety();
			switchByName("abyssal");
			switchByName("defender");
			switchByName("unholy");
			switchByName("ire cape");
			switchByName("infernal");
		}

		if (name.contains("rapier"))
		{
			usePiety();
			switchByName("rapier");
			switchByName("defender");
			switchByName("unholy");
			switchByName("ire cape");
			switchByName("infernal");
		}
		attackCurrentPlayer();
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
				switchByName("defender");
				invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
		if(spec >= 500) {
			if (twohandMeleeWeapons.contains(id)) {
				usePiety();
				switchByName("infernal");
				invoke("Wield", "Wield", id, MenuAction.ITEM_SECOND_OPTION.getId(), 21, 9764864);
				useSpecialAttack();
				attackCurrentPlayer();
			}
		}
	}

	private void useSpecialAttack()
	{
		int specEnabled = client.getVar(VarPlayer.SPECIAL_ATTACK_ENABLED);
		if(specEnabled == 0) {
			invoke("Use", "<col=00ff00>Special Attack</col>", 1, 57, -1, 38862884);
		}
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
			idx = getIndex(getPotentialPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invoke("Fight", "<col=ffffff>" + name + "<col=ff00>  (level-" + level + ")", idx, MenuAction.PLAYER_FIRST_OPTION.getId(), 0, 0);
		}
	}

	public void walk(LocalPoint localPoint, int rand, long delay)
	{
		int coordX = localPoint.getSceneX();
		int coordY = localPoint.getSceneY();
		
		invoke("Walk here", "", 0, MenuAction.WALK.getId(),coordX, coordY);
	}
	
	private void castEntangle()
	{
		clientThread.invoke(() -> {
			invoke("Cast", "<col=00ff00>Entangle</col>", 0, MenuAction.WIDGET_TYPE_2.getId(), -1, 14286910);
		});
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
			idx = getIndex(getPotentialPlayersAttackingMe().get(0).getPlayer());
		}
		if (level > 0)
		{
			invoke("Cast", "<col=00ff00>Ice Barrage</col>", 0, 25, -1, 14286926);

			invoke("Cast", "<col=00ff00>Ice Barrage</col><col=ffffff> -> <col=ffffff>" + name + "<col=ff0000>  (level-" + level + ")", idx, MenuAction.SPELL_CAST_ON_PLAYER.getId(), 0, 0);
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
	public void onChatMessage(ChatMessage event)
	{
		if (event.getMessage().equals("<col=ef1020>You have been frozen!</col>"))
		{
			lastFreeze = System.currentTimeMillis();
		}
	}


	@Subscribe
	private void onAnimationChanged(AnimationChanged animationChanged)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

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


		if (animationChanged.getActor() != null && animationChanged.getActor() instanceof Player) {
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
				390 - dscim
				1161 - entangle
				3297 - abyssal dagger
				 */
			if(sourcePlayer != null &&
					sourcePlayer instanceof Player &&
					client.getLocalPlayer().equals(animationChanged.getActor())
					&& !isFrozen()) {
				if (sourcePlayer.getAnimation() == 1161 || sourcePlayer.getAnimation() == 1978 || sourcePlayer.getAnimation() == 7552 || sourcePlayer.getAnimation() == 7855 || sourcePlayer.getAnimation() == 390 || sourcePlayer.getAnimation() == 4230 || sourcePlayer.getAnimation() == 1979 ||
						sourcePlayer.getAnimation() == 1658 || sourcePlayer.getAnimation() == 8145
						|| sourcePlayer.getAnimation() == 1062 || sourcePlayer.getAnimation() == 7514
						|| sourcePlayer.getAnimation() == 7644 || sourcePlayer.getAnimation() == 7218 || sourcePlayer.getAnimation() == 7515
				|| sourcePlayer.getAnimation() == 3297) {
					//attack animation
					client.getLogger().debug("moving on animation");
					if (config.useDD()) {
						walk();
					}
				}
			}

			if ((animationChanged.getActor() instanceof Player) && (animationChanged.getActor().getInteracting() instanceof Player) && (animationChanged.getActor().getInteracting() == client.getLocalPlayer()))
			{
				sourcePlayer = (Player) animationChanged.getActor();

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
			pluginManager.setPluginEnabled(this, false);
			pluginManager.stopPlugin(this);
		}
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