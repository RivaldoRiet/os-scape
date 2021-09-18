package api;

import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Widgets {

    public static SimpleWidget getWidget(int id, int sub_id) {
        return ClientContext.instance().widgets.getWidget(id, sub_id);
    }

    public static boolean isValidWidget(SimpleWidget w) {
        return w != null && w.visibleOnScreen();
    }

    public static Game.Tab getGameTab() {
        return ClientContext.instance().game.tab();
    }

    public static void openTab(Game.Tab tab) {
        if (!isTabOpen(tab)) ClientContext.instance().game.tab(tab);
    }

    public static boolean isTabOpen(Game.Tab tab) {
        return ClientContext.instance().game.tab().equals(tab);
    }
}
