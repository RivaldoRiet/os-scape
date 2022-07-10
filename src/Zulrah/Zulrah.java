package Zulrah;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.awt.*;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import api.Camera;
import api.MenuActions;
import api.Tasks;
import api.tasks.Supplies.PotionType;
import api.threads.PrayerObserver;
import api.utils.Utils;
import net.runelite.api.ItemID;
import net.runelite.api.VarPlayer;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.queries.SimplePlayerQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimplePlayer;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "Trester", category = Category.COMBAT, description = "Forum thread for instructions",
discord = "Trester#5088", name = "Zulrah Slayer", servers = { "Os-scape" }, version = "0.1")

public class Zulrah extends TaskScript implements LoopingScript, MouseListener{
public long STARTTIME, UPTIME;
public String status = "";
public int zulrahKills = 0;

public final Color color1 = new Color(255, 255, 255);
public final Color color2 = new Color(0, 0, 0);
public final Color color3 = new Color(255, 51, 51);

public final BasicStroke stroke1 = new BasicStroke(1);

public final Font font1 = new Font("Arial", 1, 16);
public final Font font2 = new Font("Bauhaus 93", 1, 24);
public final Font font3 = new Font("Calibri", 1, 14);

public final Image img1 = getImage("https://i.imgur.com/kWIQnHF.png");
public PrayerObserver prayerObserver = null;
public WorldArea edge = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));
public WorldPoint last_safe_location = null;
public WorldArea zulrah_pre_lobby = new WorldArea(new WorldPoint(2220, 3073, 0), new WorldPoint(2187, 3036, 0));
public JFrame frmZulrahSlayer;
public boolean staffdetected = false;
public List<Task> tasks = new ArrayList<Task>();
public String[] staff_list = {"trick","raw envy","final wish","polishcivil","miika","runite","7 dust","xion","shane","napalm","delta","trump","bio","azeem","fortune","ruax","money","the sexy","chelle","7 normie","mustbeozzi","dead","harsh","taylor twift","dequavius","praise satan","oixel","west coast","alcohol","j0kester","meyme","joggerss","revizenot","chelle","eso maniac","joey","s8n","olmlet","praise satan","coolers","000","halloween","52xp","itrustx","centrum","dharoks","excuse you","kimmy","psycho clown","yaro","west coast","jonno","bradyb","hellobroski","beastly","demix","compton","dvious","azurill","hc merica"};
public boolean useRigour = false;
public boolean useAugury = false;
public boolean useMage = true;
Point p;
boolean hide = false;
Rectangle close = new Rectangle(10, 335, 168, 40);
Rectangle open = new Rectangle(10, 335, 168, 40);
private final Image showImage = getImage("https://i.imgur.com/0oGU5px.png");

@Override
public void paint(Graphics Graphs) {
    UPTIME = System.currentTimeMillis() - STARTTIME;
        Graphics2D g = (Graphics2D) Graphs;
        if (!hide){
	        g.drawImage(img1, 5, 345, null);
	        g.setColor(color1);
	        g.fillRoundRect(39, 386, 131, 27, 16, 16);
	        g.setColor(color2);
	        g.setStroke(stroke1);
	        g.drawRoundRect(39, 386, 131, 27, 16, 16);
	        g.setColor(color1);
	        g.fillRoundRect(289, 382, 183, 29, 16, 16);
	        g.setColor(color2);
	        g.drawRoundRect(289, 382, 183, 29, 16, 16);
	        g.setColor(color1);
	        g.fillRoundRect(81, 421, 350, 29, 16, 16);
	        g.setColor(color2);
	        g.drawRoundRect(81, 421, 350, 29, 16, 16);
	        g.setFont(font1);
	        g.drawString("Kills: " + this.zulrahKills + " (" + ctx.paint.valuePerHour(this.zulrahKills, this.STARTTIME) + ")", 54, 404);
	        g.drawString("Status: "  + this.status, 99, 444);
	        g.drawString("Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 296, 404);
	        g.setFont(font2);
	        g.setColor(color1);
	        g.drawString("Zulrah", 167, 335);
	        g.setColor(color3);
	        g.drawString("           Slayer", 167, 335);
	        g.setFont(font3);
	        g.setColor(color2);
	        g.drawString("by Trester", 321, 335);
        	g.drawImage(this.showImage, 10, 345, null);
        }
        
        if(hide) { 
        	g.drawImage(this.showImage, 10, 345, null);
        }

        if(this.last_safe_location != null) {
        	ctx.paint.drawTileMatrix(g, last_safe_location, Color.CYAN);
        }

}

@Override
public void onChatMessage(ChatMessage msg) {
	
	if(msg.getMessage().contains("must collect your items")) {
//		collectItems = true;
	}
	
	if(msg.getMessage().contains("kill count")) {
		this.zulrahKills++;
	}

	if(msg.getMessage().toLowerCase().contains("duration")) {
		ctx.log("Duration: " + msg.getMessage().split("<col=ef1020>")[1].split("</col>")[0]);
	}


}

public boolean isInZulrahRoom() {
	if(this.edge.containsPoint(ctx.players.getLocal().getLocation()) || this.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation())) {
		return false;
	}
	return true;
}

