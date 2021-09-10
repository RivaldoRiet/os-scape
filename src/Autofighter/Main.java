package Autofighter;

import java.awt.Button;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import api.MenuActions;
import api.Tasks;
import api.threads.PrayerObserver;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Combat.Style;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.api.ClientContext;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Trester", category = Category.COMBAT, description = "Start at the npc, fill in the gui!", discord = "", name = "Os-scape Autofighter", servers = {
		"Osscape" }, version = "2.1")

public class Main extends Script {

private static final Skills SLAYER = null;
private String EXIT_STATUS;

private int startSlayerXp, slayerXpGained, CURRENT_SLAYER_XP, slayerlvl;
private long STARTTIME, UPTIME;
private JFrame frame;
private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
private ArrayList<String> npcName = new ArrayList<String>();
private ArrayList<String> lootName = new ArrayList<String>();
private boolean finishedGui = false;
private PrayerObserver prayerObserver = null;
private WorldArea alkharid1 = new WorldArea(new WorldPoint(3281, 3177, 0), new WorldPoint(3286, 3167, 0));
private WorldArea alkharid2 = new WorldArea(new WorldPoint(3287, 3177, 0), new WorldPoint(3298, 3167, 0));
private WorldArea alkharid3 = new WorldArea(new WorldPoint(3299, 3177, 0), new WorldPoint(3303, 3167, 0));
private WorldArea alkharid4 = new WorldArea(new WorldPoint(3282, 3166, 0), new WorldPoint(3303, 3159, 0));

@Override
public void paint(Graphics Graphs) {
	this.CURRENT_SLAYER_XP = this.ctx.skills.experience(SimpleSkills.Skills.SLAYER);
	this.UPTIME = System.currentTimeMillis() - this.STARTTIME;
	Graphics2D Graph = (Graphics2D) Graphs;
	Graphics2D g = (Graphics2D) Graphs;
	g.setColor(Color.BLACK);
	g.fillRect(9, 230, 150, 55);
	g.setColor(Color.CYAN);
	g.drawRect(9, 230, 150, 55);
	g.setColor(Color.WHITE);
	g.drawString("  Autofighter v1.0", 7, 245);
	g.setColor(Color.WHITE);
	g.drawString("  Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 7, 269);
	if (Integer.valueOf(this.CURRENT_SLAYER_XP - this.startSlayerXp) > 0) {
		g.setColor(Color.WHITE);
		g.drawString(
				"  Total XP: " + runescapeFormat(Integer.valueOf(this.CURRENT_SLAYER_XP - this.startSlayerXp)) + " / P/H ("
						+ runescapeFormat(Integer.valueOf(
								this.ctx.paint.valuePerHour(this.CURRENT_SLAYER_XP - this.startSlayerXp, this.STARTTIME)))
						+ ")",
				7, 257);
		g.setColor(Color.WHITE);
		g.drawString("  Current Level: " + this.slayerlvl + "", 7, 281);
	}

}

@Override
public void onChatMessage(ChatMessage arg0) {
	// TODO Auto-generated method stub

}

@Override
public void onExecute() {
	Tasks.init(ctx);
	CURRENT_SLAYER_XP = ctx.skills.experience(Skills.SLAYER); // check old
																// xp?????
	startSlayerXp = ctx.skills.experience(Skills.SLAYER);
	this.STARTTIME = System.currentTimeMillis();
	this.slayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.SLAYER));
	EventQueue.invokeLater(new Runnable() {
	public void run() {
		try {
			initialize();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	});
	
	prayerObserver = new PrayerObserver(ctx, new BooleanSupplier() {
		@Override
		public boolean getAsBoolean() {
			//	return Variables.USE_PRAYER;
			return true;
		}
	});
	prayerObserver.start();

}

private void drinkPrayerPot() {
	int prayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER))
			- (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER)));

	if (prayerlvl > 30) {
		SimpleItem prayerpot = ctx.inventory.populate().filter(p -> p.getName().contains("Sanfew")).next();
		if (prayerpot != null && prayerpot.click(0)) {
			ctx.onCondition(() -> Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER))
					- (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) <= 30, 1000);
		} else {
			SimpleItem restorepot = ctx.inventory.populate().filter(p -> p.getName().contains("restore")).next();
			if (restorepot != null && restorepot.click(0)) {
				ctx.onCondition(() -> Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER))
						- (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) <= 30, 1000);
			}
		}
	}
}

private void openCasket() {
	SimpleItem casket = ctx.inventory.populate().filter(p -> p.getName().contains("Coin casket")).next();
	if (casket != null && casket.click(0)) {
		ctx.sleep(500);
	}
}

