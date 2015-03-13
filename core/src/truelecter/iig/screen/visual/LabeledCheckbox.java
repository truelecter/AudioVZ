package truelecter.iig.screen.visual;

import truelecter.iig.util.FontManager;
import truelecter.iig.util.Function;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class LabeledCheckbox implements VisualPart {

    public static final int SIDES_PADDING = 20;

    private String label;
    private Checkbox checkbox;
    private float width;
    private Vector2 pos;

    public LabeledCheckbox(String label, float x, float y, float width, Texture checked, Texture unchecked,
            boolean isChecked, Function onClick, Function onCheck, Function onUncheck) {
        this.label = label;
        this.width = width;
        pos = new Vector2();
        this.checkbox = new Checkbox(x, y, unchecked.getWidth(), unchecked.getHeight(), checked, unchecked, isChecked,
                onClick, onCheck, onUncheck);
        setLocation(x, y);
    }

    @Override
    public float getHeight() {
        return Math.max(FontManager.getOptionLabelFont().getBounds(label).height, checkbox.getHeight());
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public void setLocation(float x, float y) {
        pos.x = x;
        pos.y = y;
        checkbox.setLocation(x + width - SIDES_PADDING - checkbox.getWidth(), y);
    }

    @Override
    public void render(SpriteBatch sb) {
        checkbox.drawCentered(sb, pos.x + width - SIDES_PADDING - checkbox.getWidth() / 2, pos.y - checkbox.getHeight()
                / 2);
        FontManager.getOptionLabelFont().draw(sb, label, pos.x + SIDES_PADDING, pos.y);
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
