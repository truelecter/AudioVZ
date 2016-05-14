package truelecter.iig.util.audio;

public abstract class FFT {
    protected static final int DECAY = 3;

    public static void decode(short[] input, double[] output) {
        for (int i = 0; i < output.length; i++)
            output[i] = input[i] / 32768.0;
    }

    public static float[] toFloat(double[] input) {
        float[] res = new float[input.length];
        for (int i = 0; i < input.length; i++)
            res[i] = (byte) input[i];
        return res;
    }

    protected float[][] prev;

    public FFT(int sampleSize) {
        this.prev = new float[DECAY][sampleSize];
    }

    protected float avg(int i) {
        float res = 0;
        for (int j = 0; j < DECAY; j++)
            res += this.prev[j][i];
        return res / DECAY;
    }

    public abstract void changeSamplesLength(int newLength);

    public abstract void dispose();

    protected float[] getWithPrev() {
        float[] res = new float[this.prev[0].length];
        for (int i = 0; i < this.prev[0].length; i++)
            res[i] = this.avg(i);
        return res;
    }

    protected void push(float[] a) {
        for (int i = 0; i < (DECAY - 1); i++)
            this.prev[i] = this.prev[i + 1];
        this.prev[DECAY - 1] = a;
    }

    public abstract void spectrum(short[] samples, float[] spectrum);

    public void spectrumDecay(short[] samples, float[] spectrum) {
        float[] x = new float[samples.length];
        this.spectrum(samples, x);
        this.push(x);
        x = this.getWithPrev();
        for (int i = 0; i < Math.min(x.length, spectrum.length); i++)
            spectrum[i] = x[i];
    }
}
