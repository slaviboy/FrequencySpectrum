package com.slaviboy.analyser;

import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Arrays;

/*
 * Free CurvesDrawer Class(Java)
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
 * Class that is part of analyser library, and draws curves(path) representing different
 * frequencies, on given canvas. Library supports four types - linear, linear centered,
 * radial and radial centered.
 */

public class CurvesDrawer extends Drawer {

    private float factor;                   // factor control sharpness, 0 will be straight line value is between [0,1] for -cubic curve
    private float tension;                  // tension control smoothness value is between [0,1] for -cubic curve
    private PointF[] mirroredVertically;    // vertical mirrored bar points for - radial centered type

    private PointF[] points;                // points where the curve must pass trough
    private Path path;                      // path that is generated from points

    public CurvesDrawer() {
        super();

        // default
        factor = 0.3f;
        tension = 0.5f;
        points = new PointF[0];
        mirroredVertically = new PointF[0];
    }

    public CurvesDrawer(float factor, float tension, int fillColor, int strokeColor, float strokeWidth,
                        Range range, float sensitivity, float increasePeaks, boolean mirrored,
                        RectF padding, PointF position, float degreeLimit, float degree, float spacing, float radius) {

        super(fillColor, strokeColor, strokeWidth, range, sensitivity, increasePeaks,
                mirrored, padding, position, degreeLimit, degree, spacing, radius);

        this.factor = factor;
        this.tension = tension;
    }

