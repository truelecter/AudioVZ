package truelecter.iig.screen.visual.menu;

import truelecter.iig.screen.visual.LabeledCheckbox;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Function;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Options extends Menu {

    private LabeledCheckbox pauseOnHide;

    public Options() {
        super(new Vector2(-ConfigHandler.width / 3, ConfigHandler.height), new Vector2(0,
                ConfigHandler.height), ConfigHandler.width / 3, ConfigHandler.height, new Texture(
                "data/FileManager/backgroundt.png"));
        pauseOnHide = new LabeledCheckbox("Pause music on hide", -1000, -1000, ConfigHandler.width / 3, new Texture(
                "data/checked.png"), new Texture("data/unchecked.png"), false, null, new Function() {
            public void toRun() {
                ConfigHandler.pauseOnHide = true;
            }
        }, new Function() {
            public void toRun() {
                ConfigHandler.pauseOnHide = false;
            }
        });
        updateRelativeness();
        background.setAlpha(0.1f);
    }

    public void render(SpriteBatch sb) {
        update();
        super.render(sb);
        pauseOnHide.render(sb);
    }

    protected void updateRelativeness() {
        pauseOnHide.setLocation(pos.x, ConfigHandler.height / 2 - pauseOnHide.getHeight());
        background.setPosition(pos.x, 0);
    }
}
