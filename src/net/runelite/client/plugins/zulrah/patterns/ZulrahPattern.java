// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah.patterns;

import net.runelite.api.Prayer;
import net.runelite.client.plugins.zulrah.phase.StandLocation;
import net.runelite.client.plugins.zulrah.phase.ZulrahType;
import net.runelite.client.plugins.zulrah.phase.ZulrahLocation;
import java.util.ArrayList;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import java.util.List;

public abstract class ZulrahPattern
{
    private final List<ZulrahPhase> pattern;
    
    public ZulrahPattern() {
        this.pattern = new ArrayList<ZulrahPhase>();
    }
    
    final void add(final ZulrahLocation loc, final ZulrahType type, final StandLocation standLocation, final Prayer prayer) {
        this.add(loc, type, standLocation, false, prayer);
    }
    
    final void addJad(final ZulrahLocation loc, final ZulrahType type, final StandLocation standLocation, final Prayer prayer) {
        this.add(loc, type, standLocation, true, prayer);
    }
    
    private void add(final ZulrahLocation loc, final ZulrahType type, final StandLocation standLocation, final boolean jad, final Prayer prayer) {
        this.pattern.add(new ZulrahPhase(loc, type, jad, standLocation, prayer));
    }
    
    public ZulrahPhase get(final int index) {
        if (index >= this.pattern.size()) {
            return null;
        }
        return this.pattern.get(index);
    }
    
    public boolean stageMatches(final int index, final ZulrahPhase instance) {
        final ZulrahPhase patternInstance = this.get(index);
        return patternInstance != null && patternInstance.equals(instance);
    }
    
    public boolean canReset(final int index) {
        return index >= this.pattern.size();
    }
}
