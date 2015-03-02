package truelecter.iig;

import java.io.File;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import truelecter.iig.screen.*;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.input.GlobalInputProcessor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class Main extends Game {
	private static Main instance;

	public static Main getInstance() {
		return instance;
	}

	public Main() {
		instance = this;
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				saveConfig();
			}
		}));
	}

	@Override
	public void create() {
		try {
			Ini config = new Ini(Gdx.files.local("data/config.ini").file());
			Ini.Section s = config.get("Main");
			ConfigHandler.width = s.get("width", int.class, 900);
			ConfigHandler.height = s.get("height", int.class, 600);
			ConfigHandler.volume = s.get("volume", float.class, 0.1f);
			ConfigHandler.lastFileManagerPath = s.get("lastFMPath", String.class, null);
			ConfigHandler.skinPath = s.get("skin", String.class, null);
		} catch (Exception e) {
			ConfigHandler.width = 900;
			ConfigHandler.height = 600;
			ConfigHandler.volume = 0.1f;
			ConfigHandler.lastFileManagerPath = null;
			ConfigHandler.skinPath = null;
			System.out.println("Config not found. Using default values");
			System.out.println("Watched in "+Gdx.files.local("data/config.ini").file().getAbsolutePath());
		}
		Gdx.input.setInputProcessor(new GlobalInputProcessor());
		try {
			setScreen(new FileManager(new File(ConfigHandler.lastFileManagerPath)));
		} catch (Exception e) {
			setScreen(new FileManager());
		}
	}

	public void saveConfig() {
		try {
			Ini config = new Ini();
			Section s = config.add("Main");
			s.add("width", ConfigHandler.width);
			s.add("height", ConfigHandler.height);
			s.add("volume", ConfigHandler.volume);
			s.add("lastFMPath", ConfigHandler.lastFileManagerPath);
			File f = Gdx.files.local("data/config.ini").file();
			config.store(f);
			System.out.println("Config stored to " + f.getAbsolutePath());
		} catch (Exception e) {
			System.out.println("Config wasn't saved.");
		}
	}
}
