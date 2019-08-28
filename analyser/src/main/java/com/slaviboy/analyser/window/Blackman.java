package com.slaviboy.analyser.window;

/**
 * Blackman window function implementation for Java
 * https://en.wikipedia.org/wiki/Window_function
 */

public class Blackman extends Window {

    public Blackman(int length) {
        data = new double[length];

        int N = length;
        for (int i = 0; i < N; i++) {
            double f = 6.283185307179586 * i / (N - 1);
            data[i] = 0.42 - 0.5 * Math.cos(f) + 0.08 * Math.cos(2 * f);
        }
    }
}
