package api;

import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;


public class Objects {
    public static boolean isValid(SimpleObject obj) {
        return obj != null && obj.visibleOnScreen() && obj.validateInteractable();
    }

    public static SimpleObject getNearest(String name) {
        return ClientContext.instance().objects.populate().filter(name).nearest().next();
    }

    public static SimpleObject getNearest(int id) {
        return ClientContext.instance().objects.populate().filter(id).nearest().next();
    }
}
