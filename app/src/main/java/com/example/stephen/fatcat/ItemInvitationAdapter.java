package com.example.stephen.fatcat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;

import java.text.NumberFormat;
import java.util.List;

public class ItemInvitationAdapter extends RecyclerView.Adapter<ItemInvitationAdapter.ViewHolder>{

    private List<SingleItem> mItems;

    public ItemInvitationAdapter(List<SingleItem> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_invitation_item, parent, false);
        return new ItemInvitationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mItem = mItems.get(position);
        SingleItem item = holder.mItem;
        holder.mName.setText(item.getItemName());
        holder.mPrice.setText("(" + NumberFormat.getCurrencyInstance().format(item.getPrice()) + ")");
        if (!holder.mItem.hasBeenPaidFor()) {
            holder.mPayForItem.setClickable(false);
            holder.mPayForItem.setText("Paid for by " + holder.mItem.getPayerName());
            holder.mPayForItem.setBackgroundTintList(ColorStateList.valueOf((Color.WHITE)));
        }
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public SingleItem mItem;
        final TextView mName;
        final TextView mPrice;
        final CheckBox mPayForItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = view.findViewById(R.id.item_name);
            mPrice = view.findViewById(R.id.item_price);
            mPayForItem = view.findViewById(R.id.pay_for_item);
        }
    }
}
