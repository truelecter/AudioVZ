package truelecter.iig.util.audio.fft.kissfft;

import com.badlogic.gdx.audio.analysis.KissFFT;

import truelecter.iig.util.audio.FFT;

public class KissFFTWrapper implements FFT{
    private KissFFT fft;
    
    public KissFFTWrapper(int sampleSize){
        fft = new KissFFT(sampleSize);
    }
    
    @Override
    public void spectrum(short[] samples, float[] spectrum) {
        fft.spectrum(samples, spectrum);
    }

    @Override
    public void dispose() {
        fft.dispose();
    }

}
