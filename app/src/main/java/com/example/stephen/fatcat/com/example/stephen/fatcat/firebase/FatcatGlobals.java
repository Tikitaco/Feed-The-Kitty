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
    public Vector<FatcatEvent> myEvents = new Vector<>();


    /**
     * Load friends at the beginning of the app, so we don't have to continually grab them.
     */
    public void initializeGlobals(final FatcatListener listener) {
        getMyProfile(null); // Get your profile information from the database
        getFriends(null); // Get all of your friend's information from the database
        getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
            @Override
            public void onReturnData(Vector<FatcatEvent> data) {
                listener.onReturnData(null);
            }
        });
    }

    public void getMyEvents(final FatcatListener<Vector<FatcatEvent>> listener) {
        FirebaseUtils.getAllMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
            @Override
            public void onReturnData(Vector<FatcatEvent> data) {
                myEvents = new Vector<>(data); // Copy all event data into local vector
                Log.i("Utils", "Events: " + myEvents.size());
                for (FatcatEvent event : myEvents) {
                    Log.i("Utils", "Event Name: " + event.getName());
                }
                if (listener != null) {
                    listener.onReturnData(myEvents);
                }
            }
        });
    }

    public void getMyProfile(final FatcatListener<FatcatFriend> listener) {
        FirebaseUtils.getUserProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FatcatListener<FatcatFriend>() {
            @Override
            public void onReturnData(FatcatFriend data) {
                myProfile = new FatcatFriend(data);
                if (listener != null) {
                    listener.onReturnData(myProfile);
                }
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
                        FirebaseUtils.getUserProfile(uid, new FatcatListener<FatcatFriend>() {
                            @Override
                            public void onReturnData(FatcatFriend data) {
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
                listener.onReturnData(null);
            }
        });
    }
}
