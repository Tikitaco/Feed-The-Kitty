package com.example.stephen.fatcat;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class MyEventFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private RecyclerView mList;
    private MyEventRecyclerViewAdapter mAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MyEventFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static MyEventFragment newInstance() {
        MyEventFragment fragment = new MyEventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fatcatevent_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) view;
            mAdapter = new MyEventRecyclerViewAdapter(MainActivity.globals.myEvents, new OnListFragmentInteractionListener() {
                @Override
                public void onListFragmentInteraction(FatcatEvent item) {
                    showInvitationDialog(item);
                }
            }, this);
            recyclerView.setAdapter(mAdapter);
        }

        mList = view.findViewById(R.id.event_list);
        // Add a divider between each item to make it look nice
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        mList.addItemDecoration(dividerItemDecoration);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        Toast.makeText(getContext(), "Created MyEventFragment", Toast.LENGTH_SHORT).show();
    }

    private void showInvitationDialog(final FatcatEvent eventClicked) {
        Toast.makeText(getContext(), "Inviting to event", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View informationView = inflater.inflate(R.layout.popup_invite_list, null);
        final RecyclerView list = informationView.findViewById(R.id.invitation_list);
        Button inviteButton = informationView.findViewById(R.id.invite_add_button);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(new InviteFriendListAdapter(MainActivity.globals.friendProfiles));
        // Add a divider between each item to make it look nice
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        list.addItemDecoration(dividerItemDecoration);
        builder.setView(informationView);
        final AlertDialog dialog = builder.show();
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<FatcatFriend> invites = new ArrayList<>();
                for (int i = 0; i < list.getAdapter().getItemCount(); i++) {
                    InviteFriendListAdapter.ViewHolder holder = (InviteFriendListAdapter.ViewHolder) list.findViewHolderForAdapterPosition(i);
                    if (holder.mAdd.isChecked()) {
                        invites.add(holder.mItem);
                    }
                }
                for (FatcatFriend friend : invites) {
                    FirebaseUtils.inviteFriendToEvent(eventClicked, friend);
                }
                dialog.dismiss();

            }
        });

    }

    public void updateList() {
        // Update the adapter and list.
        mList.setAdapter(mAdapter = new MyEventRecyclerViewAdapter(MainActivity.globals.myEvents, new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(FatcatEvent item) {
                showInvitationDialog(item);
            }
        }, this));
        mAdapter.notifyDataSetChanged();
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

        Toast.makeText(getContext(), "Attached EventFragment", Toast.LENGTH_SHORT).show();
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
        void onListFragmentInteraction(FatcatEvent item);
    }
}
