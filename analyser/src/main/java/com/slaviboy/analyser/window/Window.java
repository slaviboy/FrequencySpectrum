package com.slaviboy.analyser.window;

/**
 * Base analyses window, with two methods to get data
 * as array or get certain value by provided index
 */
public class Window {

    protected double[] data;

    public Window() {
    }

    /**
     * Get value from the array, on given index
     * @param index
     * @return
     */
    public double get(int index) {
        return data[index];
    }

    /**
     * Get the whole data as array
     * @return
     */
    public double[] getData() {
        return data;
    }
}
