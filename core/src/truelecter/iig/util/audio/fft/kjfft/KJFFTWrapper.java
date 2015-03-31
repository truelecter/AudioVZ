package truelecter.iig.util.audio.fft.kjfft;

import truelecter.iig.util.audio.FFT;

public class KJFFTWrapper implements FFT {
    private KJFFT fft;

    public KJFFTWrapper(int SampleSize) {
        fft = new KJFFT(SampleSize);
    }

    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        spectrum = fft.calculate(getDoubleArray(samples));
    }

    private double[] getDoubleArray(short[] samples) {
        double[] res = new double[samples.length];
        for (int i = 0; i < samples.length; i++) {
            res[i] = samples[i];
        }
        return res;
    }

    @Override
    public void dispose() {
    }

}
