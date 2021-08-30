package api.tasks;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import api.MenuActions;
import api.Tasks;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class Inventory {
	private ClientContext ctx;

	public Inventory(ClientContext ctx) {
		this.ctx = ctx;
	}

	public boolean predicate(SimpleItem item, String... itemName) {
		Stream<String> array = Stream.of(itemName);
		Predicate<String> filter = arr -> item.getName().toLowerCase().contains(arr.toLowerCase());
		return array.anyMatch(filter);
	}

	public boolean predicate(SimpleGroundItem item, String... itemName) {
		Stream<String> array = Stream.of(itemName);
		Predicate<String> filter = arr -> item.getName().toLowerCase().contains(arr.toLowerCase());
		return array.anyMatch(filter);
	}

	public SimpleItemQuery<SimpleItem> filter(String... itemName) {
		return ctx.inventory.populate().filter(p -> predicate(p, itemName));
	}

	public boolean containsAll(String... itemName) {
		return filter(itemName).population() == itemName.length;
	}

	public boolean contains(String... itemName) {
		return filter(itemName).population() > 0;
	}

	public SimpleItemQuery<SimpleItem> getItems(String... itemName) {
		return filter(itemName);
	}

	public SimpleItem getItem(String... itemName) {
		return getItems(itemName).next();
	}

	public boolean isWearing(EquipmentSlot slot, String... name) {
		String equipped = ctx.equipment.getEquippedItem(slot).getName().toLowerCase();

		return ctx.equipment.getEquippedItem(slot) != null
				&& Arrays.stream(name).anyMatch(item -> equipped.contains(item.toLowerCase()));
	}
	
	public void equip(String... name) {
		SimpleItem item = getItem(name);
		if (item != null) MenuActions.click(item, "Wear");
	}

	public void equipAll(SimpleItemQuery<SimpleItem> items) {
		items.forEach(item -> {
			MenuActions.click(item, "Wear");
			// if (item.click(0)) ctx.sleep(50, 75);
		});
	}

	public void equipAll(String... name) {
		equipAll(getItems(name));
	}

	public boolean hasPrayer() {
		return contains("prayer", "restore", "sanfew");
	}

	public boolean hasAntifire() {
		return contains("antifire");
	}

	public void equipGuthans() {
		if (Tasks.getInventory().contains("guthan")) {
			if (Tasks.getInventory().contains("warspear") && ctx.inventory.inventoryFull()) {
				Tasks.getSupplies().eat();
				ctx.onCondition(() -> !ctx.inventory.inventoryFull(), 2500);
			} else Tasks.getInventory().equipAll("guthan");
		}
	}
}
