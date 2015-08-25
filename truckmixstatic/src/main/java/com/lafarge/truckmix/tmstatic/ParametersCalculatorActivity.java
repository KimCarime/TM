package com.lafarge.truckmix.tmstatic;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.lafarge.truckmix.tmstatic.utils.DataManager;

import butterknife.ButterKnife;
import butterknife.InjectView;



public class ParametersCalculatorActivity extends AppCompatActivity {

    @InjectView(R.id.MacAddress) EditText mMACAddress;

    DataManager mDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_calculator);
        ButterKnife.inject(this);
        mDataManager=new DataManager();
        mDataManager.fetchMACAddrBT();
        mMACAddress.setText(mDataManager.getMACAddrBT());
        mMACAddress.addTextChangedListener(formatMAC);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parameters_calculator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.calculatorParam1) { //save
            mDataManager.setMACAddrBT(mMACAddress.getText().toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //format the MAC address
TextWatcher formatMAC=new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
};

}
