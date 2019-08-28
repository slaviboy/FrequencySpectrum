package com.slaviboy.analyser;

/**
 * Class that creates array with pre-calculated frequencies corresponding
 * to each bin. Depends on sample rate and fft size.
 */
public class Frequency {

    private double[] frequency;

    public Frequency(int fftSize, int sampleRate) {

        frequency = new double[fftSize];

        // vector with frequencies for each bin number. Used
        // in the graphing code (not in the analysis itself).
        double c = sampleRate / fftSize;
        for (int i = 0; i < fftSize / 2; i++) {
            frequency[i] = i * c;
        }
    }


    public double get(int index) {
        return frequency[index];
    }

    public double[] getData() {
        return frequency;
    }
}
