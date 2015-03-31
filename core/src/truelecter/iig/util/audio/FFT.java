package truelecter.iig.util.audio;

public interface FFT {
    public void spectrum(short[] samples, float[] spectrum);
    public void dispose();
}
