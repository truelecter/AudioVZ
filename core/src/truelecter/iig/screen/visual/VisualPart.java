package truelecter.iig.screen.visual;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface VisualPart {
    public float getHeight();
    public float getWidth();
    public void setWidth(float width);
    public void setLocation(float x, float y);
    public void setX(float x);
    public void setY(float y);
    public void render(SpriteBatch sb);
    public void dispose();
}
