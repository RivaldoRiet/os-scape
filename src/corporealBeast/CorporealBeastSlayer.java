package corporealBeast;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.BooleanSupplier;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import api.MenuActions;
import api.Tasks;
import api.threads.PrayerObserver;
import api.utils.Utils;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Trester", category = Category.COMBAT, description = "Start in edge, with your preset ready and fill in the gui",
discord = "Trester#5088", name = "Corporeal beast slayer", servers = { "Osscape" }, version = "2.4")


public class CorporealBeastSlayer extends Script implements LoopingScript{

private String status = "";
private long STARTTIME, UPTIME;

private JFrame frmCorporealBeastSettings;
private JTextField textField;
private JTextField textField_1;
private boolean finishedGui = false;
private String leaderName = "";
private boolean isLeader = false;
private boolean useSpecial = true;
private boolean useVenge = true;
private boolean useSaferoute = false;
private VengeTimer vengeTimer;
private CorpSpawnTimer corpSpawnTimer;
private AntibanTimer antibanTimer;
private String instancePassword = "";
private String[] staff_list = {"Coolers", "olmlet", "s8n", "Joey", "Taylor Swift", "Eso maniac", "Jonno", "Delta", "Halloween", "Eso Maniac", "7 Normie", "Runite", "Mustbeozzi", "Ruax", "Dead", "Harsh", "Taylor Swift", "7 Dust", "Dequavius", "Praise Satan", "Pixel", "WEST COAST", "Alcohol", "J0kester", "Meyme", "Joggerss", "Revize"};

private WorldArea edge = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));
private WorldArea corp_pre_lobby = new WorldArea(new WorldPoint(2962, 4376, 2), new WorldPoint(2971, 4389, 2));
private WorldArea mage_lobby = new WorldArea(new WorldPoint(2527, 4708, 0), new WorldPoint(2549, 4725, 0));
private String[] spec_wep = {"Bandos godsword", "Statius warhammer", "Dragon warhammer"};

private String[] loot = {"Teak plank", "Mahogany logs", "Torstol seed", "Adamantite ore", "Adamantite bar", "Onyx bolts (e)", "Green dragonhide", "Magic logs", "Holy elixir", "Cannonball", "Spirit shield", "Arcane sigil", "Spectral sigil", "Elysian sigil", "Coin casket (giant)", "Raw shark", "Runite ore"};

private int corpKills = 0;

private boolean antibanActivated = false;

private final String edge_wizard = "Teleports wizard";

private boolean needInstanceMoney = false;
public PrayerObserver prayerObserver = null;

