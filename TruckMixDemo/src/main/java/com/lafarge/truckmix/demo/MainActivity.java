package com.lafarge.truckmix.demo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.demo.fragments.ConsoleListFragment;
import com.lafarge.truckmix.demo.fragments.OverviewFragment;
import com.lafarge.truckmix.demo.utils.UserPreferences;
import com.lafarge.truckmix.service.TruckMix;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;
    private boolean serviceConnected;

    TruckMix mTruckMix;
    UserPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        // Others
        mTruckMix = new TruckMix(this, communicatorListener, loggerListener);
        mPrefs = new UserPreferences(this);

        toggleService();
    }

    protected void onDestroy() {
        if (!isFinishing()) {
            mTruckMix.stopService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scan:
                mTruckMix.connect("00:12:6F:35:7E:70");
                return true;
            case R.id.send_frame:
                createSendFrameDialog().show();
                return true;
            case R.id.clear:
                getConsoleFragment().clear();
                return true;
            case R.id.customize_parameters: {
                Intent intent = new Intent(this, ParametersActivity.class);
                startActivity(intent);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleService() {
        if (!this.serviceConnected) {
            mTruckMix.startService();
        } else {
            mTruckMix.stopService();
        }

        this.serviceConnected = !this.serviceConnected;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int TAB_CONSOLE = 0;
        public static final int TAB_OVERVIEW = 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_CONSOLE:
                    return ConsoleListFragment.newInstance();
                case TAB_OVERVIEW:
                    return OverviewFragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case TAB_CONSOLE:
                    return getString(R.string.title_main_section1).toUpperCase(l);
                case TAB_OVERVIEW:
                    return getString(R.string.title_main_section2).toUpperCase(l);
                default:
                    return null;
            }
        }
    }

    private Dialog createSendFrameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actions");
        builder.setItems(R.array.send_frame_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        mTruckMix.setTruckParameters(mPrefs.getTruckParameters());
                        break;
                    case 1:
                        mTruckMix.deliveryNoteReceived(mPrefs.getDeliveryParameters());
                        break;
                    case 2:
                        mTruckMix.acceptDelivery(true);
                        break;
                    case 3:
                        mTruckMix.endDelivery();
                        break;
                    case 4:
                        mTruckMix.changeExternalDisplayState(true);
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return builder.create();
    }

    private Dialog createAddWaterConfirmationDialog(int volume) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmez");
        builder.setMessage("Autorisez-vous à ajouter " + volume + " L d'eau ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTruckMix.allowWaterAddition(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTruckMix.allowWaterAddition(false);
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private ConsoleListFragment getConsoleFragment() {
        return ((ConsoleListFragment) findFragmentByPosition(SectionsPagerAdapter.TAB_CONSOLE));
    }

    private OverviewFragment getOverviewFragment() {
        return ((OverviewFragment) findFragmentByPosition(SectionsPagerAdapter.TAB_OVERVIEW));
    }

    private Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":" + mPagerAdapter.getItemId(position));
    }

    /**
     *
     */
    private final LoggerListener loggerListener = new LoggerListener() {
        @Override
        public void log(String log) {
            getConsoleFragment().addLog(log);
        }
    };

    /**
     *
     */
    private final CommunicatorListener communicatorListener = new CommunicatorListener() {
        @Override
        public void slumpUpdated(int slump) {
            getOverviewFragment().updateSlump(slump);
        }

        @Override
        public void mixingModeActivated() {
            getOverviewFragment().updateMixerMode("MALAXAGE");
        }

        @Override
        public void unloadingModeActivated() {
            getOverviewFragment().updateMixerMode("VIDANGE");
        }

        @Override
        public void waterAdded(int volume, MessageReceivedListener.WaterAdditionMode additionMode) {}

        @Override
        public void waterAdditionRequest(int volume) {
            createAddWaterConfirmationDialog(volume).show();
        }

        @Override
        public void waterAdditionBegan() {}

        @Override
        public void waterAdditionEnd() {}

        @Override
        public void alarmWaterAdditionBlocked() {
            getOverviewFragment().updateAlarm("AJOUT D'EAU BLOQUEE");
        }

        @Override
        public void stateChanged(int step, int subStep) {
            getOverviewFragment().updateStep(step, subStep);
        }

        @Override
        public void calibrationData(float inPressure, float outPressure, float rotationSpeed) {
            getOverviewFragment().updateInputPressure(inPressure);
            getOverviewFragment().updateOutputPressure(outPressure);
            getOverviewFragment().updateRotationSpeed(rotationSpeed);
        }

        @Override
        public void alarmWaterMax() {
            getOverviewFragment().updateAlarm("EAU MAX");
        }

        @Override
        public void alarmFlowageError() {
            getOverviewFragment().updateAlarm("ERREUR ECOULEMENT");
        }

        @Override
        public void alarmCountingError() {
            getOverviewFragment().updateAlarm("ERROR COMPTAGE");
        }

        @Override
        public void inputSensorConnectionChanged(boolean connected) {
            getOverviewFragment().updateInputPressureSensorState(connected);
        }

        @Override
        public void outputSensorConnectionChanged(boolean connected) {
            getOverviewFragment().updateOutputPressureSensorState(connected);
        }

        @Override
        public void speedSensorHasExceedMinThreshold(boolean thresholdExceed) {
            getOverviewFragment().updateMinSensorExceed(thresholdExceed);
        }

        @Override
        public void speedSensorHasExceedMaxThreshold(boolean thresholdExceed) {
            getOverviewFragment().updateMaxSensorExceed(thresholdExceed);
        }
    };
}
