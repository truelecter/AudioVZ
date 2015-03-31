package truelecter.iig.screen;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import javazoom.jl.decoder.Bitstream;
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

import javazoom.jl.decoder.Decoder;

public class AudioSpectrum implements Screen, SubInputProcessor {

    private final float FALLING_SPEED = (1.5f / 3.0f);
    private final float LINE_SCALE = 2f;
    private final int NB_BARS = 40;
    private static AudioSpectrum instance = null;

    private Sprite background;
    private float radius = 256;
    private Mpg123Decoder decoder;
    private AudioDevice device;
    private boolean playing = false;
    private Thread playbackThread;
    private short[] samples = new short[2048];
    private float[] spectrum = new float[2048];
    private float[] topValues = new float[2048];
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private float barWidth = 8;

    private Texture pausedImage;
    private Texture playImage;

    private Sprite b;
    private Sprite line;
    private Sprite r;

    private Button playPause;
    private Vector2 fadeTime;

    // TESTAREA_BEGIN
    // TESTAREA_END

    private FadingText ft = new FadingText(0.05f, 5000);
    private FadingText volumeft = new FadingText(0.05f, 6000);
    private Skin currentSkin;

    private Random gen = new Random(1337);
    private float angle;
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

    public static void onAndroidPause() {
        if (ConfigHandler.pauseOnHide && instance != null) {
            instance.pause();
        }
    }

    public AudioSpectrum() {
        this("!INTERNAL!/data/default.mp3", true, null);
        defaultSong = true;
    }

    public AudioSpectrum(boolean playing) {
        this("!INTERNAL!/data/default.mp3", playing, null);
        defaultSong = true;
    }

