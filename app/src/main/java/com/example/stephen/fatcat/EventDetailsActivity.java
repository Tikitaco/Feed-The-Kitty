package com.example.stephen.fatcat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.content.Intent;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatDeletionListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatInvitation;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class EventDetailsActivity extends ListActivity {

    private Button mDateButton;
    private Button mStartTimeButton;
    private Button mEndTimeButton;
    private Button mSubmitEvent;
    private Button mDeleteButton;
    private static Date mDate;
    private static TextView dateView;
    private static TextView startTimeView;
    private static TextView endTimeView;
    private static String dateString;
    private static String timeString;
    private Button inviteFriendsButton;
    private static boolean setEnd = false;
    private Context mContext = this;

    ItemsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {  //TODO Contribution options, Firebase integration, Dwolla integration
        super.onCreate(savedInstanceState);

        //TODO get event using intent and replace new FatcatEvent with the actual event
        Intent intent = getIntent();
        String event_id = intent.getExtras().get("event_id").toString();

        FatcatEvent findEvent = null;
        for (FatcatEvent fe : MainActivity.globals.myEvents) {
            if (fe.getEventID().equals(event_id)) {
                findEvent = fe;
                break;
            }
        }
        if (findEvent == null) {
            Toast.makeText(this, "Couldn't find event details", Toast.LENGTH_SHORT).show();
        }
        final FatcatEvent event = findEvent;
        mAdapter = new ItemsListAdapter(getApplicationContext(), event.getList());

        View v = (View) getLayoutInflater().inflate(R.layout.activity_event_details, null);
        getListView().setBackgroundColor(Color.WHITE);
        getListView().addHeaderView(v);
        setListAdapter(mAdapter);

        final TextView mEventName;
        final TextView mDescription;

        mEventName = (TextView) v.findViewById(R.id.NameOfEvent);
        mDescription = (TextView) v.findViewById(R.id.Description);

        dateView = (TextView) v.findViewById(R.id.Date);
        startTimeView = (TextView) v.findViewById(R.id.StartTime);
        endTimeView = (TextView) v.findViewById(R.id.EndTime);

        inviteFriendsButton = (Button) v.findViewById(R.id.btn_invite);
        mDeleteButton = (Button) v.findViewById(R.id.btn_delete);

        mEventName.setText(event.getName());
        mDescription.setText(event.getDescription());
        dateView.setText(event.getDate());
        startTimeView.setText(event.getStartTime());
        endTimeView.setText(event.getEndTime());

        inviteFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInvitationDialog(event);
            }
        });

        View footerView = getLayoutInflater().inflate(R.layout.single_list_footer_view_without_create, null);
        getListView().addFooterView(footerView);

        final ViewGroup root = (ViewGroup) getWindow().getDecorView().getRootView(); // Blur?

        Button addAnotherItem = (Button) findViewById(R.id.addAnotherItemView);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                FirebaseUtils.deleteEvent(event.getEventID(), new FatcatDeletionListener() {
                                    @Override
                                    public void onFinishDeletion(boolean success) {
                                        if (success) {
                                            MainActivity.globals.getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
                                                @Override
                                                public void onReturnData(Vector<FatcatEvent> data) {
                                                    finish();
                                                }
                                            });
                                        }
                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.this);
                builder.setMessage("Are you sure you want to delete this event?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
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
                addItemPriceEdit.addTextChangedListener(new EventDetailsActivity.MoneyTextWatcher(addItemPriceEdit));

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
                            Toast.makeText(EventDetailsActivity.this,  "Item name or price not entered",Toast.LENGTH_LONG).show();
                        } else {
                            String value = priceStr.substring(1);
                            Double priceDouble = Double.valueOf(value);

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
                if (MainActivity.globals.myProfile.getUID() == event.getOwnerUID()) {
                    showDatePickerDialog();
                } else {
                    Toast.makeText(mContext, "Only event creator can edit this", Toast.LENGTH_LONG).show();
                }
            }
        });

        mStartTimeButton = (Button) findViewById(R.id.chooseStartTime);
        mStartTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (MainActivity.globals.myProfile.getUID() == event.getOwnerUID()) {
                    showTimePickerDialog();
                } else {
                    Toast.makeText(mContext, "Only event creator can edit this", Toast.LENGTH_LONG).show();
                }
            }
        });

        mEndTimeButton = (Button) findViewById(R.id.chooseEndTime);
        mEndTimeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (MainActivity.globals.myProfile.getUID() == event.getOwnerUID()) {
                    setEnd = true;
                    showTimePickerDialog();
                } else {
                    Toast.makeText(mContext, "Only event creator can edit this", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void showInvitationDialog(final FatcatEvent eventClicked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View informationView = inflater.inflate(R.layout.popup_invite_list, null);
        final RecyclerView list = informationView.findViewById(R.id.invitation_list);
        Button inviteButton = informationView.findViewById(R.id.invite_add_button);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new InviteFriendListAdapter(MainActivity.globals.friendProfiles, this));
        // Add a divider between each item to make it look nice
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        list.addItemDecoration(dividerItemDecoration);
        builder.setView(informationView);
        final AlertDialog dialog = builder.show();
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<FatcatFriend> invites = new ArrayList<>();
                for (int i = 0; i < list.getAdapter().getItemCount(); i++) {
                    InviteFriendListAdapter.ViewHolder holder = (InviteFriendListAdapter.ViewHolder) list.findViewHolderForAdapterPosition(i);
                    if (holder.mAdd.isChecked()) {
                        invites.add(holder.mItem);
                    }
                }
                for (FatcatFriend friend : invites) {
                    FirebaseUtils.inviteFriendToEvent(eventClicked, friend);
                }
                dialog.dismiss();

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
        DialogFragment newFragment = new CreateEventActivity.DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        DialogFragment newFragment = new CreateEventActivity.TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }
}
