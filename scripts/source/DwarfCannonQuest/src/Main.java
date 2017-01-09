import java.awt.Graphics2D;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Completes Dwarf Cannon for j", name = "!!!Dwarf cannon quest", version = 1, logo = ":^)")
public class Main extends Script {
	// private final Area OUTSIDE_QUEST_START = new Area(new Position(2565,
	// 3450, 0), new Position(2571, 3455, 0));
	private final Area NULODION_AREA = new Area(new Position(3008, 3452, 0), new Position(3014, 3454, 0));
	private final Area INSIDE_QUEST_START = new Area(new Position(2563, 3457, 0), new Position(2572, 3463, 0));
	private final Area WATCHTOWER_BOTTOM = new Area(new Position(2567, 3440, 0), new Position(2572, 3444, 0));
	// private final Area WATCHTOWER_TOP = new Area(new Position(2567, 3442, 2),
	// new Position(2571, 3444, 2));
	// private final Area WATCHTOWER_MID = new Area(new Position(2569, 3440, 1),
	// new Position(2571, 3444, 1));
	private final Area GOBLIN_ENTRANCE = new Area(new Position(2621, 3389, 0), new Position(2627, 3391, 0));
	// private final Area GOBLIN_CAVE_INSIDE = new Area(new Position(2567, 9794,
	// 0), new Position(2621, 9852, 0));
	// south-most point = 9794
	// north-most = 9852
	// west-most = 2567
	// east-most = 2621
	// use this var to check if inside the goblin cave
	private final Area GOBLIN_CAVE_CHILD_AREA = new Area(new Position(2568, 9848, 0), new Position(2571, 9851, 0));
	private final Area ICE_MOUNTAIN = new Area(new Position(3009, 3452, 0), new Position(3014, 3454, 0));

	private final Position POS_1 = new Position(2574, 3458, 0), POS_2 = new Position(2563, 3459, 0),
			POS_3 = new Position(2558, 3468, 0), POS_4 = new Position(2556, 3481, 0);
	private final int BROKEN_RAILING_1 = 15595, BROKEN_RAILING_2 = 15594, BROKEN_RAILING_3 = 15593,
			BROKEN_RAILING_4 = 15592, BROKEN_RAILING_5 = 15591, BROKEN_RAILING_6 = 15590, WATCHTOWER_LADDER = 16683,
			BROKEN_CANNON = 15597, AMMO_MOULD = 4;
	private final int CAPTAIN = 5191;
	private final int RAILING_ITEM = 14, TOOLKIT_ITEM = 1;

	private boolean questStarted = false, railingsFixed = false, remainsFound = false, kidFound = false, cannonFixed = false,
			gotMold = false;

	enum State {
		START_QUEST, FIX_RAILINGS, GET_REMAINS, FIND_KID, FIX_CANNON, GET_MOLD, EAT, TALK_CAPTAIN, FINISH_QUEST, TALKING_FAILSAFE;
	}
	
	private int mouldCount = 0;
	private int desiredMoulds = random(2,5);
	long startTime;

