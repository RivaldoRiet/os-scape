package net.runelite.client.plugins.packet.inferno;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;

import com.google.inject.Provides;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.NPCManager;
import net.runelite.client.input.KeyManager;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoNamingDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoPrayerDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoSafespotDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoWaveDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoZukShieldDisplayMode;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(name = "Inferno", enabledByDefault = false, description = "Inferno helper", tags = { "combat", "overlay", "pve",
		"pvm" })
public class InfernoPlugin extends Plugin {

private static final int INFERNO_REGION = 9043;

@Inject
private Client client;

@Inject
private OverlayManager overlayManager;

@Inject
private InfoBoxManager infoBoxManager;

@Inject
private ItemManager itemManager;

@Inject
private NPCManager npcManager;

@Inject
private InfernoOverlay infernoOverlay;

@Inject
private InfernoWaveOverlay waveOverlay;

@Inject
private InfernoInfoBoxOverlay jadOverlay;

@Inject
private InfernoOverlay prayerOverlay;

@Inject
private InfernoTrueFalseOverlay falseOverlay;

@Inject
private InfernoConfig config;

@Inject
private KeyManager keyManager;

@Inject
private KeyRemappingListener keyListener;

private InfernoConfig.FontStyle fontStyle = InfernoConfig.FontStyle.BOLD;

InfernoConfig.FontStyle getFontStyle() {
	return this.fontStyle;
}

private int textSize = 32;

int getTextSize() {
	return this.textSize;
}

private WorldPoint lastLocation = new WorldPoint(0, 0, 0);

private int currentWaveNumber;

int getCurrentWaveNumber() {
	return this.currentWaveNumber;
}

private final List<InfernoNPC> infernoNpcs = new ArrayList<>();

List<InfernoNPC> getInfernoNpcs() {
	return this.infernoNpcs;
}

private final Map<Integer, Map<InfernoNPC.Attack, Integer>> upcomingAttacks = new HashMap<>();

Map<Integer, Map<InfernoNPC.Attack, Integer>> getUpcomingAttacks() {
	return this.upcomingAttacks;
}

private InfernoNPC.Attack closestAttack = null;

InfernoNPC.Attack getClosestAttack() {
	return this.closestAttack;
}

private final List<WorldPoint> obstacles = new ArrayList<>();

List<WorldPoint> getObstacles() {
	return this.obstacles;
}

private boolean finalPhase = false;

boolean isFinalPhase() {
	return this.finalPhase;
}

private NPC zukShield = null;

NPC getZukShield() {
	return this.zukShield;
}

private NPC zuk = null;

private WorldPoint zukShieldLastPosition = null;

private WorldPoint zukShieldBase = null;

private int zukShieldCornerTicks = -2;

private int zukShieldNegativeXCoord = -1;

private int zukShieldPositiveXCoord = -1;

private int zukShieldLastNonZeroDelta = 0;

private int zukShieldLastDelta = 0;

private int zukShieldTicksLeftInCorner = -1;

private ThreadPoolExecutor executorService;

private BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1);

private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

private List<String> adfadsfasdgafdadsfasfd = new ArrayList<>();

private String hwid = "";

private String licenseCheck;

private InfernoNPC centralNibbler = null;

InfernoNPC getCentralNibbler() {
	return this.centralNibbler;
}

private final Map<WorldPoint, Integer> safeSpotMap = new HashMap<>();

Map<WorldPoint, Integer> getSafeSpotMap() {
	return this.safeSpotMap;
}

private final Map<Integer, List<WorldPoint>> safeSpotAreas = new HashMap<>();

private String maul;

private long lastTick;

private InfernoSpawnTimerInfobox spawnTimerInfoBox;

Map<Integer, List<WorldPoint>> getSafeSpotAreas() {
	return this.safeSpotAreas;
}

public String getMaul() {
	return this.maul;
}

long getLastTick() {
	return this.lastTick;
}

@Provides
InfernoConfig provideConfig(ConfigManager configManager) {
	return (InfernoConfig) configManager.getConfig(InfernoConfig.class);
}

Method action;
Class<?> _class;

public void invoke(String option, String target, int id, int menuAction, int action, int widgetId) {
	MenuOptionClicked menu = new MenuOptionClicked();
	menu.setActionParam(action);
	menu.setMenuOption(option);
	menu.setMenuTarget(target);
	menu.setMenuAction(MenuAction.of(menuAction));
	menu.setId(id);
	menu.setWidgetId(widgetId);
	invoke(menu);
}

public void invoke(MenuOptionClicked option) {
	try {
		if (_class == null) _class = Class.forName("class67");
		
		if (action == null) {
			action = _class.getDeclaredMethod("method1808", int.class, int.class, int.class, int.class, java.lang.String.class,
					java.lang.String.class, int.class, int.class, int.class);
			action.setAccessible(true);
		}

		action.invoke(_class, option.getActionParam(), option.getWidgetId(), option.getMenuAction().getId(),
				option.getId(), option.getMenuOption(), option.getMenuTarget(), 640, 432, 1191285249);
	} catch (Exception e1) {
		System.out.println(option);
		e1.printStackTrace();
	}
}


