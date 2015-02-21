package truelecter.iig.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import truelecter.iig.Main;
import truelecter.iig.screen.visual.VisualFile;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FileManager implements Screen, SubInputProcessor {

	private static File lastFmDir = null;

	private File currentDir;
	private ArrayList<File> structureFiles;
	private ArrayList<File> structureDirs;
	private int rootCount = 0;
	private OrthographicCamera camera;
	private SpriteBatch batch;	
	private Sprite background;
	private int lastScreenX = 0;
	private int lastScreenY = 0;
	private int xDiff = 0;
	private int yDiff = 0;
	private int scrollPointerDiff = 0;
	private String lastError = "";
	private boolean enableHidden = false;

	public FileManager() {
		this(lastFmDir);
	}

	public FileManager(String filePath) {
		this(new File(filePath));
	}

	public FileManager(File currentDir) {
		structureDirs = new ArrayList<File>();
		structureFiles = new ArrayList<File>();
		this.currentDir = null;
		if (currentDir == null) {
			currentDir = new File(Gdx.files.getExternalStoragePath());
		}
		changeDir(currentDir);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Main.width, Main.height);
		batch = new SpriteBatch();
		background = new Sprite(new Texture("data/background.png"));
		GlobalInputProcessor.getInstance().register(this);
	}

	private void changeDir(File dir) {
		try {
			lastFmDir = currentDir;
			if (dir != null) {
				if (dir.exists() && !dir.isDirectory()) {
					Main.getInstance().setScreen(new AudioSpectrum(dir, true, Util.getMP3InfoForFile(dir)));
					dispose();
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
			lastError = e.getMessage();
			System.out.println(lastError);
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
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.begin();
		batch.setProjectionMatrix(camera.combined);
		background.draw(batch);
		VisualFile.updateAll(delta);
		VisualFile.drawAll(batch);
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
		case Input.Keys.BACKSPACE:
			if (!VisualFile.isOnRoot())
				changeDir(VisualFile.get(0).getFile());
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
				Main.getInstance().setScreen(new AudioSpectrum(fc.getSelectedFile().getAbsolutePath()));
			}
			break;
		case Input.Keys.F1:
			Main.getInstance().setScreen(new AudioSpectrum(false, "Spag Heddy - Sine Time"));
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
		lastScreenX = screenX;
		lastScreenY = screenY;
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (xDiff < 20 && yDiff < 20) {
			changeDir(VisualFile.getSelected().getFile());
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		yDiff = lastScreenY - screenY;
		scrollPointerDiff += yDiff;
		xDiff = lastScreenX - screenX;
		if (scrollPointerDiff > 50) {
			VisualFile.getSelected().next();
			scrollPointerDiff = 0;
		} else if (yDiff < -50) {
			VisualFile.getSelected().before();
			scrollPointerDiff = 0;
		}
		lastScreenX = screenX;
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
