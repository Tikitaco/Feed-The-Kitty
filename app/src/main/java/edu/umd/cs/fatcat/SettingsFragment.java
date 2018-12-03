package edu.umd.cs.fatcat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import edu.umd.cs.fatcat.firebase.FatcatFriend;
import edu.umd.cs.fatcat.firebase.FatcatListener;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditText mUsername;
    private Button mSaveChanges;
    private ImageView mProfilePicture;
    public static final int PICK_IMAGE = 1000;
    private Bitmap newPicture = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void uploadProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUsername = getView().findViewById(R.id.settingsUsername);
        mProfilePicture = getView().findViewById(R.id.settingsPicture);
        mSaveChanges = getView().findViewById(R.id.settingsSaveButton);

        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfilePicture();
            }
        });
        updateSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    private void saveSettings() {
        FirebaseUtils.updateUsername(mUsername.getText().toString());
        if (newPicture != null) {
            final ProgressDialog dialog = ProgressDialog.show(getActivity(), "Uploading your new profile picture...",
                    "Loading. Please wait...", true);
            dialog.show();
            FirebaseUtils.uploadProfilePicture(newPicture, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    dialog.dismiss();
                }
            });
        }
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();
        MainActivity.globals.getMyProfile(new FatcatListener<FatcatFriend>() {
            @Override
            public void onReturnData(FatcatFriend data) { // Wait until we're done to update the settings page...
                updateSettings();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void updateSettings() {
        // First, get the user profile information
        /*FirebaseUtils.getUserProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FatcatListener<FatcatFriend>() {
            @Override
            public void onReturnData(FatcatFriend data) {
                Log.i("Utils", data.getUsername());
                mUsername.setText(data.getUsername());
                if (data.getProfilePicture() != null) {
                    mProfilePicture.setImageBitmap(data.getProfilePicture());
                }
            }
        }); */
        FatcatFriend myProfile = MainActivity.globals.myProfile;
        Log.i("Utils", "" + (myProfile == null));
        Log.i("Utils", "" + (mUsername == null));
        mUsername.setText(myProfile.getUsername());
        if (myProfile.getProfilePicture() != null) {
            mProfilePicture.setImageBitmap(myProfile.getProfilePicture());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                Log.e("Utils", "Error opening picture");
                return;
            }
            Log.i("Utils", "Got Image!!");
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                newPicture = BitmapFactory.decodeStream(inputStream);
                mProfilePicture.setImageBitmap(newPicture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
