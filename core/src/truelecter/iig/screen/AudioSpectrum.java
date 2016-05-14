package truelecter.iig.screen;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;
import truelecter.iig.Main;
import truelecter.iig.screen.visual.Button;
import truelecter.iig.screen.visual.FadingText;
import truelecter.iig.screen.visual.Skin;
import truelecter.iig.screen.visual.menu.Options;
import truelecter.iig.util.ConfigHandler;
import truelecter.iig.util.Function;
import truelecter.iig.util.Logger;
import truelecter.iig.util.Util;
import truelecter.iig.util.audio.FFT;
import truelecter.iig.util.audio.fft.FFTType;
import truelecter.iig.util.audio.fft.FFTWrapper;
import truelecter.iig.util.input.GlobalInputProcessor;
import truelecter.iig.util.input.SubInputProcessor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Main visualization screen
 * 
 * @author _TrueLecter_
 *
 */
public class AudioSpectrum implements Screen, SubInputProcessor {

    // Last instance of this class
    private static AudioSpectrum instance = null;

    /**
     * Handles android onPause() event
     */
    public static void onAndroidPause() {
        if (ConfigHandler.pauseOnHide && (instance != null))
            instance.pause();
    }

    private final float FALLING_SPEED = (1.5f / 3.0f);
    // Spectrum lines scale
    private final float LINE_SCALE = 2f;

    // Number of bars
    private final int NB_BARS = 60;
    // Background image
    private Sprite background;
    // Button radius
    private float radius = 256;
    // MP3 decoder
    private Mpg123Decoder decoder;
    private AudioDevice device;
    // Is playing paused or not
    private boolean playing = false;
    // Thread, where we are decoding our MP3
    private Thread playbackThread;
    private short[] samples = new short[2048];
    private float[] spectrum = new float[2048];
    private float[] topValues = new float[2048];
    private OrthographicCamera camera;
    // Batch to draw on
    private SpriteBatch batch;
    private float barWidth = 8;

    // DPI ratio
    private float dpiK = (Gdx.graphics.getDensity() * 160) / 190;
    // Textures of paused and playing button
    private Texture pausedImage;

    private Texture playImage;
    // Sprites of bar skin
    private Sprite b;
    private Sprite line;

    private Sprite r;

    // Center button
    private Button playPause;

    // TESTAREA_BEGIN
    // TESTAREA_END

    private Vector2 fadeTime;
    private FadingText ft = new FadingText(0.05f, 5000);
    private FadingText volumeft = new FadingText(0.05f, 6000);

    private Skin currentSkin;
    private Random gen = new Random(1337);
    // Bars angle
    private float angle;
    // Do we need to change screen?
    private boolean needToChange = false;
    private String name;
    private String filename;
    private long playbackTime = 0;
    private long songLength = 0;
    private float centerX = ConfigHandler.width / 2;
    private float centerY = ConfigHandler.height / 2;

    private Sprite pixel = new Sprite(new Texture(Gdx.files.internal("pix.bmp")));
    private boolean changingVolume = false;
    private int factor = 1;
    private Options options;
    private Button optionsButton;
    private Button nextButton;
    private boolean defaultSong = false;

    private boolean paused;

    /**
     * Load song preview
     */
    public AudioSpectrum() {
        this("!INTERNAL!/data/default.mp3", true, null);
        this.defaultSong = true;
    }

    /**
     * Load song preview
     * 
     * @param playing
     *            - play song or not
     */
    public AudioSpectrum(boolean playing) {
        this("!INTERNAL!/data/default.mp3", playing, null);
        this.defaultSong = true;
    }

    public AudioSpectrum(boolean dummy, String name) {
        this("!INTERNAL!/data/default.mp3", true, name);
        this.defaultSong = true;
    }

    /**
     * Start visualization of <b>file</b>
     * 
     * @param file
     *            - file to play
     */
    public AudioSpectrum(File file) {
        this(file.getAbsolutePath(), true, null);
    }

