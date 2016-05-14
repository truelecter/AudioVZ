package truelecter.iig;

import java.io.File;
import java.io.IOException;

import truelecter.iig.screen.FileManager;
import truelecter.iig.screen.Loading;
import truelecter.iig.screen.visual.Skin;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Ini;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

/**
 * Main visualizer class. Reads config, and some base actions to start the
 * application correctly
 * 
 * @author _TrueLecter_
 */
public class Main extends Game {
    private static Main instance;
    public static final boolean DEBUG = true;
    /**
     * Used by {@link FileManager} to build song list on Android
     */
    public static AndroidApplication aa = null;

    /**
     * @return Instance of the main class
     */
    public static Main getInstance() {
        return instance;
    }

    public final String VERSION = "0.1.2 alpha";

    /**
     * File to play
     */
    private File toRun = null;

    /**
     * File to set as background
     */
    private File toSetAsBackground = null;

    /**
     * Default constructor. We'll start from file manager screen.
     */
    public Main() {
        this(null);
    }

    /**
     * Constructor used by AndroidLauncher
     * 
     * @param aa
     *            - android application instance. Used in {@link FileManager}
     * @param f
     *            - file to open
     */
    public Main(AndroidApplication aa, File f) {
        this(f);
        Main.aa = aa;
    }

    /**
     * Main constructor. Sets <b>instance<b> variable and basically checks our
     * file.
     * 
     * @param f
     *            - file to play. If <b>null</b> open file manager
     */
    public Main(File f) {
        instance = this;
        if ((f != null) && (f.exists() && f.isFile())) {
            if (f.getAbsolutePath().endsWith(".mp3"))
                this.toRun = f;
            if (f.getAbsolutePath().endsWith(".jpg") || f.getAbsolutePath().endsWith(".jpeg")
                    || f.getAbsolutePath().endsWith(".png") || f.getAbsolutePath().endsWith(".bmp"))
                this.toSetAsBackground = f;
        }
    }

    @Override
    public void create() {
        Logger.log("", "**************************************************************************", null);
        Logger.i("Started AudioVZ v " + this.VERSION);
        if (!Gdx.files.isExternalStorageAvailable())
            Logger.w("External storage is not available", null);
        try {
            File configFile = Gdx.files.local("data/config.ini").file();
            if (!configFile.exists())
                configFile.createNewFile();
            Ini s = new Ini(configFile);
            ConfigHandler.width = s.getInt("Main", "width", 900);
            ConfigHandler.height = s.getInt("Main", "height", 600);
            ConfigHandler.volume = (float) s.getDouble("Main", "volume", 0.1);
            ConfigHandler.lastFileManagerPath = s.getString("Main", "lastFMPath", Gdx.files.getExternalStoragePath());
            ConfigHandler.skinOrigPath = s.getString("Main", "skin", null);
            ConfigHandler.pauseOnHide = s.getBoolean("Main", "pauseOnHide", false);
            ConfigHandler.autoPlay = s.getBoolean("Main", "autoPlay", false);
            ConfigHandler.scaleBackground = s.getBoolean("Main", "scaleBackground", false);
            ConfigHandler.offsetAngle = s.getBoolean("Main", "offsetAngle", false);
            ConfigHandler.useShaders = s.getBoolean("Main", "anaglyph", false);
            if (ConfigHandler.skinOrigPath != null)
                ConfigHandler.skinPath = ConfigHandler.skinOrigPath.replace("!INTERNAL!",
                        Gdx.files.getLocalStoragePath()).replace("!EXTERNAL!", Gdx.files.getExternalStoragePath());
            else
                ConfigHandler.skinPath = null;
        } catch (Exception e) {
            // If config reading ends with error, set default values
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
        // If running on Android, set app width and height to device width and
        // height. Also, set volume ratio to 1
        if (Gdx.app.getType() == ApplicationType.Android) {
            ConfigHandler.width = Gdx.graphics.getWidth();
            ConfigHandler.height = Gdx.graphics.getHeight();
            ConfigHandler.volume = 1;
        }
        // Add back key hook
        Gdx.input.setCatchBackKey(true);
        // Change input processor to our new instance
        Gdx.input.setInputProcessor(new GlobalInputProcessor());
        // Set window height and width
        Gdx.graphics.setDisplayMode(ConfigHandler.width, ConfigHandler.height, false);
        // Set background wallpaper if any is provided
        if (this.toSetAsBackground != null)
            try {
                new Skin(new File(ConfigHandler.skinPath)).setBackground(this.toSetAsBackground.getAbsolutePath())
                        .save();
            } catch (IOException e) {
                Logger.e("Unable to change skin background", e);
            }
        // Run file if any is set by constructor
        if ((this.toRun != null) && (aa != null)) {
            Logger.i("File param is not null! Path: " + this.toRun.getAbsolutePath());
            this.setScreen(new Loading(2000l, this.toRun, null));
            return;
        } else
            Logger.i("File param is null!");
        // If file was null, then set screen to FileManager instance
        try {
            this.setScreen(new FileManager(new File(ConfigHandler.lastFileManagerPath)));
        } catch (Exception e) {
            // If we can't open last directory
            Logger.w("Can not create FileManager instance with last path", e);
            this.setScreen(new FileManager());
        }
    }

    /**
     * Just save config
     */
    @Override
    public void dispose() {
        this.saveConfig();
    }

    /**
     * Used to store config before application disposes
     */
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
            config.put("Main", "scaleBackground", ConfigHandler.scaleBackground);
            config.put("Main", "offsetAngle", ConfigHandler.offsetAngle);
            config.put("Main", "anaglyph", ConfigHandler.useShaders);
            File f = Gdx.files.local("data/config.ini").file();
            config.write(f);
            Logger.i("Config stored to " + f.getAbsolutePath());
        } catch (Exception e) {
            Logger.w("Exception while saving config", e);
        }

    }
}
