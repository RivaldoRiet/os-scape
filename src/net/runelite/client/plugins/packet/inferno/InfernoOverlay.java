package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoPrayerDisplayMode;
import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoSafespotDisplayMode;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class InfernoOverlay extends Overlay {
	private static final int TICK_PIXEL_SIZE = 60;

	private static final int BOX_WIDTH = 10;

	private static final int BOX_HEIGHT = 5;

	private final PanelComponent panelComponent;

	private final InfernoPlugin plugin;

	private final InfernoConfig config;

	private final Client client;

	@Inject
	private InfernoOverlay(Client client, InfernoPlugin plugin, InfernoConfig config) {
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		determineLayer();
		setPriority(OverlayPriority.HIGHEST);
		this.panelComponent = new PanelComponent();
		this.panelComponent.setPreferredSize(new Dimension(150, 0));
		this.panelComponent.getChildren().add(LineComponent.builder().left("Inferno Prayer1 ").build());
	}

	public Dimension render(Graphics2D graphics) {
		this.panelComponent.getChildren().clear();
		this.panelComponent.getChildren().add(LineComponent.builder().left("Inferno Prayer2 " + this.plugin.getMaul()).build());
		Widget meleePrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MELEE);
		Widget rangePrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MISSILES);
		Widget magicPrayerWidget = this.client.getWidget(WidgetInfo.PRAYER_PROTECT_FROM_MAGIC);
		if (this.config.indicateObstacles()) renderObstacles(graphics);
		if (this.config.safespotDisplayMode() == InfernoSafespotDisplayMode.AREA) {
			renderAreaSafepots(graphics);
		} else if (this.config.safespotDisplayMode() == InfernoSafespotDisplayMode.INDIVIDUAL_TILES) {
			renderIndividualTilesSafespots(graphics);
		}
		for (InfernoNPC infernoNPC : this.plugin.getInfernoNpcs()) {
			if (infernoNPC.getNpc().getConvexHull() != null) {
				if (this.config.indicateNonSafespotted() && this.plugin.isNormalSafespots(infernoNPC)
						&& infernoNPC.canAttack(this.client, this.client.getLocalPlayer().getWorldLocation()))
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.RED);
				if (this.config.indicateTemporarySafespotted() && this.plugin.isNormalSafespots(infernoNPC)
						&& infernoNPC.canMoveToAttack(this.client, this.client.getLocalPlayer().getWorldLocation(),
								this.plugin.getObstacles()))
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.YELLOW);
				if (this.config.indicateSafespotted() && this.plugin.isNormalSafespots(infernoNPC))
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.GREEN);
				if (this.config.indicateNibblers() && infernoNPC.getType() == InfernoNPC.Type.NIBBLER
						&& (!this.config.indicateCentralNibbler() || this.plugin.getCentralNibbler() != infernoNPC))
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				if (this.config.indicateCentralNibbler() && infernoNPC.getType() == InfernoNPC.Type.NIBBLER
						&& this.plugin.getCentralNibbler() == infernoNPC)
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.BLUE);
				if (this.config.indicateActiveHealerJad() && infernoNPC.getType() == InfernoNPC.Type.HEALER_JAD
						&& infernoNPC.getNpc().getInteracting() != this.client.getLocalPlayer())
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
				if (this.config.indicateActiveHealerZuk() && infernoNPC.getType() == InfernoNPC.Type.HEALER_ZUK
						&& infernoNPC.getNpc().getInteracting() != this.client.getLocalPlayer())
					OverlayUtil.renderPolygon(graphics, infernoNPC.getNpc().getConvexHull(), Color.CYAN);
			}
			if (this.plugin.isIndicateNpcPosition(infernoNPC)) renderNpcLocation(graphics, infernoNPC);
			if (this.plugin.isTicksOnNpc(infernoNPC) && infernoNPC.getTicksTillNextAttack() > 0)
				renderTicksOnNpc(graphics, infernoNPC, infernoNPC.getNpc());
			if (this.config.ticksOnNpcZukShield() && infernoNPC.getType() == InfernoNPC.Type.ZUK
					&& this.plugin.getZukShield() != null && infernoNPC.getTicksTillNextAttack() > 0)
				renderTicksOnNpc(graphics, infernoNPC, this.plugin.getZukShield());
		}
		boolean prayerWidgetHidden = (meleePrayerWidget == null || rangePrayerWidget == null || magicPrayerWidget == null
				|| meleePrayerWidget.isHidden() || rangePrayerWidget.isHidden() || magicPrayerWidget.isHidden());
		if ((this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.PRAYER_TAB
				|| this.config.prayerDisplayMode() == InfernoPrayerDisplayMode.BOTH)
				&& (!prayerWidgetHidden || this.config.alwaysShowPrayerHelper())) {
			renderPrayerIconOverlay(graphics);
			if (this.config.descendingBoxes()) renderDescendingBoxes(graphics);
		}
		return null;
	}

	private void renderObstacles(Graphics2D graphics) {
		for (WorldPoint worldPoint : this.plugin.getObstacles()) {
			LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
			if (localPoint == null) continue;
			Polygon tilePoly = Perspective.getCanvasTilePoly(this.client, localPoint);
			if (tilePoly == null) continue;
			OverlayUtil.renderPolygon(graphics, tilePoly, Color.BLUE);
		}
	}

	private void renderAreaSafepots(Graphics2D graphics) {
		for (Iterator<Integer> iterator = this.plugin.getSafeSpotAreas().keySet().iterator(); iterator.hasNext();) {
			Color colorEdge1, colorFill;
			int safeSpotId = ((Integer) iterator.next()).intValue();
			if (safeSpotId > 6) continue;
			Color colorEdge2 = null;
			switch (safeSpotId) {
				case 0:
					colorEdge1 = Color.WHITE;
					colorFill = Color.WHITE;
					break;
				case 1:
					colorEdge1 = Color.RED;
					colorFill = Color.RED;
					break;
				case 2:
					colorEdge1 = Color.GREEN;
					colorFill = Color.GREEN;
					break;
				case 3:
					colorEdge1 = Color.BLUE;
					colorFill = Color.BLUE;
					break;
				case 4:
					colorEdge1 = Color.RED;
					colorEdge2 = Color.GREEN;
					colorFill = Color.YELLOW;
					break;
				case 5:
					colorEdge1 = Color.RED;
					colorEdge2 = Color.BLUE;
					colorFill = new Color(255, 0, 255);
					break;
				case 6:
					colorEdge1 = Color.GREEN;
					colorEdge2 = Color.BLUE;
					colorFill = new Color(0, 255, 255);
					break;
				default:
					continue;
			}
			List<int[][]> allEdges = (List) new ArrayList<>();
			int edgeSizeSquared = 0;
			for (WorldPoint worldPoint : this.plugin.getSafeSpotAreas().get(Integer.valueOf(safeSpotId))) {
				LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
				if (localPoint == null) continue;
				Polygon tilePoly = Perspective.getCanvasTilePoly(this.client, localPoint);
				if (tilePoly == null) continue;
				OverlayUtil.renderPolygon(graphics, tilePoly, colorFill);
				int[][] edge1 = { { tilePoly.xpoints[0], tilePoly.ypoints[0] }, { tilePoly.xpoints[1], tilePoly.ypoints[1] } };
				edgeSizeSquared = (int) (edgeSizeSquared + Math.pow((tilePoly.xpoints[0] - tilePoly.xpoints[1]), 2.0D)
						+ Math.pow((tilePoly.ypoints[0] - tilePoly.ypoints[1]), 2.0D));
				allEdges.add(edge1);
				int[][] edge2 = { { tilePoly.xpoints[1], tilePoly.ypoints[1] }, { tilePoly.xpoints[2], tilePoly.ypoints[2] } };
				edgeSizeSquared = (int) (edgeSizeSquared + Math.pow((tilePoly.xpoints[1] - tilePoly.xpoints[2]), 2.0D)
						+ Math.pow((tilePoly.ypoints[1] - tilePoly.ypoints[2]), 2.0D));
				allEdges.add(edge2);
				int[][] edge3 = { { tilePoly.xpoints[2], tilePoly.ypoints[2] }, { tilePoly.xpoints[3], tilePoly.ypoints[3] } };
				edgeSizeSquared = (int) (edgeSizeSquared + Math.pow((tilePoly.xpoints[2] - tilePoly.xpoints[3]), 2.0D)
						+ Math.pow((tilePoly.ypoints[2] - tilePoly.ypoints[3]), 2.0D));
				allEdges.add(edge3);
				int[][] edge4 = { { tilePoly.xpoints[3], tilePoly.ypoints[3] }, { tilePoly.xpoints[0], tilePoly.ypoints[0] } };
				edgeSizeSquared = (int) (edgeSizeSquared + Math.pow((tilePoly.xpoints[3] - tilePoly.xpoints[0]), 2.0D)
						+ Math.pow((tilePoly.ypoints[3] - tilePoly.ypoints[0]), 2.0D));
				allEdges.add(edge4);
			}
			if (allEdges.size() <= 0) continue;
			edgeSizeSquared /= allEdges.size();
			int toleranceSquared = (int) Math.ceil((edgeSizeSquared / 6));
			for (int i = 0; i < allEdges.size(); i++) {
				int[][] baseEdge = allEdges.get(i);
				boolean duplicate = false;
				for (int j = 0; j < allEdges.size(); j++) {
					if (i != j) {
						int[][] checkEdge = allEdges.get(j);
						if (edgeEqualsEdge(baseEdge, checkEdge, toleranceSquared)) {
							duplicate = true;
							break;
						}
					}
				}
				// if (!duplicate) {
				// OverlayUtil.renderFullLine(graphics, baseEdge, colorEdge1);
				// if (colorEdge2 != null)
				// OverlayUtil.renderDashedLine(graphics, baseEdge, colorEdge2);
				// }
			}
		}
	}

	private void renderIndividualTilesSafespots(Graphics2D graphics) {
		for (WorldPoint worldPoint : this.plugin.getSafeSpotMap().keySet()) {
			Color color;
			int safeSpotId = ((Integer) this.plugin.getSafeSpotMap().get(worldPoint)).intValue();
			if (safeSpotId > 6) continue;
			LocalPoint localPoint = LocalPoint.fromWorld(this.client, worldPoint);
			if (localPoint == null) continue;
			Polygon tilePoly = Perspective.getCanvasTilePoly(this.client, localPoint);
			if (tilePoly == null) continue;
			switch (safeSpotId) {
				case 0:
					color = Color.WHITE;
					break;
				case 1:
					color = Color.RED;
					break;
				case 2:
					color = Color.GREEN;
					break;
				case 3:
					color = Color.BLUE;
					break;
				case 4:
					color = new Color(255, 255, 0);
					break;
				case 5:
					color = new Color(255, 0, 255);
					break;
				case 6:
					color = new Color(0, 255, 255);
					break;
				default:
					continue;
			}
			OverlayUtil.renderPolygon(graphics, tilePoly, color);
		}
	}

	private void renderTicksOnNpc(Graphics2D graphics, InfernoNPC infernoNPC, NPC renderOnNPC) {
		Color color = (infernoNPC.getTicksTillNextAttack() == 1
				|| (infernoNPC.getType() == InfernoNPC.Type.BLOB && infernoNPC.getTicksTillNextAttack() == 4))
						? infernoNPC.getNextAttack().getCriticalColor()
						: infernoNPC.getNextAttack().getNormalColor();
		Point canvasPoint = renderOnNPC.getCanvasTextLocation(graphics, String.valueOf(infernoNPC.getTicksTillNextAttack()), 0);
		OverlayUtil.renderTextLocation(graphics, canvasPoint, String.valueOf(infernoNPC.getTicksTillNextAttack()), color);
	}

	private void renderNpcLocation(Graphics2D graphics, InfernoNPC infernoNPC) {
		LocalPoint localPoint = LocalPoint.fromWorld(this.client, infernoNPC.getNpc().getWorldLocation());
		if (localPoint != null) {
			Polygon tilePolygon = Perspective.getCanvasTilePoly(this.client, localPoint);
			if (tilePolygon != null) OverlayUtil.renderPolygon(graphics, tilePolygon, Color.BLUE);
		}
	}

	private void renderDescendingBoxes(Graphics2D graphics) {
		for (Integer tick : this.plugin.getUpcomingAttacks().keySet()) {
			Map<InfernoNPC.Attack, Integer> attackPriority = this.plugin.getUpcomingAttacks().get(tick);
			int bestPriority = 999;
			InfernoNPC.Attack bestAttack = null;
			for (Map.Entry<InfernoNPC.Attack, Integer> attackEntry : attackPriority.entrySet()) {
				if (((Integer) attackEntry.getValue()).intValue() < bestPriority) {
					bestAttack = attackEntry.getKey();
					bestPriority = ((Integer) attackEntry.getValue()).intValue();
				}
			}
			/*
			 * for (InfernoNPC.Attack currentAttack : attackPriority.keySet()) {
			 * Color color = (tick.intValue() == 1 && currentAttack ==
			 * bestAttack) ? Color.RED : Color.ORANGE; Widget prayerWidget =
			 * this.client.getWidget(currentAttack.getPrayer()); int baseX =
			 * (int)prayerWidget.getBounds().getX(); baseX = (int)(baseX +
			 * prayerWidget.getBounds().getWidth() / 2.0D); baseX -= 5; int
			 * baseY = (int)prayerWidget.getBounds().getY() - tick.intValue() *
			 * 60 - 5; baseY = (int)(baseY + 60.0D - (this.plugin.getLastTick()
			 * + 600L - System.currentTimeMillis()) / 600.0D * 60.0D); Rectangle
			 * boxRectangle = new Rectangle(10, 5);
			 * boxRectangle.translate(baseX, baseY); if (currentAttack ==
			 * bestAttack) { OverlayUtil.renderPolygon(graphics, boxRectangle,
			 * color); continue; } if
			 * (this.config.indicateNonPriorityDescendingBoxes())
			 * OverlayUtil.renderPolygon(graphics, boxRectangle, color); }
			 */
		}
	}

	private void renderPrayerIconOverlay(Graphics2D graphics) {
		if (this.plugin.getClosestAttack() != null) {
			InfernoNPC.Attack prayerForAttack = null;
			if (this.client.isPrayerActive(Prayer.PROTECT_FROM_MAGIC)) {
				prayerForAttack = InfernoNPC.Attack.MAGIC;
			} else if (this.client.isPrayerActive(Prayer.PROTECT_FROM_MISSILES)) {
				prayerForAttack = InfernoNPC.Attack.RANGED;
			} else if (this.client.isPrayerActive(Prayer.PROTECT_FROM_MELEE)) {
				prayerForAttack = InfernoNPC.Attack.MELEE;
			}
			/*
			 * if (this.plugin.getClosestAttack() != prayerForAttack ||
			 * this.config.indicateWhenPrayingCorrectly()) { Color prayerColor;
			 * Widget prayerWidget =
			 * this.client.getWidget(this.plugin.getClosestAttack().getPrayer().
			 * getWidgetInfo()); Rectangle prayerRectangle = new
			 * Rectangle((int)prayerWidget.getBounds().getWidth(),
			 * (int)prayerWidget.getBounds().getHeight());
			 * prayerRectangle.translate((int)prayerWidget.getBounds().getX(),
			 * (int)prayerWidget.getBounds().getY()); if
			 * (this.plugin.getClosestAttack() == prayerForAttack) { prayerColor
			 * = Color.GREEN; } else { prayerColor = Color.RED; }
			 * OverlayUtil.renderPolygon(graphics, prayerRectangle,
			 * prayerColor); }
			 */
		}
	}

	private boolean edgeEqualsEdge(int[][] edge1, int[][] edge2, int toleranceSquared) {
		return ((pointEqualsPoint(edge1[0], edge2[0], toleranceSquared) && pointEqualsPoint(edge1[1], edge2[1], toleranceSquared))
				|| (pointEqualsPoint(edge1[0], edge2[1], toleranceSquared)
						&& pointEqualsPoint(edge1[1], edge2[0], toleranceSquared)));
	}

	private boolean pointEqualsPoint(int[] point1, int[] point2, int toleranceSquared) {
		double distanceSquared = Math.pow((point1[0] - point2[0]), 2.0D) + Math.pow((point1[1] - point2[1]), 2.0D);
		return (distanceSquared <= toleranceSquared);
	}

	public void determineLayer() {
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}
}
