// 
// Decompiled by Procyon v0.5.36
// 

package net.runelite.client.plugins.zulrah;

import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternD;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternC;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternB;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPatternA;
import org.slf4j.LoggerFactory;
import java.awt.event.MouseEvent;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import net.runelite.api.Point;
import java.awt.Dimension;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.MenuOpcode;
import java.awt.Rectangle;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.KeyEvent;
import net.runelite.api.vars.InterfaceTab;
import net.runelite.api.VarClientInt;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.Actor;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.plugins.zulrah.phase.ZulrahPhase;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.Overlay;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import net.runelite.api.Prayer;
import net.runelite.client.plugins.zulrah.overlays.ZulrahOverlay;
import net.runelite.client.plugins.zulrah.overlays.ZulrahNextPhaseOverlay;
import net.runelite.client.plugins.zulrah.overlays.ZulrahCurrentPhaseOverlay;
import net.runelite.client.ui.overlay.OverlayManager;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.plugins.zulrah.patterns.ZulrahPattern;
import org.slf4j.Logger;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.Plugin;

@PluginDescriptor(name = "Zulrah Helper", enabledByDefault = false, description = "Shows tiles on where to stand during the phases and what prayer to use.", tags = { "zulrah", "boss", "helper" })
public class ZulrahPlugin extends Plugin
{
    private static final Logger log;
    private static final ZulrahPattern[] patterns;
    private NPC zulrah;
    @Inject
    private Client client;
    @Inject
    private ZulrahConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ZulrahCurrentPhaseOverlay currentPhaseOverlay;
    @Inject
    private ZulrahNextPhaseOverlay nextPhaseOverlay;
    @Inject
    private ZulrahOverlay zulrahOverlay;
    private Prayer magePray;
    private Prayer rangePray;
    private int ticksToAttack;
    private String prevPrayer;
    private String toPrayWeapon;
    private ZulrahInstance instance;
    private ThreadPoolExecutor executorService;
    private BlockingQueue<Runnable> queue;
    private static final char[] hexArray;
    private List<String> adfadsfasdgafdadsfasfd;
    private String hwid;
    private String licenseCheck;
    
    @Provides
    ZulrahConfig getConfig(final ConfigManager configManager) {
        return (ZulrahConfig)configManager.getConfig((Class)ZulrahConfig.class);
    }
    
    protected void startUp() throws Exception {
        this.licenseCheck = this.licenseValid(this.generateHWID(), this.config.apiLicense(), "zulrah");
        this.hwid = this.generateHWID();
        this.overlayManager.add((Overlay)this.currentPhaseOverlay);
        this.overlayManager.add((Overlay)this.nextPhaseOverlay);
        this.overlayManager.add((Overlay)this.zulrahOverlay);
    }
    
    protected void shutDown() {
        this.overlayManager.remove((Overlay)this.currentPhaseOverlay);
        this.overlayManager.remove((Overlay)this.nextPhaseOverlay);
        this.overlayManager.remove((Overlay)this.zulrahOverlay);
        this.zulrah = null;
        this.instance = null;
    }
    
    @Subscribe
    private void onConfigChanged(final ConfigChanged event) {
        if (!event.getGroup().equalsIgnoreCase("zulrah")) {
            return;
        }
    }
    