@Override
public void onProcess() {
	if (this.finishedGui) {
		this.slayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.SLAYER));
		SimplePlayer p = ctx.players.getLocal();
		
		/*
		SimpleObject door = ctx.objects.populate().filter(1513, 1511).filter("Large door").next();
		if (door != null) {
			if (door.validateInteractable()) {
				door.click("Open");
				
			}
			return;
		} */
		
		this.dropJunk("key");
		this.dropJunk("ruby");
		this.dropJunk("diamond");
		this.dropJunk("uncut");
		this.dropJunk("grimy");
		
		SimpleItem casket = ctx.inventory.populate().filter(c -> c.getName().toLowerCase().contains("casket")).next();
		if (casket != null) {
			casket.click(0);
		}
		
		
		SimpleNpc npc = getNpc(this.alkharid1);
		if (npc == null) npc = getNpc(this.alkharid2); 
		if (npc == null) npc = getNpc(this.alkharid3); 
		if (npc == null) npc = getNpc(this.alkharid4); 
		if (npc == null) {
			 npc = Tasks.getCombat().getAggressiveNPC(npcName.stream().toArray(String[]::new));
			if (npc == null) npc = Tasks.getCombat().getAggressiveNPC(npcName.stream().toArray(String[]::new));
			if (npc == null) npc = Tasks.getCombat().getNPC(true, npcName.stream().toArray(String[]::new));
			if (npc == null) npc = Tasks.getCombat().getMultiNpc(npcName.stream().toArray(String[]::new));
		}
		
		
		
		if (ctx.skills.realLevel(Skills.PRAYER) >= 45) {
			
			if (Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "trident")) {
				Tasks.getSkill().addPrayer(Prayers.MYSTIC_MIGHT);
				Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
			}else if (Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "crossbow") || Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "knife") || Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "blowpipe")){
				Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
				Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);
			}
			else { 
				addMeleePray();
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);};
		}
		
		if (p.isAnimating()) {
			ctx.onCondition(() -> !p.isAnimating(), 1000);
		}
		
		setMelee();

		Utils.drinkSinglePrayerPot(true);

		openCasket();
		this.drinkAttackPotion();
		this.drinkCombatPotion();
		this.drinkRangePotion();

		if (lootOnGround() && ctx.inventory.populate().population() < 28) {
			lootItem();
		} else if (ctx.inventory.populate().population() == 28) {
			this.singleEat(false);
		} else if (npc != null) {
			if (npc != null && !p.inCombat()) {
				Tasks.getCombat().attack(npc);
				// ctx.onCondition(() -> !p.inCombat() || npcToAttack.isDead(),
				// 10000);
			}
		}

		ctx.sleep(5);
	}
}

private SimpleNpc getNpc(WorldArea w) {
	
	SimpleNpc npc = null;
	if (ctx.pathing.inArea(w)) {
		 npc = ctx.npcs.populate().filter("Al-kharid warrior").filter(n -> w.containsPoint(n.getLocation())).next();
	}
	return npc;
}

private void dropJunk(String name) {
	SimpleItemQuery<SimpleItem> items = ctx.inventory.populate()
			.filter(o -> o.getName().toLowerCase().contains(name.toLowerCase())).filterHasAction("Drop");
	for (SimpleItem item : items) {
		ctx.keyboard.pressKey(16);
		item.click(0);
		ctx.keyboard.pressKey(16);
	}
	ctx.sleep(100);
	ctx.keyboard.releaseKey(16);
}


private void dropJunkEquals(String name) {

	SimpleItemQuery<SimpleItem> items = ctx.inventory.populate().filter(o -> o.getName().equals(name))
			.filterHasAction("Drop");

	ctx.inventory.dropItems(items);

	return;
	/*
	 * for (SimpleItem item : items) { ctx.keyboard.pressKey(16); item.click(0);
	 * ctx.keyboard.pressKey(16); }
	 */
}


