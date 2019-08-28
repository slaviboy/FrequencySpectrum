package com.slaviboy.analyser;

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

/*
 * Free BarsDrawer Class(Java)
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
 * Class that is part of analyser library, and draws bars(rectangles) representing different
 * frequencies, on given canvas. Library supports four types - linear, linear centered,
 * radial and radial centered.
 */
public class BarsDrawer extends Drawer {

    private float barsWidth;       // bars width in px
    private float cornerRadius;    // bars corner radius, for round corners
    private RectF[] bars;          // bars dimensions -top, left, right, bottom

    public BarsDrawer() {
        super();

        // default
        barsWidth = 2;
        cornerRadius = 0;
        bars = new RectF[0];
    }

    public BarsDrawer(float barsWidth, float cornerRadius, int fillColor, int strokeColor, float strokeWidth,
                      Range range, float sensitivity, float increasePeaks, boolean mirrored,
                      RectF padding, PointF position, float degreeLimit, float degree, float spacing, float radius) {

        super(fillColor, strokeColor, strokeWidth, range, sensitivity, increasePeaks,
                mirrored, padding, position, degreeLimit, degree, spacing, radius);

        this.barsWidth = barsWidth;
        this.cornerRadius = cornerRadius;
        this.bars = new RectF[0];
    }

    /**
     * Initialize the array holding the bars dimensions and the limit index
     * for the current allowed degree. Using the limit index, generates
     * gradient color stack from the array with allowed gradient colors.
     *
     * @param barsNum  - new total bars number
     * @param isRadial - if graph is radial or linear
     */
    private void initBars(int barsNum, boolean isRadial) {

        // only if new total number of bars is given
        if (barsNum != bars.length) {

            bars = new RectF[barsNum];
            for (int i = 0; i < barsNum; i++) {
                bars[i] = new RectF(0, 0, 0, 0);
            }

            if (isRadial) {

                // find the end index when degree limit is reached
                degreeLimitIndex = (degreeLimit >= 360) ? (int) (360.0 / degree) : (int) (degreeLimit / degree) - 1;
                if (degreeLimitIndex > bars.length) {
                    degreeLimitIndex = bars.length;
                }

                // generate gradient stack colors
                createRadialGradientColors();

            } else {
                createLinearGradientColors();
            }
        }
    }


    public void drawRadial(Canvas canvas, Paint paint, Analyser analyser) {
        drawRadial(canvas, paint, analyser, TYPE_RADIAL);
    }

    public void drawRadialCentered(Canvas canvas, Paint paint, Analyser analyser) {
        drawRadial(canvas, paint, analyser, TYPE_RADIAL_CENTERED);
    }

    /**
     * Draw radial bars, with two available types: centered and normal.
     * You can set gradient colors or solid color for all bars.
     *
     * @param canvas   - canvas object for the view
     * @param paint    - paint object for the view
     * @param analyser - analyser object that hold the frequencies arrays
     * @param type     - TYPE_RADIAL or TYPE_RADIAL_CENTERED
     */
    private void drawRadial(Canvas canvas, Paint paint, Analyser analyser, int type) {

        int[] data = analyser.getByteFrequencyData();
        range.check(0, data.length - 1); // fix range if out of array range

        // get half sides
        widthHalf = (float) canvas.getWidth() / 2;
        heightHalf = (float) canvas.getHeight() / 2;

        float minHalfSide = Math.min(widthHalf, heightHalf);
        float radiusPx = (minHalfSide) * radius; // 70% from the minimum side


        // init array if current and previous array sizes does not match
        int barsLength = range.max - range.min + 1;
        initBars(barsLength, true);

        // get bound rectangle
        float fac = ((minHalfSide - radiusPx) / 255.0f) * sensitivity;
        if (type == TYPE_RADIAL) {

            // radial
            for (int i = 0; i < degreeLimitIndex; i++) {
                float value = increasePeaks + (data[i + range.min] * fac);

                bars[i].left = 0;
                bars[i].top = -value - radiusPx;
                bars[i].right = barsWidth;
                bars[i].bottom = -radiusPx;
            }
        } else {

            // radial centered
            for (int i = 0; i < degreeLimitIndex; i++) {
                float value = increasePeaks + (data[i + range.min] * fac);

                bars[i].left = 0;
                bars[i].top = -value / 2 - radiusPx;
                bars[i].right = barsWidth;
                bars[i].bottom = value / 2 - radiusPx;
            }
        }

        // if mirrored horizontally
        if (mirrored) {
            for (int i = 0; i < degreeLimitIndex; i++) {
                bars[i].top = Math.min(bars[i].top, bars[degreeLimitIndex - i - 1].top);          // min for top
                bars[i].bottom = Math.max(bars[i].bottom, bars[degreeLimitIndex - i - 1].bottom); // max for bottom
            }
        }


        canvas.save();

        // clip padding before translation
        canvas.clipRect(
                padding.left,
                padding.top,
                canvas.getWidth() - padding.right,
                canvas.getHeight() - padding.bottom,
                Region.Op.REPLACE);

        // translate to view center + graph move position in canvas
        canvas.translate(
                widthHalf + position.x,
                heightHalf + position.y);


        // fill
        if (gradientColors != null && gradientColors.length > 0) {
            // using stack
            drawRadialBarsStack(canvas, paint, Paint.Style.FILL);

        } else {
            // using solid color
            drawRadialBarsSolid(canvas, paint, Paint.Style.FILL, fillColor);
        }


        if (strokeColor != Color.TRANSPARENT) {
            paint.setStrokeWidth(strokeWidth);

            // stroke support only solid color
            drawRadialBarsSolid(canvas, paint, Paint.Style.STROKE, strokeColor);
        }

        canvas.restore();
    }