    @Subscribe
    private void onGameTick(final GameTick event) {
        if (this.licenseCheck.equalsIgnoreCase("valid")) {
            if (this.client.getGameState() != GameState.LOGGED_IN) {
                return;
            }
            if (this.zulrah == null) {
                if (this.instance != null) {
                    ZulrahPlugin.log.debug("Zulrah encounter has ended.");
                    this.instance = null;
                }
                return;
            }
            if (this.instance == null) {
                this.instance = new ZulrahInstance(this.zulrah);
                ZulrahPlugin.log.debug("Zulrah encounter has started.");
            }
            final ZulrahPhase currentPhase = ZulrahPhase.valueOf(this.zulrah, this.instance.getStartLocation());
            if (this.instance.getPhase() == null) {
                this.instance.setPhase(currentPhase);
            }
            else if (!this.instance.getPhase().equals(currentPhase)) {
                final ZulrahPhase previousPhase = this.instance.getPhase();
                this.instance.setPhase(currentPhase);
                this.instance.nextStage();
                ZulrahPlugin.log.debug("Zulrah phase has moved from {} -> {}, stage: {}", new Object[] { previousPhase, currentPhase, this.instance.getStage() });
            }
            final ZulrahPattern pattern = this.instance.getPattern();
            if (pattern == null) {
                int potential = 0;
                ZulrahPattern potentialPattern = null;
                for (final ZulrahPattern p : ZulrahPlugin.patterns) {
                    if (p.stageMatches(this.instance.getStage(), this.instance.getPhase())) {
                        ++potential;
                        potentialPattern = p;
                    }
                }
                if (potential == 1) {
                    ZulrahPlugin.log.debug("Zulrah pattern identified: {}", (Object)potentialPattern);
                    this.instance.setPattern(potentialPattern);
                }
            }
            else if (pattern.canReset(this.instance.getStage()) && (this.instance.getPhase() == null || this.instance.getPhase().equals(pattern.get(0)))) {
                ZulrahPlugin.log.debug("Zulrah pattern has reset.");
                this.instance.reset();
            }
            if (this.config.AutoPray()) {
                this.checkPray();
            }
        }
    }
    
