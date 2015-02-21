package truelecter.iig.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import truelecter.iig.Main;

public class DesktopLauncher {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "AudioVZ";
		cfg.backgroundFPS = 40;
		cfg.foregroundFPS = 60;
		cfg.width = 910;
		cfg.height = 700;
		cfg.resizable = false;
		new LwjglApplication(new Main(cfg.width, cfg.height, args), cfg);
	}
}
