package com.slaviboy.analyser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/*
 * Free Frequency AnalyserView Class(Java)
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
 * Class that is part of analyser library, and extends the View class and can visualize the
 * frequency data to its canvas. Listener is used to calls a method when the canvas is about
 * to be redrawn, where a specific frequency drawing is made.
 */
public class AnalyserView extends View {

    private Paint paint;                              // global paint object for the class
    private Analyser analyser;                        // analyser that is passed as argument to onDrawGraph() method
    private OnDrawGraphListener onDrawGraphListener;  // listener that is attached and listen for OnDraw() method

    public AnalyserView(Context context) {
        super(context);
        init(context);
    }

    public AnalyserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnalyserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AnalyserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        analyser = new Analyser();

        // init paint
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setAudioBuffer(float[] audioBuffer) {
        analyser.setAudioBuffer(audioBuffer);
        postInvalidate(); // for forcing view redrawing from non-ui thread
    }

    public Analyser getAnalyser() {
        return analyser;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (onDrawGraphListener != null) {
            onDrawGraphListener.onDrawGraph(canvas, paint, analyser);
        }
    }

    public void setOnDrawGraphListener(OnDrawGraphListener onDrawGraphListener) {
        this.onDrawGraphListener = onDrawGraphListener;
    }

    public interface OnDrawGraphListener {
        void onDrawGraph(Canvas canvas, Paint paint, Analyser analyser);
    }
}
