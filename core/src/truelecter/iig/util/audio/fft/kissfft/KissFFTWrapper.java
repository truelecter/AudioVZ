package truelecter.iig.util.audio.fft.kissfft;

import truelecter.iig.util.audio.FFT;

import com.badlogic.gdx.audio.analysis.KissFFT;

public class KissFFTWrapper extends FFT {
    private KissFFT fft;

    public KissFFTWrapper(int sampleSize) {
        super(sampleSize);
        this.fft = new KissFFT(sampleSize);
    }

    @Override
    public void changeSamplesLength(int newLength) {
        try {
            this.fft.dispose();
        } catch (Exception e) {
        }
        this.fft = new KissFFT(newLength);
    }

    @Override
    public void dispose() {
        this.fft.dispose();
    }

    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        this.fft.spectrum(samples, spectrum);
    }

}
