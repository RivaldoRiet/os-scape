package net.runelite.client.plugins.packet.testing;


import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Packet")
public interface PacketConfig extends Config {

@ConfigItem(
		position = 0,
		keyName = "Thieve from stalls",
		name = "Thieve from stalls?",
		description = "Stalls or else heros"
)
default boolean stalls()
{
	return true;
}

}