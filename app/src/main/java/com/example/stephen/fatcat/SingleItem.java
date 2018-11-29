package com.example.stephen.fatcat;

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

        /*public SingleItem(Intent intent) {
            mItemName = intent.getStringExtra(ITEMNAME);
            mPrice =
                    intent.getDoubleExtra(PAYERNAME);

        }*/

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