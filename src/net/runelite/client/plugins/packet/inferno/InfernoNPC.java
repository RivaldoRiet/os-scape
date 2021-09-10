package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

class InfernoNPC {
	private NPC npc;

	private Type type;

	private Attack nextAttack;

	private int ticksTillNextAttack;

	private int lastAnimation;

	private boolean lastCanAttack;

	private final Map<WorldPoint, Integer> safeSpotCache;

	NPC getNpc() {
		return this.npc;
	}

	Type getType() {
		return this.type;
	}

	Attack getNextAttack() {
		return this.nextAttack;
	}

	int getTicksTillNextAttack() {
		return this.ticksTillNextAttack;
	}

	void setTicksTillNextAttack(int ticksTillNextAttack) {
		this.ticksTillNextAttack = ticksTillNextAttack;
	}

	InfernoNPC(NPC npc) {
		this.npc = npc;
		this.type = Type.typeFromId(npc.getId());
		this.nextAttack = this.type.getDefaultAttack();
		this.ticksTillNextAttack = 0;
		this.lastAnimation = -1;
		this.lastCanAttack = false;
		this.safeSpotCache = new HashMap<>();
	}

	void updateNextAttack(Attack nextAttack, int ticksTillNextAttack) {
		this.nextAttack = nextAttack;
		this.ticksTillNextAttack = ticksTillNextAttack;
	}

	private void updateNextAttack(Attack nextAttack) {
		this.nextAttack = nextAttack;
	}

	boolean canAttack(Client client, WorldPoint target) {
		if (this.safeSpotCache.containsKey(target)) return (((Integer) this.safeSpotCache.get(target)).intValue() == 2);
		boolean hasLos = (new WorldArea(target, 1, 1)).hasLineOfSightTo(client, getNpc().getWorldArea());
		boolean hasRange = (getType().getDefaultAttack() == Attack.MELEE) ? getNpc().getWorldArea().isInMeleeDistance(target)
				: ((getNpc().getWorldArea().distanceTo(target) <= getType().getRange()));
		if (hasLos && hasRange) this.safeSpotCache.put(target, Integer.valueOf(2));
		return (hasLos && hasRange);
	}

	boolean canMoveToAttack(Client client, WorldPoint target, List<WorldPoint> obstacles) {
		if (this.safeSpotCache.containsKey(target)) return (((Integer) this.safeSpotCache.get(target)).intValue() == 1
				|| ((Integer) this.safeSpotCache.get(target)).intValue() == 2);
		List<WorldPoint> realObstacles = new ArrayList<>();
		for (WorldPoint obstacle : obstacles) {
			if (getNpc().getWorldArea().toWorldPointList().contains(obstacle)) continue;
			realObstacles.add(obstacle);
		}
		WorldArea targetArea = new WorldArea(target, 1, 1);
		WorldArea currentWorldArea = getNpc().getWorldArea();
		int steps = 0;
		while (true) {
			steps++;
			if (steps > 30) return false;
			WorldArea predictedWorldArea = currentWorldArea.calculateNextTravellingPoint(client, targetArea, true, x -> {
				for (WorldPoint obstacle : realObstacles) {
					if ((new WorldArea(x, 1, 1)).intersectsWith(new WorldArea(obstacle, 1, 1))) return false;
				}
				return true;
			});
			if (predictedWorldArea == null) {
				this.safeSpotCache.put(target, Integer.valueOf(1));
				return true;
			}
			if (predictedWorldArea == currentWorldArea) {
				this.safeSpotCache.put(target, Integer.valueOf(0));
				return false;
			}
			boolean hasLos = (new WorldArea(target, 1, 1)).hasLineOfSightTo(client, predictedWorldArea);
			boolean hasRange = (getType().getDefaultAttack() == Attack.MELEE) ? predictedWorldArea.isInMeleeDistance(target)
					: ((predictedWorldArea.distanceTo(target) <= getType().getRange()));
			if (hasLos && hasRange) {
				this.safeSpotCache.put(target, Integer.valueOf(1));
				return true;
			}
			currentWorldArea = predictedWorldArea;
		}
	}

	private boolean couldAttackPrevTick(Client client, WorldPoint lastPlayerLocation) {
		return (new WorldArea(lastPlayerLocation, 1, 1)).hasLineOfSightTo(client, getNpc().getWorldArea());
	}

