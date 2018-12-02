package com.example.stephen.fatcat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatDeletionListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatInvitation;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Vector;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MyInvitationsListFragmentFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private RecyclerView mList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyInvitationsListFragmentFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MyInvitationsListFragmentFragment newInstance() {
        MyInvitationsListFragmentFragment fragment = new MyInvitationsListFragmentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void onClickInvite(final FatcatInvitation invite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View informationView = inflater.inflate(R.layout.activity_invitation_details, null);

        FatcatFriend host = null;
        for (FatcatFriend friend : MainActivity.globals.friendProfiles) {
            if (friend.getUID().equals(invite.getEvent().getOwnerUID())) {
                host = friend;
                break;
            }
        }

        if (host == null) {
            FirebaseUtils.getUserProfile(invite.getEvent().getOwnerUID(), new FatcatListener<FatcatFriend>() {
                @Override
                public void onReturnData(FatcatFriend data) {
                    Toast.makeText(getActivity(), "You must be friends with the user at " + data.getEmail() + " to accept the invitation", Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        TextView mEventName = (TextView) informationView.findViewById(R.id.invitation_event_name);
        TextView mHost = (TextView) informationView.findViewById(R.id.invite_host);
        TextView mStart = (TextView) informationView.findViewById(R.id.invite_start);
        TextView mEnd = (TextView) informationView.findViewById(R.id.invite_end);
        TextView mDescription = (TextView) informationView.findViewById(R.id.invite_description);
        TextView mDate = (TextView) informationView.findViewById(R.id.invite_date);
        TextView mItemText = (TextView) informationView.findViewById(R.id.invite_item_request_text);
        final RecyclerView itemList = (RecyclerView) informationView.findViewById(R.id.requested_items_list);
        final RadioButton goingButton = informationView.findViewById(R.id.radio_going);
        final RadioButton declineButton = informationView.findViewById(R.id.radio_decline);
        final RadioButton pendingButton = informationView.findViewById(R.id.radio_pending);
        Button confirmButton = informationView.findViewById(R.id.btn_invitation_confirm);
        Button deleteButton = informationView.findViewById(R.id.btn_invite_delete);
        mEventName.setText(invite.getEvent().getName());
        mHost.setText("Hosted by " + host.getUsername());
        mStart.setText("Start Time: " + invite.getEvent().getStartTime());
        mEnd.setText("End Time: " + invite.getEvent().getEndTime());
        mDate.setText("Date: " + invite.getEvent().getDate());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        itemList.addItemDecoration(dividerItemDecoration);
        Log.i("Utils", String.valueOf(invite.getEvent().getList().size()));
        itemList.setAdapter(new ItemInvitationAdapter(invite.getEvent().getList()));
        itemList.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (invite.getEvent().getDescription().length() > 0) {
            mDescription.setText("Description: " + invite.getEvent().getDescription());
        } else {
            mDescription.setText("");
        }

        if (invite.getEvent().getList().size() == 0) {
            mItemText.setText(""); // Set the label to blank if there are no items
        }
        int status = invite.getStatus();

        if (status == FatcatInvitation.ACCEPTED) {
            goingButton.toggle();
            goingButton.setBackgroundColor(Color.GREEN);
        } else if (status == FatcatInvitation.DECLINED) {
            declineButton.toggle();
            declineButton.setBackgroundColor(Color.RED);
            declineButton.setTextColor(Color.WHITE);
        } else if (status == FatcatInvitation.PENDING) {
            pendingButton.toggle();
            pendingButton.setBackgroundColor(Color.YELLOW);
        }

        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pendingButton.isChecked()) {
                    pendingButton.setBackgroundColor(Color.YELLOW);
                    goingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.BLACK);

                }
            }
        });
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (declineButton.isChecked()) {
                    declineButton.setBackgroundColor(Color.RED);
                    pendingButton.setBackgroundColor(0xFAFAFA);
                    goingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.WHITE);
                }
            }
        });
        goingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goingButton.isChecked()) {
                    goingButton.setBackgroundColor(Color.GREEN);
                    pendingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.BLACK);

                }
            }
        });
        builder.setView(informationView);
        final AlertDialog dialog = builder.show();
        final FatcatFriend finalHost = host;
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newStatus = -1;
                if (goingButton.isChecked()) {
                    newStatus = FatcatInvitation.ACCEPTED;
                } else if (pendingButton.isChecked()) {
                    newStatus = FatcatInvitation.PENDING;
                } else if (declineButton.isChecked()) {
                    newStatus = FatcatInvitation.DECLINED;
                }
                final ArrayList<SingleItem> payingFor = new ArrayList<>(); // List of all items the user is paying for
                // Check each checkbox to see if the user offered to pay for anything
                for (int i = 0; i < itemList.getAdapter().getItemCount(); i++) {
                    ItemInvitationAdapter.ViewHolder holder = (ItemInvitationAdapter.ViewHolder) itemList.findViewHolderForAdapterPosition(i);
                    if (holder.mPayForItem.isChecked()) {
                        payingFor.add(holder.mItem);
                    }
                }
                if (payingFor.size() > 0) { // If paying for any money
                    double total = 0;
                    for (SingleItem item : payingFor) {
                        total += item.getPrice();
                    }

                    final int finalNewStatus = newStatus;
                    final double finalTotal = total;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    double amount_to_pay = finalTotal; // The amount the user has to send
                                    boolean success = true; // TODO set this to calling Pay API
                                    if (success) { // If successful, Update database with payment and RSVP
                                        for (SingleItem item : payingFor) {
                                            FirebaseUtils.paidForItem(invite.getEvent(), item); // Update who paid for it in backend
                                        }
                                        FirebaseUtils.rsvp_event(invite.getEvent(), finalNewStatus, new FatcatListener() {
                                            @Override
                                            public void onReturnData(Object data) {
                                                MainActivity.globals.getInvitations(new FatcatListener() {
                                                    @Override
                                                    public void onReturnData(Object data) {
                                                        updateList();
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Pay " + NumberFormat.getCurrencyInstance().format(total) + " to " + finalHost.getUsername() + " for items?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                } else {
                    FirebaseUtils.rsvp_event(invite.getEvent(), newStatus, new FatcatListener() {
                        @Override
                        public void onReturnData(Object data) {
                            updateList();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                FirebaseUtils.deleteInvitation(invite, new FatcatDeletionListener() {
                                    @Override
                                    public void onFinishDeletion(boolean success) {
                                        MainActivity.globals.getInvitations(new FatcatListener<Vector<FatcatInvitation>>() {
                                            @Override
                                        public void onReturnData(Vector<FatcatInvitation> data) {
                                                updateList();
                                                dialog.dismiss();
                                            }
                                        });

                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to delete this invitation?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
       // Intent intent = new Intent(getActivity(), InvitationDetailsActivity.class);
       // intent.putExtra("event_id", invite.getEvent().getEventID());
       // getActivity().startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myinvitationslistfragment_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            mList = recyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new MyInvitationsListFragmentRecyclerViewAdapter(MainActivity.globals.myInvitations, this, new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(FatcatInvitation item) {
                    onClickInvite(item);
                }
            }));
            Log.i("Utils", "Invites: " + MainActivity.globals.myInvitations.size());
        }

        setupListener();
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

    /**
     * Updates the list when there's been a change in invitations
     */
    public void updateList() {
        // Update the adapter and list.
        Log.i("Utils", "Size of inv: " + MainActivity.globals.myInvitations.size());

        mList.setAdapter(new MyInvitationsListFragmentRecyclerViewAdapter(MainActivity.globals.myInvitations, this, new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(FatcatInvitation item) {
                onClickInvite(item);
            }
        }));
        mList.getAdapter().notifyDataSetChanged();
    }


    /**
     * Sets up listener for changes in the invitations database
     */
    private void setupListener() {
        DatabaseReference info = FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        info.child("invites").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int number_of_invitations = MainActivity.globals.myInvitations.size();
                Log.i("Utils", "You received an invitation!! (" + number_of_invitations + ")");
                final long new_invitation_number = dataSnapshot.getChildrenCount();
                MainActivity.globals.getInvitations(new FatcatListener<Vector<FatcatInvitation>>() {
                    @Override
                    public void onReturnData(Vector<FatcatInvitation> data) {
                        if (new_invitation_number > number_of_invitations) {
                            Toast.makeText(getContext(), "You received a new invitation!", Toast.LENGTH_SHORT).show();
                            updateList();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        void onListFragmentInteraction(FatcatInvitation item);
    }
}
