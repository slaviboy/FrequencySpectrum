package com.slaviboy.analyser.window;

/**
 * Hanning window function implementation for Java
 * https://en.wikipedia.org/wiki/Hann_function
 */
public class Hanning extends Window{

    public Hanning(int length) {
        data = new double[length];

        int N = length;
        for (int i = 0; i < N; i++) {
            data[i] = (4.0 / N) * 0.5 * (1 - Math.cos(2 * Math.PI * i / N));
        }
    }
}
