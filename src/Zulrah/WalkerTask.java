package Zulrah;

import api.MenuActions;
import api.Tasks;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class WalkerTask extends Task {
	private Zulrah main;
	public final String edge_wizard = "Teleports wizard";
	
	public WalkerTask(ClientContext ctx, Zulrah main) {
		super(ctx);
		this.main = main;
	}


	@Override
	public boolean condition() {
		return main.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation()) || main.containsFood() && !main.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation()) && !main.isInZulrahRoom() && !main.isInZulrahRoom();
	}

	@Override
	public void run() {

		if(main.containsFood() && !main.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation()) && !main.isInZulrahRoom() && !main.isInZulrahRoom()) {
			main.status = "Teleporting to zulrah";
			teleToZulrah();
		}
	
		if(main.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation())) {
			if(!this.isDialogueOpen()) {
				main.status = "Opening dialogue";
				openDialogue();
			}else {
				main.status = "Sending input";
				ctx.keyboard.sendKeys("1");
				ctx.sleep(400);
			}
		}
		
	}
	
	public void teleToZulrah() {
		SimpleNpc wizard = ctx.npcs.populate().filter(edge_wizard).nearest().next();

		if(wizard != null) {
			MenuActions.click(wizard, "Previous-teleport");
		}else {
			main.teleportToEdge();
		}
	}
	
	public void openDialogue() {
		SimpleObject boat = ctx.objects.populate().filter("Sacrificial boat").next();
		if(boat != null && boat.validateInteractable()) {
			if(boat.click("Board")) {
				ctx.sleep(100);
			}
		}
	}



	public boolean isDialogueOpen() {
		SimpleWidget widg = ctx.widgets.getWidget(219, 1);
		if(widg != null) {
			if(widg.visibleOnScreen()) {
				return true;
			}
		}
		SimpleWidget chatbox = ctx.widgets.getWidget(162, 44);
		if(chatbox != null) {
			if(chatbox.getText().contains("Enter") && chatbox.visibleOnScreen()) {
				return true;
			}
		}
		
		return false;
	}


	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "";
	}
	
}
