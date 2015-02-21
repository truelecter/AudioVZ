package truelecter.iig.screen.visual;

import java.io.File;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import truelecter.iig.Main;
import truelecter.iig.util.FontManager;

public class VisualFile {
	private static final int LINE_SPLIT_HEIGHT = 7;
	private static final int LEFT_PADDING = 20;
	private static ArrayList<VisualFile> vf;
	private static int selectedId = 0;
	private static boolean onRoot = false;

	public static void prepareList() {
		if (vf == null) {
			vf = new ArrayList<VisualFile>();
		} else {
			vf.clear();
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

	private File f;
	private Vector2 pos;
	private String label;
	private boolean selected;
	private int id;

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
	}

	public VisualFile(File f2, float x, float y) {
		this(f2, f2 == null ? "NaN" : f2.getName().trim().isEmpty() ? f2.getAbsolutePath() : f2.getName(), x, y);
	}

	public float getWidth() {
		return FontManager.getFileLabelFont().getBounds(label).width;
	}

	public float getHeight() {
		return FontManager.getFileLabelFont().getBounds(label).height;
	}

	public void update(float delta) {
		Vector2 target = new Vector2(LEFT_PADDING, (Main.height - getHeight()) / 2 + (selectedId - id) * (LINE_SPLIT_HEIGHT + getHeight()));
		setPos(getPos().lerp(target, 0.3f));
	}

	public boolean isSelected() {
		return selected;
	}

	public void select() {
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
		if (vf != null)
			for (VisualFile f : vf) {
				f.draw(batch);
			}
	}

	public void draw(SpriteBatch batch) {
		FontManager.getFileLabelFont().draw(batch, (selected ? ">> " : "") + label, pos.x, pos.y);
	}

	public void before() {
		if (selected) {
			selected = false;
			if (id == 0) {
				vf.get(vf.size() - 1).select();
			} else {
				vf.get(id - 1).select();
			}
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
}