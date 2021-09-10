package net.runelite.client.plugins.packet.inferno;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.KeyListener;

public class KeyRemappingListener implements KeyListener {
	public Boolean getTogglePrayer() {
		return this.togglePrayer;
	}

	private Boolean togglePrayer = Boolean.valueOf(true);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private final Map<Integer, Integer> modified = new HashMap<>();

	private final Set<Character> blockedChars = new HashSet<>();

	public void keyTyped(KeyEvent e) {
		System.out.println(e);
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'p' && !this.togglePrayer.booleanValue()) {
			this.togglePrayer = Boolean.valueOf(true);
		} else if (e.getKeyChar() == 'p' && this.togglePrayer.booleanValue()) {
			this.togglePrayer = Boolean.valueOf(false);
		}
	}

	public void keyReleased(KeyEvent e) {
		System.out.println(e);
	}
}
