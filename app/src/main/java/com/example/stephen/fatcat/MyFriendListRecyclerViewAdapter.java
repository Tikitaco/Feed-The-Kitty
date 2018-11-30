package com.example.stephen.fatcat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stephen.fatcat.FriendListFragment.OnListFragmentInteractionListener;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FatcatFriend} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFriendListRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendListRecyclerViewAdapter.ViewHolder> {

    private final List<FatcatFriend> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext; // This context is used to load the default image of the profile pictures

    public MyFriendListRecyclerViewAdapter(List<FatcatFriend> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friendlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        FatcatFriend friend = holder.mItem;
        holder.mContentView.setText(friend.getUsername() + " (" + friend.getEmail() + ")");
        if (friend.getProfilePicture() != null) {
            holder.mProfilePicture.setImageBitmap(friend.getProfilePicture());
        } else {
            Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.baseline_person_black_24dp);
            Log.i("Utils", "Set Defautl Bitmap");
            holder.mProfilePicture.setImageBitmap(bm);
        }
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
        public final TextView mContentView;
        public final ImageView mProfilePicture;

        public FatcatFriend mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
            mProfilePicture = (ImageView) view.findViewById(R.id.list_profile_image);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
