package truelecter.iig.util.audio.fft.fftpack;

import truelecter.iig.util.audio.FFT;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class FFTPackWrapper extends FFT {
    private RealDoubleFFT fft = null;

    public FFTPackWrapper(int sampleSize) {
        super(sampleSize);
        this.fft = new RealDoubleFFT(sampleSize);
    }

    @Override
    public void changeSamplesLength(int sampleSize) {
        this.fft = new RealDoubleFFT(sampleSize);
        this.prev = new float[DECAY][sampleSize];
    }

    @Override
    public void dispose() {

    }

    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        double[] x = new double[samples.length];
        for (int i = 0; i < samples.length; i++)
            x[i] = (54 * samples[i]) / (32768.0 * 16);
        try {
            this.fft.ft(x);
        } catch (Exception e) {
            this.fft = new RealDoubleFFT(samples.length);
            this.fft.ft(x);
        }
    }

}
