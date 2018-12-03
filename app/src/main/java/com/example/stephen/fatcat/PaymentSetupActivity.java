package com.example.stephen.fatcat;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.dwolla.DwollaUtil;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatGlobals;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PaymentSetupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_setup);

        final EditText firstName = findViewById(R.id.first_name);
        final EditText lastName = findViewById(R.id.last_name);
        final EditText address1 = findViewById(R.id.address_1);
        final EditText address2 = findViewById(R.id.address_2);
        final EditText city = findViewById(R.id.city);
        final Spinner state = findViewById(R.id.state_spinner);

        // Set default spinner value to MD
        state.setSelection(23);

        final EditText zip = findViewById(R.id.postal_code);
        final DatePicker dateOfBirth = findViewById(R.id.date_of_birth);
        final EditText ssn = findViewById(R.id.ssn);

        final Button submit = findViewById(R.id.create_customer_button);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FatcatFriend user = MainActivity.globals.myProfile;

                Calendar calendar =  Calendar.getInstance();
                calendar.set(dateOfBirth.getYear(), dateOfBirth.getMonth(), dateOfBirth.getDayOfMonth());
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

                String[] args = new String[10];
                args[0] = user.getEmail();
                args[1] = firstName.getText().toString();
                args[2] = lastName.getText().toString();
                args[3] = address1.getText().toString();
                args[4] = address2.getText().toString() == "" ? null : address2.getText().toString();
                args[5] = city.getText().toString();
                args[6] = state.getSelectedItem().toString();
                args[7] = zip.getText().toString();
                args[8] = dateFormatter.format(calendar.getTime());
                args[9] = ssn.getText().toString();

                DwollaCustomerCreationTask creationTask = new DwollaCustomerCreationTask();
                creationTask.execute(args);

                finish();
            }
        });
    }

    private class DwollaCustomerCreationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            DwollaUtil util = new DwollaUtil();

            String customerId = null;
            try {
                customerId = util.createCustomer(args[0], args[1], args[2], args[3],
                                                 args[4], args[5], args[6], args[7],
                                                 args[8], args[9]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Save to Firebase
            if (null != customerId) {
                FirebaseUtils firebaseUtils = new FirebaseUtils();
                firebaseUtils.createdDwollaCustomer(customerId);
            }

            return customerId;
        }

        @Override
        protected void onPostExecute(String customerId) {
            if (null == customerId) {
                Toast.makeText(getApplicationContext(), "Unable to enable payments at this time", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Payment account created successfully", Toast.LENGTH_LONG).show();
            }
        }

    }

}
