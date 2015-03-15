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
        updateRelativeness();
        background.setAlpha(0.5f);
    }

    public void render(SpriteBatch sb) {
        update();
        super.render(sb);
        pauseOnHide.render(sb);
        autoPlay.render(sb);
    }

    protected void updateRelativeness() {
        pauseOnHide.setLocation(pos.x, ConfigHandler.height / 3 - pauseOnHide.getHeight());
        autoPlay.setLocation(pos.x, 2 * ConfigHandler.height / 3 - autoPlay.getHeight());
        background.setPosition(pos.x, 0);
    }

    public void dispose() {
        autoPlay.dispose();
        pauseOnHide.dispose();
    }

}
