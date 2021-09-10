// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.phase;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public enum ZulrahType
{
    RANGE, 
    MAGIC, 
    MELEE;
    
    private static final Logger log;
    private static final int ZULRAH_RANGE = 2042;
    private static final int ZULRAH_MELEE = 2043;
    private static final int ZULRAH_MAGIC = 2044;
    
    public static ZulrahType valueOf(final int zulrahId) {
        switch (zulrahId) {
            case 2042: {
                return ZulrahType.RANGE;
            }
            case 2043: {
                return ZulrahType.MELEE;
            }
            case 2044: {
                return ZulrahType.MAGIC;
            }
            default: {
                ZulrahType.log.debug("Unknown Zulrah Id: {}", (Object)zulrahId);
                return null;
            }
        }
    }
    
    static {
        log = LoggerFactory.getLogger((Class)ZulrahType.class);
    }
}
