package truelecter.iig.screen.visual;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
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
			toRenderer = new Sprite(new Texture(texturePath));
			this.x = x;
			this.y = y;
		}

		public SkinPart(String texturePath, float x, float y) {
			toRenderer = new Sprite(new Texture(texturePath));
			this.x = x;
			this.y = y;
		}

		public SkinPart(Texture texturePath, float x, float y) {
			toRenderer = new Sprite(texturePath);
			this.x = x;
			this.y = y;
		}

		public void renderer(SpriteBatch sb) {
			toRenderer.setX(x);
			toRenderer.setY(y);
			toRenderer.draw(sb);
		}

		public void setLocation(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private enum SkinPartType {
		CUSTOM, BUTTON, BACKGROUND, TIMEPANEL;

		protected static SkinPartType getTypeFor(String name) {
			String c = name.toLowerCase();
			if (c.equals("button")) {
				return BUTTON;
			}
			if (c.equals("background")) {
				return BACKGROUND;
			}
			if (c.equals("timepanel")) {
				return TIMEPANEL;
			}
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

	public Texture play = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(), "icon/play.png", true));
	public Texture pause = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(), "icon/pause.png", true));
	public Texture background = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(), "background.png", true));;
	public Vector2 timeLeft = new Vector2(ConfigHandler.width - 120, 100);
	public Vector2 timePassed = new Vector2(40, 100);
	public Vector2 soundPos = new Vector2(ConfigHandler.width - 90, ConfigHandler.height - 50);
	public Vector2 songNamePos = new Vector2(50, ConfigHandler.height - 50);
	public long barWidth = 8;
	public Vector4 timeBar = new Vector4(ConfigHandler.width - 62, 33, 47, barWidth);
	public Texture bars = new Texture(Util.getFile(Gdx.files.internal("data/default/default.skn").file(), "colors-borders.png", true));;
	public boolean useOldBars = false;
	public ArrayList<SkinPart> customPartsBackground;
	public ArrayList<SkinPart> customPartsForeground;
	public File skinFile;

	private static Skin defaultSkin;

	public Skin(File skin) throws InvalidFileFormatException, IOException {
		this(skin, false);
	}

	private Skin(File skin, boolean useInternal) throws InvalidFileFormatException, IOException {
		System.out.println("Using" + (useInternal ? " internal " : " ") + "skin: " + skin.getAbsolutePath());
		Ini ini = new Ini(skin);
		customPartsBackground = new ArrayList<SkinPart>();
		customPartsForeground = new ArrayList<SkinPart>();
		skinFile = skin;
		for (String name : ini.keySet()) {
			Ini.Section section = ini.get(name);
			String x = "";
			String y = "";
			String w = "";
			String v = "";
			switch (SkinPartType.getTypeFor(name)) {
			case BUTTON:
				x = section.get("playPath", "icon/play.png");
				play = new Texture(Util.getFile(skin, x, useInternal));
				x = section.get("pausePath", "icon/pause.png");
				pause = new Texture(Util.getFile(skin, x, useInternal));
				break;
			case BACKGROUND:
				x = section.get("path", "backgroundV.png");
				background = new Texture(Util.getFile(skin, x, useInternal));
				break;
			case TIMEPANEL:
				x = section.get("timePassedX", "40");
				y = section.get("timePassedY", "100");
				timePassed = new Vector2(parse(x), parse(y));
				x = section.get("timeLeftX", "width - 120");
				y = section.get("timeLeftY", "100");
				timeLeft = new Vector2(parse(x), parse(y));
				x = section.get("songNameX", "50");
				y = section.get("songNameY", "height - 50");
				songNamePos = new Vector2(parse(x), parse(y));
				x = section.get("soundX", " width - 90");
				y = section.get("soundY", "height - 50");
				soundPos = new Vector2(parse(x), parse(y));
				w = section.get("timeBarLength", "width - 62");
				x = section.get("timeBarX", "33");
				y = section.get("timeBarY", "47");
				v = section.get("timeBarWidth", "8");
				timeBar = new Vector4(parse(x), parse(y), parse(w), parse(v));
				bars = new Texture(Util.getFile(skin, section.get("bars", "colors-borders.png"), useInternal));
				barWidth = (long) parse(section.get("barWidth", "8"));
				useOldBars = section.get("oldBars", boolean.class, false);
				break;
			default:
				x = section.get("path", "data/particle.png");
				Texture t = new Texture(Util.getFile(skin, x, x.equalsIgnoreCase("data/default/particle.png")));
				SkinPart sp = new SkinPart(t, parse(section.get("x", "0")), parse(section.get("y", "0")));
				if (section.get("isOnBackground", boolean.class, true)) {
					customPartsBackground.add(sp);
				} else {
					customPartsForeground.add(sp);
				}

			}
		}
	}

	public void rendererCustomParts(SpriteBatch sb, boolean background) {
		ArrayList<SkinPart> workingWith = background ? customPartsBackground : customPartsForeground;
		for (SkinPart sp : workingWith) {
			sp.renderer(sb);
		}
	}

	public static float parse(String exp) {
		Expr expr;
		try {
			expr = Parser.parse(exp.toLowerCase().replaceAll("width", ConfigHandler.width + "")
					.replaceAll("height", ConfigHandler.height + ""));
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			return Float.NEGATIVE_INFINITY;
		}
		return (float) expr.value();
	}

	public static Skin skinByPath(String path) throws InvalidFileFormatException, IOException {
		if (path == null || !new File(path).exists()) {
			return getDefaultSkin();
		}
		return new Skin(new File(path));
	}

	public static Skin getDefaultSkin() throws InvalidFileFormatException, IOException {
		if (defaultSkin == null) {
			defaultSkin = new Skin(Gdx.files.internal("data/default/default.skn").file(), true);
		}
		return defaultSkin;
	}
}
