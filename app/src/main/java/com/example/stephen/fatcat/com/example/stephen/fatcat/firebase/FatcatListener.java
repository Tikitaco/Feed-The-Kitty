package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;


/**
 * This is a simple utility interface to streamline quueries to the firebase database
 * @param <T>
 */
public interface FatcatListener<T> {
    public void onReturnData(T data);
}
