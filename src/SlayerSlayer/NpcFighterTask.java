package SlayerSlayer;

import api.Tasks;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimplePlayers;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class NpcFighterTask extends Task {
	private Main main;

	public NpcFighterTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
	}

	@Override
	public boolean condition() {
		return  !main.shouldRestockFromWild() && main.currentMonster != null && !ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead()).isEmpty();
	}

	@Override
	public void run() {
        SimplePlayer p = ctx.players.getLocal();
        main.slayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.SLAYER));
        
        if(this.pkerFound()) handlePker();
        
        if(!isPrayerRight() && (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) > 0) {
			main.status = "Setting prayer";
			setPrayer();
		}
        
        //SimpleNpc npcToAttack = getNpc();

        if(lootOnGround() && ctx.inventory.populate().population() < 28) {
        	main.status = "looting";
        	lootItem();
        }else {
        	if (!p.inCombat()) {
        		SimpleNpc npc = getNpc();
		        if(npc != null) {
	        		main.status = "fighting";
        			Tasks.getCombat().attack(npc);
	        		ctx.onCondition(() -> !p.inCombat() && this.pkerFound() && !npc.isDead() && main.currentMonster == null && !isPrayerRight(), 3000);
		        }
        	}
        }
        
        checkAntiFire();
      //  if(p.isAnimating()) {
    	//	ctx.onCondition(() -> !p.isAnimating(), 1000);
    	//}
        Tasks.getCombat().checkPots();
        drinkPrayerPot();
        openCasket();
        antiStuck();
        tryEat();
        
        int strlvl = Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.STRENGTH));
		if(strlvl <= 112) {
			if(containsItem("combat")) {
				prepot();
			}
		}
		
		int rangelvl = Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.RANGED));
		if(rangelvl <= 108) {
			if(containsItem("Bastion")) {
				prepot();
			}
		}
        
        if(this.pkerFound()) handlePker();
        
        ctx.sleep(5);
	}
	
	@Override
	public String status() {
		return "Fighting slayer task";
	}

	
	private SimpleNpc getNpc() {
		SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead());
	     
		if(!ctx.combat.inMultiCombat()) {
        	npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead()).filter(n -> n.getInteracting() != null && n.getInteracting().getName().equals(ctx.players.getLocal().getName()));
        	if(npcs.size() <= 0) {
        		npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead()).filter(n -> !n.inCombat());
        	}
        }else { 
        	npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead()).filter(n -> n.getInteracting() != null && n.getInteracting().getName().equals(ctx.players.getLocal().getName()));
        	if(npcs.size() <= 0) {
        		npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.inCombat()).filter(n -> !n.isDead());
	        	if(npcs.size() <= 0) {
	        			npcs = ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead());
	        	}
        	}
        	
        }
		
		if(npcs.size() == 0){
			return null;
		}else {
			return npcs.nearest().next();
		}
	}
	private boolean containsItem(String itemName) {
		return !ctx.inventory.populate().filter(p -> p.getName().contains(itemName)).isEmpty();
	}
	
	private void tryEat() {
		if(getPercentageHitpoints() <= 50 && hasFood()) {
			SimpleItem shark = ctx.inventory.populate().filter(p -> p.getName().contains("Shark")).next();
			if(shark != null && shark.click(0)) {
				ctx.sleep(500);
			}
		}
	}
	
	private int getPercentageHitpoints()
	{
		//returns 50 for example
		float perc = ((float) ctx.skills.level(Skills.HITPOINTS) / ctx.skills.realLevel(Skills.HITPOINTS));
		float perc1 = (perc * 100);
		return (int) perc1;
	}
	
	private boolean hasFood()
	{
		if(containsItem("Shark")) {
			return true;
		}
		return false;
	}
	
	private boolean pkerFound() {
		SimplePlayerQuery<SimplePlayer> players = ctx.players.populate().filter(n -> n.getInteracting() != null && n.getInteracting().getName().equals(ctx.players.getLocal().getName()));
    	
		if(players.size() > 0) {
			return true;
		}
		return false;
	}
	
	private void handlePker() {
		if(inWildy()) {
			if(!above30wild()) {
				main.status = "Searching for glory";
				SimpleItem glory = ctx.equipment.populate().filter(e -> e.getName().toLowerCase().contains("glory")).next();
				
				if(glory != null && glory.validateInteractable()) {
					main.status = "Teleporting edge";
					if(glory.click("Edgeville")) {
						ctx.sleep(300);
					}
				}
			}else {
				if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
					ctx.updateStatus("Turning run on");
					ctx.pathing.running(true);
				}
				
				ctx.pathing.walkPath(main.previousPath, true);
			}
		}
	}
	
	private boolean inWildy() {
		SimpleWidget w = ctx.widgets.getWidget(90, 59); // wilderness widget
		if(w != null && w.visibleOnScreen() && w.getText().contains("Level")) {
			int wildlvl = Integer.parseInt(w.getText().split("Level: ")[1]);
			if(wildlvl > 0) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean above30wild() {
		SimpleWidget w = ctx.widgets.getWidget(90, 53); // wilderness widget
		if(w != null && w.visibleOnScreen() && w.getText().contains("Level")) {
			int wildlvl = Integer.parseInt(w.getText().split("Level: ")[1]);
			if(wildlvl > 30) {
				return true;
			}
		}
		
		return false;
	}
	
	private void antiStuck() {
		if(main.antiStuck) {
			ctx.log("antistuck activated");
			// anti stuck
			SimplePlayer p = ctx.players.getLocal();
			WorldPoint w = new WorldPoint(p.getLocation().getX(), p.getLocation().getY() - 4 ,0);
			if(ctx.pathing.reachable(w)) {
				ctx.pathing.step(w);
			}else {
				w = new WorldPoint(p.getLocation().getX() + 4, p.getLocation().getY() ,0);
				if(ctx.pathing.reachable(w)) {
					ctx.pathing.step(w);
				}
			}
			main.antiStuck = false;
		}
	}
	
	private void checkAntiFire() {
		if(main.currentMonster != null && main.currentMonster.contains("dragon") && main.shouldDrinkAntifire) {
			if(main.antifireTimer == 0 || main.antifireTimer != 0 && (System.currentTimeMillis() - main.antifireTimer) > 150000 ) {
				drinkAntiFire();
				main.antifireTimer = System.currentTimeMillis();
				main.shouldDrinkAntifire = false;
			}
		}
	}
	
	public void drinkAntiFire() {
		SimpleItem pot = ctx.inventory.populate().filter(p -> p.getName().contains("antifire")).next();
		if(pot != null && pot.click(0)) {
			ctx.sleep(1200);
		}
	}
	
	
	private void prepot() {
		SimpleItem pot = ctx.inventory.populate().filter(p -> p.getName().contains("combat")).next();
		if(pot != null && pot.click(0)) {
			ctx.sleep(500);
		}
		
		SimpleItem bpot = ctx.inventory.populate().filter(p -> p.getName().contains("Bastion")).next();
		if(bpot != null && bpot.click(0)) {
			ctx.sleep(500);
		}
	}
	
	private void drinkPrayerPot() {
		int prayerlvl =  Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) - (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER)));
		
		if(prayerlvl > 30) {
			SimpleItem prayerpot = ctx.inventory.populate().filter(p -> p.getName().contains("Sanfew")).next();
			if(prayerpot != null && prayerpot.click(0)) {
				ctx.sleep(1200);
				ctx.onCondition(() -> Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) - (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) <= 30, 1000);
			}else {
				SimpleItem restorepot = ctx.inventory.populate().filter(p -> p.getName().contains("restore")).next();
				if(restorepot != null && restorepot.click(0)) {
					ctx.sleep(1200);
					ctx.onCondition(() -> Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) - (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) <= 30, 1000);
				}
			}
		}
	}

	private void setPrayer() {
		if(main.useOffensivePrayer) {
			if(main.usePrayer > 0) {
				if(main.usePrayer == 1) {
					Tasks.getSkill().addPrayer(Prayers.PIETY);
				}
				if(main.usePrayer == 2) {
					Tasks.getSkill().addPrayer(Prayers.RIGOUR);
				}
				if(main.usePrayer == 3) {
					Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
				}
			}
		}
		Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);
		Tasks.getSkill().addPrayer(Prayers.PROTECT_ITEM);
	}
	
	private void setPrayer(Prayers p) {
		if(!isPrayerOn(p)) {
			ctx.prayers.prayer(p);
		}
	}
	
	private boolean isPrayerOn(Prayers p) {
		return ctx.prayers.prayerActive(p);
	}
	
	private boolean isPrayerRight() {
		if(this.isPrayerOn(Prayers.PROTECT_FROM_MELEE)) {
			return true;
		}
		return false;
	}
	
	private void openCasket() {
		
		SimpleItemQuery<SimpleItem> giant = ctx.inventory.populate().filter(p -> p.getName().toLowerCase().contains("iant"));
		if (giant != null && giant.size() > 0) {
			ctx.log("We have a giant casket, skipping opening");
			return;
		}
		
		SimpleItem casket = ctx.inventory.populate().filter(p -> !p.getName().toLowerCase().contains("giant") && p.getName().contains("casket")).next();
		if(casket != null && casket.click(0)) {
			ctx.sleep(500);
		}
	}

	private boolean lootOnGround() {
		SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate().filter(main.lootName).filter(l -> ctx.players.getLocal().distanceTo(l) <= 20);
		if(lootation.size() > 0) {
			return true;
		}
		return false;
	}

	private void lootItem() {
		SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate().filter(main.lootName).filter(l -> ctx.players.getLocal().distanceTo(l) <= 20);
		if (lootation.size() > 0) {
			SimpleGroundItem item = lootation.nearest().next();
			if (item != null) {
				Tasks.getLoot().loot(main.lootName);
			}
			SimpleGroundItem item1 = lootation.reverse().nearest().next();

			if (item1 != null) {
				Tasks.getLoot().loot(main.lootName);
			}
		}
	}


	
}
