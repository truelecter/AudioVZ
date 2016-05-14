package truelecter.iig.util;

import java.io.File;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mpatric.mp3agic.Mp3File;

public class Util {
    public static double[] castShortToDouble(short[] x) {
        double[] res = new double[x.length];
        for (int i = 0; i < x.length; i++)
            res[i] = x[i];
        return res;
    }

    public static float[] castShortToFloat(short[] x) {
        float[] res = new float[x.length];
        for (int i = 0; i < x.length; i++)
            res[i] = x[i];
        return res;
    }

    public static String computeTime(long length) {
        long hour = length / 3600;
        long minute = (length - (hour * 3600)) / 60;
        long sec = length - (hour * 3600) - (minute * 60);
        String res = (hour != 0 ? hour + ":" + ((minute < 10 ? "0" : "")) : "") + minute + ":"
                + (sec < 10 ? "0" + sec : sec);
        return res;
    }

    public static String convertPath(File path) {
        return convertPath(path.getAbsolutePath());
    }

    public static String convertPath(String path) {
        return path.replace(Gdx.files.getExternalStoragePath(), "!EXTERNAL!").replace(Gdx.files.getLocalStoragePath(),
                "!INTERNAL!");
    }

    public static void drawCentered(SpriteBatch canvas, Texture texture, float x, float y) {
        canvas.draw(texture, x - (texture.getWidth() / 2), y - (texture.getHeight() / 2));
    }

    public static FileHandle getFile(File path, String name, boolean internal) {
        if (internal)
            if (Gdx.app.getType() == ApplicationType.Android)
                return Gdx.files.local(path.isDirectory() ? path.getAbsolutePath() : path.getParentFile()
                        .getAbsolutePath() + File.separator + name);
            else
                return Gdx.files.internal(path.isDirectory() ? path.getAbsolutePath() : path.getParentFile()
                        .getAbsolutePath() + File.separator + name);
        File p = path.getParentFile();
        if (path.isDirectory())
            p = path;
        String res = p.getAbsolutePath();
        if (!p.getAbsolutePath().endsWith(File.separator))
            res += File.separator;
        res += name;
        return Gdx.files.absolute(res);
    }

    public static String getMP3InfoForFile(File f) {
        Mp3File mp = null;
        try {
            mp = new Mp3File(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String name = f == null ? null : f.getName();
        try {
            name = mp == null ? f.getName() : mp.getId3v2Tag().getTitle() == null ? f.getName() : (mp.getId3v2Tag()
                    .getArtist() == null ? mp.getId3v2Tag().getArtist() + " - " : "") + mp.getId3v2Tag().getTitle();
        } catch (Exception e) {
        }
        return name;
    }

    public static boolean ignoreErrors(Function toRun) {
        try {
            toRun.toRun();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void printArray(float[] spectrum) {
        if (spectrum == null) {
            System.out.println("null");
            return;
        }
        System.out.print("[");
        for (int i = 0; i < (spectrum.length - 1); i++)
            System.out.print(spectrum[i] + ", ");
        if (spectrum.length > 0)
            System.out.println(spectrum[spectrum.length - 1] + "]");
    }
}