package com.lafarge.truckmix.tmstatic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.lafarge.truckmix.TruckMix;
import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.common.enums.AlarmType;
import com.lafarge.truckmix.common.enums.RotationDirection;
import com.lafarge.truckmix.common.enums.SpeedSensorState;
import com.lafarge.truckmix.common.enums.WaterAdditionMode;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.tmstatic.utils.DataManager;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;
import com.lafarge.truckmix.tmstatic.utils.DataTruck;

import java.nio.ByteBuffer;

import javax.xml.datatype.DatatypeConfigurationException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class SlumpCalculationActivity extends AppCompatActivity {

    //butter knife objects
    @InjectView(R.id.slumpCalculationMeasuredSlump) TextView mTextViewMeasuredSlump;
    @InjectView(R.id.slumpCalculationTargetSlump) TextView mTextViewTargetSlump;
    @InjectView(R.id.slumpCalculationTuckID) TextView mTextViewTruckID;
    @InjectView(R.id.slumpCalculationEndCalculation) Button mButtonEndCalculation;
    @InjectView(R.id.slumpCalculationLoadVolume) TextView mTextViewLoadVolume;
    @InjectView(R.id.slumpCalculationSensorState) TextView mTextViewSensorState;
    //attributes
    private DataManagerMock mDataManager;
    private final String TAG="SlumpCalculation";
    private Sensors mSensorsState;
    private boolean flag_dialog=true;

    //Dialog
    private Dialog mExitDialog;
    private Dialog mAddWaterConfirmationDialog;
    private ProgressDialog mWaitingConnection;
    private AlertDialog mSpeedSensorDialog;

    //TruckMix
    // Service
    private TruckMix mTruckMix;
    //Thread
    private Handler mMainThreadHandler;
    private int mTargetSlumpValue=0;
    private int mWaterVolumeMax=0;
    private int mLoadVolume=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slump_calculation);
        ButterKnife.inject(this);

        //get data
        Intent incomingIntent=getIntent();
        mDataManager =(DataManagerMock) incomingIntent.getSerializableExtra("data");


        //widget initialisation
        mButtonEndCalculation.setOnClickListener(EndCalculation); // Event listener
        mTextViewTruckID.setText(mDataManager.getSelectedTruck().getRegistrationID());
        mTextViewTargetSlump.setText( mDataManager.getTargetSlump());
        mTextViewLoadVolume.setText(mDataManager.getVolumeLoad());
        // animate the measured slump
        mTextViewMeasuredSlump.setAnimation(getWaitAnimation());
        mTextViewMeasuredSlump.animate();

        // sensor state
        mTextViewSensorState.setText("NO");
        mSensorsState= new Sensors();

        if  (!(mDataManager.getTargetSlump().equals(getResources().getString(R.string.noSlumpEntered)))){
            mTargetSlumpValue = Integer.parseInt(mDataManager.getTargetSlump());
            mWaterVolumeMax=255;//Max value to be received (byte conversion
        }
        if  (!(mDataManager.getVolumeLoad().equals(getResources().getString(R.string.noSlumpEntered)))){
            mLoadVolume = Integer.parseInt(mDataManager.getVolumeLoad());
        }

        //TruckMix init
        // Thread
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        // Service
        mTruckMix = new TruckMix.Builder(this)
                .setCommunicatorListener(mCommunicatorListener)
                .setLoggerListener(mLoggerListener)
                .setEventListener(mEventListener)
                .setConnectionStateListener(mConnectionStateListener)
                .build();
        mTruckMix.start(mDataManager.getMACAddrBT());
        mTruckMix.setTruckParameters(mDataManager.getSelectedTruck().getTruckParameters());
        mTruckMix.deliveryNoteReceived(new DeliveryParameters(mTargetSlumpValue, mWaterVolumeMax, mLoadVolume));// target slump/volume ajout eau authorise/volume charge
        mTruckMix.acceptDelivery(true);
        mTruckMix.setWaterRequestAllowed(true);
        mTruckMix.changeExternalDisplayState(true);


        //Dialog creation
        waitingConnectionDialog();
        mSpeedSensorDialog=createSpeedSensorDialog();


    }

    //Action management
    private View.OnClickListener EndCalculation= new View.OnClickListener(){ //event handler
        @Override
        public void onClick(View vue)
        {
            startActivity(new Intent(SlumpCalculationActivity.this, MainActivity.class));
            mTruckMix.endDelivery();
            mTruckMix.stop();
            finish();
        }
    };

    //
    // TruckMix interface
    //

    private final ConnectionStateListener mConnectionStateListener = new ConnectionStateListener() {
        @Override
        public void onCalculatorConnected() {

        }

        @Override
        public void onCalculatorConnecting() {

        }

        @Override
        public void onCalculatorDisconnected() {

        }
    };

    /**
     *
     */
    private final LoggerListener mLoggerListener = new LoggerListener() {
        @Override
        public void log(final String log) {
            Log.d(TAG, log);
        }
    };

    /**
     *
     */
    private final CommunicatorListener mCommunicatorListener = new CommunicatorListener() {
        @Override
        public void slumpUpdated(final int slump) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTextViewMeasuredSlump.clearAnimation();
                    mTextViewMeasuredSlump.startAnimation(getBlinkAnimation());
                    mTextViewMeasuredSlump.setText(String.format("%d", slump));


                }
            });
        }
        @Override
        public void temperatureUpdated(final float temperature) {
        }

        @Override
        public void rotationDirectionChanged(final RotationDirection rotationDirection) {

        }

        @Override
        public void waterAdded(final int volume, final WaterAdditionMode additionMode) {

        }

        @Override
        public void waterAdditionRequest(final int volume) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAddWaterConfirmationDialog!=null){
                        if (mAddWaterConfirmationDialog.isShowing()) {
                            mAddWaterConfirmationDialog.dismiss();
                        }
                    }

                    mAddWaterConfirmationDialog = createAddWaterConfirmationDialog(volume);
                    mAddWaterConfirmationDialog.show();
                }
            });
        }

        @Override
        public void waterAdditionBegan() {
        }

        @Override
        public void waterAdditionEnd() {
        }

        @Override
        public void stateChanged(final int step, final int subStep) {

        }

        @Override
        public void internData(final boolean inputSensorConnected, final boolean outputSensorConnected, final SpeedSensorState speedSensorState) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorsState.setStateInputSensor(inputSensorConnected);
                    mSensorsState.setStateOutputSensor(outputSensorConnected);
                    if (speedSensorState ==SpeedSensorState.NORMAL){
                        if (mSpeedSensorDialog.isShowing())
                            mSpeedSensorDialog.dismiss();
                    }else if (speedSensorState ==SpeedSensorState.TOO_FAST){
                        if (!mSpeedSensorDialog.isShowing()) {
                            mSpeedSensorDialog.setMessage(getResources().getString(R.string.SpeedSensorDialodTooFast));
                            mSpeedSensorDialog.show();
                        }
                    }else if (speedSensorState ==SpeedSensorState.TOO_SLOW){
                        if (!mSpeedSensorDialog.isShowing()) {
                            mSpeedSensorDialog.setMessage(getResources().getString(R.string.SpeedSensorDialodTooSlow));
                            mSpeedSensorDialog.show();
                        }
                    }
                }
            });
        }

        @Override
        public void calibrationData(final float inputPressure, final float outputPressure, final float rotationSpeed) {

        }

        @Override
        public void inputSensorConnectionChanged(final boolean connected) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorsState.setStateInputSensor(connected);
                }
            });

        }

        @Override
        public void outputSensorConnectionChanged(final boolean connected) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorsState.setStateOutputSensor(connected);
                }
            });
        }

        @Override
        public void speedSensorStateChanged(final SpeedSensorState speedSensorState) { //NORMAL, TOO_SLOW, TOO_FAST
           mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (speedSensorState ==SpeedSensorState.NORMAL){
                        if (mSpeedSensorDialog.isShowing())
                            mSpeedSensorDialog.dismiss();
                    }else if (speedSensorState ==SpeedSensorState.TOO_FAST){
                        if (!mSpeedSensorDialog.isShowing()) {
                            mSpeedSensorDialog.setMessage(getResources().getString(R.string.SpeedSensorDialodTooFast));
                            mSpeedSensorDialog.show();
                        }
                    }else if (speedSensorState ==SpeedSensorState.TOO_SLOW){
                        if (!mSpeedSensorDialog.isShowing()) {
                            mSpeedSensorDialog.setMessage(getResources().getString(R.string.SpeedSensorDialodTooSlow));
                            mSpeedSensorDialog.show();
                        }
                    }

                }
            });
        }

        @Override
        public void alarmTriggered(final AlarmType alarmType) {

        }
    };

    /**
     *
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(final Event event) {
            Log.i(TAG, "new quality tracking event triggered: " + event);
        }
    };

    //Close the TM service when leaving activity or application

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            mExitDialog = createExitDialog();
            mExitDialog.show();

        }
        return true;
    }

    @Override
    protected void onDestroy(){
        mTruckMix.endDelivery();
        mTruckMix.stop();
        super.onDestroy();
    }
    @Override
    protected void onStop(){
        mTruckMix.endDelivery();
        mTruckMix.stop();
        super.onStop();
    }
 /*   @Override
    protected void onPause(){
        mTruckMix.stop();
        super.onPause();
    }*/

    //Dialog factory

    private AlertDialog createSpeedSensorDialog(){
        final AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.SpeedSensorDialogTitle));
        builder.setMessage("...");
        return builder.create();
    }
    private Dialog createAddWaterConfirmationDialog(int volume) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.waterAdditionDialogTitle));
        builder.setMessage(getResources().getString(R.string.waterAdditionDialogMessage) + volume + " L ");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTruckMix.allowWaterAddition(false);
                dialog.dismiss();
            }
        });
        return builder.create();
    }
    private Dialog createExitDialog(){
        final AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.exitTitle));
        builder.setMessage(getResources().getString(R.string.exitMessage));
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                //leave the application
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                //don't leave the application
                dialog.dismiss();
            }
        });
        return builder.create();
    }
    private void waitingConnectionDialog() {
        flag_dialog=true;
        mWaitingConnection= new ProgressDialog(this);
        mWaitingConnection.setTitle(getResources().getString(R.string.waitingConnectionDialogTitle));
        mWaitingConnection.setMessage(getResources().getString(R.string.waitingConnectionDialogMessage));
        mWaitingConnection.setCancelable(false);
        mWaitingConnection.setButton(ProgressDialog.BUTTON_NEUTRAL,
                getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        flag_dialog = false;

                    }
                });
        mWaitingConnection.show();


        new Thread((new Runnable() {
            @Override
            public void run() {
                while ((mTruckMix.isConnected()==false)&&(flag_dialog==true))
                {
                    Log.d("COUCOU","thread is running");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mWaitingConnection.dismiss();
                if(flag_dialog==false){
                    startActivity(new Intent(SlumpCalculationActivity.this, MainActivity.class));
                    mTruckMix.endDelivery();
                    mTruckMix.stop();
                    finish();

                }
            }
        })).start();
    }

    //Animation
    private Animation getBlinkAnimation(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(300);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }
    private Animation getWaitAnimation(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }

    // privates class
    private class Sensors{
        //attributes
        private boolean stateOutputSensor=false;
        private boolean stateInputSensor=false;
        private boolean stateSensors=false;
        //methods
        public void setStateInputSensor(boolean stateInputSensor) {
           // Log.d("COUCOU","input"+String.valueOf(stateInputSensor));
            if (this.stateInputSensor!=stateInputSensor){
                this.stateInputSensor = stateInputSensor;
                onStateChange();

            }
        }

        public void setStateOutputSensor(boolean stateOutputSensor) {
           // Log.d("COUCOU","output"+String.valueOf(stateOutputSensor));
            if (this.stateOutputSensor!=stateOutputSensor){
                this.stateOutputSensor = stateOutputSensor;
                onStateChange();
            }
        }


        private void onStateChange(){
           // Log.d("COUCOU","State change"+ String.valueOf(stateInputSensor)+String.valueOf(stateOutputSensor));
            if(stateOutputSensor&&stateInputSensor)
                stateSensors=true;
            else
                stateSensors=false;
            refreshDisplay();
        }
        public void refreshDisplay(){
            if(stateSensors)
                mTextViewSensorState.setText("YES");
            else
                mTextViewSensorState.setText("NO");

        }


    }

    //menu management
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slump_calculation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.debugSlumpCalculation) {
            mTruckMix.setTruckParameters(mDataManager.getSelectedTruck().getTruckParameters());
            mTruckMix.changeExternalDisplayState(true);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
