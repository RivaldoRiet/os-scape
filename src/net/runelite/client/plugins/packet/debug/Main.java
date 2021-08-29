package net.runelite.client.plugins.packet.debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@PluginDescriptor(name = "Debug", description = "debug menu actions", tags = { "debug" }, enabledByDefault = false)
public class Main extends Plugin {

	@Inject
	@Nullable
	public Client client;
	@Inject
	@Getter
	private ItemManager itemManager;
	@Inject
	private ConfigManager configManager;
	
	private static Class<?> _class;
	private Method action;

	@Provides
	PacketConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(PacketConfig.class);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		String text =  '"' + event.getMenuOption() + '"' + "," + '"' + event.getMenuTarget() + '"' + "," + event.getId() + ","
				+ event.getMenuAction().getId() + "," + event.getActionParam() + "," + event.getWidgetId();

	/*	
		System.out.println(event.toString());
		*/
		System.out.println("------------EVENT-------------------");
		System.out.println(text); 
		System.out.println("-------------END------------------");
	}
	
	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned event)
	{
	}
	
	@Subscribe
	private void onNpcSpawned(NpcSpawned event)
	{
		

	}

	int tick = 0;
	@Subscribe
	public void onGameTick(GameTick event) {
		

		tick++;
	}
	
	public boolean isDialogueOpen()
	{
		final Widget npcDialogue = client.getWidget(231, 4);

		if (npcDialogue != null && !npcDialogue.isHidden())
		{
			return true;
		}
		return false;
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

	@Override
	protected void shutDown() throws Exception {

	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals("Packet"))
			return;
	}

	private void switchByName(String name) {
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		Item item = getItemByName(name);
		if (item != null) {
			int slot = getItemBySlot(name);
			switchItem(item.getId(), slot);
		}
	}

	private int getItemBySlot(String name) {
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (e != null && e.getItems().length > 0) {
			for (int i = 0; i < e.getItems().length; i++) {
				final String in = getItemDefinition(e.getItems()[i].getId());
				if (in.contains(name)) {
					return i;
				}
			}
		}
		return -1;
	}

	private Item getItemByName(String... name) {
		
		for (String n : name) {
			n.toLowerCase();
		}
		
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		for (Item i : e.getItems()) {
			final String in = getItemDefinition(i.getId());
			in.toLowerCase();
			List<String> itemList = Arrays.asList(name);  
			if (itemList.contains(in)) {
				return i;
			}
		}
		return null;
	}

	private String getItemDefinition(int itemId) {
		ItemComposition id = itemManager.getItemComposition(itemId);
		if (id != null) {
			return id.getName().toLowerCase();
		}
		return "";
	}

	private void switchItem(int itemId, int slot) {
		if (itemId > 0 && slot > -1) {
			invoke("Wield", "Wield", itemId, MenuAction.ITEM_SECOND_OPTION.getId(), slot, 9764864);
		}
	}

	@Override
	protected void startUp() throws Exception {
		System.out.println("Startup msg working");
	}

}