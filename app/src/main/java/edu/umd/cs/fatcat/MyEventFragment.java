package edu.umd.cs.fatcat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FatcatListener;
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
                    showDetailsOfEvent(item);
                }
            }, this);
            recyclerView.setAdapter(mAdapter);
        }

        mList = view.findViewById(R.id.event_list);
        // Add a divider between each item to make it look nice
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mList.getContext(), DividerItemDecoration.VERTICAL);
        mList.addItemDecoration(dividerItemDecoration);
        setupListener();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
    }



    private void showDetailsOfEvent(FatcatEvent event) {
        Intent i = new Intent(getActivity(), ViewEventActivity.class);
        i.putExtra("event_id", event.getEventID()); // Pass on the event_id, so it knows which one to display
        startActivity(i);
    }
    public void updateList() {
        // Update the adapter and list.
        mList.setAdapter(mAdapter = new MyEventRecyclerViewAdapter(MainActivity.globals.myEvents, new OnListFragmentInteractionListener() {
            @Override
            public void onListFragmentInteraction(FatcatEvent item) {
                //showInvitationDialog(item);
                showDetailsOfEvent(item);
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

        //Toast.makeText(getContext(), "Attached EventFragment", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setupListener() {
        DatabaseReference info = FirebaseDatabase.getInstance().getReference().child("events");
        info.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MainActivity.globals.getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
                    @Override
                    public void onReturnData(Vector<FatcatEvent> data) {
                        updateList();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
