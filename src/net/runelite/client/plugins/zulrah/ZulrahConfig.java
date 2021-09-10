// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.Config;

@ConfigGroup("zulrah")
public interface ZulrahConfig extends Config
{
    @ConfigSection(name = "Api", description = "Configure your api  here", position = 0, keyName = "api")
    default boolean api() {
        return false;
    }
    
    @ConfigItem(name = "Api Key", keyName = "apiKey", description = "Enter your api key here", position = 0, section = "api")
    default String apiKey() {
        return "6VnH9N8Eq65hD0bc0FGsYa8sNDLnhGQm2PKjYShA";
    }
    
    @ConfigItem(name = "Api License key", keyName = "apiLicense", description = "Enter your api license key here", position = 0, section = "api")
    default String apiLicense() {
        return "QlM7G6WiXmStyO27j2QmAeLhVutsyO7h";
    }
    
    @ConfigItem(keyName = "AutoPray", name = "Enable auto praying", description = "auto pray for zulrah", position = 1)
    default boolean AutoPray() {
        return false;
    }
    
    @ConfigItem(keyName = "AutoOffensivePray", name = "Enable auto Offensive praying", description = "auto Offensive pray for gorillas", position = 2)
    default boolean AutoOffensivePray() {
        return false;
    }
    
    @ConfigItem(position = 3, keyName = "Clicks", name = "Simulate clicks", description = "Simulate clicks instead of packet.")
    default boolean Clicks() {
        return false;
    }
    
    @ConfigItem(keyName = "rangePrayer", name = "Range pray to click on", description = "Range pray to click on", position = 4)
    default PrayerStyleRange rangePrayer() {
        return PrayerStyleRange.Eagle_eye;
    }
    
    @ConfigItem(keyName = "magePrayer", name = "Mage pray to click on", description = "Mage pray to click on", position = 5)
    default PrayerStyleMage magePrayer() {
        return PrayerStyleMage.Mystic_might;
    }
    
    @ConfigItem(keyName = "prayerHotkey", name = "Your prayer tab hotkey", description = "Your prayer tab hotkey", position = 6)
    default int prayerHotkey() {
        return 112;
    }
    
    @ConfigItem(keyName = "invHotKey", name = "Your inventory hotkey", description = "Your inventory hotkey", position = 7)
    default int invHotKey() {
        return 113;
    }
    
    @ConfigItem(keyName = "stretchedMode", name = "If stretched mode is on", description = "If stretched mode is on.", position = 8)
    default boolean stretchedMode() {
        return false;
    }
}
