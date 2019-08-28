package com.slaviboy.stars;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;


/**
 * Free Cluster Class(Java)
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

 * Cluster class, representing a star cluster, that hold multiple
 * star object values, and can update and draw them on given canvas.
 */
public class Cluster {

    private int viewWidth;       // view width
    private int viewHeight;      // view height
    private int color;           // stars color
    private float speed;         // stars speed
    private float focalLength;   // focal length
    private PointF center;       // view center
    private Star[] stars;        // array with stars

    public Cluster(int numStars, int color, int speed, int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.speed = speed;
        this.color = color;

        // init stars
        stars = new Star[numStars];
        for (int i = 0; i < stars.length; i++) {
            stars[i] = new Star(viewWidth, viewHeight);
        }

        focalLength = viewWidth;
        center = new PointF((float) viewWidth / 2, (float) viewHeight / 2);
    }


    public void update() {

        // update star positions
        for (int i = 0; i < stars.length; i++) {
            stars[i].update(viewWidth, viewHeight, speed);
        }
    }

    public void draw(Canvas canvas, Paint paint) {

        paint.setColor(color);

        // draw stars on canvas
        for (int i = 0; i < stars.length; i++) {
            stars[i].draw(canvas, paint, focalLength, center);
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(float focalLength) {
        this.focalLength = focalLength;
    }

    public PointF getCenter() {
        return center;
    }

    public void setCenter(PointF center) {
        this.center = center;
    }
}
