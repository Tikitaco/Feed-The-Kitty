package com.example.stephen.fatcat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stephen.fatcat.MyEventFragment.OnListFragmentInteractionListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatDeletionListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FirebaseUtils;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Vector;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FatcatEvent} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyEventRecyclerViewAdapter extends RecyclerView.Adapter<MyEventRecyclerViewAdapter.ViewHolder> {

    private final List<FatcatEvent> mValues;
    private final OnListFragmentInteractionListener mListener;
    private MyEventFragment mFragment;

    public MyEventRecyclerViewAdapter(List<FatcatEvent> items, OnListFragmentInteractionListener listener, MyEventFragment fragment) {
        mValues = items;
        mListener = listener;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_fatcatevent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mEventName.setText(mValues.get(position).getName());
        holder.mEventDate.setText(mValues.get(position).getDate());
        if (holder.mItem.participants.size() > 0) {
            int invites = holder.mItem.participants.size();
            int accepts = holder.mItem.getNumberOfPeopleGoing();
            holder.mPeopleGoing.setText(accepts + "/" + invites + " Going");
        }
        //holder.mContentView.setText(mValues.get(position).content);
        /*holder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUtils.deleteEvent(holder.mItem.getEventID(), new FatcatDeletionListener() {
                    @Override
                    public void onFinishDeletion(boolean success) {
                        if (success) {
                            MainActivity.globals.getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
                                @Override
                                public void onReturnData(Vector<FatcatEvent> data) {
                                    mFragment.updateList();
                                }
                            });
                        }
                    }
                });
            }
        }); */
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mEventName;
        public final TextView mEventDate;
        public final TextView mPeopleGoing;
        public FatcatEvent mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEventName = (TextView) view.findViewById(R.id.list_event_name);
            mEventDate = (TextView) view.findViewById(R.id.list_event_date);
            mPeopleGoing = (TextView) view.findViewById(R.id.going_number);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mEventDate.getText() + "'";
        }
    }
}
