import java.awt.Graphics2D;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.Bank;
import org.osbot.rs07.api.GrandExchange;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Levels up to 35 smith", name = "Cucky Smith", version = 1, logo = ":^)")

public class main extends Script {
	private final Area SMITH_AREA_VARROCK = new Area(new Position(3186, 3423, 0), new Position(3189, 3427, 0));
	private final Area BANK_AREA_VARROCK = new Area(new Position(3182, 3435, 0), new Position(3185, 3439, 0));
	private Bank b;
	private Random rand = new Random();

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case BANK:
			log("Bank case");
			bank();
			break;
		case SMITH:
			log("Smith case");
			startSmithing();
			break;
		case SMITHING:
			log("Smithing case");
			sleep(random(700, 1200));
			break;
		case WALKBANK:
			log("Walk bank case");
			walkTo(BANK_AREA_VARROCK);
			break;
		case WALKSMITH:
			log("Walk smith case");
			walkTo(SMITH_AREA_VARROCK);
			break;
		case DONE:
			log("Done! Gratz on 35 smith!Walking to GE and logging out!");
			getWalking().webWalk(new Position(3164, 3483, 0));
			stop(true);
		}
		return random(100, 300);
	}

	private void bank() {
		b = getBank();
		openBank();

		b.depositAllExcept("Hammer", "Iron bar", "Bronze bar", "Steel bar");

		if (!b.contains("Steel bar") && !b.contains("Iron bar")) {
			log("Out of steel and iron bars! Exiting script.");
			stop(true);
		}

		if (!getInventory().contains("Hammer")) {
			if (b.contains("Hammer"))
				b.withdraw("Hammer", 1);
			else {
				log("No hammer?");
				stop(true);
			}
		}

		withdrawBars();

		if (rand.nextInt(5) == 0)
			b.close();
	}

	private void withdrawBars() {
		int smithLvl = getSmithLevel();
		if (smithLvl < 15)
			b.withdrawAll("Bronze bar");
		else if (smithLvl < 30)
			b.withdrawAll("Iron bar");
		else
			b.withdrawAll("Steel bar");
	}

	private int getSmithLevel() {
		return getSkills().getDynamic(Skill.SMITHING);
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
			while (myPlayer().isMoving())
				try {
					sleep(random(50, 75));
					;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
	}

	private void startSmithing() throws InterruptedException {
		objects.closest("Anvil").interact("Smith");
		sleep(random(1500, 2000));
		if (widgets.isVisible(312)) {
			if (rand.nextInt(6) == 0) {
				smithWhat().interact("Smith X");
				sleep(random(500, 700));
				switch (rand.nextInt(5)) {
				case 0:
					keyboard.typeString("9999", true);

					break;
				case 1:
					keyboard.typeString("9999", true);

					break;
				case 2:
					keyboard.typeString("999999", true);

					break;
				case 3:
					keyboard.typeString("111", true);

					break;
				case 4:
					keyboard.typeString("5555", true);

					break;
				}
			}
			smithWhat().interact("Smith 10");
		}
		sleep(random(1000, 2000));
		// }

	}

	private State getState() {
		if (getSmithLevel() >= 35)
			return State.DONE;

		if (BANK_AREA_VARROCK.contains(myPlayer())) {
			if (hasBars())
				return State.WALKSMITH;
			else
				return State.BANK;
		} else if (SMITH_AREA_VARROCK.contains(myPlayer())) {
			if (isSmithing())
				return State.SMITHING;
			else if (hasBars())
				return State.SMITH;
			else
				return State.WALKBANK;
		}

		return State.WALKBANK;
	}

	private void walkTo(Area a) {
		if (a == SMITH_AREA_VARROCK)
			getWalking().walk(SMITH_AREA_VARROCK);
		else
			getWalking().walk(BANK_AREA_VARROCK);
	}

	private boolean isSmithing() {
		for (int i = 0; i < 10; i++) {
			if (dialogues.inDialogue()) {
				log("Gz on " + getSkills().getDynamic(Skill.SMITHING) + " smith");
				try {
					sleep(random(500, 1000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (random(0, 10) == 3) {
					dialogues.clickContinue();
					try {
						sleep(random(250, 400));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
			if (myPlayer().getAnimation() == 898)
				return true;
			try {
				sleep(random(200, 300));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private RS2Widget smithWhat() {
		int smithLvl = getSmithLevel();
		if (smithLvl < 15 && smithLvl >= 5)
			return widgets.get(312, 4, 2);
		else if (smithLvl < 30 && smithLvl >= 20)
			return widgets.get(312, 4, 2);
		return widgets.get(312, 2, 2);
	}

	private boolean hasBars() {
		if (getInventory().getEmptySlots() == 13)
			return false;
		return getInventory().contains("Steel bar") || getInventory().contains("Iron bar")
				|| getInventory().contains("Bronze bar");
	}

	private boolean hasHammer() {
		return getInventory().contains("Hammer");
	}

	private enum State {
		BANK, SMITH, SMITHING, WALKBANK, WALKSMITH, DONE;
	};

	private long startTime;

	public void onStart() {
		if (myPlayer().getX() <= 3182)
			getWalking().webWalk(BANK_AREA_VARROCK);
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onExit() {
		log("Finished in " + ft(System.currentTimeMillis() - startTime));
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString(skills.getDynamic(Skill.SMITHING) + "/35", 10, 280);
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
