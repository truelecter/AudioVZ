package truelecter.iig;

import java.io.File;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import truelecter.iig.screen.*;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;

import com.badlogic.gdx.Application.ApplicationType;
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
            if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
                ConfigHandler.width = Gdx.graphics.getWidth();
                ConfigHandler.height = Gdx.graphics.getHeight();
                Gdx.input.setCatchBackKey(true);
            } else {
                ConfigHandler.width = s.get("width", int.class, 900);
                ConfigHandler.height = s.get("height", int.class, 600);
            }
            ConfigHandler.volume = s.get("volume", float.class, 0.1f);
            ConfigHandler.lastFileManagerPath = s.get("lastFMPath", String.class, null);
            ConfigHandler.skinOrigPath = s.get("skin", String.class, null);
            if (ConfigHandler.skinOrigPath != null) {
                ConfigHandler.skinPath = ConfigHandler.skinOrigPath.replace("!INTERNAL!",
                        Gdx.files.getLocalStoragePath()).replace("!EXTERNAL!", Gdx.files.getExternalStoragePath());
            } else {
                ConfigHandler.skinPath = null;
            }
        } catch (Exception e) {
            ConfigHandler.width = 900;
            ConfigHandler.height = 600;
            ConfigHandler.volume = 0.1f;
            ConfigHandler.lastFileManagerPath = Gdx.files.getExternalStoragePath();
            ConfigHandler.skinPath = Gdx.files.internal("data/228/test.skn").path();
            System.out.println("Config not found. Using default values");
            System.out.println("Watched in " + Gdx.files.local("data/config.ini").file().getAbsolutePath());
            e.printStackTrace();
        }
        Gdx.input.setInputProcessor(new GlobalInputProcessor());

        Gdx.graphics.setDisplayMode(ConfigHandler.width, ConfigHandler.height, false);
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
            s.add("lastFMPath", Util.convertPath(ConfigHandler.lastFileManagerPath));
            s.add("skin", ConfigHandler.skinOrigPath);
            File f = Gdx.files.local("data/config.ini").file();
            config.store(f);
            System.out.println("Config stored to " + f.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Config wasn't saved.");
            e.printStackTrace();
        }
    }
}
