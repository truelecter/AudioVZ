package truelecter.iig.screen.visual;

import truelecter.iig.util.Function;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class LabeledCheckbox implements VisualPart {

    public static final int SIDES_PADDING = 20;

    private String label;
    private Checkbox checkbox;
    private float width;
    private Vector2 pos;
    private BitmapFont font;

    public LabeledCheckbox(BitmapFont font, String label, float x, float y, float width, Texture checked,
            Texture unchecked, boolean isChecked, Function onClick, Function onCheck, Function onUncheck,
            float checkboxWidth, float checkboxHeight) {
        this.label = label;
        this.width = width;
        pos = new Vector2();
        this.checkbox = new Checkbox(x, y, checkboxWidth, checkboxHeight, checked, unchecked, isChecked, onClick,
                onCheck, onUncheck);
        setLocation(x, y);
        this.font = font;
    }

    @Override
    public float getHeight() {
        return Math.max(font.getBounds(label).height, checkbox.getHeight());
    }

    private float getMinHeight() {
        return Math.min(font.getBounds(label).height, checkbox.getHeight());
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void setLocation(float x, float y) {
        pos.x = x;
        pos.y = y;
        checkbox.setLocation(x + width - SIDES_PADDING - checkbox.getWidth(), y - checkbox.getHeight() / 2);
    }

    @Override
    public void render(SpriteBatch sb) {
        checkbox.drawCentered(sb, pos.x + width - SIDES_PADDING - checkbox.getWidth() / 2, pos.y - checkbox.getHeight()
                / 2);
        font.draw(sb, label, pos.x + SIDES_PADDING, pos.y - (getHeight() - getMinHeight()) / 2);
    }

    @Override
    public void setX(float x) {
        setLocation(x, pos.y);
    }

    @Override
    public void setY(float y) {
        setLocation(pos.x, y);
    }

    @Override
    public void dispose() {
        checkbox.dispose();
    }

}