@Override
public void paint(Graphics Graphs) {

    this.UPTIME = System.currentTimeMillis() - this.STARTTIME;
        Graphics2D g = (Graphics2D) Graphs;
        g.setColor(Color.BLACK);
        g.fillRect(9, 230, 150, 55);
        g.setColor(Color.CYAN);
        g.drawRect(9, 230, 150, 55);
        g.setColor(Color.WHITE);
        g.drawString("  Corporeal beast Slayer v2.3", 7, 245);
        g.drawString("  Corp kills: " + this.corpKills + " (" + ctx.paint.valuePerHour(this.corpKills, this.STARTTIME) + ")" , 7, 257);
        g.drawString("  Status: " + this.status , 7, 269);
        g.drawString("  Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 7, 281);
}


@Override
public void onChatMessage(ChatMessage msg) {
if(msg.getMessage().contains("kill count")) {
	this.corpKills++;
}

if(msg.getMessage().contains("received a drop")) {
	ctx.log(msg.getMessage().split("received a drop")[1]);
}

}

@Override
public void onExecute() {
        this.STARTTIME = System.currentTimeMillis();
        Tasks.init(ctx);
        prayerObserver = new PrayerObserver(ctx, new BooleanSupplier() {
    		@Override
    		public boolean getAsBoolean() {
    			return true;
    		}
    	});
    	prayerObserver.setUncaughtExceptionHandler(Utils.handler);
    	prayerObserver.start();
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
					frmCorporealBeastSettings.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
}

@Override
public void onProcess() {
	antiBanTimerCheck();
	if(finishedGui && this.antibanActivated == false) {
		
		SimplePlayer p = ctx.players.getLocal();
		
		openCasket();
		
		if(edge.containsPoint(p.getLocation()) && !containsFood()) {
			this.status = "getting preset";
			usePreset();
		}
		
		if(this.useSaferoute && !corp_pre_lobby.containsPoint(p.getLocation()) && !this.isInCorpRoom()) {
			this.status = "using saferoute";
			this.teleToCorpSafe();
		}
		
		if(this.useSaferoute == false && containsFood() && !corp_pre_lobby.containsPoint(p.getLocation()) && !this.isInCorpRoom()) {
			this.status = "teleporting to corp";
			teleToCorp();
		}
		
		int strengthlvl = Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.STRENGTH));
		if(corp_pre_lobby.containsPoint(p.getLocation())){
			if(this.useSpecial) {
				useSpecialAttack();
			}
			if(this.needInstanceMoney && this.isLeader) {
				this.status = "Paying for instance";

				if(this.isDialogueOpen()) {
					this.status = "Going through dialogue";
					leaderEnterInstance();
					this.checkInstanceMoney();
					if(this.needInstanceMoney) {
						this.createInstanceDialogue();
					}
					leaderEnterInstance();
				}else {
					this.status = "Opening dialogue";
					this.openDialogue();
				}
				
				if(this.isLeader && this.needInstanceMoney){
					this.status = "Creating instance";
					this.createInstanceDialogue();
				}
				
			}
			
			if(!this.isLeader) {
				if(isDialogueOpen()) {
					this.status = "Entering friend instance";
					this.enterFriendInstance();
				}else {
					this.status = "Opening friend dialogue";
					this.openDialogue();
				}
			}else if(this.isLeader && !this.needInstanceMoney){
			
				if(isDialogueOpen()) {
					this.status = "Entering leader instance";
					checkInstanceMoney();
					if(this.needInstanceMoney) {
						return;
					}
					leaderEnterInstance();
					
				}else {
					this.status = "Opening leader dialogue";
					this.openDialogue();
				}
			}
		}
		
		drinkPrayerPot();
		if(lootOnGround() && !shouldEat()) {
			this.status = "looting";
			lootItem();
		}
		
		if(shouldEat()) {
			this.status = "eating";
			comboEat();
		}
		
		if(isInCorpRoom() && !isCorpAlive() && !containsFood()) {
			restock();
		}
		
		if(isInCorpRoom() && !isCorpAlive()) {
			this.status = "waiting for corp";
			this.spawnTimerCheck();
			if(strengthlvl <= 115) {
				this.status = "potting";
				prepot();
			}
		}
		
		if(isInCorpRoom() && !isCorpAlive()) {
			prepray();
			if(this.useVenge) {
				this.venge();
			}
		}
		
		checkForStaff();
		
		if(isInCorpRoom() && isCorpAlive()) {
			if(this.corpSpawnTimer != null) {
				this.corpSpawnTimer = null;
			}
			strengthlvl = Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.STRENGTH));
			prepray();
			if(strengthlvl <= 113) {
				this.status = "potting";
				prepot();
			}

			if(lootOnGround() && !shouldEat()) {
				this.status = "looting";
				lootItem();
			}else if(isInCorpRoom() && isCorpAlive()) {
				this.status = "attacking corp";
				if(shouldEat()) {
					comboEat();
				}
				
				if(this.useVenge && !shouldEat() && containsFood()) {
					this.venge();
				}
				
				if(lootOnGround() && !shouldEat()) {
					this.status = "looting";
					lootItem();
				}
				
				if(containsFood()) {
					attackCorpBeast();
				}
				
				openCasket();
				
				if(!shouldEat() && ctx.combat.getSpecialAttackPercentage() >= 50 && !specEquipped() && this.useSpecial) {
					this.status = "Equipping spec wep";
					equipSpecWep();
				}
				if(!shouldEat() && this.useSpecial) {
					useSpecialAttack();
				}
				if(shouldEat()) {
					this.status = "Combo eating";
					comboEat();
				}
				drinkPrayerPot();
				if(!containsFood()) {
					this.status = "Restocking";
					restock();
				}
				
			}
			checkForStaff();
			//ctx.sleep(100);
		}
		
	}
}