    public void drawLinear(Canvas canvas, Paint paint, Analyser analyser) {
        drawLinear(canvas, paint, analyser, TYPE_LINEAR);
    }

    public void drawLinearCentered(Canvas canvas, Paint paint, Analyser analyser) {
        drawLinear(canvas, paint, analyser, TYPE_LINEAR_CENTERED);
    }

    /**
     * Draw linear bars, with two available types: centered and normal.
     * You can set gradient colors or solid color for all bars.
     *
     * @param canvas   - canvas object for the view
     * @param paint    - paint object for the view
     * @param analyser - analyser object that hold the frequencies arrays
     * @param type     - TYPE_LINEAR or TYPE_LINEAR_CENTERED
     */
    private void drawLinear(Canvas canvas, Paint paint, Analyser analyser, int type) {

        int[] data = analyser.getByteFrequencyData();
        range.check(0, data.length - 1); // fix range if out of array range

        int totalBars = range.max - range.min + 1;
        int totalAllowedBars = (int) ((canvas.getWidth() - padding.left - padding.right) / (barsWidth + spacing));
        totalAllowedBars += 1;

        // find how many to skip
        int skipNum = (int) ((float) totalBars / totalAllowedBars);
        if (skipNum < 1) {
            skipNum = 1;
        }

        // the total bars
        int totalFinalBars = Math.min(totalAllowedBars, totalBars);
        initBars(totalFinalBars, false);

        float translateX = position.x + padding.left;
        float translateY;

        if (type == TYPE_LINEAR) {
            translateY = position.y - padding.bottom;

            // linear
            for (int i = 0; i < totalFinalBars; i++) {
                int realIndex = range.min + i * skipNum;

                if (realIndex < data.length) {
                    float barHeight = increasePeaks + data[realIndex] * sensitivity;

                    // change bar dimension
                    bars[i].left = (barsWidth + spacing) * i;
                    bars[i].top = canvas.getHeight() - barHeight;
                    bars[i].right = bars[i].left + barsWidth;
                    bars[i].bottom = bars[i].top + barHeight;
                }
            }
        } else {
            translateY = position.y; // do not use padding for centered type

            // linear centered
            for (int i = 0; i < totalFinalBars; i++) {
                int realIndex = range.min + i * skipNum;

                if (realIndex < data.length) {
                    float barHeight = increasePeaks + data[realIndex] * sensitivity;

                    // change bar dimension
                    bars[i].left = (barsWidth + spacing) * i;
                    bars[i].top = (canvas.getHeight() - barHeight) / 2;
                    bars[i].right = bars[i].left + barsWidth;
                    bars[i].bottom = bars[i].top + barHeight;
                } else break;
            }
        }


        // if mirrored horizontally
        if (mirrored) {
            for (int i = 0; i < totalFinalBars; i++) {
                bars[i].top = Math.min(bars[i].top, bars[totalFinalBars - i - 1].top);           // min for top
                bars[i].bottom = Math.max(bars[i].bottom, bars[totalFinalBars - i - 1].bottom);  // max for bottom
            }
        }

        canvas.save();

        // clip padding before translation
        canvas.clipRect(
                padding.left,
                padding.top,
                canvas.getWidth() - padding.right,
                canvas.getHeight() - padding.bottom,
                Region.Op.REPLACE);

        // translate graph in canvas, take in account clipping
        canvas.translate(translateX, translateY);


        // fill
        if (gradientColors != null && gradientColors.length > 0) {
            // using stack
            drawLinearBarsStack(canvas, paint, Paint.Style.FILL);

        } else {
            // using solid color
            drawLinearBarsSolid(canvas, paint, Paint.Style.FILL, fillColor);
        }

        // stroke
        if (strokeColor != Color.TRANSPARENT) {
            paint.setStrokeWidth(strokeWidth);
            drawLinearBarsSolid(canvas, paint, Paint.Style.STROKE, strokeColor);
        }

        canvas.restore();
    }


