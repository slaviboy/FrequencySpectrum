package com.slaviboy.frequencyspectrumexample;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.slaviboy.analyser.Analyser;
import com.slaviboy.analyser.AnalyserView;
import com.slaviboy.analyser.BarsDrawer;
import com.slaviboy.analyser.CurvesDrawer;
import com.slaviboy.microphone.RecordingThread;


import static com.slaviboy.frequencyspectrumexample.Base.hideSystemUI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecordingThread recordingThread;  // thread that records audio from microphone
    private AnalyserView analyserView;        // the view that will display the frequency graph

    private BarsDrawer barsDrawer;            // draws the frequencies in a form of bars(rectangles)
    private CurvesDrawer curvesDrawer;        // draws the frequencies in a form of curves(paths)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set tag name font
        Typeface font = Typeface.createFromAsset(getAssets(), "SCRIPTBL.TTF");
        TextView textView = findViewById(R.id.text_name);
        textView.setTypeface(font);

        CustomGraph.init(this);

        // create bars drawer using builder
        barsDrawer = new BarsDrawer.Builder()
                .withFillColor(Color.WHITE)
                .withMirrored(true)
                .withBarsWidth(2)
                .withSpacing(1)
                .withCornerRadius(4)
                .withRange(0, Integer.MAX_VALUE)
                .withIncreasePeaks(0)
                .withSensitivity(1)
                .withPosition(0, 0)
                .withPadding(0, 0, 0, 0)
                .withStrokeColor(Color.TRANSPARENT)
                /*.withGradientColors(new int[]{
                        Color.WHITE,
                        Color.RED
                })*/
                .build();

        // creates curves drawer using builder
        curvesDrawer = new CurvesDrawer.Builder()
                .withFillColor(Color.WHITE)
                .withMirrored(true)
                .withSpacing(1.5f)
                .withRange(0, Integer.MAX_VALUE)
                .withIncreasePeaks(0)
                .withSensitivity(1)
                .withPosition(0, 0)
                .withPadding(0, 0, 0, 0)
                .withStrokeColor(Color.TRANSPARENT)
                .withRadius(0.7f)
                .build();


        analyserView = findViewById(R.id.analyser_view);
        analyserView.setOnClickListener(this);
        analyserView.setOnDrawGraphListener(new AnalyserView.OnDrawGraphListener() {
            @Override
            public void onDrawGraph(Canvas canvas, Paint paint, Analyser analyser) {

                recordingThread.setReading(true);

                CustomGraph.draw(canvas, paint, analyser);

                // bars drawer use of all four types
                //barsDrawer.drawLinear(canvas, paint, analyser);
                //barsDrawer.drawLinearCentered(canvas, paint, analyser);
                //barsDrawer.drawRadial(canvas, paint, analyser);
                //barsDrawer.drawRadialCentered(canvas, paint, analyser);

                // bars drawer use of all four types
                //curvesDrawer.drawLinear(canvas, paint, analyser);
                //curvesDrawer.drawLinearCentered(canvas, paint, analyser);
                //curvesDrawer.drawRadial(canvas, paint, analyser);
                //curvesDrawer.drawRadialCentered(canvas, paint, analyser);

            }
        });

        recordingThread = new RecordingThread();
        recordingThread.setOnReceiveDataListener(new RecordingThread.OnReceiveDataListener() {
            @Override
            public void onReceiveData(float[] buffer) {

                // set raw data to the analyser view
                analyserView.setAudioBuffer(buffer);
            }
        });
        startAudioRecordingSafe();
    }

    private void startAudioRecordingSafe() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            recordingThread.start();
        } else {
            requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                android.Manifest.permission.RECORD_AUDIO}, 13);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 13 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordingThread.stop();
        }
    }

    @Override
    public void onClick(View v) {
        v.requestFocus();
        hideSystemUI((Activity) v.getContext());

        CustomGraph.changeGradient();
        //CustomGraph.changeDegreeLimit();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recordingThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        recordingThread.stop();
    }

}
