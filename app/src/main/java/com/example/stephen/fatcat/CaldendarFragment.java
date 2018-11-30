package com.example.stephen.fatcat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatGlobals;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CaldendarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CaldendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CaldendarFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private com.applandeo.materialcalendarview.CalendarView mCalendarView;
    private List<EventDay> mEventDays = new ArrayList<>();
    ArrayList<MyEventDay> currentday = new ArrayList<>();
    String print = "";


    private List<FatcatEvent> fatcatEvents;
    DatabaseReference databaseReference;
    FirebaseUser user;
    EventDay event;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public CaldendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.`
     * @return A new instance of fragment CaldendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CaldendarFragment newInstance(String param1, String param2) {
        CaldendarFragment fragment = new CaldendarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fatcatEvents = new ArrayList<>();



        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_caldendar, container, false);
        mCalendarView = v.findViewById(R.id.datePicker);
        int counter = 0;
        for(FatcatEvent f: MainActivity.globals.myEvents){
            Calendar day = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date convertedDate = new Date();
            try {
                convertedDate = dateFormat.parse(f.getDate());
            }catch (ParseException e){
                e.printStackTrace();
            }
            day.setTime(convertedDate);

            MyEventDay myEventDay = new MyEventDay(day,R.drawable.com_facebook_profile_picture_blank_portrait,f.getName(), convertedDate);
            //adds array list
            if(!mEventDays.contains(myEventDay)){
                mEventDays.add(myEventDay);
            }
            counter++;

        }
        //arraylist is being added to thet calednar view
        mCalendarView.setEvents(mEventDays);
        Toast.makeText(getActivity(),String.valueOf(counter),Toast.LENGTH_LONG).show();

        mCalendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                TextView calendarOutput = getView().findViewById(R.id.noteEditText);
                currentday.clear();
                print = "";
                if(eventDay instanceof  MyEventDay){
                       MyEventDay myEventDay = (MyEventDay)eventDay;
                       //get time from the event search for others with same time
                       if(currentday.isEmpty() == false)currentday.clear();
                       print = "";
                       for(EventDay a: mEventDays){
                           MyEventDay myEventDay1 = (MyEventDay)a;
                           if(myEventDay.getCalendar().getTime().getDay() == myEventDay1.getCalendar().getTime().getDay()){
                             currentday.add(myEventDay1);
                           }
                       }

                    Collections.sort(currentday);
                   }
                calendarOutput.setText(null);

                for(MyEventDay A: currentday){
                     print +=   A.toString() + '\n';
                   }


                calendarOutput.setText(print);
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override

    //https://applandeo.com/blog/material-calendar-view-customized-calendar-widget-android/
   public void onStart() {
        super.onStart();

        }

        @Override
    public void onResume(){
        super.onResume();
        print = "";

    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
