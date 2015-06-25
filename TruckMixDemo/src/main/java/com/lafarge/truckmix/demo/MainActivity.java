package com.lafarge.truckmix.demo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.lafarge.truckmix.TruckMix;
import com.lafarge.truckmix.bluetooth.ConnectionStateListener;
import com.lafarge.truckmix.communicator.events.Event;
import com.lafarge.truckmix.communicator.listeners.CommunicatorListener;
import com.lafarge.truckmix.communicator.listeners.EventListener;
import com.lafarge.truckmix.communicator.listeners.LoggerListener;
import com.lafarge.truckmix.demo.fragments.ConsoleListFragment;
import com.lafarge.truckmix.demo.fragments.OverviewFragment;
import com.lafarge.truckmix.demo.utils.UserPreferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private ViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;
    private boolean serviceConnected;

    private TruckMix mTruckMix;
    private ArrayList<Event> mEvents = new ArrayList<Event>();
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
        mTruckMix = TruckMix.get(this);

        // Others
        mPrefs = new UserPreferences(this);
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
            case R.id.connect: {
                // This is the bluetooth mac address of the calculator.
                final String address = "00:12:6F:35:7E:70";

                mTruckMix.setCommunicatorListener(mCommunicatorListener);
                mTruckMix.setEventListener(mEventListener);
                mTruckMix.setLoggerListener(mLoggerListener);

                mTruckMix.connect(address, new ConnectionStateListener() {
                    @Override
                    public void onCalculatorConnected() {
                        Log.i(TAG, "Calculator connected");
                    }

                    @Override
                    public void onCalculatorConnecting() {
                        Log.i(TAG, "Calculator connecting");
                    }

                    @Override
                    public void onCalculatorDisconnected() {
                        Log.i(TAG, "Calculator disconnected");
                    }
                });
                return true;
            }
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
            case R.id.send_logs: {
                final Intent intent=new Intent(Intent.ACTION_SEND);
                String[] recipients = {"kim.abdoul-carime@lafarge.com"};
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.putExtra(Intent.EXTRA_SUBJECT, "[TruckMix] Logs");
                intent.putExtra(Intent.EXTRA_TEXT, "Vous trouverez les logs ci-joint.\nCordialement,\nTruckMix");
                for (Uri uri : getUriListForLogs()) {
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                }

                startActivity(Intent.createChooser(intent, "Envoie mail"));
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //
    // TruckMix interface
    //

    /**
     *
     */
    private final LoggerListener mLoggerListener = new LoggerListener() {
        @Override
        public void log(final String log) {
            Log.d(TAG, log);
            LOGGER.info(log);
            getConsoleFragment().addLog(log);
        }
    };

    /**
     *
     */
    private final CommunicatorListener mCommunicatorListener = new CommunicatorListener() {
        @Override
        public void slumpUpdated(final int slump) {
            getOverviewFragment().updateSlump(slump);
        }

        @Override
        public void rotationDirectionChanged(final RotationDirection rotationDirection) {
            switch (rotationDirection) {
                case MIXING:
                    getOverviewFragment().updateMixerMode("MALAXAGE");
                    break;
                case UNLOADING:
                    getOverviewFragment().updateMixerMode("VIDANGE");
                    break;
            }
        }

        @Override
        public void waterAdded(final int volume, final WaterAdditionMode additionMode) {

        }

        @Override
        public void waterAdditionRequest(final int volume) {
            createAddWaterConfirmationDialog(volume).show();
        }

        @Override
        public void waterAdditionBegan() {}

        @Override
        public void waterAdditionEnd() {}

        @Override
        public void stateChanged(final int step, final int subStep) {
            getOverviewFragment().updateStep(step, subStep);
        }

        @Override
        public void internData(final boolean inputSensorConnected, final boolean outputSensorConnected, final SpeedSensorState speedSensorState) {

        }

        @Override
        public void calibrationData(final float inputPressure, final float outputPressure, final float rotationSpeed) {
            getOverviewFragment().updateInputPressure(inputPressure);
            getOverviewFragment().updateOutputPressure(outputPressure);
            getOverviewFragment().updateRotationSpeed(rotationSpeed);
        }

        @Override
        public void inputSensorConnectionChanged(final boolean connected) {
            getOverviewFragment().updateInputPressureSensorState(connected);
        }

        @Override
        public void outputSensorConnectionChanged(final boolean connected) {
            getOverviewFragment().updateOutputPressureSensorState(connected);
        }

        @Override
        public void speedSensorStateChanged(final SpeedSensorState speedSensorState) {
            switch (speedSensorState) {
                case NORMAL:
                    getOverviewFragment().updateMinSensorExceed(false);
                    getOverviewFragment().updateMaxSensorExceed(false);
                    break;
                case TOO_SLOW:
                    getOverviewFragment().updateMinSensorExceed(true);
                    break;
                case TOO_FAST:
                    getOverviewFragment().updateMaxSensorExceed(true);
                    break;
            }
        }

        @Override
        public void alarmTriggered(final AlarmType alarmType) {
            switch (alarmType) {
                case WATER_ADDITION_BLOCKED:
                    getOverviewFragment().updateAlarm("AJOUT D'EAU BLOQUEE");
                    break;
                case WATER_MAX:
                    getOverviewFragment().updateAlarm("EAU MAX");
                    break;
                case FLOWAGE_ERROR:
                    getOverviewFragment().updateAlarm("ERREUR ECOULEMENT");
                    break;
                case COUNTING_ERROR:
                    getOverviewFragment().updateAlarm("ERROR COMPTAGE");
                    break;
            }
        }
    };

    /**
     *
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(final Event event) {
            String str =
                    "{" +
                            "  id: " + event.id.getIdValue() +
                            "  value:" + event.value +
                            "  timestamp: " + event.timestamp +
                            "}";
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    //
    // Other
    //

    private ArrayList<Uri> getUriListForLogs() {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.lafarge.truckmix.demo/logs";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File files[] = f.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (File file : files) {
            Log.d("Files", "FileName:" + file.getName());
            uris.add(Uri.fromFile(file));
        }
        return uris;
    }
}
