package truelecter.iig.screen.visual.menu;

import truelecter.iig.screen.visual.LabeledCheckbox;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Function;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Options extends Menu {

    private LabeledCheckbox pauseOnHide;
    private LabeledCheckbox autoPlay;
    private LabeledCheckbox useShaders;
    private LabeledCheckbox scaleBackground;
    private LabeledCheckbox offsetAngle;

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
        useShaders = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Use shaders", -1000, -1000,
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
        scaleBackground = new LabeledCheckbox(FontManager.getOptionLabelFont(), "Scale background on offsets", -1000, -1000,
                ConfigHandler.width / 3, new Texture("data/icons/checked.png"),
                new Texture("data/icons/unchecked.png"), ConfigHandler.scaleBackground, null, new Function() {
                    public void toRun() {
                        ConfigHandler.scaleBackground = true;
                    }
                }, new Function() {
                    public void toRun() {
                        ConfigHandler.scaleBackground = false;
                    }
                }, ConfigHandler.height / 20, ConfigHandler.height / 20);
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
        updateRelativeness();
        background.setAlpha(0.5f);
    }

    public void render(SpriteBatch sb) {
        update();
        super.render(sb);
        pauseOnHide.render(sb);
        autoPlay.render(sb);
        useShaders.render(sb);
        scaleBackground.render(sb);
        offsetAngle.render(sb);
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
        background.setPosition(pos.x, 0);
    }

    public void dispose() {
        autoPlay.dispose();
        pauseOnHide.dispose();
        useShaders.dispose();
        scaleBackground.dispose();
        offsetAngle.dispose();
    }

}