public void setMelee() {
	int attackLvl = ctx.skills.realLevel(Skills.ATTACK);
	int strLvl = ctx.skills.realLevel(Skills.STRENGTH);
	int defLvl = ctx.skills.realLevel(Skills.DEFENCE);
	int ranged = ctx.skills.realLevel(Skills.RANGED);

	if (strLvl < 20) ctx.combat.style(Style.AGGRESSIVE);
	else if (attackLvl < 40) ctx.combat.style(Style.ACCURATE);
	else if (defLvl < 55) ctx.combat.style(Style.DEFENSIVE);
	else if (strLvl < 60) ctx.combat.style(Style.AGGRESSIVE);
	else if (attackLvl < 70) ctx.combat.style(Style.ACCURATE);
	else if (defLvl < 70) ctx.combat.style(Style.DEFENSIVE);
	else if (strLvl < 99) ctx.combat.style(Style.AGGRESSIVE);
	else if (attackLvl < 99) ctx.combat.style(Style.ACCURATE);
	else if (defLvl < 99) ctx.combat.style(Style.DEFENSIVE);
	else if (ranged < 90) {
		ctx.combat.style(Style.AGGRESSIVE);
	}

	int weapon = attackLvl >= 70 ? ItemID.ABYSSAL_WHIP
			: attackLvl >= 60 ? ItemID.DRAGON_SCIMITAR : attackLvl >= 40 ? ItemID.RUNE_SCIMITAR : ItemID.IRON_SCIMITAR;
	if (ctx.equipment.populate().filter(weapon).population() == 0) {
		SimpleItem scim = ctx.inventory.populate().filter(weapon).next();
		if (scim != null) MenuActions.click(scim, "Wear");
		ctx.sleep(250, 450);
	}
}

private void addMeleePray() {
	int prayerLvl = ctx.skills.realLevel(Skills.PRAYER);
	if (prayerLvl >= 31 && prayerLvl < 70) {
		Tasks.getSkill().addPrayer(Prayers.ULTIMATE_STRENGTH);
		Tasks.getSkill().addPrayer(Prayers.INCREDIBLE_REFLEXES);
	} else {
		Tasks.getSkill().addPrayer(Prayers.PIETY);
	}
}

private boolean skillShouldBeBoosted(Skills s) {
	int lvl = ctx.skills.realLevel(s);
	int lvl1 = ctx.skills.level(s);
	int diff = lvl1 - lvl;
	if (diff <= 6) { return true; }
	return false;
}

private SimpleItem getItem(String... itemName) {
	return ClientContext.instance().inventory.populate()
			.filter(p -> Stream.of(itemName).anyMatch(arr -> p.getName().toLowerCase().contains(arr.toLowerCase()))).next();
}

private void drinkCombatPotion() {

	if (shouldEat()) return;

	if (skillShouldBeBoosted(SimpleSkills.Skills.STRENGTH)) {
		SimpleItem pot = getItem("combat");
		if (pot != null && pot.click(0)) {
			ClientContext.instance().sleep(200);
		}

		SimpleItem pot1 = getItem("super strength");
		if (pot1 != null && pot1.click(0)) {
			ClientContext.instance().sleep(200);
		}
	}
}

private void drinkRangePotion() {

	if (shouldEat()) return;

	if (skillShouldBeBoosted(SimpleSkills.Skills.RANGED)) {
		SimpleItem pot = getItem("ranging");
		if (pot != null && pot.click(0)) {
			ClientContext.instance().sleep(200);
		}
	}
}

private void drinkAttackPotion() {

	if (shouldEat()) return;

	if (skillShouldBeBoosted(SimpleSkills.Skills.ATTACK)) {
		SimpleItem pot2 = getItem("super attack");
		if (pot2 != null && pot2.click(0)) {
			ClientContext.instance().sleep(200);
		}
	}
}

public void singleEat(boolean check) {
	if (check && (!shouldEat())) return;

	SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();

	if (food != null) {
		food.click(0);
	}
}

public boolean shouldEat() {
	return getPercentageHitpoints() < 60;
}

public int getPercentageHitpoints() {
	// returns 50 for example
	float perc = ((float) ClientContext.instance().skills.level(Skills.HITPOINTS)
			/ ClientContext.instance().skills.realLevel(Skills.HITPOINTS));
	float perc1 = (perc * 100);
	return (int) perc1;
}

private boolean lootOnGround() {
	SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate()
			.filter(this.lootName.stream().toArray(String[]::new));
	if (lootation.size() > 0) { return true; }
	return false;
}

private void lootItem() {
	SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate()
			.filter(this.lootName.stream().toArray(String[]::new));
	if (lootation.size() > 0) {
		SimpleGroundItem item = lootation.nearest().next();
		if (item != null) {
			if (item.validateInteractable() && item.turnTo()) {
				if (item.click("Take")) {
					ctx.sleep(1000);
				}
			}
		}
	}
}

@Override
public void onTerminate() {
}

