package truelecter.iig.util;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontManager {
	private static HashMap<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
	private static final String DEFAULT_FONT_PATH = "data/font/";

	public static BitmapFont getFont(String fontN, boolean defaultPath) {
		if (fonts.containsKey(fontN.toLowerCase())) {
			return fonts.get(fontN.toLowerCase());
		} else {
			try {
				String s = defaultPath ? DEFAULT_FONT_PATH + fontN : fontN;
				BitmapFont f = new BitmapFont(Gdx.files.local(s));
				fonts.put(fontN.toLowerCase(), f);
				return f;
			} catch (Exception e) {
				System.out.println("Unable to load font: " + fontN.toLowerCase());
				e.printStackTrace();
				BitmapFont f = new BitmapFont(Gdx.files.internal(DEFAULT_FONT_PATH + "default.fnt"));
				fonts.put(fontN.toLowerCase(), f);
				return f;
			}
		}
	}

	public static BitmapFont getFileLabelFont() {
		return getFont("fileLabel.fnt", true);
	}

	public static BitmapFont getSongNameFont() {
		return getFont("songName.fnt", true);
	}

	public static BitmapFont getTimeFont() {
		return getFont("timeFont.fnt", true);
	}
	
}
