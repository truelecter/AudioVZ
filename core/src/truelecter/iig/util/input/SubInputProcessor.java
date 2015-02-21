package truelecter.iig.util.input;

public interface SubInputProcessor {
	public boolean keyDown(int keycode);

	public boolean keyUp(int keycode);

	public boolean keyTyped(char character);

	public boolean touchDown(int screenX, int screenY, int pointer, int button);

	public boolean touchUp(int screenX, int screenY, int pointer, int button);

	public boolean touchDragged(int screenX, int screenY, int pointer);

	public boolean mouseMoved(int screenX, int screenY);

	public boolean scrolled(int amount);
	
}
