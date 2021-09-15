package SlayerSlayer;

import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

import api.Tasks;
import api.threads.PrayerObserver;
import api.utils.Utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.BooleanSupplier;

import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import simple.hooks.filters.SimpleSkills;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.task.Task;
import simple.hooks.scripts.task.TaskScript;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.utils.WorldArea;

//@ScriptManifest(author = "Trester", category = Category.SLAYER, description = "Premium version of my wildy slayer script. See the thread for instructions! Has been updated to version 0.5!",
//discord = "Trester#5088", name = "Vitality Wilderness Slayer Premium", servers = { "Vitality" }, version = "0.6")

@ScriptManifest(author = "Trester", category = Category.SLAYER, description = "Premium version of my wildy slayer script. See the thread for instructions! Has been updated to version 0.7!",
discord = "Trester#5088", name = "Wilderness Slayer Premium", servers = { "Os-scape" }, version = "0.7")

public class Main extends TaskScript implements MouseListener{
	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	public int startSlayerXp, slayerXpGained, CURRENT_SLAYER_XP, slayerlvl;
	private long STARTTIME, UPTIME;
	private List<Task> tasks = new ArrayList<Task>();
	public boolean hasTask = false;
	public boolean shouldGetTask = false;
	public String currentMonster = null;
	public WorldPoint[] previousPath = null;
	public String[] lootName = {"Draconic visage", 
    		"Blood money casket (giant)", "Blood money casket (large)", "Blood money casket (medium)", "Wilderness casket (small)", "Wilderness casket (medium)", 
    		"Wilderness casket (large)", "Wilderness casket (giant)", "Larran's key", "Pumpkin", "Casket"};
	public String status = "";
	private WorldArea edge = new WorldArea(new WorldPoint(3073, 3516, 0), new WorldPoint(3108, 3471, 0));
	public long antifireTimer = 0;
	public boolean antiStuck = false;
	public boolean shouldDrinkAntifire = true;
	public boolean useOffensivePrayer = false;
	public int usePrayer = 0;
	public boolean useSaferoute = false;
	private final Color color1 = new Color(255, 255, 255);
    private final Color color2 = new Color(255, 0, 0);
    private final Color color3 = new Color(0, 0, 0);

    private final Font font1 = new Font("Bauhaus 93", 1, 30);
    private final Font font2 = new Font("Arial", 1, 14);
    private final Font font3 = new Font("Arial", 1, 11);

    private final Image img = getImage("https://i.imgur.com/EYbq9nH.png");
    private final Image hideImage = getImage("https://i.imgur.com/0oGU5px.png");
    private final Image showImage = getImage("https://i.imgur.com/0oGU5px.png");
    
    Point p;
    boolean hide = false;
    Rectangle close = new Rectangle(10, 335, 168, 40);
    Rectangle open = new Rectangle(10, 335, 168, 40);
	
	private static Class<?> _class;
	private static Method action;
	private PrayerObserver prayerObserver = null;
	
	@Override
	public void paint(Graphics Graphs) {
		  this.CURRENT_SLAYER_XP = this.ctx.skills.experience(SimpleSkills.Skills.SLAYER);
		    this.UPTIME = System.currentTimeMillis() - this.STARTTIME;
	        Graphics2D g = (Graphics2D) Graphs;
	        if (!hide){
	        	g.drawImage(img, 2, 302, null);
		        //g.drawImage(this.hideImage, 10, 345, null);

		        g.setColor(color1);
		        if(Integer.valueOf(this.CURRENT_SLAYER_XP - this.startSlayerXp) > 0) {
		        	g.drawString("Total XP: " + runescapeFormat(Integer.valueOf(this.CURRENT_SLAYER_XP - this.startSlayerXp)) + " / P/H (" + runescapeFormat(Integer.valueOf(this.ctx.paint.valuePerHour(this.CURRENT_SLAYER_XP - this.startSlayerXp, this.STARTTIME))) + ")", 295, 367);
		        }
		        if(this.hasTask && this.currentMonster != null) {
		        	String monster = currentMonster.substring(0, 1).toUpperCase() + currentMonster.substring(1);
		        	monster += "s";
		        	g.drawString("Current slayer task: " + monster, 296, 397);
		        }
		        g.drawString("Current status: " + status, 20, 453);
		        g.drawString("Uptime: " + this.ctx.paint.formatTime(this.UPTIME), 294, 425);      
	     	}
	        if(hide) { 
	        	g.drawImage(this.showImage, 10, 345, null);
	        }
	}

