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
    public static final boolean DEBUG = true;
    public final String VERSION = "0.0.3 pre-alpha";
    public static AndroidApplication aa = null;
    private final File toRun;

    public static Main getInstance() {
        return instance;
    }

    public Main() {
        this(null);
    }

    public Main(AndroidApplication aa, File f) {
        this(f);
        Main.aa = aa;
    }

    public Main(File f) {
        instance = this;
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveConfig();
            }
        }));
        if (f != null && (aa != null || (f.exists() && f.isFile() && f.getAbsolutePath().endsWith(".mp3"))))
            toRun = f;
        else
            toRun = null;
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
            ConfigHandler.pauseOnHide = s.getBoolean("Main", "pauseOnHide", false);
            ConfigHandler.autoPlay = s.getBoolean("Main", "autoPlay", false);
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
            ConfigHandler.pauseOnHide = false;
            ConfigHandler.autoPlay = false;
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
        if (toRun != null) {
            Logger.i("File param is not null! Path: " + toRun.getAbsolutePath());
            setScreen(new Loading(2000l, toRun, null));
            return;
        } else {
            Logger.i("File param is null!");
        }
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
            config.put("Main", "width", ConfigHandler.width);
            config.put("Main", "height", ConfigHandler.height);
            config.put("Main", "volume", ConfigHandler.volume);
            config.put("Main", "lastFMPath", Util.convertPath(ConfigHandler.lastFileManagerPath));
            config.put("Main", "skin", ConfigHandler.skinOrigPath);
            config.put("Main", "pauseOnHide", ConfigHandler.pauseOnHide);
            config.put("Main", "autoPlay", ConfigHandler.autoPlay);
            File f = Gdx.files.local("data/config.ini").file();
            config.write(f);
            System.out.println("Config stored to " + f.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Config wasn't saved.");
            e.printStackTrace();
        }

    }
}