private void openCasket() {
	SimpleItem casket = ctx.inventory.populate().filter(p -> p.getName().contains("casket")).next();
	if(casket != null && casket.click(0)) {
		ctx.sleep(500);
	}
}

private void restock() {
	this.status = "Restocking";
	if(this.useSaferoute) {
		this.teleToMagebank();
	}else {
		this.teleportToEdge();
	}
}

private boolean mageTabOpen() {
	if(ctx.game.tab().equals(Tab.MAGIC)) {
		return true;
	}
	return false;
}


private void openMageTab() {
	if(!mageTabOpen()) {
		if(ctx.game.tab(Tab.MAGIC)) {
			ctx.sleep(300);
		}
	}
}

private void antiBanTimerCheck() {
	if(this.antibanActivated && this.antibanTimer != null) {
		this.status = "Waiting 10 mins, staff detected";
		if((System.currentTimeMillis() - corpSpawnTimer.getMs()) > 600000) {
			// wait 10 minutes
			ctx.log("Waiting antiban time complete");
			this.antibanActivated = false;
			this.antibanTimer = null;
		}else {
			ctx.sleep(1000);
		}
	}
}


private void spawnTimerCheck() {
	if(isInCorpRoom() && !isCorpAlive() && containsFood()) {
		if(this.corpSpawnTimer == null) {
			this.corpSpawnTimer = new CorpSpawnTimer();
		}else {
			// check timer higher than 1 minute
			if(corpSpawnTimer != null && (System.currentTimeMillis() - corpSpawnTimer.getMs()) > 60000) {
				this.status = "corp hasn't spawned for 60 secs.. leaving";
				this.teleportToEdge();
			}
		}
	}else if(isCorpAlive()) {
		this.corpSpawnTimer = null;
	}
}


private void venge() {
	if(vengeTimer != null && (System.currentTimeMillis() - vengeTimer.getMs()) > 30000 || vengeTimer == null) {
		this.status = "Casting venge";
		
		castVenge();
		vengeTimer = new VengeTimer();
	}
	
}


public void castVenge()
{
	MenuActions.invoke("Cast", "<col=00ff00>Vengeance</col>", 1, 57, -1, 14286986);
}

private void checkForStaff() {
	if(this.isInCorpRoom() && isStaffInArea()) {
		ctx.log("detected staff shutting down");
		this.teleportToEdge();
		this.antibanActivated = true;
		this.antibanTimer = new AntibanTimer();
	}
	if(this.corp_pre_lobby.containsPoint(ctx.players.getLocal().getLocation()) && isStaffInArea()) {
		ctx.log("detected staff shutting down");
		this.teleportToEdge();
		this.antibanActivated = true;
		this.antibanTimer = new AntibanTimer();
	}
	if(this.mage_lobby.containsPoint(ctx.players.getLocal().getLocation()) && isStaffInArea()) {
		ctx.log("detected staff shutting down");
		this.teleportToEdge();
		this.antibanActivated = true;
		this.antibanTimer = new AntibanTimer();
	}
}

private boolean isStaffInArea() {
	SimplePlayerQuery<SimplePlayer> staff = ctx.players.populate().filter(this.staff_list);
	if(staff.size() > 0) {
		for(SimplePlayer s : staff) {
			ctx.log("found staff member: " + s.getName() );
		}
		return true;
	}
	return false;
}

private boolean containsFood() {
	if(containsItem("Shark") && containsItem("Cooked karambwan") && (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) > 0) {
		return true;
	}
	return false;
}

private void checkInstanceMoney() {
	if(isDialogueOpen()) {
		if(ctx.widgets.getWidget(219, 1) != null) {
			SimpleWidget createinstance = ctx.widgets.getWidget(219, 1).getChild(1);
			if(createinstance != null && createinstance.getText().contains("Create an instance")) {
				if(this.needInstanceMoney == false) {
					this.needInstanceMoney = true;
				}
			}else if(createinstance != null && createinstance.getText().contains("Enter your instance")){
				this.needInstanceMoney = false;
			}
		}
	}
}


private void leaderEnterInstance() {
	SimpleWidget enterpaidp = ctx.widgets.getWidget(219, 1);
	if(enterpaidp != null) {
		SimpleWidget enterpaid = enterpaidp.getChild(1);
		if(enterpaid != null && enterpaid.getText().contains("Enter your instance") && enterpaid.click(0)) {
			this.status = "Entering cave";
			ctx.sleep(1500);
		}
	}
}

