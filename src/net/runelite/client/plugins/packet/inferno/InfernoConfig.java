package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoNamingDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoPrayerDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoSafespotDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoZukShieldDisplayMode;


@ConfigGroup("inferno")
public interface InfernoConfig extends Config {
	@ConfigItem(name = "Prayer", description = "Configuration options forPprayer", position = 0, keyName = "Prayerwarning")
	default boolean prayerwarning() {
		return false;
	}

	@ConfigItem(name = "Safespots", description = "Configuration options for Safespots", position = 1, keyName = "Safespotswarning")
	default boolean safespotswarning() {
		return false;
	}

	@ConfigItem(name = "Waves", description = "Configuration options for Waves", position = 2, keyName = "Waveswarning")
	default boolean waveswarning() {
		return false;
	}

	@ConfigItem(name = "Extrawarning", description = "Configuration options for Extras", position = 3, keyName = "Extrawarning")
	default boolean extrawarning() {
		return false;
	}

	@ConfigItem(name = "Nibblers", description = "Configuration options for Nibblers", position = 4, keyName = "Nibblerswarning")
	default boolean nibblerswarning() {
		return false;
	}

	@ConfigItem(name = "Bats", description = "Configuration options for Bats", position = 5, keyName = "Batswarning")
	default boolean batswarning() {
		return false;
	}

	@ConfigItem(name = "Blobs", description = "Configuration options for Blobs", position = 6, keyName = "Blobswarning")
	default boolean blobswarning() {
		return false;
	}

	@ConfigItem(name = "Meleers", description = "Configuration options for Meleers", position = 7, keyName = "Meleerswarning")
	default boolean meleerswarning() {
		return false;
	}

	@ConfigItem(name = "Rangers", description = "Configuration options for Rangers", position = 8, keyName = "Rangerswarning")
	default boolean rangerswarning() {
		return false;
	}

	@ConfigItem(name = "Magers", description = "Configuration options for Magers", position = 9, keyName = "Magerswarning")
	default boolean magerswarning() {
		return false;
	}

	@ConfigItem(name = "Jad", description = "Configuration options for Jad", position = 10, keyName = "Jadwarning")
	default boolean jadwarning() {
		return false;
	}

	@ConfigItem(name = "Jad Healers", description = "Configuration options for Jad Healers", position = 11, keyName = "JadHealerswarning")
	default boolean jadHealerswarning() {
		return false;
	}

	@ConfigItem(name = "Zuk", description = "Configuration options for  Zuk", position = 12, keyName = "Zukwarning")
	default boolean zukwarning() {
		return false;
	}

	@ConfigItem(name = "Zuk Healers", description = "Configuration options for Zuk Healers", position = 13, keyName = "ZukHealerswarning")
	default boolean zukHealerswarning() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerDisplayMode", name = "Prayer Display Mode", description = "Display prayer indicator in the prayer tab or in the bottom right corner of the screen", warning = "Prayerwarning")
	default InfernoPrayerDisplayMode prayerDisplayMode() {
		return InfernoPrayerDisplayMode.BOTH;
	}