protected void startUp() throws Exception {
	this.keyManager.registerKeyListener(this.keyListener);
	this.licenseCheck = "valid";
	this.waveOverlay.setDisplayMode(this.config.waveDisplay());
	this.waveOverlay.setWaveHeaderColor(this.config.getWaveOverlayHeaderColor());
	this.waveOverlay.setWaveTextColor(this.config.getWaveTextColor());
	this.overlayManager.add(this.falseOverlay);
	if (isInInferno()) {
		this.overlayManager.add(this.infernoOverlay);
		if (this.config.waveDisplay() != InfernoWaveDisplayMode.NONE) this.overlayManager.add(this.waveOverlay);
		this.overlayManager.add(this.jadOverlay);
		this.overlayManager.add(this.prayerOverlay);
		hideNpcDeaths();
	}
}

protected void shutDown() {
	this.overlayManager.remove(this.infernoOverlay);
	this.overlayManager.remove(this.waveOverlay);
	this.overlayManager.remove(this.jadOverlay);
	this.overlayManager.remove(this.prayerOverlay);
	this.infoBoxManager.removeInfoBox(this.spawnTimerInfoBox);
	this.currentWaveNumber = -1;
	showNpcDeaths();
}

@Subscribe
private void onConfigChanged(ConfigChanged event) {
	if (!"inferno".equals(event.getGroup())) return;
	hideNpcDeaths();
	showNpcDeaths();
	if (event.getKey().endsWith("color")) {
		this.waveOverlay.setWaveHeaderColor(this.config.getWaveOverlayHeaderColor());
		this.waveOverlay.setWaveTextColor(this.config.getWaveTextColor());
	} else if ("waveDisplay".equals(event.getKey())) {
		this.overlayManager.remove(this.waveOverlay);
		this.waveOverlay.setDisplayMode(this.config.waveDisplay());
		if (isInInferno() && this.config.waveDisplay() != InfernoWaveDisplayMode.NONE) this.overlayManager.add(this.waveOverlay);
	}
	if (event.getKey().equals("mirrorMode") && isInInferno()) {
		this.infernoOverlay.determineLayer();
		this.jadOverlay.determineLayer();
		this.prayerOverlay.determineLayer();
		this.overlayManager.remove(this.infernoOverlay);
		this.overlayManager.remove(this.jadOverlay);
		this.overlayManager.remove(this.prayerOverlay);
		this.overlayManager.add(this.infernoOverlay);
		this.overlayManager.add(this.jadOverlay);
		this.overlayManager.add(this.prayerOverlay);
		if (this.config.waveDisplay() != InfernoWaveDisplayMode.NONE) {
			this.waveOverlay.determineLayer();
			this.overlayManager.remove(this.waveOverlay);
			this.overlayManager.add(this.waveOverlay);
		}
	}
}

@Subscribe
private void onGameTick(GameTick event) {
	if (!isInInferno()) return;
	this.lastTick = System.currentTimeMillis();
	this.upcomingAttacks.clear();
	calculateUpcomingAttacks();
	this.closestAttack = null;
	calculateClosestAttack();
	this.safeSpotMap.clear();
	calculateSafespots();
	this.safeSpotAreas.clear();
	calculateSafespotAreas();
	this.obstacles.clear();
	calculateObstacles();
	this.centralNibbler = null;
	calculateCentralNibbler();
	calculateSpawnTimerInfobox();
	this.maul = this.keyListener.getTogglePrayer().toString();
	checkPrayer2();
	// checkPrayer2();
}

@Subscribe
private void onNpcSpawned(NpcSpawned event) {
	if (!isInInferno()) return;
	int npcId = event.getNpc().getId();
	if (npcId == 7707) {
		this.zukShield = event.getNpc();
		return;
	}
	InfernoNPC.Type infernoNPCType = InfernoNPC.Type.typeFromId(npcId);
	if (infernoNPCType == null) return;
	switch (infernoNPCType) {
		case MAGE:
			this.infernoNpcs.add(new InfernoNPC(event.getNpc()));
			return;
		case RANGER:
			if (this.zuk != null && this.spawnTimerInfoBox != null) {
				this.spawnTimerInfoBox.reset();
				this.spawnTimerInfoBox.run();
			}
			break;
		case MELEE:
			this.finalPhase = false;
			this.zukShieldCornerTicks = -2;
			this.zukShieldLastPosition = null;
			this.zukShieldBase = null;
			if (this.config.spawnTimerInfobox()) {
				this.zuk = event.getNpc();
				if (this.spawnTimerInfoBox != null) this.infoBoxManager.removeInfoBox(this.spawnTimerInfoBox);
				this.spawnTimerInfoBox = new InfernoSpawnTimerInfobox((BufferedImage) this.itemManager.getImage(22319), this);
				this.infoBoxManager.addInfoBox(this.spawnTimerInfoBox);
			}
			break;
		default:
			this.finalPhase = true;
			for (InfernoNPC infernoNPC : this.infernoNpcs) {
				if (infernoNPC.getType() == InfernoNPC.Type.ZUK)
					infernoNPC.setTicksTillNextAttack(infernoNPC.getTicksTillNextAttack() - 3);
			}
			break;
	}
	this.infernoNpcs.add(0, new InfernoNPC(event.getNpc()));
}