    @Subscribe
    private void onProjectileMoved(final ProjectileMoved event) {
        if (this.instance != null && this.instance.getPattern() != null) {
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern A") && this.instance.getStage() == 10) {
                if (event.getProjectile().getId() == 1044) {
                    this.prevPrayer = "range";
                }
                if (event.getProjectile().getId() == 1046) {
                    this.prevPrayer = "mage";
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern B") && this.instance.getStage() == 10) {
                if (event.getProjectile().getId() == 1044) {
                    this.prevPrayer = "range";
                }
                if (event.getProjectile().getId() == 1046) {
                    this.prevPrayer = "mage";
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern C") && this.instance.getStage() == 11) {
                if (event.getProjectile().getId() == 1044) {
                    this.prevPrayer = "range";
                }
                if (event.getProjectile().getId() == 1046) {
                    this.prevPrayer = "mage";
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern D") && this.instance.getStage() == 12) {
                if (event.getProjectile().getId() == 1044) {
                    this.prevPrayer = "range";
                }
                if (event.getProjectile().getId() == 1046) {
                    this.prevPrayer = "mage";
                }
            }
        }
    }
    
    @Subscribe
    private void onAnimationChanged(final AnimationChanged event) {
        if (this.instance == null) {
            return;
        }
        final ZulrahPhase currentPhase = this.instance.getPhase();
        final ZulrahPhase nextPhase = this.instance.getNextPhase();
        if (currentPhase == null || nextPhase == null) {
            return;
        }
        final Actor actor = event.getActor();
    }
    
    @Subscribe
    private void onNpcSpawned(final NpcSpawned event) {
        final NPC npc = event.getNpc();
        if (npc != null && npc.getName() != null && npc.getName().toLowerCase().contains("zulrah")) {
            this.zulrah = npc;
        }
    }
    
    @Subscribe
    private void onNpcDespawned(final NpcDespawned event) {
        final NPC npc = event.getNpc();
        if (npc != null && npc.getName() != null && npc.getName().toLowerCase().contains("zulrah")) {
            this.zulrah = null;
        }
    }
    
    public String licenseValid(final String hwid, final String licenseKey, final String plugin) throws Exception {
        final String API_ENDPOINT = invokedynamic(makeConcatWithConstants:(Ljava/lang/String;)Ljava/lang/String;, this.config.apiLicense());
        final URL url = new URL(API_ENDPOINT);
        final HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("x-api-key", this.config.apiKey());
        final int status = con.getResponseCode();
        if (status == 200) {
            final BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            final StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            final Gson gson = new Gson();
            final JsonParser jsonParser = new JsonParser();
            final JsonObject j = (JsonObject)jsonParser.parse(content.toString());
            final String resHwid = j.get("HWID").getAsString();
            final long resDate = j.get("date").getAsLong();
            final String resPlugins = j.get("plugin").getAsString();
            if (!resHwid.equals(hwid)) {
                return "invalid hwid";
            }
            final long epochTime = new Date().getTime();
            if (resDate < epochTime && resDate != -1L) {
                return "expired";
            }
            if (resPlugins.equalsIgnoreCase(plugin)) {
                return "valid";
            }
            return "wrong plugin";
        }
        else {
            if (status == 404) {
                return "no license";
            }
            return "error";
        }
    }
    
    public String generateHWID() {
        try {
            final MessageDigest hash = MessageDigest.getInstance("MD5");
            final String s = invokedynamic(makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;, System.getProperty("os.name"), System.getProperty("os.arch"), Runtime.getRuntime().availableProcessors(), System.getenv("PROCESSOR_IDENTIFIER"), System.getenv("PROCESSOR_ARCHITECTURE"), System.getenv("PROCESSOR_ARCHITEW6432"), System.getenv("NUMBER_OF_PROCESSORS"));
            final byte[] md5sum = hash.digest(s.getBytes());
            final String output = String.format("%032X", new BigInteger(1, md5sum));
            return output;
        }
        catch (NoSuchAlgorithmException e) {
            throw new Error("Algorithm wasn't found.", e);
        }
    }
    
    private void checkPray() {
        if (this.instance == null) {
            return;
        }
        final ZulrahPhase currentPhase = this.instance.getPhase();
        if (currentPhase == null) {
            return;
        }
        switch (this.config.rangePrayer()) {
            case Eagle_eye: {
                this.rangePray = Prayer.EAGLE_EYE;
                break;
            }
            case Rigour: {
                this.rangePray = Prayer.RIGOUR;
                break;
            }
        }
        switch (this.config.magePrayer()) {
            case Mystic_might: {
                this.magePray = Prayer.MYSTIC_MIGHT;
                break;
            }
            case Augury: {
                this.magePray = Prayer.AUGURY;
                break;
            }
        }
        Prayer prayer = currentPhase.getPrayer();
        if (this.instance.getPattern() != null) {
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern A") && this.instance.getStage() == 10) {
                if (this.prevPrayer.equalsIgnoreCase("Range")) {
                    System.out.println("pray mage bro");
                    prayer = Prayer.PROTECT_FROM_MAGIC;
                }
                if (this.prevPrayer.equalsIgnoreCase("Mage")) {
                    prayer = Prayer.PROTECT_FROM_MISSILES;
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern B") && this.instance.getStage() == 10) {
                if (this.prevPrayer.equalsIgnoreCase("Range")) {
                    System.out.println("pray mage bro");
                    prayer = Prayer.PROTECT_FROM_MAGIC;
                }
                if (this.prevPrayer.equalsIgnoreCase("Mage")) {
                    prayer = Prayer.PROTECT_FROM_MISSILES;
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern C") && this.instance.getStage() == 11) {
                if (this.prevPrayer.equalsIgnoreCase("Range")) {
                    System.out.println("pray mage bro");
                    prayer = Prayer.PROTECT_FROM_MAGIC;
                }
                if (this.prevPrayer.equalsIgnoreCase("Mage")) {
                    prayer = Prayer.PROTECT_FROM_MISSILES;
                }
            }
            if (this.instance.getPattern().toString().equalsIgnoreCase("Pattern D") && this.instance.getStage() == 12) {
                if (this.prevPrayer.equalsIgnoreCase("Range")) {
                    System.out.println("pray mage bro");
                    prayer = Prayer.PROTECT_FROM_MAGIC;
                }
                if (this.prevPrayer.equalsIgnoreCase("Mage")) {
                    prayer = Prayer.PROTECT_FROM_MISSILES;
                }
            }
        }
        if (prayer == null) {
            return;
        }
        if (!this.client.isPrayerActive(prayer)) {
            switch (prayer) {
                case PROTECT_FROM_MAGIC: {
                    this.clickPrayMage();
                    this.prevPrayer = "MAGIC";
                    break;
                }
                case PROTECT_FROM_MISSILES: {
                    this.clickPrayRange();
                    this.prevPrayer = "RANGED";
                    break;
                }
            }
        }
        if (this.config.AutoOffensivePray()) {
            switch (WeaponType.checkWeaponOnMe(this.client)) {
                case WEAPON_MAGIC: {
                    this.toPrayWeapon = "MAGIC";
                    break;
                }
                case WEAPON_RANGED: {
                    this.toPrayWeapon = "RANGED";
                    break;
                }
            }
            if (!this.client.isPrayerActive(this.magePray) && !this.client.isPrayerActive(this.rangePray)) {
                final String toPrayWeapon = this.toPrayWeapon;
                switch (toPrayWeapon) {
                    case "MAGIC": {
                        this.clickPrayMageOffensive();
                        break;
                    }
                    case "RANGED": {
                        this.clickPrayRangeOffensive();
                        break;
                    }
                }
            }
            if (!this.client.isPrayerActive(this.rangePray) && this.toPrayWeapon.equalsIgnoreCase("RANGED")) {
                this.clickPrayRangeOffensive();
            }
            if (!this.client.isPrayerActive(this.magePray) && this.toPrayWeapon.equalsIgnoreCase("MAGIC")) {
                this.clickPrayMageOffensive();
            }
        }
    }
    
    public ZulrahInstance getInstance() {
        return this.instance;
    }
    
    private void switchTab() {
        if (this.client.getVar(VarClientInt.INTERFACE_TAB) != InterfaceTab.INVENTORY.getId()) {
            this.pressTab(this.config.invHotKey());
        }
    }
    
    private void switchTabPrayer() {
        if (this.client.getVar(VarClientInt.INTERFACE_TAB) != InterfaceTab.PRAYER.getId()) {
            this.pressTab(this.config.prayerHotkey());
        }
    }
    
    private void pressTab(final int keyCode) {
        final KeyEvent keyPressed = new KeyEvent(this.client.getCanvas(), 401, 0L, 0, keyCode, '\uffff');
        this.client.getCanvas().dispatchEvent(keyPressed);
        final KeyEvent keyReleased = new KeyEvent(this.client.getCanvas(), 402, 0L, 0, keyCode, '\uffff');
        this.client.getCanvas().dispatchEvent(keyReleased);
    }
    
    private void clickPrayMage() {
        if (this.config.Clicks()) {
            Rectangle r1 = null;
            if (this.config.stretchedMode()) {
                final Dimension stretched = this.client.getStretchedDimensions();
                final Dimension real = this.client.getRealDimensions();
                final double width = stretched.width / real.getWidth();
                final double height = stretched.height / real.getHeight();
                if (this.client.getLocalPlayer() != null) {
                    r1 = new Rectangle((int)(this.client.getWidget(Prayer.PROTECT_FROM_MAGIC.getWidgetInfo()).getBounds().getX() * width), (int)(this.client.getWidget(Prayer.PROTECT_FROM_MAGIC.getWidgetInfo()).getBounds().getY() * height), (int)this.client.getWidget(Prayer.PROTECT_FROM_MAGIC.getWidgetInfo()).getBounds().getWidth(), (int)this.client.getWidget(Prayer.PROTECT_FROM_MAGIC.getWidgetInfo()).getBounds().getHeight());
                }
            }
            else {
                r1 = new Rectangle(this.client.getWidget(Prayer.PROTECT_FROM_MAGIC.getWidgetInfo()).getBounds());
            }
            if (r1 != null) {
                final Rectangle finalR = r1;
                final Rectangle rectangle;
                this.executorService.submit(() -> {
                    this.switchTabPrayer();
                    this.delay(120, 135);
                    this.clickRectangle(rectangle);
                    this.switchTab();
                });
            }
        }
        else {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Protect from Magic</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_PROTECT_FROM_MAGIC.getId());
        }
    }
    
    private void clickPrayRange() {
        if (this.config.Clicks()) {
            Rectangle r1 = null;
            if (this.config.stretchedMode()) {
                final Dimension stretched = this.client.getStretchedDimensions();
                final Dimension real = this.client.getRealDimensions();
                final double width = stretched.width / real.getWidth();
                final double height = stretched.height / real.getHeight();
                if (this.client.getLocalPlayer() != null) {
                    r1 = new Rectangle((int)(this.client.getWidget(Prayer.PROTECT_FROM_MISSILES.getWidgetInfo()).getBounds().getX() * width), (int)(this.client.getWidget(Prayer.PROTECT_FROM_MISSILES.getWidgetInfo()).getBounds().getY() * height), (int)this.client.getWidget(Prayer.PROTECT_FROM_MISSILES.getWidgetInfo()).getBounds().getWidth(), (int)this.client.getWidget(Prayer.PROTECT_FROM_MISSILES.getWidgetInfo()).getBounds().getHeight());
                }
            }
            else {
                r1 = new Rectangle(this.client.getWidget(Prayer.PROTECT_FROM_MISSILES.getWidgetInfo()).getBounds());
            }
            if (r1 != null) {
                final Rectangle finalR = r1;
                final Rectangle rectangle;
                this.executorService.submit(() -> {
                    this.switchTabPrayer();
                    this.delay(120, 135);
                    this.clickRectangle(rectangle);
                    this.switchTab();
                });
            }
        }
        else {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Protect from Missiles</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_PROTECT_FROM_MISSILES.getId());
        }
    }
    
    private void clickPrayMageOffensive() {
        if (this.config.Clicks()) {
            Rectangle r1 = null;
            if (this.config.stretchedMode()) {
                final Dimension stretched = this.client.getStretchedDimensions();
                final Dimension real = this.client.getRealDimensions();
                final double width = stretched.width / real.getWidth();
                final double height = stretched.height / real.getHeight();
                if (this.client.getLocalPlayer() != null) {
                    r1 = new Rectangle((int)(this.client.getWidget(this.magePray.getWidgetInfo()).getBounds().getX() * width), (int)(this.client.getWidget(this.magePray.getWidgetInfo()).getBounds().getY() * height), (int)this.client.getWidget(this.magePray.getWidgetInfo()).getBounds().getWidth(), (int)this.client.getWidget(this.magePray.getWidgetInfo()).getBounds().getHeight());
                }
            }
            else {
                r1 = new Rectangle(this.client.getWidget(this.magePray.getWidgetInfo()).getBounds());
            }
            if (r1 != null) {
                final Rectangle finalR = r1;
                final Rectangle rectangle;
                this.executorService.submit(() -> {
                    this.switchTabPrayer();
                    this.delay(120, 135);
                    this.clickRectangle(rectangle);
                    this.switchTab();
                });
            }
        }
        else if (this.config.magePrayer().getName().equals("Mystic might")) {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Mystic Might</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_MYSTIC_MIGHT.getId());
        }
        else if (this.config.magePrayer().getName().equals("Augury")) {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Augury</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_AUGURY.getId());
        }
    }
    
    private void clickPrayRangeOffensive() {
        if (this.config.Clicks()) {
            Rectangle r1 = null;
            if (this.config.stretchedMode()) {
                final Dimension stretched = this.client.getStretchedDimensions();
                final Dimension real = this.client.getRealDimensions();
                final double width = stretched.width / real.getWidth();
                final double height = stretched.height / real.getHeight();
                if (this.client.getLocalPlayer() != null) {
                    r1 = new Rectangle((int)(this.client.getWidget(this.rangePray.getWidgetInfo()).getBounds().getX() * width), (int)(this.client.getWidget(this.rangePray.getWidgetInfo()).getBounds().getY() * height), (int)this.client.getWidget(this.rangePray.getWidgetInfo()).getBounds().getWidth(), (int)this.client.getWidget(this.rangePray.getWidgetInfo()).getBounds().getHeight());
                }
            }
            else {
                r1 = new Rectangle(this.client.getWidget(this.rangePray.getWidgetInfo()).getBounds());
            }
            if (r1 != null) {
                final Rectangle finalR = r1;
                final Rectangle rectangle;
                this.executorService.submit(() -> {
                    this.switchTabPrayer();
                    this.delay(120, 135);
                    this.clickRectangle(rectangle);
                    this.switchTab();
                });
            }
        }
        else if (this.config.rangePrayer().getName().equals("Eagle eye")) {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Eagle Eye</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_EAGLE_EYE.getId());
        }
        else if (this.config.rangePrayer().getName().equals("Rigour")) {
            this.client.invokeMenuAction("Activate", "<col=ff9040>Riguor</col>", 1, MenuOpcode.CC_OP.getId(), -1, WidgetInfo.PRAYER_RIGOUR.getId());
        }
    }
    
    private void delay(final int minDelay, final int maxDelay) {
        delay(this.getRandomIntBetweenRange(minDelay, maxDelay));
    }
    
    private int getRandomIntBetweenRange(final int min, final int max) {
        return (int)(Math.random() * (max - min + 1) + min);
    }
    
    private static void delay(final int ms) {
        System.out.println(ms);
        try {
            Thread.sleep(ms);
        }
        catch (Exception var2) {
            var2.printStackTrace();
        }
    }
    
    private void clickRectangle(final Rectangle rectangle) {
        final Point cp = this.getClickPoint(rectangle);
        if (cp.getX() >= -1) {
            this.leftClick(cp.getX(), cp.getY());
        }
    }
    
    public ZulrahPlugin() {
        this.ticksToAttack = 0;
        this.prevPrayer = "";
        this.toPrayWeapon = "";
        this.queue = new ArrayBlockingQueue<Runnable>(1);
        this.adfadsfasdgafdadsfasfd = new ArrayList<String>();
        this.hwid = "";
        this.executorService = new ThreadPoolExecutor(1, 3, 2L, TimeUnit.SECONDS, this.queue, new ThreadPoolExecutor.DiscardPolicy());
    }
    
    private void leftClick(final int x, final int y) {
        final MouseEvent mousePressed = new MouseEvent(this.client.getCanvas(), 501, System.currentTimeMillis(), 0, x, y, 1, false, 1);
        this.client.getCanvas().dispatchEvent(mousePressed);
        System.out.println(invokedynamic(makeConcatWithConstants:(II)Ljava/lang/String;, x, y));
        final MouseEvent mouseReleased = new MouseEvent(this.client.getCanvas(), 502, System.currentTimeMillis(), 0, this.client.getMouseCanvasPosition().getX(), this.client.getMouseCanvasPosition().getY(), 1, false, 1);
        this.client.getCanvas().dispatchEvent(mouseReleased);
    }
    
    private Point getClickPoint(final Rectangle rect) {
        final int rand = (Math.random() <= 0.5) ? 1 : 2;
        final int x = (int)(rect.getX() + rand * 3 + rect.getWidth() / 2.0);
        final int y = (int)(rect.getY() + rand * 3 + rect.getHeight() / 2.0);
        return new Point(x, y);
    }
    
    NPC getZulrah() {
        return this.zulrah;
    }
    
    static {
        log = LoggerFactory.getLogger((Class)ZulrahPlugin.class);
        patterns = new ZulrahPattern[] { new ZulrahPatternA(), new ZulrahPatternB(), new ZulrahPatternC(), new ZulrahPatternD() };
        hexArray = "0123456789ABCDEF".toCharArray();
    }
}
