package truelecter.iig.screen.visual;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Function;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
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
    protected int priority = 89;

    public Button(Texture texture, float x, float y, float width, float height, Function onClick) {
        this.skin = new Sprite(texture);
        this.skin.setPosition(x, y);
        this.origHeight = height;
        this.origWidth = width;
        this.skin.setSize(width, height);
        this.onClick = onClick;
        this.setX(x);
        this.setY(y);
        this.scale = 1;
        GlobalInputProcessor.register(this);
    }

    public Button(Texture texture, float width, float height, Function onClick) {
        this(texture, 0, 0, width, height, onClick);
    }

    public void changeSkin(Texture toChange) {
        this.changeSkin(toChange, toChange.getWidth(), toChange.getHeight());
    }

    public void changeSkin(Texture toChange, float width, float height) {
        this.skin = new Sprite(toChange);
        this.origWidth = width;
        this.origHeight = height;
        this.skin.setSize(width * this.scale, height * this.scale);
    }

    protected boolean checkIfClicked(float ix, float iy) {
        if ((ix > this.skin.getX()) && (ix < (this.skin.getX() + (this.origWidth * this.scale))))
            if ((iy > this.skin.getY()) && (iy < (this.skin.getY() + (this.scale * this.origHeight))))
                if (this.checkTransparency(this.skin.getTexture(), (int) ix, (int) iy)) {
                    this.click();
                    return true;
                }
        return false;
    }

    private boolean checkTransparency(Texture t, int x, int y) {
        TextureData td = t.getTextureData();
        if (!td.isPrepared())
            td.prepare();
        int a = td.consumePixmap().getPixel(x, y) << 8;
        System.out.println("a=" + a);
        return (a) > (255 * 0.95);
    }

    public void click() {
        if (this.onClick != null)
            this.onClick.toRun();
    }

    @Override
    public void dispose() {
        GlobalInputProcessor.remove(this);
    }

    public void draw(SpriteBatch sb) {
        this.skin.draw(sb);
    }

    public void drawCentered(SpriteBatch sb) {
        this.skin.setPosition(this.x - ((this.origWidth / 2) * this.scale), this.y
                - ((this.origHeight / 2) * this.scale));
        this.skin.draw(sb);
    }

    public void drawCentered(SpriteBatch sb, float x, float y) {
        this.setLocation(x, y);
        this.drawCentered(sb);
    }

    @Override
    public float getHeight() {
        return this.origHeight * this.scale;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public Sprite getSprite() {
        return this.skin;
    }

    @Override
    public float getWidth() {
        return this.origWidth * this.scale;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public void render(SpriteBatch sb) {
        this.draw(sb);
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPriority(int i) {
        this.priority = i;
    }

    public void setScale(double scale) {
        this.setScale((float) scale);
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.skin.setSize(this.origWidth * scale, this.origHeight * scale);
    }

    @Override
    public void setWidth(float width) {

    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return this.checkIfClicked(screenX, Math.abs(screenY - ConfigHandler.height));
    }
}