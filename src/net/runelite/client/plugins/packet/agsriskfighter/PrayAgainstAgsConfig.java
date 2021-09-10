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

package net.runelite.client.plugins.packet.agsriskfighter;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("prayagainstags")
public interface PrayAgainstAgsConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "damageNeeded",
			name = "Damage needed",
			description = "On this amount of damage it will turn a spec color"
	)
	default int damageNeeded()
	{
		return 20;
	}


	@ConfigItem(
			position = 0,
			keyName = "Eat on low hp",
			name = "Eat on low hp",
			description = "Put 0 to not eat on low hp"
	)
	default int eatOnLowHp()
	{
		return 20;
	}
	
	@ConfigItem(
			name = "Auto eat",
			keyName = "Auto eat",
			description = "Eats when it detects your enemy performing a gmaul / ags spec",
			position = 0
	)
	default boolean autoEat()
	{
		return false;
	}

	@ConfigItem(
			name = "Render lower combat",
			keyName = "Render lower combat",
			description = "Renders players who have a lower combat",
			position = 0
	)
	default boolean renderLowerCombat()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Use autospec maul",
			name = "Use autospec maul",
			description = "Use autospec maul"
	)
	default boolean useAutomaul()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Venge on enemy ags",
			name = "Venge on enemy ags",
			description = "Venge on enemy ags"
	)
	default boolean useVengeOnAgs()
	{
		return false;
	}

	@ConfigItem(
			keyName = "Dharok axe key",
			name = "Dharok axe key",
			description = "Dharok axe smack key",
			position = 0
	)
	default Keybind dharokAxeKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Single maul spec",
			name = "single Special attack",
			description = "Special attack activation key",
			position = 1
	)
	default Keybind singleMaulKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Double Maul Spec",
			name = "double Special attack",
			description = "Special attack activation key",
			position = 1
	)
	default Keybind doubleMaulKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Ags or Claws spec key",
			name = "Ags or Claws spec key",
			description = "Ags or Claws single spec key",
			position = 1
	)
	default Keybind agsAttackKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Smack attack key",
			name = "Smack attack key",
			description = "Switches between your godsword and whip / defender",
			position = 1
	)
	default Keybind smackAttackKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Ballista axe spec",
			name = "Ballista throwning axe Special attack",
			description = "Special attack activation key",
			position = 0
	)
	default Keybind ballistaSpecKey()
	{
		return Keybind.NOT_SET;
	}

	@ConfigItem(
			keyName = "Venge casting key",
			name = "Venge casting key",
			description = "Casts venge on keypress",
			position = 1
	)
	default Keybind vengeKey()
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

/*
	@ConfigItem(
			position = 0,
			keyName = "Eat on enemy spec",
			name = "Eat on enemy spec",
			description = "Eat on enemy special attack"
	)
	default boolean useAutoSpecEat()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Gmaul spec after venge",
			name = "Gmaul spec after venge",
			description = "When the script auto venges on enemy specs, it will gmaul smack aswell"
	)
	default boolean useAutoGmaulAfterVenge()
	{
		return true;
	}


	@ConfigItem(
			position = 0,
			keyName = "Triple eat on enemy ags",
			name = "Triple eat on enemy ags",
			description = "Triple eat on enemy ags"
	)
	default boolean tripleEatOnEnemyAgs()
	{
		return true;
	}

	@ConfigItem(
			position = 0,
			keyName = "Use auto eat",
			name = "Use auto eat",
			description = "will triple eat"
	)
	default boolean useAutoEat()
	{
		return true;
	}
	*/

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
