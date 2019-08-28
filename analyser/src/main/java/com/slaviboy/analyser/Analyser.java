package com.slaviboy.analyser;

import com.slaviboy.analyser.window.Blackman;

import java.util.Arrays;

/*
 * Free Frequency Analyser Class(Java)
 *
 * Copyright (c) 2019 Stanislav Georgiev. (MIT License)
 * https://github.com/slaviboy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other
 * liability, whether in an action of contract, tort or otherwise, arising from,
 * out of or in connection with the Software or the use or other dealings in the
 * Software.
 *
 * Class that is part of analyser library, and transform an audio signal as float array, from its original
 * time domain to a representation in the frequency domain using the FFT(FastFourierTransform) algorithm.
 *
 * Based on Web Audio API Documentation https://www.w3.org/TR/webaudio/#fft-windowing-and-smoothing-over-time
 */
public class Analyser {

    // public default static values
    public static final int FFT_SIZE = 1024;
    public static final int MAX_DECIBELS = -30;
    public static final int MIN_DECIBELS = -100;
    public static final int SAMPLE_RATE = 44100;
    public static final double SMOOTHING_TIME_CONSTANT = 0.8;

    private int fftSize;                        // fft size 128, 256, 512,...
    private int sampleRate;                     // actual microphone sample rate (Hz)
    private Range decibels;                     // decibels range
    private double smoothingTimeConstant;       // value between [0-1] for smoothing data, from previous transition

    private Frequency frequency;                // frequencies array corresponding to each bar
    private Blackman window;                    // analysis window (Hanning, Blackman, ...)

    private double[] smoothingData;             // last smoothed data
    private double[] realArray;                 // real component for fft
    private double[] imaginaryArray;            // imaginary component for fft

    private int[] byteFrequencyData;            // frequency data in bytes [0,255]
    private float[] floatTimeDomainData;        // data from microphone or audio files [-1,1]
    private double[] doubleFrequencyData;       // frequency data(magnitudes)

    public Analyser() {

        // default
        this(FFT_SIZE, SAMPLE_RATE, new Range(MIN_DECIBELS, MAX_DECIBELS),
                SMOOTHING_TIME_CONSTANT);
    }

    public Analyser(int fftSize, int sampleRate, Range decibels, double smoothingTimeConstant) {

        this.sampleRate = sampleRate;
        this.decibels = decibels;
        this.smoothingTimeConstant = smoothingTimeConstant;

        setFftSize(fftSize);
    }

    /**
     * Set buffer data from microphone or audio file, to convert the signal
     * from its original time domain to a representation in the frequency
     * domain using FFT(FastFourierTransform)
     *
     * @param audioBuffer
     */
    public void setAudioBuffer(float[] audioBuffer) {

        this.floatTimeDomainData = audioBuffer;

        // set the real component while applying analyses window
        for (int i = 0; i < fftSize; i++) {
            realArray[i] = audioBuffer[i] * window.get(i);
            imaginaryArray[i] = 0.0;
        }

        // apply fft
        FastFourierTransform.transform(realArray, imaginaryArray);

        // get frequency spectrum from fft real and imaginary parts
        for (int i = 0; i < fftSize / 2; i++) {
            double re = realArray[i];
            double im = imaginaryArray[i];
            doubleFrequencyData[i] = Math.sqrt(re * re + im * im) / fftSize;
        }

        // smooth data
        for (int i = 0; i < fftSize / 2; i++) {
            smoothingData[i] =
                    smoothingTimeConstant * smoothingData[i] +
                            (1.0 - smoothingTimeConstant) * doubleFrequencyData[i];
        }

        // convert to dB to get magnitude
        for (int i = 0; i < fftSize / 2; i++) {
            doubleFrequencyData[i] = 20.0 * Math.log10(smoothingData[i]);
        }

        // clip frequency data between [0-255]
        byteFrequencyData = new int[doubleFrequencyData.length];
        for (int i = 0; i < doubleFrequencyData.length; i++) {

            int byteValue =
                    (int) (255.0 / (decibels.max - decibels.min) *
                            (doubleFrequencyData[i] - decibels.min));
            if (byteFrequencyData[i] < 0) {
                byteValue = 0;
            }
            if (byteFrequencyData[i] > 255) {
                byteValue = 255;
            }
            byteFrequencyData[i] = byteValue;
        }
    }

    /**
     * Get frequency data in clipped range between [0,255]
     *
     * @return
     */
    public int[] getByteFrequencyData() {
        return byteFrequencyData;
    }

    /**
     * Get double frequency data representing magnitude
     *
     * @return
     */
    public double[] getDoubleFrequencyData() {
        return doubleFrequencyData;
    }

    /**
     * Get time domain data in range [0,255]
     *
     * @return
     */
    public int[] getByteTimeDomainData() {
        int[] byteData = new int[doubleFrequencyData.length];
        for (int i = 0; i < doubleFrequencyData.length; i++) {
            byteData[i] = (int) (128 * (1 + floatTimeDomainData[i]));
        }
        return byteData;
    }

    /**
     * Return the original signal from microphone or audio file
     *
     * @return
     */
    public float[] getFloatTimeDomainData() {
        return floatTimeDomainData;
    }

    /**
     * Round double value, to a given decimal place
     * 4.88748372 -> 2 decimal places = 4.88
     *
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) {
            return 0;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public int getFftSize() {
        return fftSize;
    }

    public void setFftSize(int fftSize) {
        this.fftSize = fftSize;

        // init arrays
        frequency = new Frequency(fftSize, sampleRate);
        window = new Blackman(fftSize);
        doubleFrequencyData = new double[fftSize / 2];
        byteFrequencyData = new int[fftSize / 2];

        // default smoothing data is array with 0.0
        smoothingData = new double[fftSize / 2];
        Arrays.fill(smoothingData, 0.0);

        realArray = new double[fftSize];
        imaginaryArray = new double[fftSize];
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        frequency = new Frequency(fftSize, sampleRate);
    }

    public double getSmoothingTimeConstant() {
        return smoothingTimeConstant;
    }

    public void setSmoothingTimeConstant(double smoothingTimeConstant) {
        this.smoothingTimeConstant = smoothingTimeConstant;
    }

    public int getFrequencyBinCount() {
        return fftSize / 2;
    }

    public Range getDecibels() {
        return decibels;
    }

    public void setDecibels(Range decibels) {
        this.decibels = decibels;
    }

    public Frequency getFrequency() {
        return frequency;
    }
}
