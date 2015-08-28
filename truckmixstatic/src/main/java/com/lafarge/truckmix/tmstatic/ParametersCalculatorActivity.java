package com.lafarge.truckmix.tmstatic;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.lafarge.truckmix.tmstatic.utils.DataManager;

import butterknife.ButterKnife;
import butterknife.InjectView;



public class ParametersCalculatorActivity extends AppCompatActivity {

    @InjectView(R.id.MacAddress) EditText mMACAddress;

    DataManager mDataManager;

    //Shared preferences
    public final static String MAC_ADDRESS ="mac address";
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters_calculator);
        ButterKnife.inject(this);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPref.edit();

        String _macAddress= mPref.getString(MAC_ADDRESS,"00:12:6F:35:7E:70");

        mMACAddress.setText(_macAddress);
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
            if (mMACAddress.getText().toString().length()==17) {
                //mDataManager.setMACAddrBT(mMACAddress.getText().toString());

                mEditor.putString(MAC_ADDRESS,mMACAddress.getText().toString());
                mEditor.commit();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.CalculatorParametersMACSaved), Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.CalculatorParametersMACIncorrect), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //format the MAC address
TextWatcher formatMAC=new TextWatcher() {
        String mPreviousMac = null;

        @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String enteredMac = mMACAddress.getText().toString().toUpperCase();
        String cleanMac = clearNonMacCharacters(enteredMac);
        String formattedMac = formatMacAddress(cleanMac);

        int selectionStart = mMACAddress.getSelectionStart();
        formattedMac = handleColonDeletion(enteredMac, formattedMac, selectionStart);
        int lengthDiff = formattedMac.length() - enteredMac.length();

        setMacEdit(cleanMac, formattedMac, selectionStart, lengthDiff);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
        /**
         * Strips all characters from a string except A-F and 0-9.
         * @param mac       User input string.
         * @return          String containing MAC-allowed characters.
         */
        private String clearNonMacCharacters(String mac) {
            return mac.toString().replaceAll("[^A-Fa-f0-9]", "");
        }
        /**
         * Adds a colon character to an unformatted MAC address after
         * every second character (strips full MAC trailing colon)
         * @param cleanMac      Unformatted MAC address.
         * @return              Properly formatted MAC address.
         */
        private String formatMacAddress(String cleanMac) {
            int grouppedCharacters = 0;
            String formattedMac = "";

            for (int i = 0; i < cleanMac.length(); ++i) {
                formattedMac += cleanMac.charAt(i);
                ++grouppedCharacters;

                if (grouppedCharacters == 2) {
                    formattedMac += ":";
                    grouppedCharacters = 0;
                }
            }

            // Removes trailing colon for complete MAC address
            if (cleanMac.length() == 12)
                formattedMac = formattedMac.substring(0, formattedMac.length() - 1);

            return formattedMac;
        }
        /**
         * Upon users colon deletion, deletes MAC character preceding deleted colon as well.
         * @param enteredMac            User input MAC.
         * @param formattedMac          Formatted MAC address.
         * @param selectionStart        MAC EditText field cursor position.
         * @return                      Formatted MAC address.
         */
        private String handleColonDeletion(String enteredMac, String formattedMac, int selectionStart) {
            if (mPreviousMac != null && mPreviousMac.length() > 1) {
                int previousColonCount = colonCount(mPreviousMac);
                int currentColonCount = colonCount(enteredMac);

                if (currentColonCount < previousColonCount) {
                    formattedMac = formattedMac.substring(0, selectionStart - 1) + formattedMac.substring(selectionStart);
                    String cleanMac = clearNonMacCharacters(formattedMac);
                    formattedMac = formatMacAddress(cleanMac);
                }
            }
            return formattedMac;
        }
        /**
         * Gets MAC address current colon count.
         * @param formattedMac      Formatted MAC address.
         * @return                  Current number of colons in MAC address.
         */
        private int colonCount(String formattedMac) {
            return formattedMac.replaceAll("[^:]", "").length();
        }

        /**
         * Removes TextChange listener, sets MAC EditText field value,
         * sets new cursor position and re-initiates the listener.
         * @param cleanMac          Clean MAC address.
         * @param formattedMac      Formatted MAC address.
         * @param selectionStart    MAC EditText field cursor position.
         * @param lengthDiff        Formatted/Entered MAC number of characters difference.
         */
        private void setMacEdit(String cleanMac, String formattedMac, int selectionStart, int lengthDiff) {
            mMACAddress.removeTextChangedListener(this);
            if (cleanMac.length() <= 12) {
                mMACAddress.setText(formattedMac);
                mMACAddress.setSelection(selectionStart + lengthDiff);
                mPreviousMac = formattedMac;
            } else {
                mMACAddress.setText(mPreviousMac);
                mMACAddress.setSelection(mPreviousMac.length());
            }
            mMACAddress.addTextChangedListener(this);
        }

};


}
