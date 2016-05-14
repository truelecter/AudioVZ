package truelecter.iig.desktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import truelecter.iig.Main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
    private static void check() throws IOException, URISyntaxException {
        File jarFile = new File(DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                .getPath());
        if (jarFile.getAbsolutePath().endsWith("jar"))
            return;
        String destDir = jarFile.getParent();
        JarFile jar = new java.util.jar.JarFile(jarFile);
        Enumeration<JarEntry> enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry file = enumEntries.nextElement();
            if (!file.getName().startsWith("data")) {
                continue;
            }
            File f = new File(destDir + java.io.File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            InputStream is = jar.getInputStream(file); // get the input
            // stream
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) { // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
    }

    public static void main(String[] args) {
        try {
            check();
        } catch (IOException | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "AudioVZ";
        cfg.backgroundFPS = 40;
        cfg.foregroundFPS = 60;
        cfg.resizable = false;
        cfg.audioDeviceBufferCount = 10;
        new LwjglApplication(new Main(), cfg);
    }
}
