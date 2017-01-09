import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Smelts cannonballs for j", name = "Cucky Cannonballs", version = 1, logo = ":^)")
public class main extends Script {
	private final Area FURNACE_AREA_EDGE = new Area(new Position(3105, 3496, 0), new Position(3110, 3501, 0));
	private final Area BANK_AREA_EDGE = new Area(new Position(3093, 3491, 0), new Position(3098, 3497, 0));
	private final Area FURNACE_AREA_AK = new Area(new Position(3274, 3184, 0), new Position(3278, 3187, 0));
	private final Area BANK_AREA_AK = new Area(new Position(3269, 3164, 0), new Position(3272, 3171, 0));
	// private final Area MIDWAY_AREA_AK = new Area(new Position(3275, 3174, 0),
	// new Position(3279, 3177, 0));

	private Bank b = getBank();
	private Random rand = new Random();
	private int cballStart = 0, cballs = 0;
	private long startTime = 0;
	private int steelBarPrice, cBallPrice, profitPerBar;
	private SmeltLocation smeltLoc = SmeltLocation.EDGE;
	private GrandExchange gE = new GrandExchange();
	private long barsRemaining = -1;
	private int xpStart = 0;

	private enum State {
		BANK, SMELT, WALKBANK, WALKFURNACE, SMELTING;
	};

	private enum Location {
		BANK, FURNACE;
	};

	private enum SmeltLocation {
		EDGE, ALKHARID;
	};

	private State getState() {
		if (FURNACE_AREA_EDGE.contains(myPlayer().getPosition())
				|| FURNACE_AREA_AK.contains(myPlayer().getPosition())) {

			if (isSmelting()) {
				return State.SMELTING;
			} else if (hasBarsInInv() && !isSmelting()) {
				return State.SMELT;
			} else {
				return State.WALKBANK;
			}

			// return State.WALKBANK;
		} else if (BANK_AREA_EDGE.contains(myPlayer().getPosition())
				|| BANK_AREA_AK.contains(myPlayer().getPosition())) {

			if (hasBarsInInv()) {
				return State.WALKFURNACE;
			} else {
				return State.BANK;
			}

			// return State.WALKFURNACE;
		}
		if (hasBarsInInv())
			return State.WALKFURNACE;
		return State.WALKBANK;
	}

	@Override
	public int onLoop() throws InterruptedException {
		cballs = getCballAmount();
		switch (getState()) {
		case BANK:
			log("Bank case");
			bank();
			break;
		case SMELT:
			log("Smelt case");
			startSmelting();
			break;
		case SMELTING:
			log("Smelting case");
			// sleep(random(165000, 175000));
			AntiBan();
			sleep(random(2000, 3000));
			break;
		case WALKBANK:
			log("Walk bank case");
			walkTo(Location.BANK);
			// sleep(random(1500, 2000));
			break;
		case WALKFURNACE:
			log("Walk furnace case");
			walkTo(Location.FURNACE);
			// sleep(random(1500, 2000));
			break;
		}
		return random(100, 300);
	}