@Subscribe
private void onNpcDespawned(NpcDespawned event) {
	if (!isInInferno()) return;
	int npcId = event.getNpc().getId();
	switch (npcId) {
		case 7707:
			this.zukShield = null;
			return;
		case 7706:
			this.zuk = null;
			if (this.spawnTimerInfoBox != null) this.infoBoxManager.removeInfoBox(this.spawnTimerInfoBox);
			this.spawnTimerInfoBox = null;
			break;
	}
	this.infernoNpcs.removeIf(infernoNPC -> (infernoNPC.getNpc() == event.getNpc()));
}

@Subscribe
private void onAnimationChanged(AnimationChanged event) {
	if (!isInInferno()) return;
	if (event.getActor() instanceof NPC) {
		NPC npc = (NPC) event.getActor();
		if (ArrayUtils.contains(InfernoNPC.Type.NIBBLER.getNpcIds(), npc.getId()) && npc.getAnimation() == 7576)
			this.infernoNpcs.removeIf(infernoNPC -> (infernoNPC.getNpc() == npc));
	}
}

@Subscribe
private void onGameStateChanged(GameStateChanged event) {
	if (event.getGameState() != GameState.LOGGED_IN) return;
	if (!isInInferno()) {
		this.infernoNpcs.clear();
		this.currentWaveNumber = -1;
		this.overlayManager.remove(this.infernoOverlay);
		this.overlayManager.remove(this.waveOverlay);
		this.overlayManager.remove(this.jadOverlay);
		this.overlayManager.remove(this.prayerOverlay);
		this.zukShield = null;
		this.zuk = null;
		if (this.spawnTimerInfoBox != null) this.infoBoxManager.removeInfoBox(this.spawnTimerInfoBox);
		this.spawnTimerInfoBox = null;
	} else if (this.currentWaveNumber == -1) {
		this.infernoNpcs.clear();
		this.currentWaveNumber = 1;
		this.overlayManager.add(this.infernoOverlay);
		this.overlayManager.add(this.jadOverlay);
		this.overlayManager.add(this.prayerOverlay);
		if (this.config.waveDisplay() != InfernoWaveDisplayMode.NONE) this.overlayManager.add(this.waveOverlay);
	}
}

@Subscribe
private void onChatMessage(ChatMessage event) {
	if (!isInInferno() || event.getType() != ChatMessageType.GAMEMESSAGE) return;
	String message = event.getMessage();
	if (event.getMessage().contains("Wave:")) {
		System.out.println(event.getMessage());
		message = message.substring(message.indexOf(": ") + 2);
		this.currentWaveNumber = Integer.parseInt(event.getMessage().replace("<col=FF0000>Wave: ", "").replace("</col>", ""));
	}
}

private boolean isInInferno() {
	return ArrayUtils.contains(this.client.getMapRegions(), 9043);
}

int getNextWaveNumber() {
	return (this.currentWaveNumber == -1 || this.currentWaveNumber == 69) ? -1 : (this.currentWaveNumber + 1);
}

