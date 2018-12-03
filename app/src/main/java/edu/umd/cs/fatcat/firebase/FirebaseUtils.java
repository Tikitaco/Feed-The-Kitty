package edu.umd.cs.fatcat.firebase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import edu.umd.cs.fatcat.MainActivity;
import edu.umd.cs.fatcat.SingleItem;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import edu.umd.cs.fatcat.MainActivity;

public class FirebaseUtils {

    public static String TAG = "FirebaseUtils";


    /**
     * Utility method to link a user's profile to the event as its owner
     * @param uuid the unique id of the event
     */
    private static void addEventToProfile(String uuid) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference profileInformation = mdb.child("profiles").child(uid);
        profileInformation.child("my_events").child(uuid).setValue(true);
    }

    /**
     * Deletes a specific event
     * @param event_uuid
     * @param listener
     */
    public static void deleteEvent(String event_uuid, final FatcatDeletionListener listener) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        mdb.child("events").child(event_uuid).removeValue(new DatabaseReference.CompletionListener() { // First, delete the event from the master event list
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    listener.onFinishDeletion(true);
                } else {
                    listener.onFinishDeletion(false);
                }
            }
        });
    }

    public static void inviteFriendToEvent(FatcatEvent event, FatcatFriend friend) {
        DatabaseReference events = FirebaseDatabase.getInstance().getReference("events");
        // Add this user to the participants list of the event
        events.child(event.getEventID()).child("participants").child(friend.getUID()).setValue(FatcatInvitation.PENDING);
        // Add the invitation to the profile to let the user know they have an event
        DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.child(friend.getUID()).child("invites").child(event.getEventID()).setValue(FatcatInvitation.PENDING);
    }

    /**
     * Gets the information of a single event specified by event_id
     * @param event_id The unique id of the event
     * @param listener used for callback once the information is retrieved
     */
    public static void getEventInformation(String event_id, final FatcatListener<FatcatEvent> listener) {
        DatabaseReference events = FirebaseDatabase.getInstance().getReference("events");
        events.child(event_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FatcatEvent event = dataSnapshot.getValue(FatcatEvent.class);
                if (event != null) {

                    if (dataSnapshot.hasChild("participants")) {
                        for (DataSnapshot participant : dataSnapshot.child("participants").getChildren()) {
                            String user = participant.getKey();
                            int status = Integer.parseInt(participant.getValue().toString());
                            event.participants.put(user, status);
                        }
                    }

                    if (dataSnapshot.hasChild("items")) {
                        for (DataSnapshot item : dataSnapshot.child("items").getChildren()) {
                            int index = Integer.parseInt(item.getKey());
                            SingleItem newItem = new SingleItem();
                            newItem.setItemName(item.child("item_name").getValue().toString());
                            if (item.hasChild("payer_id")) {
                                newItem.setPayerName(item.child("payer_id").getValue().toString());
                            } else {
                                newItem.setPayerName("Not yet paid for");
                            }
                            if (item.hasChild("price")) {
                                newItem.setPrice(Double.parseDouble(item.child("price").getValue().toString()));
                            }
                            newItem.indexInDatabase = index;
                            event.getList().add(newItem);

                        }
                    }
                    if (event != null && dataSnapshot.getKey() != null) {
                        event.setEventID(dataSnapshot.getKey());
                    }
                    listener.onReturnData(event);
                } else {
                    listener.onReturnData(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void removeInvite(String event_id) {
        DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("invites").child(event_id).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "Removed event from invites");
            }
        });
    }

    public static void rsvp_event(FatcatEvent event, final int status, final FatcatListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String event_uid = event.getEventID();
        final DatabaseReference myProfile = FirebaseDatabase.getInstance().getReference("profiles").child(uid);
        DatabaseReference theEvent = FirebaseDatabase.getInstance().getReference("events").child(event_uid);
        myProfile.child("invites").child(event_uid).setValue(status);
        theEvent.child("participants").child(uid).setValue(status, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                myProfile.child("invites").child(event_uid).setValue(status, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        listener.onReturnData(null);
                    }
                });
            }
        });

    }

    public static void deleteInvitation(FatcatInvitation invitation, final FatcatDeletionListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String event_uid = invitation.getEvent().getEventID();
        final DatabaseReference myProfile = FirebaseDatabase.getInstance().getReference("profiles").child(uid);
        DatabaseReference theEvent = FirebaseDatabase.getInstance().getReference("events").child(event_uid);
        theEvent.child("participants").child(uid).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                myProfile.child("invites").child(event_uid).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        listener.onFinishDeletion(true);
                    }
                });
            }
        });
    }

    public static void getMyInvites(final FatcatListener<Map<String, Integer>> listener) {
        DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profiles.child(uid).child("invites").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> invites = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    invites.put(snapshot.getKey(), snapshot.getValue(Integer.class));
                }
                listener.onReturnData(invites);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /**
     * Retrieves event information of all events the user is hosting
     * @param listener used for callback once the information is retrieved
     */
    public static void getAllMyEvents(final FatcatListener<Vector<FatcatEvent>> listener) {
        final Vector<FatcatEvent> events = new Vector<>();
        final DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profiles.child(uid).child("my_events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> event_ids = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    event_ids.add(snapshot.getKey()); // Add event UUID to arrayList
                }
                final int[] counter = {0};
                final int size = event_ids.size();
                synchronized(counter) { // Prevent concurrency errors
                    if (size > 0) {
                        for (final String event_id : event_ids) {
                            getEventInformation(event_id, new FatcatListener<FatcatEvent>() {
                                @Override
                                public void onReturnData(FatcatEvent data) {
                                    if (data != null) { // If the event exists, add it
                                        if (data.getName() == null) {
                                        }
                                        events.add(data);
                                    } else {
                                        Log.i("Utils", "Deleted Event detected");
                                        profiles.child(uid).child("my_events").child(event_id).removeValue(); // Remove hanging event

                                    }
                                    counter[0]++;
                                    if (counter[0] == size) {
                                        listener.onReturnData(events);
                                        return;
                                    }
                                }
                            });
                        }
                    } else {
                        listener.onReturnData(events); // If it's empty, just return.
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void addNewItemToEvent(FatcatEvent event, SingleItem item) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("events").child(event.getEventID());
        eventRef.child("items").child(String.valueOf(item.indexInDatabase)).child("item_name").setValue(item.getItemName());
        eventRef.child("items").child(String.valueOf(item.indexInDatabase)).child("price").setValue(item.getPrice());
        eventRef.child("items").child(String.valueOf(item.indexInDatabase)).child("payer_id").setValue(item.getPayerName());

    }

    public static void paidForItem(FatcatEvent event, SingleItem item) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("events").child(event.getEventID());
        eventRef.child("items").child(String.valueOf(item.indexInDatabase)).child("payer_id").setValue(MainActivity.globals.myProfile.getUsername());
    }

    public static void unpaidForItem(FatcatEvent event, SingleItem item) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference().child("events").child(event.getEventID());
        eventRef.child("items").child(String.valueOf(item.indexInDatabase)).child("payer_id").setValue("Not yet paid for");
    }

    // Dwolla related methods
    public static void createdDwollaCustomer(String customerId) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference().child("profiles").child(MainActivity.globals.myProfile.getUID());
        mdb.child("dwolla_id").setValue(customerId);
    }
    public static void addedFundingSource(String fundSourceId, String name) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference().child("profiles").child(MainActivity.globals.myProfile.getUID()).child("funding_sources");
        mdb.child(fundSourceId).setValue(name);
    }
    public static void sentPayment(String transferId, String amount, String receiverUID) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference().child("profiles");

        // Sender
        mdb.child(MainActivity.globals.myProfile.getUID()).child("transfers").child(transferId).setValue("-" + amount);
        // Receiver
        mdb.child(receiverUID).child("transfers").child(transferId).setValue("+" + amount);
    }


    /**
     * Uploads a New Event and adds the user as its owner
     * @param evt The event object being uploaded
     * @param listener The listener that is called when the upload is finished
     */
    public static void uploadNewEvent(FatcatEvent evt, DatabaseReference.CompletionListener listener) {
        String new_event_id = UUID.randomUUID().toString().replaceAll("-", ""); // Generate a random UUID to identify the event by.
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        addEventToProfile(new_event_id);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Used to tell users apart
        evt.setOwnerUID(uid);
        if (listener == null) {
            mdb.child("events").child(new_event_id).setValue(evt);
        } else {
            mdb.child("events").child(new_event_id).setValue(evt, listener);
        }
        // Add items of the event if there are any
        if (evt.getList().size() > 0) {
            Log.i("Utils", "Added items");
            for (int i = 0; i < evt.getList().size(); i++) {

                SingleItem item = evt.getList().get(i);
                DatabaseReference newItem = mdb.child("events").child(new_event_id).child("items").child(Integer.toString(i));
                newItem.child("item_name").setValue(item.getItemName());
                newItem.child("price").setValue(item.getPrice());
                Log.i("Utils", "Made New Item in DB");
                if (item.getPayerName() != null) {
                    newItem.child("payer_id").setValue(item.getPayerName());
                }
            }
        }
    }

    /**
     * Utility method to check is a username is valid before uploading
     * @param username The username being checked
     * @return true if the username is valid, false otherwise.
     */
    private static boolean isValidUsername(String username) {
        // TODO Add more username limits
        if (username.length() == 0) {
            return false;
        }
        return true;
    }

    /**
     * Adds a friend to the user's friends list by inputting their email
     * @param friendEmail The email of the user you want to friend
     * @param listener The Database listener that's called once the upload is complete
     */
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

    /**
     * Will change the username of the user currently logged in
     * @param newUsername The new username
     * @return False is the username is not valid
     */
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

    /**
     * Retrieves a user's profile information from their email. The FatcatFriend object is returned in
     * the listener callback
     * @param email The email of the user you are getting information from
     * @param listener Returns the FatcatFriend object once the query is finished
     */
    public static void getUserProfileByEmail(String email, final FatcatListener<FatcatFriend> listener) {
        // First, search for the user with that email
        final DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String friendUID = data.getKey();
                    getUserProfile(friendUID, listener);
                    break; // We only need the first element of this iterable
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    listener.onReturnData(null);
                }
    }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onReturnData(null);
                return;
            }
        });
    }

    /**
     * Retrieves a user's profile information from their UID. The FatcatFriend object is returned in
     * the listener callback
     * @param uid The uid of the user you are getting information from
     * @param listener Returns the FatcatFriend object once the query is finished
     */
    public static void getUserProfile(String uid, final FatcatListener<FatcatFriend> listener) {
        DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.orderByKey().equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final FatcatFriend profile = new FatcatFriend();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String username = snapshot.child("username").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String uid = snapshot.getKey();
                    profile.setUsername(username);
                    profile.setUID(uid);
                    profile.setEmail(email);
                    if (snapshot.hasChild("invites")) {
                        for (DataSnapshot invite : snapshot.child("invites").getChildren()) {
                            profile.invites.put(invite.getKey(), invite.getValue(Integer.class));
                        }
                    }

                    if (snapshot.hasChild("dwolla_id")) {
                        profile.customerId = snapshot.child("dwolla_id").getValue().toString();
                    }

                    if (snapshot.hasChild("funding_sources")) {
                        for (DataSnapshot source : snapshot.child("funding_sources").getChildren()) {
                            profile.fundingSources.put(source.getKey(), source.getValue().toString());
                        }
                    }

                    if (snapshot.hasChild("transfers")) {
                        for (DataSnapshot source : snapshot.child("transfers").getChildren()) {
                            profile.transfers.put(source.getKey(), source.getValue().toString());
                        }
                    }

                    getProfilePicture(uid, new FatcatListener<Bitmap>() {
                        @Override
                        public void onReturnData(Bitmap data) {
                            if (data != null) {
                                profile.setProfilePicture(data);
                                listener.onReturnData(profile);
                                return;
                            } else {
                                listener.onReturnData(profile);
                                return;
                            }
                        }
                    });
                    return; // Stop after a single iteration, only one should be equal to the UID
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This static class is used to download profile images without interfering with any other threads / causing errors
     */
    private static class DownloadImagesTask extends AsyncTask<URL, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(URL... urls) {
            return download_Image(urls[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
        }

        private Bitmap download_Image(URL url) {
            Bitmap bm = null;
            try {
                URLConnection conn = url.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (IOException e) {
                Log.e("Utils", "Error getting the image from server : " + e.getMessage().toString());
            }
            return bm;
        }
    }

    /**
     * Uploads a Bitmap image into the database as the profile picture of the user logged in.
     * @param image The bitmap of the image being uploaded
     * @param listener The listener that can be used for callbacks once it's finished
     */
        public static void uploadProfilePicture(Bitmap image, OnCompleteListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fat-cat-784a2.appspot.com");
        StorageReference location = reference.child(uid + ".jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask task = location.putBytes(data);
        if (listener != null) {
            task.addOnCompleteListener(listener);
        }
    }

    /**
     * Method to retrieve the profile picture of a specific user
     * @param uid
     */
    public static void getProfilePicture(final String uid, final FatcatListener<Bitmap> listener) {
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://fat-cat-784a2.appspot.com");
        StorageReference location = reference.child(uid + ".jpg");
        location.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    URL url = new URL(uri.toString());
                    DownloadImagesTask task = new DownloadImagesTask() {
                        @Override
                        protected void onPostExecute(Bitmap result) {
                            listener.onReturnData(result);
                        }
                    };
                    task.execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onReturnData(null); // Return null if there is no image to be found or if retreival failed
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                listener.onReturnData(null);
            }
        });
    }

    /**
     * Removes a friend from the friendlist of the user that's logged in
     * @param friendUID The UID identifier of the friend being removed
     */
    public static void removeFriend(String friendUID) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference profileInformation = mdb.child("profiles").child(uid);
        profileInformation.child("friends").child(friendUID).removeValue();
    }

    /**
     * This will do an update of the user's profile on the database. This is the method
     * that will create a profile if non-exist.
     * @param user The user currently logged in
     */
    public static void updateProfile(FirebaseUser user, final FatcatListener listener) {
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
                    if (data.getKey().equals("username")) {
                        hasUsername = true;
                    }
                }

                if (!hasUsername) {
                    profileInformation.child("username").setValue("New User");
                }
                listener.onReturnData(null);
                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getDetails());
            }
        });
    }
}
