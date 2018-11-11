package com.example.stephen.fatcat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.Date;

public class CreateEventActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    // 7 days in milliseconds - 7 * 24 * 60 * 60 * 1000
    private static final int SEVEN_DAYS = 604800000;

    private Button mDateButton;
    private Button mStartTimeButton;
    private Button mEndTimeButton;
    private Button mSubmitEvent;
    private Date mDate;
    private static TextView dateView;
    private static TextView startTimeView;
    private static TextView endTimeView;
    private static String dateString;
    private static String timeString;
    private static boolean setEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        final TextView mEventName;
        final TextView mDescription;
        final EditText mEnterEventName;
        final EditText mEnterDescription;

        mEventName = (TextView) findViewById(R.id.EventName);
        mEnterEventName = (EditText) findViewById(R.id.EnterEventName);
        mDescription = (TextView) findViewById(R.id.Description);
        mEnterDescription = (EditText) findViewById(R.id.EnterEventDescription);


        dateView = (TextView) findViewById(R.id.Date);
        startTimeView = (TextView) findViewById(R.id.StartTime);
        endTimeView = (TextView) findViewById(R.id.EndTime);

        final Button datePickerButton = (Button) findViewById(R.id.chooseDate);
        datePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        final Button timePickerButton = (Button) findViewById(R.id.chooseStartTime);
        timePickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        final Button timePickerButton2 = (Button) findViewById(R.id.chooseEndTime);
        timePickerButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setEnd = true;
                showTimePickerDialog();
            }
        });

        final Button submit = (Button) findViewById(R.id.createEventSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEnterEventName.getText().toString();
                String description = mEnterDescription.getText().toString();
                String startTime = startTimeView.getText().toString();
                String endTime = endTimeView.getText().toString();
                mDate = Calendar.getInstance().getTime(); // TODO This is only until the date picker is fully implemented
                FatcatEvent event = new FatcatEvent(name, description, mDate, startTime, endTime);

                FirebaseUtils.uploadNewEvent(event, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) // If the upload was successful...
                        {
                            Toast.makeText(CreateEventActivity.this, "Event Created Succesfully", Toast.LENGTH_SHORT).show();
                        } else { // If the upload failed...
                            Toast.makeText(CreateEventActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    private static void setTimeString(int hourOfDay, int minute) {
        String min = "" + minute;
        String amPm = "am";

        if (hourOfDay >= 13) {
            hourOfDay -= 12;
            amPm = "pm";
        }
        if (minute < 10)
            min = "0" + minute;

        String hour = "" + hourOfDay;

        timeString = hour + ":" + min + amPm;
    }

    private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

        // Increment monthOfYear for Calendar/Date -> Time Format setting
        monthOfYear++;
        String mon;
        String day = "" + dayOfMonth;

        switch(monthOfYear) {
            case 1:
                mon = "January";
                break;
            case 2:
                mon = "February";
                break;
            case 3:
                mon = "March";
                break;
            case 4:
                mon = "April";
                break;
            case 5:
                mon = "May";
                break;
            case 6:
                mon = "June";
                break;
            case 7:
                mon = "July";
                break;
            case 8:
                mon = "August";
                break;
            case 9:
                mon = "September";
                break;
            case 10:
                mon = "October";
                break;
            case 11:
                mon = "November";
                break;
            default:
                mon = "December";
                break;


        }
        dateString = mon + " " + day + ", " + year;
    }

    // DialogFragment used to pick a deadline date

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the picker

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            setDateString(year, monthOfYear, dayOfMonth);

            dateView.setText(dateString);
        }

    }

    // DialogFragment used to pick a ToDoItem deadline time

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setTimeString(hourOfDay, minute);

            if (setEnd) {
                endTimeView.setText(timeString);
                setEnd = false;
            } else {
                startTimeView.setText(timeString);
            }
        }
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }


    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.nav_account:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CreateEventActivity.this, MainActivity.class));
                finish();
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
