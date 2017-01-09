import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Splashes for cucky", name = "CuckySplash", version = 1, logo = ":^)")
public class main extends Script {
	private final Area CASTING_AREA = new Area(new Position(3212, 3472, 0), new Position(3217, 3476, 0));
	private long startTime;
	private String status;
	private boolean hasSupplies = true;

	private enum State {
		CASTING, WALKING_TO_MONK, WAITING, EXITING;
	}

	private State getState() {
		if (!CASTING_AREA.contains(myPlayer()))
			return State.WALKING_TO_MONK;
		else if (isCasting())
			return State.CASTING;
		else if (getMagicLevel() >= 40 || !hasSupplies)
			return State.EXITING;
		return State.WAITING;

	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case CASTING:
			status = "Casting";
			log("Status: " + status);
			sleep(random(100, 200));
			break;
		case WALKING_TO_MONK:
			status = "Walking to monk";
			log("Status: " + status);
			getWalking().webWalk(CASTING_AREA);
			sleep(random(100, 200));
			break;
		case WAITING:
			status = "Waiting";
			log("Status: " + status);
			castSpell();
			sleep(random(100, 200));
			break;
		case EXITING:
			status = "Exiting";
			getWalking().walk(Banks.GRAND_EXCHANGE);
			stop();
		}
		return 0;
	}

	@Override
	public void onStart() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onExit() {
		log("Exiting with magic level " + getMagicLevel());
	}

	private void castSpell() {
		if (tabs.getOpen() != Tab.MAGIC)
			tabs.open(Tab.MAGIC);

		checkSupplies();

		if (getMagicLevel() < 3) {
			magic.castSpell(Spells.NormalSpells.WIND_STRIKE);
		} else if (getMagicLevel() < 11) {
			magic.castSpell(Spells.NormalSpells.CONFUSE);
		} else if (getMagicLevel() < 19) {
			magic.castSpell(Spells.NormalSpells.WEAKEN);
		} else if (getMagicLevel() < 40) {
			magic.castSpell(Spells.NormalSpells.CURSE);
		}

		// 2886 - monk of zammy
		npcs.closest(2886).interact("Cast");
	}

	private void checkSupplies() {
		hasSupplies = inventory.contains("Body rune") && inventory.contains("Earth rune")
				&& inventory.contains("Water rune");
	}

	private boolean isCasting() {
		for (int i = 0; i < 5; i++) {
			if (myPlayer().getAnimation() != -1)
				return true;
			try {
				sleep(random(50, 100));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	private int getMagicLevel() {
		return skills.getDynamic(Skill.MAGIC);
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString(getMagicLevel() + "/40", 10, 280);
		g.drawString("Status: " + status, 10, 295);
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
