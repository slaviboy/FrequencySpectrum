package com.slaviboy.analyser;

/**
 * Simple range class that holds min and max values, for
 * close range [min, max]
 */
public class Range {

    public int min;
    public int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public Range(Range range) {
        this.min = range.min;
        this.max = range.max;
    }

    /**
     * Check if current range values are in another range, if
     * not then fit the current range.
     *
     * @param min
     * @param max
     */
    public void check(int min, int max) {
        if (this.min < min || this.min > max) {
            this.min = min;
        }
        if (this.max < min || this.max > max) {
            this.max = max;
        }
    }

    @Override
    public String toString() {
        return "min:" + min + ", max:" + max;
    }
}
