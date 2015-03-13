package truelecter.iig.screen.visual;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Function;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Button implements SubInputProcessor, VisualPart {

    protected Sprite skin;
    protected Function onClick;
    protected float scale;
    protected float origWidth;
    protected float origHeight;
    protected float x;
    protected float y;

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
        GlobalInputProcessor.register(this);
    }

    public void dispose() {
        GlobalInputProcessor.remove(this);
    }

    protected boolean checkIfClicked(float ix, float iy) {
        if (ix > skin.getX() && ix < skin.getX() + origWidth * scale) {
            if (iy > skin.getY() && iy < skin.getY() + scale * origHeight) {
                click();
                return true;
            }
        }
        return false;
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
        skin.draw(sb);
    }

    public Sprite getSprite() {
        return skin;
    }

    public void changeSkin(Texture toChange) {
        changeSkin(toChange, toChange.getWidth(), toChange.getHeight());
    }

    public void changeSkin(Texture toChange, float width, float height) {
        skin = new Sprite(toChange);
        origWidth = width;
        origHeight = height;
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
        if (onClick != null)
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
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return checkIfClicked(screenX, Math.abs(screenY - ConfigHandler.height));
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public float getHeight() {
        return origHeight * scale;
    }

    @Override
    public float getWidth() {
        return origWidth * scale;
    }

    @Override
    public void render(SpriteBatch sb) {
        draw(sb);
    }

    @Override
    public int getPriority() {
        return 98;
    }
}