private void calculateUpcomingAttacks() {
	// System.out.println("Amount of npcs: " + infernoNpcs);
	for (InfernoNPC infernoNPC : this.infernoNpcs) {
		infernoNPC.gameTick(this.client, this.lastLocation, this.finalPhase);
		if (infernoNPC.getType() == InfernoNPC.Type.ZUK && this.zukShieldCornerTicks == -1) {
			infernoNPC.updateNextAttack(InfernoNPC.Attack.UNKNOWN, 12);
			this.zukShieldCornerTicks = 0;
		}
		if (infernoNPC.getTicksTillNextAttack() > 0 && isPrayerHelper(infernoNPC)
				&& (infernoNPC.getNextAttack() != InfernoNPC.Attack.UNKNOWN || (this.config.indicateBlobDetectionTick()
						&& infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() >= 4))) {
			this.upcomingAttacks.computeIfAbsent(Integer.valueOf(infernoNPC.getTicksTillNextAttack()), k -> new HashMap<>());
			if (this.config.indicateBlobDetectionTick() && infernoNPC.getType() == InfernoNPC.Type.BLOB
					&& infernoNPC.getTicksTillNextAttack() >= 4) {
				this.upcomingAttacks.computeIfAbsent(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3),
						k -> new HashMap<>());
				this.upcomingAttacks.computeIfAbsent(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 4),
						k -> new HashMap<>());
				if (((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
						.containsKey(InfernoNPC.Attack.MAGIC)) {
					if (((Integer) ((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
							.get(InfernoNPC.Attack.MAGIC)).intValue() > InfernoNPC.Type.BLOB.getPriority())
						((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks
								.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).put(InfernoNPC.Attack.MAGIC,
										Integer.valueOf(InfernoNPC.Type.BLOB.getPriority()));
					continue;
				}
				if (((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
						.containsKey(InfernoNPC.Attack.RANGED)) {
					if (((Integer) ((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
							.get(InfernoNPC.Attack.RANGED)).intValue() > InfernoNPC.Type.BLOB.getPriority())
						((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks
								.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).put(InfernoNPC.Attack.RANGED,
										Integer.valueOf(InfernoNPC.Type.BLOB.getPriority()));
					continue;
				}
				if (((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack())))
						.containsKey(InfernoNPC.Attack.MAGIC)
						|| ((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 4)))
								.containsKey(InfernoNPC.Attack.MAGIC)) {
					if (!((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
							.containsKey(InfernoNPC.Attack.RANGED)
							|| ((Integer) ((Map) this.upcomingAttacks
									.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).get(InfernoNPC.Attack.RANGED))
											.intValue() > InfernoNPC.Type.BLOB.getPriority())
						((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks
								.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).put(InfernoNPC.Attack.RANGED,
										Integer.valueOf(InfernoNPC.Type.BLOB.getPriority()));
					continue;
				}
				if (((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack())))
						.containsKey(InfernoNPC.Attack.RANGED)
						|| ((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 4)))
								.containsKey(InfernoNPC.Attack.RANGED)) {
					if (!((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3)))
							.containsKey(InfernoNPC.Attack.MAGIC)
							|| ((Integer) ((Map) this.upcomingAttacks
									.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).get(InfernoNPC.Attack.MAGIC))
											.intValue() > InfernoNPC.Type.BLOB.getPriority())
						((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks
								.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).put(InfernoNPC.Attack.MAGIC,
										Integer.valueOf(InfernoNPC.Type.BLOB.getPriority()));
					continue;
				}
				((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks
						.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack() - 3))).put(InfernoNPC.Attack.MAGIC,
								Integer.valueOf(InfernoNPC.Type.BLOB.getPriority()));
				continue;
			}
			InfernoNPC.Attack attack = infernoNPC.getNextAttack();
			int priority = infernoNPC.getType().getPriority();
			if (!((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack()))).containsKey(attack)
					|| ((Integer) ((Map) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack())))
							.get(attack)).intValue() > priority)
				((Map<InfernoNPC.Attack, Integer>) this.upcomingAttacks.get(Integer.valueOf(infernoNPC.getTicksTillNextAttack())))
						.put(attack, Integer.valueOf(priority));
		}
	}
}

private void calculateClosestAttack() {
	if (this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.PRAYER_TAB
			|| this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.BOTH) {
		int closestTick = 999;
		int closestPriority = 999;
		for (Integer tick : this.upcomingAttacks.keySet()) {
			Map<InfernoNPC.Attack, Integer> attackPriority = this.upcomingAttacks.get(tick);
			for (InfernoNPC.Attack currentAttack : attackPriority.keySet()) {
				int currentPriority = ((Integer) attackPriority.get(currentAttack)).intValue();
				if (tick.intValue() < closestTick || (tick.intValue() == closestTick && currentPriority < closestPriority)) {
					this.closestAttack = currentAttack;
					closestPriority = currentPriority;
					closestTick = tick.intValue();
				}
			}
		}
	}
}

private void calculateSafespots() {
	if (this.currentWaveNumber < 69) {
		if (this.config.safespotDisplayMode() != InfernoSafespotDisplayMode.OFF) {
			int checkSize = (int) Math.floor(this.config.safespotsCheckSize() / 2.0D);
			for (int x = -checkSize; x <= checkSize; x++) {
				for (int y = -checkSize; y <= checkSize; y++) {
					WorldPoint checkLoc = this.client.getLocalPlayer().getWorldLocation().dx(x).dy(y);
					if (!this.obstacles.contains(checkLoc)) for (InfernoNPC infernoNPC : this.infernoNpcs) {
						if (!isNormalSafespots(infernoNPC)) continue;
						if (!this.safeSpotMap.containsKey(checkLoc)) this.safeSpotMap.put(checkLoc, Integer.valueOf(0));
						if (infernoNPC.canAttack(this.client, checkLoc)
								|| infernoNPC.canMoveToAttack(this.client, checkLoc, this.obstacles)) {
							if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MELEE)
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 0) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(1));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 2) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(4));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 3) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(5));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 6) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(7));
								}
							if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.MAGIC
									|| (infernoNPC.getType() == InfernoNPC.Type.BLOB
											&& ((Integer) this.safeSpotMap.get(checkLoc)).intValue() != 2
											&& ((Integer) this.safeSpotMap.get(checkLoc)).intValue() != 4))
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 0) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(3));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 1) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(5));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 2) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(6));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 5) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(7));
								}
							if (infernoNPC.getType().getDefaultAttack() == InfernoNPC.Attack.RANGED
									|| (infernoNPC.getType() == InfernoNPC.Type.BLOB
											&& ((Integer) this.safeSpotMap.get(checkLoc)).intValue() != 3
											&& ((Integer) this.safeSpotMap.get(checkLoc)).intValue() != 5))
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 0) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(2));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 1) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(4));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 3) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(6));
								} else if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 4) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(7));
								}
							if (infernoNPC.getType() == InfernoNPC.Type.JAD
									&& infernoNPC.getNpc().getWorldArea().isInMeleeDistance(checkLoc)) {
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 0) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(1));
									continue;
								}
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 2) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(4));
									continue;
								}
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 3) {
									this.safeSpotMap.put(checkLoc, Integer.valueOf(5));
									continue;
								}
								if (((Integer) this.safeSpotMap.get(checkLoc)).intValue() == 6)
									this.safeSpotMap.put(checkLoc, Integer.valueOf(7));
							}
						}
					}
				}
			}
		}
	} else if (this.currentWaveNumber == 69 && this.zukShield != null) {
		WorldPoint zukShieldCurrentPosition = this.zukShield.getWorldLocation();
		if (this.zukShieldLastPosition != null && this.zukShieldLastPosition.getX() != zukShieldCurrentPosition.getX()
				&& this.zukShieldCornerTicks == -2) {
			this.zukShieldBase = this.zukShieldLastPosition;
			this.zukShieldCornerTicks = -1;
		}
		if (this.zukShieldLastPosition != null) {
			int zukShieldDelta = zukShieldCurrentPosition.getX() - this.zukShieldLastPosition.getX();
			if (zukShieldDelta != 0) this.zukShieldLastNonZeroDelta = zukShieldDelta;
			if (this.zukShieldLastDelta == 0 && zukShieldDelta != 0) this.zukShieldTicksLeftInCorner = 4;
			if (zukShieldDelta == 0) {
				if (this.zukShieldLastNonZeroDelta > 0) {
					this.zukShieldPositiveXCoord = zukShieldCurrentPosition.getX();
				} else if (this.zukShieldLastNonZeroDelta < 0) {
					this.zukShieldNegativeXCoord = zukShieldCurrentPosition.getX();
				}
				if (this.zukShieldTicksLeftInCorner > 0) this.zukShieldTicksLeftInCorner--;
			}
			this.zukShieldLastDelta = zukShieldDelta;
		}
		this.zukShieldLastPosition = zukShieldCurrentPosition;
		if (this.config.safespotDisplayMode() != InfernoSafespotDisplayMode.OFF) {
			if ((this.finalPhase && this.config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.LIVE)
					|| (!this.finalPhase && this.config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.LIVE))
				drawZukSafespot(this.zukShield.getWorldLocation().getX(), this.zukShield.getWorldLocation().getY(), 0);
			if ((this.finalPhase && this.config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.LIVEPLUSPREDICT)
					|| (!this.finalPhase
							&& this.config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.LIVEPLUSPREDICT)) {
				drawZukSafespot(this.zukShield.getWorldLocation().getX(), this.zukShield.getWorldLocation().getY(), 0);
				drawZukPredictedSafespot();
			} else if ((this.finalPhase && this.config.safespotsZukShieldAfterHealers() == InfernoZukShieldDisplayMode.PREDICT)
					|| (!this.finalPhase
							&& this.config.safespotsZukShieldBeforeHealers() == InfernoZukShieldDisplayMode.PREDICT)) {
								drawZukPredictedSafespot();
							}
		}
	}
}

