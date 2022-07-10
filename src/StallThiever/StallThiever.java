package StallThiever;

import api.*;
import api.utils.Bank;
import api.utils.Utils;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.listeners.InventoryChangeEvent;
import simple.hooks.scripts.listeners.InventoryChangeListener;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

import java.awt.*;

/*Author: HunterAgent*/
import static simple.hooks.filters.SimpleSkills.Skills.THIEVING;

@ScriptManifest(
        author = "HunterAgent",
        category = Category.THIEVING,
        description = "OS-Scape stall thiever",
        discord = "HunterAgent#8066",
        name = "Lucky Stall Thiever",
        servers = {"Osscape"},
        version = "0.1")

public class StallThiever extends Script implements InventoryChangeListener {

    private long STARTTIME;
    private int totalBM = 0, startXp;
    private SimplePlayer staff = null;
    private boolean avoidStaff = true;

    final private WorldArea stallsArea = Locations.makeArea(3092,3487,3098,3485,0);

    @Override
    public void onExecute() {
        Tasks.init(ctx);
        Camera.setupCameraZoom();
        this.STARTTIME = System.currentTimeMillis();
        this.startXp = Tasks.getSkill().getXP(THIEVING);
    }


    @Override
    public void onProcess() {
        if (avoidStaff) {
            staff = Tasks.getAntiban().staffFound();
            if (staff != null) {
                ctx.log("Found staff: " + staff.getName());
                Tasks.getAntiban().waitOutStaff(30, true);
            }
        }
        if (Jail.isJailed()) {
            ctx.log("Jailed");
            if (!Jail.handleJail()) {
                ctx.log("Failed handling jail, stopping");
                ctx.sendLogout();
                ctx.stopScript();
            }
        }
        else if (Jail.handlePilloryGuard()) {
            ctx.log("Successfully handled guard event");
        }

        else if (ctx.inventory.inventoryFull()) {
            ctx.log("Banking");
            if (Bank.openBank()) {
                Bank.depositAll();
                ctx.sleepCondition(() -> ctx.inventory.isEmpty(), 1000);
            }
        }

        else if (!stallsArea.containsPoint(Locations.getPlayerLocation())) {
            if (ctx.pathing.reachable(stallsArea.randomTile())) {
                ctx.log("Walking to stalls");
                ctx.pathing.step(stallsArea.randomTile());
            }
            else {
                ctx.log("Teleporting to edge");
                MenuActions.invoke("Cast","<col=00ff00>Lumbridge Home Teleport</col>",1,57,-1,14286853);
            }
        } else {
            SimpleObject stall = Objects.getNearest(Stall.getMaxStall(ctx.skills.level(THIEVING)).getName());
            if (Objects.isValid(stall)) {
                ctx.log("Stealing from stall");
                stall.click(0);
                ctx.sleepCondition(() -> ctx.players.getLocal().isAnimating(), 500);
            }
        }
    }

    @Override
    public void onTerminate() {
        int gainedXp = Tasks.getSkill().getXP(THIEVING) - this.startXp;
        ctx.log("Uptime: " + this.ctx.paint.formatTime(System.currentTimeMillis() - STARTTIME));
        ctx.log("Total XP: " + Utils.formatNumber(gainedXp));
        ctx.log("Total BP: " + Utils.formatNumber(totalBM));
    }

    @Override
    public void paint(Graphics graphics) {
        int gainedXp = Tasks.getSkill().getXP(THIEVING) - this.startXp;
        long uptime = System.currentTimeMillis() - this.STARTTIME;
        Graphics2D g = (Graphics2D) graphics;

        g.drawString("Total XP: " +
                Utils.formatNumber(gainedXp) + " / " +
                Utils.formatNumber(this.ctx.paint.valuePerHour(gainedXp, this.STARTTIME)),
                550, 420);
        g.drawString("Total BM: " +
                        Utils.formatNumber(totalBM) + " / " +
                        Utils.formatNumber(this.ctx.paint.valuePerHour(totalBM, this.STARTTIME)),
                550, 440);
        g.drawString("Uptime: " + this.ctx.paint.formatTime(uptime), 550, 460);

    }

    @Override
    public void onChange(InventoryChangeEvent inventoryChangeEvent) {
        if (inventoryChangeEvent.getItemName().equals("Blood money") && inventoryChangeEvent.getNewQuantity() != 0) {
            totalBM += inventoryChangeEvent.getQuantityChange();
        }
    }

    @Override
    public void onChatMessage(ChatMessage chatMessage) {
    }

}