private void enterFriendInstance() {
	drinkPrayerPot();

	if(ctx.widgets.getWidget(219, 1) != null && ctx.widgets.getWidget(219, 1).getChild(2) != null && ctx.widgets.getWidget(219, 1).getChild(2).getText().contains("Enter a friend") && ctx.widgets.getWidget(219, 1).getChild(2).click(0)) {
		ctx.sleep(1500);
	}
	
	SimpleWidget typeInput = ctx.widgets.getWidget(162, 44);
	if(typeInput != null && typeInput.getText().contains("Enter friend")) {
		ctx.keyboard.sendKeys(this.leaderName);
		ctx.sleep(1500);
	}
	
	SimpleWidget passwordInput = ctx.widgets.getWidget(162, 44);
	if(passwordInput != null && passwordInput.getText().contains("is password protected")) {
		ctx.keyboard.sendKeys(this.instancePassword);
		ctx.sleep(1500);
	}
	
	if(typeInput != null && typeInput.getText().contains("Player not online or does not")) {
		ctx.keyboard.sendKeys(this.leaderName);
		ctx.sleep(1500);
	}

	
}

private void createInstanceDialogue() {
	if(this.isLeader && isDialogueOpen()) {
	SimpleWidget createinstance = ctx.widgets.getWidget(219, 1).getChild(1);
	if(createinstance != null && createinstance.getText().contains("Create") && createinstance.click(0)) {
		ctx.sleep(1000);
		}

	SimpleWidget privacy1 = ctx.widgets.getWidget(219, 1).getChild(2);
	if(privacy1 != null && privacy1.getText().contains("Privacy") && privacy1.click(0)) {
		ctx.sleep(1000);
	}

	SimpleWidget privacy2 = ctx.widgets.getWidget(219, 1).getChild(2);
	if(privacy2 != null && privacy2.getText().contains("players must enter") && privacy2.click(0)) {
		ctx.sleep(1000);
	}


	SimpleWidget privacy5 = ctx.widgets.getWidget(162, 44);
	if(privacy5 != null && privacy5.getText().contains("Enter a password")) {
	ctx.keyboard.sendKeys(this.instancePassword);
	ctx.sleep(1000);
	}
	

	SimpleWidget enter = ctx.widgets.getWidget(219, 1).getChild(3);
    SimpleWidget privacy3 = ctx.widgets.getWidget(219, 1).getChild(2);
    if(privacy3 != null && privacy3.getText().contains("Password-protected") && enter != null && enter.getText().contains("Create Instance") && enter.click(0)) {
    	ctx.sleep(1000);
    	
    }
	}

    SimpleWidget privacy4 = ctx.widgets.getWidget(584, 1).getChild(0);
	if(privacy4 != null && privacy4.getText().contains("Yes") && privacy4.click(0)) {
		this.needInstanceMoney = false;
		ctx.sleep(1000);
	}
    
}

private void drinkPrayerPot() {
	int prayerlvl =  Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) - (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER)));
	
	if(prayerlvl > 50 && !shouldEat() && !this.edge.containsPoint(ctx.players.getLocal().getLocation())) {
		SimpleItem prayerpot = ctx.inventory.populate().filter(p -> p.getName().contains("Sanfew")).next();
		if(prayerpot != null && prayerpot.click(0)) {
			ctx.sleep(400);
		}else {
			SimpleItem restorepot = ctx.inventory.populate().filter(p -> p.getName().contains("restore")).next();
			if(restorepot != null && restorepot.click(0)) {
				ctx.sleep(400);
			}
		}
	}
}


private boolean containsItem(String itemName) {
	return !ctx.inventory.populate().filter(p -> p.getName().contains(itemName)).isEmpty();
}

private boolean shouldEat() {
	if(ctx.players.getLocal().getHealth() <= 65 && !this.edge.containsPoint(ctx.players.getLocal().getLocation())) {
		return true;
	}
	return false;
}

public boolean lootOnGround() {
	SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate().filter(p -> p.getId() != 592);
	if(lootation.size() > 0 && this.isInCorpRoom()) {
		return true;
	}
	return false;
}

