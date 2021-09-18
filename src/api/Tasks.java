package api;

import api.tasks.*;
import lombok.Getter;
import simple.hooks.simplebot.teleporter.Teleporter;
import simple.robot.api.ClientContext;

public class Tasks {
	@Getter
	private static Banking banking;
	@Getter
	private static Skill skill;
	@Getter
	private static Inventory inventory;
	@Getter
	private static Supplies supplies;
	@Getter
	private static Combat combat;
	@Getter
	private static Looting loot;
	@Getter
	private static AntiBan antiban;
	@Getter
	private static Teleporter teleporter;
	@Getter
	private static MenuActions menuAction;

	public static void init(ClientContext ctx) {
		banking = new Banking(ctx);
		skill = new Skill(ctx);
		inventory = new Inventory(ctx);
		supplies = new Supplies(ctx);
		combat = new Combat(ctx);
		loot = new Looting(ctx);
		antiban = new AntiBan(ctx);
		teleporter = new Teleporter(ctx);
		menuAction = new MenuActions(ctx);
	}

}
