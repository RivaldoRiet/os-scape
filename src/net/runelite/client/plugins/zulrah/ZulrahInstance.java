// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

import net.runelite.api.Prayer;
import net.runelite.client.plugins.zulrah.phase.ZulrahLocation;
import net.runelite.client.plugins.zulrah.phase.StandLocation;
import net.runelite.client.plugins.zulrah.phase.ZulrahType;
import javax.annotation.Nullable;
import net.runelite.api.NPC;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPattern;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;

public class ZulrahInstance
{
    private static final ZulrahPhase NO_PATTERN_MAGIC_PHASE;
    private static final ZulrahPhase NO_PATTERN_RANGE_PHASE;
    private static final ZulrahPhase PATTERN_A_OR_B_RANGE_PHASE;
    private final LocalPoint startLocation;
    private ZulrahPattern pattern;
    private int stage;
    private ZulrahPhase phase;
    
    ZulrahInstance(final NPC zulrah) {
        this.startLocation = zulrah.getLocalLocation();
    }
    
    public LocalPoint getStartLocation() {
        return this.startLocation;
    }
    
    public ZulrahPattern getPattern() {
        return this.pattern;
    }
    
    public void setPattern(final ZulrahPattern pattern) {
        this.pattern = pattern;
    }
    
    int getStage() {
        return this.stage;
    }
    
    void nextStage() {
        ++this.stage;
    }
    
    public void reset() {
        this.pattern = null;
        this.stage = 0;
    }
    
    @Nullable
    public ZulrahPhase getPhase() {
        ZulrahPhase patternPhase = null;
        if (this.pattern != null) {
            patternPhase = this.pattern.get(this.stage);
        }
        return (patternPhase != null) ? patternPhase : this.phase;
    }
    
    public void setPhase(final ZulrahPhase phase) {
        this.phase = phase;
    }
    
    @Nullable
    public ZulrahPhase getNextPhase() {
        if (this.pattern != null) {
            return this.pattern.get(this.stage + 1);
        }
        if (this.phase != null) {
            final ZulrahType type = this.phase.getType();
            final StandLocation standLocation = this.phase.getStandLocation();
            if (type == ZulrahType.MELEE) {
                return (standLocation == StandLocation.TOP_EAST) ? ZulrahInstance.NO_PATTERN_MAGIC_PHASE : ZulrahInstance.NO_PATTERN_RANGE_PHASE;
            }
            if (type == ZulrahType.MAGIC) {
                return (standLocation == StandLocation.TOP_EAST) ? ZulrahInstance.NO_PATTERN_RANGE_PHASE : ZulrahInstance.PATTERN_A_OR_B_RANGE_PHASE;
            }
        }
        return null;
    }
    
    static {
        NO_PATTERN_MAGIC_PHASE = new ZulrahPhase(ZulrahLocation.NORTH, ZulrahType.MAGIC, false, StandLocation.PILLAR_WEST_OUTSIDE, Prayer.PROTECT_FROM_MAGIC);
        NO_PATTERN_RANGE_PHASE = new ZulrahPhase(ZulrahLocation.NORTH, ZulrahType.RANGE, false, StandLocation.TOP_EAST, Prayer.PROTECT_FROM_MISSILES);
        PATTERN_A_OR_B_RANGE_PHASE = new ZulrahPhase(ZulrahLocation.NORTH, ZulrahType.RANGE, false, StandLocation.PILLAR_WEST_OUTSIDE, Prayer.PROTECT_FROM_MISSILES);
    }
}