	@Override
	public boolean prioritizeTasks() {
		return false;
	}

	@Override
	public List<Task> tasks() {
		return tasks;
	}

	@Override
	public void onChatMessage(ChatMessage msg) {
		
		//ctx.log(msg.getMessage());
		
		if(msg.getMessage().toLowerCase().contains("return to a slayer")) {
			this.shouldGetTask = true;
			this.hasTask = false;
			this.currentMonster = null;
		}
		
		if(msg.getMessage().contains("slayer task is now complete")) {
			this.shouldGetTask = true;
			this.hasTask = false;
			this.currentMonster = null;
		}
		
		if(msg.getMessage().contains("not currently have a slayer assignment")) {
			this.shouldGetTask = true;
			this.hasTask = false;
			this.currentMonster = null;
		}
		
		if(msg.getMessage().contains("about to expire") || msg.getMessage().contains("antifire potion has expired")) {
			shouldDrinkAntifire = true;
			antifireTimer = 0;
		}
		
		if(msg.getMessage().contains("reach that")) {
			antiStuck = true;
		}
		
		if(msg.getMessage().contains("current slayer")) {
			currentMonster = msg.getMessage().split("current slayer assignment is ")[1].split("only")[0].replace(".", "");
			currentMonster = removeTrailingSpaces(currentMonster);
			currentMonster = removeLastChar(currentMonster);
			ctx.log("Monster: " + currentMonster);
			this.hasTask = true;
			//ctx.log("Monster: " + msg.getMessage());
		}

		// After wildy death teleport spam preventions
		if(msg.getMessage().contains("You need to wait at least 30 seconds")) {
			ctx.sleep(30 * 1000);
		}
	}

	@Override
	public void onExecute() {
		CURRENT_SLAYER_XP = ctx.skills.experience(Skills.SLAYER); //check old xp?????
        startSlayerXp = ctx.skills.experience(Skills.SLAYER);
        this.STARTTIME = System.currentTimeMillis();
        this.slayerlvl = Integer.valueOf(this.ctx.skills.realLevel(SimpleSkills.Skills.SLAYER));
        
        Tasks.init(ctx);
        
        prayerObserver = new PrayerObserver(ctx, new BooleanSupplier() {
			@Override
			public boolean getAsBoolean() {
				return true;
			}
		});
		prayerObserver.setUncaughtExceptionHandler(Utils.handler);
		prayerObserver.start();
       // SimpleTrial simpleTrial = new SimpleTrial(ctx, "vitality slayer", 72);
     //   simpleTrial.validate(ctx);
        
      // if(ctx.user.forumsId() != 1943) {
    	//ctx.log("Wrong forum id");
    	//	ctx.stopScript();
    	//}
        
        /* try {
        	String s = get("https://pastebin.com/raw/w8rb4J1w");
			//ctx.log(s);
			if(s.contains("1")) {
				ctx.log("Starting");
			}else {
				ctx.stopScript();
				ctx.log("Stopping");
			}
		} catch (Exception e1) {
			ctx.stopScript();
		}*/
        EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initialize();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
        
       
        
	}

	@Override
	public void onTerminate() {
		Tasks.getSkill().removeAll();
		Tasks.getSkill().disablePrayers();
	}
	
