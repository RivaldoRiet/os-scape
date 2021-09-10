package api;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

import net.runelite.api.MenuAction;
import net.runelite.api.VarClientStr;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuOptionClicked;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class MenuActions {

	private static Class<?> _class;
	private static Method action;
	private ClientContext ctx;

	public MenuActions(ClientContext ctx) {
		this.ctx = ctx;
	}

	public static void invoke(String option, String target, int id, int menuAction, int action, int widgetId) {
		ClientContext.instance().game.invokeMenuAction(action, widgetId, menuAction, id, option, target);
	}
	
	/*public static void invoke(String option, String target, int id, int menuAction, int action, int widgetId) {
		ClientContext.instance().game.invokeMenuAction(id, menuAction, action, widgetId, option, target);
		MenuOptionClicked menu = new MenuOptionClicked();
		menu.setActionParam(action);
		menu.setMenuOption(option);
		menu.setMenuTarget(target);
		menu.setMenuAction(MenuAction.of(menuAction));
		menu.setId(id);
		menu.setWidgetId(widgetId);
		invoke(menu); 
	}
	
	public static void invoke(MenuOptionClicked option) {
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
	}*/

	public static void clickNpc(SimpleNpc npc, MenuAction action) {
		try {
			if (npc == null) return;
			int index = npc.getNpc().getIndex();
			invoke("Attack", "", index, action.getId(), 0, 0);
		} catch (Exception e) {
		}

	}

	public static void clickItem(SimpleItem item, MenuAction action) {
		if (item == null) return;
		int index = item.getInventoryIndex();
		invoke("", "", item.getId(), action.getId(),index , 9764864);
	}

	public static boolean clickObject(SimpleObject object, MenuAction action) {
		if (object == null) return false;
		LocalPoint loc = object.getTileObject().getLocalLocation();
		invoke("", "", object.getId(), action.getId(), loc.getSceneX(), loc.getSceneY());
		return true;
	}

	public static void clickObject(SimpleObject object, MenuAction action, int sceneX, int sceneY) {
		if (object == null) return;
		invoke("", "", object.getId(), action.getId(), sceneX, sceneY);
	}

	public static void clickGroundItem(SimpleGroundItem item, MenuAction action) {
		if (item == null) return;
		LocalPoint loc = item.getTile().getLocalLocation();
		invoke("", "", item.getId(), action.getId(), loc.getSceneX(), loc.getSceneY());
	}

	public static void clickDialogue(int action) {
		invoke("", "", action, MenuAction.WIDGET_TYPE_6.getId(), 0, 14352385);
	}

	public static void clickInterface(int action) {
		invoke("", "", action, MenuAction.WIDGET_TYPE_6.getId(), 0, 12255235);
	}

	public static void click(SimpleItem item, String action) {
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

	public static void click(SimpleNpc npc, String str) {
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_FIRST_OPTION;
		String[] actions = npc.getNpc().getComposition().getActions();
		int actionIndex = IntStream.range(0, actions.length).filter(val -> {
			String action = actions[val];
			return action != null && action.toLowerCase().contains(str.toLowerCase());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) {
			System.out.println("Action index: " + actionIndex);
			return;
		}

		switch (actionIndex) {
			case 1:
				option = MenuAction.NPC_SECOND_OPTION;
				break;
			case 2:
				option = MenuAction.NPC_THIRD_OPTION;
				break;
			case 3:
				option = MenuAction.NPC_FOURTH_OPTION;
				break;
			case 4:
				option = MenuAction.NPC_FIFTH_OPTION;
				break;
			case 5:
				option = MenuAction.EXAMINE_NPC;
				break;
		}

		clickNpc(npc, option);
	}

	public static boolean click(SimpleObject object, String str) {
		if (object == null) return false;
		MenuAction option = MenuAction.GAME_OBJECT_FIRST_OPTION;
		String[] actions = object.getActions();
		int actionIndex = IntStream.range(0, actions.length).filter(val -> {
			String action = actions[val];
			return action != null && action.toLowerCase().contains(str.toLowerCase());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) return false;

		switch (actionIndex) {
			case 0:
				option = MenuAction.GAME_OBJECT_FIRST_OPTION;
				break;
			case 1:
				option = MenuAction.GAME_OBJECT_SECOND_OPTION;
				break;
			case 2:
				option = MenuAction.GAME_OBJECT_THIRD_OPTION;
				break;
			case 3:
				option = MenuAction.GAME_OBJECT_FOURTH_OPTION;
				break;
			case 4:
				option = MenuAction.GAME_OBJECT_FIFTH_OPTION;
				break;
		}
		return clickObject(object, option);
	}

	public static boolean withdraw(SimpleItem item, String action) {
		if (item == null) return false;
		ClientContext ctx = ClientContext.instance();
		int length = ctx.bank.populate().population();
		SimpleItem[] items = ctx.bank.toStream().toArray(SimpleItem[]::new);
		int actionIndex = IntStream.range(0, length).filter(val -> {
			return items[val] != null && items[val].getName().equalsIgnoreCase(item.getName());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) return false;
		int option = 1;
		switch (action) {
			case "1":
				option = 2;
				break;
			case "5":
				option = 3;
				break;
			case "10":
				option = 4;
				break;
			case "all":
				option = 7;
				break;
			default:
				option = 5;
				int amt = Integer.parseInt(action);
				if (ctx.bank.withdrawXAmount() != amt) {
					MenuActions.invoke("", "", -1, 57, 2, 786465);
					ctx.onCondition(() -> ctx.dialogue.pendingInput());
					ctx.getClient().setVar(VarClientStr.INPUT_TEXT, amt + "");
					ctx.keyboard.sendKeys("", true);
					ctx.onCondition(() -> ctx.bank.withdrawXAmount() == amt);
				}
				break;
		}
		invoke("", "", actionIndex, 57, option, 786444);
		return true;
	}

	public void itemOnNPC(SimpleItem item, SimpleNpc npc) {
		if (item == null || npc == null) return;
		if (ctx.inventory.itemSelectionState() == 0) click(item, "Use");

		invoke("", "", 0, MenuAction.ITEM_USE_ON_NPC.getId(), npc.getNpc().getIndex(), 0);
	}

	public void itemOnObject(SimpleItem item, SimpleObject object) {
		if (item == null || object == null) return;
		if (ctx.inventory.itemSelectionState() == 0) click(item, "Use");

		LocalPoint loc = object.getTileObject().getLocalLocation();
		invoke("", "", loc.getSceneX(), MenuAction.ITEM_USE_ON_GAME_OBJECT.getId(), object.getId(), loc.getSceneY());
	}

	public static void click(SimpleGroundItem item, String action) {
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
