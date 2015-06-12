package com.lafarge.truckmix.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.lafarge.truckmix.common.models.DeliveryParameters;
import com.lafarge.truckmix.common.models.TruckParameters;
import com.lafarge.truckmix.demo.fragments.DeliveryParametersFragment;
import com.lafarge.truckmix.demo.fragments.TruckParametersFragment;
import com.lafarge.truckmix.demo.utils.UserPreferences;

import java.util.Locale;

public class ParametersActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private UserPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Other
        mPrefs = new UserPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_parameters, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.save:
                saveParameters();
                finish();
                return true;
            case R.id.reset:
                resetParameters();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveParameters() {
        // Get updated parameters
        TruckParameters truckParameters = getTruckParametersFragment().getUpdatedParameters();
        DeliveryParameters deliveryParameters = getDeliveryParametersFragment().getUpdatedParameters();

        // Save them
        mPrefs.setTruckParameters(truckParameters);
        mPrefs.setDeliveryParameters(deliveryParameters);
    }

    private void resetParameters() {
        mPrefs.clear();
        getTruckParametersFragment().updateParameters();
        getDeliveryParametersFragment().updateParameters();
    }

    private TruckParametersFragment getTruckParametersFragment() {
        return ((TruckParametersFragment) findFragmentByPosition(SectionsPagerAdapter.TAB_TRUCK_PARAMETERS));
    }

    private DeliveryParametersFragment getDeliveryParametersFragment() {
        return ((DeliveryParametersFragment) findFragmentByPosition(SectionsPagerAdapter.TAB_DELIVERY_PARAMETERS));
    }

    private Fragment findFragmentByPosition(int position) {
        return getSupportFragmentManager().findFragmentByTag(
                "android:switcher:" + mViewPager.getId() + ":" + mSectionsPagerAdapter.getItemId(position));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int TAB_TRUCK_PARAMETERS = 0;
        public static final int TAB_DELIVERY_PARAMETERS = 1;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case TAB_TRUCK_PARAMETERS:
                    return TruckParametersFragment.newInstance();
                case TAB_DELIVERY_PARAMETERS:
                    return DeliveryParametersFragment.newInstance();
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
                case TAB_TRUCK_PARAMETERS:
                    return getString(R.string.title_customize_parameters_section1).toUpperCase(l);
                case TAB_DELIVERY_PARAMETERS:
                    return getString(R.string.title_customize_parameters_section2).toUpperCase(l);
                default:
                    return null;
            }
        }
    }
}