    /**
     * Draw bars and rotate them with a given degree creating the radial affect,
     * and set solid color for fill or stroke.
     *
     * @param canvas
     * @param paint
     * @param style
     * @param color
     */
    private void drawRadialBarsSolid(Canvas canvas, Paint paint, Paint.Style style,
                                     int color) {

        if (color != Color.TRANSPARENT) {

            canvas.save();
            paint.setStyle(style);
            paint.setColor(color);
            for (int i = 0; i < degreeLimitIndex; i++) {
                canvas.drawRoundRect(
                        bars[i],
                        cornerRadius,
                        cornerRadius,
                        paint);
                canvas.rotate(degree);
            }
            canvas.restore();
        }
    }

    /**
     * Draw bars and rotate them, creating radial effect set a stack(array) with colors,
     * where each value correspond to a bar. That way a gradient effect is created.
     *
     * @param canvas
     * @param paint
     * @param style
     */
    private void drawRadialBarsStack(Canvas canvas, Paint paint, Paint.Style style) {

        canvas.save();
        paint.setStyle(style);
        for (int i = 0; i < degreeLimitIndex; i++) {

            paint.setColor(gradientStack[i]);
            canvas.drawRoundRect(
                    bars[i],
                    cornerRadius,
                    cornerRadius,
                    paint);
            canvas.rotate(degree);
        }
        canvas.restore();
    }


    /**
     * Draw bars and set the solid color for all of them.
     *
     * @param canvas
     * @param paint
     * @param style
     * @param color
     */
    private void drawLinearBarsSolid(Canvas canvas, Paint paint, Paint.Style style, int color) {

        if (color != Color.TRANSPARENT) {

            paint.setStyle(style);
            paint.setColor(color);
            for (int i = 0; i < bars.length; i++) {
                canvas.drawRoundRect(
                        bars[i],
                        cornerRadius,
                        cornerRadius,
                        paint);
            }
        }
    }

    /**
     * Draw bars and set a stack(array) with colors, where each value
     * correspond to a bar. That way a gradient effect is created.
     *
     * @param canvas
     * @param paint
     * @param style
     */
    private void drawLinearBarsStack(Canvas canvas, Paint paint, Paint.Style style) {

        paint.setStyle(style);
        for (int i = 0; i < bars.length; i++) {

            paint.setColor(gradientStack[i]);
            canvas.drawRoundRect(
                    bars[i],
                    cornerRadius,
                    cornerRadius,
                    paint);
        }
    }


    /**
     * Generate stack(array) with colors for each bars, using array with given
     * gradient colors from which a color for each bin is generated.
     */
    private void createRadialGradientColors() {

        if (bars == null || bars.length == 0 || gradientColors == null) {
            return;
        }

        gradientStack = new int[bars.length];

        int modNum = (int) ((double) degreeLimitIndex) / (gradientColors.length);
        if (modNum < 1) {
            modNum = 1;
        }

        for (int i = 0; i < bars.length; i++) {

            // get real index if (degree > 360)
            int index = (i > degreeLimitIndex) ? i % degreeLimitIndex : i;

            // get first and second colors, and current fraction value
            float fraction = ((float) index / modNum);
            int startIndex = (int) fraction;
            int endIndex = startIndex + 1;
            if (endIndex >= gradientColors.length) endIndex = gradientColors.length - 1;
            if (startIndex >= gradientColors.length) startIndex = gradientColors.length - 1;

            // generate a merge color between the two colors
            int color = (Integer) new ArgbEvaluator().evaluate(fraction - startIndex,
                    gradientColors[startIndex], gradientColors[endIndex]);

            gradientStack[i] = color;
        }
    }

