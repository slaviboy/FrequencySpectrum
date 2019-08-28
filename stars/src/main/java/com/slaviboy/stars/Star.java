package com.slaviboy.stars;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Free Star Class (Java)
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
 * Class that hold value for a circle object like - color, position using
 * x, y, z and scale. Contains two methods one for updating star position
 * on canvas and the other is for drawing circle on canvas.
 */
public class Star {

    private int color;      // color
    private double x;       // x
    private double y;       // y
    private double z;       // for depth perception
    private double scale;   // star size

    public Star(int viewWidth, int viewHeight) {
        this(Color.TRANSPARENT, 0.7, viewWidth, viewHeight);
    }

    public Star(int color, double scale, int viewWidth, int viewHeight) {
        this.scale = scale;
        this.color = color;
        this.x = Math.random() * viewWidth;
        this.y = Math.random() * viewHeight;
        this.z = Math.random() * viewWidth;
    }

    /**
     * Update star position on canvas by changing the z-coordinate,
     * for depth perception
     *
     * @param viewWidth   - view width
     * @param viewHeight  - view height
     * @param speed       - star speed
     */
    public void update(int viewWidth, int viewHeight, float speed) {
        this.z -= speed;
        if (this.z < 1) {
            this.x = Math.random() * viewWidth;
            this.y = Math.random() * viewHeight;
            this.z = viewWidth;
        }
    }

    /**
     * Draw current star on given canvas
     *
     * @param canvas      - canvas
     * @param paint       - paint
     * @param focalLength - focal length
     * @param center      - view center point
     */
    public void draw(Canvas canvas, Paint paint, float focalLength, PointF center) {

        double x = (this.x - center.x) * (focalLength / this.z) + center.x;
        double y = (this.y - center.y) * (focalLength / this.z) + center.y;
        double s = this.scale * (focalLength / this.z);

        if (color != Color.TRANSPARENT) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
        }
        canvas.drawCircle((float) x, (float) y, (float) s, paint);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
