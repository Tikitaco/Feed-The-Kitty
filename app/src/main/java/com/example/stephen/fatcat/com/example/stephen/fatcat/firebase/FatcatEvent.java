package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.stephen.fatcat.MainActivity;
import com.example.stephen.fatcat.SingleItem;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FatcatEvent implements Comparable<FatcatEvent>{

    private String mName;
    private String mDescription;
    private Date mDate;
    private String mStartTime;
    private String mEndTime;
    private String mOwnerUID;
    @Exclude
    private ArrayList<SingleItem> items;
    private String eventID;
    public int indexInDatabase = 0;

    @Exclude
    public Map<String, Integer> participants = new HashMap<>();

    public final static String ITEMNAME = "itemname";
    public final static String PAYERNAME = "payername";
    public final static String PRICE = "price";

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    // Default Constructor for Firebase to create instances on reads
    public FatcatEvent() {
        mName = "NoName";
        mDescription = "";
        mDate = Calendar.getInstance().getTime();
        mStartTime = "";
        mEndTime = "";
        items = new ArrayList<>();
    }

    public FatcatEvent(String name, String description, Date date, String startTime, String endTime, ArrayList<SingleItem> itemsList) {
        mName = name;
        mDescription = description;
        mDate = date;
        mStartTime = startTime;
        mEndTime = endTime;
        items = itemsList;
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

    public int getNumberOfPeopleGoing() {
        int number = 0;
        for (String key : participants.keySet()) {
            if (participants.get(key) == FatcatInvitation.ACCEPTED) {
                number++;
            }
        }
        return number;
    }

    public String getOwnerUID() {
        return mOwnerUID;
    }

    public void setOwnerUID(String uid) {
        mOwnerUID = uid;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setEventID(String id) {
        eventID = id;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
    }

    public void setEndTime(String endTime) { mEndTime = endTime; }

    public void addItem(SingleItem item) { items.add(item);}

    public String getName() {
        return mName;
    }

    public String getDate() {

       int day = mDate.getDay();
       int month = mDate.getMonth();
       int year = mDate.getYear() +1900;

        month++;
        String mon;
        String dayString = "" + day;

        switch(month) {
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
        return  mon + " " + dayString + ", " + year;

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

    public String getEventID() {
        return eventID;
    }

    @Exclude
    public ArrayList<SingleItem> getList() { return items;}

    @Override
    public boolean equals(Object other) {
        if (other instanceof FatcatEvent) {
            FatcatEvent event = (FatcatEvent) other;
            return event.getEventID().equals(eventID);
        } else {
            return false;
        }
    }

    @Exclude
    public FatcatFriend getOwner() {
        for (FatcatFriend friend : MainActivity.globals.friendProfiles) {
            if (friend.getUID().equals(getOwnerUID())) {
                return friend;
            }
        }
        return null;
    }

    /**
     * Allow the events list to be sorted by date in the invitations and events list
     * @param event
     * @return
     */
    @Override
    public int compareTo(@NonNull FatcatEvent event) {
        Date now = Calendar.getInstance().getTime();
        try {
            return dateFormat.parse(event.getDate()).compareTo(dateFormat.parse(getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
