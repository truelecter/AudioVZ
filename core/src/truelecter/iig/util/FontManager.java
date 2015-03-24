package truelecter.iig.util;

import java.util.HashMap;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontManager {
    private static HashMap<String, BitmapFont> fonts = new HashMap<String, BitmapFont>();
    private static final String DEFAULT_FONT_PATH = "data/font/";

    public static BitmapFont getFont(String fontN, boolean defaultPath) {
        String s = defaultPath ? DEFAULT_FONT_PATH + fontN : fontN;
        if (fonts.containsKey(s)) {
            return fonts.get(s);
        } else {
            try {
                BitmapFont f = new BitmapFont(Gdx.files.local(s));
                fonts.put(s, f);
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

    public static void reloadFonts() {
        Set<String> s = fonts.keySet();
        fonts.clear();
        for (String f : s) {
            fonts.put(f, new BitmapFont(Gdx.files.internal(DEFAULT_FONT_PATH + "default.fnt")));
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

    public static BitmapFont getOptionLabelFont() {
        return getFont("timeFont.fnt", true);
    }

}
