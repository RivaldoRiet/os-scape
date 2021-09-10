// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.overlays;

import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.plugins.zulrah.ZulrahInstance;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;

@Singleton
public class ZulrahCurrentPhaseOverlay extends Overlay
{
    private final ZulrahPlugin plugin;
    
    @Inject
    ZulrahCurrentPhaseOverlay(final ZulrahPlugin plugin) {
        this.setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.setPriority(OverlayPriority.HIGH);
        this.plugin = plugin;
    }
    
    public Dimension render(final Graphics2D graphics) {
        final ZulrahInstance instance = this.plugin.getInstance();
        if (instance == null) {
            return null;
        }
        final ZulrahPhase currentPhase = instance.getPhase();
        if (currentPhase == null) {
            return null;
        }
        return null;
    }
}
