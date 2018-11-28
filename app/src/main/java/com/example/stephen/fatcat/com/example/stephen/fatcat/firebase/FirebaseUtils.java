package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.stephen.fatcat.MainActivity;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class FirebaseUtils {

    public static void uploadNewEvent(FatcatEvent evt) {
       uploadNewEvent(evt, null);
    }
    public static String TAG = "FirebaseUtils";


    public static void uploadNewEvent(FatcatEvent evt, DatabaseReference.CompletionListener listener) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Used to tell users apart
        if (listener == null) {
            mdb.child(uid).child("events").child(evt.getName()).setValue(evt);
        } else {
            mdb.child(uid).child("events").child(evt.getName()).setValue(evt, listener);
        }
    }

    private static boolean isValidUsername(String username) {
        // TODO Add more username limits
        if (username.length() == 0) {
            return false;
        }
        return true;
    }

    public static void addFriend(String friendEmail, final DatabaseReference.CompletionListener listener) {
        // First, search for the user with that email
        final DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.orderByChild("email").equalTo(friendEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String friendUID = data.getKey();
                    DatabaseReference myProfileInformation = profiles.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if (listener == null) {
                        myProfileInformation.child("friends").child(friendUID).setValue(true);
                    } else {
                        myProfileInformation.child("friends").child(friendUID).setValue(true, listener);
                    }
                    break; // Stop after the first one.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static boolean updateUsername(String newUsername) {
        if (isValidUsername(newUsername)) {
            DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference profileInformation = mdb.child("profiles").child(uid);
            profileInformation.child("username").setValue(newUsername);
            return true;
        } else {
            return false;
        }
    }

    public static void updateProfile(FirebaseUser user) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = user.getUid();
        final DatabaseReference profileInformation = mdb.child("profiles").child(uid);
        if (user.getEmail() != null) {
            profileInformation.child("email").setValue(user.getEmail());
        }
        profileInformation.child("last-login").setValue(Calendar.getInstance().getTime().toString()); // Update the last login
        // Check for username. If it doesn't exist, assign a default one:
        profileInformation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean hasUsername = false;
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.i(TAG, data.getKey());
                    if (data.getKey().equals("username")) {
                        hasUsername = true;
                    }
                }
                if (!hasUsername) {
                    profileInformation.child("username").setValue("New User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getDetails());
            }
        });
    }



}
