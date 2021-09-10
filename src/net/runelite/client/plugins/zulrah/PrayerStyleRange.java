// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

public enum PrayerStyleRange
{
    Eagle_eye("Eagle eye"), 
    Rigour("Rigour");
    
    private final String name;
    
    @Override
    public String toString() {
        return this.name;
    }
    
    public String getName() {
        return this.name;
    }
    
    private PrayerStyleRange(final String name) {
        this.name = name;
    }
}
