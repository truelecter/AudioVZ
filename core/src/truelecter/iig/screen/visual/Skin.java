package truelecter.iig.screen.visual;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Ini;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;

public class Skin {
    public class SkinPart {
        private Sprite toRenderer;
        private float x;
        private float y;

        public SkinPart(FileHandle texturePath, float x, float y) {
            this.toRenderer = new Sprite(new Texture(texturePath));
            this.x = x;
            this.y = y;
        }

        public SkinPart(String texturePath, float x, float y) {
            this.toRenderer = new Sprite(new Texture(texturePath));
            this.x = x;
            this.y = y;
        }

        public SkinPart(Texture texturePath, float x, float y) {
            this.toRenderer = new Sprite(texturePath);
            this.x = x;
            this.y = y;
        }

        public void renderer(SpriteBatch sb) {
            this.toRenderer.setX(this.x);
            this.toRenderer.setY(this.y);
            this.toRenderer.draw(sb);
        }

        public void setLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private enum SkinPartType {
        CUSTOM, BUTTON, BACKGROUND, TIMEPANEL, FONT;

        protected static SkinPartType getTypeFor(String name) {
            String c = name.toLowerCase();
            if (c.equals("button"))
                return BUTTON;
            if (c.equals("background"))
                return BACKGROUND;
            if (c.equals("timepanel"))
                return TIMEPANEL;
            if (c.equals("font"))
                return FONT;
            return CUSTOM;
        }
    }

    public class Vector4 {
        public float x;
        public float y;
        public float z;
        public float t;

        public Vector4(float x, float y, float z, float t) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
        }
    }

    private static Skin defaultSkin;

