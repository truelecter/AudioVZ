package truelecter.iig;

import java.io.File;

import truelecter.iig.screen.*;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Ini;
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
        Logger.log("", "**************************************************************************", null);
        Logger.i("Started AudioVZ v " + VERSION);
        if (!Gdx.files.isExternalStorageAvailable()) {
            Logger.w("External storage is not available", null);
        }
        try {
            File configFile = Gdx.files.local("data/config.ini").file();
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            Ini s = new Ini(configFile);
            ConfigHandler.width = s.getInt("Main", "width", 900);
            ConfigHandler.height = s.getInt("Main", "height", 600);
            ConfigHandler.volume = (float) s.getDouble("Main", "volume", 0.1);
            ConfigHandler.lastFileManagerPath = s.getString("Main", "lastFMPath", Gdx.files.getExternalStoragePath());
            ConfigHandler.skinOrigPath = s.getString("Main", "skin", null);
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
            ConfigHandler.skinPath = Gdx.files.local("data/228/test.skn").path();
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
            config.put("Main","width", ConfigHandler.width);
            config.put("Main","height", ConfigHandler.height);
            config.put("Main","volume", ConfigHandler.volume);
            config.put("Main","lastFMPath", Util.convertPath(ConfigHandler.lastFileManagerPath));
            config.put("Main","skin", ConfigHandler.skinOrigPath);
            File f = Gdx.files.local("data/config.ini").file();
            config.write(f);
            System.out.println("Config stored to " + f.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Config wasn't saved.");
            e.printStackTrace();
        }

    }
}