@Override
public void onExecute() {
	Tasks.init(ctx);
	Camera.setupCameraZoom();
    STARTTIME = System.currentTimeMillis();
    
    prayerObserver = new PrayerObserver(ctx, new BooleanSupplier() {
		@Override
		public boolean getAsBoolean() {
			return true;
		}
	});
	prayerObserver.setUncaughtExceptionHandler(Utils.handler);
	prayerObserver.start();
    
    EventQueue.invokeLater(new Runnable() {
		public void run() {
			try {
				initialize();
				frmZulrahSlayer.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
}

public void setRun() {
	if(ctx.pathing.energyLevel() >= 5 && !ctx.pathing.running()) {
		ctx.pathing.running(true);
	}
}

public void checkForStaff() {
	if(this.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation()) && isStaffInArea()) {
		staffdetected = true;
	}
}

public boolean isStaffInArea() {
	SimplePlayerQuery<SimplePlayer> staff = ctx.players.populate().filter(this.staff_list);
	if(staff.size() > 0) {
		for(SimplePlayer s : staff) {
			ctx.log("found staff member: " + s.getName() );
		}
		return true;
	}
	return false;
}

public boolean containsFood() {
	if(containsItem("Shark") && containsItem("Cooked karambwan")) {
		return true;
	}
	return false;
}

public void setCamera() {
	if(ctx.viewport.pitch() != 180) {
		ctx.viewport.pitch(180);
	}
}


public boolean inLobby() {
  if(this.zulrah_pre_lobby.containsPoint(ctx.players.getLocal().getLocation())) {
	  return true;
  }
  return false;
}

public void teleportToEdge() {
	MenuActions.invoke("Cast","<col=00ff00>Lumbridge Home Teleport</col>",1,57,-1,14286853);
}

public boolean recoilIsOn() {
	SimpleItem c = ctx.equipment.getEquippedItem(EquipmentSlot.RING);
	if(c != null && c.getName().contains("recoil")) {
		return true;
	}
	return false;
}

public void openCasket() {
	SimpleItem casket = ctx.inventory.populate().filter(p -> p.getName().contains("Coin casket")).next();
	if(casket != null && casket.click(0)) {
		ctx.sleep(500);
	}
}

public boolean isTabOpen(Tab tab) {
	return ctx.game.tab().equals(tab);
}

public void openQuestTab() {
	if(!isTabOpen(Tab.QUESTS)) {
		if(ctx.game.tab(Tab.QUESTS)) {
			ctx.sleep(300);
		}
	}
}

public void openMagicTab() {
	if(!isTabOpen(Tab.MAGIC)) {
		if(ctx.game.tab(Tab.MAGIC)) {
			ctx.sleep(300);
		}
	}
}

public void openInventory() {
	if(!isTabOpen(Tab.INVENTORY)) {
		if(ctx.game.tab(Tab.INVENTORY)) {
			ctx.sleep(300);
		}
	}
}

public boolean presetOpen()
{
	final SimpleWidget screen = ctx.widgets.getWidget(830, 12);

	if (screen != null && !screen.isHidden() && screen.getText().toLowerCase().contains("configuration"))
	{
		return true;
	}
	
	return false;
} 

public void usePreset() {
	if (presetOpen()) {
		MenuActions.invoke("Load preset","",1,57,-1,54394908);
	}else {
		MenuActions.invoke("Open Presets","",1,57,-1,54919175);
	}
}

public boolean lootOnGround() {
	SimpleEntityQuery<SimpleGroundItem> lootation = ctx.groundItems.populate();
	if(lootation.size() > 0 && isInZulrahRoom()) {
		return true;
	}
	return false;
}


public boolean shouldRestock() {
	if(!lootOnGround() && this.containsItem("scales")) {
		return true;
	}
	
	if(!containsFood()) {
		return true;
	}
	return false;
}

public boolean containsItem(String itemName) {
	return !ctx.inventory.populate().filter(p -> p.getName().contains(itemName)).isEmpty();
}

public boolean shouldRestockSupplies() {
	if(containsItem("(3)")) {
		return true;
	}
	if(containsItem("(2)")) {
		return true;
	}
	
	if(containsItem("(1)")) {
		return true;
	}
	if(!containsFood()) {
		return true;
	}
	return false;
}

public void startScript() {
	  tasks.addAll(Arrays.asList(new NpcFighterTask(ctx, this), new BankTask(ctx, this), new WalkerTask(ctx, this)));
}

@Override
  public void onTerminate() {
	Tasks.getSkill().removeAll();
	Tasks.getSkill().disablePrayers();
	teleportToEdge();
  }
 
  public static String runescapeFormat(Integer number) {
    String[] suffix = { "K", "M", "B", "T" };
    int size = (number.intValue() != 0) ? (int)Math.log10(number.intValue()) : 0;
    if (size >= 3)
      while (size % 3 != 0)
        size--; 
    return (size >= 3) ? (String.valueOf(Math.round(number.intValue() / Math.pow(10.0D, size) * 10.0D) / 10.0D) +
      suffix[size / 3 - 1]) : (
      new StringBuilder(String.valueOf(number.intValue()))).toString();
  }

/////////////////////////////////////////////////////// BOOOOOOOLEANSSSSSS //////////////////////////////////////////////////////////////////////



@Override
public int loopDuration() {
	// TODO Auto-generated method stub
	return 1;
}

/**
 * Initialize the contents of the frame.
 */
public void initialize() {
	frmZulrahSlayer = new JFrame();
	frmZulrahSlayer.setTitle("Zulrah Slayer");
	frmZulrahSlayer.setBounds(100, 100, 330, 159);
	frmZulrahSlayer.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frmZulrahSlayer.getContentPane().setLayout(null);
	
	JButton btnStart = new JButton("Start");
	btnStart.setForeground(Color.BLACK);
	btnStart.setBounds(192, 73, 89, 23);
	frmZulrahSlayer.getContentPane().add(btnStart);
	
	JCheckBox chckbxUseRigour = new JCheckBox("Use rigour prayer");
	chckbxUseRigour.setForeground(Color.BLACK);
	chckbxUseRigour.setSelected(false);
	chckbxUseRigour.setBounds(30, 18, 217, 23);
	frmZulrahSlayer.getContentPane().add(chckbxUseRigour);
	
	JCheckBox chckbxUseAuguryMage = new JCheckBox("Use augury mage prayer");
	chckbxUseAuguryMage.setForeground(Color.BLACK);
	chckbxUseAuguryMage.setSelected(false);
	chckbxUseAuguryMage.setBounds(30, 50, 217, 23);
	frmZulrahSlayer.getContentPane().add(chckbxUseAuguryMage);
	
	
	btnStart.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			useRigour = chckbxUseRigour.isSelected();
			useAugury = chckbxUseAuguryMage.isSelected();
			startScript();
			frmZulrahSlayer.dispose();
		}
	});
}

public Image getImage(String url) {
    try {
        return ImageIO.read(new URL(url));
    } catch(IOException e) {
        return null;
    }
}



@Override
public boolean prioritizeTasks() {
	// TODO Auto-generated method stub
	return true;
}



@Override
public List<Task> tasks() {
	// TODO Auto-generated method stub
	return tasks;
}

@Override
public void mouseClicked(MouseEvent e) {
	p = e.getPoint();
  	if (close.contains(p) && !hide) {
  		hide = true;
  	} else if (open.contains(p) && hide) {
  		hide = false;
  	}
}

@Override
public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

}

