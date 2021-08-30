/*
 * Copyright (c) 2019, gazivodag <https://github.com/gazivodag>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.runelite.client.plugins.packet.TribridStyle;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("TribridStyle")
public interface TribridStyleConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "show available pvp targets",
			name = "show available pvp targets",
			description = "blue colored pvp targets"
	)
	default boolean showAvailable()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "autoSwitchPrayer",
			name = "autoSwitchPrayer",
			description = "Chooses wether to change prayer according to your opponents weapon"
	)
	default boolean autoSwitchPrayer()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Mage / Range Only",
			name = "Mage and range",
			description = "Use this when you don't want to tribrid"
	)
	default boolean mageRangeOnly()
	{
		return false;
	}

	@ConfigItem(
			position = 0,
			keyName = "switch dscim / crossbow",
			name = "switch dscim",
			description = "when no overheads for dps reasons you may want to use a crossbow instead of a dscim"
	)
	default boolean switchDscim()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "Use surge otherwise flames of zammy",
			name = "Use surge otherwise flames of zammy",
			description = "Use surge otherwise flames of zammy"
	)
	default boolean useSurge()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Auto DD walking",
			name = "Use DD walking",
			description = "leave open to use DD"
	)
	default boolean useDD()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Use autoeat",
			name = "Use autoeat",
			description = "Use auto eat on gmaul or ags spec when detected"
	)
	default boolean useAutoeat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "Opposite style",
			name = "Opposite style",
			description = "Opposite style activation key",
			position = 0
	)
	default Keybind oppStyle()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Special attack key",
			name = "Special attack key",
			description = "Special attack key",
			position = 0
	)
	default Keybind speckey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Switch key",
			name = "Switch key",
			description = "Switch key",
			position = 0
	)
	default Keybind switcher()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Barrage | Mage switch",
			name = "Barrage",
			description = "Barrage",
			position = 0
	)
	default Keybind barrage()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Entangle",
			name = "Entangle",
			description = "Entangle",
			position = 0
	)
	default Keybind entangle()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Teleblock",
			name = "Teleblock",
			description = "Teleblock",
			position = 0
	)
	default Keybind teleblock()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Surge",
			name = "Surge",
			description = "Surge",
			position = 0
	)
	default Keybind surge() {
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "DD Walk",
			name = "DD walk",
			description = "Walk on current enemy",
			position = 0
	)
	default Keybind DDWalk()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Single eat key",
			name = "Single eat key",
			description = "Single eat key",
			position = 0
	)
	default Keybind singeEatKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Double eat key",
			name = "Double eat key",
			description = "Double eat key",
			position = 0
	)
	default Keybind doubleEatKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Triple eat key",
			name = "Triple eat key",
			description = "Triple eat key",
			position = 0
	)
	default Keybind tripleEatKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
		position = 0,
		keyName = "attackerPlayerColor",
		name = "Attacker color",
		description = "This is the color that will be used to highlight attackers."
	)
	default Color attackerPlayerColor()
	{
		return new Color(0xFF0006);
	}

	@ConfigItem(
		position = 1,
		keyName = "potentialPlayerColor",
		name = "Potential Attacker color",
		description = "This is the color that will be used to highlight potential attackers."
	)
	default Color potentialPlayerColor()
	{
		return new Color(0xFFFF00);
	}

	@ConfigItem(
		position = 2,
		keyName = "attackerTargetTimeout",
		name = "Attacker Timeout",
		description = "Seconds until attacker is no longer highlighted."
	)
	default int attackerTargetTimeout()
	{
		return 10;
	}

	@ConfigItem(
		position = 3,
		keyName = "potentialTargetTimeout",
		name = "Potential Attacker Timeout",
		description = "Seconds until potential attacker is no longer highlighted."
	)
	default int potentialTargetTimeout()
	{
		return 10;
	}

	@ConfigItem(
		position = 4,
		keyName = "newSpawnTimeout",
		name = "New Player Timeout",
		description = "Seconds until logged in/spawned player is no longer highlighted."
	)
	default int newSpawnTimeout()
	{
		return 5;
	}

	@ConfigItem(
		position = 5,
		keyName = "ignoreFriends",
		name = "Ignore Friends",
		description = "This lets you decide whether you want friends to be highlighted by this plugin."
	)
	default boolean ignoreFriends()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "ignoreClanMates",
		name = "Ignore Clan Mates",
		description = "This lets you decide whether you want clan mates to be highlighted by this plugin."
	)
	default boolean ignoreClanMates()
	{
		return true;
	}

	@ConfigItem(
		position = 7,
		keyName = "markNewPlayer",
		name = "Mark new player as potential attacker",
		description = "Marks someone that logged in or teleported as a potential attacker for your safety\nDO NOT RUN THIS IN WORLD 1-2 GRAND EXCHANGE!"
	)
	default boolean markNewPlayer()
	{
		return false;
	}

	@ConfigItem(
		position = 8,
		keyName = "drawTargetPrayAgainst",
		name = "Draw what to pray on attacker",
		description = "Tells you what to pray from what weapon the attacker is holding"
	)
	default boolean drawTargetPrayAgainst()
	{
		return true;
	}

	@ConfigItem(
		position = 9,
		keyName = "drawPotentialTargetPrayAgainst",
		name = "Draw what to pray on potential attacker",
		description = "Tells you what to pray from what weapon the potential attacker is holding"
	)
	default boolean drawPotentialTargetPrayAgainst()
	{
		return true;
	}

	@ConfigItem(
		position = 10,
		keyName = "drawTargetPrayAgainstPrayerTab",
		name = "Draw what to pray from prayer tab",
		description = "Tells you what to pray from what weapon the attacker is holding from the prayer tab"
	)
	default boolean drawTargetPrayAgainstPrayerTab()
	{
		return false;
	}

	@ConfigItem(
		position = 11,
		keyName = "drawTargetsName",
		name = "Draw name on attacker",
		description = "Configures whether or not the attacker\'s name should be shown"
	)
	default boolean drawTargetsName()
	{
		return true;
	}

	@ConfigItem(
		position = 12,
		keyName = "drawPotentialTargetsName",
		name = "Draw name on potential attacker",
		description = "Configures whether or not the potential attacker\'s name should be shown"
	)
	default boolean drawPotentialTargetsName()
	{
		return true;
	}

	@ConfigItem(
		position = 13,
		keyName = "drawTargetHighlight",
		name = "Draw highlight around attacker",
		description = "Configures whether or not the attacker should be highlighted"
	)
	default boolean drawTargetHighlight()
	{
		return true;
	}

	@ConfigItem(
		position = 14,
		keyName = "drawPotentialTargetHighlight",
		name = "Draw highlight around potential attacker",
		description = "Configures whether or not the potential attacker should be highlighted"
	)
	default boolean drawPotentialTargetHighlight()
	{
		return true;
	}

	@ConfigItem(
		position = 15,
		keyName = "drawTargetTile",
		name = "Draw tile under attacker",
		description = "Configures whether or not the attacker\'s tile be highlighted"
	)
	default boolean drawTargetTile()
	{
		return false;
	}

	@ConfigItem(
		position = 16,
		keyName = "drawPotentialTargetTile",
		name = "Draw tile under potential attacker",
		description = "Configures whether or not the potential attacker\'s tile be highlighted"
	)
	default boolean drawPotentialTargetTile()
	{
		return false;
	}

	@ConfigItem(
		position = 17,
		keyName = "drawUnknownWeapons",
		name = "Draw unknown weapons",
		description = "Configures whether or not the unknown weapons should be shown when a player equips one"
	)
	default boolean drawUnknownWeapons()
	{
		return false;
	}
}
