package com.example.salvares.audiorecorderapp;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

import android.media.MediaPlayer;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioButtonPinName = "J7_58";
    private Button mButton;
    private MediaPlayer mPlayer = null;
    private MediaRecorder mRecorder = null;
    private static String mFileName = null;

    public MainActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        Log.i(TAG, "Filename " + mFileName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyButton();
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();

        final File file = new File(mFileName);
        try {
            if (file.exists()) {
                FileInputStream is = new FileInputStream(file);
                FileDescriptor fd = is.getFD();
                mPlayer.setDataSource(fd);
                is.close();
            } else {
                throw new IOException("setDataSource failed.");
            }
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.reset();
        mRecorder.release();
        mRecorder = null;
    }

    private void setupButton() {
        try {
            mButton = new Button(gpioButtonPinName,
                    // high signal indicates the button is pressed
                    // use with a pull-down resistor
                    Button.LogicState.PRESSED_WHEN_HIGH
            );
            Log.i(TAG, "Opening button");
            mButton.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    Log.i(TAG, "Button Event pressed : " + pressed);
                    onPlay(pressed);
                    //onRecord(pressed);
                }
            });
        } catch (IOException e) {
            // couldn't configure the button...
            Log.e(TAG, "Error Opening button");
        }
    }

   private void destroyButton() {
        if (mButton != null) {
            Log.i(TAG, "Closing button");
            try {
                mButton.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing button", e);
            } finally {
                mButton = null;
            }
        }
    }
}