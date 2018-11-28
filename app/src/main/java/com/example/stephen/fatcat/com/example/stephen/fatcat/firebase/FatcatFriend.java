package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

public class FatcatFriend {
    public String mUsername;
    public String mUid;
    public String mEmail;

    // Empty constructor for firebase instances
    public FatcatFriend() {

    }

    public FatcatFriend(String username) {
        mUsername = username;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setUID(String uid) {
        mUid = uid;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getUID() {
        return mUid;
    }

    public String getUsername() {
        return mUsername;
    }
}
