package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FatcatEvent {

    private String mName;
    private String mDescription;
    private Date mDate;
    private String mStartTime;
    private String mEndTime;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // Default Constructor for Firebase to create instances on reads
    public FatcatEvent() {
        mName = "NoName";
        mDescription = "";
        mDate = Calendar.getInstance().getTime();
        mStartTime = "";
        mEndTime = "";
    }

    public FatcatEvent(String name, String description, Date date, String startTime, String endTime) {
        mName = name;
        mDescription = description;
        mDate = date;
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public void setName(String name) {
        mName = name;
    }
    public void setDate(String dateString) {
        try {
            mDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            mDate = null;
        }
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
    }


    public String getName() {
        return mName;
    }



    public String getDate() {
        return dateFormat.format(mDate);
    }

    public String getDescription() {
        return mDescription;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getEndTime() {
        return mEndTime;
    }

}
