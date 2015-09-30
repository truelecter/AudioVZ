package truelecter.iig.util.audio.fft;

import truelecter.iig.util.audio.FFT;
import truelecter.iig.util.audio.fft.fftpack.FFTPackWrapper;
import truelecter.iig.util.audio.fft.kissfft.KissFFTWrapper;
import truelecter.iig.util.audio.fft.kjfft.KJFFTWrapper;

public class FFTWrapper {

    public static FFT getFFT(FFTType type, int sampleSize) {
        switch (type) {
        case KJFFT:
            return new KJFFTWrapper(sampleSize);
        case KISSFFT:
            return new KissFFTWrapper(sampleSize);
        case FFTPACK:
            return new FFTPackWrapper(sampleSize);
        default:
            return null;
        }
    }

}
