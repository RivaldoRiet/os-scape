// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

import net.runelite.api.ItemDefinition;
import net.runelite.api.kit.KitType;
import net.runelite.api.Player;
import net.runelite.api.Client;

enum WeaponType
{
    WEAPON_MELEE, 
    WEAPON_RANGED, 
    WEAPON_MAGIC, 
    WEAPON_UNKNOWN;
    
    private static final String[] meleeWeaponNames;
    private static final String[] rangedWeaponNames;
    private static final String[] magicWeaponNames;
    
    public static WeaponType checkWeaponOnPlayer(final Client client, final Player attacker) {
        final int itemId = attacker.getPlayerAppearance().getEquipmentId(KitType.WEAPON);
        final ItemDefinition itemComposition = client.getItemDefinition(itemId);
        final String weaponNameGivenLowerCase = itemComposition.getName().toLowerCase();
        if (itemId == -1) {
            return WeaponType.WEAPON_MELEE;
        }
        if (weaponNameGivenLowerCase.toLowerCase().contains("null")) {
            return WeaponType.WEAPON_MELEE;
        }
        for (final String meleeWeaponName : WeaponType.meleeWeaponNames) {
            if (weaponNameGivenLowerCase.contains(meleeWeaponName) && !weaponNameGivenLowerCase.contains("thrownaxe")) {
                return WeaponType.WEAPON_MELEE;
            }
        }
        for (final String rangedWeaponName : WeaponType.rangedWeaponNames) {
            if (weaponNameGivenLowerCase.contains(rangedWeaponName)) {
                return WeaponType.WEAPON_RANGED;
            }
        }
        for (final String magicWeaponName : WeaponType.magicWeaponNames) {
            if (weaponNameGivenLowerCase.contains(magicWeaponName)) {
                return WeaponType.WEAPON_MAGIC;
            }
        }
        return WeaponType.WEAPON_UNKNOWN;
    }
    
    public static WeaponType checkWeaponOnMe(final Client client) {
        final int itemId = client.getLocalPlayer().getPlayerAppearance().getEquipmentId(KitType.WEAPON);
        final ItemDefinition itemComposition = client.getItemDefinition(itemId);
        final String weaponNameGivenLowerCase = itemComposition.getName().toLowerCase();
        if (itemId == -1) {
            return WeaponType.WEAPON_MELEE;
        }
        if (weaponNameGivenLowerCase.toLowerCase().contains("null")) {
            return WeaponType.WEAPON_MELEE;
        }
        for (final String meleeWeaponName : WeaponType.meleeWeaponNames) {
            if (weaponNameGivenLowerCase.contains(meleeWeaponName) && !weaponNameGivenLowerCase.contains("thrownaxe")) {
                return WeaponType.WEAPON_MELEE;
            }
        }
        for (final String rangedWeaponName : WeaponType.rangedWeaponNames) {
            if (weaponNameGivenLowerCase.contains(rangedWeaponName)) {
                return WeaponType.WEAPON_RANGED;
            }
        }
        for (final String magicWeaponName : WeaponType.magicWeaponNames) {
            if (weaponNameGivenLowerCase.contains(magicWeaponName)) {
                return WeaponType.WEAPON_MAGIC;
            }
        }
        return WeaponType.WEAPON_UNKNOWN;
    }
    
    static {
        meleeWeaponNames = new String[] { "sword", "scimitar", "dagger", "spear", "mace", "axe", "whip", "tentacle", "-ket-", "-xil-", "warhammer", "halberd", "claws", "hasta", "scythe", "maul", "anchor", "sabre", "excalibur", "machete", "dragon hunter lance", "event rpg", "silverlight", "darklight", "arclight", "flail", "granite hammer", "rapier", "bulwark" };
        rangedWeaponNames = new String[] { "bow", "blowpipe", "xil-ul", "knife", "dart", "thrownaxe", "chinchompa", "ballista" };
        magicWeaponNames = new String[] { "staff", "trident", "wand", "dawnbringer" };
    }
}
