package com.lafarge.truckmix.demo;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // View management
    private ViewPager mViewPager;
    private SectionsPagerAdapter mPagerAdapter;

    // Dialog
    private Dialog mAddWaterConfirmationDialog;

    // Service
    private TruckMix mTruckMix;

    private ArrayList<Event> mEvents = new ArrayList<Event>();

    private UserPreferences mPrefs;

    private Handler mMainThreadHandler;

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

        // Thread
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        // Service
        mTruckMix = new TruckMix.Builder(this)
                .setCommunicatorListener(mCommunicatorListener)
                .setLoggerListener(mLoggerListener)
                .setEventListener(mEventListener)
                .setConnectionStateListener(mConnectionStateListener)
                .build();

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
                mTruckMix.start("00:12:6F:35:7E:70");
                return true;
            }
            case R.id.send_frame:
                createSendFrameDialog().show();
                return true;
            case R.id.clear:
                getConsoleFragment().clear();
                return true;
            case R.id.customize_parameters:
                startActivity(new Intent(this, ParametersActivity.class));
                return true;
            case R.id.send_logs: {
                final Intent intent= new Intent(Intent.ACTION_SEND);
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
            LOGGER.info(log);
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getConsoleFragment().addLog(log);
                }
            });
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
                    getOverviewFragment().updateSlump(slump);
                }
            });
        }

        @Override
        public void temperatureUpdated(final float temperature) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateTemperature(temperature);
                }
            });
        }

        @Override
        public void rotationDirectionChanged(final RotationDirection rotationDirection) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateRotationDirection(rotationDirection);
                }
            });
        }

        @Override
        public void waterAdded(final int volume, final WaterAdditionMode additionMode) {

        }

        @Override
        public void waterAdditionRequest(final int volume) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mAddWaterConfirmationDialog.isShowing()) {
                        mAddWaterConfirmationDialog.dismiss();
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
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateStep(step, subStep);
                }
            });
        }

        @Override
        public void internData(final boolean inputSensorConnected, final boolean outputSensorConnected, final SpeedSensorState speedSensorState) {

        }

        @Override
        public void calibrationData(final float inputPressure, final float outputPressure, final float rotationSpeed) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateInputPressure(inputPressure);
                    getOverviewFragment().updateOutputPressure(outputPressure);
                    getOverviewFragment().updateRotationSpeed(rotationSpeed);
                }
            });
        }

        @Override
        public void inputSensorConnectionChanged(final boolean connected) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateInputPressureSensorState(connected);
                }
            });
        }

        @Override
        public void outputSensorConnectionChanged(final boolean connected) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateOutputPressureSensorState(connected);
                }
            });
        }

        @Override
        public void speedSensorStateChanged(final SpeedSensorState speedSensorState) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateSpeedSensorState(speedSensorState);
                }
            });
        }

        @Override
        public void alarmTriggered(final AlarmType alarmType) {
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    getOverviewFragment().updateAlarm(alarmType);
                }
            });
        }
    };

    /**
     *
     */
    private final EventListener mEventListener = new EventListener() {
        @Override
        public void onNewEvents(final Event event) {
            LOGGER.info("new event triggered: " + event);
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
        File f = new File(path);
        File files[] = f.listFiles();
        for (File file : files) {
            uris.add(Uri.fromFile(file));
        }
        return uris;
    }
}
