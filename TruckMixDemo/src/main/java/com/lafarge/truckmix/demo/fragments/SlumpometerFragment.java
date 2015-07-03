package com.lafarge.truckmix.demo.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lafarge.truckmix.controls.SlumpometerGauge;
import com.lafarge.truckmix.demo.R;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SlumpometerFragment extends Fragment {

    private static final String TAG = "SlumpometerFragment";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Timer timer = new Timer();

    @InjectView(R.id.slumpometer) SlumpometerGauge slumpometer;

    //
    // Constructor
    //

    public SlumpometerFragment() {
        // Required empty public constructor
    }

    public static SlumpometerFragment newInstance() {
        return new SlumpometerFragment();
    }

    //
    // Fragment lifecycle
    //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slumpometer, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        slumpometer.setMaxSpeed(300);
        slumpometer.setLabelConverter(new SlumpometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        slumpometer.setMaxSpeed(300);
        slumpometer.setMajorTickStep(30);
        slumpometer.setMinorTicks(2);
        slumpometer.addColoredRange(30, 140, Color.GREEN);
        slumpometer.addColoredRange(140, 180, Color.YELLOW);
        slumpometer.addColoredRange(180, 400, Color.RED);
    }

    @Override
    public void onStart() {
        super.onStart();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        double nextSpeed = new Random().nextInt(300);
                        Log.d(TAG, "nextSpeed: " + nextSpeed);
                        slumpometer.setSpeed(nextSpeed, 300, 0);
                    }
                });
            }
        }, 5000, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }
}
