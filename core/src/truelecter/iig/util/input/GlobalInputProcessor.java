package truelecter.iig.util.input;

import java.util.ArrayList;

import com.badlogic.gdx.InputProcessor;

public class GlobalInputProcessor implements InputProcessor {

	private static ArrayList<SubInputProcessor> processors;
	private static GlobalInputProcessor instance;

	public static GlobalInputProcessor getInstance() {
		return instance;
	}

	public GlobalInputProcessor() {
		processors = new ArrayList<SubInputProcessor>();
		instance = this;
	}

	private ArrayList<SubInputProcessor> getProcessors() {
		if (processors == null)
			processors = new ArrayList<SubInputProcessor>();
		return processors;
	}

	public void remove(SubInputProcessor s) {
		getProcessors().remove(s);
	}

	public void register(SubInputProcessor s) {
		getProcessors().add(s);
	}

	@Override
	public boolean keyDown(int keycode) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.keyDown(keycode);
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.keyUp(keycode);
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.keyTyped(character);
		}
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.touchDown(screenX, screenY, pointer, button);
		}
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.touchUp(screenX, screenY, pointer, button);
		}
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.touchDragged(screenX, screenY, pointer);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.mouseMoved(screenX, screenY);
		}
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		for (int i = 0; i < processors.size(); i++) {
			SubInputProcessor p = processors.get(i);
			if (p != null)
				p.scrolled(amount);
		}
		return true;
	}
}