    /**
     * Initialize the array holding the points coordinates and set limit
     * degree index for radial graphs.
     *
     * @param pointsNum - new arrays size
     * @param isRadial  - if graph is radial
     */
    private void initPoints(int pointsNum, boolean isRadial) {

        // only if new total number of bars is given
        if (pointsNum != points.length) {

            // init array with points
            points = new PointF[pointsNum];
            mirroredVertically = new PointF[pointsNum];
            for (int i = 0; i < points.length; i++) {
                points[i] = new PointF(0, 0);
                mirroredVertically[i] = new PointF(0, 0);
            }

            if (isRadial) {

                // find the end index when degree limit is reached
                degreeLimitIndex = (degreeLimit >= 360) ? (int) (360.0 / degree) : (int) (degreeLimit / degree) - 1;
                if (degreeLimitIndex > points.length) {
                    degreeLimitIndex = points.length;
                }
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
     * Draw radial curves, with two available types one centered and one
     * normal. You can set only solid color to fill or stroke all bars.
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

        // if current and previous array sizes does not match, init arrays again
        int barsLength = range.max - range.min + 1;
        initPoints(barsLength, true);

        float peaksFactor = ((minHalfSide - radiusPx) / 255.0f) * sensitivity;
        float remainder = heightHalf - radiusPx;
        if (type == TYPE_RADIAL) {

            // radial
            for (int i = 0; i < degreeLimitIndex; i++) {
                float value = increasePeaks + (data[i + range.min] * peaksFactor);

                points[i].x = widthHalf;
                points[i].y = remainder - value;
            }
        } else {

            // radial centered
            for (int i = 0; i < degreeLimitIndex; i++) {
                float value = (increasePeaks + (data[i + range.min] * peaksFactor)) / 2;

                points[i].x = widthHalf;
                points[i].y = remainder - value;

                mirroredVertically[degreeLimitIndex - 1 - i].x = widthHalf;
                mirroredVertically[degreeLimitIndex - 1 - i].y = remainder + value;
            }
        }


        // if mirrored horizontally is enabled
        if (mirrored) {
            // get the MIN, from both ends
            for (int i = 0; i < degreeLimitIndex; i++) {
                points[i].y = Math.min(points[i].y, points[degreeLimitIndex - i - 1].y);
            }
        }

        // rotate points with given global angle
        rotate(points, false);

        // reset path
        path = new Path();
        path.moveTo(points[0].x, points[0].y);

        lines(points, degreeLimitIndex);
        path.lineTo(points[0].x, points[0].y); // to close path


        // for radial centered only
        if (type == TYPE_RADIAL_CENTERED) {

            // if mirrored horizontally
            if (mirrored) {
                // get the MAX, from both ends
                for (int i = 0; i < degreeLimitIndex; i++) {
                    mirroredVertically[i].y = Math.max(mirroredVertically[i].y, mirroredVertically[degreeLimitIndex - i - 1].y);
                }
            }

            // rotate points with reverse angle
            rotate(mirroredVertically, true);

            // line to reveres array direction to match previous path
            lines(mirroredVertically, degreeLimitIndex);
            path.lineTo(mirroredVertically[0].x, mirroredVertically[0].y);
        }

        canvas.save();

        // clip padding before translation
        canvas.clipRect(
                padding.left,
                padding.top,
                canvas.getWidth() - padding.right,
                canvas.getHeight() - padding.bottom,
                Region.Op.REPLACE);

        // translate to graph move position in canvas
        canvas.translate(position.x, position.y);


        // fill
        drawCurveSolid(canvas, paint, Paint.Style.FILL, fillColor);

        // stroke
        if (strokeColor != Color.TRANSPARENT) {
            paint.setStrokeWidth(strokeWidth);
            drawCurveSolid(canvas, paint, Paint.Style.STROKE, strokeColor);
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
     * Draw linear curves, with two available types: centered and normal.
     * You can set only solid color to fill or stroke all bars.
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
        int totalAllowedBars = (int) ((canvas.getWidth() - padding.left - padding.right) / (spacing));
        totalAllowedBars += 1;

        // find how many to frequencies to skip
        int skipNum = (int) ((float) totalBars / totalAllowedBars);
        if (skipNum < 1) {
            skipNum = 1;
        }

        // init array with points again, if array size is changed
        int totalFinalBars = Math.min(totalAllowedBars, totalBars);
        initPoints(totalFinalBars, false);

        // total translate coordinates
        float translateX = position.x + padding.left;
        float translateY;

        float y; // static y coordinate for first and last points

        if (type == TYPE_LINEAR) {

            translateY = position.y - padding.bottom;
            y = canvas.getHeight();

            // linear
            for (int i = 0; i < totalFinalBars; i++) {
                int realIndex = range.min + i * skipNum;

                if (realIndex < data.length) {
                    float barHeight = increasePeaks + data[realIndex] * sensitivity;
                    if (barHeight < 0) {
                        barHeight = 0; // limit only to positive value
                    }

                    points[i].x = spacing * i;
                    points[i].y = canvas.getHeight() - barHeight;
                } else break;
            }
        } else {

            translateY = position.y; // do not use padding for centered type
            y = (float) canvas.getHeight() / 2;

            // linear centered
            for (int i = 0; i < totalFinalBars; i++) {
                int realIndex = range.min + i * skipNum;

                if (realIndex < data.length) {
                    float barHeight = increasePeaks + data[realIndex] * sensitivity;
                    if (barHeight < 0) {
                        barHeight = 0; // limit only to positive value
                    }

                    points[i].x = spacing * i;
                    points[i].y = (canvas.getHeight() - barHeight) / 2;

                    mirroredVertically[totalFinalBars - 1 - i].x = spacing * i;
                    mirroredVertically[totalFinalBars - 1 - i].y = (canvas.getHeight() + barHeight) / 2;
                } else break;
            }
        }

        // mirror path horizontally
        if (mirrored) {
            for (int i = 0; i < points.length; i++) {
                points[i].y = Math.min(points[i].y, points[points.length - i - 1].y);
            }
        }

        // dist and last point with static -y (minimum graph value)
        points[0].y = y;
        points[points.length - 1].y = y;

        // reset path
        path = new Path();
        path.moveTo(points[0].x, points[0].y); // move to first point with static -y
        cubicCurve(points, points.length);
        path.lineTo(points[points.length - 1].x, points[points.length - 1].y); // line to last point with static -y


        // mirror path vertically for centered linear
        if (type == TYPE_LINEAR_CENTERED) {

            // if mirrored horizontally
            if (mirrored) {
                // get the MAX, from both ends
                for (int i = 0; i < totalFinalBars; i++) {
                    mirroredVertically[i].y = Math.max(mirroredVertically[i].y, mirroredVertically[totalFinalBars - i - 1].y);
                }
            }

            cubicCurve(mirroredVertically, mirroredVertically.length);
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
        drawCurveSolid(canvas, paint, Paint.Style.FILL, fillColor);

        // stroke
        if (strokeColor != Color.TRANSPARENT) {
            paint.setStrokeWidth(strokeWidth);
            drawCurveSolid(canvas, paint, Paint.Style.STROKE, strokeColor);
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
    private void drawCurveSolid(Canvas canvas, Paint paint, Paint.Style style, int color) {

        if (color != Color.TRANSPARENT) {
            paint.setStyle(style);
            paint.setColor(color);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * Set up the path using array points
     */
    private void lines(PointF[] pointArray, int numPoints) {

        for (int i = 0; i < numPoints; i++) {
            path.lineTo(pointArray[i].x, pointArray[i].y);
        }
    }

    /**
     * Set up the path points by setting cubic curve, using the array
     * with points, it has two properties factor and tension. Factor
     * controls curve sharpness, and tension controls the smoothness.
     */
    private void cubicCurve(PointF[] pointArray, int numPoints) {

        float m;
        float dx1 = 0;
        float dy1 = 0;
        float dx2;
        float dy2;

        PointF previous = pointArray[0];
        for (int i = 1; i < numPoints - 1; i++) {
            PointF current = pointArray[i];
            PointF next = pointArray[i + 1];

            m = (next.y - previous.y) / (next.x - previous.x);
            dx2 = (next.x - current.x) * -factor;
            dy2 = dx2 * m * tension;

            path.cubicTo(
                    previous.x - dx1,
                    previous.y - dy1,
                    current.x + dx2,
                    current.y + dy2,
                    current.x,
                    current.y);

            dx1 = dx2;
            dy1 = dy2;
            previous = current;
        }

    }


    /**
     * Set up the path points by setting quadratic curve, using the
     * array with points.
     */
    public void quadraticCurve(int numPoints) {

        for (int i = 1; i < numPoints; i++) {

            float x_mid = (points[i].x + points[i + 1].x) / 2;
            float y_mid = (points[i].y + points[i + 1].y) / 2;
            float cp_x1 = (x_mid + points[i].x) / 2;
            float cp_x2 = (x_mid + points[i + 1].x) / 2;

            path.quadTo(cp_x1,
                    points[i].y,
                    x_mid,
                    y_mid);
            path.quadTo(cp_x2,
                    points[i + 1].y,
                    points[i + 1].x,
                    points[i + 1].y);
        }
    }

    /**
     * Rotate point to a given angle(+) positive angle is rotation
     * clockwise and negative angle is rotation anticlockwise
     */
    private void rotate(PointF[] pointArray, boolean reverse) {

        float currentDegree = reverse ? -degree : degree;

        float centerX = widthHalf;  // center of rotation x coordinate
        float centerY = heightHalf; // center of rotation y coordinate

        if (currentDegree == 0) {
            return;
        }

        double radFactor = (Math.PI / -180);
        double radians;

        int sumDegree = 0;
        for (int i = 0; i < pointArray.length; i++) {

            radians = radFactor * sumDegree;
            sumDegree += currentDegree;

            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            float xDelta = (pointArray[i].x - centerX);
            float yDelta = (pointArray[i].y - centerY);

            pointArray[i].x = (float) (cos * xDelta + sin * yDelta + centerX);
            pointArray[i].y = (float) (cos * yDelta - sin * xDelta + centerY);
        }
    }


    /**
     * Builder class for simple and easy BarsDrawer object creation, that way
     * only certain properties can be set, and use default for all others.
     */
    public static class Builder {

        private CurvesDrawer drawer;

        public Builder() {
            drawer = new CurvesDrawer();
        }

        public Builder withDegree(float degree) {
            drawer.setDegree(degree);
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

        public Builder withTension(float tension) {
            drawer.setTension(tension);
            return this;
        }

        public Builder withFactor(float factor) {
            drawer.setFactor(factor);
            return this;
        }

        public Builder withRadius(float radius) {
            drawer.setRadius(radius);
            return this;
        }

        public CurvesDrawer build() {
            return drawer;
        }
    }

    @Override
    public void setGradientColors(int[] gradientColors) {
        super.setGradientColors(gradientColors);

        // to force gradient color regenerate
        points = new PointF[0];
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

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public float getTension() {
        return tension;
    }

    public void setTension(float tension) {
        this.tension = tension;
    }
}
