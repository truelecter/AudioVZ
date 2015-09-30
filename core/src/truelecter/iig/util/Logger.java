package truelecter.iig.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import truelecter.iig.Main;

import com.badlogic.gdx.Gdx;

public class Logger {

    private static BufferedWriter getWriter() {
        try {
            File file = null;
            if (!Main.DEBUG || !Gdx.files.isExternalStorageAvailable())
                file = new File(Gdx.files.getLocalStoragePath() + File.separator + "log.txt");
            else
                file = new File(Gdx.files.getExternalStoragePath() + File.separator + "AudioVZ_log.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            return bw;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void log(String tag, String message, Exception e) {
        BufferedWriter bw = getWriter();
        PrintWriter pw = new PrintWriter(bw);
        System.out.println("[" + tag.toUpperCase() + " " + (new Date()).toString() + "] " + message);
        if (e != null) {
            e.printStackTrace(System.out);
        }
        if (bw != null) {
            try {
                bw.write("[" + tag.toUpperCase() + " " + (new Date()).toString() + "] " + message + "\n");
                if (e != null)
                    e.printStackTrace(pw);
                pw.close();
                bw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void e(String message, Exception e) {
        log("error", message, e);
    }

    public static void w(String message, Exception e) {
        log("warning", message, e);
    }

    public static void i(String message) {
        log("info", message, null);
    }

}
