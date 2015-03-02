package truelecter.iig.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontManager {
	private static HashMap<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();

	public static BitmapFont getFont(String fontN) {
		if (fonts.containsKey(fontN.toLowerCase())) {
			return fonts.get(fontN.toLowerCase());
		} else {
			try {
				BitmapFont f = new BitmapFont(Gdx.files.internal("data/font/" + fontN.toLowerCase() + ".fnt"));
				fonts.put(fontN.toLowerCase(), f);
				return f;
			} catch (Exception e) {
				System.out.println("Unable to load font: " + fontN.toLowerCase());
				BitmapFont f = new BitmapFont(Gdx.files.internal("data/font/default.fnt"));
				fonts.put(fontN.toLowerCase(), f);
				return f;
			}
		}
	}

	public static BitmapFont getFileLabelFont() {
		return getFont("fileLabel");
	}

	public static BitmapFont getSongNameFont() {
		return getFont("songName");
	}

	public static BitmapFont getTimeFont(){
		return getFont("timeFont");
	}
}
