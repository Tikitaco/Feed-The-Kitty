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

    public static void getUserProfileByEmail(String email, final FatcatListener<FatcatFriend> friend) {
        // First, search for the user with that email
        final DatabaseReference profiles = FirebaseDatabase.getInstance().getReference("profiles");
        profiles.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String friendUID = data.getKey();
                    getUserProfile(friendUID, friend);
                    break; // We only need the first element of this iterable
                }
    }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                friend.onReturnData(null);
            }
        });
    }
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
                        break; // Stop after a single iteration, only one should be equal
                    }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void uploadProfilePicture(Bitmap image) {
        uploadProfilePicture(image, null);
    }

    public static class DownloadImagesTask extends AsyncTask<URL, Void, Bitmap> {

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
        Log.i("Utils", "Getting Profile picture for " + uid);
        StorageReference location = reference.child(uid + ".jpg");
        location.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    URL url = new URL(uri.toString());
                    Log.i("Utils", " URL Success from " + uid);
                    DownloadImagesTask task = new DownloadImagesTask() {
                        @Override
                        protected void onPostExecute(Bitmap result) {
                            listener.onReturnData(result);
                        }
                    };
                    task.execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.i("Utils", "Failed here");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Utils", "Failure from " + uid);
                listener.onReturnData(null); // Return null if there is no image to be found or if retreival failed
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                listener.onReturnData(null);
            }
        });
    }

    public static void removeFriend(String friendUID) {
        DatabaseReference mdb = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference profileInformation = mdb.child("profiles").child(uid);
        profileInformation.child("friends").child(friendUID).removeValue();
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
