package api.tasks;

import api.Locations;
import api.utils.Utils;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class Jail {

    private final ClientContext ctx;
    private WorldArea jailArea = Utils.makeArea(3280, 9424, 3291,9453,0);

    public Jail(ClientContext ctx) {
        this.ctx = ctx;
    }

    public boolean isJailed() {
        return jailArea.containsPoint(ctx.players.getLocal().getLocation());
    }

    private SimpleNpc getPilloryGuard() {
        return ctx.npcs.populate().filter("Pillory Guard").next();
    }

    public boolean isPilloryGuardActive() {
        return this.getPilloryGuard() != null;
    }

    public boolean handlePilloryGuard() {
        SimpleNpc guard = getPilloryGuard();
        if (guard == null) return false;
        ctx.log("Handling guard");
        guard.click(0);
        ctx.sleepCondition(ctx.dialogue::dialogueOpen, 2000);
        while (ctx.dialogue.dialogueOpen()) {
            ctx.dialogue.clickContinue();
            ctx.sleep(200);
        }
        ctx.sleepCondition(() -> !guard.visibleOnScreen(), 1000);
        return getPilloryGuard() == null;
    }

    public boolean handleJail() {
        while (true) {
            if (!isJailed()) {
                return true;
            }
            if (ctx.inventory.inventoryFull()) {
                SimpleItem rock = ctx.inventory.populate().filter("Rock").next();
                if (rock == null) {
                    ctx.log("Inventory is full but no rocks, stopping script");
                    return false;
                }
                else if (ctx.inventory.itemSelectionState() <= 0) {
                    ctx.log("Selecting rock");
                   rock.click("Use");
                }

                SimpleNpc guard = ctx.npcs.populate().filter("Security Guard").next();
                if (guard != null && guard.validateInteractable() && ctx.inventory.itemSelectionState() == 1) {
                    ctx.log("Using rock on guard");
                    guard.click(0);
                }
            }
            else {
                if (!ctx.players.getLocal().isAnimating()) {
                    SimpleObject rock = ctx.objects.populate().filter("Rocks").nearest().next();
                    if (rock != null && rock.validateInteractable()) {
                        ctx.log("Mining");
                        rock.click(0);
                        ctx.onCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
                    }
                }
            }
        }
    }
}
