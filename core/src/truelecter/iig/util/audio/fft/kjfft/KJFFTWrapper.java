package truelecter.iig.util.audio.fft.kjfft;

import truelecter.iig.util.audio.FFT;

public class KJFFTWrapper extends FFT {
    private KJFFT fft;

    public KJFFTWrapper(int SampleSize) {
        this.fft = new KJFFT(500);
    }

    @Override
    public void changeSamplesLength(int newLength) {
        this.fft = new KJFFT(newLength);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        double[] in = new double[samples.length / 2];
        decode(toByte(samples), in);
        spectrum = this.fft.calculate(toFloat(in));
    }
}
