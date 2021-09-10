// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.phase;

import org.slf4j.LoggerFactory;
import net.runelite.api.coords.LocalPoint;
import org.slf4j.Logger;

public enum ZulrahLocation
{
    NORTH, 
    SOUTH, 
    EAST, 
    WEST;
    
    private static final Logger log;
    
    public static ZulrahLocation valueOf(final LocalPoint start, final LocalPoint current) {
        final int dx = start.getX() - current.getX();
        final int dy = start.getY() - current.getY();
        if (dx == -1280 && dy == 256) {
            return ZulrahLocation.EAST;
        }
        if (dx == 1280 && dy == 256) {
            return ZulrahLocation.WEST;
        }
        if (dx == 0 && dy == 1408) {
            return ZulrahLocation.SOUTH;
        }
        if (dx == 0 && dy == 0) {
            return ZulrahLocation.NORTH;
        }
        ZulrahLocation.log.debug("Unknown Zulrah location dx: {}, dy: {}", (Object)dx, (Object)dy);
        return null;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)ZulrahLocation.class);
    }
}
