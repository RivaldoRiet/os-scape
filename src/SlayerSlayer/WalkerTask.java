package SlayerSlayer;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.task.Task;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class WalkerTask extends Task {
	private Main main;
	private WorldArea graves = new WorldArea(new WorldPoint(3126, 3705, 0), new WorldPoint(3210, 3649, 0));
	private WorldArea graves_mammoth = new WorldArea(new WorldPoint(3123, 3683, 0), new WorldPoint(3177, 3610, 0));
	private WorldArea rev_cave_entrance = new WorldArea(new WorldPoint(3058, 3661, 0), new WorldPoint(3092, 3633, 0));
	private WorldArea rev_cave = new WorldArea(new WorldPoint(3122, 10247, 0), new WorldPoint(3278, 10047, 0));
	private WorldArea edge = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));
	private WorldArea entArea = new WorldArea(new WorldPoint(3182, 3715, 0), new WorldPoint(3240, 3651, 0));
	private WorldArea edge_dungeon = new WorldArea(new WorldPoint(3055, 10013, 0), new WorldPoint(3163, 9813, 0));
	
	private Paths paths = new Paths();
	
	
	public WalkerTask(ClientContext ctx, Main main) {
		super(ctx);
		this.main = main;
	}


	@Override
	public boolean condition() {
		return main.currentMonster != null && !main.shouldRestock() && !main.shouldRestockFromWild() && !main.blockedMonster() && main.isGeared() && !slayerMonsterPresent();
	}

	@Override
	public void run() {
		main.status = "Searching npc path";
		
		if(this.pkerFound()) handlePker();
		
		if(!isPrayerRight() && (Integer.valueOf(this.ctx.skills.level(SimpleSkills.Skills.PRAYER))) > 0) {
			main.status = "Setting prayer";
			setPrayer();
		}
		
		if(nameEquals("ent") && !ctx.pathing.inArea(entArea)) {
			walkEnt();
		}
		
		if(nameContains("greater demon")) {
			this.walkGreater();
		}
		
		if(nameContains("lesser demon")) {
			this.walkLesser();
		}
		
		if(nameEquals("ankou")) {
			this.walkAnkou();
		}
		
		if(nameEquals("black dragon")) {
			this.walkBlackdragon();
		}
		
		if(nameEquals("hellhound")) {
			this.walkHellhound();
		}
		
		if(nameEquals("black demon")) {
			//this.walkBlackDemon();
			this.walkBlackDemon2();
		}
		
		if(nameEquals("green dragon")) {
			this.walkGreenDragon();
		}
		
		if(nameEquals("ice giant")) {
			this.walkIceGiant();
		}
		
		if(nameEquals("mammoth")) {
			this.walkMammoth();
		}
	}
	   
	@Override
	public String status() {
		return "Walking to slayer task";
	}
	
	private boolean nameEquals(String s) {
		return main.currentMonster.toLowerCase().equals(s.toLowerCase());
	}
	
	private boolean nameContains(String s) {
		return main.currentMonster.toLowerCase().contains(s.toLowerCase());
	}
	
	private boolean above20wild() {
		SimpleWidget w = ctx.widgets.getWidget(90, 59); // wilderness widget
		if(w != null && w.visibleOnScreen() && w.getText().contains("Level")) {
			int wildlvl = Integer.parseInt(w.getText().split("Level: ")[1]);
			if(wildlvl > 20) {
				return true;
			}
		}
		
		return false;
	}
	
	private void setPrayer() {
		if(main.useOffensivePrayer) {
			if(main.usePrayer > 0) {
				if(main.usePrayer == 1) {
					setPrayer(Prayers.PIETY);
				}
				if(main.usePrayer == 2) {
					setPrayer(Prayers.RIGOUR);
				}
				if(main.usePrayer == 3) {
					setPrayer(Prayers.EAGLE_EYE);
				}
			}
		}
		setPrayer(Prayers.PROTECT_FROM_MELEE);
		setPrayer(Prayers.PROTECT_ITEM);
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
	
	
	private void teleToGraves() {
		ctx.keyboard.sendKeys("::graves");
		ctx.sleep(300);
		sendSpacebar();
		ctx.sleep(300);
		SimpleWidget w = ctx.widgets.getWidget(219, 1);
		if(w != null) {
			SimpleWidget c = w.getChild(1);
			if(c != null && c.visibleOnScreen() && c.validateInteractable()) {
				c.click(0);
				ctx.sleep(1300);
			}
		}
	}
	
	private void sendSpacebar() {
		SimpleWidget w = ctx.widgets.getWidget(229, 2);
		if(w != null) {
			if(w != null && w.visibleOnScreen() && w.validateInteractable()) {
				w.click(0);
				ctx.sleep(300);
			}
		}
		
		SimpleWidget c = ctx.widgets.getWidget(231, 3);
		if(c != null) {
			if(c != null && c.visibleOnScreen() && c.validateInteractable()) {
				c.click(0);
				ctx.sleep(300);
			}
		}
		
		SimpleWidget d = ctx.widgets.getWidget(11, 4);
		if(d != null) {
			if(d != null && d.visibleOnScreen() && d.validateInteractable()) {
				d.click(0);
				ctx.sleep(300);
			}
		}
	}

	private boolean teleportWizardScreenOpen()
	{
		final SimpleWidget screen = ctx.widgets.getWidget(832, 12);

		if (screen != null && !screen.isHidden() && screen.getText().toLowerCase().contains("teleportation"))
		{
			return true;
		}
		
		return false;
	}

	private void teleToRevCave() {
		if (teleportWizardScreenOpen()) {
			main.invoke("Teleport to","<col=ff9040>Revenants Cave</col>",1,57,48,54525999);
		}else {
			SimpleNpc wiz = ctx.npcs.populate().filter("Teleports wizard").next();
			if (wiz != null) {
				main.clickFirst(wiz.getNpc());
			}
		}
	}
	private void walkMammoth() {
		main.status = "Walking to mammoth";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(graves_mammoth)) {
				main.previousPath = paths.getMammothPath();
				ctx.pathing.walkPath(paths.getMammothPath());
			}else if(!above20wild()){
				teleToGraves();
			}
		}
	}
	
	private void walkGreenDragon() {
		main.status = "Walking to green dragon";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(graves)) {
				main.previousPath = paths.getGreendragonPath2();
				ctx.pathing.walkPath(paths.getGreendragonPath2());
			}else if(!above20wild()){
				teleToGraves();
			}
		}
	}
	
	
	private void walkEnt() {
		main.status = "Walking to ent";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(graves)) {
				main.previousPath = paths.getEntPath();
				ctx.pathing.walkPath(paths.getEntPath());
			}else if(!above20wild()){
				teleToGraves();
			}
		}
	}
	
	private void walkBlackDemon() {
		main.status = "Walking to Black Demons";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			main.status = "Turning run on";
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(!ctx.pathing.inArea(edge_dungeon)) {
				if(!isTeleportScreenOpen()) {
					SimpleNpc wizard = ctx.npcs.populate().filter("Vitality wizard").nearest().next();
						if(wizard != null && wizard.validateInteractable()) {
							if(wizard.click("Teleport")) {
								main.status = "Using teleport";
								ctx.sleep(1000);
							}
						}else {
							main.status = "Teleporting to edge";
							teleportToEdge();
						}
					}else {
						main.status = "Teleporting edge dung";
						teleportToEdgeDung();
					}
				}else {
					main.status = "Walking edge dung";
					main.previousPath = paths.getBlackdemonPath2();
					ctx.pathing.walkPath(paths.getBlackdemonPath2());
				}
			}
			/*
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getBlackdemonPath();
					ctx.pathing.walkPath(paths.getBlackdemonPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}*/
	}
	
	private void walkBlackDemon2() {
		main.status = "Walking to Black Demons";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			main.status = "Turning run on";
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getBlackdemonPath();
					ctx.pathing.walkPath(paths.getBlackdemonPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	private void walkIceGiant() {
		main.status = "Walking to ice giants";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getIceGiantPath();
					ctx.pathing.walkPath(paths.getIceGiantPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	private void walkHellhound() {
		main.status = "Walking to Hellhounds";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getHellhoundPath();
					ctx.pathing.walkPath(paths.getHellhoundPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	private void walkBlackdragon() {
		main.status = "Walking to black dragon";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getBlackDragonPath();
					ctx.pathing.walkPath(paths.getBlackDragonPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	private void walkAnkou() {
		main.status = "Walking to Ankou";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getAnkouPath();
					ctx.pathing.walkPath(paths.getAnkouPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	
	private void walkGreater() {
		main.status = "Walking to greater demon";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getGreaterDemonPath();
					ctx.pathing.walkPath(paths.getGreaterDemonPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	
	private void walkLesser() {
		main.status = "Walking to lesser demon";
		if (!ctx.pathing.running() && ctx.pathing.energyLevel() >= 30 ) {
			ctx.updateStatus("Turning run on");
			ctx.pathing.running(true);
		}
		
		if(main.isGeared()) {
			if(ctx.pathing.inArea(this.rev_cave_entrance)) {
				SimpleObject cave = ctx.objects.populate().filter(31555).filter("Cavern").next();
				if(cave != null && cave.validateInteractable()) {
					if(cave.click("Enter")) {
						ctx.sleep(300);
					}
				}
			}else {
				if(ctx.pathing.inArea(this.rev_cave)) {
					main.previousPath = paths.getLesserdemonPath();
					ctx.pathing.walkPath(paths.getLesserdemonPath());
				}else if(!above20wild()){
					teleToRevCave();
				}
			}
		}
	}
	
	private boolean isTeleportScreenOpen() {
		SimpleWidget w = ctx.widgets.getWidget(804, 2);
		if(w != null && w.visibleOnScreen() && w.getText().contains("Vitality Teleportation")) {
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
					glory.click("Edgeville");
					ctx.sleep(300);
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
		SimpleWidget w = ctx.widgets.getWidget(90, 59); // wilderness widget
		if(w != null && w.visibleOnScreen() && w.getText().contains("Level")) {
			int wildlvl = Integer.parseInt(w.getText().split("Level: ")[1]);
			if(wildlvl > 30) {
				return true;
			}
		}
		
		return false;
	}
	

private void teleportToEdge() {
	this.sendSpacebar();
	ctx.sleep(300);
	ctx.keyboard.sendKeys("::home");
	ctx.sleep(2000);
}
	
	private boolean slayerMonsterPresent() {
		return !ctx.npcs.populate().filter(n -> n.getName().toLowerCase().equals(main.currentMonster)).filter(n -> !n.isDead()).isEmpty();
	}

	private void teleportToEdgeDung() {
		if(this.isTeleportScreenOpen()) {
			SimpleWidget w = ctx.widgets.getWidget(804, 10);
			if(w != null && w.visibleOnScreen()) {
				SimpleWidget scrollList = w.getChild(4);
				if(scrollList != null && scrollList.visibleOnScreen()) {
					if(scrollList.click(0)) {
						ctx.sleep(1000);
					}
				}
			}
			
			SimpleWidget d = ctx.widgets.getWidget(804, 14);
			if(d != null && d.visibleOnScreen()) {
				SimpleWidget dungList = d.getChild(12);
				if(dungList != null && dungList.visibleOnScreen()) {
					if(dungList.click(0)) {
						ctx.sleep(1000);
					}
				}
			}
			
		}
	}
}
