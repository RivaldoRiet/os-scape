package StallThiever;

import api.MenuActions;
import api.Tasks;
import api.utils.Utils;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleNpc;
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

public class StallThiever extends Script {

    private long STARTTIME;
    private int totalStalls = 0, startXp, startLvl;
    private SimplePlayer staff = null;
    private boolean avoidStaff = true;

    final private WorldArea stallsArea = Utils.makeArea(3092,3487,3098,3485,0);

    @Override
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getMessage().contains("You attempt to steal")) {
            totalStalls += 1;
        }
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
        if (Tasks.getJail().isJailed()) {
            ctx.log("Jailed");
            if (!Tasks.getJail().handleJail()) {
                ctx.log("Failed handling jail, stopping");
                ctx.sendLogout();
                ctx.stopScript();
            }
        }
        else if (Tasks.getJail().handlePilloryGuard()) {
            ctx.log("Successfully handled guard event");
        }

        else if (ctx.inventory.inventoryFull()) {
            ctx.log("Banking");
            if (ctx.bank.bankOpen()) {
                ctx.bank.depositInventory();
                ctx.sleepCondition(() -> ctx.inventory.isEmpty(), 1000);
            }
            else {
                SimpleObject bank = Tasks.getBanking().getBank();
                if (bank != null) {
                    bank.click(0);
                    ctx.sleepCondition(() -> ctx.bank.bankOpen(), 3000);
                }
            }
        }

        else if (!stallsArea.containsPoint(ctx.players.getLocal().getLocation())) {
            // Walk to stalls
            if (ctx.pathing.reachable(stallsArea.randomTile())) {
                ctx.log("Walking to stalls");
                ctx.pathing.step(stallsArea.randomTile());
            }
            else {
                // Tele to edge
                ctx.log("Teleporting to edge");
                MenuActions.invoke("Cast","<col=00ff00>Lumbridge Home Teleport</col>",1,57,-1,14286853);
            }
        } else {
            SimpleObject stall = ctx.objects.populate().filter(Stall.getMaxStall(ctx.skills.level(THIEVING)).getName()).next();
            if (stall != null) {
                if (stall.visibleOnScreen() && stall.validateInteractable()) {
                    ctx.log("Stealing from stall");
                    stall.click(0);
                    ctx.sleepCondition(() -> ctx.players.getLocal().isAnimating(), 500);
                }
                else {
                    ctx.log("Something is wrong, stall is not interactable");
                }
            }
        }
    }

    @Override
    public void onExecute() {
        Tasks.init(ctx);
        this.STARTTIME = System.currentTimeMillis();
        this.startXp = Tasks.getSkill().getXP(THIEVING);
        this.startLvl = Tasks.getSkill().getLvl(THIEVING);

    }

    @Override
    public void onTerminate() {
        ctx.log(String.valueOf(System.currentTimeMillis() - STARTTIME));

    }

    @Override
    public void paint(Graphics graphics) {
        int gainedXp = Tasks.getSkill().getXP(THIEVING) - this.startXp;
        int gainedLvl = Tasks.getSkill().getLvl(THIEVING) - this.startLvl;
        long uptime = System.currentTimeMillis() - this.STARTTIME;
        Graphics2D g = (Graphics2D) graphics;

        g.drawString("Total XP: " +
                Utils.formatNumber(gainedXp) + " / " +
                Utils.formatNumber(this.ctx.paint.valuePerHour(gainedXp, this.STARTTIME)),
                550, 425);
        g.drawString("Uptime: " + this.ctx.paint.formatTime(uptime), 550, 450);

    }
}
