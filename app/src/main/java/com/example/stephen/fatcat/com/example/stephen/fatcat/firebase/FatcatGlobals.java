package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Vector;

public class FatcatGlobals {
    public FatcatFriend myProfile;
    public Vector<FatcatFriend> friendProfiles = new Vector<>();


    /**
     * Load friends at the beginning of the app, so we don't have to continually grab them.
     */
    public void initializeGlobals() {
        getMyProfile();
        getFriends(null);
    }

    public void getMyProfile() {
        FirebaseUtils.getUserProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FatcatListener<FatcatFriend>() {
            @Override
            public void onReturnData(FatcatFriend data) {
                Log.i("Utils", "Hello World");
                Log.i("Utils", data.getUsername());
                myProfile = new FatcatFriend(data);
            }
        });
    }

    public void getFriends(final FatcatListener<Vector<FatcatFriend>> listener) {
        friendProfiles.clear();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> friendUIDs = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    friendUIDs.add(snapshot.getKey());
                }
                final int size = friendUIDs.size();
                final int[] counter = {0}; // Counts the number loaded (so we can tell when we're done)
                synchronized (counter) { // Ensure we don't run into concurrency issues
                    for (String uid : friendUIDs) {
                        Log.i("Utils", "Adding uid: " + uid);
                        FirebaseUtils.getUserProfile(uid, new FatcatListener<FatcatFriend>() {
                            @Override
                            public void onReturnData(FatcatFriend data) {
                                Log.i("Utils", "Adding friend: " + data.getEmail());
                                friendProfiles.add(data);
                                counter[0]++;
                                if (listener != null && counter[0] == size) {
                                    listener.onReturnData(friendProfiles);
                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