private void drawZukPredictedSafespot() {
	WorldPoint zukShieldCurrentPosition = this.zukShield.getWorldLocation();
	if (this.zukShieldPositiveXCoord != -1 && this.zukShieldNegativeXCoord != -1) {
		int nextShieldXCoord = zukShieldCurrentPosition.getX();
		for (InfernoNPC infernoNPC : this.infernoNpcs) {
			if (infernoNPC.getType() == InfernoNPC.Type.ZUK) {
				int ticksTilZukAttack = infernoNPC.getTicksTillNextAttack();
				if (ticksTilZukAttack < 1) return;
				if (this.zukShieldLastNonZeroDelta > 0) {
					nextShieldXCoord += ticksTilZukAttack;
					if (nextShieldXCoord > this.zukShieldPositiveXCoord) {
						nextShieldXCoord -= this.zukShieldTicksLeftInCorner;
						if (nextShieldXCoord <= this.zukShieldPositiveXCoord) {
							nextShieldXCoord = this.zukShieldPositiveXCoord;
							continue;
						}
						nextShieldXCoord = this.zukShieldPositiveXCoord - nextShieldXCoord + this.zukShieldPositiveXCoord;
					}
					continue;
				}
				nextShieldXCoord -= ticksTilZukAttack;
				if (nextShieldXCoord < this.zukShieldNegativeXCoord) {
					nextShieldXCoord += this.zukShieldTicksLeftInCorner;
					if (nextShieldXCoord >= this.zukShieldNegativeXCoord) {
						nextShieldXCoord = this.zukShieldNegativeXCoord;
						continue;
					}
					nextShieldXCoord = this.zukShieldNegativeXCoord - nextShieldXCoord + this.zukShieldNegativeXCoord;
				}
			}
		}
		drawZukSafespot(nextShieldXCoord, this.zukShield.getWorldLocation().getY(), 2);
	}
}