    private static boolean copyFile(File source, File dest) throws IOException {
        if ((source == null) || (dest == null))
            return false;
        InputStream is = null;
        OutputStream os = null;
        boolean res = false;
        Logger.i("Copying from " + source.getAbsolutePath() + " to " + dest.getAbsolutePath());
        try {
            if (!dest.exists()) {
                dest.getParentFile().mkdirs();
                dest.createNewFile();
            }
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);
            res = true;
        } catch (Exception e) {
            Logger.e("Error copying files", e);
        } finally {
            if (is != null)
                is.close();
            if (is != null)
                os.close();
        }
        return res;
    }

    public static Skin getDefaultSkin() throws IOException {
        if (defaultSkin == null)
            if (Gdx.app.getType() == ApplicationType.Android)
                defaultSkin = new Skin(Gdx.files.local("data/default/default.skn").file(), false);
            else
                defaultSkin = new Skin(Gdx.files.internal("data/default/default.skn").file(), true);
        return defaultSkin;
    };

    public static float parse(String exp) {
        Expr expr;
        try {
            expr = Parser.parse(exp.toLowerCase().replaceAll("width", ConfigHandler.width + "")
                    .replaceAll("height", ConfigHandler.height + ""));
        } catch (SyntaxException e) {
            Logger.w("Expression parsing error", e);
            return Float.NEGATIVE_INFINITY;
        }
        return (float) expr.value();
    }

    public static Skin skinByPath(String path) {
        if ((path == null) || !new File(path).exists())
            try {
                return getDefaultSkin();
            } catch (IOException e) {
                Logger.e("Error loading default skin!", e);
                return null;
            }
        Skin res = null;
        try {
            res = new Skin(new File(path));
        } catch (IOException e) {
            Logger.e("Invalid skin properties!", e);
        }
        if (res == null)
            try {
                return getDefaultSkin();
            } catch (IOException e) {
                Logger.e("Error loading default skin!", e);
                return null;
            }
        return res;
    }

    public Texture play = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(),
            "icon/play.png", true));
    public Texture pause = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(),
            "icon/pause.png", true));
    public Texture background = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(),
            "background.png", true));
    public Vector2 timeLeft = new Vector2(ConfigHandler.width - 120, 100);
    public Vector2 timePassed = new Vector2(40, 100);;
    public Vector2 soundPos = new Vector2(ConfigHandler.width - 90, ConfigHandler.height - 50);
    public Vector2 songNamePos = new Vector2(50, ConfigHandler.height - 50);
    public long barWidth = 8;
    public Vector4 timeBar = new Vector4(ConfigHandler.width - 62, 33, 47, this.barWidth);
    public Texture bars = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(),
            "colors-borders.png", true));
    public boolean useOldBars = false;
    public ArrayList<SkinPart> customPartsBackground;
    public ArrayList<SkinPart> customPartsForeground;
    public File skinFile;

    public Vector2 button = new Vector2(ConfigHandler.width, ConfigHandler.height);

    public BitmapFont fileLabel = FontManager.getFileLabelFont();

    public BitmapFont songName = FontManager.getSongNameFont();

    public BitmapFont timeFont = FontManager.getTimeFont();

    public BitmapFont volumeFont = FontManager.getTimeFont();

    private Ini ini = null;

    private boolean internal;

    public Skin(File skin) throws IOException {
        this(skin, false);
    }

    private Skin(File skin, boolean useInternal) throws IOException {
        this.internal = useInternal;
        System.out.println("Using" + (useInternal ? " internal " : " ") + "skin: " + skin.getAbsolutePath());
        Ini section = new Ini(skin);
        this.customPartsBackground = new ArrayList<SkinPart>();
        this.customPartsForeground = new ArrayList<SkinPart>();
        this.skinFile = skin;
        for (String name : section._entries.keySet()) {
            String x = "";
            String y = "";
            String w = "";
            String v = "";
            switch (SkinPartType.getTypeFor(name)) {
            case BUTTON:
                x = section.getString("Button", "playPath", "icon/play.png");
                this.play = new Texture(Util.getFile(skin, x, useInternal));
                x = section.getString("Button", "pausePath", "icon/pause.png");
                this.pause = new Texture(Util.getFile(skin, x, useInternal));
                x = section.getString("Button", "posX", "width / 2");
                y = section.getString("Button", "posY", "height / 2");
                this.button = new Vector2(parse(x), parse(y));
                break;
            case BACKGROUND:
                x = section.getString("Background", "path", "backgroundV.png");
                Logger.i("Background internal - " + false);
                this.background = new Texture(Util.getFile(skin, x, false));
                break;
            case FONT:
                break;
            case TIMEPANEL:
                x = section.getString("TimePanel", "timePassedX", "40");
                y = section.getString("TimePanel", "timePassedY", "100");
                this.timePassed = new Vector2(parse(x), parse(y));
                x = section.getString("TimePanel", "timeLeftX", "width - 120");
                y = section.getString("TimePanel", "timeLeftY", "100");
                this.timeLeft = new Vector2(parse(x), parse(y));
                x = section.getString("TimePanel", "songNameX", "50");
                y = section.getString("TimePanel", "songNameY", "height - 50");
                this.songNamePos = new Vector2(parse(x), parse(y));
                x = section.getString("TimePanel", "soundX", " width - 90");
                y = section.getString("TimePanel", "soundY", "height - 50");
                this.soundPos = new Vector2(parse(x), parse(y));
                w = section.getString("TimePanel", "timeBarLength", "width - 62");
                x = section.getString("TimePanel", "timeBarX", "33");
                y = section.getString("TimePanel", "timeBarY", "47");
                v = section.getString("TimePanel", "timeBarWidth", "8");
                this.timeBar = new Vector4(parse(x), parse(y), parse(w), parse(v));
                this.bars = new Texture(Util.getFile(skin,
                        section.getString("TimePanel", "bars", "colors-borders.png"), useInternal));
                this.barWidth = (long) parse(section.getString("TimePanel", "barWidth", "8"));
                this.useOldBars = Boolean.valueOf(section.getString("TimePanel", "oldBars", "false"));
                break;
            default:
                x = section.getString("Main", "path", "data/particle.png");
                Texture t = new Texture(Util.getFile(skin, x, x.equalsIgnoreCase("data/default/particle.png")));
                SkinPart sp = new SkinPart(t, parse(section.getString(name, "x", "0")), parse(section.getString(name,
                        "y", "0")));
                if (Boolean.valueOf(section.getString(name, "isOnBackground", "true")))
                    this.customPartsBackground.add(sp);
                else
                    this.customPartsForeground.add(sp);

            }
        }
        this.ini = section;
    }

    public void rendererCustomParts(SpriteBatch sb, boolean background) {
        ArrayList<SkinPart> workingWith = background ? this.customPartsBackground : this.customPartsForeground;
        for (SkinPart sp : workingWith)
            sp.renderer(sb);
    }

    public void save() {
        try {
            this.ini.write(this.skinFile);
        } catch (FileNotFoundException e) {
            Logger.w("Unable to save skin!", e);
        }
    }

    public Skin setBackground(String path) {
        String timestamp = System.currentTimeMillis() + ".img";
        // Try to copy new background to local app files
        try {
            if (copyFile(new File(path), new File(this.skinFile.getParentFile().getAbsolutePath() + File.separator
                    + timestamp))) {
                String oldPath = this.ini.getString("Background", "path", "backgroundV.png");
                this.ini.put("Background", "path", timestamp);
                this.ini.put("Background", "local", true);
                this.background = new Texture(Util.getFile(this.skinFile, timestamp, false));
                // Try to delete old background.
                try {
                    FileHandle prevBackground = Util.getFile(this.skinFile, oldPath, this.internal);
                    prevBackground.delete();
                } catch (Exception e) {
                    Logger.w("Unable to delete previous background", e);
                }
            } else
                Logger.w("File was not copied!", null);
        } catch (Exception e) {
            Logger.w("Unable to copy new background", e);
        }
        return this;
    }
}