public void lootItem() {
	SimpleGroundItem loot = ctx.groundItems.populate().filter(p -> p.getId() != 592).next();
	if(ctx.inventory.populate().population() == 28) {
		comboEat();
		ctx.sleep(300);
	}
	if(loot != null) {
		status = "Looting";
		MenuActions.click(loot, "Take");
	}
}

private void equipSpecWep() {
	SimpleItem specwep = ctx.inventory.populate().filter(this.spec_wep).next();
	if(specwep != null) {
		if(specwep.click(0)) {
			ctx.sleep(800);
		}
	}
	
}


private void useSpecialAttack() {
	if(specEquipped() && ctx.combat.getSpecialAttackPercentage() >= 50) {
		if(!ctx.combat.specialAttack()) {
			if (ctx.widgets.getWidget(160,32) !=null) {
				this.status = "Using spec wep";
                ctx.widgets.getWidget(160,32).click(0);
				ctx.sleep(500);
			}
		}
	}else if(ctx.combat.getSpecialAttackPercentage() < 50){

		if(specEquipped()){ 
			SimpleItem hasta = ctx.inventory.populate().filter(p -> p.getName().contains("hasta")).next();
			SimpleItem defender = ctx.inventory.populate().filter(p -> p.getName().contains("defender")).next();
			SimpleItem spear = ctx.inventory.populate().filter(p -> p.getName().contains("spear")).next();
			if(hasta != null) {
				Tasks.getInventory().equipAll("hasta");
			}
			
			if(defender != null) {
				Tasks.getInventory().equipAll("defender");
			}
			
			if(spear != null) {
				Tasks.getInventory().equipAll("spear");
			}
		}
		
	}
}

private boolean specEquipped() {
	SimpleItemQuery<SimpleItem> hasta = ctx.inventory.populate().filter(p -> p.getName().contains("hasta"));
	SimpleItemQuery<SimpleItem> spear = ctx.inventory.populate().filter(p -> p.getName().contains("spear"));
	if(hasta.size() > 0) {
		return true;
	}
	
	if(spear.size() > 0) {
		return true;
	}
	
	return false;
}

private boolean isCorpAlive() {
	if(isInCorpRoom()) {
		SimpleEntityQuery<SimpleNpc> corp = ctx.npcs.populate().filter("Corporeal Beast");
		if(corp.size() > 0) {
			return true;
		}
	}
	return false;
}

private boolean isInCorpRoom() {
	SimpleEntityQuery<SimpleObject> passage = ctx.objects.populate().filter("Passage").filter(677);
	if(passage.size() > 0 && !this.corp_pre_lobby.containsPoint(ctx.players.getLocal().getLocation())) {
		return true;
	}
	return false;
}

private void attackCorpBeast() {
	SimpleNpc dec = ctx.npcs.populate().filter("Dark energy core").filter(p -> !p.isDead()).nearest().next();
		if(dec != null && dec.validateInteractable() && !shouldEat()) {
			Tasks.getCombat().attack(dec);
		}
		SimpleNpc corp = ctx.npcs.populate().filter("Corporeal Beast").nearest().next();
		if(ctx.npcs.populate().filter("Dark energy core").size() <= 0) {
			if(corp != null && !ctx.players.getLocal().inCombat() && !shouldEat()) {
				Tasks.getCombat().attack(corp);
	        }
		}
}


private void comboEat() {
	Tasks.getSupplies().doubleEat();
}


private void prepot() {
	SimpleItem pot = ctx.inventory.populate().filter(p -> p.getName().contains("combat")).next();
	if(pot != null && pot.click(0)) {
		ctx.sleep(500);
	}
}

private void prepray() {
	Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
	Tasks.getSkill().addPrayer(Prayers.PIETY);
	Tasks.getSkill().addPrayer(Prayers.RAPID_HEAL);
	Tasks.getSkill().addPrayer(Prayers.PROTECT_ITEM);
}

private void openDialogue() {
	SimpleObject cave = ctx.objects.populate().filter("Private portal").next();
	if(cave != null && cave.click("Enter")) {
		ctx.sleep(1000);
	}
}