	/* has potions half drunk indicating we have been in the wild */
	public boolean shouldRestock() {
		if(ctx.pathing.inArea(edge)) {
			if(containsItem("Coins")) {
				return true;
			}
			
			if(containsItem("Blood money")) {
				return true;
			}
			
			if(containsItem("(3)")) {
				return true;
			}
			if(containsItem("(2)")) {
				return true;
			}
			
			if(containsItem("(1)")) {
				return true;
			}
			if(!containsItem("sanfew") && !containsItem("restore")) {
				ctx.log("Out of restores");
				return true;
			}
			if (!isWearingChargedGlory()) {
				ctx.log("Out of glory charges");
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldRestockFromWild() {
		if(inWildy()) {
			if(containsItem("wilderness casket (giant)")) {
				return true;
			}
			
			if(this.hasTask == false && this.shouldGetTask == true) {
				return true;
			}
			
			if(!containsItem("sanfew") && !containsItem("restore")) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean containsItem(String itemName) {
		return !ctx.inventory.populate().filter(p -> p.getName().toLowerCase().contains(itemName.toLowerCase())).isEmpty();
	}
	
	public boolean isGeared() {
		if(currentMonster.contains("dragon")) {
			if(containsItem("antifire") || isWearingShield()) {
				return true;
			}else {
				ctx.log("no antifire potion / antidragon shield found");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isWearingChargedGlory()
	{
		if(ctx.equipment.getEquippedItem(EquipmentSlot.AMULET) == null)
		{
			return true;
		}
		
		if(ctx.equipment.getEquippedItem(EquipmentSlot.AMULET).getName().contains("\\("))
		{
			return true;
		}
		
		if(ctx.equipment.getEquippedItem(EquipmentSlot.AMULET).getName().contains("("))
		{
			return true;
		}
		
		return false;
	}
	
	private boolean isWearingShield()
	{
		if(ctx.equipment.getEquippedItem(EquipmentSlot.SHIELD).getName().toLowerCase().contains("dragon"))
		{
			return true;
		}
		return false;
	}
	
	
	public boolean inWildy() {
		SimpleWidget w = ctx.widgets.getWidget(90, 53); // wilderness widget
		if(w != null && w.visibleOnScreen() && w.getText().contains("Level")) {
			int wildlvl = Integer.parseInt(w.getText().split("Level: ")[1]);
			if(wildlvl > 0) {
				return true;
			}
		}
		
		return false;
	}
	
	public String removeLastChar(String str) {
		if(str.toLowerCase().charAt(str.length() - 1) == 's') {
			return str.substring(0, str.length() - 1);
		}else {
			return str;
		}
	}
	
	public boolean blockedMonster() {
		if(currentMonster != null) {
			if(currentMonster.toLowerCase().contains("spiritual")) {
				return true;
			}
			if(currentMonster.toLowerCase().contains("bloodveld")) {
				return true;
			}
			if(currentMonster.toLowerCase().contains("fire giant")) {
				return true;
			}
			if(currentMonster.toLowerCase().contains("lava dragon")) {
				return true;
			}
		}
		return false;
	}
	
  private static String runescapeFormat(Integer number) {
	    String[] suffix = { "K", "M", "B", "T" };
	    int size = (number.intValue() != 0) ? (int)Math.log10(number.intValue()) : 0;
	    if (size >= 3)
	      while (size % 3 != 0)
	        size--; 
	    return (size >= 3) ? (String.valueOf(Math.round(number.intValue() / Math.pow(10.0D, size) * 10.0D) / 10.0D) +
	      suffix[size / 3 - 1]) : (
	      new StringBuilder(String.valueOf(number.intValue()))).toString();
	  }
  
  private Image getImage(String url) {
      try {
          return ImageIO.read(new URL(url));
      } catch(IOException e) {
          return null;
      }
  }
  
  public static String removeTrailingSpaces(String param) 
  {
      if (param == null)
          return null;
      int len = param.length();
      for (; len > 0; len--) {
          if (!Character.isWhitespace(param.charAt(len - 1)))
              break;
      }
      return param.substring(0, len);
  }
  
  private void startScript() {
	  tasks.addAll(Arrays.asList(new BlockTask(ctx, this), new BankTask(ctx, this), new GetSlayerTask(ctx, this), new WalkerTask(ctx, this), new NpcFighterTask(ctx, this)));
  }
  
  private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		//frame.setBounds(100, 100, 414, 247);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JCheckBox chckbxUseOffensivePrayers = new JCheckBox("Use offensive prayers");
		chckbxUseOffensivePrayers.setBounds(52, 28, 186, 23);
		frame.getContentPane().add(chckbxUseOffensivePrayers);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Piety");
		rdbtnNewRadioButton.setEnabled(false);
		buttonGroup.add(rdbtnNewRadioButton);
		rdbtnNewRadioButton.setBounds(105, 54, 167, 23);
		frame.getContentPane().add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Rigour");
		rdbtnNewRadioButton_1.setEnabled(false);
		buttonGroup.add(rdbtnNewRadioButton_1);
		rdbtnNewRadioButton_1.setBounds(105, 94, 177, 23);
		frame.getContentPane().add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Eagle eye");
		rdbtnNewRadioButton_2.setEnabled(false);
		buttonGroup.add(rdbtnNewRadioButton_2);
		rdbtnNewRadioButton_2.setBounds(105, 137, 167, 23);
		frame.getContentPane().add(rdbtnNewRadioButton_2);
		
		JCheckBox chckbxSafeRoute = new JCheckBox("Logout after giant casket drop?");
		chckbxSafeRoute.setSelected(false);
		chckbxSafeRoute.setBounds(52, 198, 248, 23);
		frame.getContentPane().add(chckbxSafeRoute);
		
		JButton btnStart = new JButton("Start");
		btnStart.setBounds(277, 174, 89, 23);
		frame.getContentPane().add(btnStart);
		
		
		
		chckbxUseOffensivePrayers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxUseOffensivePrayers.isSelected()) {
					rdbtnNewRadioButton.setEnabled(true);
					rdbtnNewRadioButton_1.setEnabled(true);
					rdbtnNewRadioButton_2.setEnabled(true);
				}else {
					rdbtnNewRadioButton.setEnabled(false);
					rdbtnNewRadioButton_1.setEnabled(false);
					rdbtnNewRadioButton_2.setEnabled(false);
				}
			}
		});
		
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				useSaferoute = chckbxSafeRoute.isSelected();
				if(chckbxUseOffensivePrayers.isSelected()) {
					useOffensivePrayer = true;
					if(rdbtnNewRadioButton.isSelected()) {
						usePrayer = 1;
					}
					
					if(rdbtnNewRadioButton_1.isSelected()) {
						usePrayer = 2;
					}
					
					if(rdbtnNewRadioButton_2.isSelected()) {
						usePrayer = 3;
					}
					
				}
				
				startScript();
				frame.dispose();
			}
		});
		
	}
  
  public static String get(String url) throws Exception {
	   StringBuilder sb = new StringBuilder();
	   for(Scanner sc = new Scanner(new URL(url).openStream()); sc.hasNext(); )
	      sb.append(sc.nextLine()).append('\n');
	   return sb.toString();
	}
  
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
	
	public void clickFirst(NPC npc)
	{
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_FIRST_OPTION;

		clickNpc(npc, option);
	}
	
	public void click(NPC npc) {
		if (npc == null) return;

		MenuAction option = MenuAction.NPC_SECOND_OPTION;

		clickNpc(npc, option);
	}
	
	public void clickNpc(NPC npc, MenuAction action) {
		try {
			if (npc == null) return;
			int index = npc.getIndex();
			invoke("", "", index, action.getId(), 0, 0);
		} catch (Exception e) {
		}
	}
  
public void onMouse(MouseEvent e) {
	
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
public void mouseEntered(MouseEvent e) {
}

@Override
public void mouseExited(MouseEvent e) {
}

@Override
public void mousePressed(MouseEvent e) {
}

@Override
public void mouseReleased(MouseEvent e) {
}
	
}
