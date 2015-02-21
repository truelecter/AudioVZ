package truelecter.iig.screen.visual;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import truelecter.iig.Main;
import truelecter.iig.util.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import expr.Expr;
import expr.Parser;
import expr.SyntaxException;

public class Skin {
	public final Texture play;
	public final Texture pause;
	public final Texture background;
	public final Vector2 timeLeft;
	public final Vector2 timePassed;
	public final Vector2 soundPos;
	public final Vector2 songNamePos;
	public final Vector3 timeBar;
	public final Texture bars;

	private static Skin defaultSkin;

	public Skin(File skin) throws InvalidFileFormatException, IOException {
		this(skin, false);
	}

	private Skin(File skin, boolean useInternal) throws InvalidFileFormatException, IOException {
		Ini ini = new Ini(skin);
		Ini.Section buttonIni = ini.get("Button");
		play = new Texture(Util.getFile(skin, buttonIni.get("playPath"), false));
		pause = new Texture(Util.getFile(skin, buttonIni.get("pausePath"), false));
		Ini.Section backgroundIni = ini.get("Background");
		background = new Texture(Util.getFile(skin, backgroundIni.get("path"), false));
		Ini.Section timePanelIni = ini.get("TimePanel");
		timePassed = new Vector2(parse(timePanelIni.get("timePassedX")), parse(timePanelIni.get("timePassedY")));
		timeLeft = new Vector2(parse(timePanelIni.get("timeLeftX")), parse(timePanelIni.get("timeLeftY")));
		songNamePos = new Vector2(parse(timePanelIni.get("songNameX")), parse(timePanelIni.get("songNameY")));
		soundPos = new Vector2(parse(timePanelIni.get("soundX")), parse(timePanelIni.get("soundY")));
		timeBar = new Vector3(parse(timePanelIni.get("timeBarX")), parse(timePanelIni.get("timeBarY")),
				parse(timePanelIni.get("timeBarLength")));
		bars = new Texture(Util.getFile(skin, timePanelIni.get("bars"), false));
	}

	public static float parse(String exp) {
		Expr expr;
		try {
			expr = Parser.parse(exp.toLowerCase().replaceAll("width", Main.width + "").replaceAll("height", Main.height + ""));
		} catch (SyntaxException e) {
			System.err.println(e.explain());
			return Float.NEGATIVE_INFINITY;
		}
		return (float) expr.value();
	}

	public static Skin getDefaultSkin() throws InvalidFileFormatException, IOException {
		if (defaultSkin == null) {
			defaultSkin = new Skin(Gdx.files.internal("data/default/default.skn").file(), true);
		}
		return defaultSkin;
	}

}
