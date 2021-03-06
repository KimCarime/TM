package com.lafarge.truckmix.demo.fragments;

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

    private double nextSlump;
    private boolean ascending;

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

        slumpometer.setConcreteRange(100, 150);
        slumpometer.setTolerance(10);
        slumpometer.setConcreteCode("S3");
    }

    @Override
    public void onStart() {
        super.onStart();

        nextSlump = 0;
        ascending = true;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "nextSlump: " + nextSlump);

                        slumpometer.setSlump(nextSlump, 300, 0);

                        if (ascending) {
                            ++nextSlump;
                        } else {
                            --nextSlump;
                        }
                        if (nextSlump == 0) {
                            ascending = true;
                        } else if (nextSlump == SlumpometerGauge.MAX_SLUMP) {
                            ascending = false;
                        }
                    }
                });
            }
        }, 5000, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }
}