    /**
     * Init visualization of playing <b>file</b>
     * 
     * @param file
     *            - file to play
     * @param playing
     *            - start playing or not
     */
    public AudioSpectrum(File file, boolean playing) {
        this(file.getAbsolutePath(), playing, null);
    }

    /**
     * Init visualization of playing <b>file</b>
     * 
     * @param file
     *            - file to play
     * @param playing
     *            - start playing or not
     * @param name
     *            - name to show
     */
    public AudioSpectrum(File file, boolean playing, String name) {
        this(file.getAbsolutePath(), playing, name);
    }

    /**
     * Start visualization of <b>file</b>
     * 
     * @param file
     *            - file to play
     * @param name
     *            - name to show
     */
    public AudioSpectrum(File file, String name) {
        this(file.getAbsolutePath(), true, name);
    }

    /**
     * Start visualization of <b>filename</b>
     * 
     * @param filename
     *            - path for file to play
     */
    public AudioSpectrum(String filename) {
        this(filename, true, null);
    }

    /**
     * Main constructor
     * 
     * @param filenameN
     *            - path of file to play
     * @param p
     *            - begin playing or not
     * @param name
     *            - name to show
     */
    public AudioSpectrum(String filenameN, boolean p, String name) {
        // Normalize file path
        String tfilename = filenameN.replace("!INTERNAL!", Gdx.files.getLocalStoragePath()).replace("!EXTERNAL!",
                Gdx.files.getExternalStoragePath());
        while (tfilename.contains("/"))
            tfilename = tfilename.replace("/", "\\");
        while (tfilename.contains("\\\\"))
            tfilename = tfilename.replace("\\\\", "\\");
        while (tfilename.contains("\\"))
            tfilename = tfilename.replace("\\", "?");
        while (tfilename.contains("?"))
            tfilename = tfilename.replace("?", File.separator);
        this.filename = tfilename;
        this.playing = p;
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, ConfigHandler.width, ConfigHandler.height);
        // Init and load skin
        this.currentSkin = Skin.skinByPath(ConfigHandler.skinPath);
        this.loadSkin(this.currentSkin);

        this.batch = new SpriteBatch();

