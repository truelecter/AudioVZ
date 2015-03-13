package truelecter.iig.screen.visual.menu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Menu {
    public static final int PARTS_PADDING = 20;
    public static final int DELIMITER_HEIGHT = 40;

    protected Vector2 pos;
    protected Vector2 begPos;
    protected Vector2 toPos;
    protected Vector2 endPos;
    protected float width;
    protected float height;
    protected Sprite background;
    protected boolean isOpen = false;

    public Menu(Vector2 pos, Vector2 toPos, float width, float height, Texture background) {
        this.pos = new Vector2(pos);
        this.begPos = new Vector2(pos);
        this.toPos = new Vector2(pos);
        this.endPos = new Vector2(toPos);
        this.width = width;
        this.height = height;
        this.background = new Sprite(background);
        this.background.setSize(width, height);
    }

    public void update() {
        pos = pos.lerp(toPos, 0.2f);
        updateRelativeness();
    }

    public void open() {
        toPos = endPos;
        isOpen = true;
    }

    public void close() {
        toPos = begPos;
        isOpen = false;
    }

    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    protected abstract void updateRelativeness();

    public void render(SpriteBatch s) {
        background.draw(s);
    }
}
