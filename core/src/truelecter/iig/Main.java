package truelecter.iig;

import truelecter.iig.screen.*;
import truelecter.iig.util.input.GlobalInputProcessor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class Main extends Game {
	public static int width = 900;
	public static int height = 600;
	private static Main instance;
	public static float volume = 0.1f;

	public static Main getInstance() {
		if (instance == null) {
			String args[] = new String[0];
			instance = new Main(width, height, args);
		}
		return instance;
	}

	public Main(int i, int j, String[] args) {
		width = i;
		height = j;
		instance = this;
	}

	@Override
	public void create() {
		Gdx.input.setInputProcessor(new GlobalInputProcessor());
		/**
		 * deprecated AudioSpectrum as; boolean play = false; if
		 * (filename.equals("")) { JFileChooser fc = new JFileChooser();
		 * fc.setFileFilter(new FileFilter() {
		 * 
		 * @Override public boolean accept(File f) { return f.isDirectory() ||
		 *           f.getName().toLowerCase().endsWith("mp3"); }
		 * @Override public String getDescription() { return
		 *           "*.mp3 | MP3 audio file"; }
		 * 
		 *           }); if (fc.showDialog(null, "Choose your file") ==
		 *           JFileChooser.APPROVE_OPTION) { as = new
		 *           AudioSpectrum(fc.getSelectedFile().getAbsolutePath(),
		 *           play); } else { as = new AudioSpectrum(play); }
		 * 
		 *           } else { as = new AudioSpectrum(filename, play); }
		 *           as.remove(); as.dispose();
		 */
		setScreen(new FileManager());
	}
}
