import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(author = "m", info = "Stays logged in, in yo rimmy house", name = "Stay Logged In Cucky", version = 1, logo = ":^)")
public class main extends Script {
	private long startTime;
	private final int PORTAL_OUTSIDE = 15478;
	private String status = "Starting...";

	private enum State {
		CHILLIN, ENTERING_HOUSE;
	};

	private State getState() {
		if (objects.closest(PORTAL_OUTSIDE) != null)
			return State.ENTERING_HOUSE;
		return State.CHILLIN;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case CHILLIN:
			status = "Chillin'";
			log("Status: " + status);
			int rand = random(0, 10);
			int x = myPlayer().getX();
			int y = myPlayer().getY();
			switch (rand) {
			case 0:
				log("Walking -5,5 x and y");
				getWalking().walk(new Position(x + random(-5, 5), y + random(-5, 5), 0));
				break;
			case 1:
				String sentence = SentenceGen.getSentence();
				log("Saying: " + sentence);
				keyboard.typeString(sentence, true);
				break;
			case 2:
				log("Opening tab: skills");
				tabs.open(Tab.SKILLS);
				break;
			case 3:
				log("Opening tab: inventory");
				if (tabs.getOpen() != Tab.INVENTORY)
					tabs.open(Tab.INVENTORY);
				break;

			case 4:
				log("Opening tab: magic");
				tabs.open(Tab.MAGIC);
				break;

			case 5:
				log("Opening tab: emotes");
				tabs.open(Tab.EMOTES);
				break;

			case 6:
				log("Opening tab: inventory");
				tabs.open(Tab.INVENTORY);
				break;

			case 7:
				log("Opening tab: friends");
				tabs.open(Tab.FRIENDS);
				break;
			case 8:
				log("Saying: check out my house/welcome to my house");
				if (random(0, 4) == 1)
					keyboard.typeString("welcome to my house!", true);
				else
					keyboard.typeString("check out my house", true);
				break;
			case 9:
				AntiBan();
				break;
			default:
				log("Walking -2,2 x and y");
				getWalking().walk(new Position(x + random(-2, 2), y + random(-2, 2), 0));
				break;
			}
			sleep(random(10000, 20000));
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
					dialogues.selectOption(1);
					sleep(random(1500, 2000));
				}
			}
			break;
		}
		return random(100, 300);
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.drawString(ft(System.currentTimeMillis() - startTime), 10, 265);
		g.drawString("Status: " + status, 10, 280);

	}

	@Override
	public void onStart() {
		log("Starting Stay Logged In");
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onExit() {
		log("Exiting Stay Logged In\\");
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