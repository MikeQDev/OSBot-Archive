import java.awt.Graphics2D;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Makes teletabs in someone elses house", name = "Cucky teletabs SLAVE", version = 1, logo = ":^)")
public class main extends Script {
	private final String HOUSE_OWNER = "iliekconst";

	private long startTime;
	private final int SOFT_CLAY = 1761, HOUSE_TELE = 8013, DUST_RUNE = 4696, LAW_RUNE = 563;
	private final Area CWARS_AREA = new Area(new Position(2438, 3082, 0), new Position(2444, 3094, 0));
	private final Area DRAYNOR_BANK_AREA = new Area(new Position(3092, 3242, 0), new Position(3095, 3245, 0));
	private final Area EDGE_AREA = new Area(new Position(3085, 3487, 0), new Position(3098, 3498, 0));
	private final Area EDGE_BANK_AREA = new Area(new Position(3091, 3489, 0), new Position(3098, 3495, 0));
	private Area study_area;
	private int tab_price, clay_price, dust_rune_price, law_rune_price, profitPerTab;
	private int xpStart = 0;
	private int clay_in_bank = -1;
	private int MAKING_ANIM = 4067;
	private final int BANK_OBJECT = 4483;
	private boolean knowsRooms = false;
	private final int PORTAL_OBJECT = 4525, LECTERN = 13647, GLORY_OBJECT = 13523, PORTAL_OUTSIDE = 15478;
	private final int DOOR_A = 13100, DOOR_B = 13101;
	private String status = "Starting...";
	private boolean useEdge = true;
	private boolean useDraynor = true;
	boolean typeOwnerName = true;

	private enum State {
		USING_LECTERN, BANKING, TELE_HOME, MAKING_TABS, UNKNOWN, ENTERING_HOUSE;
	};

	private State getState() {
		if (objects.closest(PORTAL_OUTSIDE) != null)
			return State.ENTERING_HOUSE;
		else if (!hasSuppliesInInv())
			return State.BANKING;
		else if ((inEdge() && hasSuppliesInInv()) || (inCwars() && hasSuppliesInInv())
				|| (inDraynorBank() && hasSuppliesInInv()))
			return State.TELE_HOME;
		else if (isMakingTabs())
			return State.MAKING_TABS;
		else if (!knowsRooms)
			return State.UNKNOWN;
		else if (knowsRooms)
			return State.USING_LECTERN;
		return State.UNKNOWN;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case BANKING:
			status = "Banking";
			log("Status: " + status);
			if(random(0,85) == 22)
				useEdge = true;
			if (useEdge) {
				if (!inEdge())
					teleEdge();
				bankNormal();
			} else if (useDraynor) {
				teleDraynor();
				bankNormal();
			} else {
				teleCwars();
				bankCwars();
			}
			useEdge = false;
			break;
		case USING_LECTERN:
			status = "Using lectern";
			log("Status: " + status);
			if (!dialogues.inDialogue())
				walkLectern();
			useLectern();
			break;
		case TELE_HOME:
			status = "Teleing home";
			log("Status: " + status);
			teleHome();
			// leaveHouse();
			sleep(random(750, 1750)); // ~990 per hour when 3sec wait; now ~1025
										// per hour
			break;
		case MAKING_TABS:
			status = "Making tabs";
			log("Status: " + status);
			typeOwnerName = false; // in the house
			AntiBan();
			sleep(random(300, 400));
			break;
		case ENTERING_HOUSE:
			status = "Entering house";
			log("Status: " + status);
			if (objects.closest(PORTAL_OUTSIDE) != null) {
				for (int i = 0; i < 5; i++) {
					if (objects.closest(PORTAL_OUTSIDE) == null)
						break;
					objects.closest(PORTAL_OUTSIDE).interact("Enter");
					sleep(random(2000, 3000));
					dialogues.selectOption(3);
					sleep(random(1500, 2000));
					if (typeOwnerName || i == 4) {
						keyboard.typeString(HOUSE_OWNER, true);
					} else {
						widgets.get(162, 31).hover();
						mouse.move(random(30, 480), random(354, 365));
						sleep(random(400, 500));
						mouse.click(false);
					}
					sleep(random(2000, 3000));
					if (objects.closest(PORTAL_OUTSIDE) != null) {
						sleep(random(10000, 20000));
					} else {
						openDoors();
					}
				}
			}
		case UNKNOWN:
			status = "Unknown - in house?";
			log("Status: " + status);
			getHouseCoordinates();
			break;
		}
		return

