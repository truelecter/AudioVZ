package truelecter.iig.screen;

import java.io.File;
import java.util.Random;

import truelecter.iig.Main;
import truelecter.iig.screen.visual.Button;
import truelecter.iig.screen.visual.FadingText;
import truelecter.iig.screen.visual.Skin;
import truelecter.iig.util.FontManager;
import truelecter.iig.util.Function;
import truelecter.iig.util.Util;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AudioSpectrum implements Screen, SubInputProcessor {

	private final float FALLING_SPEED = (1.5f / 3.0f);
	private final float LINE_SCALE = 2f;
	private final int NB_BARS = 40;

	private Sprite background;
	private float radius = 256;
	private Mpg123Decoder decoder;
	private AudioDevice device;
	private boolean playing = false;
	private Thread playbackThread;
	private short[] samples = new short[2048];
	private KissFFT fft;
	private float[] spectrum = new float[2048];
	// private float[] maxValues = new float[2048];
	private float[] topValues = new float[2048];
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private float barWidth = 8;

	private Texture pausedImage;
	private Texture playImage;

	private Sprite b;
	// private Sprite y;
	private Sprite line;
	private Sprite r;

	private Button playPause;

	// TESTAREA_BEGIN
	private ParticleEffect effect;
	private FadingText ft = new FadingText(0.05f, 5000);
	private FadingText volumeft = new FadingText(0.05f, 6000);
	private Skin currentSkin;
	// TESTAREA_END

	private Random gen = new Random(1337);
	private float angle;
	private boolean needToChange = false;
	private String name;
	private String filename;
	private long playbackTime = 0;
	private long songLength = 0;
	private float centerX = Main.width / 2;
	private float centerY = Main.height / 2;

	private boolean changingVolume = false;
	private int factor = 1;

	private boolean changingSkin = false;

	public AudioSpectrum() {
		this("!INTERNAL!/data/default.mp3", true, null);
	}

	public AudioSpectrum(boolean playing) {
		this("!INTERNAL!/data/default.mp3", playing, null);
	}

	public AudioSpectrum(boolean dummy, String name) {
		this("!INTERNAL!/data/default.mp3", true, name);
	}

	public AudioSpectrum(String filename) {
		this(filename, true, null);
	}

	public AudioSpectrum(File file) {
		this(file.getAbsolutePath(), true, null);
	}

	public AudioSpectrum(File file, String name) {
		this(file.getAbsolutePath(), true, name);
	}

	public AudioSpectrum(File file, boolean playing) {
		this(file.getAbsolutePath(), playing, null);
	}

	public AudioSpectrum(File file, boolean playing, String name) {
		this(file.getAbsolutePath(), playing, name);
	}

	public AudioSpectrum(String filename, boolean p, String name) {
		this.filename = filename.replace("!INTERNAL!", Gdx.files.getLocalStoragePath()).replace("!EXTERNAL!",
				Gdx.files.getExternalStoragePath());
		while (this.filename.contains("/")) {
			this.filename = this.filename.replace("/", "\\");
		}
		while (this.filename.contains("\\\\")) {
			this.filename = this.filename.replace("\\\\", "\\");
		}
		playing = p;
		camera = new OrthographicCamera();

		try {
			currentSkin = Skin.getDefaultSkin();
		} catch (Exception e) {
			System.out.println("Invalid skin properties!");
			e.printStackTrace();
		}

		loadSkin(currentSkin);

		camera.setToOrtho(false, Main.width, Main.height);

		batch = new SpriteBatch();

		fft = new KissFFT(2048);

		for (int i = 0; i < spectrum.length; i++) {
			// maxValues[i] = 0;
			topValues[i] = 0;
		}

		DecoderInfo s = initDecoder(this.filename);
		decoder = s.decoder;
		device = s.device;

		playbackThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int readSamples = 1;
				long toStay = 5000;
				long totalSamples = 0;
				do {
					if (playing) {
						try {
							readSamples = decoder.readSamples(samples, 0, samples.length);
							totalSamples += readSamples;
							fft.spectrum(samples, spectrum);
							device.writeSamples(samples, 0, readSamples);
							playbackTime = totalSamples * 500 / decoder.getRate();
						} catch (Exception e) {
						}
					}
				} while (readSamples > 0);
				long lastTime = System.currentTimeMillis();
				do {
					toStay += lastTime - System.currentTimeMillis();
					lastTime = System.currentTimeMillis();
				} while (toStay > 1);
				needToChange = true;
			}
		});
		playbackThread.setDaemon(true);
		playbackThread.start();
		device.setVolume(Main.volume);
		// y = new Sprite(t, 0, 10, 16, 5);
		playPause = new Button(pausedImage, Main.width / 2, Main.height / 2, radius * 2, radius * 2, new Function() {
			@Override
			public void toRun() {
				playing = !playing;
				if (playing) {
					playPause.changeSkin(pausedImage, 512, 512);
				} else {
					playPause.changeSkin(playImage, 512, 512);
				}
			}
		});
		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/3.p"), Gdx.files.internal("data"));
		effect.scaleEffect(0.5f);
		effect.start();
		angle = 10;
		this.name = name == null ? this.filename.substring(this.filename.lastIndexOf('\\') + 1, this.filename.length() - 4) : name;
		GlobalInputProcessor.getInstance().register(this);
		songLength = (long) decoder.getLength();
	}

	@Override
	public void dispose() {
		remove();
		playing = false;
		Util.ignoreErrors(new Function() {
			@Override
			public void toRun() {
				device.dispose();
				device = null;
			}
		});
		Util.ignoreErrors(new Function() {
			@Override
			public void toRun() {
				decoder.dispose();
				decoder = null;
			}
		});
		Util.ignoreErrors(new Function() {
			@Override
			public void toRun() {
				effect.dispose();
			}
		});
		Util.ignoreErrors(new Function() {
			@Override
			public void toRun() {
				playbackThread.interrupt();
				playbackThread = null;
			}
		});
	}

	public void remove() {
		GlobalInputProcessor.getInstance().remove(playPause);
	}

	@Override
	public void render(float delta) {
		if (needToChange) {
			Main.getInstance().setScreen(new FileManager());
		}
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float scale = calculateScale() / 125 / 4096 + 0.2f;
		float angle = 180f / NB_BARS;
		float offsetAngle = -this.angle;
		float k = Main.width / 910f;
		float tScale = (float) (Math.pow(Math.PI, scale + 1) / 10f) * k;
		camera.update();
		batch.begin();
		background.draw(batch);
		currentSkin.rendererCustomParts(batch, true);
		batch.setProjectionMatrix(camera.combined);
		playPause.setScale(tScale);

		for (int i = 0; i < NB_BARS; i++) {
			int histoX = 0;
			if (i < NB_BARS / 2) {
				histoX = NB_BARS / 2 - i;
			} else {
				histoX = i - NB_BARS / 2;
			}

			int nb = (samples.length / NB_BARS) / 2;
			float avg = avg(histoX, nb) / (NB_BARS / 40) / LINE_SCALE;
			// if (avg > maxValues[histoX]) {
			// maxValues[histoX] = avg;
			// }

			if (avg > topValues[histoX]) {
				topValues[histoX] = avg;
			}

			float tmp = 0;

			// drawing spectrum (in blue)
			tmp = scale(avg) / 256 + tScale * radius;
			b.setSize(barWidth * k, tmp);
			b.setPosition(centerX - barWidth * k / 2, centerY);
			b.setOrigin(barWidth * k / 2, 0);
			b.setRotation(angle * i + offsetAngle);
			b.draw(batch);
			b.setRotation(angle * i + 180 + offsetAngle);
			b.draw(batch);

			// drawing top values(in red)
			tmp = scale(currentSkin.useOldBars ? topValues[histoX] : avg) / 256 + tScale * radius;
			r.setSize(barWidth * k, 4);
			r.setPosition(centerX - barWidth * k / 2, centerY + tmp);
			r.setOrigin(barWidth * k / 2, -tmp);
			r.setRotation(angle * i + offsetAngle);
			r.draw(batch);
			r.setRotation(angle * i + 180 + offsetAngle);
			r.draw(batch);

			// drawing max values (in yellow)
			// tmp = scale(maxValues[histoX]) / 256 + tScale * radius;
			// y.setSize(barWidth * k, 2);
			// y.setPosition((Main.width - barWidth * k) / 2, Main.height / 2 +
			// tmp);
			// y.setOrigin(barWidth * k / 2, -tmp);
			// y.setRotation(angle * i + offsetAngle);
			// y.draw(batch);
			// y.setRotation(angle * i + 180 + offsetAngle);
			// y.draw(batch);

			topValues[histoX] -= FALLING_SPEED;
		}
		if (scale > 0.33 && playing) {
			this.angle += gen.nextBoolean() ? (scale - 0.2) * 20 : -(scale - 0.2) * 20;
		}
		if (effect.isComplete())
			effect.reset();
		if (!ft.finished()) {
			FontManager.getSongNameFont().setColor(1, 1, 1, ft.getFadeX());
			FontManager.getSongNameFont().draw(batch, name, currentSkin.songNamePos.x, currentSkin.songNamePos.y);
			FontManager.getSongNameFont().setColor(1, 1, 1, 1);
		}
		if (changingVolume) {
			volumeft.show();
			Main.volume += Math.signum(factor) * 0.01;
			if (Main.volume < 0) {
				Main.volume = 0;
			}
			if (Main.volume > 1) {
				Main.volume = 1;
			}
			device.setVolume(Main.volume);
		}
		if (!volumeft.finished()) {
			String volumeString = Math.round((Main.volume * 100)) + "%";
			FontManager.getTimeFont().setColor(1, 1, 1, volumeft.getFadeX());
			FontManager.getTimeFont().draw(batch, volumeString, currentSkin.soundPos.x, currentSkin.soundPos.y);
			FontManager.getTimeFont().setColor(1, 1, 1, 1);
		}
		FontManager.getTimeFont().draw(batch, Util.computeTime(playbackTime / 1000), currentSkin.timePassed.x, currentSkin.timePassed.y);
		String s = "-" + Util.computeTime((long) (songLength - playbackTime / 1000));
		FontManager.getTimeFont().draw(batch, s, currentSkin.timeLeft.x, currentSkin.timeLeft.y);
		float timeLeftScale = playbackTime / 1000f / songLength;
		// line.setSize(10 * k, (Main.width - 62) * timeLeftScale);
		// line.setColor(1, 1, 1, 0.1f + 1.5f * (scale > 0.13 ? (0.1f + scale -
		// 0.12f) : 0.1f));
		// line.setPosition(33, 47);
		// line.setOrigin(0, 10 * k / 2);
		// line.setRotation(-90);
		// line.draw(batch);
		// line.setColor(1, 1, 1, 1);
		line.setSize(6 * k, (currentSkin.timeBar.z) * timeLeftScale);
		line.setPosition(currentSkin.timeBar.x, currentSkin.timeBar.y);
		line.setOrigin(0, 6 * k / 2);
		line.setRotation(-90);
		line.draw(batch);
		// effect.setPosition(31 + (Main.width - 62) * timeLeftScale, 45
		// +barWidth * k / 2); effect.draw(batch, Gdx.graphics.getDeltaTime());
		playPause.drawCentered(batch, centerX, centerY);
		currentSkin.rendererCustomParts(batch, false);
		batch.end();
	}

	private float calculateScale() {
		float res = 0;
		for (int i = 0; i < NB_BARS; i++) {
			int histoX = 0;
			if (i < NB_BARS / 2) {
				histoX = NB_BARS / 2 - i;
			} else {
				histoX = i - NB_BARS / 2;
			}

			int nb = (samples.length / NB_BARS) / 2;
			res = Math.max(res, scale(avg(histoX, nb)));
		}
		return res;
	}

	private float scale(float x) {
		return x * Main.height;
	}

	private float avg(int pos, int nb) {
		int sum = 0;
		for (int i = 0; i < nb; i++) {
			sum += spectrum[pos + i];
		}

		return (float) (sum / nb);
	}

	@Override
	public void show() {
	}

	@Override
	public void resize(int width, int height) {
		Main.width = width;
		Main.height = height;
		playPause.setLocation(width / 2, height / 2);
		camera.setToOrtho(false, Main.width, Main.height);
		background.setSize(width, height);
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

	private DecoderInfo initDecoder(String path) {
		FileHandle externalFile = Gdx.files.absolute(path);
		Mpg123Decoder decoder = new Mpg123Decoder(externalFile);
		AudioDevice device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 ? true : false);
		return new DecoderInfo(decoder, device);
	}

	private class DecoderInfo {
		public Mpg123Decoder decoder;
		public AudioDevice device;

		public DecoderInfo(Mpg123Decoder decoder, AudioDevice device) {
			this.decoder = decoder;
			this.device = device;
		}
	}

	public void setPlaying(boolean p) {
		playing = p;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Input.Keys.UP:
			changingVolume = true;
			factor = 1;
			break;
		case Input.Keys.DOWN:
			changingVolume = true;
			factor = -1;
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Input.Keys.ESCAPE:
			Main.getInstance().setScreen(new FileManager());
			break;
		case Input.Keys.SPACE:
			playPause.click();
			break;
		case Input.Keys.DOWN:
		case Input.Keys.UP:
			changingVolume = false;
			break;
		case Input.Keys.F2:
			changingSkin = !changingSkin;
			try {
				if (changingSkin) {
					loadSkin(new Skin(Gdx.files.internal("data/test/test.skn").file()));
				} else {
					loadSkin(Skin.getDefaultSkin());
				}
				if (playing) {
					playPause.changeSkin(pausedImage, 512, 512);
				} else {
					playPause.changeSkin(playImage, 512, 512);
				}
			} catch (Exception e) {
			}
			break;
		default:
			return false;
		}
		return true;
	}

	private void loadBarsTexture(Texture t) {
		b = new Sprite(t, 0, 0, 16, 5);
		r = new Sprite(t, 0, 15, 16, currentSkin.useOldBars ? 5 : 8);
		line = new Sprite(t, 0, 5, 16, 5);
	}

	private void loadSkin(Skin skin) {
		currentSkin = skin;
		pausedImage = currentSkin.pause;
		playImage = currentSkin.play;
		loadBarsTexture(currentSkin.bars);
		background = new Sprite(currentSkin.background);
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}