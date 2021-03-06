
package edu.umd.cs.fatcat;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.umd.cs.fatcat.MyInvitationsListFragmentFragment.OnListFragmentInteractionListener;
import edu.umd.cs.fatcat.firebase.FatcatInvitation;

import java.util.Vector;

/**
 * {@link RecyclerView.Adapter} that can display a {@link .} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyInvitationsListFragmentRecyclerViewAdapter extends RecyclerView.Adapter<MyInvitationsListFragmentRecyclerViewAdapter.ViewHolder> {

    private final Vector<FatcatInvitation> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final MyInvitationsListFragmentFragment mFragment;

    public MyInvitationsListFragmentRecyclerViewAdapter(Vector<FatcatInvitation> items, MyInvitationsListFragmentFragment fragment, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_myinvitationslistfragment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        final FatcatInvitation invite = holder.mItem;
        holder.mName.setText(invite.getEvent().getName());
        holder.mDate.setText(invite.getEvent().getDate());
        if (invite.getStatus() == FatcatInvitation.ACCEPTED) {
            holder.mStatus.setText("Going!");
            holder.mStatus.setTextColor(Color.BLUE);
        } else if (invite.getStatus() == FatcatInvitation.PENDING) {
            holder.mStatus.setText("Pending...");
            holder.mStatus.setTextColor(Color.MAGENTA);
        } else if (invite.getStatus() == FatcatInvitation.DECLINED) {
            holder.mStatus.setText("Not Going");
            holder.mStatus.setTextColor(Color.RED);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(invite);
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
        public final TextView mName;
        public final TextView mDate;
        public final TextView mStatus;
        public FatcatInvitation mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mStatus = view.findViewById(R.id.invitation_status);
            mName = view.findViewById(R.id.invitation_list_event_name);
            mDate = view.findViewById(R.id.invitation_list_event_date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mName.getText() + "'";
        }
    }
}