		random(100, 300);
	}

	private void teleDraynor() {
		if (objects.closest(GLORY_OBJECT) == null)
			return;
		openDoors();
		if (!(myPlayer().isAnimating() && myPlayer().isMoving())) {
			objects.closest(GLORY_OBJECT).interact("Draynor Village");
			try {
				sleep(random(4000, 5000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void walkLectern() {
		if (!study_area.contains(myPlayer())) {
			openDoors();
		}
	}

	private boolean hasOpenDoors() {
		if (objects.closest(13102) == null)
			return false;
		return true;
	}

	private void openDoors() {
		if (hasOpenDoors())
			return;
		try {
			if (random(0, 2) == 0)
				objects.closest(DOOR_A).interact("Open");
			else
				objects.closest(DOOR_B).interact("Open");
			try {
				sleep(random(1750, 2250));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception ex) {
			log("Issue looking for doors to open..");
		}
	}

	private void useLectern() {
		while (widgets.get(79, 0) == null) {
			openDoors();
			objects.closest(LECTERN).interact("Study");
			try {
				sleep(random(3000, 3500));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		widgets.get(79, 17).interact("Make-All");
	}

	private void bankCwars() {
		while (!hasSuppliesInInv()) {
			boolean needsRing = false;
			Bank b = getBank();
			while (!b.isOpen()) {
				if (!CWARS_AREA.contains(myPlayer()))
					teleCwars();
				needsRing = !equipment.isWearingItem(EquipmentSlot.RING);
				objects.closest(BANK_OBJECT).interact("Use");
				try {
					sleep(random(3000, 4000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (needsRing) {
				b.withdraw("Ring of dueling(8)", 1);
				b.close();
				inventory.getItem("Ring of dueling(8)").interact("Wear");
				while (!b.isOpen()) {
					objects.closest(BANK_OBJECT).interact("Use");
					try {
						sleep(random(800, 1000));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
			if (!b.contains("Soft clay") || !inventory.contains(LAW_RUNE) || !inventory.contains(DUST_RUNE)) {
				log("Out of soft clay! Exiting script.");
				stop(true);
			}
			b.withdrawAll("Soft clay");
			clay_in_bank = getAmtClayBank();
			while (b.isOpen()) {
				try {
					sleep(random(300, 800));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				b.close();
			}
		}
	}

	private void bankNormal() {
		if (!EDGE_BANK_AREA.contains(myPlayer()) && useEdge)
			getWalking().webWalk(EDGE_BANK_AREA);
		else if (!DRAYNOR_BANK_AREA.contains(myPlayer()) && useDraynor) {
			getWalking().webWalk(DRAYNOR_BANK_AREA);
		}
		while (!hasSuppliesInInv()) {
			Bank b = getBank();
			while (!b.isOpen()) {
				if (b.isOpen())
					break;
				if (random(0, 5) != 0) {
					objects.closest("Bank booth").interact("Bank");
				} else {
					npcs.closest("Banker").interact("Bank");
				}

				try {
					sleep(random(1550, 2000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (!b.contains("Soft clay") || !inventory.contains(LAW_RUNE) || !inventory.contains(DUST_RUNE)) {
				log("Out of soft clay! Exiting script.");
				stop(true);
			}
			b.withdrawAll("Soft clay");
			clay_in_bank = getAmtClayBank();
			while (b.isOpen()) {
				try {
					sleep(random(300, 800));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				b.close();
			}
		}
	}

	private void teleHome() {
		knowsRooms = false;
		// inventory.getItem(HOUSE_TELE).interact("Break");
		tabs.open(Tab.MAGIC);
		magic.castSpell(Spells.NormalSpells.HOUSE_TELEPORT);
		try {
			sleep(random(2000, 3000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tabs.open(Tab.INVENTORY);
	}

	private void leaveHouse() {
		objects.closest(PORTAL_OBJECT).interact("Leave");
	}

	private void teleCwars() {
		if (inCwars())
			return;
		if (!(myPlayer().isAnimating() && myPlayer().isMoving())) {
			equipment.interactWithNameThatContains("Castle Wars", "dueling");
			try {
				sleep(random(2750, 3250));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void teleEdge() {
		if (inEdge())
			return;
		if (!(myPlayer().isAnimating() && myPlayer().isMoving())) {
			objects.closest(GLORY_OBJECT).interact("Edgeville");
			try {
				sleep(random(4000, 5000));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isMakingTabs() {
		for (int i = 0; i < 5; i++) {
			if (myPlayer().getAnimation() == MAKING_ANIM)
				return true;
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private int getAmtClayBank() {
		return (int) bank.getAmount(SOFT_CLAY);
	}

	private boolean inCwars() {
		return CWARS_AREA.contains(myPlayer());
	}

	private boolean inEdge() {
		return EDGE_AREA.contains(myPlayer());
	}

	private boolean inDraynorBank() {
		return DRAYNOR_BANK_AREA.contains(myPlayer());
	}

	private boolean hasSuppliesInInv() {
		return getInventory().contains(SOFT_CLAY) && getInventory().contains(DUST_RUNE)
				&& getInventory().contains(LAW_RUNE);
	}

	private void loadPrices() throws IOException {
		GrandExchange gE = new GrandExchange();
		tab_price = gE.getOverallPrice(HOUSE_TELE);
		clay_price = gE.getOverallPrice(SOFT_CLAY);
		dust_rune_price = gE.getOverallPrice(DUST_RUNE);
		law_rune_price = gE.getOverallPrice(LAW_RUNE);
		profitPerTab = tab_price - (clay_price + dust_rune_price + law_rune_price);
	}

	private int calcHourlyProfit(int profit) {
		return (int) (profit / ((System.currentTimeMillis() - startTime) / 3600000.0D));
	}

	private int calcTabsPerHour() {
		return (int) (getTabsMade() / ((System.currentTimeMillis() - startTime) / 3600000.0D));
	}

	private int getMagicXp() {
		return skills.getExperience(Skill.MAGIC);
	}

	private int getTabsMade() {
		return (getMagicXp() - xpStart) / 30;
	}

	private void getHouseCoordinates() {
		if (knowsRooms)
			return;
		try {
			RS2Object portal = objects.closest(PORTAL_OBJECT);
			// portal.interact("Lock");
			log("Found portal...");
			Position portalPosition = portal.getPosition();
			int portalX = portalPosition.getX();
			int portalY = portalPosition.getY();

			int yStudyRoom = portalY;
			int xStudyRoom = portalX - 5;

			study_area = new Area(xStudyRoom - 2, yStudyRoom - 2, xStudyRoom - 2, yStudyRoom + 2);
			knowsRooms = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log("Teleing cwars - error occured while getting house coordinates");
			teleCwars();
			// stop();
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		int tabsCreated = getTabsMade();
		int profit = profitPerTab * tabsCreated;
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString("Tabs made: " + tabsCreated + " [" + clay_in_bank + " to go]", 10, 280);
		g.drawString("Total profit: " + profit / 1000.0 + "k [" + calcHourlyProfit(profit) / 1000 + "k gp/hr, "
				+ calcTabsPerHour() + " tabs per hour]", 10, 295);
		g.drawString("Status: " + status, 10, 310);
		g.drawString("Friends house: " + HOUSE_OWNER, 10, 325);

	}

	@Override
	public void onStart() {
		log("Starting Cucky Teletabs");
		try {
			loadPrices();
			log("Loaded prices!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		startTime = System.currentTimeMillis();
		xpStart = getSkills().getExperience(Skill.MAGIC);
		log("Starting with " + xpStart + " xp");
	}

	@Override
	public void onExit() {
		log("Made " + getTabsMade() + " tabs in " + ((System.currentTimeMillis() - startTime) / 60000.0) + " mins!");
		log("Ending with xp: " + getSkills().getExperience(Skill.SMITHING) + " (Started w/ " + xpStart + ")");
		log("Exiting Cucky Teletabs\\");
	}

	private String ft(long duration) {
		String res = "";
		long days = TimeUnit.MILLISECONDS.toDays(duration);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration));
		if (days == 0) {
			res = (hours + ":" + minutes + ":" + seconds);
		} else {
			res = (days + ":" + hours + ":" + minutes + ":" + seconds);
		}
		return res;
	}

	public void AntiBan() throws InterruptedException {
		if (random(1, 30) > 20) {
			sleep(random(1000, 2000));
			return;
		}
		boolean backToInvy = false;
		switch (random(1, 30)) {
		case 1:
			camera.movePitch(random(10, 100));
			break;
		case 2:
			camera.movePitch(79 + (random(1, 99)));
			break;
		case 4:
			tabs.open(Tab.SKILLS);
			backToInvy = true;
			break;
		case 5:
			tabs.open(Tab.QUEST);
			backToInvy = true;
			break;
		case 6:
			camera.movePitch(50 + random(1, 70));
			break;
		case 7:
			camera.movePitch(150 + (random(30, 70)));
			break;
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
			mouse.moveOutsideScreen();
			sleep(random(3000, 7000));
			break;
		}
		sleep(random(900, 1300));
		if (backToInvy)
			tabs.open(Tab.INVENTORY); // RETURNS TO THE INVENTORY TAB AFTER
										// EVERY
										// ANTIBAN INSTANCE
	}
}