    /**
     * Generate stack(array) with colors for each bars, using array with given
     * gradient colors from which a color for each bin is generated.
     */
    private void createLinearGradientColors() {
        if (bars == null || bars.length == 0 || gradientColors == null) {
            return;
        }

        gradientStack = new int[bars.length];

        int modNum = (int) ((double) gradientStack.length) / (gradientColors.length);
        if (modNum < 1) {
            modNum = 1;
        }

        for (int i = 0; i < bars.length; i++) {

            // get first and second colors, and current fraction value
            float fraction = ((float) i / modNum);
            int startIndex = (int) fraction;
            int endIndex = startIndex + 1;
            if (endIndex >= gradientColors.length) endIndex = gradientColors.length - 1;
            if (startIndex >= gradientColors.length) startIndex = gradientColors.length - 1;

            // generate a merge color between the two colors
            int color = (Integer) new ArgbEvaluator().evaluate(fraction - startIndex,
                    gradientColors[startIndex], gradientColors[endIndex]);

            gradientStack[i] = color;
        }
    }

    /**
     * Builder class for simple and easy BarsDrawer object creation, that way
     * only certain properties can be set, and use default for all others.
     */
    public static class Builder {

        private BarsDrawer drawer;

        public Builder() {
            drawer = new BarsDrawer();
        }

        public Builder withBarsWidth(float barsWidth) {
            drawer.setBarsWidth(barsWidth);
            return this;
        }

        public Builder withDegree(float degree) {
            drawer.setDegree(degree);
            return this;
        }

        public Builder withCornerRadius(float cornerRadius) {
            drawer.setCornerRadius(cornerRadius);
            return this;
        }

        public Builder withFillColor(int fillColor) {
            drawer.setFillColor(fillColor);
            return this;
        }

        public Builder withStrokeColor(int strokeColor) {
            drawer.setStrokeColor(strokeColor);
            return this;
        }

        public Builder withSensitivity(float sensitivity) {
            drawer.setSensitivity(sensitivity);
            return this;
        }

        public Builder withIncreasePeaks(float increasePeaks) {
            drawer.setIncreasePeaks(increasePeaks);
            return this;
        }

        public Builder withRange(int min, int max) {
            drawer.setRange(min, max);
            return this;
        }

        public Builder withMirrored(boolean mirrored) {
            drawer.setMirrored(mirrored);
            return this;
        }

        public Builder withGradientColors(int[] gradientColors) {
            drawer.setGradientColors(gradientColors);
            return this;
        }

        public Builder withStrokeWidth(float strokeWidth) {
            drawer.setStrokeWidth(strokeWidth);
            return this;
        }

        public Builder withSpacing(float spacing) {
            drawer.setSpacing(spacing);
            return this;
        }

        public Builder withPadding(float left, float top, float right, float bottom) {
            drawer.setPadding(new RectF(left, top, right, bottom));
            return this;
        }

        public Builder withPosition(float x, float y) {
            drawer.setPosition(new PointF(x, y));
            return this;
        }

        public Builder withDegreeLimit(float degreeLimit) {
            drawer.setDegreeLimit(degreeLimit);
            return this;
        }

        public Builder withRadius(float radius) {
            drawer.setRadius(radius);
            return this;
        }

        public BarsDrawer build() {
            return drawer;
        }
    }

    @Override
    public void setGradientColors(int[] gradientColors) {
        super.setGradientColors(gradientColors);

        // to force gradient color regenerate
        bars = new RectF[0];
    }


    @Override
    public void setDegreeLimit(float degreeLimit) {
        super.setDegreeLimit(degreeLimit);

        degreeLimitIndex = (int) (degreeLimit / degree) - 1;
        createRadialGradientColors();
    }

    public float getBarsWidth() {
        return barsWidth;
    }

    public void setBarsWidth(float barsWidth) {
        this.barsWidth = barsWidth;
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
    }

}