private boolean isDialogueOpen() {
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

private boolean isInMageBank() {
	return this.mage_lobby.containsPoint(this.ctx.players.getLocal().getLocation());
}

private void teleToMagebank() {
	if(!isInMageBank()) {
		ctx.keyboard.sendKeys("::mb");
	}
}

private void bankInMageBank() {
	SimpleObject BANK_BOOTH = ctx.objects.populate().filter("Bank chest").nearest().next();
	if(this.isInMageBank()) {
		if(ctx.bank.bankOpen() && !containsItem("Games necklace")) {
			this.status = "withdrawing games necklace";
			SimpleItem games_necklace = ctx.bank.filter(g -> g.getName().contains("Games necklace")).next();
				if(games_necklace != null && ctx.bank.withdraw(games_necklace.getName(), 1)) {
					ctx.sleep(1000);
				}
			}else {
				//opening bank
				this.status = "opening magebank bank chest";
				if(BANK_BOOTH != null && BANK_BOOTH.validateInteractable()) {
					if(BANK_BOOTH.click("Use")) {
						ctx.onCondition(() -> ctx.bank.bankOpen(), 5000);
					}
				}
			}
			
		}

	
	if(containsItem("Games necklace") && ctx.bank.bankOpen()) {
		if(ctx.bank.closeBank()) {
			ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
		}
	}
}

private void teleToCorpSafe() {
	if(!isInMageBank()) {
		this.status = "teleporting to magebank";
		this.teleToMagebank();
	}
	
	if(isInMageBank() && containsFood()) {
		if(this.containsItem("Games necklace")) {
			if(this.isDialogueOpen()) {
				ctx.keyboard.sendKeys("3");
				ctx.sleep(1500);
			}else {
				SimpleItem gamesNeck = ctx.inventory.filter(p -> p.getName().contains("Games necklace")).next();
				if(gamesNeck != null && gamesNeck.click("Rub")) {
					ctx.sleep(500);
				}
			}
		}else {
			if(containsFood()) {
				if(ctx.inventory.populate().population() < 28) {
					this.status = "banking in mb";
					this.bankInMageBank();
				}else {
					this.status = "eating food for empty space";
					SimpleItem shark = ctx.inventory.populate().filter("Shark").next();
					if(shark != null && shark.click(0)) {
						ctx.sleep(100);
					}
				}
			}else {
				this.status = "using preset at mb";
				this.usePreset();
			}
		}
	}else if(!containsFood()){
		this.status = "equipping preset";
		this.usePreset();
	}
}

public void usePreset() {
	if (presetOpen()) {
		MenuActions.invoke("Load preset","",1,57,-1,54394908);
	}else {
		MenuActions.invoke("Open Presets","",1,57,-1,54919175);
	}
}

private void teleToCorp() {
	SimpleNpc wizard = ctx.npcs.populate().filter(edge_wizard).nearest().next();

	if(wizard != null) {
		MenuActions.click(wizard, "Previous-teleport");
	}else {
		teleportToEdge();
	}
}

public void teleportToEdge() {
	MenuActions.invoke("Cast","<col=00ff00>Lumbridge Home Teleport</col>",1,57,-1,14286853);
}

private void openInventory() {
	if(!isTabOpen(Tab.INVENTORY)) {
		if(ctx.game.tab(Tab.INVENTORY)) {
			ctx.sleep(300);
		}
	}
}

private boolean isTabOpen(Tab tab) {
	return ctx.game.tab().equals(tab);
}

public boolean presetOpen()
{
	final SimpleWidget screen = ctx.widgets.getWidget(830, 12);

	if (screen != null && !screen.isHidden() && screen.getText().toLowerCase().contains("configuration"))
	{
		return true;
	}
	
	return false;
}

@Override
  public void onTerminate() {
	Tasks.getSkill().removeAll();
	Tasks.getSkill().disablePrayers();
  }

private void initialize() {
	frmCorporealBeastSettings = new JFrame();
	frmCorporealBeastSettings.setForeground(Color.BLACK);
	frmCorporealBeastSettings.setTitle("Corporeal beast settings");
	frmCorporealBeastSettings.setBounds(100, 100, 450, 300);
	frmCorporealBeastSettings.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frmCorporealBeastSettings.getContentPane().setLayout(null);
	
	JCheckBox chckbxIsLeaderAccount = new JCheckBox("Is leader account");
	chckbxIsLeaderAccount.setForeground(Color.BLACK);
	chckbxIsLeaderAccount.setBounds(52, 39, 170, 23);
	frmCorporealBeastSettings.getContentPane().add(chckbxIsLeaderAccount);
	
	textField = new JTextField();
	textField.setForeground(Color.BLACK);
	textField.setBounds(122, 105, 162, 20);
	textField.setText("ccc");
	frmCorporealBeastSettings.getContentPane().add(textField);
	textField.setColumns(10);
	
	JLabel lblInstancePassword = new JLabel("Instance password");
	lblInstancePassword.setFont(new Font("Tahoma", Font.PLAIN, 11));
	lblInstancePassword.setForeground(Color.BLACK);
	lblInstancePassword.setBounds(20, 108, 151, 14);
	frmCorporealBeastSettings.getContentPane().add(lblInstancePassword);
	
	JButton btnNewButton = new JButton("Start");
	btnNewButton.setForeground(Color.BLACK);
	btnNewButton.setBounds(308, 212, 89, 23);
	frmCorporealBeastSettings.getContentPane().add(btnNewButton);
	
	JCheckBox chckbxUseVengeance = new JCheckBox("Use vengeance? (have runes in preset)");
	chckbxUseVengeance.setSelected(true);
	chckbxUseVengeance.setBounds(52, 146, 288, 23);
	frmCorporealBeastSettings.getContentPane().add(chckbxUseVengeance);
	
	JCheckBox chckbxUseSpecialAttack = new JCheckBox("Use special attack?");
	chckbxUseSpecialAttack.setBounds(52, 172, 173, 23);
	chckbxUseSpecialAttack.setSelected(true);
	frmCorporealBeastSettings.getContentPane().add(chckbxUseSpecialAttack);
	
	JCheckBox chckbxSafeRoute = new JCheckBox("Saferoute (games necklace in bank)");
	chckbxSafeRoute.setSelected(false);
	chckbxSafeRoute.setBounds(52, 198, 248, 23);
	frmCorporealBeastSettings.getContentPane().add(chckbxSafeRoute);
	
	JLabel lblLeaderNameto = new JLabel("Leader name (to rejoin instance):");
	lblLeaderNameto.setFont(new Font("Tahoma", Font.PLAIN, 11));
	lblLeaderNameto.setForeground(Color.BLACK);
	lblLeaderNameto.setBounds(10, 83, 208, 14);
	frmCorporealBeastSettings.getContentPane().add(lblLeaderNameto);
	
	textField_1 = new JTextField();
	textField_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
	textField_1.setForeground(Color.BLACK);
	textField_1.setBounds(180, 81, 145, 20);
	textField_1.setText("Your leader name");
	frmCorporealBeastSettings.getContentPane().add(textField_1);
	textField_1.setColumns(10);
	
	chckbxIsLeaderAccount.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(textField_1.isEnabled()) {
				textField_1.setEnabled(false);
			}else {
				textField_1.setEnabled(true);
			}
			
		}
	});
	
	btnNewButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			leaderName = textField_1.getText();
			isLeader = chckbxIsLeaderAccount.isSelected();
			useSpecial = chckbxUseSpecialAttack.isSelected();
			useVenge = chckbxUseVengeance.isSelected();
			useSaferoute = chckbxSafeRoute.isSelected();
			instancePassword = textField.getText();
			finishedGui = true;
			frmCorporealBeastSettings.dispose();
		}
	});
}


 
  public static String runescapeFormat(Integer number) {
    String[] suffix = { "K", "M", "B", "T" };
    int size = (number.intValue() != 0) ? (int)Math.log10(number.intValue()) : 0;
    if (size >= 3)
      while (size % 3 != 0)
        size--; 
    return (size >= 3) ? (String.valueOf(Math.round(number.intValue() / Math.pow(10.0D, size) * 10.0D) / 10.0D) +
      suffix[size / 3 - 1]) : (
      new StringBuilder(String.valueOf(number.intValue()))).toString();
  }


@Override
public int loopDuration() {
	// TODO Auto-generated method stub
	return 150;
}
}