private void checkPrayer2() {
	for (Integer tick : this.upcomingAttacks.keySet()) {
		Map<InfernoNPC.Attack, Integer> attackPriority = this.upcomingAttacks.get(tick);
		int bestPriority = 999;
		InfernoNPC.Attack bestAttack = null;
		for (Map.Entry<InfernoNPC.Attack, Integer> attackEntry : attackPriority.entrySet()) {
			System.out.println("entry value: " + attackEntry.getValue().intValue());
			if (attackEntry.getValue().intValue() < bestPriority) {
				bestAttack = attackEntry.getKey();
				bestPriority = attackEntry.getValue().intValue();
			}
		}
		System.out.println(bestAttack);
		for (InfernoNPC.Attack currentAttack : attackPriority.keySet()) {
			if (tick.intValue() == 1 && currentAttack == bestAttack && this.client.getLocalPlayer() != null)
				if (this.client.getLocalPlayer().getOverheadIcon() == null) {
					System.out.println("curr attack");
					System.out.println(currentAttack);
					switch (currentAttack) {
						case MAGIC:
							clickPrayMage();
							break;
						case RANGED:
							clickPrayRange();
							break;
						case MELEE:
							clickPrayMelee();
							break;
					}
				} else if (!this.client.getLocalPlayer().getOverheadIcon().toString().equals(currentAttack.toString())) {
					switch (currentAttack) {
						case MAGIC:
							clickPrayMage();
							break;
						case RANGED:
							clickPrayRange();
							break;
						case MELEE:
							clickPrayMelee();
							break;
					}
				}
			if (this.client.getLocalPlayer() != null && !this.upcomingAttacks.containsKey(Integer.valueOf(1))
					&& this.client.getLocalPlayer().getOverheadIcon() != null)
				switch (this.client.getLocalPlayer().getOverheadIcon()) {
				case MAGIC:
				clickPrayMage();
				break;
				case RANGED:
				clickPrayRange();
				break;
				case MELEE:
				clickPrayMelee();
				break;
				case RANGE_MAGE:
				break;
				case REDEMPTION:
				break;
				case RETRIBUTION:
				break;
				case SMITE:
				break;
				default:
				break;
				}
		}
	}
}

private void switchTab() {
	// if (this.client.getVar(VarClientInt.INTERFACE_TAB) !=
	// InterfaceTab.INVENTORY.getId())
	pressTab(112);
}

private void switchTabPrayer() {
	// if (this.client.getVar(VarClientInt.INTERFACE_TAB) !=
	// InterfaceTab.PRAYER.getId())
	pressTab(114);
}

private void pressTab(int keyCode) {
	KeyEvent keyPressed = new KeyEvent(this.client.getCanvas(), 401, 0L, 0, keyCode);
	this.client.getCanvas().dispatchEvent(keyPressed);
	KeyEvent keyReleased = new KeyEvent(this.client.getCanvas(), 402, 0L, 0, keyCode);
	this.client.getCanvas().dispatchEvent(keyReleased);
}

private void drawZukSafespot(int xCoord, int yCoord, int colorSafeSpotId) {
	for (int x = xCoord - 1; x <= xCoord + 3; x++) {
		for (int y = yCoord - 4; y <= yCoord - 2; y++)
			this.safeSpotMap.put(new WorldPoint(x, y, this.client.getPlane()), Integer.valueOf(colorSafeSpotId));
	}
}

private void calculateSafespotAreas() {
	if (this.config.safespotDisplayMode() == InfernoSafespotDisplayMode.AREA)
		for (WorldPoint worldPoint : this.safeSpotMap.keySet()) {
			if (!this.safeSpotAreas.containsKey(this.safeSpotMap.get(worldPoint)))
				this.safeSpotAreas.put(this.safeSpotMap.get(worldPoint), new ArrayList<>());
			((List<WorldPoint>) this.safeSpotAreas.get(this.safeSpotMap.get(worldPoint))).add(worldPoint);
		}
	this.lastLocation = this.client.getLocalPlayer().getWorldLocation();
}

private void calculateObstacles() {
	for (NPC npc : this.client.getNpcs())
		this.obstacles.addAll(npc.getWorldArea().toWorldPointList());
}

