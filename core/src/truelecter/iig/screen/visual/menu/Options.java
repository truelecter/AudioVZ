package truelecter.iig.screen.visual.menu;

import java.util.ArrayList;

import truelecter.iig.screen.visual.LabeledCheckbox;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Function;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Options extends Menu implements SubInputProcessor {

    private ArrayList<LabeledCheckbox> checkboxes = new ArrayList<LabeledCheckbox>();

    private LabeledCheckbox pauseOnHide;
    private LabeledCheckbox autoPlay;
    private LabeledCheckbox useShaders;
    private LabeledCheckbox scaleBackground;
    private LabeledCheckbox offsetAngle;
    private LabeledCheckbox showButton;

    public Options() {
        super(new Vector2(-ConfigHandler.width / 3, ConfigHandler.height), new Vector2(0, ConfigHandler.height),
                ConfigHandler.width / 3, ConfigHandler.height, new Texture("data/FileManager/backgroundt.png"));
        pauseOnHide = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Pause music on hide", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.pauseOnHide, null, new Function() {
                    public void toRun() {
                        ConfigHandler.pauseOnHide = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.pauseOnHide = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(pauseOnHide);
        autoPlay = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Playlist mode", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.autoPlay, null, new Function() {
                    public void toRun() {
                        ConfigHandler.autoPlay = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.autoPlay = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(autoPlay);
        useShaders = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Anaglyph", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.useShaders, null, new Function() {
                    public void toRun() {
                        ConfigHandler.useShaders = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.useShaders = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(useShaders);
        scaleBackground = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Scale background on offsets", -1000,
                -1000, ConfigHandler.width / 3, new Texture("data/icons/checked.png"), new Texture(
                        "data/icons/unchecked.png"), ConfigHandler.scaleBackground, null, new Function() {
                    public void toRun() {
                        ConfigHandler.scaleBackground = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.scaleBackground = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(scaleBackground);
        offsetAngle = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Bars angle change", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.offsetAngle, null, new Function() {
                    public void toRun() {
                        ConfigHandler.offsetAngle = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.offsetAngle = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(offsetAngle);
        showButton = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Show buttons on visualizer", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.showButtons, null, new Function() {
                    public void toRun() {
                        ConfigHandler.showButtons = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.showButtons = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
        checkboxes.add(showButton);
        updateRelativeness();
        background.setAlpha(0.5f);
        updatePreferableSize();
        pos.x = -width;
        GlobalInputProcessor.register(this);
    }

    public void render(SpriteBatch sb) {
        if (checkboxes != null && checkboxes.size() > 0) {
            super.update();
            super.render(sb);
            pauseOnHide.render(sb);
            autoPlay.render(sb);
            useShaders.render(sb);
            scaleBackground.render(sb);
            offsetAngle.render(sb);
            showButton.render(sb);
        }
    }

    protected void updatePreferableSize() {
        float minWidth = Float.MIN_VALUE;
        for (LabeledCheckbox c : checkboxes) {
            if (c != null && c.getPreferableWidth() > minWidth) {
                minWidth = c.getPreferableWidth();
            }
        }
        for (LabeledCheckbox c : checkboxes) {
            if (c != null) {
                c.setWidth(minWidth);
            }
        }
        this.setWidth(minWidth);
        pos.x = -width;
    }

    protected void updateRelativeness() {
        float y = 3 * ConfigHandler.height / 4 - pauseOnHide.getHeight();
        pauseOnHide.setLocation(pos.x, y);
        y -= autoPlay.getHeight() + ConfigHandler.height / 200;
        autoPlay.setLocation(pos.x, y);
        y -= useShaders.getHeight() + ConfigHandler.height / 200;
        useShaders.setLocation(pos.x, y);
        y -= scaleBackground.getHeight() + ConfigHandler.height / 200;
        scaleBackground.setLocation(pos.x, y);
        y -= offsetAngle.getHeight() + ConfigHandler.height / 200;
        offsetAngle.setLocation(pos.x, y);
        y -= showButton.getHeight() + ConfigHandler.height / 200;
        showButton.setLocation(pos.x, y);
        background.setPosition(pos.x, 0);
    }

    public void dispose() {
        if (checkboxes != null) {
            for (LabeledCheckbox l : checkboxes) {
                if (l != null) {
                    l.dispose();
                    l = null;
                }
            }
            checkboxes.clear();
            checkboxes = null;
        }
        GlobalInputProcessor.remove(this);
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
        if (screenX <= pos.x + width) {
            return true;
        }
        return false;
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
    public int getPriority() {
        return 90;
    }

}
