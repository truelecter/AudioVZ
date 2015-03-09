package truelecter.iig;

import java.io.File;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import truelecter.iig.screen.*;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class Main extends Game {
    private static Main instance;
    public final boolean DEBUG = true;
    public final String VERSION = "0.0.3 pre-alpha";
    public static AndroidApplication aa = null;

    public static Main getInstance() {
        return instance;
    }

    public Main(AndroidApplication aa) {
        this();
        Main.aa = aa;
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
        Logger.i("Started AudioVZ v " + VERSION);
        if (!Gdx.files.isExternalStorageAvailable()) {
            Logger.w("External storage is not available", null);
        }
        try {
            File configFile = Gdx.files.local("data/config.ini").file();
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            Ini config = new Ini(configFile);
            Ini.Section s = config.get("Main");
            ConfigHandler.width = s.get("width", int.class, 900);
            ConfigHandler.height = s.get("height", int.class, 600);
            ConfigHandler.volume = s.get("volume", float.class, 0.1f);
            ConfigHandler.lastFileManagerPath = s.get("lastFMPath", String.class, Gdx.files.getExternalStoragePath());
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
            Logger.w("Config not found. Using default values\n" + "Watched in "
                    + Gdx.files.local("data/config.ini").file().getAbsolutePath(), e);
        }
        if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS) {
            ConfigHandler.width = Gdx.graphics.getWidth();
            ConfigHandler.height = Gdx.graphics.getHeight();
        }
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(new GlobalInputProcessor());
        Gdx.graphics.setDisplayMode(ConfigHandler.width, ConfigHandler.height, false);
        try {
            setScreen(new FileManager(new File(ConfigHandler.lastFileManagerPath)));
        } catch (Exception e) {
            Logger.w("Cannot create FileManager instance with last path", e);
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
