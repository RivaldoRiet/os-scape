package api;

import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;

public class Npc {
    public static SimpleNpc getNearest(String name) {
        return ClientContext.instance().npcs.populate().filter(name).nearest().next();
    }

    public static boolean isValid(SimpleNpc npc) {
        return npc != null && npc.visibleOnScreen() && npc.validateInteractable();
    }
}
