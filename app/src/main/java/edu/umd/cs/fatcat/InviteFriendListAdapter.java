package edu.umd.cs.fatcat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import edu.umd.cs.fatcat.firebase.FatcatFriend;

import java.util.List;

public class InviteFriendListAdapter extends RecyclerView.Adapter<InviteFriendListAdapter.ViewHolder> {

    private final List<FatcatFriend> mValues;
    private Context mContext;

    public InviteFriendListAdapter(List<FatcatFriend> items, Context context) {
        mValues = items;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend_invite_item, parent, false);
        return new InviteFriendListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        final FatcatFriend profile = holder.mItem;
        holder.mName.setText(profile.getUsername());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                holder.mAdd.toggle();
                if (holder.mAdd.isChecked()) {
                    holder.mView.setBackgroundColor(Color.CYAN);
                } else {
                    holder.mView.setBackgroundColor(0xFAFAFA);
                }
            }
        });
        if (profile.getProfilePicture() != null) {
            holder.mImage.setImageBitmap(profile.getProfilePicture());
        } else {
            holder.mImage.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.baseline_person_black_24dp));
        }
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public FatcatFriend mItem;
        public final TextView mName;
        public final CheckBox mAdd;
        public final ImageView mImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = view.findViewById(R.id.invite_friend_textview);
            mAdd = view.findViewById(R.id.invite_checkbox);
            mImage = view.findViewById(R.id.invite_picture);

        }
    }
}
