package api.tasks;

import java.util.List;

import api.MenuActions;
import api.Tasks;
import api.Variables;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.robot.api.ClientContext;

public class Looting {

	private ClientContext ctx;

	public Looting(ClientContext ctx) {
		this.ctx = ctx;
	}

	public boolean loot(String... loots) {
		SimpleEntityQuery<SimpleGroundItem> possible = ctx.groundItems.populate();
		if (possible.population() == 0) return false;

		SimpleGroundItem loot = possible.filter(p -> Tasks.getInventory().predicate(p, loots))
				.filter(item -> item.getId() != 995 || (item.getId() == 995 && item.getQuantity() >= 20000 && ctx.players.getLocal().distanceTo(item) <= 15)).nearest().next();
		if (loot == null) return false;

		if (ctx.inventory.canPickupItem(loot)) {
			Variables.STATUS = "Picking up loot";
			int population = ctx.inventory.populate().filter(loot.getId()).population(true);
			MenuActions.click(loot, "Take");
			ctx.sleep(200);
			ctx.onCondition(() -> population != ctx.inventory.populate().filter(loot.getId()).population(true));
			return true;
		} else if (ctx.inventory.inventoryFull() && Tasks.getSupplies().hasFood()) {
			Tasks.getSupplies().eat();
			return true;
		}
		return false;

	}

	public boolean loot(List<String> loots) {
		return loot(loots.stream().toArray(String[]::new));
	}

}