	private State getState() {
		if (getSkills().getDynamic(Skill.HITPOINTS) <= 5 && getInventory().contains("Trout")) {
			return State.EAT;
		} else if (dialogues.inDialogue()) {
			return State.TALKING_FAILSAFE;
		} else if (!questStarted) {
			return State.START_QUEST;
		} else if (!railingsFixed) {
			return State.FIX_RAILINGS;
		} else if (!remainsFound) {
			return State.GET_REMAINS;
		} else if (!kidFound) {
			return State.FIND_KID;
		} else if (!cannonFixed) {
			return State.FIX_CANNON;
		} else if (!gotMold) {
			return State.GET_MOLD;
		} else if (inventory.contains("Nulodion's notes") && (mouldCount >= desiredMoulds)) {
			return State.FINISH_QUEST;
		}
		return State.TALK_CAPTAIN;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case START_QUEST:
			log("Starting quest");
			if (!INSIDE_QUEST_START.contains(myPlayer()))
				getWalking().webWalk(INSIDE_QUEST_START);
			talkCaptain(2);
			questStarted = true;
			break;
		case FIX_RAILINGS:
			log("Fixing railings");
			fixRailings();
			sleep(random(2000, 3000));
			if (!inventory.contains("Railing")) {
				railingsFixed = true;
				talkCaptain();
			}
			break;
		case GET_REMAINS:
			log("Getting remains");
			getRemains();
			remainsFound = true;
			talkCaptain();
			break;
		case FIND_KID:
			log("Finding kid");
			findKid();
			kidFound = true;
			talkCaptain(1);
			break;
		case FIX_CANNON:
			log("Fixing cannon");
			fixCannon();
			cannonFixed = true;
			talkCaptain(-1);
			break;
		case GET_MOLD:
			log("Getting mold");
			goIceMountain();
		case FINISH_QUEST:
			log("Finishing quest");
			//if (ICE_MOUNTAIN.contains(myPlayer()))
				teleFishGuild();
			talkCaptain();
		case TALK_CAPTAIN:
			log("Talking captain");
			talkCaptain();
			break;
		case TALKING_FAILSAFE:
			log("Talking failsafe");
			spamContinue();
			break;
		case EAT:
			log("Healing!");
			getInventory().getItem("Trout").interact("Eat");
			break;

		}
		return random(100, 300);

	}

	private int getTeleItem(String item) {
		for (int i = 1; i < 7; i++) {
			if (inventory.contains(item + "(" + i + ")"))
				return i;
		}
		return -1;
	}

	private void teleFishGuild() throws InterruptedException {
		inventory.getItem("Skills necklace(" + getTeleItem("Skills necklace") + ")").interact("Rub");
		sleep(random(1000, 2000));
		dialogues.selectOption(1);
		sleep(random(4000, 5000));
	}

	private void teleEdge() throws InterruptedException {
		inventory.getItem("Amulet of glory(" + getTeleItem("Amulet of glory") + ")").interact("Rub");
		sleep(random(1000, 2000));
		dialogues.selectOption(1);
		sleep(random(4000, 5000));
	}

	private void goIceMountain() throws InterruptedException {

		if (INSIDE_QUEST_START.contains(myPlayer())) {
			teleEdge();
		}

		// getWalking().webWalk(new Position(3015, 3453, 0));

		/*
		 * while (!NULODION_AREA.contains(myPlayer())) { getWalking().walk(new
		 * Position(3015, 3453, 0)); sleep(random(1000, 2000)); if
		 * (!NULODION_AREA.contains(myPlayer()))
		 * objects.closest(3).interact("Open"); }
		 */

		getWalking().webWalk(NULODION_AREA);

		// while (!inventory.contains(AMMO_MOULD) && !dialogues.inDialogue())
		talkNulodion();

		gotMold = true;
	}

	private void talkNulodion() throws InterruptedException {
		for (int i = 0; i < desiredMoulds; i++) {
			npcs.closest(1400).interact("Talk-to");
			sleep(random(1000, 2000));
			while (!inventory.contains(AMMO_MOULD))
				spamContinue();
			if (inventory.contains(AMMO_MOULD))
				inventory.drop(AMMO_MOULD);
		}
		GroundItem ammoGround = null;
		while ((ammoGround = groundItems.closest(AMMO_MOULD)) != null) {
			ammoGround.interact("Take");
			sleep(random(100, 200));
			mouldCount = (int) inventory.getAmount(AMMO_MOULD);
		}
	}

	private void fixCannon() throws InterruptedException {
		if (widgets.get(409, 1) == null) {
			getInventory().getItem(TOOLKIT_ITEM).interact("Use");
			objects.closest(BROKEN_CANNON).interact("Use");
			sleep(random(1000, 2000));
		}
		RS2Widget TOOTHED = widgets.get(409, 1), PLIERS = widgets.get(409, 2), HOOKED = widgets.get(409, 3);
		RS2Widget CANNON_SPRING = widgets.get(409, 8), CANNON_HAMMER_GEAR = widgets.get(409, 9),
				CANNON_SAFETY_SWITCH = widgets.get(409, 7);
		PLIERS.interact("SELECT");
		CANNON_SAFETY_SWITCH.interact("Safety switch");
		sleep(random(3000, 4000));
		HOOKED.interact("SELECT");
		CANNON_SPRING.interact("Spring");
		sleep(random(3000, 4000));
		TOOTHED.interact("SELECT");
		CANNON_HAMMER_GEAR.interact("Gear");
		sleep(random(3000, 4000));
	}

	private void findKid() throws InterruptedException {
		getWalking().webWalk(GOBLIN_ENTRANCE);
		if (GOBLIN_ENTRANCE.contains(myPlayer()))
			objects.closest(2).interact("Enter");
		getWalking().webWalk(GOBLIN_CAVE_CHILD_AREA);
		if (GOBLIN_CAVE_CHILD_AREA.contains(myPlayer()))
			objects.closest(1).interact("Search");
		sleep(random(5000, 6000));
		spamContinue();
		getWalking().webWalk(new Position(2619, 9797, 0)); // go back to
															// entrance
		objects.closest(13).interact("Climb-over");
		sleep(random(2000, 3000));
	}

	private void getRemains() throws InterruptedException {
		getWalking().webWalk(WATCHTOWER_BOTTOM);
		if (!getInventory().contains(0)) {
			objects.closest(WATCHTOWER_LADDER).interact("Climb-up");
			sleep(random(2000, 3000));
			objects.closest(11).interact("Climb-up");
			sleep(random(2000, 3000));
			objects.closest(0).interact("Take");
			sleep(random(2000, 3000));
			while (dialogues.clickContinue())
				sleep(random(200, 500));
		} else if (getInventory().contains(0)) {
			if (myPlayer().getZ() != 0) {
				objects.closest(16679).interact("Climb-down");
				sleep(random(2000, 3000));
			}
		}
	}

	private void talkCaptain() throws InterruptedException {
		if (!INSIDE_QUEST_START.contains(myPlayer()))
			getWalking().webWalk(INSIDE_QUEST_START);
		while (!dialogues.inDialogue()) {
			npcs.closest(CAPTAIN).interact("Talk-to");
			sleep(random(1000, 2000));
		}
		while (dialogues.inDialogue())
			spamContinue();
	}

	private void talkCaptain(int selectOption) throws InterruptedException {
		if (!INSIDE_QUEST_START.contains(myPlayer()))
			getWalking().webWalk(INSIDE_QUEST_START);
		while (!dialogues.inDialogue()) {
			npcs.closest(CAPTAIN).interact("Talk-to");
			sleep(random(1000, 2000));
		}
		while (dialogues.inDialogue()) {
			spamContinue();
			if (dialogues.isPendingOption()) {
				dialogues.selectOption(selectOption);
				spamContinue();
			}
		}
		if (selectOption == -1) {
			sleep(random(5000, 10000));
			spamContinue();
			talkCaptain(2);
			// dialogues.selectOption(2);
			// sleep(random(2000, 3000));
			// spamContinue();
		}
	}

	private void spamContinue() throws InterruptedException {
		while (dialogues.clickContinue())
			sleep(random(200, 500));
	}

	private void fixRailings() throws InterruptedException {
		if (!getInventory().contains(RAILING_ITEM)) {
			log("No railings founds... Returning");
			return;
		}
		int amt = (int) getInventory().getAmount(RAILING_ITEM);
		if (!myPlayer().isAnimating() && !myPlayer().isMoving()) {
			if (amt >= 5) {
				getWalking().walk(POS_1);
				if (amt == 6)
					fixRailing(BROKEN_RAILING_1);
				else if (amt == 5)
					fixRailing(BROKEN_RAILING_2);
			} else if (amt >= 3) {
				if (amt == 4) {
					getWalking().walk(POS_2);
					fixRailing(BROKEN_RAILING_3);
				} else
					fixRailing(BROKEN_RAILING_4);
			} else if (amt == 2) {
				getWalking().walk(POS_3);
				fixRailing(BROKEN_RAILING_5);
			} else if (amt == 1) {
				getWalking().walk(POS_4);
				fixRailing(BROKEN_RAILING_6);
			}
		}

	}

	private void fixRailing(int railingId) throws InterruptedException {
		objects.closest(railingId).interact("Inspect");
		sleep(random(2500, 3500));
		while (dialogues.clickContinue())
			sleep(random(6000, 8000));
	}

	@Override
	public void onExit() {

	}

	@Override
	public void onStart() throws InterruptedException {
		// check for fally teleports, skills necklace, food, hammer
		// ^ don't implement this check; what if the necklace(s) don't have 4
		// charges?
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
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
