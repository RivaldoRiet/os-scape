package JailMiner;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Trester/Riet", category = Category.MINING, description = "Vitality Jail Miner, start with empty inventory", discord = "", name = "Vitality Jail miner", servers = {
		"Osscape" }, version = "0.1")

public class JailMiner extends Script {

	private long STARTTIME, UPTIME;

	private int rocksMined = 0;

	private WorldArea jail_area = new WorldArea(new WorldPoint(3280, 9424, 0), new WorldPoint(3291, 9453, 0));

	private List<String> staff_names = Arrays.asList("trick", "raw envy", "final wish", "polishcivil", "miika",
			"runite", "7 dust", "xion", "shane", "napalm", "delta", "trump", "four", "bio", "azeem", "fortune", "ruax",
			"money", "one", "the sexy", "chelle", "7 normie", "mustbeozzi", "dead", "harsh", "taylor twift",
			"dequavius", "praise satan", "oixel", "west coast", "alcohol", "j0kester", "meyme", "joggerss", "revizenot",
			"chelle", "eso maniac", "joey", "s8n", "olmlet", "praise satan", "coolers", "000", "halloween", "52xp",
			"itrustx", "centrum", "dharoks", "excuse you", "kimmy", "psycho clown", "yaro", "west coast", "jonno",
			"bradyb", "hellobroski", "beastly", "demix", "compton", "dvious", "azurill", "hc merica");

	@Override
	public void paint(Graphics Graphs) {
		this.UPTIME = System.currentTimeMillis() - this.STARTTIME;
		Graphics2D g = (Graphics2D) Graphs;

		g.setColor(Color.BLACK);
		g.fillRect(0, 230, 150, 55);
		g.setColor(Color.BLACK);
		g.drawRect(0, 230, 150, 55);
		g.setColor(Color.white);
		g.drawString("Osscape Jail Miner v1.0", 7, 245);
		g.drawString("Rocks mined: " + this.rocksMined + " / P/H ("
				+ Integer.valueOf(this.ctx.paint.valuePerHour(this.rocksMined, this.STARTTIME)) + ")", 7, 259);
		g.drawString("Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 7, 271);
	}

	@Override
	public void onChatMessage(ChatMessage msg) {
// TODO Auto-generated method stub
		if (msg.getMessage().contains("mine some")) {
			rocksMined++;
		}

	}

	@Override
	public void onExecute() {
		this.STARTTIME = System.currentTimeMillis();
	}

	@Override
	public void onProcess() {

		if (staffFound())
		{
			ctx.log("staff member found, sleeping until he is gone");
			ctx.sleep(10000);
		}else {
			if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 10) {
				ctx.updateStatus("Turning run on");
				ctx.pathing.running(true);
			}
			if (ctx.pathing.inArea(this.jail_area)) {
				if (ctx.inventory.populate().population() == 28) {
					// use rock on guard
					SimpleItem rock = ctx.inventory.populate().filter("Rock").next();
					if (rock != null && ctx.inventory.itemSelectionState() <= 0) {
						rock.click("Use");
					}
	
					SimpleNpc guard = ctx.npcs.populate().filter("Security guard").next();
					if (guard != null && guard.validateInteractable() && ctx.inventory.itemSelectionState() == 1) {
						guard.click(0);
					}
				} else {
					if (!ctx.players.getLocal().isAnimating()) {
						// mine
						SimpleObject rock = ctx.objects.populate().filter("Rocks").nearest().next();
						if (rock != null && rock.validateInteractable()) {
							rock.click("Mine");
							ctx.onCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
						}
					} else {
						ctx.sleep(100);
					}
	
				}
	
			} else {
				// ctx.log("Walking path");
				ctx.log("Done with the task");
			}
		}
	}

	private boolean staffFound() {
		SimplePlayerQuery<SimplePlayer> players = ctx.players.populate();

		for (SimplePlayer p : players) {
			boolean isMatched = this.staff_names.stream().anyMatch(p.getName()::equalsIgnoreCase);
			if (isMatched) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onTerminate() {
	}
}