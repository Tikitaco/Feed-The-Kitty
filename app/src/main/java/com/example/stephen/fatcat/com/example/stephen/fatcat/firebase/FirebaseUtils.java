package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    public static void uploadNewEvent(FatcatEvent evt) {
       uploadNewEvent(evt, null);
    }

    public static void uploadNewEvent(FatcatEvent evt, DatabaseReference.CompletionListener listener) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Used to tell users apart
        if (listener == null) {
            mdb.child(uid).child("events").child(evt.getName()).setValue(evt);
        } else {
            mdb.child(uid).child("events").child(evt.getName()).setValue(evt, listener);
        }
    }

    public static void addNewFriend(FatcatFriend friend) {

    }



}