	@ConfigItem(position = 1, keyName = "indicateWhenPrayingCorrectly", name = "Indicate When Praying Correctly", description = "Indicate the correct prayer, even if you are already praying that prayer", warning = "Prayerwarning")
	default boolean indicateWhenPrayingCorrectly() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "descendingBoxes", name = "Descending Boxes", description = "Draws timing boxes above the prayer icons, as if you were playing Piano Tiles", warning = "Prayerwarning")
	default boolean descendingBoxes() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateNonPriorityDescendingBoxes", name = "Indicate Non-Priority Boxes", description = "Render descending boxes for prayers that are not the priority prayer for that tick", warning = "Prayerwarning")
	default boolean indicateNonPriorityDescendingBoxes() {
		return true;
	}

	@ConfigItem(position = 4, keyName = "alwaysShowPrayerHelper", name = "Always Show Prayer Helper", description = "Render prayer helper at all time, even when other inventory tabs are open.", warning = "Prayerwarning")
	default boolean alwaysShowPrayerHelper() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "safespotDisplayMode", name = "Tile Safespots", description = "Indicate safespots on the ground: safespot (white), pray melee (red), pray range (green), pray magic (blue) and combinations of those", warning = "Safespotswarning")
	default InfernoSafespotDisplayMode safespotDisplayMode() {
		return InfernoSafespotDisplayMode.AREA;
	}

	@ConfigItem(position = 5, keyName = "safespotsCheckSize", name = "Tile Safespots Check Size", description = "The size of the area around the player that should be checked for safespots (SIZE x SIZE area)", warning = "Safespotswarning")
	default int safespotsCheckSize() {
		return 6;
	}

	@ConfigItem(position = 6, keyName = "indicateNonSafespotted", name = "Non-safespotted NPC's Overlay", description = "Red overlay for NPC's that can attack you", warning = "Safespotswarning")
	default boolean indicateNonSafespotted() {
		return false;
	}

	@ConfigItem(position = 7, keyName = "indicateTemporarySafespotted", name = "Temporary safespotted NPC's Overlay", description = "Orange overlay for NPC's that have to move to attack you", warning = "Safespotswarning")
	default boolean indicateTemporarySafespotted() {
		return false;
	}

	@ConfigItem(position = 8, keyName = "indicateSafespotted", name = "Safespotted NPC's Overlay", description = "Green overlay for NPC's that are safespotted (can't attack you)", warning = "Safespotswarning")
	default boolean indicateSafespotted() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "waveDisplay", name = "Wave Display", description = "Shows monsters that will spawn on the selected wave(s).", warning = "Waveswarning")
	default InfernoWaveDisplayMode waveDisplay() {
		return InfernoWaveDisplayMode.BOTH;
	}

	@ConfigItem(position = 1, keyName = "npcNaming", name = "NPC Naming", description = "Simple (ex: Bat) or Complex (ex: Jal-MejRah) NPC naming", warning = "Waveswarning")
	default InfernoNamingDisplayMode npcNaming() {
		return InfernoNamingDisplayMode.SIMPLE;
	}

	@ConfigItem(position = 2, keyName = "npcLevels", name = "NPC Levels", description = "Show the combat level of the NPC next to their name", warning = "Waveswarning")
	default boolean npcLevels() {
		return false;
	}

	@ConfigItem(position = 3, keyName = "getWaveOverlayHeaderColor", name = "Wave Header", description = "Color for Wave Header", warning = "Waveswarning")
	default Color getWaveOverlayHeaderColor() {
		return Color.ORANGE;
	}

	@ConfigItem(position = 4, keyName = "getWaveTextColor", name = "Wave Text Color", description = "Color for Wave Texts", warning = "Waveswarning")
	default Color getWaveTextColor() {
		return Color.WHITE;
	}

	@ConfigItem(position = 0, keyName = "indicateObstacles", name = "Obstacles", description = "Indicate obstacles that NPC's cannot pass through", warning = "Extrawarning")
	default boolean indicateObstacles() {
		return false;
	}

	@ConfigItem(position = 1, keyName = "spawnTimerInfobox", name = "Spawn Timer Infobox", description = "Display an Infobox that times spawn sets during Zuk fight.", warning = "Extrawarning")
	default boolean spawnTimerInfobox() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "indicateNibblers", name = "Indicate Nibblers", description = "Indicate's nibblers that are alive", warning = "Nibblerswarning")
	default boolean indicateNibblers() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "hideJalNibDeath", name = "Hide On Death", description = "Hide Nibblers on death animation", warning = "Nibblerswarning")
	default boolean hideNibblerDeath() {
		return false;
	}

	@ConfigItem(position = 2, keyName = "indicateCentralNibbler", name = "Indicate Central Nibbler", description = "Indicate the most central nibbler. If multiple nibblers will freeze the same amount of other nibblers, the nibbler closest to the player's location is chosen.", warning = "Nibblerswarning")
	default boolean indicateCentralNibbler() {
		return true;
	}

	@ConfigItem(position = 0, keyName = "prayerBat", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Batswarning")
	default boolean prayerBat() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcBat", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Batswarning")
	default boolean ticksOnNpcBat() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "safespotsBat", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Batswarning")
	default boolean safespotsBat() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateNpcPositionBat", name = "Indicate Main Tile", description = "Indicate the main tile for multi-tile NPC's. This tile is used for and pathfinding.", warning = "Batswarning")
	default boolean indicateNpcPositionBat() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "hideJalMejRahDeath", name = "Hide On Death", description = "Hide Jal-MejRah on death animation", warning = "Batswarning")
	default boolean hideBatDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerBlob", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Blobswarning")
	default boolean prayerBlob() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "indicateBlobDetectionTick", name = "Indicate Blob Dection Tick", description = "Show a prayer indicator (default: magic) for the tick on which the blob will detect prayer", warning = "Blobswarning")
	default boolean indicateBlobDetectionTick() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "ticksOnNpcBlob", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Blobswarning")
	default boolean ticksOnNpcBlob() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "safespotsBlob", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Blobswarning")
	default boolean safespotsBlob() {
		return true;
	}

	@ConfigItem(position = 4, keyName = "indicateNpcPositionBlob", name = "Indicate Main Tile", description = "Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", warning = "Blobswarning")
	default boolean indicateNpcPositionBlob() {
		return false;
	}

	@ConfigItem(position = 5, keyName = "hideJalAkDeath", name = "Hide Blob On Death", description = "Hide Jal-Ak on death animation", warning = "Blobswarning")
	default boolean hideBlobDeath() {
		return false;
	}

	@ConfigItem(position = 6, keyName = "hideJalAkRekXilDeath", name = "Hide Small Range Blob On Death", description = "Hide Jal-AkRek-Xil on death animation", warning = "Blobswarning")
	default boolean hideBlobSmallRangedDeath() {
		return false;
	}

	@ConfigItem(position = 7, keyName = "hideJalAkRekMejDeath", name = "Hide Small Magic Blob On Death", description = "Hide Jal-AkRek-Mej on death animation", warning = "Blobswarning")
	default boolean hideBlobSmallMagicDeath() {
		return false;
	}

	@ConfigItem(position = 8, keyName = "hideJalAkRekKetDeath", name = "Hide Small Melee Blob On Death", description = "Hide Jal-AkRek-Ket on death animation", warning = "Blobswarning")
	default boolean hideBlobSmallMeleeDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerMeleer", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Meleerswarning")
	default boolean prayerMeleer() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcMeleer", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Meleerswarning")
	default boolean ticksOnNpcMeleer() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "safespotsMeleer", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Meleerswarning")
	default boolean safespotsMeleer() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateNpcPositionMeleer", name = "Indicate Main Tile", description = "Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", warning = "Meleerswarning")
	default boolean indicateNpcPositionMeleer() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "hideJalImKotDeath", name = "Hide On Death", description = "Hide Jal-ImKot on death animation", warning = "Meleerswarning")
	default boolean hideMeleerDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerRanger", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Rangerswarning")
	default boolean prayerRanger() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcRanger", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Rangerswarning")
	default boolean ticksOnNpcRanger() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "safespotsRanger", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Rangerswarning")
	default boolean safespotsRanger() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateNpcPositionRanger", name = "Indicate Main Tile", description = "Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", warning = "Rangerswarning")
	default boolean indicateNpcPositionRanger() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "hideJalXilDeath", name = "Hide On Death", description = "Hide Jal-Xil on death animation", warning = "Rangerswarning")
	default boolean hideRangerDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerMage", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Magerswarning")
	default boolean prayerMage() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcMage", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Magerswarning")
	default boolean ticksOnNpcMage() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "safespotsMage", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Magerswarning")
	default boolean safespotsMage() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateNpcPositionMage", name = "Indicate Main Tile", description = "Indicate the main tile for multi-tile NPC's. This tile is used for pathfinding.", warning = "Magerswarning")
	default boolean indicateNpcPositionMage() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "hideJalZekDeath", name = "Hide On Death", description = "Hide Jal-Zek on death animation", warning = "Magerswarning")
	default boolean hideMagerDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerHealersJad", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "JadHealerswarning")
	default boolean prayerHealerJad() {
		return false;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcHealersJad", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "JadHealerswarning")
	default boolean ticksOnNpcHealerJad() {
		return false;
	}

	@ConfigItem(position = 2, keyName = "safespotsHealersJad", name = "Safespots", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "JadHealerswarning")
	default boolean safespotsHealerJad() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "indicateActiveHealersJad", name = "Indicate Active Healers", description = "Indicate healers that are still healing Jad", warning = "JadHealerswarning")
	default boolean indicateActiveHealerJad() {
		return true;
	}

	@ConfigItem(position = 4, keyName = "hideYtHurKotDeath", name = "Hide On Death", description = "Hide Yt-HurKot on death animation", warning = "JadHealerswarning")
	default boolean hideHealerJadDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "prayerJad", name = "Prayer Helper", description = "Indicate the correct prayer when this NPC attacks", warning = "Jadwarning")
	default boolean prayerJad() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "ticksOnNpcJad", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Jadwarning")
	default boolean ticksOnNpcJad() {
		return true;
	}

	@ConfigItem(position = 2, keyName = "safespotsJad", name = "Safespots (Melee Range Only)", description = "Enable or disable safespot calculation for this specific NPC. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Jadwarning")
	default boolean safespotsJad() {
		return true;
	}

	@ConfigItem(position = 3, keyName = "hideJalTokJadDeath", name = "Hide On Death", description = "Hide JalTok-Jad on death animation", warning = "Jadwarning")
	default boolean hideJadDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "indicateActiveHealersZuk", name = "Indicate Active Healers (UNTESTED)", description = "Indicate healers that are still healing Zuk", warning = "ZukHealerswarning")
	default boolean indicateActiveHealerZuk() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "hideJalMejJakDeath", name = "Hide On Death", description = "Hide Jal-MejJak on death animation", warning = "ZukHealerswarning")
	default boolean hideHealerZukDeath() {
		return false;
	}

	@ConfigItem(position = 0, keyName = "ticksOnNpcZuk", name = "Ticks on NPC", description = "Draws the amount of ticks before an NPC is going to attack on the NPC", warning = "Zukwarning")
	default boolean ticksOnNpcZuk() {
		return true;
	}

	@ConfigItem(position = 1, keyName = "safespotsZukShieldBeforeHealers", name = "Safespots (Before Healers)", description = "Indicate the zuk shield safespots. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect. Shield must go back and forth at least 1 time before the predict option will work.", warning = "Zukwarning")
	default InfernoZukShieldDisplayMode safespotsZukShieldBeforeHealers() {
		return InfernoZukShieldDisplayMode.PREDICT;
	}

	@ConfigItem(position = 2, keyName = "safespotsZukShieldAfterHealers", name = "Safespots (After Healers)", description = "Indicate the zuk shield safespots. 'Tile Safespots' in the 'Safespots' category needs to be turned on for this to take effect.", warning = "Zukwarning")
	default InfernoZukShieldDisplayMode safespotsZukShieldAfterHealers() {
		return InfernoZukShieldDisplayMode.LIVE;
	}

	@ConfigItem(position = 3, keyName = "hideTzKalZukDeath", name = "Hide On Death", description = "Hide TzKal-Zuk on death animation", warning = "Zukwarning")
	default boolean hideZukDeath() {
		return false;
	}

	@ConfigItem(position = 4, keyName = "ticksOnNpcZukShield", name = "Ticks on Zuk Shield", description = "Draws the amount of ticks before Zuk attacks on the floating shield", warning = "Zukwarning")
	default boolean ticksOnNpcZukShield() {
		return false;
	}

	public enum FontStyle {
		BOLD("Bold", 1),
		ITALIC("Italic", 2),
		PLAIN("Plain", 0);

		FontStyle(String name, int font) {
			this.name = name;
			this.font = font;
		}

		private String name;

		private int font;

		public String getName() {
			return this.name;
		}

		public int getFont() {
			return this.font;
		}

		public String toString() {
			return getName();
		}
	}
}
