package com.slaviboy.analyser;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

/*
 * Free Drawer Class(Java)
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
 * Class that is part of analyser library, and is base class for curves and bars drawer classes.
 * Contains the base properties and there corresponding getter/setter methods.
 */
public class Drawer {

    public static final int TYPE_RADIAL = 0;
    public static final int TYPE_LINEAR = 1;
    public static final int TYPE_RADIAL_CENTERED = 2;
    public static final int TYPE_LINEAR_CENTERED = 3;


    protected float widthHalf;       // half canvas width
    protected float heightHalf;      // half canvas height
    protected int fillColor;         // fill solid color
    protected int strokeColor;       // stroke solid color
    protected float strokeWidth;     // stroke width
    protected float sensitivity;     // sensitivity for frequency peaks
    protected float increasePeaks;   // increase or decrease all frequency peaks
    protected boolean mirrored;      // if graph should be mirrored horizontally

    protected Range range;           // allowed bars/points range (limit the frequency range)
    protected PointF position;       // translate canvas and move the drawing
    protected RectF padding;         // graph padding in canvas

    protected float degree;          // angle in degrees between bars/points for radial graph
    protected float spacing;         // space between bars/points for linear graph

    protected int[] gradientColors;  // gradient color
    protected int[] gradientStack;   // array with colors for each bar, that is generated from gradient colors

    protected float degreeLimit;     // limit the total degree for radial graph
    protected int degreeLimitIndex;  // the bar/point index, when degree limit is reached

    protected float radius;          // radial graph radius as percentage of minimum half side 1.0 = 100%

    public Drawer() {

        // default
        this(Color.BLACK, Color.TRANSPARENT, 0,  new Range(0, Integer.MAX_VALUE),
                1.0f, 0.0f, false, new RectF(0,0,0,0),
                new PointF(0,0), Float.MAX_VALUE, 2, 1, 0.7f);
    }

    public Drawer(int fillColor, int strokeColor, float strokeWidth, Range range,
                  float sensitivity, float increasePeaks, boolean mirrored, RectF padding,
                  PointF position, float degreeLimit, float degree, float spacing, float radius) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        this.range = range;
        this.sensitivity = sensitivity;
        this.increasePeaks = increasePeaks;
        this.mirrored = mirrored;
        this.padding = padding;
        this.position = position;
        this.degreeLimit = degreeLimit;
        this.degree = degree;
        this.spacing = spacing;
        this.radius = radius;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public float getIncreasePeaks() {
        return increasePeaks;
    }

    public void setIncreasePeaks(float increasePeaks) {
        this.increasePeaks = increasePeaks;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public void setRange(int min, int max) {
        this.range = new Range(min, max);
    }

    public boolean isMirrored() {
        return mirrored;
    }

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int[] getGradientColors() {
        return gradientColors;
    }

    public void setGradientColors(int[] gradientColors) {
        this.gradientColors = gradientColors;
    }

    public RectF getPadding() {
        return padding;
    }

    public void setPadding(RectF padding) {
        this.padding = padding;
    }

    public float getDegreeLimit() {
        return degreeLimit;
    }

    public void setDegreeLimit(float degreeLimit) {
        this.degreeLimit = degreeLimit;
    }

    public float getDegree() {
        return degree;
    }

    public void setDegree(float degree) {
        this.degree = degree;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public PointF getPosition() {
        return position;
    }

    public void setPosition(PointF position) {
        this.position = position;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}

