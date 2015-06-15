package com.lafarge.truckmix.demo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.lafarge.truckmix.demo.R;

public class OverviewFragment extends Fragment {

    private static final String TAG = "OverviewFragment";

    @InjectView(R.id.textView) TextView mSlump;
    @InjectView(R.id.textView1) TextView mAlarm;
    @InjectView(R.id.textView2) TextView mMixerMode;
    @InjectView(R.id.textView12) TextView mInputPressure;
    @InjectView(R.id.textView13) TextView mOutputPressure;
    @InjectView(R.id.textView3) TextView mRotationSpeed;
    @InjectView(R.id.textView4) TextView mInputPressureSensorState;
    @InjectView(R.id.textView5) TextView mOutputPressureSensorState;
    @InjectView(R.id.textView6) TextView mMinSensorExceed;
    @InjectView(R.id.textView7) TextView mMaxSensorExceed;
    @InjectView(R.id.textView8) TextView mAcquisitionSubstep;
    @InjectView(R.id.textView9) TextView mRegulationSubstep;
    @InjectView(R.id.textView10) TextView mPumpSubstep;
    @InjectView(R.id.textView11) TextView mSlumpSubstep;

    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    public void updateSlump(int slump) {
        mSlump.startAnimation(getBlinkAnimation());
        mSlump.setText(String.format("%d mm", slump));
    }

    public void updateAlarm(String alarm) {
        mAlarm.startAnimation(getBlinkAnimation());
        mAlarm.setText(alarm);
    }

    public void updateMixerMode(String mode) {
        mMixerMode.startAnimation(getBlinkAnimation());
        mMixerMode.setText(mode);
    }

    public void updateInputPressure(float inPressure) {
        mInputPressure.startAnimation(getBlinkAnimation());
        mInputPressure.setText(String.format("%f", inPressure));
    }

    public void updateOutputPressure(float outPressure) {
        mOutputPressure.startAnimation(getBlinkAnimation());
        mOutputPressure.setText(String.format("%f", outPressure));
    }

    public void updateRotationSpeed(float rotationSpeed) {
        mRotationSpeed.startAnimation(getBlinkAnimation());
        mRotationSpeed.setText(String.format("%f tr/min", rotationSpeed));
    }

    public void updateInputPressureSensorState(boolean activated) {
        mInputPressureSensorState.startAnimation(getBlinkAnimation());
        mInputPressureSensorState.setText(activated ? "CONNECTÉ" : "DÉCONNECTÉ");
    }

    public void updateOutputPressureSensorState(boolean activated) {
        mOutputPressureSensorState.startAnimation(getBlinkAnimation());
        mOutputPressureSensorState.setText(activated ? "CONNECTÉ" : "DÉCONNECTÉ");
    }

    public void updateMinSensorExceed(boolean thresholdExceed) {
        mMinSensorExceed.startAnimation(getBlinkAnimation());
        mMinSensorExceed.setText(thresholdExceed ? "OUI" : "NON");
    }

    public void updateMaxSensorExceed(boolean thresholdExceed) {
        mMaxSensorExceed.startAnimation(getBlinkAnimation());
        mMaxSensorExceed.setText(thresholdExceed ? "OUI" : "NON");
    }

    public void updateStep(int step, int subStep) {
        switch (step) {
            case 1:
                mAcquisitionSubstep.startAnimation(getBlinkAnimation());
                mAcquisitionSubstep.setText(captionForStep(step, subStep));
                break;
            case 2:
                mRegulationSubstep.startAnimation(getBlinkAnimation());
                mRegulationSubstep.setText(captionForStep(step, subStep));
                break;
            case 3:
                mPumpSubstep.startAnimation(getBlinkAnimation());
                mPumpSubstep.setText(captionForStep(step, subStep));
                break;
            case 6:
                mSlumpSubstep.startAnimation(getBlinkAnimation());
                mSlumpSubstep.setText(captionForStep(step, subStep));
                break;
        }
    }

    private String captionForStep(int step, int subStep) {
        switch (step) {
            case 1:
                switch (subStep) {
                    case -1:
                        return "Reset trame";
                    case 0:
                        return "Init terminée";
                    case 1:
                        return "Paramètres camion reçus";
                    case 2:
                        return "Paramètres livraison reçus";
                    case 11:
                        return "Bouton eau ON";
                    case 12:
                        return "Bouton eau OFF";
                    case 13:
                        return "Trame stable";
                    case 14:
                        return "Trame instable";
                }
                break;
            case 2:
                switch (subStep) {
                    case 0:
                        return "Init terminée";
                    case 1:
                        return "Debut période stabilisation";
                    case 2:
                        return "Stop période stabilisation";
                    case 3:
                        return "Reset période stabilisation";
                    case 4:
                        return "Délai période dépassé";
                }
                break;
            case 3:
                switch (subStep) {
                    case 0:
                        return "Init terminée";
                    case 1:
                        return "Debut période stabilisation";
                    case 2:
                        return "Stop période stabilisation";
                    case 3:
                        return "Reset période stabilisation";
                    case 4:
                        return "Délai période dépassé";
                }
            case 6:
                switch (subStep) {
                    case 0:
                        return "Init slump";
                    case 1:
                        return "Attente stabilisation slump";
                    case 2:
                        return "Ajout eau possible (envoie proposition)";
                    case 3:
                        return "Attente 5x de \"Ajout eau possible\"";
                }
        }
        return "Inconnu (step: " + step + ", substep " + subStep + ")";
    }

    private Animation getBlinkAnimation(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }
}
