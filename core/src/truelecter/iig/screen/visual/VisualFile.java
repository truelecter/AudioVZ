package truelecter.iig.screen.visual;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Logger;

public class VisualFile {
    private static final int LINE_SPLIT_HEIGHT = 10;
    private static final int LEFT_PADDING = 20;
    private static ArrayList<VisualFile> vf;
    private static int selectedId = 0;
    private static boolean onRoot = false;
    private static float lerpSpeed = 0.1337f;
    private static Texture selectedTexture = new Texture("data/FileManager/selected.png");
    private static Texture mainTexture = new Texture("data/FileManager/lines.png");
    private File f;
    private Vector2 pos;
    private String label;
    private boolean selected;
    private Sprite texture;
    private int id;

    public boolean isVisible() {
        float h = getHeight();
        float fh = h + pos.y;
        return fh > 1 && pos.y - h < ConfigHandler.height;
    }

    public static ArrayList<VisualFile> getOnlyMusic() {
        if (vf == null) {
            return null;
        }
        ArrayList<VisualFile> res = new ArrayList<VisualFile>();
        for (VisualFile v : vf) {
            if (v.getFile().getAbsolutePath().endsWith(".mp3")) {
                res.add(v);
            }
        }
        return res;
    }

    public static File nextForPath(String path) {
        ArrayList<VisualFile> s = getOnlyMusic();
        Logger.i("Playlist: " + s);
        if (s == null) {
            return null;
        }
        Logger.i("Playlist length: " + s.size() + ", path: " + path);
        if (path == null) {
            return s.get(0).f;
        }
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i).getFile().getAbsolutePath().equals(path)) {
                return getNextForId(i, s).f;
            }
        }
        return s.get(0).f;
    }

    private static VisualFile getNextForId(int id, ArrayList<VisualFile> vf) {
        if (id + 1 == vf.size()) {
            return vf.get(0);
        } else {
            return vf.get(id + 1);
        }
    }

    public static void prepareList() {
        if (vf == null) {
            vf = new ArrayList<VisualFile>();
        } else {
            vf.clear();
            System.gc();
        }
    }

    public static VisualFile getSelected() {
        return VisualFile.vf.get(VisualFile.selectedId);
    }

    public static void updateAll(float delta) {
        if (vf != null) {
            for (VisualFile v : vf) {
                v.update(delta);
            }
        }
    }

    public VisualFile(File f, String label, float x, float y) {
        if (vf == null) {
            prepareList();
        }
        this.f = f;
        this.label = label;
        pos = new Vector2(x, y);
        selected = false;
        id = vf.size();
        vf.add(this);
        texture = new Sprite(mainTexture);
    }

    public VisualFile(File f) {
        this(f, 0, 0);
    }

    public VisualFile(File f, String name) {
        this(f, name, 0, 0);
    }

    public VisualFile(File f2, float x, float y) {
        this(f2, f2 == null ? "NaN" : f2.getName().trim().isEmpty() ? f2.getAbsolutePath() : f2.getName(), x, y);
    }

    public float getWidth() {
        return Math.max(texture.getWidth(), getBounds().width);
    }

    public TextBounds getBounds() {
        return FontManager.getFileLabelFont().getBounds(label);
    }

    public float getHeight() {
        return Math.max(texture.getHeight(), getBounds().height);
    }

    public void update(float delta) {
        Vector2 target = new Vector2(LEFT_PADDING, (ConfigHandler.height - getHeight()) / 2 + (selectedId - id)
                * (LINE_SPLIT_HEIGHT + getHeight()) - 0.1f);
        setPos(getPos().lerp(target, lerpSpeed));
    }

    public boolean isSelected() {
        return selected;
    }

    public void select() {
        texture = new Sprite(selectedTexture);
        selectedId = id;
        selected = true;
    }

    public void next() {
        if (selected) {
            selected = false;
            if (id + 1 == vf.size()) {
                vf.get(0).select();
            } else {
                vf.get(id + 1).select();
            }
            texture = new Sprite(mainTexture);
        }
    }

    public File getFile() {
        return f;
    }

    public Vector2 getPos() {
        return pos;
    }

    public void setPos(float x, float y) {
        pos.set(x, y);
    }

    public void setPos(Vector2 pos) {
        this.pos.set(pos);
    }

    public static void drawAll(SpriteBatch batch) {
        int visibles = 0;
        if (vf != null)
            for (VisualFile f : vf) {
                if (f.isVisible()) {
                    f.draw(batch);
                    visibles++;
                }
            }
        FontManager.getFileLabelFont().draw(batch, "Visibles: " + visibles, 100, ConfigHandler.height / 2 + 100);
    }

    public void draw(SpriteBatch batch) {
        String str = (selected ? ">> " : "") + label;
        TextBounds tb = FontManager.getFileLabelFont().getBounds(str);
        texture.setSize(ConfigHandler.width - LEFT_PADDING * 2 - (selected ? 10 : 0), texture.getHeight());
        texture.setPosition(pos.x + (selected ? 10 : 0), pos.y - texture.getHeight());
        texture.draw(batch);
        FontManager.getFileLabelFont().draw(batch, str, pos.x + 15, pos.y - (texture.getHeight() - tb.height) / 2);

    }

    public void before() {
        if (selected) {
            selected = false;
            if (id == 0) {
                vf.get(vf.size() - 1).select();
            } else {
                vf.get(id - 1).select();
            }
            texture = new Sprite(mainTexture);
        }
    }

    public static VisualFile get(int i) {
        if (vf == null) {
            prepareList();
        }
        return vf.get(i);
    }

    public static boolean isOnRoot() {
        return onRoot;
    }

    public static void setOnRoot(boolean onRoot) {
        VisualFile.onRoot = onRoot;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return "Label: " + label + ", path: " + f.getAbsolutePath();
    }
}