package truelecter.iig.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import truelecter.iig.Main;
import truelecter.iig.screen.visual.VisualFile;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;
import android.database.Cursor;
import android.provider.MediaStore;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class FileManager implements Screen, SubInputProcessor {

    private static File lastFmDir = null;

    private File currentDir;
    private ArrayList<File> structureFiles;
    private ArrayList<File> structureDirs;
    private int rootCount = 0;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sprite background;
    private int lastScreenY = 0;
    private int lastScreenTouchX = 0;
    private int lastScreenTouchY = 0;
    private int scrollPointerDiff = 0;
    private boolean enableHidden = false;
    private Animation loaderAnimation;
    private float stateTime = 0f;
    private boolean loading = false;
    private String name = null;
    private File selectedFile = null;

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
        camera = new OrthographicCamera();
        camera.setToOrtho(false, ConfigHandler.width, ConfigHandler.height);
        batch = new SpriteBatch();
        background = new Sprite(new Texture("data/FileManager/background.png"));
        GlobalInputProcessor.getInstance().register(this);
        background.setSize(ConfigHandler.width, ConfigHandler.height);
        initAnimation();
    }

    private void initAndroidView() {
        String[] proj = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE };
        Cursor musicCursor = Main.aa.managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, null, null, null);
        VisualFile.prepareList();
        while (musicCursor.moveToNext()) {
            Logger.i(musicCursor.getString(2) + " : " + musicCursor.getString(1));
            new VisualFile(new File(musicCursor.getString(1)), musicCursor.getString(2));
        }
        VisualFile.get(0).select();
    }

    private void initAnimation() {
        Texture loadingAnimationTexture = new Texture(Gdx.files.internal("loading.gif"));
        TextureRegion[][] sprites = TextureRegion.split(loadingAnimationTexture, 128, 128);
        loaderAnimation = new Animation(0.025f, sprites[0]);
    }

    private void changeDir(final File dir) {
        try {
            lastFmDir = currentDir;
            if (lastFmDir != null)
                ConfigHandler.lastFileManagerPath = lastFmDir.getAbsolutePath();
            if (dir != null) {
                if (dir.exists() && !dir.isDirectory()) {
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            loading = true;
                            name = Util.getMP3InfoForFile(dir);
                        }
                    });
                    selectedFile = dir;
                    t.start();
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

    }

    @Override
    public void render(float delta) {
        camera.update();
        batch.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (loading) {
            stateTime += Gdx.graphics.getDeltaTime();
            TextureRegion currentFrame = loaderAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, ConfigHandler.width - currentFrame.getRegionWidth() - 25, 25);
            if (name != null) {
                try {
                    Main.getInstance().setScreen(new AudioSpectrum(selectedFile, true, name));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dispose();
            }
        } else {
            batch.setProjectionMatrix(camera.combined);
            background.draw(batch);
            VisualFile.updateAll(delta);
            VisualFile.drawAll(batch);
        }
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

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        try {
            GlobalInputProcessor.getInstance().remove(this);
            batch.dispose();
        } catch (Exception e) {
            Logger.w("Error while disposing FileManager instance", e);
        }
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
        case Input.Keys.BACK:
            Gdx.app.exit();
            break;
        case Input.Keys.BACKSPACE:
            if (!VisualFile.isOnRoot())
                changeDir(VisualFile.get(0).getFile());
            else
                Gdx.app.exit();
            break;
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (Math.abs(lastScreenTouchX - screenX) < 10 && Math.abs(lastScreenTouchY - screenY) < 10) {
            changeDir(VisualFile.getSelected().getFile());
        }
        return false;
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
        return false;
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
        return false;
    }

}
