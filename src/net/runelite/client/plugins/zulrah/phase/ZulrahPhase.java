// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.phase;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import java.awt.Color;

public class ZulrahPhase
{
    private static final Color RANGE_COLOR;
    private static final Color MAGIC_COLOR;
    private static final Color MELEE_COLOR;
    private static final Color JAD_COLOR;
    private final ZulrahLocation zulrahLocation;
    private final ZulrahType type;
    private final boolean jad;
    private final StandLocation standLocation;
    private final Prayer prayer;
    
    public ZulrahPhase(final ZulrahLocation zulrahLocation, final ZulrahType type, final boolean jad, final StandLocation standLocation, final Prayer prayer) {
        this.zulrahLocation = zulrahLocation;
        this.type = type;
        this.jad = jad;
        this.standLocation = standLocation;
        this.prayer = prayer;
    }
    
    public static ZulrahPhase valueOf(final NPC zulrah, final LocalPoint start) {
        final ZulrahLocation zulrahLocation = ZulrahLocation.valueOf(start, zulrah.getLocalLocation());
        final ZulrahType zulrahType = ZulrahType.valueOf(zulrah.getId());
        if (zulrahLocation == null || zulrahType == null) {
            return null;
        }
        final StandLocation standLocation = (zulrahType == ZulrahType.MAGIC) ? StandLocation.PILLAR_WEST_OUTSIDE : StandLocation.TOP_EAST;
        final Prayer prayer = (zulrahType == ZulrahType.MAGIC) ? Prayer.PROTECT_FROM_MAGIC : null;
        return new ZulrahPhase(zulrahLocation, zulrahType, false, standLocation, prayer);
    }
    
    @Override
    public String toString() {
        return invokedynamic(makeConcatWithConstants:(Lnet/runelite/client/plugins/zulrah/phase/ZulrahLocation;Lnet/runelite/client/plugins/zulrah/phase/ZulrahType;ZLnet/runelite/client/plugins/zulrah/phase/StandLocation;Lnet/runelite/api/Prayer;)Ljava/lang/String;, this.zulrahLocation, this.type, this.jad, this.standLocation, this.prayer);
    }
    
    public LocalPoint getZulrahTile(final LocalPoint startTile) {
        switch (this.zulrahLocation) {
            case SOUTH: {
                return new LocalPoint(startTile.getX(), startTile.getY() - 1408);
            }
            case EAST: {
                return new LocalPoint(startTile.getX() + 1280, startTile.getY() - 256);
            }
            case WEST: {
                return new LocalPoint(startTile.getX() - 1280, startTile.getY() - 256);
            }
            default: {
                return startTile;
            }
        }
    }
    
    public LocalPoint getStandTile(final LocalPoint startTile) {
        switch (this.standLocation) {
            case WEST: {
                return new LocalPoint(startTile.getX() - 640, startTile.getY());
            }
            case EAST: {
                return new LocalPoint(startTile.getX() + 640, startTile.getY() - 256);
            }
            case SOUTH: {
                return new LocalPoint(startTile.getX(), startTile.getY() - 768);
            }
            case SOUTH_WEST: {
                return new LocalPoint(startTile.getX() - 512, startTile.getY() - 512);
            }
            case SOUTH_EAST: {
                return new LocalPoint(startTile.getX() + 256, startTile.getY() - 768);
            }
            case TOP_EAST: {
                return new LocalPoint(startTile.getX() + 768, startTile.getY() + 256);
            }
            case TOP_WEST: {
                return new LocalPoint(startTile.getX() - 512, startTile.getY() + 384);
            }
            case PILLAR_WEST_INSIDE: {
                return new LocalPoint(startTile.getX() - 512, startTile.getY() - 384);
            }
            case PILLAR_WEST_OUTSIDE: {
                return new LocalPoint(startTile.getX() - 640, startTile.getY() - 384);
            }
            case PILLAR_EAST_INSIDE: {
                return new LocalPoint(startTile.getX() + 512, startTile.getY() - 384);
            }
            case PILLAR_EAST_OUTSIDE: {
                return new LocalPoint(startTile.getX() + 512, startTile.getY() - 512);
            }
            default: {
                return startTile;
            }
        }
    }
    
    public ZulrahType getType() {
        return this.type;
    }
    
    public boolean isJad() {
        return this.jad;
    }
    
    public StandLocation getStandLocation() {
        return this.standLocation;
    }
    
    public Prayer getPrayer() {
        return this.prayer;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final ZulrahPhase other = (ZulrahPhase)obj;
        return this.jad == other.jad && this.zulrahLocation == other.zulrahLocation && this.type == other.type;
    }
    
    public Color getColor() {
        if (this.jad) {
            return ZulrahPhase.JAD_COLOR;
        }
        switch (this.type) {
            case RANGE: {
                return ZulrahPhase.RANGE_COLOR;
            }
            case MAGIC: {
                return ZulrahPhase.MAGIC_COLOR;
            }
            case MELEE: {
                return ZulrahPhase.MELEE_COLOR;
            }
            default: {
                return ZulrahPhase.RANGE_COLOR;
            }
        }
    }
    
    static {
        RANGE_COLOR = new Color(150, 255, 0, 100);
        MAGIC_COLOR = new Color(20, 170, 200, 100);
        MELEE_COLOR = new Color(180, 50, 20, 100);
        JAD_COLOR = new Color(255, 115, 0, 100);
    }
}
