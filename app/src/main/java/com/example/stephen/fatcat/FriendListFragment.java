package com.example.stephen.fatcat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatGlobals;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FriendListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mList;
    private FloatingActionButton mAddFriendButton;
    //private ArrayList<FatcatFriend> friends = new ArrayList<>();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FriendListFragment newInstance(int columnCount) {
        FriendListFragment fragment = new FriendListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void updateFriendsList() {
        mList.getAdapter().notifyDataSetChanged();
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Friend");

        // Set up the input
        final EditText input = new EditText(getActivity());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Email of Friend");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String email = input.getText().toString();
                final ProgressDialog loadingDialog = ProgressDialog.show(getActivity(), "Adding friend...",
                        "Loading. Please wait...", true);
                loadingDialog.show();
                FirebaseUtils.getUserProfileByEmail(email, new FatcatListener<FatcatFriend>() {
                    @Override
                    public void onReturnData(FatcatFriend data) {
                        loadingDialog.dismiss();
                        if (data == null) { // If nothing returned, say user does not exist
                            Toast.makeText(FriendListFragment.this.getContext(), "User does not exist", Toast.LENGTH_SHORT).show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            // Get the layout inflater
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View informationView = inflater.inflate(R.layout.popup_add_friend, null);
                            TextView username = informationView.findViewById(R.id.add_username);
                            ImageView image = informationView.findViewById(R.id.add_image);
                            Button accept = informationView.findViewById(R.id.add_button);
                            Button cancel = informationView.findViewById(R.id.cancel_button);
                            builder.setView(informationView);
                            username.setText(data.getUsername());
                            if (data.getProfilePicture() != null) {
                                image.setImageBitmap(data.getProfilePicture());
                            }
                            final AlertDialog finalAddDialog = builder.show();
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finalAddDialog.dismiss();
                                }
                            });
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final ProgressDialog loadingDialog = ProgressDialog.show(getActivity(), "Adding friend...",
                                            "Loading. Please wait...", true);
                                    loadingDialog.show();
                                    FirebaseUtils.addFriend(email, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            MainActivity.globals.getFriends(new FatcatListener<Vector<FatcatFriend>>() { // Update the global friends list and update the UI when done
                                                @Override
                                                public void onReturnData(Vector<FatcatFriend> data) {
                                                    updateFriendsList();
                                                    loadingDialog.dismiss();
                                                    finalAddDialog.dismiss();
                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showFriendPopup(final FatcatFriend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View informationView = inflater.inflate(R.layout.popup_friend_information, null);
        TextView friendUsername = informationView.findViewById(R.id.popup_name);
        ImageView friendProfilePicture = informationView.findViewById(R.id.popup_profile_picture);
        Button removeFriendButton = informationView.findViewById(R.id.popup_remove_friend);
        if (friend.getProfilePicture() != null) {
            friendProfilePicture.setImageBitmap(friend.getProfilePicture());
        }
        friendUsername.setText(friend.getUsername() + " (" + friend.getEmail() + ")");
        builder.setView(informationView);
        final AlertDialog dialog = builder.show();

        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.removeFriend(friend.getUID());
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Removing Friend...",
                        "Loading. Please wait...", true);
                progressDialog.show();
                MainActivity.globals.getFriends(new FatcatListener<Vector<FatcatFriend>>() {
                    @Override
                    public void onReturnData(Vector<FatcatFriend> data) {
                        updateFriendsList();
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Removed friend successfully", Toast.LENGTH_SHORT).show();

                    }
                });
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mList = getView().findViewById(R.id.list);
        mAddFriendButton = getView().findViewById(R.id.addFriendButton);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFriendDialog();
            }
        });
        MyFriendListRecyclerViewAdapter adapter = new MyFriendListRecyclerViewAdapter(MainActivity.globals.friendProfiles, new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(FatcatFriend friend) {
                Log.i("Utils", friend.getUsername());
                showFriendPopup(friend);
            }
        });
        adapter.setContext(getContext());
        mList.setAdapter(adapter);
        // Add a divider between each item to make it look nice
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        mList.addItemDecoration(dividerItemDecoration);
        Log.i("Utils", "Updated Friends List");
        updateFriendsList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendlist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyFriendListRecyclerViewAdapter(MainActivity.globals.friendProfiles, mListener));
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(FatcatFriend friend);
    }
}
