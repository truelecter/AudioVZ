package truelecter.iig.screen.visual;

import java.io.File;
import java.util.ArrayList;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Logger;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class VisualFile {
    private static final int LINE_SPLIT_HEIGHT = 10;
    private static final int LEFT_PADDING = 20;
    private static ArrayList<VisualFile> vf;
    private static int selectedId = 0;
    private static boolean onRoot = false;
    private static float lerpSpeed = 0.1337f;
    private static Texture selectedTexture = new Texture("data/FileManager/selected.png");
    private static Texture mainTexture = new Texture("data/FileManager/lines.png");

    public static void drawAll(SpriteBatch batch) {
        if (vf != null)
            for (VisualFile f : vf)
                if (f.isVisible())
                    f.draw(batch);
    }

    public static VisualFile get(int i) {
        if (vf == null)
            prepareList();
        return vf.get(i);
    }

    private static VisualFile getNextForId(int id, ArrayList<VisualFile> vf) {
        if ((id + 1) == vf.size())
            return vf.get(0);
        else
            return vf.get(id + 1);
    }

    public static ArrayList<VisualFile> getOnlyImages() {
        if (vf == null)
            return null;
        ArrayList<VisualFile> res = new ArrayList<VisualFile>();
        for (VisualFile v : vf)
            if (v.getFile().getAbsolutePath().endsWith(".jpg") || v.getFile().getAbsolutePath().endsWith(".jpeg")
                    || v.getFile().getAbsolutePath().endsWith(".png") || v.getFile().getAbsolutePath().endsWith(".bmp"))
                res.add(v);
        return res;
    }

    public static ArrayList<VisualFile> getOnlyMusic() {
        if (vf == null)
            return null;
        ArrayList<VisualFile> res = new ArrayList<VisualFile>();
        for (VisualFile v : vf)
            if (v.getFile().getAbsolutePath().endsWith(".mp3"))
                res.add(v);
        return res;
    }

    public static VisualFile getSelected() {
        return VisualFile.vf.get(VisualFile.selectedId);
    }

    public static boolean isOnRoot() {
        return onRoot;
    }

    public static File nextForPath(String path) {
        ArrayList<VisualFile> s = getOnlyMusic();
        Logger.i("Playlist: " + s);
        if (s == null)
            return null;
        Logger.i("Playlist length: " + s.size() + ", path: " + path);
        if (path == null)
            return s.get(0).f;
        for (int i = 0; i < s.size(); i++)
            if (s.get(i).getFile().getAbsolutePath().equals(path))
                return getNextForId(i, s).f;
        return s.get(0).f;
    }

    public static void prepareList() {
        if (vf == null)
            vf = new ArrayList<VisualFile>();
        else {
            vf.clear();
            System.gc();
        }
    }

    public static void reloadTexture() {
        selectedTexture = new Texture("data/FileManager/selected.png");
        mainTexture = new Texture("data/FileManager/lines.png");
    }

    public static void setOnRoot(boolean onRoot) {
        VisualFile.onRoot = onRoot;
    }

    public static void updateAll(float delta) {
        if (vf != null)
            for (VisualFile v : vf)
                v.update(delta);
    }

    private File f;

    private Vector2 pos;

    private String label;

    private boolean selected;

    private Sprite texture;

    private int id;

    private BitmapFont font = FontManager.getFileLabelFont();

    public VisualFile(File f) {
        this(f, 0, 0);
    }

    public VisualFile(File f2, float x, float y) {
        this(f2, f2 == null ? "NaN" : f2.getName().trim().isEmpty() ? f2.getAbsolutePath() : f2.getName(), x, y);
    }

    public VisualFile(File f, String name) {
        this(f, name, 0, 0);
    }

    public VisualFile(File f, String label, float x, float y) {
        if (vf == null)
            prepareList();
        this.f = f;
        this.label = label;
        this.pos = new Vector2(x, y);
        this.selected = false;
        this.id = vf.size();
        vf.add(this);
        this.texture = new Sprite(mainTexture);
    }

    public void before() {
        if (this.selected) {
            this.selected = false;
            if (this.id == 0)
                vf.get(vf.size() - 1).select();
            else
                vf.get(this.id - 1).select();
            this.texture = new Sprite(mainTexture);
        }
    }

    public void draw(SpriteBatch batch) {
        String str = (this.selected ? ">> " : "") + this.label;
        TextBounds tb = this.font.getBounds(str);
        this.texture.setSize(ConfigHandler.width - (LEFT_PADDING * 2) - (this.selected ? 10 : 0),
                this.texture.getHeight());
        this.texture.setPosition(this.pos.x + (this.selected ? 10 : 0), this.pos.y - this.texture.getHeight());
        this.texture.draw(batch);
        this.font.draw(batch, str, this.pos.x + 15, this.pos.y - ((this.texture.getHeight() - tb.height) / 2));

    }

    public TextBounds getBounds() {
        return this.font.getBounds(this.label);
    }

    public File getFile() {
        return this.f;
    }

    public float getHeight() {
        return Math.max(this.texture.getHeight(), this.getBounds().height);
    }

    public String getLabel() {
        return this.label;
    }

    public Vector2 getPos() {
        return this.pos;
    }

    public float getWidth() {
        return Math.max(this.texture.getWidth(), this.getBounds().width);
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isVisible() {
        float h = this.getHeight();
        float fh = h + this.pos.y;
        return (fh > 1) && ((this.pos.y - h) < ConfigHandler.height);
    }

    public void next() {
        if (this.selected) {
            this.selected = false;
            if ((this.id + 1) == vf.size())
                vf.get(0).select();
            else
                vf.get(this.id + 1).select();
            this.texture = new Sprite(mainTexture);
        }
    }

    public void select() {
        this.texture = new Sprite(selectedTexture);
        selectedId = this.id;
        this.selected = true;
    }

    public void setPos(float x, float y) {
        this.pos.set(x, y);
    }

    public void setPos(Vector2 pos) {
        this.pos.set(pos);
    }

    @Override
    public String toString() {
        return "Label: " + this.label + ", path: " + this.f.getAbsolutePath();
    }

    public void update(float delta) {
        Vector2 target = new Vector2(LEFT_PADDING,
                (((ConfigHandler.height - this.getHeight()) / 2) + ((selectedId - this.id) * (LINE_SPLIT_HEIGHT + this
                        .getHeight()))) - 0.1f);
        this.setPos(this.getPos().lerp(target, lerpSpeed));
    }
}