        for (int i = 0; i < this.spectrum.length; i++)
            this.topValues[i] = 0;
        // Prepare decoder, fft and audio device
        FileHandle externalFile = Gdx.files.absolute(this.filename);
        final Mpg123Decoder decoder = new Mpg123Decoder(externalFile);
        this.device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 ? true : false);
        final FFT fft = FFTWrapper.getFFT(FFTType.KISSFFT, 2500);
        this.playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.i("Playback thread started!");
                // Pause before changing screen
                long toStay = 5000;
                // Total samples read
                long totalSamples = 0;
                Bitstream bitStream = null;
                try {
                    bitStream = new Bitstream(new FileInputStream(AudioSpectrum.this.filename));
                } catch (Exception e) {
                    Logger.w("Bit stream init error", e);
                }
                Decoder decode = new Decoder();
                do
                    if (AudioSpectrum.this.playing) {
                        try {
                            AudioSpectrum.this.samples = ((SampleBuffer) decode.decodeFrame(bitStream.readFrame(),
                                    bitStream)).getBuffer();
                        } catch (Exception e) {
                            AudioSpectrum.this.samples = new short[0];
                            Logger.w("Error decoding file", e);
                        }
                        totalSamples += AudioSpectrum.this.samples.length;
                        try {
                            fft.spectrumDecay(AudioSpectrum.this.samples, AudioSpectrum.this.spectrum);
                            // Util.printArray(AudioSpectrum.this.spectrum);
                        } catch (Exception eoob) {
                            Logger.e("Something went wrong.", eoob);
                            fft.changeSamplesLength(AudioSpectrum.this.samples.length);
                            AudioSpectrum.this.spectrum = new float[2048];
                        }
                        try {
                            AudioSpectrum.this.device.writeSamples(AudioSpectrum.this.samples, 0,
                                    AudioSpectrum.this.samples.length);
                        } catch (Exception e) {
                            Logger.w("Error while writing to audio device", e);
                        }
                        AudioSpectrum.this.playbackTime = (totalSamples * 500) / decoder.getRate();
                        bitStream.closeFrame();
                    }
                while ((AudioSpectrum.this.samples.length > 0) && !AudioSpectrum.this.needToChange);
                if (!ConfigHandler.autoPlay) {
                    long lastTime = System.currentTimeMillis();
                    do {
                        toStay += lastTime - System.currentTimeMillis();
                        lastTime = System.currentTimeMillis();
                    } while (toStay > 1);
                }
                AudioSpectrum.this.device.dispose();
                decoder.dispose();
                AudioSpectrum.this.needToChange = true;
                fft.dispose();
            }
        });
        this.device.setVolume(ConfigHandler.volume);
        // Create our pause/play button
        this.playPause = new Button(this.pausedImage, ConfigHandler.width / 2, ConfigHandler.height / 2,
                this.radius * 2, this.radius * 2, new Function() {
                    @Override
                    public void toRun() {
                        AudioSpectrum.this.playing = !AudioSpectrum.this.playing;
                        if (AudioSpectrum.this.playing)
                            AudioSpectrum.this.playPause.changeSkin(AudioSpectrum.this.pausedImage, 512, 512);
                        else
                            AudioSpectrum.this.playPause.changeSkin(AudioSpectrum.this.playImage, 512, 512);
                    }
                });
        // Default bars angle
        this.angle = 10;
        // If name is null - set it to file name
        this.name = name == null ? this.filename.substring(this.filename.lastIndexOf('\\') + 1,
                this.filename.length() - 4) : name;
        // Retrieve length of song
        this.songLength = (long) decoder.getLength();
        // Fade-in preparations
        this.fadeTime = new Vector2(0, 0);
        this.pixel.setSize(ConfigHandler.width, ConfigHandler.height);
        this.playbackThread.setDaemon(true);
        this.playbackThread.start();
        instance = this;
        this.options = new Options();
        this.optionsButton = new Button(new Texture("data/icons/settings.png"), 20f, 20f, 60f, 60f, new Function() {
            @Override
            public void toRun() {
                AudioSpectrum.this.options.toggle();
            }
        });
        this.optionsButton.setPriority(99);
        // If it is not our preview song - show button to play next song in list
        if (!this.defaultSong)
            this.nextButton = new Button(new Texture("data/icons/fastforward.png"), ConfigHandler.width - 80, 20f, 60f,
                    60f, new Function() {
                        @Override
                        public void toRun() {
                            AudioSpectrum.this.needToChange = true && !AudioSpectrum.this.defaultSong;
                            ConfigHandler.nextButtonPressed = true;
                        }
                    });
        // Are we ready to play?
        ConfigHandler.autoPlayReady = true;
    }

    /**
     * Calculate spectrum average for visualization
     */
    private float avg(int pos, int nb) {
        int sum = 0;
        for (int i = 0; i < nb; i++)
            sum += this.spectrum[pos + i];
        if (nb == 0)
            return 0;
        return Math.abs(sum / nb);
    }

    private float calculateScale() {
        float res = 0;
        for (int i = 0; i < this.NB_BARS; i++) {
            int histoX = 0;
            if (i < (this.NB_BARS / 2))
                histoX = (this.NB_BARS / 2) - i;
            else
                histoX = i - (this.NB_BARS / 2);

            int nb = (this.spectrum.length / this.NB_BARS) / 2;
            res = Math.max(res, this.scale(this.avg(histoX, nb)));
        }
        return res;
    }

    /**
     * Change image of play button
     */
    private void changeButtonSkinAccordingOnPlaying() {
        if (this.playing)
            this.playPause.changeSkin(this.pausedImage, 512, 512);
        else
            this.playPause.changeSkin(this.playImage, 512, 512);
    }

    @Override
    public void dispose() {
        this.remove();
        this.playing = false;
        Util.ignoreErrors(new Function() {
            @Override
            public void toRun() {
                AudioSpectrum.this.device.dispose();
                AudioSpectrum.this.device = null;
            }
        });
        Util.ignoreErrors(new Function() {
            @Override
            public void toRun() {
                AudioSpectrum.this.decoder.dispose();
                AudioSpectrum.this.decoder = null;
            }
        });
        Util.ignoreErrors(new Function() {
            @Override
            public void toRun() {
                AudioSpectrum.this.playbackThread.interrupt();
                AudioSpectrum.this.playbackThread = null;
            }
        });
        // Collect some garbage
        System.gc();
        System.out.println("AudioSpectrum disposed!");
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public void hide() {
        this.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
        case Input.Keys.UP:
            this.changingVolume = true;
            this.factor = 1;
            break;
        case Input.Keys.DOWN:
            this.changingVolume = true;
            this.factor = -1;
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
    public boolean keyUp(int keycode) {
        switch (keycode) {
        case Input.Keys.ESCAPE:
        case Input.Keys.BACK:
            ConfigHandler.autoPlayReady = false;
            Main.getInstance().setScreen(new FileManager());
            break;
        case Input.Keys.SPACE:
            this.playPause.click();
            break;
        case Input.Keys.DOWN:
        case Input.Keys.UP:
            this.changingVolume = false;
            break;
        case Input.Keys.R:
            try {
                this.loadSkin(new Skin(this.currentSkin.skinFile));
                this.changeButtonSkinAccordingOnPlaying();
            } catch (Exception e) {
                Logger.w("Error while refreshing skin", e);
            }
            break;
        case Input.Keys.RIGHT:
            this.needToChange = true && !this.defaultSong;
            ConfigHandler.nextButtonPressed = true;
            break;
        default:
            return false;
        }
        return true;
    }

    /**
     * Used to pars skin texture of bars.
     * 
     * @param t
     *            - {@link Texture} to parse
     */
    private void loadBarsTexture(Texture t) {
        this.b = new Sprite(t, 0, 0, 16, 5);
        this.r = new Sprite(t, 0, 15, 16, this.currentSkin.useOldBars ? 5 : 8);
        this.line = new Sprite(t, 0, 5, 16, 5);
    }

    /**
     * Parse and load <b>skin</b>. Exit, if failed to load skin.
     * 
     * @param skin
     *            - {@link Skin} to load.
     */
    private void loadSkin(Skin skin) {
        if (skin == null)
            try {
                this.currentSkin = Skin.getDefaultSkin();
            } catch (Exception e) {
                Logger.e("Skin loading error", e);
                Gdx.app.exit();
            }
        this.currentSkin = skin;
        this.pausedImage = this.currentSkin.pause;
        this.playImage = this.currentSkin.play;
        this.loadBarsTexture(this.currentSkin.bars);
        this.background = new Sprite(this.currentSkin.background);
        int width = this.currentSkin.background.getWidth();
        int height = this.currentSkin.background.getHeight();
        float scale = Math.min((width * 1.0f) / ConfigHandler.width, (height * 1.0f) / ConfigHandler.height);
        this.background.setSize(width / scale, height / scale);
        this.centerX = this.currentSkin.button.x;
        this.centerY = this.currentSkin.button.y;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public void pause() {
        if (ConfigHandler.pauseOnHide) {
            this.playing = false;
            this.changeButtonSkinAccordingOnPlaying();
        }
        this.paused = true;
    }

    /**
     * Remove all {@link InputProcessor}s from regisrty
     */
    public void remove() {
        GlobalInputProcessor.remove(this.playPause);
        GlobalInputProcessor.remove(this);
        this.options.dispose();
        this.optionsButton.dispose();
        this.nextButton.dispose();
    }

    @Override
    public void render(float delta) {
        // Change screen if it is needed
        if (this.needToChange) {
            this.dispose();
            Main.getInstance().setScreen(new FileManager());
            return;
        }
        // If paused - don't render
        if (this.paused && (Main.aa != null))
            return;

        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Fade-out for song name, artist etc.
        this.fadeTime = this.fadeTime.lerp(new Vector2(1, 0), 0.01f);
        // Calculating scale and angles for bars.
        float scale = (this.calculateScale() / 125 / 4096) + 0.2f;
        float angle = 180f / this.NB_BARS;
        float offsetAngle = -this.angle;
        float k = ConfigHandler.width / 910f;
        float tScale = (float) (Math.pow(Math.PI, scale + 1) / 10f) * k;
        // Idk why I wrote this :)
        this.camera.update();
        float vX = (float) (ConfigHandler.width * (-0.4377 + (tScale / k))) / 2.5f;
        float vY = (float) (ConfigHandler.height * (-0.4377 + (tScale / k))) / 2.5f;
        // Background scaling according to beats
        if (ConfigHandler.scaleBackground)
            if ((tScale / k) > 0.4377) {
                this.background.setSize(vX + ConfigHandler.width, vY + ConfigHandler.height);
                this.background.setPosition(-vX / 2, -vY / 2);
            } else {
                this.background.setSize(ConfigHandler.width, ConfigHandler.height);
                this.background.setPosition(0, 0);
            }
        // Anaglyph_BEGIN
        if (ConfigHandler.useShaders) {
            if ((tScale / k) > 0.4377) {
                this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                this.batch.begin();
                float x = this.background.getX();
                float y = this.background.getY();
                this.background.setPosition(x + ((vX / 4) * this.dpiK), y);
                this.background.setColor(0, 1, 1, 1);
                this.background.draw(this.batch);
                this.background.setPosition(x - ((vX / 4) * this.dpiK), y);
                this.background.setColor(1, 0, 0, 1);
                this.background.draw(this.batch);
                this.background.setPosition(x, y);
                this.batch.end();
                this.batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                this.batch.begin();
            } else {
                this.batch.begin();
                this.background.setColor(1, 1, 1, 1);
                this.background.draw(this.batch);
            }
        } else {
            this.batch.begin();
            this.background.setColor(1, 1, 1, 1);
            this.background.draw(this.batch);
        }
        // Anaglyph_BEGIN
        this.currentSkin.rendererCustomParts(this.batch, true);
        this.batch.setProjectionMatrix(this.camera.combined);
        this.playPause.setScale(tScale);
        for (int i = 0; i < this.NB_BARS; i++) {
            int histoX = 0;
            if (i < (this.NB_BARS / 2))
                histoX = (this.NB_BARS / 2) - i;
            else
                histoX = i - (this.NB_BARS / 2);

            int nb = (this.samples.length / this.NB_BARS) / 2;
            float avg = this.avg(histoX, nb) / (this.NB_BARS / 40) / this.LINE_SCALE;

            if (avg > this.topValues[histoX])
                this.topValues[histoX] = avg;

            float tmp = 0;

            // drawing spectrum
            tmp = (this.scale(avg) / 256) + (tScale * this.radius);
            this.b.setSize(this.barWidth * k, tmp);
            this.b.setPosition(this.centerX - ((this.barWidth * k) / 2), this.centerY);
            this.b.setOrigin((this.barWidth * k) / 2, 0);
            this.b.setRotation((angle * i) + offsetAngle);
            this.b.draw(this.batch);
            this.b.setRotation((angle * i) + 180 + offsetAngle);
            this.b.draw(this.batch);

            // drawing top values
            tmp = (this.scale(this.currentSkin.useOldBars ? this.topValues[histoX] : avg) / 256)
                    + (tScale * this.radius);
            this.r.setSize(this.barWidth * k, 4);
            this.r.setPosition(this.centerX - ((this.barWidth * k) / 2), this.centerY + tmp);
            this.r.setOrigin((this.barWidth * k) / 2, -tmp);
            this.r.setRotation((angle * i) + offsetAngle);
            this.r.draw(this.batch);
            this.r.setRotation((angle * i) + 180 + offsetAngle);
            this.r.draw(this.batch);

            this.topValues[histoX] -= this.FALLING_SPEED;
        }
        // Changing bars rotation if enabled in config.
        if (ConfigHandler.offsetAngle)
            if ((scale > 0.33) && this.playing)
                this.angle += this.gen.nextBoolean() ? (scale - 0.2) * 20 : -(scale - 0.2) * 20;
        // Do we need to render song name?
        if (!this.ft.finished()) {
            this.currentSkin.songName.setColor(1, 1, 1, this.ft.getFadeX());
            this.currentSkin.songName.draw(this.batch, this.name, this.currentSkin.songNamePos.x,
                    this.currentSkin.songNamePos.y);
            this.currentSkin.songName.setColor(1, 1, 1, 1);
        }
        // Handle if one of key changing buttons is pressed and change volume.
        if (this.changingVolume) {
            this.volumeft.show();
            ConfigHandler.volume += Math.signum(this.factor) * 0.01;
            if (ConfigHandler.volume < 0)
                ConfigHandler.volume = 0;
            if (ConfigHandler.volume > 1)
                ConfigHandler.volume = 1;
            this.device.setVolume(ConfigHandler.volume);
        }
        // Show volume pointer, if we've changed
        if (!this.volumeft.finished() && (Main.aa == null)) {
            String volumeString = Math.round((ConfigHandler.volume * 100)) + "%";
            this.currentSkin.volumeFont.setColor(1, 1, 1, this.volumeft.getFadeX());
            this.currentSkin.volumeFont.draw(this.batch, volumeString, this.currentSkin.soundPos.x,
                    this.currentSkin.soundPos.y);
            this.currentSkin.volumeFont.setColor(1, 1, 1, 1);
        }
        this.currentSkin.timeFont.draw(this.batch, Util.computeTime(this.playbackTime / 1000),
                this.currentSkin.timePassed.x, this.currentSkin.timePassed.y);
        String s = "-" + Util.computeTime(this.songLength - (this.playbackTime / 1000));
        this.currentSkin.timeFont.draw(this.batch, s, this.currentSkin.timeLeft.x, this.currentSkin.timeLeft.y);
        float timeLeftScale = this.playbackTime / 1000f / this.songLength;
        this.line.setSize(this.currentSkin.timeBar.t * k, (this.currentSkin.timeBar.z) * timeLeftScale);
        this.line.setPosition(this.currentSkin.timeBar.x, this.currentSkin.timeBar.y);
        this.line.setOrigin(0, (this.currentSkin.timeBar.t * k) / 2);
        this.line.setRotation(-90);
        this.line.draw(this.batch);
        // Draw button
        this.playPause.drawCentered(this.batch, this.centerX, this.centerY);
        // Render skin custom parts
        this.currentSkin.rendererCustomParts(this.batch, false);
        // If option is checked
        if (ConfigHandler.showButtons) {
            this.options.render(this.batch);
            this.optionsButton.drawCentered(this.batch, 50, 50);
            if (!this.defaultSong)
                this.nextButton.drawCentered(this.batch, ConfigHandler.width - 50, 50);
        }
        // Fading in
        this.pixel.setColor(0, 0, 0, 1 - this.fadeTime.x);
        this.pixel.draw(this.batch);
        // End drawing
        this.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        ConfigHandler.width = width;
        ConfigHandler.height = height;
        this.playPause.setLocation(width / 2, height / 2);
        this.camera.setToOrtho(false, ConfigHandler.width, ConfigHandler.height);
        this.background.setSize(width, height);
    }

    @Override
    public void resume() {
        if (ConfigHandler.pauseOnHide) {
            if (!this.playing)
                this.playing = true;
            this.changeButtonSkinAccordingOnPlaying();
        }
        this.paused = false;
    }

    private float scale(float x) {
        return x * ConfigHandler.height;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public void setPlaying(boolean p) {
        this.playing = p;
    }

    @Override
    public void show() {
        GlobalInputProcessor.removeAllOfClass(this.getClass());
        GlobalInputProcessor.register(this);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

}