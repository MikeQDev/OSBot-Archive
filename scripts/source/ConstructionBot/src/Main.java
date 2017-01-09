import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.Tabs;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Gets 64 construction for your acc", name = "Constructioner", version = 1, logo = ":^)")
public class Main extends Script {

	private final Area CWARS_AREA = new Area(new Position(2438, 3082, 0), new Position(2444, 3094, 0));
	private Area CHAIR_ROOM_AREA = new Area(new Position(1, 1, 1), new Position(1, 1, 1));
	private Area KITCHEN_AREA = new Area(new Position(1, 1, 1), new Position(1, 1, 1));
	private final int PLANK_ITEM = 960, OAK_PLANK_ITEM = 8778;
	private final int NAIL_ITEM = 1539, HAMMER_ITEM = 2347, SAW_ITEM = 8794, COIN_ITEM = 995, TELETAB_ITEM = 8013;
	private String currentState = "Starting...";
	private long startTime;
	private final int PORTAL_OBJECT = 4525;
	private final int CHAIR_SPACE_OBJECT = 4515; // CAN BE 4515-4517
	private final int CHAIR_OBJECT = 6752;
	private final int BUILD_ANIMATION = 3676;
	private final int BANK_OBJECT = 4483;
	private final int LARDER_SPACE_OBJECT = 15403;
	private final int LARDER_OBJECT = 13566;

	private boolean knowsRooms = false;

	enum State {
		BANKING, UNKNOWN, IN_KITCHEN, IN_CHAIR_ROOM, IN_HOUSE, BUILDING, REMOVING_CHAIR, REMOVING_LARDER;
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

			int yChairRoom = portalY + 9;
			int xChairRoom = portalX;

			CHAIR_ROOM_AREA = new Area(xChairRoom - 3, yChairRoom + 3, xChairRoom + 3, yChairRoom - 3);

			int yKitchen = portalY;
			int xKitchen = portalX + 6;

			KITCHEN_AREA = new Area(xKitchen - 4, yKitchen + 4, xKitchen + 4, yKitchen - 4);

