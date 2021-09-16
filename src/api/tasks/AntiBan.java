package api.tasks;

import java.util.Arrays;
import java.util.List;

import api.Locations;
import net.runelite.api.GameState;
import simple.robot.api.ClientContext;
import simple.robot.utils.ScriptUtils;

public class AntiBan {

	public List<String> STAFF_NAMES = Arrays.asList("david", "hope", "polar", "spooky", "chaflie", "corey", "klem",
			"professor oak", "adreas", "pegasus", "perfection", "raids", "setup", "trilobita", "kenzz", "spirit", "leaned",
			"paine", "h a r r y", "scape", "mamba", "supreme", "isleview", "kenzz", "andy", "fe chaflie", "zaros", "v12",
			"zachery", "vcx", "hc wizard", "immortal fox", "listen", "niedermayer", "jake", "julia", "harsh", "hans");

	private ClientContext ctx;
	private int staffSecondsCounter;

	public AntiBan(ClientContext ctx) {
		this.ctx = ctx;
		this.staffSecondsCounter = 0;
	}

	public boolean staffFound() {
		return ctx.players.populate().toStream().anyMatch(val -> {
			return STAFF_NAMES.contains(ScriptUtils.stripHtml(val.getName()).toLowerCase());
		});
	}

	public void waitOutStaff(int seconds, boolean logout) {
		while (this.staffFound()) {
			if (this.staffSecondsCounter >= seconds) {
				if (logout) {
					this.ctx.sendLogout();
				}
				this.ctx.log("Stopping script because staff won't leave");
				this.ctx.stopScript();
			}
			this.ctx.log(String.format("Waiting out staff. \nWill log out in %d seconds if staff is still here.", seconds - this.staffSecondsCounter));
			this.staffSecondsCounter++;
			this.ctx.sleep(1000);
		}
		this.staffSecondsCounter = 0;
	}

	public void panic() {
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			ctx.magic.castSpellOnce("Home Teleport");
		} else if (!ctx.players.getLocal().inCombat()) {
			while (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
				ctx.sendLogout();
			}
		}
	}
}
