package com.lafarge.truckmix;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import com.lafarge.truckmix.TruckMix;

/**
 * An interface for an Android <code>Activity</code> or <code>Service</code>
 * that wants to interact with beacons. The interface is used in conjunction
 * with <code>TruckMix</code> and provides a callback when the <code>TruckMixService</code>
 * is ready to use. Until this callback is call, you can't use interact with the calculator.
 *
 * In the example below, an Activity implements the <code>TruckMixConsumer</code> interface, binds
 * to the service, then when it gets the callback saying the service is ready, it starts ranging //TODO: FUCK.
 *
 *  <pre><code>
 *  public class MainActivity extends Activity implements TruckMixConsumer {
 *      protected static final String TAG = "MainActivity";
 *      private TruckMix truckMix = TruckMix.getInstanceForApplication(this);
 *
 *      {@literal @}Override
 *      protected void onCreate(Bundle savedInstanceState) {
 *          super.onCreate(savedInstanceState);
 *          setContentView(R.layout.activity_ranging);
 *          truckMix.bind(this);
 *      }
 *
 *      {@literal @}Override
 *      protected void onDestroy() {
 *          super.onDestroy();
 *          truckMix.unbind(this);
 *      }
 *
 *      {@literal @}Override
 *      public void onTruckMixServiceConnect() {
 *          truckMix.connect("32:42:42:AB:42");
 *      }
 *  }
 *  </code></pre>
 *
 * @see TruckMix
 */
public interface TruckMixConsumer {

    /**
     * Called when the beacon service is running and ready to accept your commands through the TruckMix instance.
     */
    void onTruckMixServiceConnect();

    /**
     * Called by TruckMix to get the context of your Service or Activity. This method is implemented by Service or Activity.
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    Context getApplicationContext();

    /**
     * Called by TruckMix to unbind your TruckMixConsumer to the TruckMixService. This method is implemented by Service or Activity, and
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    void unbindService(ServiceConnection connection);

    /**
     * Called by TruckMix to bind your TruckMixConsumer to the TruckMixService. This method is implemented by Service or Activity, and
     * You generally should not override it.
     *
     * @return the application context of your service or activity
     */
    boolean bindService(Intent intent, ServiceConnection connection, int mode);
}
