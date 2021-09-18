package api;

import simple.hooks.simplebot.Game;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Camera {

    public enum Zoom{
        MAX_ZOOM,
        ZOOM_2,
        ZOOM_3,
        MED_ZOOM,
        ZOOM_5,
        ZOOM_6,
        MIN_ZOOM
    }

    public static void setZoom(Zoom zoom) {
        Game.Tab tab = Widgets.getGameTab();
        if (!Widgets.isTabOpen(Game.Tab.OPTIONS)) {
            Widgets.openTab(Game.Tab.OPTIONS);
            ClientContext.instance().sleepCondition(() -> Widgets.isTabOpen(Game.Tab.OPTIONS), 1000);
        }

        SimpleWidget widget = Widgets.getWidget(116, 51 + zoom.ordinal());
        if (Widgets.isValidWidget(widget)){
            widget.click(0);
            ClientContext.instance().log("Adjusting zoom");
        }
        else {
            ClientContext.instance().log("Failed adjusting zoom");
        }

        if (!Widgets.isTabOpen(tab)) {
            Widgets.openTab(tab);
        }
    }

    public static void zoomOut() {
        setZoom(Zoom.MAX_ZOOM);
    }

    public static void setNorth() {
        SimpleWidget north = Widgets.getWidget(548, 7);
        if (Widgets.isValidWidget(north)) {
            north.click(0);
        }
    }

    public static void setupCameraZoom() {
        ClientContext ctx = ClientContext.instance();
        setNorth();
        zoomOut();
        ctx.viewport.pitch(true);
    }

}
