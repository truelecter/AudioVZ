package truelecter.iig.util.audio;

public abstract class FFT {
    public static void decode(byte[] input, double[] output) {
        for (int i = 0; i < output.length; i++) {
            output[i] = (short) (((0xFF & input[(2 * i) + 1]) << 8) | (0xFF & input[2 * i]));
            output[i] /= Short.MAX_VALUE;
        }
    }

    public static byte[] toByte(short[] input) {
        byte[] res = new byte[input.length];
        for (int i = 0; i < input.length; i++)
            res[i] = (byte) input[i];
        return res;
    }

    public static float[] toFloat(double[] input) {
        float[] res = new float[input.length];
        for (int i = 0; i < input.length; i++)
            res[i] = (byte) input[i];
        return res;
    }

    public abstract void changeSamplesLength(int newLength);

    public abstract void dispose();

    public abstract void spectrum(short[] samples, float[] spectrum);
}
