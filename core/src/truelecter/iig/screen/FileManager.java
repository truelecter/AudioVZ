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

    public static boolean isHidden(File f) {
        boolean s = f.isHidden() || (f.getName().charAt(0) == '.');
        boolean names = f.getName().toLowerCase().equals("RECYCLER".toLowerCase())
                || f.getName().toLowerCase().equals("$Recycle.Bin".toLowerCase());
        return s || names;
    }

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

    public FileManager(File currentDir) {
        this.structureDirs = new ArrayList<File>();
        this.structureFiles = new ArrayList<File>();
        this.currentDir = null;
        if (currentDir == null)
            currentDir = new File(Gdx.files.getExternalStoragePath());
        if (currentDir.getAbsolutePath().toLowerCase().endsWith(".mp3"))
            currentDir = currentDir.getParentFile();
        switch (Gdx.app.getType()) {
        case Desktop:
            this.changeDir(currentDir);
            break;
        case Android:
            this.initAndroidView();
            break;
        default:
            Logger.e("Unsupported system", null);
            Gdx.app.exit();
        }
        this.batch = new SpriteBatch();
        this.background = new Sprite(new Texture("data/FileManager/background.png"));
        this.background.setSize(ConfigHandler.width, ConfigHandler.height);
        this.options = new Options();
        this.optionsButton = new Button(new Texture("data/icons/settings.png"), 20f, 20f, 60f, 60f, new Function() {
            @Override
            public void toRun() {
                FileManager.this.options.toggle();
            }
        });
        this.optionsButton.setPriority(99);
    }

    public FileManager(String filePath) {
        this(Gdx.app.getType() == ApplicationType.Android ? Gdx.files.absolute(filePath).file() : new File(filePath));
    }

    private void changeDir(final File dir) {
        if (Gdx.app.getType() == ApplicationType.Android) {
            Main.getInstance().setScreen(
                    new Loading(0l, VisualFile.getSelected().getFile(), VisualFile.getSelected().getLabel()));
            return;
        }
        try {
            lastFmDir = this.currentDir;
            if (lastFmDir != null)
                ConfigHandler.lastFileManagerPath = lastFmDir.getAbsolutePath();
            if (dir != null) {
                if (dir.exists() && !dir.isDirectory()) {
                    Main.getInstance().setScreen(new Loading(0l, dir, null));
                    return;
                }
                if (!dir.exists() || !dir.isDirectory())
                    this.currentDir = lastFmDir == null ? new File(Gdx.files.getExternalStoragePath()) : lastFmDir;
                else
                    this.currentDir = dir;
                this.rootCount = File.listRoots().length;
                try {
                    this.makeStructure(new ArrayList<File>(Arrays.asList(this.currentDir.listFiles())));
                } catch (Exception e) {

                }
            } else {
                this.rootCount = File.listRoots().length;
                this.makeStructure(new ArrayList<File>(Arrays.asList(File.listRoots())), true);
            }
        } catch (Exception e) {
            this.changeDir(lastFmDir);
            Logger.w("Error changing dir. Dir changed to last succesfull", e);
        }
    }

    @Override
    public void dispose() {
        try {
            GlobalInputProcessor.remove(this);
            this.batch.dispose();
        } catch (Exception e) {
            Logger.w("Error while disposing FileManager instance", e);
        }
        this.options.dispose();
        this.optionsButton.dispose();
        System.gc();
        System.out.println("FileManager disposed!");
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void hide() {
        this.dispose();
    }

    @SuppressWarnings("deprecation")
    private void initAndroidView() {
        String[] proj = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE };
        Cursor musicCursor = Main.aa.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        VisualFile.prepareList();
        while (musicCursor.moveToNext())
            if (musicCursor.getString(1).toLowerCase().endsWith(".mp3"))
                new VisualFile(new File(musicCursor.getString(1)), musicCursor.getString(2));
        VisualFile.get(0).select();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
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
            this.changeDir(VisualFile.getSelected().getFile());
            break;
        case Input.Keys.BACKSPACE:
            if (!VisualFile.isOnRoot())
                this.changeDir(VisualFile.get(0).getFile());
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
            if (fc.showDialog(null, "Choose your file") == JFileChooser.APPROVE_OPTION)
                try {
                    Main.getInstance().setScreen(new AudioSpectrum(fc.getSelectedFile().getAbsolutePath()));
                } catch (Exception e) {
                    Logger.e("Unable to select file", e);
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
            this.enableHidden = !this.enableHidden;
            break;
        case Input.Keys.O:
            this.options.toggle();
            break;
        default:
            return false;
        }
        return true;
    }

    private void makeStructure(ArrayList<File> list) {
        this.makeStructure(list, false);
    }

    private void makeStructure(ArrayList<File> structure, boolean root) {
        this.structureDirs.clear();
        this.structureFiles.clear();
        for (File f : File.listRoots())
            this.structureDirs.add(f);
        this.structureDirs.add(this.currentDir.getParentFile());
        for (File f : structure)
            if (f.isDirectory()) {
                if (!isHidden(f) || this.enableHidden || root)
                    this.structureDirs.add(f);
            } else if (f.getAbsolutePath().endsWith("mp3"))
                if (!isHidden(f) || this.enableHidden)
                    this.structureFiles.add(f);
        this.makeVisualFilesStructure(root);
        VisualFile.setOnRoot(root);
    }

    private void makeVisualFilesStructure(boolean root) {
        int isRoot = 0;
        VisualFile.prepareList();
        for (File f : this.structureDirs)
            if (isRoot < this.rootCount)
                isRoot++;
            else if (isRoot == this.rootCount) {
                isRoot++;
                if (!root) {
                    VisualFile vf = new VisualFile(f, "..", 0, 0);
                    vf.select();
                }
            } else
                new VisualFile(f, 0, 0);
        if (root)
            VisualFile.get(0).select();
        else
            for (File f : this.structureFiles)
                new VisualFile(f, 0, 0);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public void pause() {

    }

    @Override
    public void render(float delta) {
        if (ConfigHandler.autoPlayReady && (ConfigHandler.nextButtonPressed || ConfigHandler.autoPlay)) {
            File next = VisualFile.nextForPath(lastFilePath);
            ConfigHandler.nextButtonPressed = false;
            Main.getInstance().setScreen(new Loading(0, next, null));
        }
        this.batch.begin();
        ConfigHandler.autoPlayReady = false;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.background.draw(this.batch);
        VisualFile.updateAll(delta);
        VisualFile.drawAll(this.batch);
        this.options.render(this.batch);
        this.optionsButton.drawCentered(this.batch, 50, 50);
        this.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void resume() {
        FontManager.reloadFonts();
        VisualFile.reloadTexture();
    }

    @Override
    public boolean scrolled(int amount) {
        if (amount > 0)
            for (int i = 0; i < amount; i++)
                VisualFile.getSelected().next();
        else
            for (int i = 0; i < -amount; i++)
                VisualFile.getSelected().before();
        return true;
    }

    @Override
    public void show() {
        GlobalInputProcessor.removeAllOfClass(this.getClass());
        GlobalInputProcessor.register(this);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        this.lastScreenTouchX = screenX;
        this.lastScreenTouchY = screenY;
        this.lastScreenY = screenY;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        this.scrollPointerDiff += this.lastScreenY - screenY;
        if (this.scrollPointerDiff > 50) {
            VisualFile.getSelected().next();
            this.scrollPointerDiff = 0;
        } else if ((this.lastScreenY - screenY) < -50) {
            VisualFile.getSelected().before();
            this.scrollPointerDiff = 0;
        }
        this.lastScreenY = screenY;
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if ((Math.abs(this.lastScreenTouchX - screenX) < 10) && (Math.abs(this.lastScreenTouchY - screenY) < 10))
            this.changeDir(VisualFile.getSelected().getFile());
        return true;
    }

}
