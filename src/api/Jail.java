package api;


import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class Jail {

    private static final WorldArea jailArea = Locations.makeArea(3280, 9424, 3291,9453,0);

    public static boolean isJailed() {
        return jailArea.containsPoint(ClientContext.instance().players.getLocal().getLocation());
    }

    private static SimpleNpc getPilloryGuard() {
        return Npc.getNearest("Pillory Guard");
    }

    public static boolean isPilloryGuardActive() {
        return getPilloryGuard() != null;
    }

    public static boolean handlePilloryGuard() {
        ClientContext ctx = ClientContext.instance();

        SimpleNpc guard = getPilloryGuard();
        if (Npc.isValid(guard)) return false;

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

    public static boolean handleJail() {
        ClientContext ctx = ClientContext.instance();
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

                SimpleNpc guard = Npc.getNearest("Security Guard");
                if (Npc.isValid(guard) && ctx.inventory.itemSelectionState() == 1) {
                    ctx.log("Using rock on guard");
                    guard.click(0);
                }
            }
            else {
                if (!ctx.players.getLocal().isAnimating()) {
                    SimpleObject rock = Objects.getNearest("Rocks");
                    if (Objects.isValid(rock)) {
                        ctx.log("Mining");
                        rock.click(0);
                        ctx.onCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
                    }
                }
            }
        }
    }
}
