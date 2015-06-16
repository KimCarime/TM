package com.lafarge.truckmix.demo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.decoder.listeners.MessageReceivedListener;
import com.lafarge.truckmix.demo.fragments.ConsoleListFragment;
import com.lafarge.truckmix.demo.fragments.OverviewFragment;
import com.lafarge.truckmix.demo.utils.UserPreferences;
import com.lafarge.truckmix.TruckMix;
import com.lafarge.truckmix.TruckMixConnectionState;
import com.lafarge.truckmix.TruckMixConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TruckMixConsumer {

    private static final String TAG = "MainActivity";
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private ViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;
    private boolean serviceConnected;

    private TruckMix mTruckMix = TruckMix.getInstanceForApplication(this);
    private UserPreferences mPrefs;

    //
    // Life cycle
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        // Binding to the service
        mTruckMix.bind(this);

        // Others
        mPrefs = new UserPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTruckMix.unbind(this);
    }

    //
    // Menu management
    //

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
            case R.id.send_frame:
                createSendFrameDialog().show();
                return true;
            case R.id.clear:
                getConsoleFragment().clear();
                return true;
            case R.id.customize_parameters: {
                final Intent intent = new Intent(this, ParametersActivity.class);
                startActivity(intent);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //
    // TruckMix interface
    //

    @Override
    public void onTruckMixServiceConnect() {
        // This is the bluetooth mac address of the calculator.
        final String address = "00:12:6F:35:7E:70";

        try {
            mTruckMix.connect(address, mConnectionState, mCommunicatorListener, mLoggerListener, mEventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final TruckMixConnectionState mConnectionState = new TruckMixConnectionState() {
        @Override
        public void onCalculatorConnected() {
            Log.i(TAG, "Calculator connected");
        }

        public void onCalculatorConnecting() {
            Log.i(TAG, "Calculator connecting");
        }

        @Override
        public void onCalculatorDisconnected() {
            Log.i(TAG, "Calculator disconnected");
        }
    };

    /**
     *
     */
    private final LoggerListener mLoggerListener = new LoggerListener() {
        @Override
        public void log(String log) {
            LOGGER.info(log);
            getConsoleFragment().addLog(log);
        }
    };

    /**
     *
     */
    private final CommunicatorListener mCommunicatorListener = new CommunicatorListener() {
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
        public void calibrationData(float inputPressure, float outputPressure, float rotationSpeed) {
            getOverviewFragment().updateInputPressure(inputPressure);
            getOverviewFragment().updateOutputPressure(outputPressure);
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

    /**
     *
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(Event event) {

        }
    };

    /**
     *
     */
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

    //
    // Dialogs factory
    //

    private Dialog createSendFrameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actions");
        builder.setItems(R.array.send_frame_array, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
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
                } catch (RemoteException e) {
                    e.printStackTrace();
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmez");
        builder.setMessage("Autorisez-vous à ajouter " + volume + " L d'eau ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mTruckMix.allowWaterAddition(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mTruckMix.allowWaterAddition(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    //
    // Fragment getters
    //

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
}