    public AudioSpectrum(boolean dummy, String name) {
        this("!INTERNAL!/data/default.mp3", true, name);
        defaultSong = true;
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

    public AudioSpectrum(String filenameN, boolean p, String name) {
        String tfilename = filenameN.replace("!INTERNAL!", Gdx.files.getLocalStoragePath()).replace("!EXTERNAL!",
                Gdx.files.getExternalStoragePath());
        while (tfilename.contains("/")) {
            tfilename = tfilename.replace("/", "\\");
        }
        while (tfilename.contains("\\\\")) {
            tfilename = tfilename.replace("\\\\", "\\");
        }
        while (tfilename.contains("\\")) {
            tfilename = tfilename.replace("\\", "?");
        }
        while (tfilename.contains("?")) {
            tfilename = tfilename.replace("?", File.separator);
        }
        this.filename = tfilename;
        playing = p;
        camera = new OrthographicCamera();
        try {
            currentSkin = Skin.skinByPath(ConfigHandler.skinPath);
        } catch (Exception e) {
            Logger.e("Invalid skin properties!", e);
        }
        loadSkin(currentSkin);
        camera.setToOrtho(false, ConfigHandler.width, ConfigHandler.height);

        batch = new SpriteBatch();

        final FFT fft = FFTWrapper.getFFT(FFTType.KISSFFT, 2500);

        for (int i = 0; i < spectrum.length; i++) {
            topValues[i] = 0;
        }
        FileHandle externalFile = Gdx.files.absolute(filename);
        final Mpg123Decoder decoder = new Mpg123Decoder(externalFile);
        device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 ? true : false);
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.i("Playback thread started!");
                long toStay = 5000;
                long totalSamples = 0;
                Bitstream bitStream = null;
                try {
                    bitStream = new Bitstream(new FileInputStream(filename));
                } catch (Exception e) {
                    Logger.w("Bit stream init error", e);
                }
                Decoder decode = new Decoder();
                do {
                    if (playing) {
                        try {
                            samples = ((SampleBuffer) decode.decodeFrame(bitStream.readFrame(), bitStream)).getBuffer();
                        } catch (Exception e) {
                            samples = new short[0];
                            Logger.w("Error decoding file", e);
                        }
                        totalSamples += samples.length;
                        fft.spectrum(samples, spectrum);
                        try {
                            device.writeSamples(samples, 0, samples.length);
                        } catch (Exception e) {
                            Logger.w("Error while writing to audio device", e);
                        }
                        playbackTime = totalSamples * 500 / decoder.getRate();
                        bitStream.closeFrame();
                    }
                } while (samples.length > 0 && !needToChange);
                if (!ConfigHandler.autoPlay) {
                    long lastTime = System.currentTimeMillis();
                    do {
                        toStay += lastTime - System.currentTimeMillis();
                        lastTime = System.currentTimeMillis();
                    } while (toStay > 1);
                }
                device.dispose();
                decoder.dispose();
                needToChange = true;
                fft.dispose();
            }
        });
        device.setVolume(ConfigHandler.volume);
        playPause = new Button(pausedImage, ConfigHandler.width / 2, ConfigHandler.height / 2, radius * 2, radius * 2,
                new Function() {
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
        angle = 10;
        this.name = name == null ? this.filename.substring(this.filename.lastIndexOf('\\') + 1,
                this.filename.length() - 4) : name;
        songLength = (long) decoder.getLength();
        fadeTime = new Vector2(0, 0);
        pixel.setSize(ConfigHandler.width, ConfigHandler.height);
        playbackThread.setDaemon(true);
        playbackThread.start();
        instance = this;
        options = new Options();
        optionsButton = new Button(new Texture("data/icons/settings.png"), 20f, 20f, 60f, 60f, new Function() {
            @Override
            public void toRun() {
                options.toggle();
            }
        });
        optionsButton.setPriority(99);
        if (!defaultSong)
            nextButton = new Button(new Texture("data/icons/fastforward.png"), ConfigHandler.width - 80, 20f, 60f, 60f,
                    new Function() {
                        @Override
                        public void toRun() {
                            needToChange = true && !defaultSong;
                            ConfigHandler.nextButtonPressed = true;
                        }
                    });
        ConfigHandler.autoPlayReady = true;
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
                playbackThread.interrupt();
                playbackThread = null;
            }
        });
        System.gc();
        System.out.println("AudioSpectrum disposed!");
    }

    public void remove() {
        GlobalInputProcessor.remove(playPause);
        GlobalInputProcessor.remove(this);
        options.dispose();
        optionsButton.dispose();
        nextButton.dispose();
    }

    @Override
    public void render(float delta) {
        if (needToChange) {
            dispose();
            Main.getInstance().setScreen(new FileManager());
            return;
        }

        if (paused && Main.aa != null) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fadeTime = fadeTime.lerp(new Vector2(1, 0), 0.01f);
        float scale = calculateScale() / 125 / 4096 + 0.2f;
        float angle = 180f / NB_BARS;
        float offsetAngle = -this.angle;
        float k = ConfigHandler.width / 910f;
        float tScale = (float) (Math.pow(Math.PI, scale + 1) / 10f) * k;
        camera.update();
        float vX = (float) (ConfigHandler.width * (-0.4377 + tScale / k)) / 2.5f;
        float vY = (float) (ConfigHandler.height * (-0.4377 + tScale / k)) / 2.5f;
        if (ConfigHandler.scaleBackground)
            if (tScale / k > 0.4377) {
                background.setSize(vX + ConfigHandler.width, vY + ConfigHandler.height);
                background.setPosition(-vX / 2, -vY / 2);
            } else {
                background.setSize(ConfigHandler.width, ConfigHandler.height);
                background.setPosition(0, 0);
            }
        // Anaglyph_BEGIN
        if (ConfigHandler.useShaders) {
            if (tScale / k > 0.4377) {
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
                batch.begin();
                float x = background.getX();
                float y = background.getY();
                background.setPosition(x + vX / 4, y);
                background.setColor(0, 1, 1, 1);
                background.draw(batch);
                background.setPosition(x - vX / 4, y);
                background.setColor(1, 0, 0, 1);
                background.draw(batch);
                background.setPosition(x, y);
                batch.end();
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                batch.begin();
            } else {
                batch.begin();
                background.setColor(1, 1, 1, 1);
                background.draw(batch);
            }
        } else {
            batch.begin();
            background.setColor(1, 1, 1, 1);
            background.draw(batch);
        }
        // Anaglyph_BEGIN
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

            topValues[histoX] -= FALLING_SPEED;
        }
        if (ConfigHandler.offsetAngle)
            if (scale > 0.33 && playing) {
                this.angle += gen.nextBoolean() ? (scale - 0.2) * 20 : -(scale - 0.2) * 20;
            }
        if (!ft.finished()) {
            currentSkin.songName.setColor(1, 1, 1, ft.getFadeX());
            currentSkin.songName.draw(batch, name, currentSkin.songNamePos.x, currentSkin.songNamePos.y);
            currentSkin.songName.setColor(1, 1, 1, 1);
        }
        if (changingVolume) {
            volumeft.show();
            ConfigHandler.volume += Math.signum(factor) * 0.01;
            if (ConfigHandler.volume < 0) {
                ConfigHandler.volume = 0;
            }
            if (ConfigHandler.volume > 1) {
                ConfigHandler.volume = 1;
            }
            device.setVolume(ConfigHandler.volume);
        }
        if (!volumeft.finished() && Main.aa == null) {
            String volumeString = Math.round((ConfigHandler.volume * 100)) + "%";
            currentSkin.volumeFont.setColor(1, 1, 1, volumeft.getFadeX());
            currentSkin.volumeFont.draw(batch, volumeString, currentSkin.soundPos.x, currentSkin.soundPos.y);
            currentSkin.volumeFont.setColor(1, 1, 1, 1);
        }
        currentSkin.timeFont.draw(batch, Util.computeTime(playbackTime / 1000), currentSkin.timePassed.x,
                currentSkin.timePassed.y);
        String s = "-" + Util.computeTime((long) (songLength - playbackTime / 1000));
        currentSkin.timeFont.draw(batch, s, currentSkin.timeLeft.x, currentSkin.timeLeft.y);
        float timeLeftScale = playbackTime / 1000f / songLength;
        line.setSize(currentSkin.timeBar.t * k, (currentSkin.timeBar.z) * timeLeftScale);
        line.setPosition(currentSkin.timeBar.x, currentSkin.timeBar.y);
        line.setOrigin(0, currentSkin.timeBar.t * k / 2);
        line.setRotation(-90);
        line.draw(batch);
        playPause.drawCentered(batch, centerX, centerY);
        currentSkin.rendererCustomParts(batch, false);
        if (ConfigHandler.showButtons) {
            options.render(batch);
            optionsButton.drawCentered(batch, 50, 50);
            if (!defaultSong)
                nextButton.drawCentered(batch, ConfigHandler.width - 50, 50);
        }
        pixel.setColor(0, 0, 0, 1 - fadeTime.x);
        pixel.draw(batch);
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

            int nb = (spectrum.length / NB_BARS) / 2;
            res = Math.max(res, scale(avg(histoX, nb)));
        }
        return res;
    }

    private float scale(float x) {
        return x * ConfigHandler.height;
    }

    private float avg(int pos, int nb) {
        int sum = 0;
        for (int i = 0; i < nb; i++) {
            sum += spectrum[pos + i];
        }
        if (nb == 0) {
            return 0;
        }
        return (float) (sum / nb);
    }

    @Override
    public void show() {
        GlobalInputProcessor.removeAllOfClass(this.getClass());
        GlobalInputProcessor.register(this);
    }

    @Override
    public void resize(int width, int height) {
        ConfigHandler.width = width;
        ConfigHandler.height = height;
        playPause.setLocation(width / 2, height / 2);
        camera.setToOrtho(false, ConfigHandler.width, ConfigHandler.height);
        background.setSize(width, height);
    }

    @Override
    public void pause() {
        if (ConfigHandler.pauseOnHide) {
            playing = false;
            changeButtonSkinAccordingOnPlaying();
        }
        paused = true;
    }

    @Override
    public void resume() {
        if (ConfigHandler.pauseOnHide) {
            if (!playing) {
                playing = true;
            }
            changeButtonSkinAccordingOnPlaying();
        }
        paused = false;
    }

    @Override
    public void hide() {
        dispose();
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
        case Input.Keys.BACK:
            ConfigHandler.autoPlayReady = false;
            Main.getInstance().setScreen(new FileManager());
            break;
        case Input.Keys.SPACE:
            playPause.click();
            break;
        case Input.Keys.DOWN:
        case Input.Keys.UP:
            changingVolume = false;
            break;
        case Input.Keys.R:
            try {
                loadSkin(new Skin(currentSkin.skinFile));
                changeButtonSkinAccordingOnPlaying();
            } catch (Exception e) {
                Logger.w("Error while refreshing skin", e);
            }
            break;
        case Input.Keys.RIGHT:
            needToChange = true && !defaultSong;
            ConfigHandler.nextButtonPressed = true;
            break;
        default:
            return false;
        }
        return true;
    }

    private void changeButtonSkinAccordingOnPlaying() {
        if (playing) {
            playPause.changeSkin(pausedImage, 512, 512);
        } else {
            playPause.changeSkin(playImage, 512, 512);
        }
    }

    private void loadBarsTexture(Texture t) {
        b = new Sprite(t, 0, 0, 16, 5);
        r = new Sprite(t, 0, 15, 16, currentSkin.useOldBars ? 5 : 8);
        line = new Sprite(t, 0, 5, 16, 5);
    }

    private void loadSkin(Skin skin) {
        if (skin == null) {
            try {
                currentSkin = Skin.getDefaultSkin();
            } catch (Exception e) {
                Logger.e("Skin loading error", e);
                Gdx.app.exit();
            }
        }
        currentSkin = skin;
        pausedImage = currentSkin.pause;
        playImage = currentSkin.play;
        loadBarsTexture(currentSkin.bars);
        background = new Sprite(currentSkin.background);
        int width = currentSkin.background.getWidth();
        int height = currentSkin.background.getHeight();
        float scale = Math.min(width * 1.0f / ConfigHandler.width, height * 1.0f / ConfigHandler.height);
        background.setSize(width / scale, height / scale);
        centerX = currentSkin.button.x;
        centerY = currentSkin.button.y;
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

    @Override
    public int getPriority() {
        return 2;
    }

}