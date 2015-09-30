package truelecter.iig.util.audio.fft.fftpack;

import truelecter.iig.util.audio.FFT;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class FFTPackWrapper extends FFT {
    private RealDoubleFFT fft = null;

    public FFTPackWrapper(int sampleSize) {
        this.fft = new RealDoubleFFT(sampleSize);
    }

    @Override
    public void changeSamplesLength(int sampleSize) {
        this.fft = new RealDoubleFFT(sampleSize);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        double[] x = new double[samples.length / 2];
        decode(toByte(samples), x);
        try {
            this.fft.bt(x);
        } catch (Exception e) {
            this.fft = new RealDoubleFFT(samples.length / 2);
        }
        int j = Math.min(x.length, spectrum.length);
        for (int i = 0; i < j; i++)
            spectrum[i] = (float) x[i] * 3.0f;
    }

}
