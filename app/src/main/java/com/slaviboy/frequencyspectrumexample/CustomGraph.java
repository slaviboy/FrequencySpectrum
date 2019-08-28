package com.slaviboy.frequencyspectrumexample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.slaviboy.analyser.Analyser;
import com.slaviboy.analyser.BarsDrawer;
import com.slaviboy.analyser.Drawer;
import com.slaviboy.stars.Cluster;
import com.slaviboy.stars.Star;

import java.util.Random;

/**
 * CustomGraph is a static class with static methods and properties,
 * that show a simple use of BarsDrawer with some extra objects
 * that are drawn on canvas like -circles, stars and bitmap.
 */
public class CustomGraph {

    private static Cluster cluster;        // star cluster object
    private static float halfViewWidth;    // half view width
    private static float halfViewHeight;   // half view height
    private static float minHalfSide;      // minimum half side from width and height
    private static float radius;           // circle radius is 70% of minimum half side
    private static Bitmap bitmap;          // scaled bitmap from the original
    private static PointF bitmapPosition;  // bitmap position in canvas
    private static Bitmap bitmapTemp;      // original bitmap from resource
    private static BarsDrawer barsDrawer;  // drawer object for drawing centered bars


    private static int lastViewWidth;
    private static int lastViewHeight;

    public static void init(Context context) {

        // init
        if (barsDrawer == null) {
            barsDrawer = new BarsDrawer.Builder()
                    .withFillColor(Color.WHITE)
                    .withBarsWidth(4)
                    .withDegree(3)
                    .withCornerRadius(4)
                    //.withRange(0, 250)
                    .withIncreasePeaks(-33)
                    .withSensitivity(1.4f)
                    .withMirrored(false)
                    .withStrokeColor(Color.TRANSPARENT)
                    .withGradientColors(new int[]{
                            Color.parseColor("#9457A5"),
                            Color.parseColor("#47B1C1")
                    })
                    .build();
        }

        if (bitmapTemp == null) {
            bitmapTemp = drawableToBitmap(context.getResources().getDrawable(R.drawable.icon_2));
        }
    }

    /**
     * Generate random colors in array for, as gradient
     * colors for the drawer
     */
    public static void changeGradient() {

        Random rnd = new Random();
        int[] randomColors = new int[4];
        for (int i = 0; i < randomColors.length - 1; i++) {
            randomColors[i] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }
        randomColors[randomColors.length - 1] = randomColors[0]; // make sure last color matches first color
        barsDrawer.setGradientColors(randomColors);
    }

    /**
     * Generate random degree limit for the drawer
     */
    public static void changeDegreeLimit() {
        barsDrawer.setDegreeLimit(new Random().nextInt(360));
    }

    /**
     * Draw or set -stars, bars, circles and bitmap, on given canvas
     *
     * @param canvas
     * @param paint
     * @param analyser
     */
    public static void draw(Canvas canvas, Paint paint, Analyser analyser) {

        // if view width or height is changed init again
        if (lastViewWidth != canvas.getWidth() ||
                lastViewHeight != canvas.getHeight()) {

            // init values
            cluster = new Cluster(300, Color.WHITE,
                    2, canvas.getWidth(), canvas.getHeight());

            lastViewWidth = canvas.getWidth();
            lastViewHeight = canvas.getHeight();

            halfViewWidth = (float) lastViewWidth / 2;
            halfViewHeight = (float) lastViewHeight / 2;
            minHalfSide = Math.min(halfViewWidth, halfViewHeight);
            radius = (minHalfSide) * 0.7f; // 70% from the minimum side

            // get resized bitmap
            bitmap = resizeBitmap(bitmapTemp);
            //bitmapTemp = null;
            bitmapPosition = new PointF(
                    halfViewWidth - (float) bitmap.getWidth() / 2,
                    halfViewHeight - (float) bitmap.getHeight() / 2
            );

        } else {

            updateClusterSpeed(analyser);

            // update and redraw stars
            cluster.update();
            cluster.draw(canvas, paint);

            // draw bars
            barsDrawer.drawRadial(canvas, paint, analyser);

            // stroke circle
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            canvas.drawCircle(halfViewWidth, halfViewHeight, radius - 10, paint);

            // fill circle
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#EEEEEE"));
            canvas.drawCircle(halfViewWidth, halfViewHeight, radius - 35, paint);
            paint.setShader(null);

            // bitmap icon
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, bitmapPosition.x, bitmapPosition.y, paint);
            }
        }

    }

    /**
     * Update stars speed, by bass sound level, using the
     * frequency data by he analyser
     *
     * @param analyser
     */
    private static void updateClusterSpeed(Analyser analyser) {

        // change stars speed depending on bass
        int bassSum = 0;
        float maxSpeed = 50f;
        int[] byteData = analyser.getByteFrequencyData();
        int bassLength = Math.min(150, byteData.length);
        for (int i = 0; i < bassLength; i++) {
            bassSum += byteData[i];
        }
        cluster.setSpeed((maxSpeed * bassSum) / (bassLength * 255));
    }

    /**
     * Get bitmap from drawable
     *
     * @param drawable
     * @return
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        // make sure drawable is bitmap drawable
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            // single color bitmap will be created of 1x1 pixel
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        // draw drawable on new canvas with bitmap set
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Resize current bitmap, to keep aspect ration, first the minimum
     * bitmaps side is set to x1.7 times the radius value
     *
     * @param bitmap
     * @return
     */
    private static Bitmap resizeBitmap(Bitmap bitmap) {

        int bitmapWidth;
        int bitmapHeight;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            bitmapWidth = (int) (radius * 1.7);
            bitmapHeight = (int) (bitmapWidth * ((double) bitmap.getHeight() / bitmap.getWidth()));
        } else {
            bitmapHeight = (int) (radius * 1.7);
            bitmapWidth = (int) (bitmapHeight * ((double) bitmap.getWidth() / bitmap.getHeight()));
        }
        return Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, true);
    }
}
