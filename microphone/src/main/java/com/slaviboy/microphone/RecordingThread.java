package com.slaviboy.microphone;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import static android.media.AudioRecord.READ_NON_BLOCKING;

/*
 * Free RecordingThread Class(Java)
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
 * Thread that records audio from microphone using AudioRecord object, and send the
 * audio data through a listener. For android version bigger than (M)Marshmallow
 * data is read as float array [-1,1], and for lower version data is read in short
 * and then converted to a float array.
 */
public class RecordingThread implements Runnable {

    private Thread thread;                                // thread for getting audio data
    private boolean isRunning;                            // if thread is active and is running
    private boolean reading;                              // if reading audio data is allowed
    private OnReceiveDataListener onReceiveDataListener;  // listener for receiving data from microphone
    private int sampleRate;                               // maximum allowed sample rate for current microphone

    public RecordingThread() {
    }

    public RecordingThread(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }

    /**
     * Thread runnable that is implemented by the class, and uses while loop
     * to keep the thread alive. If reading audio data is allowed then, the
     * AudioRecord read audio from microphone non-blocking and send the data
     * to the listener. If thread is on hold, then sleep the thread for 1ms
     * and check again if reading is allowed.
     */
    @Override
    public void run() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // get encoding for reading shorts and floats
        int encoding;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            encoding = AudioFormat.ENCODING_PCM_16BIT;
        } else {
            encoding = AudioFormat.ENCODING_PCM_FLOAT;
        }

        // get maximum available sample rate
        sampleRate = 0;
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100}) {
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, encoding);
            if (bufferSize > 0) {
                sampleRate = rate;
            }
        }

        // buffer size in bytes
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                encoding);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return;
        }

        // object to record audio data from microphone
        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                encoding,
                bufferSize);
        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }
        record.startRecording();

        // create a while loop that holds the thread running
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            // read data as shorts for versions lower than marshmallow

            short[] audioBuffer = new short[bufferSize / 2];
            while (isRunning) {

                // if reading is postponed set reading on hold
                if (!reading) {
                    // sleep thread 1ms and continue
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                reading = false;

                record.read(audioBuffer, 0, audioBuffer.length);

                // convert short[-32768,32767] -> float between [-1,1]
                float[] data = new float[audioBuffer.length];
                for (int i = 0; i < data.length; i++) {
                    float value = 0.0f;
                    if (audioBuffer[i] > 0) {
                        value = (float) audioBuffer[i] / 32767.0f;
                    } else if (audioBuffer[i] < 0) {
                        value = (float) audioBuffer[i] / 32768.0f;
                    }
                    data[i] = value;
                }

                if (onReceiveDataListener != null) {
                    onReceiveDataListener.onReceiveData(data);
                }
            }
        } else {

            // read data as floats for android version > Marshmallow
            float[] audioBuffer = new float[bufferSize / 2];

            // if reading is postponed set reading on hold
            while (isRunning) {
                if (!reading) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                reading = false;

                record.read(audioBuffer, 0, audioBuffer.length, READ_NON_BLOCKING);

                if (onReceiveDataListener != null) {
                    onReceiveDataListener.onReceiveData(audioBuffer);
                }
            }
        }

        record.stop();
        record.release();
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }

    public boolean isRecording() {
        return isRunning;
    }

    public void start() {
        if (thread == null) {
            isRunning = true;
            thread = new Thread(this); // set this runnable, to new thread
            thread.start();
            reading = true;
        }
    }

    public void stop() {
        if (thread != null) {
            isRunning = false;
            thread.interrupt();
            thread = null;
        }
    }


    public interface OnReceiveDataListener {
        void onReceiveData(float[] audioBuffer);
    }
}
