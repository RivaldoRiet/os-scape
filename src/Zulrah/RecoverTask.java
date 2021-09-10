package Zulrah;

import simple.hooks.scripts.task.Task;
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class RecoverTask extends Task {
	private Zulrah main;
	
	public RecoverTask(ClientContext ctx, Zulrah main) {
		super(ctx);
		this.main = main;
	}

	@Override
	public boolean condition() {
		return false;
	}

	@Override
	public void run() {

	}
	public boolean shouldFillPipe() {
		SimpleItem unfilledpipe = ctx.inventory.populate().filter(12924).next();
		SimpleItem blowpipe = ctx.inventory.populate().filter(12926).next();
		SimpleItem scales = ctx.inventory.populate().filter(12934).next();
		SimpleItem dart = ctx.inventory.populate().filter(p -> p.getName().contains("dart")).next();
		if(unfilledpipe != null && scales != null) {
			return true;
		}
		if(blowpipe != null && scales != null) {
			return true;
		}
		if(blowpipe != null && dart != null) {
			return true;
		}
		return false;
	}

	public void refillPipe() {
		SimpleItem unfilledpipe = ctx.inventory.populate().filter(12924).next();
		SimpleItem scales = ctx.inventory.populate().filter(12934).next();
		SimpleItem dart = ctx.inventory.populate().filter(p -> p.getName().contains("dart")).next();
		
		if(unfilledpipe != null && scales != null && ctx.inventory.contains(unfilledpipe) && ctx.inventory.contains(scales)) {
			if(ctx.inventory.itemOnItem(unfilledpipe, scales)) {
				ctx.sleep(500);
			}
		}
		
		SimpleItem blowpipe = ctx.inventory.populate().filter(12926).next();
		
		if(blowpipe != null && dart != null && ctx.inventory.contains(blowpipe) && ctx.inventory.contains(dart)) {
			if(ctx.inventory.itemOnItem(blowpipe, dart)) {
				ctx.sleep(500);
			}
		}
		SimpleItem serp = ctx.inventory.populate().filter(12929).next();
		if(serp != null && scales != null && ctx.inventory.contains(serp) && ctx.inventory.contains(scales)) {
			if(ctx.inventory.itemOnItem(serp, scales)) {
				ctx.sleep(500);
			}
		}
	}

	public void clickRecoverButton() {
		SimpleWidget w = ctx.widgets.getWidget(602, 6); //screen
		if(w != null) {
			w.click(0);
			main.status = "Recovering items";
			ctx.sleep(400);
		}
	}

	public void clickRecoverClose() {
		SimpleWidget w = ctx.widgets.getWidget(602, 1); //screen
		if(w != null) {
			//text 
			SimpleWidget t = ctx.widgets.getWidget(602, 1).getChild(11);
			if(t != null) {
				t.click(0);
				main.status = "Closing screen";
				ctx.sleep(400);
			}
		}
	}


	public boolean recoverScreenIsOpen() {
		SimpleWidget w = ctx.widgets.getWidget(602, 1); //screen
		if(w != null) {
			//text 
			SimpleWidget t = ctx.widgets.getWidget(602, 1).getChild(1);
			if(t != null && t.getText().contains("Magical chest")) {
				return true;
			}
		}
		return false;
	}
	
	/* public void deathWalk() {
		main.status = "deathwalking";
		if(!main.containsItem("Coins") && paid == false) {
			if(this.isInMageBank()) {
				bankInMageBank();
			}else {
				this.teleToMagebank();
			}
		}
		
		
		
		if(containsItem("Coins") && !inLobby()){
			this.status = "Deathwalk: teleporting to zulrah";
			teleToZulrah();
		}
		
		if(!recoverScreenIsOpen() && ctx.inventory.populate().population() < 28 && inLobby()) {
			SimpleNpc r = ctx.npcs.populate().filter("Priestess Zul-Gwenwynig").next();
			if(r != null && r.validateInteractable()) {
				if(r.click("Collect")) {
					ctx.sleep(1000);
				}
			}
		}
		
		if(containsItem("Coins") && inLobby() && recoverScreenIsOpen() && ctx.inventory.populate().population() < 28) {
			// click recoverscreen button
			this.clickRecoverButton();
			this.clickRecoverButton();
			this.clickRecoverButton();
			this.clickRecoverButton();
			this.paid = true;
		}
		
		if(paid && inLobby() && recoverScreenIsOpen() && ctx.inventory.populate().population() < 28 && this.getAmountOfItemsInRecoverScreen() > 0) {
			// click recoverscreen button
			this.status = "recovering more items";
			this.clickRecoverButton();
			this.clickRecoverButton();
			this.clickRecoverButton();
			this.clickRecoverButton();
		}
		
		if(paid && inLobby() && recoverScreenIsOpen() && ctx.inventory.populate().population() == 28) {
			// close
			this.clickRecoverClose();
			this.clickRecoverClose();
			this.clickRecoverClose();
		}
		
		if(paid && inLobby() && recoverScreenIsOpen() && this.getAmountOfItemsInRecoverScreen() == 0) {
			// close
			this.clickRecoverClose();
			this.clickRecoverClose();
			this.clickRecoverClose();
		}
		
		if(paid && inLobby() && !recoverScreenIsOpen() && ctx.inventory.populate().population() == 28) {
			// switchgear & blowpipe here
			this.switchGear();
			ctx.sleep(500);
			this.useMage = !this.useMage;
			this.switchGear();
			ctx.sleep(500);
			if(this.containsFood()) {
				this.comboEat();
			}
			if(this.shouldFillPipe()) {
				this.refillPipe();
			}
		}
		
		if(inLobby() && !containsItem("Coins") && !this.shouldFillPipe() && this.recoverScreenIsOpen() && this.getAmountOfItemsInRecoverScreen() == 0) {
			this.clickRecoverClose();
			this.clickRecoverClose();
			this.clickRecoverClose();
			
			if(!this.isDialogueOpen()) {
				this.status = "Opening dialogue";
				openDialogue();
				ctx.sleep(1500);
			}
			if(this.isDialogueOpen()) {
				this.status = "Sending input";
				ctx.keyboard.sendKeys("1");
				ctx.sleep(4000);
				if(this.isInZulrahRoom()) {
					this.teleportToEdge();
					ctx.sleep(1000);
					if(!this.inEdge()) {
						this.teleportToEdge();
					}
					this.usePreset();
					ctx.sleep(1000);
					paid = false;
				}
				
			}
		}

		if(inLobby() && !containsItem("Coins") && !this.shouldFillPipe() && !this.recoverScreenIsOpen()) {
			this.clickRecoverClose();
			this.clickRecoverClose();
			this.clickRecoverClose();
			
			if(!this.isDialogueOpen()) {
				this.status = "Opening dialogue";
				openDialogue();
				ctx.sleep(1500);
			}
			if(this.isDialogueOpen()) {
				this.status = "Sending input";
				ctx.keyboard.sendKeys("1");
				ctx.sleep(4000);
				if(this.isInZulrahRoom()) {
					this.teleportToEdge();
					ctx.sleep(1000);
					if(!this.inEdge()) {
						this.teleportToEdge();
					}
					this.usePreset();
					ctx.sleep(1000);
					paid = false;
				}
				
			}
		}
	}

	public int getAmountOfItemsInRecoverScreen() {
		int amount = 0;
		if(this.recoverScreenIsOpen()) {
			SimpleWidget w = ctx.widgets.getWidget(602, 3); // container screen
			if(w != null) {
				SimpleWidget[] c = w.getChildren(); // child
				for(SimpleWidget wc : c) {
					if(wc != null && wc.visibleOnScreen()) {
						amount++;
					}
				}
			}
		}
		return amount;
	}

	public boolean isInMageBank() {
		return this.mage_lobby.containsPoint(this.ctx.players.getLocal().getLocation());
	}


	public void teleToMagebank() {
		if(!isInMageBank()) {
			ctx.keyboard.sendKeys("::mb");
		}
	}

	public void bankInMageBank() {
		SimpleObject BANK_BOOTH = ctx.objects.populate().filter("Bank chest").nearest().next();
		if(this.isInMageBank()) {
			if(ctx.bank.bankOpen() && ctx.inventory.populate().population() > 0) {
				ctx.bank.depositAllExcept("Coins");
				ctx.sleep(1000);
			}
			
			if(ctx.bank.bankOpen() && !containsItem("Coins")) {
				this.status = "withdrawing coins";
					if(ctx.bank.withdraw("Coins", 200000)) {
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

		
		if(containsItem("Coins") && ctx.bank.bankOpen()) {
			if(ctx.bank.closeBank()) {
				ctx.onCondition(() -> !ctx.bank.bankOpen(), 5000);
			}
		}
	} */
	
	@Override
	public String status() {
		// TODO Auto-generated method stub
		return "";
	}
	
}
