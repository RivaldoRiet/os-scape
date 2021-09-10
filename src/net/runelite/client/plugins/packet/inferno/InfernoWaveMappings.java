package net.runelite.client.plugins.packet.inferno;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.runelite.client.plugins.packet.inferno.displaymodes.InfernoNamingDisplayMode;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class InfernoWaveMappings {
	private static final Map<Integer, int[]> waveMapping;

	private static final Map<Integer, String> npcNameMappingComplex;

	private static final Map<Integer, String> npcNameMappingSimple;

	static Map<Integer, int[]> getWaveMapping() {
		return waveMapping;
	}

	static Map<Integer, String> getNpcNameMappingComplex() {
		return npcNameMappingComplex;
	}

	static Map<Integer, String> getNpcNameMappingSimple() {
		return npcNameMappingSimple;
	}

	static {
		ImmutableMap.Builder<Integer, int[]> waveMapBuilder = new ImmutableMap.Builder();
		waveMapBuilder.put(Integer.valueOf(1), new int[] { 32, 32, 32, 85 });
		waveMapBuilder.put(Integer.valueOf(2), new int[] { 32, 32, 32, 85, 85 });
		waveMapBuilder.put(Integer.valueOf(3), new int[] { 32, 32, 32, 32, 32, 32 });
		waveMapBuilder.put(Integer.valueOf(4), new int[] { 32, 32, 32, 165 });
		waveMapBuilder.put(Integer.valueOf(5), new int[] { 32, 32, 32, 85, 165 });
		waveMapBuilder.put(Integer.valueOf(6), new int[] { 32, 32, 32, 85, 85, 165 });
		waveMapBuilder.put(Integer.valueOf(7), new int[] { 32, 32, 32, 165, 165 });
		waveMapBuilder.put(Integer.valueOf(8), new int[] { 32, 32, 32, 32, 32, 32 });
		waveMapBuilder.put(Integer.valueOf(9), new int[] { 32, 32, 32, 240 });
		waveMapBuilder.put(Integer.valueOf(10), new int[] { 32, 32, 32, 85, 240 });
		waveMapBuilder.put(Integer.valueOf(11), new int[] { 32, 32, 32, 85, 85, 240 });
		waveMapBuilder.put(Integer.valueOf(12), new int[] { 32, 32, 32, 165, 240 });
		waveMapBuilder.put(Integer.valueOf(13), new int[] { 32, 32, 32, 85, 165, 240 });
		waveMapBuilder.put(Integer.valueOf(14), new int[] { 32, 32, 32, 85, 85, 165, 240 });
		waveMapBuilder.put(Integer.valueOf(15), new int[] { 32, 32, 32, 165, 165, 240 });
		waveMapBuilder.put(Integer.valueOf(16), new int[] { 32, 32, 32, 240, 240 });
		waveMapBuilder.put(Integer.valueOf(17), new int[] { 32, 32, 32, 32, 32, 32 });
		waveMapBuilder.put(Integer.valueOf(18), new int[] { 32, 32, 32, 370 });
		waveMapBuilder.put(Integer.valueOf(19), new int[] { 32, 32, 32, 85, 370 });
		waveMapBuilder.put(Integer.valueOf(20), new int[] { 32, 32, 32, 85, 85, 370 });
		waveMapBuilder.put(Integer.valueOf(21), new int[] { 32, 32, 32, 165, 370 });
		waveMapBuilder.put(Integer.valueOf(22), new int[] { 32, 32, 32, 85, 165, 370 });
		waveMapBuilder.put(Integer.valueOf(23), new int[] { 32, 32, 32, 85, 85, 165, 370 });
		waveMapBuilder.put(Integer.valueOf(24), new int[] { 32, 32, 32, 165, 165, 370 });
		waveMapBuilder.put(Integer.valueOf(25), new int[] { 32, 32, 32, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(26), new int[] { 32, 32, 32, 85, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(27), new int[] { 32, 32, 32, 85, 85, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(28), new int[] { 32, 32, 32, 165, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(29), new int[] { 32, 32, 32, 85, 165, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(30), new int[] { 32, 32, 32, 85, 85, 165, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(31), new int[] { 32, 32, 32, 165, 165, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(32), new int[] { 32, 32, 32, 240, 240, 370 });
		waveMapBuilder.put(Integer.valueOf(33), new int[] { 32, 32, 32, 370, 370 });
		waveMapBuilder.put(Integer.valueOf(34), new int[] { 32, 32, 32, 32, 32, 32 });
		waveMapBuilder.put(Integer.valueOf(35), new int[] { 32, 32, 32, 490 });
		waveMapBuilder.put(Integer.valueOf(36), new int[] { 32, 32, 32, 85, 490 });
		waveMapBuilder.put(Integer.valueOf(37), new int[] { 32, 32, 32, 85, 85, 490 });
		waveMapBuilder.put(Integer.valueOf(38), new int[] { 32, 32, 32, 165, 490 });
		waveMapBuilder.put(Integer.valueOf(39), new int[] { 32, 32, 32, 85, 165, 490 });
		waveMapBuilder.put(Integer.valueOf(40), new int[] { 32, 32, 32, 85, 85, 165, 490 });
		waveMapBuilder.put(Integer.valueOf(41), new int[] { 32, 32, 32, 165, 165, 490 });
		waveMapBuilder.put(Integer.valueOf(42), new int[] { 32, 32, 32, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(43), new int[] { 32, 32, 32, 85, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(44), new int[] { 32, 32, 32, 85, 85, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(45), new int[] { 32, 32, 32, 165, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(46), new int[] { 32, 32, 32, 85, 165, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(47), new int[] { 32, 32, 32, 85, 85, 165, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(48), new int[] { 32, 32, 32, 165, 165, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(49), new int[] { 32, 32, 32, 240, 240, 490 });
		waveMapBuilder.put(Integer.valueOf(50), new int[] { 32, 32, 32, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(51), new int[] { 32, 32, 32, 85, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(52), new int[] { 32, 32, 32, 85, 85, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(53), new int[] { 32, 32, 32, 165, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(54), new int[] { 32, 32, 32, 85, 165, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(55), new int[] { 32, 32, 32, 85, 85, 165, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(56), new int[] { 32, 32, 32, 165, 165, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(57), new int[] { 32, 32, 32, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(58), new int[] { 32, 32, 32, 85, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(59), new int[] { 32, 32, 32, 85, 85, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(60), new int[] { 32, 32, 32, 165, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(61), new int[] { 32, 32, 32, 85, 165, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(62), new int[] { 32, 32, 32, 85, 85, 165, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(63), new int[] { 32, 32, 32, 165, 165, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(64), new int[] { 32, 32, 32, 240, 240, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(65), new int[] { 32, 32, 32, 370, 370, 490 });
		waveMapBuilder.put(Integer.valueOf(66), new int[] { 32, 32, 32, 490, 490 });
		waveMapBuilder.put(Integer.valueOf(67), new int[] { 900 });
		waveMapBuilder.put(Integer.valueOf(68), new int[] { 900, 900, 900 });
		waveMapBuilder.put(Integer.valueOf(69), new int[] { 1400 });
		waveMapping = (Map<Integer, int[]>) waveMapBuilder.build();
		ImmutableMap.Builder<Integer, String> nameMapBuilderSimple = new ImmutableMap.Builder();
		nameMapBuilderSimple.put(Integer.valueOf(32), "Nibbler");
		nameMapBuilderSimple.put(Integer.valueOf(85), "Bat");
		nameMapBuilderSimple.put(Integer.valueOf(165), "Blob");
		nameMapBuilderSimple.put(Integer.valueOf(240), "Meleer");
		nameMapBuilderSimple.put(Integer.valueOf(370), "Ranger");
		nameMapBuilderSimple.put(Integer.valueOf(490), "Mage");
		nameMapBuilderSimple.put(Integer.valueOf(900), "Jad");
		nameMapBuilderSimple.put(Integer.valueOf(1400), "Zuk");
		npcNameMappingSimple = (Map<Integer, String>) nameMapBuilderSimple.build();
		ImmutableMap.Builder<Integer, String> nameMapBuilderComplex = new ImmutableMap.Builder();
		nameMapBuilderComplex.put(Integer.valueOf(32), "Jal-Nib");
		nameMapBuilderComplex.put(Integer.valueOf(85), "Jal-MejRah");
		nameMapBuilderComplex.put(Integer.valueOf(165), "Jal-Ak");
		nameMapBuilderComplex.put(Integer.valueOf(240), "Jal-ImKot");
		nameMapBuilderComplex.put(Integer.valueOf(370), "Jal-Xil");
		nameMapBuilderComplex.put(Integer.valueOf(490), "Jal-Zek");
		nameMapBuilderComplex.put(Integer.valueOf(900), "JalTok-Jad");
		nameMapBuilderComplex.put(Integer.valueOf(1400), "TzKal-Zuk");
		npcNameMappingComplex = (Map<Integer, String>) nameMapBuilderComplex.build();
	}

	static void addWaveComponent(InfernoConfig config, PanelComponent panelComponent, String header, int wave, Color titleColor,
			Color color) {
		int[] monsters = waveMapping.get(Integer.valueOf(wave));
		if (monsters == null) return;
		panelComponent.getChildren().add(TitleComponent.builder().text(header).color(titleColor).build());
		for (int i = 0; i < monsters.length; i++) {
			int monsterType = monsters[i];
			int count = 1;
			for (; i < monsters.length - 1 && monsters[i + 1] == monsterType; i++)
				count++;
			TitleComponent.TitleComponentBuilder builder = TitleComponent.builder();
			String npcNameText = "";
			if (config.npcNaming() == InfernoNamingDisplayMode.SIMPLE) {
				npcNameText = npcNameText + npcNameText;
			} else {
				npcNameText = npcNameText + npcNameText;
			}
			if (config.npcLevels()) npcNameText = npcNameText + " (" + npcNameText + ")";
			builder.text("" + count + "x " + count);
			builder.color(color);
			panelComponent.getChildren().add(builder.build());
		}
	}
}
