package truelecter.iig.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import truelecter.iig.Main;
import truelecter.iig.screen.visual.Button;
import truelecter.iig.screen.visual.VisualFile;
import truelecter.iig.screen.visual.menu.Options;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Function;
import truelecter.iig.util.Logger;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;
import android.database.Cursor;
import android.provider.MediaStore;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FileManager implements Screen, SubInputProcessor {

    private static File lastFmDir = null;
    protected static String lastFilePath = null;

    private File currentDir;
    private ArrayList<File> structureFiles;
    private ArrayList<File> structureDirs;
    private int rootCount = 0;
    private SpriteBatch batch;
    private Sprite background;
    private int lastScreenY = 0;
    private int lastScreenTouchX = 0;
    private int lastScreenTouchY = 0;
    private int scrollPointerDiff = 0;
    private boolean enableHidden = false;
    private Options options;
    private Button optionsButton;

    public FileManager() {
        this(ConfigHandler.lastFileManagerPath);
    }

    public FileManager(String filePath) {
        this(Gdx.app.getType() == ApplicationType.Android ? Gdx.files.absolute(filePath).file() : new File(filePath));
    }

    public FileManager(File currentDir) {
        structureDirs = new ArrayList<File>();
        structureFiles = new ArrayList<File>();
        this.currentDir = null;
        if (currentDir == null) {
            currentDir = new File(Gdx.files.getExternalStoragePath());
        }
        if (currentDir.getAbsolutePath().toLowerCase().endsWith(".mp3")) {
            currentDir = currentDir.getParentFile();
        }
        switch (Gdx.app.getType()) {
        case Desktop:
            changeDir(currentDir);
            break;
        case Android:
            initAndroidView();
            break;
        default:
            Logger.e("Unsupported system", null);
            Gdx.app.exit();
        }
        batch = new SpriteBatch();
        background = new Sprite(new Texture("data/FileManager/background.png"));
        background.setSize(ConfigHandler.width, ConfigHandler.height);
        options = new Options();
        optionsButton = new Button(new Texture("data/icons/settings.png"), 20f, 20f, 60f, 60f, new Function() {
            @Override
            public void toRun() {
                options.toggle();
            }
        });
        optionsButton.setPriority(99);
    }

    private void initAndroidView() {
        String[] proj = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE };
        Cursor musicCursor = Main.aa.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        VisualFile.prepareList();
        while (musicCursor.moveToNext()) {
            if (musicCursor.getString(1).toLowerCase().endsWith(".mp3"))
                new VisualFile(new File(musicCursor.getString(1)), musicCursor.getString(2));
        }
        VisualFile.get(0).select();
    }

    private void changeDir(final File dir) {
        if (Gdx.app.getType() == ApplicationType.Android) {
            Main.getInstance().setScreen(
                    new Loading(0l, VisualFile.getSelected().getFile(), VisualFile.getSelected().getLabel()));
            return;
        }
        try {
            lastFmDir = currentDir;
            if (lastFmDir != null)
                ConfigHandler.lastFileManagerPath = lastFmDir.getAbsolutePath();
            if (dir != null) {
                if (dir.exists() && !dir.isDirectory()) {
                    Main.getInstance().setScreen(new Loading(0l, dir, null));
                    return;
                }
                if (!dir.exists() || !dir.isDirectory()) {
                    currentDir = lastFmDir == null ? new File(Gdx.files.getExternalStoragePath()) : lastFmDir;
                } else {
                    currentDir = dir;
                }
                rootCount = File.listRoots().length;
                try {
                    makeStructure(new ArrayList<File>(Arrays.asList(currentDir.listFiles())));
                } catch (Exception e) {

                }
            } else {
                rootCount = File.listRoots().length;
                makeStructure(new ArrayList<File>(Arrays.asList(File.listRoots())), true);
            }
        } catch (Exception e) {
            changeDir(lastFmDir);
            Logger.w("Error changing dir. Dir changed to last succesfull", e);
        }
    }

    private void makeStructure(ArrayList<File> list) {
        makeStructure(list, false);
    }

    public static boolean isHidden(File f) {
        boolean s = f.isHidden() || f.getName().charAt(0) == '.';
        boolean names = f.getName().toLowerCase().equals("RECYCLER".toLowerCase())
                || f.getName().toLowerCase().equals("$Recycle.Bin".toLowerCase());
        return s || names;
    }

    private void makeStructure(ArrayList<File> structure, boolean root) {
        structureDirs.clear();
        structureFiles.clear();
        for (File f : File.listRoots()) {
            structureDirs.add(f);
        }
        structureDirs.add(currentDir.getParentFile());
        for (File f : structure) {
            if (f.isDirectory()) {
                if (!isHidden(f) || enableHidden || root)
                    structureDirs.add(f);
            } else {
                if (f.getAbsolutePath().endsWith("mp3")) {
                    if (!isHidden(f) || enableHidden)
                        structureFiles.add(f);
                }
            }
        }
        makeVisualFilesStructure(root);
        VisualFile.setOnRoot(root);
    }

    private void makeVisualFilesStructure(boolean root) {
        int isRoot = 0;
        VisualFile.prepareList();
        for (File f : structureDirs) {
            if (isRoot < rootCount)
                isRoot++;
            else if (isRoot == rootCount) {
                isRoot++;
                if (!root) {
                    VisualFile vf = new VisualFile(f, "..", 0, 0);
                    vf.select();
                }
            } else {
                new VisualFile(f, 0, 0);
            }
        }
        if (root) {
            VisualFile.get(0).select();
        } else
            for (File f : structureFiles) {
                new VisualFile(f, 0, 0);
            }
    }

    @Override
    public void show() {
        GlobalInputProcessor.removeAllOfClass(this.getClass());
        GlobalInputProcessor.register(this);
    }

    @Override
    public void render(float delta) {
        if (ConfigHandler.autoPlayReady && (ConfigHandler.nextButtonPressed || ConfigHandler.autoPlay)) {
            File next = VisualFile.nextForPath(lastFilePath);
            ConfigHandler.nextButtonPressed = false;
            Main.getInstance().setScreen(new Loading(0, next, null));
        }
        batch.begin();
        ConfigHandler.autoPlayReady = false;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        background.draw(batch);
        VisualFile.updateAll(delta);
        VisualFile.drawAll(batch);
        options.render(batch);
        optionsButton.drawCentered(batch, 50, 50);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        FontManager.reloadFonts();
        VisualFile.reloadTexture();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        try {
            GlobalInputProcessor.remove(this);
            batch.dispose();
        } catch (Exception e) {
            Logger.w("Error while disposing FileManager instance", e);
        }
        options.dispose();
        optionsButton.dispose();
        System.gc();
        System.out.println("FileManager disposed!");
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
        case Input.Keys.DOWN:
            VisualFile.getSelected().next();
            break;
        case Input.Keys.UP:
            VisualFile.getSelected().before();
            break;
        case Input.Keys.ENTER:
            changeDir(VisualFile.getSelected().getFile());
            break;
        case Input.Keys.BACKSPACE:
            if (!VisualFile.isOnRoot())
                changeDir(VisualFile.get(0).getFile());
            else
                Gdx.app.exit();
            break;
        case Input.Keys.BACK:
        case Input.Keys.ESCAPE:
            Gdx.app.exit();
            break;
        case Input.Keys.TAB:
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".mp3");
                }

                @Override
                public String getDescription() {
                    return "*.mp3 | MP3 audio file";
                }

            });
            if (fc.showDialog(null, "Choose your file") == JFileChooser.APPROVE_OPTION) {
                try {
                    Main.getInstance().setScreen(new AudioSpectrum(fc.getSelectedFile().getAbsolutePath()));
                } catch (Exception e) {
                    Logger.e("Unable to select file", e);
                }
            }
            break;
        case Input.Keys.F1:
            try {
                Main.getInstance().setScreen(new AudioSpectrum(false, "Spag Heddy - Sine Time"));
            } catch (Exception e) {
                Logger.e("Error while launching preview", e);
            }
            break;
        case Input.Keys.H:
            enableHidden = !enableHidden;
            break;
        case Input.Keys.O:
            options.toggle();
            break;
        default:
            return false;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastScreenTouchX = screenX;
        lastScreenTouchY = screenY;
        lastScreenY = screenY;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Math.abs(lastScreenTouchX - screenX) < 10 && Math.abs(lastScreenTouchY - screenY) < 10) {
            changeDir(VisualFile.getSelected().getFile());
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        scrollPointerDiff += lastScreenY - screenY;
        if (scrollPointerDiff > 50) {
            VisualFile.getSelected().next();
            scrollPointerDiff = 0;
        } else if (lastScreenY - screenY < -50) {
            VisualFile.getSelected().before();
            scrollPointerDiff = 0;
        }
        lastScreenY = screenY;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount > 0) {
            for (int i = 0; i < amount; i++)
                VisualFile.getSelected().next();
        } else {
            for (int i = 0; i < -amount; i++)
                VisualFile.getSelected().before();
        }
        return true;
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