	private void startSmelting() {
		getInventory().getItem("Steel bar").interact("Use");
		objects.closest("Furnace").interact("Use");
		/*
		 * //objects.closest("Furnace").hover(); try { sleep(random(50, 100)); }
		 * catch (InterruptedException e) { e.printStackTrace(); }
		 * mouse.click(false);
		 */
		try {
			sleep(random(750, 2000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		///
		// todo:
		// random(100)==50,
		// right click -> smelt all, instead of clicking text to do all at once
		///

		while (myPlayer().isMoving())
			try {
				sleep(random(100, 200));
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		try {
			sleep(random(300, 700));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getMouse().move(random(123, 398), random(362, 371));

		try {
			sleep(random(150, 250));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getMouse().click(false);
	}

	private void bank() {
		b = getBank();
		openBank();

		b.depositAllExcept("Cannonball", "Ammo mould", "Steel bar");

		if (!getInventory().contains("Ammo mould")) {
			if (b.contains("Ammo mould"))
				b.withdraw("Ammo mould", 1);
			else {
				log("No cannonball mould?");
				stop(false);
			}
		}
		if (!b.contains("Steel bar")) {
			log("Out of steel bars! Trying to go to GE, then Exiting script?");
			teleGe();
			stop(true);
		}
		b.withdrawAll("Steel bar");
		barsRemaining = getRemainingBars();

		if (rand.nextInt(5) == 0)
			b.close();
	}

	private void teleGe() {
		if (wearingNecklace())
			equipment.interactWithNameThatContains("Cooking guild", "Skills necklace(");
		try {
			sleep(random(2750, 3250));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	};

	private boolean wearingNecklace() {
		if (equipment.isWearingItemThatContains(EquipmentSlot.AMULET, "Skills necklace("))
			return true;
		//openBank();
		//todo: get amulet from bank
		return false;
	}

	private void openBank() {
		/*
		 * if(!banker.isOnScreen()) { camera.toEntity(banker); }
		 */
		while (!b.isOpen()) {
			if (b.isOpen())
				break;
			if (rand.nextInt(4) != 0) {
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
	}

	private void walkTo(Location l) throws InterruptedException {
		if (smeltLoc == SmeltLocation.EDGE) {
			if (l == Location.BANK) {
				// getWalking().walk(BANK_AREA_EDGE);
				getWalking().webWalk(BANK_AREA_EDGE);
			} else if (l == Location.FURNACE) {
				// getWalking().walk(FURNACE_AREA_EDGE);
				getWalking().webWalk(FURNACE_AREA_EDGE);
			}
		} else {

			if (l == Location.BANK) {
				getWalking().webWalk(BANK_AREA_AK);
			} else if (l == Location.FURNACE) {
				getWalking().webWalk(FURNACE_AREA_AK);
			}

			// if webwalking breaks:
			// note: does NOT open doors...
			/*
			 * if (l == Location.BANK) { if (myPlayer().getY() <= 3178)
			 * getWalking().walk(BANK_AREA_AK); else
			 * getWalking().walk(MIDWAY_AREA_AK);
			 * 
			 * } else if (l == Location.FURNACE) { if (myPlayer().getY() >=
			 * 3170) getWalking().walk(FURNACE_AREA_AK); else
			 * getWalking().walk(MIDWAY_AREA_AK); }
			 */
		}
	}

	private boolean isSmelting() {
		for (int i = 0; i < 10; i++) {
			if (myPlayer().getAnimation() == 827 || myPlayer().getAnimation() == 899)
				return true;
			try {
				sleep(random(200, 300));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private long getRemainingBars() {
		return bank.getAmount(2353);
	}

	private void checkLocation() {
		if (myPlayer().getY() >= 3473)
			return;
		smeltLoc = SmeltLocation.ALKHARID;
		log("Smelting in Al-Kharid!");
	}

	private boolean hasBarsInInv() {
		return getInventory().contains("Steel bar");
	}

	/*
	 * private int calculateProfit(){
	 * 
	 * }
	 */
	private void loadPrices() throws IOException {
		cBallPrice = gE.getOverallPrice(2);
		steelBarPrice = gE.getOverallPrice(2353);
		profitPerBar = (cBallPrice * 4) - steelBarPrice;
	}

	private int getCballAmount() {
		if (getInventory().contains("Cannonball"))
			return (int) getInventory().getAmount("Cannonball");
		return 0;
	}

	private int calcHourlyProfit(int profit) {
		return (int) (profit / ((System.currentTimeMillis() - startTime) / 3600000.0D));
	}

	private int calcBarsPerHour() {
		return (int) ((cBallsSmelted() / 4) / ((System.currentTimeMillis() - startTime) / 3600000.0D));
	}

	private int cBallsSmelted() {
		return getCballAmount() - cballStart;
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
		if (random(1, 30) > 15) {
			sleep(random(2000, 4000));
			return;
		}
		boolean backToInvy = false;
		switch (random(1, 30)) {
		case 1:
			camera.movePitch(random(10, 100));
			break;
		case 2:
			camera.movePitch(87 + (random(1, 99)));
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
			sleep(random(5000, 10000));
			break;
		}
		sleep(random(1300, 2400));
		if (backToInvy)
			tabs.open(Tab.INVENTORY); // RETURNS TO THE INVENTORY TAB AFTER
										// EVERY
										// ANTIBAN INSTANCE
	}

	@Override
	public void onPaint(Graphics2D g) {
		int smeltAmt = cBallsSmelted();
		int barsSmelted = smeltAmt / 4;
		int profit = barsSmelted * profitPerBar;
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString("CBalls made: " + smeltAmt + " [" + barsSmelted + " bars used, " + barsRemaining + " to go]", 10,
				280);
		g.drawString("Total profit: " + profit / 1000.0 + "k [" + calcHourlyProfit(profit) / 1000 + "k gp/hr, "
				+ calcBarsPerHour() + " bars per hour]", 10, 295);

	}

	@Override
	public void onStart() {
		log("Starting Cucky Cannonballs");
		checkLocation();
		try {
			loadPrices();
			log("Loaded prices! Steel bar=" + steelBarPrice + "gp, cBall=" + cBallPrice + "gp");
		} catch (IOException e) {
			e.printStackTrace();
		}
		cballStart = getCballAmount();
		startTime = System.currentTimeMillis();
		xpStart = getSkills().getExperience(Skill.SMITHING);
		log("Starting with " + xpStart + " xp");
	}

	@Override
	public void onExit() {
		log("Started with " + cballStart + " cballs, now at " + cballs + "... " + (cballs - cballStart)
				+ " cballs smelted in ~" + ((System.currentTimeMillis() - startTime) / 60000.0) + " mins!");
		log("Ending with xp: " + getSkills().getExperience(Skill.SMITHING) + " (Started w/ " + xpStart + ")");
		log("Exiting Cucky Cannonballs\\");
	}
}