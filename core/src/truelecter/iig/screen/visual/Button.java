package truelecter.iig.screen.visual;

import truelecter.iig.util.Function;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button implements SubInputProcessor {

	private Sprite skin;
	private Function onClick;
	private float scale;
	private float origWidth;
	private float origHeight;
	private float x;
	private float y;

	public Button(Texture texture, float width, float height, Function onClick) {
		this(texture, 0, 0, width, height, onClick);
	}

	public Button(Texture texture, float x, float y, float width, float height, Function onClick) {
		skin = new Sprite(texture);
		skin.setPosition(x, y);
		origHeight = height;
		origWidth = width;
		skin.setSize(width, height);
		this.onClick = onClick;
		this.setX(x);
		this.setY(y);
		scale = 1;
		GlobalInputProcessor.getInstance().register(this);
	}

	private void checkIfClicked(float ix, float iy) {
		if (ix > skin.getX() && ix < skin.getX() + origWidth * scale) {
			if (iy > skin.getY() && iy < skin.getY() + scale * origHeight) {
				click();
			}
		}
	}

	public void drawCentered(SpriteBatch sb, float x, float y) {
		setLocation(x, y);
		drawCentered(sb);
	}

	public void drawCentered(SpriteBatch sb) {
		skin.setPosition(x - origWidth / 2 * scale, y - origHeight / 2 * scale);
		skin.draw(sb);
	}

	public void draw(SpriteBatch sb) {

	}

	public Sprite getSprite() {
		return skin;
	}

	public void changeSkin(Texture toChange, float width, float height) {
		skin = new Sprite(toChange);
		skin.setSize(width * scale, height * scale);
	}

	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void setScale(double scale) {
		setScale((float) scale);
	}

	public void setScale(float scale) {
		this.scale = scale;
		skin.setSize(origWidth * scale, origHeight * scale);
	}

	public void click() {
		onClick.toRun();
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	@Override
	public boolean keyDown(int keycode) {
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		checkIfClicked(screenX, screenY);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		return true;
	}

}