private void calculateCentralNibbler() {
	InfernoNPC bestNibbler = null;
	int bestAmountInArea = 0;
	int bestDistanceToPlayer = 999;
	for (InfernoNPC infernoNPC : this.infernoNpcs) {
		if (infernoNPC.getType() != InfernoNPC.Type.NIBBLER) continue;
		int amountInArea = 0;
		int distanceToPlayer = infernoNPC.getNpc().getWorldLocation().distanceTo(this.client.getLocalPlayer().getWorldLocation());
		for (InfernoNPC checkNpc : this.infernoNpcs) {
			if (checkNpc.getType() != InfernoNPC.Type.NIBBLER
					|| checkNpc.getNpc().getWorldArea().distanceTo(infernoNPC.getNpc().getWorldArea()) > 1)
				continue;
			amountInArea++;
		}
		if (amountInArea > bestAmountInArea || (amountInArea == bestAmountInArea && distanceToPlayer < bestDistanceToPlayer))
			bestNibbler = infernoNPC;
	}
	if (bestNibbler != null) this.centralNibbler = bestNibbler;
}

private void calculateSpawnTimerInfobox() {
	if (this.zuk == null || this.finalPhase || this.spawnTimerInfoBox == null) return;
	int pauseHp = 600;
	int resumeHp = 480;
	int hp = 600;
	if (hp <= 0) return;
	if (this.spawnTimerInfoBox.isRunning()) {
		if (hp >= 480 && hp < 600) this.spawnTimerInfoBox.pause();
	} else if (hp < 480) {
		this.spawnTimerInfoBox.run();
	}
}

private static int calculateNpcHp(int ratio, int health, int maxHp) {
	if (ratio < 0 || health <= 0 || maxHp == -1) return -1;
	int exactHealth = 0;
	if (ratio > 0) {
		int maxHealth, minHealth = 1;
		if (health > 1) {
			if (ratio > 1) minHealth = (maxHp * (ratio - 1) + health - 2) / (health - 1);
			maxHealth = (maxHp * ratio - 1) / (health - 1);
			if (maxHealth > maxHp) maxHealth = maxHp;
		} else {
			maxHealth = maxHp;
		}
		exactHealth = (minHealth + maxHealth + 1) / 2;
	}
	return exactHealth;
}

private boolean isPrayerHelper(InfernoNPC infernoNPC) {
	switch (infernoNPC.getType()) {
		case BAT:
			return this.config.prayerBat();
		case BLOB:
			return this.config.prayerBlob();
		case MELEE:
			return this.config.prayerMeleer();
		case RANGER:
			return this.config.prayerRanger();
		case MAGE:
			return this.config.prayerMage();
		case HEALER_JAD:
			return this.config.prayerHealerJad();
		case JAD:
			return this.config.prayerJad();

	}
	return true;
}

boolean isTicksOnNpc(InfernoNPC infernoNPC) {

	return true;
}

boolean isNormalSafespots(InfernoNPC infernoNPC) {

	switch (infernoNPC.getType()) {
		case BAT:
			return this.config.safespotsBat();
		case BLOB:
			return this.config.safespotsBlob();
		case MELEE:
			return this.config.safespotsMeleer();
		case RANGER:
			return this.config.safespotsRanger();
		case MAGE:
			return this.config.safespotsMage();
		case HEALER_JAD:
			return this.config.safespotsHealerJad();
		case JAD:
			return this.config.safespotsJad();
	}

	return false;
}

boolean isIndicateNpcPosition(InfernoNPC infernoNPC) {

	switch (infernoNPC.getType()) {
		case BAT:
			return this.config.indicateNpcPositionBat();
		case BLOB:
			return this.config.indicateNpcPositionBlob();
		case MELEE:
			return this.config.indicateNpcPositionMeleer();
		case RANGER:
			return this.config.indicateNpcPositionRanger();
		case MAGE:
			return this.config.indicateNpcPositionMage();
	}

	return false;
}

private void hideNpcDeaths() {
	/*
	 * if (this.config.hideNibblerDeath())
	 * this.client.addHiddenNpcDeath("Jal-Nib"); if (this.config.hideBatDeath())
	 * this.client.addHiddenNpcDeath("Jal-MejRah"); if
	 * (this.config.hideBlobDeath()) this.client.addHiddenNpcDeath("Jal-Ak"); if
	 * (this.config.hideBlobSmallMeleeDeath())
	 * this.client.addHiddenNpcDeath("Jal-AkRek-Ket"); if
	 * (this.config.hideBlobSmallMagicDeath())
	 * this.client.addHiddenNpcDeath("Jal-AkRek-Mej"); if
	 * (this.config.hideBlobSmallRangedDeath())
	 * this.client.addHiddenNpcDeath("Jal-AkRek-Xil"); if
	 * (this.config.hideMeleerDeath())
	 * this.client.addHiddenNpcDeath("Jal-ImKot"); if
	 * (this.config.hideRangerDeath()) this.client.addHiddenNpcDeath("Jal-Xil");
	 * if (this.config.hideMagerDeath())
	 * this.client.addHiddenNpcDeath("Jal-Zek"); if
	 * (this.config.hideHealerJadDeath() && isInInferno())
	 * this.client.addHiddenNpcDeath("Yt-HurKot"); if
	 * (this.config.hideJadDeath()) this.client.addHiddenNpcDeath("JalTok-Jad");
	 * if (this.config.hideHealerZukDeath())
	 * this.client.addHiddenNpcDeath("Jal-MejJak"); if
	 * (this.config.hideZukDeath()) this.client.addHiddenNpcDeath("TzKal-Zuk");
	 */
}

