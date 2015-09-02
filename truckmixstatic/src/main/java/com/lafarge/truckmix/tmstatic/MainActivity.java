package com.lafarge.truckmix.tmstatic;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lafarge.truckmix.tmstatic.database.DAOTrucks;
import com.lafarge.truckmix.tmstatic.utils.DataManager;
import com.lafarge.truckmix.tmstatic.utils.DataManagerMock;

import java.util.ArrayList;
import java.util.List;
//view
import butterknife.ButterKnife;
import butterknife.InjectView;



public class MainActivity extends AppCompatActivity {

    //butter knife objects
    @InjectView(R.id.buttonStartCalculation) Button mButtonStartCalculation;
    @InjectView(R.id.fieldTagetSlump) NumberPicker mTargetSlump;
    @InjectView(R.id.truckSelectionTruckID) Spinner Liste;
    @InjectView(R.id.fieldLoadVolume) NumberPicker mLoadVolume;
    private EditText mAdminDialogEditText;

    private ArrayAdapter<String> spinnerAdapter=null;
    //Attributes
    private DataManagerMock mDataManager;
    //private DataManager mDataManager;

    //Dialogs
    private AlertDialog mAdminDialog;

    //Shared preferences
    public final static String MAC_ADDRESS ="mac address";
    SharedPreferences mPref;

    //Database
    private DAOTrucks db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        //Data manager creation
        mDataManager= new DataManagerMock();
        //Database init
        db = new DAOTrucks(this);

        //TEST MOCK
        //test purpose
        db.purge(); // A SUPPRIMER QUAND DEV FINI
        db.newTruck(mDataManager.getMockTruck1());
        db.newTruck(mDataManager.getMockTruck2());

        ////
    //WIDGET
       //spinner
        refreshSpinner();
        Liste.setAdapter(spinnerAdapter);
        // number picker
        mLoadVolume.setMinValue(0);
        mLoadVolume.setMaxValue(15);
        mLoadVolume.setValue(6);

        mTargetSlump.setMinValue(0);
        mTargetSlump.setMaxValue(300);
        mTargetSlump.setValue(0);


        mPref = PreferenceManager.getDefaultSharedPreferences(this);



        mButtonStartCalculation.setOnClickListener(StartCalculation); //listener creation for button start calculation
    }

    // Action
    private View.OnClickListener StartCalculation= new View.OnClickListener() //event handler for action start calculation
    {
        @Override
        public void onClick(View vue)
        {
            //get the field content and put them in the the data manager class
                //TruckID
            String _TruckID=Liste.getSelectedItem().toString();

            if (_TruckID==getResources().getString(R.string.noTruckAvailable)) //no truck available
            {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.noTruckAvailableWarning), Toast.LENGTH_SHORT).show();
            }
            else { //Truck is selected

                mDataManager.setVolumeLoad(String.valueOf(mLoadVolume.getValue())); //get the load volume
                mDataManager.fetchSelectedTruck(db.fetchTruck(_TruckID));
                //mDataManager.fetchMACAddrBT();// fetch mac address here to avoid data corruption when updating mac address on settings
                String buff=mPref.getString(MAC_ADDRESS,"00:12:6F:35:7E:70");

                mDataManager.setMACAddrBT(buff);
                mDataManager.setTargetSlump(String.valueOf(mTargetSlump.getValue()));
                Toast.makeText(MainActivity.this,"connect to : "+mDataManager.getMACAddrBT() , Toast.LENGTH_SHORT).show();
                //TargetSlump
                Intent intentSlumpCalculation = new Intent(MainActivity.this, SlumpCalculationActivity.class);
                intentSlumpCalculation.putExtra("data", mDataManager);
                startActivity(intentSlumpCalculation);
                finish();
            }
        }
    };

// menu management
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //intentCalculatorSettings.putExtra("data",mDataManager);
        mAdminDialog=createAdminDialog(id);
        mAdminDialog.show();
        //noinspection SimplifiableIfStatement
      /*  if (id == R.id.menu_1) { // Truck settings
            mAdminDialog=createAdminDialog(id);
            startActivity(intentTruckSettings);
            return true;
        }
        if (id == R.id.menu_2) { // Calculator settings
            mAdminDialog=createAdminDialog(id);
            startActivity(intentCalculatorSettings);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    //Dialog factory
    AlertDialog createAdminDialog(final int  choice){
        View view=getLayoutInflater().inflate(R.layout.dialog_admin,null);

        mAdminDialogEditText=(EditText)view.findViewById(R.id.adminDialogEditText);


        final AlertDialog.Builder builder =new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.AdminDialogTitle));
        builder.setView(view);
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdminDialog.dismiss();
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                boolean pass=false;
                String password = mAdminDialogEditText.getText().toString();
                pass=passwordChecker(password);
                if(pass==true){
                    //pass data throught intent
                    Intent intentTruckSettings = new Intent(MainActivity.this,ParametersTruckListActivity.class);
                    intentTruckSettings.putExtra("data",mDataManager);
                    Intent intentCalculatorSettings = new Intent(MainActivity.this,ParametersCalculatorActivity.class);
                    // launch activity here
                    if (choice == R.id.menu_1) { // Truck settings
                        startActivity(intentTruckSettings);
                    }
                    if (choice == R.id.menu_2) { // Calculator settings
                        startActivity(intentCalculatorSettings);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.adminDialogErrorMessage), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return builder.create();
    }

    //other methods
    private void refreshSpinner()
    {

        List<String> buffer = new ArrayList<String>();
        mDataManager.fetchTruckList(db.fetchTruckList()); //fetch truck list in database
        if(mDataManager.getTruckList().isEmpty())
            buffer.add(getResources().getString(R.string.noTruckAvailable));
        else {

            buffer=new ArrayList<String>(mDataManager.getTruckList());
        }
        //updating adapter
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, buffer);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
    }
    private boolean passwordChecker(String password){

        boolean result=false;
        String pass1=password;
        String pass2="dev"; // registered password

        if(pass1.equals(pass2))
            result=true;
        else
            result=false;
        return result;
    }
}
