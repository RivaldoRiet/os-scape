package SlayerSlayer;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.game.ItemManager;

public class MenuActions {

	private Client client;
	private ItemManager itemManager;
	private static Class<?> _class;
	private static Method action;
	
	public MenuActions(Client client, ItemManager itemManager) {
		this.client = client;
		this.itemManager = itemManager;
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
	
	private String getItemDefinition(int itemId)
	{
		ItemComposition id = itemManager.getItemComposition(itemId);
		if (id != null)
		{
			return id.getName().toLowerCase();
		}
		return "";
	}
	
	public int getItemBySlot(String name)
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
	
	public int getItemBySlot(int id)
	{
		final ItemContainer e = client.getItemContainer(InventoryID.INVENTORY);
		if (e != null && e.getItems().length > 0)
		{
			for (int i = 0; i < e.getItems().length; i++)
			{
				if (e.getItems()[i].getId() == id)
				{
					return i;
				}
			}
		}
		return -1;
	}

	public void clickItem(Item item, MenuAction action) {
		if (item == null) return;
		int index = getItemBySlot(item.getId());
		invoke("", "", item.getId(), action.getId(), index, 9764864);
	}

	public void clickGroundItem(TileItem item, MenuAction action) {
		if (item == null) return;
		//LocalPoint loc = item.;
		//invoke("", "", loc.getSceneX(), action.getId(), item.getId(), loc.getSceneY());
	}

	public void clickDialogue(int action) {
		invoke("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), action, 14352385);
		// id = 30
	}
	
	public void clickContinue()
	{
		invoke("Continue","",0,30,-1,15138819);
		//14221315
	}

	public void clickInterface(int action) {
		invoke("", "", 0, MenuAction.WIDGET_TYPE_6.getId(), action, 12255235);
	}

	public void click(Item item, String action) {
		if (item == null) return;
		MenuAction option = MenuAction.ITEM_FIRST_OPTION;
		switch (action.toLowerCase()) {
			case "wield":
			case "wear":
				option = MenuAction.ITEM_SECOND_OPTION;
				break;
			case "rub":
				option = MenuAction.ITEM_THIRD_OPTION;
				break;
			case "check":
				option = MenuAction.ITEM_FOURTH_OPTION;
				break;
			case "drop":
				option = MenuAction.ITEM_DROP;
				break;
			case "examine":
				option = MenuAction.EXAMINE_ITEM;
				break;
			case "cancel":
				option = MenuAction.CANCEL;
				break;
			case "use":
				option = MenuAction.ITEM_USE;
				break;
			case "use with":
				option = MenuAction.ITEM_USE_ON_WIDGET;
				break;
		}
		clickItem(item, option);
	}

	public void clickFirst(NPC npc)
	{
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_FIRST_OPTION;

		clickNpc(npc, option);
	}
	
	public void click(NPC npc) {
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_SECOND_OPTION;

		clickNpc(npc, option);
	}
	
	public void clickSecond(NPC npc) {
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_THIRD_OPTION;

		clickNpc(npc, option);
	}

	public boolean clickObject(GameObject object, MenuAction action) {
		if (object == null) return false;
		invoke("", "", object.getId(), action.getId(), object.getSceneMinLocation().getX(), object.getSceneMinLocation().getY());
		return true;
	}

	public void clickObject(GameObject object, MenuAction action, int sceneX, int sceneY) {
		if (object == null) return;
		invoke("", "", object.getId(), action.getId(), sceneX, sceneY);
	}
	
	public boolean click(GameObject object) {
		if (object == null) return false;
		MenuAction option = MenuAction.GAME_OBJECT_SECOND_OPTION;
		
		return clickObject(object, option);
	}

	public boolean clickFirst(GameObject object) {
		if (object == null) return false;
		MenuAction option = MenuAction.GAME_OBJECT_FIRST_OPTION;
		
		return clickObject(object, option);
	}

	public void click(TileItem item, String action) {
		if (item == null) return;
		MenuAction option = MenuAction.GROUND_ITEM_THIRD_OPTION;
		switch (action.toLowerCase()) {
			case "examine":
				option = MenuAction.EXAMINE_ITEM_GROUND;
				break;
		}
		clickGroundItem(item, option);
	}

}
