package com.example.stephen.fatcat;

import android.util.Log;

public class SingleItem {
    private String mItemName;
    private double mPrice;
    private String mPayerName;
    public int indexInDatabase = -1;

    public SingleItem() {
        mItemName = "NoItemName";
        mPrice = 0.00;
        mPayerName = "Not yet paid for";
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

    public boolean hasBeenPaidFor() {
        Log.i("Utils", getPayerName());
        return getPayerName().equals("Not yet paid for");
    }

    public void setPayerName(String name) {
        mPayerName = name;
    }
}