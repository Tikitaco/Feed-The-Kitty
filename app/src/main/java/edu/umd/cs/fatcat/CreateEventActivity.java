package edu.umd.cs.fatcat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.view.ViewGroupOverlay;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;


import java.util.Calendar;
import java.util.Date;

import edu.umd.cs.fatcat.firebase.FirebaseUtils;

public class CreateEventActivity extends ListActivity{

    private Button mDateButton;
    private Button mStartTimeButton;
    private Button mEndTimeButton;
    private Button mSubmitEvent;
    private static Date mDate = Calendar.getInstance().getTime();
    private static TextView dateView;
    private static TextView startTimeView;
    private static TextView endTimeView;
    private static String dateString;
    private static String timeString;
    private static boolean setEnd = false;
    private Context mContext = this;

    ItemsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAdapter = new ItemsListAdapter(getApplicationContext());

        View v = (View) getLayoutInflater().inflate(R.layout.activity_create_event, null);
        getListView().setBackgroundColor(Color.WHITE);
        getListView().addHeaderView(v);
        setListAdapter(mAdapter);

        final TextView mEventName;
        final TextView mDescription;
        final EditText mEnterEventName;
        final EditText mEnterDescription;

        mEventName = (TextView) v.findViewById(R.id.EventName);
        mEnterEventName = (EditText) v.findViewById(R.id.EnterEventName);
        mDescription = (TextView) v.findViewById(R.id.Description);
        mEnterDescription = (EditText) v.findViewById(R.id.EnterEventDescription);

        dateView = (TextView) v.findViewById(R.id.Date);
        startTimeView = (TextView) v.findViewById(R.id.StartTime);
        endTimeView = (TextView) v.findViewById(R.id.EndTime);

        View footerView = getLayoutInflater().inflate(R.layout.single_list_footer_view, null);
        getListView().addFooterView(footerView);

        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView(); // Blur?

        Button addAnotherItem = (Button) findViewById(R.id.addAnotherItemView);
        addAnotherItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.single_list_add_item, root, false);

                final TextView addItemTitle = (TextView) popupView.findViewById(R.id.add_item_title);
                final TextView addItemName = (TextView) popupView.findViewById(R.id.item_name_view);
                final EditText addItemNameEdit = (EditText) popupView.findViewById(R.id.item_edit_name_view);
                final TextView addItemPrice = (TextView) popupView.findViewById(R.id.price_view);
                final EditText addItemPriceEdit = (EditText) popupView.findViewById(R.id.price_edit_view);

                applyDim(root, 0.5f);

                final PopupWindow pw = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true); // LayoutParams.WRAP_CONTENT

                pw.showAtLocation(popupView, Gravity.CENTER, 0 ,0); //?? popupView

                addItemPriceEdit.setRawInputType(Configuration.KEYBOARD_12KEY);
                addItemPriceEdit.addTextChangedListener(new MoneyTextWatcher(addItemPriceEdit));

                Button cancel = (Button) popupView.findViewById(R.id.cancel_new_item);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pw.dismiss();
                        clearDim(root);
                    }
                });

                Button submit = (Button) popupView.findViewById(R.id.submit_new_item);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String priceStr = addItemPriceEdit.getText().toString();
                        String itemNameEdit = addItemNameEdit.getText().toString();

                        // Check for null edittext
                        if(priceStr.equals(null) || priceStr.equals("") || itemNameEdit.equals(null) || itemNameEdit.equals(""))
                        {
                            Toast.makeText(CreateEventActivity.this,  "Item name or price not entered",Toast.LENGTH_LONG).show();
                        } else {
                            String value = priceStr.substring(1);
                            NumberFormat format = NumberFormat.getInstance();
                            Number number = null;
                            try {
                                number = format.parse(value);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            double d = number.doubleValue();
                            Double priceDouble = d;

                            SingleItem addItem = new SingleItem(addItemNameEdit.getText().toString(), priceDouble);

                            mAdapter.add(addItem);
                            pw.dismiss();
                            clearDim(root);
                        }
                    }
                });
            }
        });



        mDateButton = (Button) findViewById(R.id.chooseDate);
        mDateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mStartTimeButton = (Button) findViewById(R.id.chooseStartTime);
        mStartTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        mEndTimeButton = (Button) findViewById(R.id.chooseEndTime);
        mEndTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setEnd = true;
                showTimePickerDialog();
            }
        });

        mSubmitEvent = (Button) findViewById(R.id.createEventSubmit);
        mSubmitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mEnterEventName.getText().toString();
                String description = mEnterDescription.getText().toString();
                String startTime = startTimeView.getText().toString();
                String endTime = endTimeView.getText().toString();
                //mDate = Calendar.getInstance().getTime();
                ArrayList<SingleItem> items = (ArrayList) mAdapter.getList();
                FatcatEvent event = new FatcatEvent(name, description, mDate, startTime, endTime, items);
                event.setOwnerUID(MainActivity.globals.myProfile.getUID());

                FirebaseUtils.uploadNewEvent(event, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) // If the upload was successful...
                        {
                            Toast.makeText(CreateEventActivity.this, "Event Created Successfully", Toast.LENGTH_SHORT).show();
                            //closes create event view and returns to event list
                            setResult(RESULT_OK);
                            finishActivity(HomepageActivity.CREATE_ACTIVITY_REQUEST_CODE); // Finish and signify the list fragment to update
                            finish();

                        } else { // If the upload failed...
                            Toast.makeText(CreateEventActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        if(isFinishing()){
            overridePendingTransition(R.anim.stay_still,R.anim.slide_out_bottom);
        }
    }
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.stay_still,R.anim.slide_out_bottom);
    }

    public class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<EditText>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            if (s.isEmpty()) return;
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[$,.]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    public static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    public static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    private static void setTimeString(int hourOfDay, int minute) {
        String min = "" + minute;
        String amPm = "am";

        if (hourOfDay >= 13) {
            hourOfDay -= 12;
            amPm = "pm";
        } else if (hourOfDay == 12) {
            amPm = "pm";
        } else if (hourOfDay == 0) {
            hourOfDay = 12;
        }
        if (minute < 10)
            min = "0" + minute;

        String hour = "" + hourOfDay;

        timeString = hour + ":" + min + amPm;
    }

    private static void setDateString(int year, int monthOfYear, int dayOfMonth) {

        Calendar cal = Calendar.getInstance();
        cal.set(year, monthOfYear, dayOfMonth,0,0,0);
        mDate = cal.getTime();

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
            return new TimePickerDialog(getActivity(), this, hour, minute, false);
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



}
