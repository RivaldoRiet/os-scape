package api.tasks;

import api.MenuActions;
import api.Tasks;
import api.Variables;
import api.utils.Timer;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class Banking {

	private ClientContext ctx;

	public Banking(ClientContext ctx) {
		this.ctx = ctx;
	}

	public SimpleObject getBank() {
		return ctx.objects.populate().filter(10355).nearest().next();
	}

	public boolean open() {
		if (ctx.bank.bankOpen()) return true;
		SimpleObject bank = getBank();
		if (bank == null) return false;
		if (bank.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking to bank";
			ctx.pathing.step(bank.getLocation());
			ctx.sleep(450, 650);
			return false;
		}

		Variables.STATUS = "Opening bank";
		MenuActions.click(bank, "Bank");
		ctx.sleepCondition(() -> ctx.bank.bankOpen(), 2000);
		return false;
	}

	public boolean usePreset() {
		return usePreset(false);
	}

	public boolean usePreset(boolean slayer) {
		SimpleObject bank = getBank();
		if (bank == null || (Tasks.getSupplies().hasFood() && Tasks.getSupplies().hasPrayer() && !Variables.FORCE_BANK) || (slayer && !Tasks.getInventory().contains("Slayer casket")))
			return true;
		if (ctx.pathing.distanceTo(bank.getLocation()) > 5) {
			Variables.STATUS = "Walking to bank";
			ctx.pathing.step(bank.getLocation());
			ctx.onCondition(() -> ctx.pathing.inMotion());
			return false;
		}
		Variables.STATUS = "Getting last preset";
		if (ctx.inventory.itemSelectionState() == 1) ctx.inventory.populate().next().click(0);
		SimpleItem before = ctx.inventory.populate().next();
		if (MenuActions.click(bank, "Last-preset")) {
			ctx.onCondition(() -> !before.equals(ctx.inventory.populate().next()), 500, 5);
			Variables.FORCE_BANK = false;
		}
		return true;
	}

	public boolean ready() {
		return ctx.skills.level(Skills.HITPOINTS) == ctx.skills.realLevel(Skills.HITPOINTS)
				&& ctx.skills.level(Skills.PRAYER) == ctx.skills.realLevel(Skills.PRAYER);
	}

	public boolean heal() {
		if (ready()) return true;

		SimpleObject box = ctx.objects.populate().filter(60003).nearest().next();
		if (box == null) return false;
		if (box.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking to heal chest";
			ctx.pathing.step(box.getLocation());
			ctx.sleep(450, 650);
			return false;
		}
		Variables.STATUS = "Refilling hitpoints";
		MenuActions.click(box, "Heal");
		ctx.onCondition(() -> ready());
		return ready();
	}

	public boolean withdrawItem(String name) {
		return withdrawItem(name, 1);
	}

	public boolean withdrawItem(String name, int amount) {
		if (open()) {
			SimpleItem item = Tasks.getBanking().getItem(name);
			if (item == null) {
				ctx.log("Unable to find: " + name);
				Variables.STOP = true;
				ctx.bank.closeBank();
				return false;
			}
			return MenuActions.withdraw(item, amount + "");
		}
		return false;
	}

	public boolean containsAll(String... itemName) {
		return ctx.bank.populate().filter(p -> Tasks.getInventory().predicate(p, itemName)).population() == itemName.length;
	}

	public boolean contains(String... itemName) {
		return !ctx.bank.populate().filter(p -> Tasks.getInventory().predicate(p, itemName)).isEmpty();
	}

	public SimpleItem getItem(String... itemName) {
		return ctx.bank.populate().filter(p -> Tasks.getInventory().predicate(p, itemName)).next();
	}

	private Timer altarTimer = new Timer(1);

	public void prayAtAltar(String name) {
		if (Tasks.getSkill().getPercentage(Skills.PRAYER) > 60 || altarTimer.isRunning()) return;
		SimpleObject altar = ctx.objects.populate().filter(name).next();
		if (altar == null) return;
		MenuActions.click(altar, "Pray");
		ctx.onCondition(() -> Tasks.getSkill().getPercentage(Skills.PRAYER) >= 95, 5000);
		altarTimer.setEndIn(600000);
	}

}
