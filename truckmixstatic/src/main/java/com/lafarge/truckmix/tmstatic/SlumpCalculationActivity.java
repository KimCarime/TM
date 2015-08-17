package com.lafarge.truckmix.tmstatic;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.OnClick;


public class SlumpCalculationActivity extends ActionBarActivity {

    //butter knife objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slump_calculation);
        final Button mButtonEndCalculation= (Button) findViewById(R.id.buttonEndCalculation);
        mButtonEndCalculation.setOnClickListener(EndCalculation); // Event listener
    }

    //Action management
    private View.OnClickListener EndCalculation= new View.OnClickListener(){ //event handler
        @Override
        public void onClick(View vue)
        {
            startActivity(new Intent(SlumpCalculationActivity.this, MainActivity.class));
            finish();
        }
    };

   /* @Override
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
