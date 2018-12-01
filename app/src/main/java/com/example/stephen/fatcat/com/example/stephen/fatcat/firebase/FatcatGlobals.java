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
import java.util.Map;
import java.util.Vector;

public class FatcatGlobals {
    public FatcatFriend myProfile;
    public Vector<FatcatFriend> friendProfiles = new Vector<>();
    public Vector<FatcatEvent> myEvents = new Vector<>();
    public Vector<FatcatInvitation> myInvitations = new Vector<>();
    /**
     * Load friends at the beginning of the app, so we don't have to continually grab them.
     */
    public void initializeGlobals(final FatcatListener listener) {
        friendProfiles.clear();
        myEvents.clear();
        myInvitations.clear();
        final boolean profileDone[] = {false};
        final boolean friendsDone[] = {false};
        final boolean eventsDone[] = {false};
        synchronized (profileDone) {
            synchronized (friendsDone) {
                synchronized (eventsDone) {
                    getMyProfile(new FatcatListener<FatcatFriend>() {
                        @Override
                        public void onReturnData(FatcatFriend data) {
                            getInvitations(new FatcatListener<Vector<FatcatInvitation>>() {
                                @Override
                                public void onReturnData(Vector<FatcatInvitation> data) {
                                    profileDone[0] = true;
                                    Log.i("Utils", "Finished getting profile");
                                    if (profileDone[0] && friendsDone[0] && eventsDone[0]) {
                                        listener.onReturnData(null);
                                        return;
                                    }
                                }
                            });
                        }
                    }); // Get your profile information from the database
                    getFriends(new FatcatListener<Vector<FatcatFriend>>() {
                        @Override
                        public void onReturnData(Vector<FatcatFriend> data) {
                            Log.i("Utils", "Finished getting friends");
                            friendsDone[0] = true;
                            if (profileDone[0] && friendsDone[0] && eventsDone[0]) {
                                listener.onReturnData(null);
                                return;
                            }
                        }
                    }); // Get all of your friend's information from the database
                    getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
                        @Override
                        public void onReturnData(Vector<FatcatEvent> data) {
                            Log.i("Utils", "Finished getting events");
                            eventsDone[0] = true;
                            if (profileDone[0] && friendsDone[0] && eventsDone[0]) {
                                listener.onReturnData(null);
                                return;
                            }
                        }
                    });
                }
            }
        }
    }


    /**
     *  Retrieves all the events that the user is hosting
     * @param listener is used as a callback for when the events are retrieved
     */
    public void getMyEvents(final FatcatListener<Vector<FatcatEvent>> listener) {
        FirebaseUtils.getAllMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
            @Override
            public void onReturnData(Vector<FatcatEvent> data) {
                myEvents = new Vector<>(data); // Copy all event data into local vector
                if (listener != null) {
                    listener.onReturnData(myEvents);
                    return;
                }
            }
        });
    }

    /**
     * Retrieves the user's profile information
     * @param listener used for callback once the information is retrieved
     */
    public void getMyProfile(final FatcatListener<FatcatFriend> listener) {
        FirebaseUtils.getUserProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FatcatListener<FatcatFriend>() {
            @Override
            public void onReturnData(FatcatFriend data) {
                myProfile = new FatcatFriend(data);
                if (listener != null) {
                    listener.onReturnData(myProfile);
                    return;
                }
            }
        });
    }

    public void updateMyInvites(final FatcatListener listener) {
        FirebaseUtils.getMyInvites(new FatcatListener<Map<String, Integer>>() {
            @Override
            public void onReturnData(Map<String, Integer> data) {
                myProfile.invites.clear();
                myProfile.invites.putAll(data);
                listener.onReturnData(null);
            }
        });
    }

    public void getInvitations(final FatcatListener<Vector<FatcatInvitation>> listener) {
        myInvitations.clear();
        updateMyInvites(new FatcatListener() {
            @Override
            public void onReturnData(Object data) {
                final int counter[] = {0};
                final int number_events = myProfile.invites.size();
                Log.i("Utils", "Getting " + number_events + " invitations");
                for (final String event_id : myProfile.invites.keySet()) {
                    final int status = myProfile.invites.get(event_id);
                    FirebaseUtils.getEventInformation(event_id, new FatcatListener<FatcatEvent>() {
                        @Override
                        public void onReturnData(FatcatEvent data) {
                            if (data != null) {
                                myInvitations.add(new FatcatInvitation(data, status));
                            } else {
                                FirebaseUtils.removeInvite(event_id);
                            }
                            counter[0]++;
                            if (counter[0] == number_events) {
                                listener.onReturnData(myInvitations);
                                return;
                            }
                        }
                    });
                }
                // Just return if there are no invites
                listener.onReturnData(myInvitations);
                return;
            }
        });

    }

    /**
     * Retrieves the profile information of everyone on the user's friends list
     * @param listener used for callback once information is retrieved
     */
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
                                    return;
                                }
                            }
                        });
                    }
                }
                // If no friends, just return
                if (listener != null) {
                    listener.onReturnData(friendProfiles);
                    return;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onReturnData(null);
            }
        });
    }
}
