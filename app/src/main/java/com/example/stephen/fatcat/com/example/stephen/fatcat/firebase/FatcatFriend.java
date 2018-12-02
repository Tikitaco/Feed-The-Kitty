package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class FatcatFriend {
    private String mUsername;
    private String mUid;
    private String mEmail;
    private Bitmap mProfilePicture;
    private ArrayList<FatcatEvent> events = new ArrayList<>();
    public Map<String, Integer> invites = new HashMap<>();
    //Dwolla api information
    String customerId;
    Map<String, String> fundingSources = new HashMap<>();
    Map<String, String> transfers = new HashMap<>();

    // Empty constructor for firebase instances
    public FatcatFriend() {

    }

    /**
     * Create a copy of another profile
     * @param copy
     */
    public FatcatFriend(FatcatFriend copy) {
        mUsername = new String(copy.getUsername());
        mUid = new String(copy.getUID());
        mEmail = new String(copy.getEmail());
        if (copy.getProfilePicture() != null) {
            mProfilePicture = copy.getProfilePicture().copy(copy.getProfilePicture().getConfig(), true);
        }
        invites.putAll(copy.invites);
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

    public void setProfilePicture(Bitmap image) {
        mProfilePicture = image;
    }

    public Bitmap getProfilePicture() {
        return mProfilePicture;
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