public static String runescapeFormat(Integer number) {
	String[] suffix = { "K", "M", "B", "T" };
	int size = (number.intValue() != 0) ? (int) Math.log10(number.intValue()) : 0;
	if (size >= 3) while (size % 3 != 0)
		size--;
	return (size >= 3)
			? (String.valueOf(Math.round(number.intValue() / Math.pow(10.0D, size) * 10.0D) / 10.0D) + suffix[size / 3 - 1])
			: (new StringBuilder(String.valueOf(number.intValue()))).toString();
}

/**
 * Initialize the contents of the frame.
 */
private void initialize() {
	frame = new JFrame();
	frame.setBounds(100, 100, 450, 300);
	frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.getContentPane().setLayout(null);
	tabbedPane.setToolTipText("Text");
	tabbedPane.setBounds(0, 0, 434, 261);
	frame.getContentPane().add(tabbedPane);

	JPanel panel = new JPanel();
	tabbedPane.addTab("Npc to attack", null, panel, null);
	panel.setLayout(null);

	Button button = new Button("Add npc");
	button.setBounds(235, 183, 70, 22);
	button.setForeground(Color.BLACK);
	panel.add(button);

	List list = new List();
	list.setBounds(109, 10, 175, 146);
	list.setForeground(Color.BLACK);
	panel.add(list);

	JLabel lblNpcList = new JLabel("Npc list");
	lblNpcList.setFont(new Font("Tahoma", Font.PLAIN, 18));
	lblNpcList.setBounds(20, 11, 83, 40);
	panel.add(lblNpcList);

	TextField textField = new TextField();
	textField.setBounds(92, 183, 124, 22);
	textField.setForeground(Color.BLACK);
	panel.add(textField);

	JLabel lblAddNpc = new JLabel("Add Npc");
	lblAddNpc.setBounds(10, 183, 93, 14);
	panel.add(lblAddNpc);

	JLabel lblExactNameWith = new JLabel("Exact name with capitals Ex: Green dragon");
	lblExactNameWith.setBounds(68, 163, 340, 14);
	panel.add(lblExactNameWith);

	Button button_1 = new Button("Start");
	button_1.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
		if (npcName.size() > 0) {
			finishedGui = true;
			frame.dispose();
		} else {
			JOptionPane.showMessageDialog(null, "You need to put in atleast one npc!", "InfoBox",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	});
	button_1.setFont(new Font("Dialog", Font.PLAIN, 18));
	button_1.setBounds(336, 183, 83, 40);
	button_1.setForeground(Color.BLACK);
	panel.add(button_1);

	JPanel panel_1 = new JPanel();
	tabbedPane.addTab("Loot", null, panel_1, null);
	panel_1.setLayout(null);

	JButton btnAddLoot = new JButton("Add loot");
	btnAddLoot.setBounds(227, 184, 89, 23);
	btnAddLoot.setForeground(Color.BLACK);
	panel_1.add(btnAddLoot);

	JLabel lblAddItem = new JLabel("Add item");
	lblAddItem.setBounds(10, 188, 66, 14);
	panel_1.add(lblAddItem);

	JLabel lblWriteTheExact = new JLabel("Write the exact name Ex: Black mask");
	lblWriteTheExact.setBounds(95, 158, 221, 14);
	panel_1.add(lblWriteTheExact);

	TextField textField_1 = new TextField();
	textField_1.setBounds(82, 184, 122, 22);
	textField_1.setForeground(Color.BLACK);
	panel_1.add(textField_1);

	JLabel lblItemList = new JLabel("Item list");
	lblItemList.setFont(new Font("Tahoma", Font.PLAIN, 18));
	lblItemList.setBounds(10, 11, 83, 23);
	panel_1.add(lblItemList);

	List list_1 = new List();
	list_1.setBounds(95, 10, 189, 131);
	list_1.setForeground(Color.BLACK);
	panel_1.add(list_1);

	Button button_2 = new Button("Start");
	button_2.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
		if (npcName.size() > 0) {
			finishedGui = true;
			frame.dispose();
		} else {
			JOptionPane.showMessageDialog(null, "You need to put in atleast one npc!", "InfoBox: ",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
	});
	button_2.setFont(new Font("Dialog", Font.PLAIN, 18));
	button_2.setForeground(Color.BLACK);
	button_2.setBounds(336, 184, 83, 39);
	panel_1.add(button_2);

	button.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent arg0) {
		npcName.add(textField.getText());
		list.add(textField.getText());
		textField.setText("");
	}
	});

	btnAddLoot.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
		lootName.add(textField_1.getText());
		list_1.add(textField_1.getText());
		textField_1.setText("");
	}
	});

}

}
