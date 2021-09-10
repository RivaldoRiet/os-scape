package Zulrah;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class BankTask extends Task {
	private Zulrah main;
	private WorldArea edge = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));
	
	public BankTask(ClientContext ctx, Zulrah main) {
		super(ctx);
		this.main = main;
	}

	@Override
	public boolean condition() {
		return edge.containsPoint(ctx.players.getLocal().getLocation()) && main.shouldRestockSupplies();
	}

	@Override
	public void run() {
		main.setRun();
		main.openCasket();
		
		if(edge.containsPoint(ctx.players.getLocal().getLocation()) && main.shouldRestockSupplies()) {
			main.useMage = true;
			main.status = "Getting preset";
			main.usePreset();
			return;
		}
	}
	
	@Override
	public String status() {
		return "";
	}
	
}