			knowsRooms = true;
		} catch (Exception ex) {
			ex.printStackTrace();
			log("Exiting script - error occured while getting house coordinates");
			stop();
		}

	}

	private boolean isBuilding() {
		for (int i = 0; i < 10; i++) {
			if (myPlayer().getAnimation() == BUILD_ANIMATION)
				return true;
			try {
				sleep(random(25, 75));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean hasSupplies() {
		if (getSkills().getDynamic(Skill.CONSTRUCTION) < 33) {
			// crude chairs
			if (inventory.getAmount(PLANK_ITEM) >= 2 && inventory.getAmount(NAIL_ITEM) >= 10)
				return true;
		} else {
			// oak larders
			if (inventory.getAmount(OAK_PLANK_ITEM) >= 8)
				return true;
		}
		return false;
	}

	private void removeLarder() {
		// while (!dialogues.isPendingOption()) {
		RS2Object chair = objects.closest(LARDER_OBJECT);
		chair.interact("Remove");
		while (myPlayer().isMoving())
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		try {
			sleep(random(800, 1000));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * RS2Object chair = objects.closest(CHAIR_OBJECT);
		 * while(!chair.interact("Remove")) sleep(random(100,200));
		 */
		// }

		dialogues.selectOption(1);
		try {
			sleep(random(400, 500));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startBuildingLarder() {
		while (widgets.get(458, 5, 4) == null) {
			RS2Object chairSpace = objects.closest(LARDER_SPACE_OBJECT);
			chairSpace.interact("Build");
			while (myPlayer().isMoving())
				try {
					sleep(random(100, 200));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			try {
				sleep(random(1000, 2000));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		widgets.get(458, 5, 4).interact("Build");

		while (isBuilding()) {
			currentState = "Building...!!!";
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void startBuildingChair() {
		while (widgets.get(458, 4, 4) == null) {
			RS2Object chairSpace = objects.closest(CHAIR_SPACE_OBJECT);
			chairSpace.interact("Build");
			while (myPlayer().isMoving())
				try {
					sleep(random(100, 200));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			try {
				sleep(random(1000, 2000));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		widgets.get(458, 4, 4).interact("Build");

		while (isBuilding()) {
			currentState = "Building...!!!";
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void removeChair() {
		// while (!dialogues.isPendingOption()) {
		RS2Object chair = objects.closest(CHAIR_OBJECT);
		chair.interact("Remove");
		while (myPlayer().isMoving())
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		try {
			sleep(random(800, 1000));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * RS2Object chair = objects.closest(CHAIR_OBJECT);
		 * while(!chair.interact("Remove")) sleep(random(100,200));
		 */
		// }

		dialogues.selectOption(1);
		try {
			sleep(random(400, 500));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private State getState() {
		if (isBuilding())
			return State.BUILDING;
		else if (!hasSupplies())
			return State.BANKING;
		else if (objects.closest(CHAIR_OBJECT) != null)
			return State.REMOVING_CHAIR;
		else if (objects.closest(LARDER_OBJECT) != null)
			return State.REMOVING_LARDER;
		else if (CWARS_AREA.contains(myPlayer()))
			return State.BANKING;
		else if (CWARS_AREA.contains(myPlayer()))
			return State.IN_HOUSE;
		else if (CHAIR_ROOM_AREA.contains(myPlayer()))
			return State.IN_CHAIR_ROOM;
		else if (KITCHEN_AREA.contains(myPlayer()))
			return State.IN_KITCHEN;
		return State.UNKNOWN;
	}

	private void useBank() {
		while (!hasSupplies()) {
			boolean doChairs = skills.getDynamic(Skill.CONSTRUCTION) < 33;
			boolean needsRing = !equipment.isWearingItem(EquipmentSlot.RING);

			Bank b = getBank();
			while (!b.isOpen()) {
				if (!CWARS_AREA.contains(myPlayer()))
					teleCwars();
				needsRing = !equipment.isWearingItem(EquipmentSlot.RING);
				objects.closest(BANK_OBJECT).interact("Use");
				try {
					sleep(random(1000, 1200));
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
						sleep(random(600, 800));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			if (doChairs) {
				b.withdrawAll("Plank");
			} else {
				b.withdrawAll("Oak plank");
			}
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

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case BUILDING:
			currentState = "Building";
			log("Status: " + currentState);
			sleep(random(200, 300));
			break;
		case BANKING:
			currentState = "Banking";
			log("Status: " + currentState);
			sleep(random(1000, 2000));
			knowsRooms = false;
			useBank();
			teleHome();
			break;
		case REMOVING_CHAIR:
			currentState = "Removing";
			log("Status: " + currentState);
			if (tabs.getOpen() != Tab.INVENTORY)
				tabs.open(Tab.INVENTORY);
			removeChair();
			break;
		case REMOVING_LARDER:
			currentState = "Removing";
			log("Status: " + currentState);
			if (tabs.getOpen() != Tab.INVENTORY)
				tabs.open(Tab.INVENTORY);
			removeLarder();
			break;
		case IN_HOUSE:
			currentState = "In house";
			getWalking().webWalk(CHAIR_ROOM_AREA);
			log("Status: " + currentState);
			break;
		case IN_KITCHEN:
			currentState = "In kitchen";
			log("Status: " + currentState);
			startBuildingLarder();
			break;
		case IN_CHAIR_ROOM:
			currentState = "In chair room";
			log("Status: " + currentState);
			startBuildingChair();
			break;
		case UNKNOWN:
			currentState = "Unknown - getting house coordinates?";
			log("Status: " + currentState);
			getHouseCoordinates();
			if (getSkills().getDynamic(Skill.CONSTRUCTION) < 33)
				getWalking().walk(CHAIR_ROOM_AREA);
			else
				getWalking().walk(KITCHEN_AREA);
		}
		return random(100, 300);

	}

	private void enableBuildingMode() {
		while (tabs.getOpen() != Tab.SETTINGS)
			tabs.open(Tab.SETTINGS);
		try {
			sleep(random(400, 600));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sleep(random(300, 600));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!widgets.get(261, 70).interact("View House Options"))
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		try {
			sleep(random(700, 1200));
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (!widgets.get(370, 5).interact("On"))
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		try {
			sleep(random(3000, 4000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (widgets.get(370, 17) != null) {
			widgets.get(370, 17).interact("Close");
		}

	}

	private void teleHome() {
		inventory.getItem(TELETAB_ITEM).interact("Break");
		try {
			sleep(random(3000, 4000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		enableBuildingMode();
		getHouseCoordinates();
	}

	private void teleCwars() {
		try {
			sleep(random(1700, 2000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inCwars())
			return;
		equipment.interactWithNameThatContains("Castle Wars", "dueling");
		/*
		 * try { sleep(random(1700, 2000)); } catch (InterruptedException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */
	}

	private boolean inCwars() {
		return CWARS_AREA.contains(myPlayer());
	}

	@Override
	public void onExit() {

	}

	@Override
	public void onStart() throws InterruptedException {
		startTime = System.currentTimeMillis();
		startXp = getConstructionXp();
	}

	private int startXp = 0;

	@Override
	public void onPaint(Graphics2D g) {
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString("XP: " + getSkills().getExperience(Skill.CONSTRUCTION) + "   ("
				+ getSkills().getDynamic(Skill.CONSTRUCTION) + ").........+" + (getConstructionXp() - startXp)
				+ " xp////[ " + xpPerHour() + " xp/h ]", 10, 285);
		g.drawString("State: " + currentState, 10, 305);
	}

	private double xpPerHour() {
		return ((getConstructionXp() - startXp) * 3600000.0D) / (System.currentTimeMillis() - startTime);
	}

	private int getConstructionXp() {
		return getSkills().getExperience(Skill.CONSTRUCTION);
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

}
