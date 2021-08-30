package api.tasks;

import api.MenuActions;
import api.Tasks;
import api.Variables;
import api.utils.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class Supplies {

	@AllArgsConstructor
	public enum PotionType {
		ATTACK(new String[] { "attack", "combat" }),
		STRENGTH(new String[] { "strength", "combat" }),
		DEFENCE(new String[] { "defence", "combat" }),
		MAGE(new String[] { "magic" }),
		RANGED(new String[] { "ranging" }),
		ANTIPOISON(new String[] { "antipoison", "antidote" }),
		PRAYER(new String[] { "prayer", "restore" }),
		ANTIFIRE(new String[] { "antifire" }),
		ENERGY(new String[] { "energy", "stamina" }),;

		@Getter
		private String[] value;
	}

	private ClientContext ctx;

	public Supplies(ClientContext ctx) {
		this.ctx = ctx;
	}

	public Timer antiFire = new Timer(1);
	public Timer antiPoison = new Timer(1);

	public boolean drink(PotionType type) {
		SimpleItem potion = Tasks.getInventory().getItem(type.getValue());
		if (potion != null) {
			Variables.STATUS = "Drinking " + type.toString() + " potion";
			MenuActions.click(potion, "Drink");
			if (type.equals(PotionType.ANTIFIRE)) antiFire = new Timer(200000);
			ctx.sleep(350, 550);
			return true;
		}
		return false;
	}

	public boolean eat() {
		SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();
		if (food != null) {
			Variables.STATUS = "Eating food";
			MenuActions.click(food, "Eat");
			ctx.sleep(150, 250);
			return true;
		}
		return false;
	}

	public boolean eat(String name) {
		SimpleItem food = Tasks.getInventory().getItem(name);
		if (food != null) {
			Variables.STATUS = "Eating food";
			MenuActions.click(food, "Eat");
			return true;
		}
		return false;
	}

	public boolean hasFood() {
		return ctx.inventory.populate().filterHasAction("Eat").population() > 0;
	}

	public boolean hasPrayer() {
		return Tasks.getInventory().contains("prayer", "restore", "sanfew");
	}

}
