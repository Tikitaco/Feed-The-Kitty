package com.example.stephen.fatcat;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

                DwollaUtil util = new DwollaUtil();

                String customerId = null;
                try {
                    customerId = util.createCustomer(user.getEmail(),
                                                            firstName.getText().toString(),
                                                            lastName.getText().toString(),
                                                            address1.getText().toString(),
                                                            address2.getText().toString() == "" ? null : address2.getText().toString(),
                                                            city.getText().toString(),
                                                            state.getSelectedItem().toString(),
                                                            zip.getText().toString(),
                                                            dateFormatter.format(calendar.getTime()),
                                                            ssn.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (null == customerId) {
                    Toast.makeText(getApplicationContext(), "Unable to enable payments at this time", Toast.LENGTH_LONG).show();
                } else {
                    FirebaseUtils firebaseUtils = new FirebaseUtils();
                    firebaseUtils.createdDwollaCustomer(customerId);
                }

                finish();
            }
        });
    }

}
