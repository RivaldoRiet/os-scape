package Zulrah;

import api.MenuActions;
import api.Tasks;
import api.tasks.Supplies.PotionType;
import net.runelite.api.ItemID;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class NpcFighterTask extends Task {
	private Zulrah main;
	public String[] mageArmour = {"spectral", "wyvern", "ahrim", "tome","arcane", "ancestral", "staff", "mystic", "book", "trident", "cape", "occult", "wand", "void mage", "tormented", "zuriel", "hat", "malediction ward"};
	public String[] rangeArmour = {"karil", "buckler", "d'hide", "crossbow", "ava's", "blowpipe", "anguish", "armadyl", "fury", "void ranger", "bow", "gloves", "dragonfire ward", "odium ward"};

	public String[] loot = {"Tanzanite fang", "Magic fang", "Serpentine visage", "Uncut onyx", "Tanzanite mutagen", "Magma mutagen", "Crystal weapon seed",
			"Jar of swamp", "Coin casket (giant)",
			"Battlestaff", "Magic seed", "Torstol seed", "Dragon bones", "Adamantite bar", "Runite ore", "Mahogany logs", "Zul-andra teleport", 
			"Grapes", "Coal", "Zulrah's scales"};

	public final int rangeZulrah = 2042;
	public final int mageZulrah = 2044;
	public final int meleeZulrah = 2043;

	public final int zulrah_pilar = 11699;
	public final int zulrah_smog = 3961;

	public int zulrah_toxic_cloud = 11700;
	public WorldPoint center_spot = null;
	public WorldPoint center_back_spot = null;
	public WorldPoint right_spot = null;
	public WorldPoint left_spot = null;

	public WorldPoint tile_we_should_use = null;

	public WorldPoint zulrah_location = null;
	public WorldPoint pilar_location = null;
	public WorldArea mage_lobby = new WorldArea(new WorldPoint(2527, 4708, 0), new WorldPoint(2549, 4725, 0));
	public int danger_cloud = 0;
	public WorldArea zulrah_lair = null;
	
	SimpleEntityQuery<SimpleObject> objects = null;
	SimpleNpc zulrah = null;
	public long zulrahTimer = 0;

	public long antiGlitchTimer = 0;
	public boolean running = false;
	public boolean paid = false;
	public boolean collectItems = false;


	public String zulrah_ids = "Zulrah";
	
	public NpcFighterTask(ClientContext ctx, Zulrah main) {
		super(ctx);
		this.main = main;
	}

	@Override
	public boolean condition() {
		return  main.isInZulrahRoom();
	}

	@Override
	public void run() {
		main.status = "Calculating conditions";
		/* if(staffdetected) {
			ctx.log("detected staff shutting down");
			this.teleportToEdge();
			getTheFuckOut();
		} */
		setZulrah();
		prepot();
		 if(main.lootOnGround()) {
			main.status = "Looting";
			lootItem();
		}else {
			if(main.lootOnGround()) {
				lootItem();
				return;
			}
			
			if(!main.containsFood() && !main.lootOnGround()) {
				main.status = "Out of food";
				main.teleportToEdge();
				return;
			}
			if(main.shouldRestock()) {
				main.status = "Restocking";
				main.teleportToEdge();
				return;
			}
			if(shouldEat() && main.containsFood()) {
				main.status = "Combo eating";
				comboEat();
			}else if(!shouldEat() && main.containsFood() && !main.shouldRestock()){
				updateObjs();
					if(main.lootOnGround()) {
						lootItem();
						return;
					}
					if(!shouldSwitchGear()) {
						this.switchGear();
					}
					
					if (this.isPoisonedOrVenomed()) {
						this.drinkAntiPoison();
					}
					
					if(this.isMeleeZulrahActive() && !shouldEat()) {
						main.status = "handeling melee zulrah";
						avoidMeleeZulrah();
					}else {
						if(!shouldSwitchGear()) {
							//fight
							main.status = "Attacking zulrah";
							this.attackZulrah();
						}
						//updateObjs();
						if(shouldWalkNext() && !cloudOnSafeTile() && !this.isZulrahNull()) {
							main.status = "Walking next tileset";
							walkNext();
						}
						//updateObjs();
						if(!isZulrahLocationRight()) {
							getZulrahLocation();
						}
						//updateObjs();
						if(!isPilarLocationRight()) {
							getPilarLocation();
						}
						//updateObjs();
						if(shouldUpdateLoc()) {
							this.main.last_safe_location = this.getSafeTile();
						}
						//updateObjs();
						if(shouldWalkNextNull() && !cloudOnSafeTile() && !main.lootOnGround()) {
							main.status = "Walking next tileset";
							walkNextNull();
						}
						if(shouldWalkNext() && !cloudOnSafeTile() && !this.isZulrahNull() && !main.lootOnGround()) {
							main.status = "Walking next tileset";
							walkNext();
						}
						//updateObjs();
						if(shouldSwitchGear()) {
							main.status = "Switching gear";
							switchGear();
						}
					//	updateObjs();
						if(!isPrayerRight() && (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) > 0) {
							main.status = "Setting prayer";
							setPrayer();
						}
					}
					
				}
				drinkPrayerPot();
				main.setRun();
			}
	}
	
	public boolean isZulrahDissapearing() {
		if(zulrah == null) {
			return false;
		}
		/*
		if(zulrah != null && zulrah.getAnimation() == 5070) {
			return true;
		}
		if(zulrah != null && zulrah.getAnimation() == 5071) {
			return true;
		}
		
		if(zulrah != null && zulrah.getAnimation() == 5073) {
			return true;
		}*/
		if(zulrah != null && zulrah.getAnimation() == 5072) {
			return true;
		}
		return false;
		
	}
	
	public boolean cloudOnSafeTile() {
		if(pilar_location != null && this.zulrah_location != null) {
			WorldPoint cloudTile = new WorldPoint(pilar_location.getX() + 1, pilar_location.getY() + 2, 0);
			SimpleObject cloudOnSafeTile = ctx.objects.populate().filter(cloudTile).filter(this.zulrah_toxic_cloud).next();
			WorldPoint left_tile = new WorldPoint(pilar_location.getX() + 4, pilar_location.getY() + 11, 0);
			if(ctx.players.getLocal().getLocation().equals(left_tile) && zulrah_location.equals(new WorldPoint(pilar_location.getX() - 2, pilar_location.getY() - 4,0))) {	
			}
		}
		
		return false;
	}

	public boolean isMeleeZulrahActive() {
		if(this.zulrah != null && this.zulrah.getId() == meleeZulrah) {
			return true;
		}
		return false;
	}

	public void avoidMeleeZulrah() {
		this.updateObjs();
		if(!main.useMage) {
			main.useMage = true;
		}
		
		if(this.isMeleeZulrahActive()) {
			if(pilar_location != null) {
			//WorldPoint right_tile = new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 11, 0);
			//WorldPoint left_tile = new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 9, 0);
			WorldPoint safespot = new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 5, 0);
			SimplePlayer p = ctx.players.getLocal();

				if(!safespot.equals(p.getLocation())) {
					this.main.status = "Safespotting melee zulrah";
					walkToTile(safespot);
					this.switchGear();
				}else {
					this.main.status = "Attacking melee zulrah";
					attackMeleeZulrah();
				}
			}
		}
	}
	

	public SimpleItem getItemsContainsName(String[] array){
		SimpleItem item;
		for(int i = 0; i < array.length; i++) {
			String s = array[i];
			item = ctx.inventory.populate().filter(it -> it.getName().toLowerCase().contains(s)).next();
			if(item != null) {
				return item;
			}
			
		}
		return null;
	}

	
	public boolean shouldSwitchGear() {
		if(main.useMage && getItemsContainsName(mageArmour) != null) {
			return true;	
		}
		
		if(!main.useMage && getItemsContainsName(rangeArmour) != null) {
			 return true;	
		}

		return false;
	}

	public void attackMeleeZulrah() {
		SimpleNpc z = ctx.npcs.populate().filter(this.zulrah_ids).filter(n -> !n.isDead()).nearest().next();
			if(z != null && !ctx.players.getLocal().inCombat() && !shouldEat()) {
				Tasks.getCombat().attack(z);
	        }
	}


	public void walkNext() {
		this.updateObjs();
		this.main.last_safe_location = getSafeTile();
		
		if(!isZulrahNull() && main.last_safe_location != null && !main.last_safe_location.equals(ctx.players.getLocal().getLocation())) {
			if(this.main.last_safe_location != null) {
				if(!ctx.players.getLocal().getLocation().equals(main.last_safe_location)) {
					walkToTile(main.last_safe_location);
				}
			}
		}else if(!isZulrahNull() && main.last_safe_location == null) {
			if(main.last_safe_location != null) {
				if(!ctx.players.getLocal().getLocation().equals(main.last_safe_location)) {
					walkToTile(main.last_safe_location);
				}
			}
		}
	}

	public void walkNextNull() {
		if(this.isZulrahNull() && pilar_location != null) {
			WorldPoint right_tile = new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 11, 0);
			WorldPoint left_tile = new WorldPoint(pilar_location.getX() + 4, pilar_location.getY() + 11, 0);

			SimplePlayer p = ctx.players.getLocal();
			if(p != null && p.getLocation() != null && this.isZulrahNull() && pilar_location != null) {

				if(((right_tile.distanceTo(p.getLocation()) + 2) - (left_tile.distanceTo(p.getLocation()))) > 0 ) {
					//left tile closer
					walkToTile(new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 11, 0));
					//ctx.sleep(1300);
					//ctx.onCondition(() -> !isZulrahNull(), 2000);
					this.updateObjs();
					//ctx.onCondition(() -> ctx.players.getLocal().getLocation().distanceTo(right_tile) <= 2, 3000);
				}else {
					//right tile closer
					walkToTile(new WorldPoint(pilar_location.getX() - 3, pilar_location.getY() + 3, 0));
					//ctx.sleep(1300);
					this.updateObjs();
					//ctx.onCondition(() -> ctx.players.getLocal().getLocation().distanceTo(left_tile) <= 2, 3000);
				}
			}
		}
	}

	
	public void switchGear() {
		if(main.useMage) {
			//mage gear
			Tasks.getInventory().equipAll(mageArmour);
		}else {
			//range gear
			Tasks.getInventory().equipAll(rangeArmour);
		}
	}

	public void setZulrah()
	{
		if(zulrah != null) {
			if(zulrah.getId() == rangeZulrah) {
				main.useMage = true;
			}else if(zulrah.getId() == mageZulrah) {
				main.useMage = false;
			 }else if(zulrah.getId() == this.meleeZulrah) {
					main.useMage = true;
			}
		}
	}
	
	public void setPrayer() {
		if(zulrah != null && (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) > 0) {
			if(zulrah.getId() == rangeZulrah) {
				main.status = "setting range pray";
				main.useMage = true;
				Tasks.getSkill().removeAll();
				if(main.useAugury) {
					setPrayer(Prayers.AUGURY);
				}else {
					setPrayer(Prayers.MYSTIC_MIGHT);
					setPrayer(Prayers.STEEL_SKIN);
				}
				setPrayer(Prayers.PROTECT_FROM_MISSILES);
			}else if(zulrah.getId() == mageZulrah) {
				main.useMage = false;
				main.status = "setting mage pray";
				Tasks.getSkill().removeAll();
				if(main.useRigour) {
					setPrayer(Prayers.RIGOUR);
				}else {
					setPrayer(Prayers.EAGLE_EYE);
					setPrayer(Prayers.STEEL_SKIN);
				}
				setPrayer(Prayers.PROTECT_FROM_MAGIC);
			}else if(zulrah.getId() == this.meleeZulrah) {
				main.useMage = true;
			}
		}
	}
	
	public void walkToTile(WorldPoint w) {
		 if(w != null && !ctx.players.getLocal().getLocation().equals(w)) {
	     	if(ctx.pathing.reachable(w)) {
	     		if(ctx.pathing.step(w)){
	 				ctx.sleep(100);
	 			}
	     	}
	 	}
	}

	public WorldPoint getSafeTile() {
		if(readyToCalculateTile()) {

				if(zulrah_location.equals(new WorldPoint(pilar_location.getX() - 2, pilar_location.getY() + 5,0))) {
					// zulrah is center tile 
					WorldPoint right_tile = new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 11, 0);
					WorldPoint left_tile = new WorldPoint(pilar_location.getX() + 4, pilar_location.getY() + 11, 0);
						if(((right_tile.distanceTo(ctx.players.getLocal().getLocation()) - 2) - (left_tile.distanceTo(ctx.players.getLocal().getLocation()))) > 0 ) {
							return left_tile;
						}else {
							return right_tile;
						}
						
				}

				if(zulrah_location.equals(new WorldPoint(pilar_location.getX() - 2, pilar_location.getY() - 4,0))) {
					// zulrah is center back tile 
					// go stand on the left center tile if no poison cloud is there
					SimpleEntityQuery<SimpleObject> objects = ctx.objects.populate();
					SimpleObject toxic_cloud = null;
					SimpleObject toxic_cloud_right = null;

					if(!objects.filter(11700).isEmpty()) {
						toxic_cloud = objects.filter(11700).nearest(new WorldPoint(pilar_location.getX() + 3, pilar_location.getY() + 3, 0)).next();
						toxic_cloud_right = objects.filter(11700).nearest(new WorldPoint(pilar_location.getX() - 3, pilar_location.getY() + 3, 0)).next();
					}
					
					if(toxic_cloud == null) {
						return new WorldPoint(pilar_location.getX() + 3, pilar_location.getY() + 3, 0);
					}
					
					if(toxic_cloud != null && toxic_cloud.getLocation().distanceTo(new WorldPoint(pilar_location.getX() + 3, pilar_location.getY() + 3, 0)) > 1) {
						return new WorldPoint(pilar_location.getX() + 3, pilar_location.getY() + 3, 0);
					}else if(toxic_cloud_right != null && toxic_cloud_right.getLocation().distanceTo(new WorldPoint(pilar_location.getX() - 3, pilar_location.getY() + 3, 0)) > 1){
						return new WorldPoint(pilar_location.getX() - 3, pilar_location.getY() + 3, 0);
					}else {
						// zulrah is left spot
						return new WorldPoint(pilar_location.getX() + 4, pilar_location.getY() + 11, 0);
					}
				}

				if(zulrah_location.equals(new WorldPoint(pilar_location.getX() - 12 , pilar_location.getY() + 4,0))) {
					// zulrah is right spot

					return new WorldPoint(pilar_location.getX() - 4, pilar_location.getY() + 11, 0);
				}

				if(zulrah_location.equals(new WorldPoint(pilar_location.getX() + 8 , pilar_location.getY() + 4,0))) {
					// zulrah is left spot

					return new WorldPoint(pilar_location.getX() + 4, pilar_location.getY() + 11, 0);
				}
			}

		return null;
	}

	public void getPilarLocation() {
		SimpleObject pilar = getObj(zulrah_pilar);
		if(pilar != null && pilar.getLocation() != null && !pilar.getLocation().equals(this.pilar_location)) {
			pilar_location = pilar.getLocation();
		}
	}

	public void getZulrahLocation() {
		if(zulrah != null && zulrah.getLocation() != null && !zulrah.getLocation().equals(this.zulrah_location)) {
			zulrah_location = zulrah.getLocation();
			this.main.last_safe_location = this.getSafeTile();
		}
	}
	
	public void setPrayer(Prayers p) {
		Tasks.getSkill().addPrayer(p);
	}

	public void getTheFuckOut() {
		ctx.log("detected staff shutting down");
		main.teleportToEdge();
		ctx.stopScript();
	}
	

	public boolean shouldEat() {
		if(ctx.players.getLocal().getHealth() <= 61 && !main.edge.containsPoint(ctx.players.getLocal().getLocation())) {
			return true;
		}
		return false;
	}

	public boolean isPoisonedOrVenomed() {
		return ctx.getClient().getVar(VarPlayer.IS_POISONED) >= 30;
	}

	public final static int[] ANTI_IDS = { ItemID.ANTIPOISON4, ItemID.ANTIPOISON3, ItemID.ANTIPOISON2, ItemID.ANTIPOISON1,
			ItemID.ANTIVENOM4_12913, ItemID.ANTIVENOM3_12915, ItemID.ANTIVENOM2_12917, ItemID.ANTIVENOM1_12919 };

	public void drinkAntiPoison() {
		SimpleItem potion = ctx.inventory.populate().filter(ANTI_IDS).next();
		if (potion != null && potion.click(0)) {
		}
	}

	public void updateObjs(){
		this.objects = ctx.objects.populate();
		this.zulrah = ctx.npcs.populate().filter(zulrah_ids).next();
		SimpleObject pilar = ctx.objects.populate().filter(this.zulrah_pilar).next();
		if(main.isInZulrahRoom() && pilar != null) {
			this.pilar_location = pilar.getLocation();
		}
		
		if(main.isInZulrahRoom() && !this.isZulrahNull()) {
			this.zulrah_location = zulrah.getLocation();
			this.main.last_safe_location = this.getSafeTile();
		}	
	}
	
	public void drinkPrayerPot() {
		int prayerlvl =  Integer.valueOf(ctx.skills.realLevel(SimpleSkills.Skills.PRAYER)) - (Integer.valueOf(ctx.skills.level(SimpleSkills.Skills.PRAYER)));
		
		if(prayerlvl > 50 && !ctx.players.getLocal().inCombat() && !shouldEat() && !main.edge.containsPoint(ctx.players.getLocal().getLocation())) {
			this.main.status = "Drinking prayer potion";
			SimpleItem prayerpot = ctx.inventory.populate().filter(p -> p.getName().contains("Sanfew")).next();
			Tasks.getSupplies().drink(PotionType.PRAYER);
			this.attackZulrah();
		}
	}
	
	public void attackZulrah() {
		if(!isPrayerRight()) {
			setPrayer();
		}
		this.updateObjs();
		if(specEquipped() && ctx.combat.getSpecialAttackPercentage() >= 50 && !shouldEat()) {
			if(!ctx.combat.specialAttack()) {
				if (ctx.widgets.getWidget(160,32) !=null) {
					this.main.status = "Using spec wep";
	                ctx.widgets.getWidget(160,32).click(0);
					//ctx.sleep(500);
				}
			}
		}
		SimpleNpc z = ctx.npcs.populate().filter(this.zulrah_ids).filter(n -> !n.isDead()).nearest().next();
			if(z != null && !ctx.players.getLocal().inCombat() && !shouldEat() && !shouldSwitchGear()) {
				Tasks.getCombat().attack(z);
	        }
	}
	
	public boolean isPrayerRight() {
		if(zulrah == null) {
			return true;
		}
		
		if(zulrah != null) {
			if(zulrah.getId() == rangeZulrah) {
				if(this.isPrayerOn(Prayers.PROTECT_FROM_MISSILES)) {
					return true;
				}else {
					return false;
				}
			}else if(zulrah.getId() == mageZulrah) {
				if(this.isPrayerOn(Prayers.PROTECT_FROM_MAGIC)) {
					return true;
				}else {
					return false;
				}
			}
		}
		return false;
	}
	
	public boolean specEquipped() {
		SimpleItem c = ctx.equipment.getEquippedItem(EquipmentSlot.WEAPON);
		if(c != null && c.getName().toLowerCase().contains("pipe")) {
			return true;
		}
		return false;

	}


	public void lootItem() {
		SimpleGroundItem loot = ctx.groundItems.populate().next();
		if(ctx.inventory.populate().population() == 28) {
			comboEat();
			ctx.sleep(300);
		}
		if(loot != null) {
			main.status = "Looting";
			MenuActions.click(loot, "Take");
		}
	}

	public boolean readyToCalculateTile() {
		return pilar_location != null && zulrah_location != null && ctx.players.getLocal().getLocation() != null;
	}

	public boolean isPrayerOn(Prayers p) {
		return ctx.prayers.prayerActive(p);
	}

	public boolean shouldUpdateLoc() {
		if(getObj(11700) != null) {
			return true;
		}
		return false;
	}

	public boolean shouldWalkNext() {
		if(!isZulrahNull() && main.last_safe_location != null && !main.last_safe_location.equals(ctx.players.getLocal().getLocation())) {
			return true;
		}
		
		if(!isZulrahNull() && main.last_safe_location == null) {
			return true;
		}
		
		return false;
	}

	public boolean shouldWalkNextNull() {
		if(this.isZulrahNull() && pilar_location != null) {
			return true;
		}
		return false;
	}

	public boolean isZulrahLocationRight() {
		if(zulrah == null) {
			return true;
		}
		
		if(zulrah != null && zulrah.getLocation() != null && !zulrah.getLocation().equals(this.zulrah_location)) {
			return false;
		}
		return true;
	}

	public SimpleObject getObj(int id) {
		for(SimpleObject o : this.objects) {
			if(o.getId() == id) {
				return o;
			}
		}
		return null;
	}

	public boolean isPilarLocationRight() {
		if(getObj(this.zulrah_pilar) == null) {
			return true;
		}
		SimpleObject pilar = getObj(zulrah_pilar);
		if(pilar != null && pilar.getLocation() != null && !pilar.getLocation().equals(this.pilar_location)) {
			return false;
		}
		return true;
	}

	public boolean isMeleeZulrahDissapearing() {
		if(zulrah != null && zulrah.getId() == this.meleeZulrah && zulrah.getAnimation() == 5072) {
			return true;
		}
		return false;
	}


	public boolean isZulrahNull() {
		if(ctx.npcs.populate().filter(this.zulrah_ids).isEmpty()) {
			main.last_safe_location = null;
			return true;
		}
		return false;
	}
	
	public void comboEat() {
		main.status = "Combo eating";
		Tasks.getSupplies().doubleEat();
	}

	
	public void prepot() {
		Tasks.getCombat().checkPots();
	}
	
	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "";
	}


	
}
