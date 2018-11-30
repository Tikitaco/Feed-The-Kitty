package com.example.stephen.fatcat;

import android.os.Parcel;
import android.os.Parcelable;

import com.applandeo.materialcalendarview.EventDay;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class MyEventDay extends EventDay implements Parcelable,Comparable<MyEventDay> {
    private String mNote;
    private Date mTime;


    MyEventDay(Calendar day, int imageResource, String note,Date time) {
        super(day, imageResource);
        mNote = note;
        mTime = time;
    }

    String getNote() {
        return mNote;
    }

    private MyEventDay(Parcel in) {
        super((Calendar) in.readSerializable(), in.readInt());
        mNote = in.readString();
    }

    public static final Creator<MyEventDay> CREATOR = new Creator<MyEventDay>() {
        @Override
        public MyEventDay createFromParcel(Parcel in) {
            return new MyEventDay(in);
        }

        @Override
        public MyEventDay[] newArray(int size) {
            return new MyEventDay[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(getCalendar());
        parcel.writeString(mNote);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int compareTo(MyEventDay o ){
       Date A = this.getCalendar().getTime();
       Date B = o.getCalendar().getTime();
       if(A.before(B))return -1;
       if(B.before(A))return 1;
       if(A.equals(B))return 0;
       return 0 ;
       }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof  MyEventDay){
            MyEventDay Obj = (MyEventDay)obj;
            Date A = this.getCalendar().getTime();
            Date B = Obj.getCalendar().getTime();
            if(((MyEventDay) obj).getNote().equals(this.getNote()) && A.equals(B)){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }


    public String toString(){
        Date A = mTime;
       String B =  new SimpleDateFormat("hh:mm:ss").format(A);
        return this.getNote() + " " + B;


    }
    public Date getTime(){
        return mTime ;
    }




}