private void showNpcDeaths() {
	/*
	 * if (!this.config.hideNibblerDeath())
	 * this.client.removeHiddenNpcDeath("Jal-Nib"); if
	 * (!this.config.hideBatDeath())
	 * this.client.removeHiddenNpcDeath("Jal-MejRah"); if
	 * (!this.config.hideBlobDeath())
	 * this.client.removeHiddenNpcDeath("Jal-Ak"); if
	 * (!this.config.hideBlobSmallMeleeDeath())
	 * this.client.removeHiddenNpcDeath("Jal-AkRek-Ket"); if
	 * (!this.config.hideBlobSmallMagicDeath())
	 * this.client.removeHiddenNpcDeath("Jal-AkRek-Mej"); if
	 * (!this.config.hideBlobSmallRangedDeath())
	 * this.client.removeHiddenNpcDeath("Jal-AkRek-Xil"); if
	 * (!this.config.hideMeleerDeath())
	 * this.client.removeHiddenNpcDeath("Jal-ImKot"); if
	 * (!this.config.hideRangerDeath())
	 * this.client.removeHiddenNpcDeath("Jal-Xil"); if
	 * (!this.config.hideMagerDeath())
	 * this.client.removeHiddenNpcDeath("Jal-Zek"); if
	 * (!this.config.hideHealerJadDeath())
	 * this.client.removeHiddenNpcDeath("Yt-HurKot"); if
	 * (!this.config.hideJadDeath())
	 * this.client.removeHiddenNpcDeath("JalTok-Jad"); if
	 * (!this.config.hideHealerZukDeath())
	 * this.client.removeHiddenNpcDeath("Jal-MejJak"); if
	 * (!this.config.hideZukDeath())
	 * this.client.removeHiddenNpcDeath("TzKal-Zuk");
	 */
}

private void clickPrayMage() {
	System.out.println("We are here");
	invoke("Activate", "<col=ff9040>Protect from Magic</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId() );
}

private void clickPrayRange() {
	System.out.println("We are here1");
	invoke("Activate", "<col=ff9040>Protect from Missiles</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId() );
}

private void clickPrayMelee() {
	System.out.println("We are here2");
	invoke("Activate", "<col=ff9040>Protect from Melee</col>", 1 , 57, -1, WidgetInfo.PRAYER_PROTECT_FROM_MELEE.getId());
}

private void delay(int minDelay, int maxDelay) {
	delay(getRandomIntBetweenRange(minDelay, maxDelay));
}

private int getRandomIntBetweenRange(int min, int max) {
	return (int) (Math.random() * (max - min + 1) + min);
}

private static void delay(int ms) {
	try {
		Thread.sleep(ms);
	} catch (Exception var2) {
		var2.printStackTrace();
	}
}

public InfernoPlugin() {
	this.executorService = new ThreadPoolExecutor(1, 3, 2L, TimeUnit.SECONDS, this.queue, new ThreadPoolExecutor.DiscardPolicy());
}

private void clickRectangle(Rectangle rectangle) {
	Point cp = getClickPoint(rectangle);
	if (cp.getX() >= -1) leftClick(cp.getX(), cp.getY());
}

private void leftClick(int x, int y) {
	MouseEvent mousePressed = new MouseEvent(this.client.getCanvas(), 501, System.currentTimeMillis(), 0, x, y, 1, false, 1);
	this.client.getCanvas().dispatchEvent(mousePressed);
	System.out.println("click" + x + y);
	MouseEvent mouseReleased = new MouseEvent(this.client.getCanvas(), 502, System.currentTimeMillis(), 0,
			this.client.getMouseCanvasPosition().getX(), this.client.getMouseCanvasPosition().getY(), 1, false, 1);
	this.client.getCanvas().dispatchEvent(mouseReleased);
}

private static double clamp(double val) {
	return Math.max(1.0D, Math.min(13000.0D, val));
}

private void moveMouse(int x, int y) {
	MouseEvent mouseMoved = new MouseEvent(this.client.getCanvas(), 503, 0L, 0, x, y, 0, false);
	this.client.getCanvas().dispatchEvent(mouseMoved);
}

private Point getClickPoint(Rectangle rect) {
	int rand = (Math.random() <= 0.5D) ? 1 : 2;
	int x = (int) (rect.getX() + (rand * 3) + rect.getWidth() / 2.0D);
	int y = (int) (rect.getY() + (rand * 3) + rect.getHeight() / 2.0D);
	return new Point(x, y);
}

public String licenseValid(String paramString1, String paramString2, String paramString3) throws Exception {
	return "valid";
}
}
