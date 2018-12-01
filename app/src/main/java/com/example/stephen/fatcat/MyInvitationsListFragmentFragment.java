package com.example.stephen.fatcat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatInvitation;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    private void onClickInvite(FatcatInvitation invite) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View informationView = inflater.inflate(R.layout.activity_invitation_details, null);
        builder.setView(informationView);
        builder.show();
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
