package api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import api.utils.Timer;
import net.runelite.client.plugins.discord.DiscordPlugin;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Variables {
	public static boolean USE_PRAYER = false;
	public static List<String> LOOTABLES = new ArrayList<String>();
	public static long COUNT = 0;
	public static Timer START_TIME = new Timer();
	public static String STATUS;
	public static boolean STOP;
	public static boolean STARTED;

	public static String NEXT_MESSAGE = null;
	public static String DISCORD_KEY, DISCORD_CHANNEL;
	public static Class<?> CURRENT_SCRIPT = null;
	public static DiscordPlugin DISCORD_API;
	public static boolean FORCE_BANK = false;

	public static CopyOnWriteArrayList<Prayers> scheduledPrayers = new CopyOnWriteArrayList<Prayers>();

	public static void reset() {
		STOP = false;
		STARTED = false;
		COUNT = 0;
		START_TIME = new Timer();
		STATUS = "Booting up";
		FORCE_BANK = false;
		NEXT_MESSAGE = null;
		USE_PRAYER = false;
	}
}
