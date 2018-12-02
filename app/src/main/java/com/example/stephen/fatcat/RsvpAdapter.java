package com.example.stephen.fatcat;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatInvitation;

import java.util.ArrayList;

public class RsvpAdapter extends RecyclerView.Adapter<RsvpAdapter.ViewHolder> {
    private ArrayList<FatcatFriend> participants;
    private FatcatEvent event;
    private Context context;

    public RsvpAdapter(ArrayList<FatcatFriend> participants, FatcatEvent event, Context context) {
        this.participants = participants;
        this.event = event;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_rsvp, parent, false);
        return new RsvpAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mFriend = participants.get(position);
        holder.mUsername.setText(holder.mFriend.getUsername());
        if (holder.mFriend.getProfilePicture() != null) {
            holder.mImage.setImageBitmap(holder.mFriend.getProfilePicture());
        } else {
            holder.mImage.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.baseline_person_black_24dp));
        }
        int status = 0;
        for (String uid : event.participants.keySet()) {
            if (uid.equals(holder.mFriend.getUID())) {
                status = event.participants.get(uid);
                break;
            }
        }
        switch(status) {
            case FatcatInvitation.ACCEPTED:
                holder.mStatus.setText("Going!");
                holder.mStatus.setTextColor(Color.BLUE);
                break;
            case FatcatInvitation.DECLINED:
                holder.mStatus.setText("Not Going");
                holder.mStatus.setTextColor(Color.RED);
                break;
            case FatcatInvitation.PENDING:
                holder.mStatus.setText("Pending...");
                holder.mStatus.setTextColor(Color.MAGENTA);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public ImageView mImage;
        public TextView mStatus;
        public TextView mUsername;
        public FatcatFriend mFriend;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mUsername = view.findViewById(R.id.rsvp_username);
            mStatus = view.findViewById(R.id.rsvp_status);
            mImage = view.findViewById(R.id.rsvp_profile);
        }
    }
}
