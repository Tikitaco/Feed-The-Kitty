package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;

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
    private String mOwnerUID;
    private ArrayList<SingleItem> items;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public class SingleItem {
        private String mItemName;
        private double mPrice;
        private String mPayerName;

        public SingleItem() {
            mItemName = "NoItemName";
            mPrice = 0.00;
            mPayerName = "NoPayer";
        }

        public SingleItem(String itemName, double price) {
            mItemName = itemName;
            mPrice = price;
            mPayerName = "Not yet paid for";
        }

        public SingleItem(String itemName, double price, String payerName) {
            mItemName = itemName;
            mPrice = price;
            mPayerName = payerName;
        }

        public String getItemName() {
            return mItemName;
        }

        public String getPayerName() {
            return mPayerName;
        }

        public double getPrice() {
            return mPrice;
        }

        public void setItemName(String itemName) {
            mItemName = itemName;
        }

        public void setPrice(Double price) {
            mPrice = price;
        }

        public void setPayerName(String name) {
            mPayerName = name;
        }
    }

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

    public String getOwnerUID() {
        return mOwnerUID;
    }

    public void setOwnerUID(String uid) {
        mOwnerUID = uid;
    }
    
    public void setDescription(String description) {
        mDescription = description;
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

    public ArrayList<SingleItem> getList() { return items;}

}
