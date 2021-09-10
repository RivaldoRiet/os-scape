// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

public enum PrayerStyleMage
{
    Mystic_might("Mystic might"), 
    Augury("Augury");
    
    private final String name;
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public String getName() {
        return this.name;
    }
    
    private PrayerStyleMage(final String name) {
        this.name = name;
    }
}
