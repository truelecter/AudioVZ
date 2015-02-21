package truelecter.iig.screen.visual;

import com.badlogic.gdx.math.Vector2;

public class FadingText {
	private float factor;
	private Vector2 vec = new Vector2(0, 0), target = new Vector2(1, 0), zeroTarget = new Vector2(0, 0);
	private boolean wasShown = false, finished = false, waited = false;
	private long toStay, lastTime, toStayOrig;

	public FadingText(float factor, long toStay) {
		this.factor = factor;
		this.toStay = toStay;
		this.toStayOrig = toStay;
		this.lastTime = System.currentTimeMillis();
	}

	public void reset() {
		wasShown = false;
		finished = false;
		waited = false;
		lastTime = System.currentTimeMillis();
	}

	public void resetShowing() {
		finished = false;
		waited = false;
	}

	public void show() {
		if (finished) {
			reset();
		} else {
			resetShowing();
		}
		toStay = toStayOrig;
	}

	public float getFadeX() {
		if (!finished) {
			Vector2 t = !wasShown || !waited ? target : zeroTarget;
			vec = vec.lerp(t, factor);
			if (Math.abs(t.x - vec.x) < 0.01f) {
				if (!wasShown) {
					wasShown = true;
				} else if (!waited) {
					toStay += lastTime - System.currentTimeMillis();
					lastTime = System.currentTimeMillis();
					if (toStay < 1) {
						waited = true;
					}
				} else {
					if (vec.x < 0.001) {
						finished = true;
					}
				}
			}
			return vec.x;
		} else {
			return 0;
		}
	}

	public boolean finished() {
		return finished;
	}

}
