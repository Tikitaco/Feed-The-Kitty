package com.example.stephen.fatcat.com.example.stephen.fatcat.firebase;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.stephen.fatcat.MainActivity;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.Vector;

import static java.util.UUID.randomUUID;

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

    public static void getEventInformation(String event_id, final FatcatListener<FatcatEvent> listener) {
        DatabaseReference events = FirebaseDatabase.getInstance().getReference("events");
        events.child(event_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FatcatEvent event = dataSnapshot.getValue(FatcatEvent.class);
                listener.onReturnData(event);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getAllMyEvents(final FatcatListener<Vector<FatcatEvent>> listener) {
        final Vector<FatcatEvent> events = new Vector<>();
        DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        profiles.child(uid).child("my_events").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<String> event_ids = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Log.i(TAG, snapshot.getKey());
                    event_ids.add(snapshot.getKey()); // Add event UUID to arrayList
                }
                final int[] counter = {0};
                final int size = event_ids.size();
                synchronized(counter) { // Prevent concurrency errors
                    for (String event_id : event_ids) {
                        getEventInformation(event_id, new FatcatListener<FatcatEvent>() {
                            @Override
                            public void onReturnData(FatcatEvent data) {
                                if (data != null) { // If the event exists, add it
                                    events.add(data);
                                }
                                counter[0]++;
                                if (counter[0] == size) {
                                    listener.onReturnData(events);
                                }
                            }
                        });
                    }

                    listener.onReturnData(events); // If it's empty, just return.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
    }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onReturnData(null);
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
                        getProfilePicture(uid, new FatcatListener<Bitmap>() {
                            @Override
                            public void onReturnData(Bitmap data) {
                                if (data != null) {
                                    profile.setProfilePicture(data);
                                    listener.onReturnData(profile);
                                } else {
                                    listener.onReturnData(profile);
                                }
                            }
                        });
                        break; // Stop after a single iteration, only one should be equal to the UID
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
            //---------------------------------------------------
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