	void gameTick(Client client, WorldPoint lastPlayerLocation, boolean finalPhase) {
		this.safeSpotCache.clear();
		if (this.ticksTillNextAttack > 0) this.ticksTillNextAttack--;
		if (getType() == Type.JAD && getNpc().getAnimation() != -1 && getNpc().getAnimation() != this.lastAnimation) {
			Attack currentAttack = Attack.attackFromId(getNpc().getAnimation());
			if (currentAttack != null && currentAttack != Attack.UNKNOWN)
				updateNextAttack(currentAttack, getType().getTicksAfterAnimation());
		}
		if (this.ticksTillNextAttack <= 0) switch (getType()) {
			case ZUK:
				if (getNpc().getAnimation() == 7566) {
					if (finalPhase) {
						updateNextAttack(getType().getDefaultAttack(), 7);
						break;
					}
					updateNextAttack(getType().getDefaultAttack(), 10);
				}
				break;
			case JAD:
				if (getNextAttack() != Attack.UNKNOWN) updateNextAttack(getType().getDefaultAttack(), 8);
				break;
			case BLOB:
				if (!this.lastCanAttack && couldAttackPrevTick(client, lastPlayerLocation)) {
					updateNextAttack(Attack.UNKNOWN, 3);
					break;
				}
				if (!this.lastCanAttack && canAttack(client, client.getLocalPlayer().getWorldLocation())) {
					updateNextAttack(Attack.UNKNOWN, 4);
					break;
				}
				if (getNpc().getAnimation() != -1)
					updateNextAttack(getType().getDefaultAttack(), getType().getTicksAfterAnimation());
				break;
			case BAT:
				if (canAttack(client, client.getLocalPlayer().getWorldLocation()) && getNpc().getAnimation() != 7577
						&& getNpc().getAnimation() != -1)
					updateNextAttack(getType().getDefaultAttack(), getType().getTicksAfterAnimation());
				break;
			case MELEE:
			case RANGER:
			case MAGE:
				if (getNpc().getAnimation() == 7597 || getNpc().getAnimation() == 7605 || getNpc().getAnimation() == 7604
						|| getNpc().getAnimation() == 7610 || getNpc().getAnimation() == 7612) {
							updateNextAttack(getType().getDefaultAttack(), getType().getTicksAfterAnimation());
							break;
						}
				if (getNpc().getAnimation() == 7600) {
					updateNextAttack(getType().getDefaultAttack(), 12);
					break;
				}
				if (getNpc().getAnimation() == 7611) updateNextAttack(getType().getDefaultAttack(), 8);
				break;
			default:
				if (getNpc().getAnimation() != -1)
					updateNextAttack(getType().getDefaultAttack(), getType().getTicksAfterAnimation());
				break;
		}
		if (getType() == Type.BLOB && getTicksTillNextAttack() == 3
				&& client.getLocalPlayer().getWorldLocation().distanceTo(getNpc().getWorldArea()) <= Type.BLOB.getRange()) {
			Attack nextBlobAttack = Attack.UNKNOWN;
			if (client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
				nextBlobAttack = Attack.MAGIC;
			} else if (client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
				nextBlobAttack = Attack.RANGED;
			}
			updateNextAttack(nextBlobAttack);
		}
		this.lastAnimation = getNpc().getAnimation();
		this.lastCanAttack = canAttack(client, client.getLocalPlayer().getWorldLocation());
	}

	enum Attack {
		MELEE(Prayer.PROTECT_FROM_MELEE, Color.ORANGE, Color.RED, new int[] { 7574, 7582, 7597, 7604, 7612 }),
		RANGED(Prayer.PROTECT_FROM_MISSILES, Color.GREEN, new Color(0, 128, 0), new int[] { 7578, 7581, 7605, 7593 }),
		MAGIC(Prayer.PROTECT_FROM_MAGIC, Color.CYAN, Color.BLUE, new int[] { 7583, 7610, 7592 }),
		UNKNOWN(null, Color.WHITE, Color.GRAY, new int[0]);

		private final Prayer prayer;

		private final Color normalColor;

		private final Color criticalColor;

		private final int[] animationIds;

		Prayer getPrayer() {
			return this.prayer;
		}

		Color getNormalColor() {
			return this.normalColor;
		}

		Color getCriticalColor() {
			return this.criticalColor;
		}

		int[] getAnimationIds() {
			return this.animationIds;
		}

		Attack(Prayer prayer, Color normalColor, Color criticalColor, int[] animationIds) {
			this.prayer = prayer;
			this.normalColor = normalColor;
			this.criticalColor = criticalColor;
			this.animationIds = animationIds;
		}

		static Attack attackFromId(int animationId) {
			for (Attack attack : values()) {
				if (ArrayUtils.contains(attack.getAnimationIds(), animationId)) return attack;
			}
			return null;
		}
	}

	enum Type {
		NIBBLER(new int[] { 7691 }, InfernoNPC.Attack.MELEE, 4, 99, 100),
		BAT(new int[] { 7692 }, InfernoNPC.Attack.RANGED, 3, 4, 7),
		BLOB(new int[] { 7693 }, InfernoNPC.Attack.UNKNOWN, 6, 15, 4),
		MELEE(new int[] { 7697 }, InfernoNPC.Attack.MELEE, 4, 1, 3),
		RANGER(new int[] { 7698, 7702 }, InfernoNPC.Attack.RANGED, 4, 98, 2),
		MAGE(new int[] { 7699, 7703 }, InfernoNPC.Attack.MAGIC, 4, 98, 1),
		JAD(new int[] { 7700, 7704 }, InfernoNPC.Attack.UNKNOWN, 3, 99, 0),
		HEALER_JAD(new int[] { 3128, 7701, 7705 }, InfernoNPC.Attack.MELEE, 4, 1, 6),
		ZUK(new int[] { 7706 }, InfernoNPC.Attack.UNKNOWN, 10, 99, 99),
		HEALER_ZUK(new int[] { 7708 }, InfernoNPC.Attack.UNKNOWN, -1, 99, 100);

		private final int[] npcIds;

		private final InfernoNPC.Attack defaultAttack;

		private final int ticksAfterAnimation;

		private final int range;

		private final int priority;

		int[] getNpcIds() {
			return this.npcIds;
		}

		InfernoNPC.Attack getDefaultAttack() {
			return this.defaultAttack;
		}

		int getTicksAfterAnimation() {
			return this.ticksAfterAnimation;
		}

		int getRange() {
			return this.range;
		}

		int getPriority() {
			return this.priority;
		}

		Type(int[] npcIds, InfernoNPC.Attack defaultAttack, int ticksAfterAnimation, int range, int priority) {
			this.npcIds = npcIds;
			this.defaultAttack = defaultAttack;
			this.ticksAfterAnimation = ticksAfterAnimation;
			this.range = range;
			this.priority = priority;
		}

		static Type typeFromId(int npcId) {
			for (Type type : values()) {
				if (ArrayUtils.contains(type.getNpcIds(), npcId)) return type;
			}
			return null;
		}
	}
}
