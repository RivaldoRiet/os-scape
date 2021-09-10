// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.overlays;

import org.slf4j.LoggerFactory;
import java.awt.FontMetrics;
import net.runelite.api.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.BasicStroke;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.client.plugins.zulrah.ZulrahInstance;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPosition;
import javax.annotation.Nullable;
import net.runelite.client.plugins.zulrah.ZulrahPlugin;
import net.runelite.api.Client;
import java.awt.Color;
import org.slf4j.Logger;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;

@Singleton
public class ZulrahOverlay extends Overlay
{
    private static final Logger log;
    private static final Color TILE_BORDER_COLOR;
    private static final Color NEXT_TEXT_COLOR;
    private final Client client;
    private final ZulrahPlugin plugin;
    
    @Inject
    ZulrahOverlay(@Nullable final Client client, final ZulrahPlugin plugin) {
        this.setPosition(OverlayPosition.DYNAMIC);
        this.client = client;
        this.plugin = plugin;
    }
    
    public Dimension render(final Graphics2D graphics) {
        final ZulrahInstance instance = this.plugin.getInstance();
        if (instance == null) {
            return null;
        }
        final ZulrahPhase currentPhase = instance.getPhase();
        final ZulrahPhase nextPhase = instance.getNextPhase();
        if (currentPhase == null) {
            ZulrahOverlay.log.debug("Current phase is NULL");
            return null;
        }
        final LocalPoint startTile = instance.getStartLocation();
        if (nextPhase != null && currentPhase.getStandLocation() == nextPhase.getStandLocation()) {
            this.drawStandTiles(graphics, startTile, currentPhase, nextPhase);
        }
        else {
            this.drawStandTile(graphics, startTile, currentPhase, false);
            this.drawStandTile(graphics, startTile, nextPhase, true);
        }
        this.drawZulrahTileMinimap(graphics, startTile, currentPhase, false);
        this.drawZulrahTileMinimap(graphics, startTile, nextPhase, true);
        return null;
    }
    
    private void drawStandTiles(final Graphics2D graphics, final LocalPoint startTile, final ZulrahPhase currentPhase, final ZulrahPhase nextPhase) {
        final LocalPoint localTile = currentPhase.getStandTile(startTile);
        final Polygon northPoly = this.getCanvasTileNorthPoly(this.client, localTile);
        final Polygon southPoly = this.getCanvasTileSouthPoly(this.client, localTile);
        final Polygon poly = Perspective.getCanvasTilePoly(this.client, localTile);
        final Point textLoc = Perspective.getCanvasTextLocation(this.client, graphics, localTile, "Stand / Next", 0);
        if (northPoly != null && southPoly != null && poly != null && textLoc != null) {
            final Color northColor = currentPhase.getColor();
            final Color southColor = nextPhase.getColor();
            graphics.setColor(northColor);
            graphics.fillPolygon(northPoly);
            graphics.setColor(southColor);
            graphics.fillPolygon(southPoly);
            graphics.setColor(ZulrahOverlay.TILE_BORDER_COLOR);
            graphics.setStroke(new BasicStroke(2.0f));
            graphics.drawPolygon(poly);
            graphics.setColor(ZulrahOverlay.NEXT_TEXT_COLOR);
            graphics.drawString("Stand / Next", textLoc.getX(), textLoc.getY());
        }
    }
    
    private void drawStandTile(final Graphics2D graphics, final LocalPoint startTile, final ZulrahPhase phase, final boolean next) {
        if (phase == null) {
            ZulrahOverlay.log.debug("phase null");
            return;
        }
        final LocalPoint localTile = phase.getStandTile(startTile);
        final Polygon poly = Perspective.getCanvasTilePoly(this.client, localTile);
        final Color color = phase.getColor();
        if (poly != null) {
            graphics.setColor(ZulrahOverlay.TILE_BORDER_COLOR);
            graphics.setStroke(new BasicStroke(2.0f));
            graphics.drawPolygon(poly);
            graphics.setColor(color);
            graphics.fillPolygon(poly);
            final Point textLoc = Perspective.getCanvasTextLocation(this.client, graphics, localTile, next ? "Next" : "Stand", 0);
            if (textLoc != null) {
                graphics.setColor(ZulrahOverlay.NEXT_TEXT_COLOR);
                graphics.drawString(next ? "Next" : "Stand", textLoc.getX(), textLoc.getY());
            }
        }
        else {
            ZulrahOverlay.log.debug("poly null");
        }
    }
    
    private void drawZulrahTileMinimap(final Graphics2D graphics, final LocalPoint startTile, final ZulrahPhase phase, final boolean next) {
        if (phase == null) {
            return;
        }
        final LocalPoint zulrahLocalTile = phase.getZulrahTile(startTile);
        final Point zulrahMinimapPoint = Perspective.localToMinimap(this.client, zulrahLocalTile);
        final Color color = phase.getColor();
        graphics.setColor(color);
        if (zulrahMinimapPoint != null) {
            graphics.fillOval(zulrahMinimapPoint.getX() - 2, zulrahMinimapPoint.getY() - 2, 4, 4);
        }
        graphics.setColor(ZulrahOverlay.TILE_BORDER_COLOR);
        graphics.setStroke(new BasicStroke(1.0f));
        if (zulrahMinimapPoint != null) {
            graphics.drawOval(zulrahMinimapPoint.getX() - 2, zulrahMinimapPoint.getY() - 2, 4, 4);
        }
        if (next) {
            graphics.setColor(ZulrahOverlay.NEXT_TEXT_COLOR);
            final FontMetrics fm = graphics.getFontMetrics();
            if (zulrahMinimapPoint != null) {
                graphics.drawString("Next", zulrahMinimapPoint.getX() - fm.stringWidth("Next") / 2, zulrahMinimapPoint.getY() - 2);
            }
        }
    }
    
    private Polygon getCanvasTileNorthPoly(final Client client, final LocalPoint localLocation) {
        final int plane = client.getPlane();
        final int halfTile = 64;
        final Point p1 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() - halfTile), plane);
        final Point p2 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() + halfTile), plane);
        final Point p3 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() + halfTile), plane);
        if (p1 == null || p2 == null || p3 == null) {
            return null;
        }
        final Polygon poly = new Polygon();
        poly.addPoint(p1.getX(), p1.getY());
        poly.addPoint(p2.getX(), p2.getY());
        poly.addPoint(p3.getX(), p3.getY());
        return poly;
    }
    
    private Polygon getCanvasTileSouthPoly(final Client client, final LocalPoint localLocation) {
        final int plane = client.getPlane();
        final int halfTile = 64;
        final Point p1 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() - halfTile, localLocation.getY() - halfTile), plane);
        final Point p2 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() + halfTile), plane);
        final Point p3 = Perspective.localToCanvas(client, new LocalPoint(localLocation.getX() + halfTile, localLocation.getY() - halfTile), plane);
        if (p1 == null || p2 == null || p3 == null) {
            return null;
        }
        final Polygon poly = new Polygon();
        poly.addPoint(p1.getX(), p1.getY());
        poly.addPoint(p2.getX(), p2.getY());
        poly.addPoint(p3.getX(), p3.getY());
        return poly;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)ZulrahOverlay.class);
        TILE_BORDER_COLOR = new Color(0, 0, 0, 100);
        NEXT_TEXT_COLOR = new Color(255, 255, 255, 100);
    }
}
