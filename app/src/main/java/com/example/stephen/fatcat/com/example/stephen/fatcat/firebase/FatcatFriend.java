package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

public class FatcatFriend {
    public String mUsername;

    // Empty constructor for firebase instances
    public FatcatFriend() {

    }

    public FatcatFriend(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }
}
