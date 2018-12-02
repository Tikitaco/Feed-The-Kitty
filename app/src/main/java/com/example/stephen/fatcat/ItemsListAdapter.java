package com.example.stephen.fatcat;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.Button;


public class ItemsListAdapter extends BaseAdapter {

    private final List<SingleItem> mItems = new ArrayList<>();
    private final Context mContext;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    public ItemsListAdapter(Context context) {

        mContext = context;

    }

    // Add a SingleItem to the adapter
    // Notify observers that the data set has changed

    public void add(SingleItem item) {

        mItems.add(item);
        notifyDataSetChanged();

    }

    // Clears the list adapter of all items.

    public void clear() {

        mItems.clear();
        notifyDataSetChanged();

    }

    // Returns the number of SingleItem

    @Override
    public int getCount() {

        return mItems.size();

    }

    // Retrieve the number of SingleItem

    @Override
    public Object getItem(int pos) {

        return mItems.get(pos);

    }

    // Get the ID for the SingleItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {

        return pos;

    }


    public void remove(int pos) {
        mItems.remove(pos);
        notifyDataSetChanged();
    }

    public List<SingleItem> getList() {
        return mItems;
    }

    // Create a View for the SingleItem at specified position
    // Remember to check whether convertView holds an already allocated View
    // before created a new View.
    // Consider using the ViewHolder pattern to make scrolling more efficient
    // See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dataView = convertView;
        // create layout and mItem
        final SingleItem mItem = mItems.get(position);
        dataView = inflater.inflate(R.layout.single_list_item, null);
        // create title of item
        final TextView titleItemView = (TextView) dataView.findViewById(R.id.titleItemView);
        titleItemView.setText(mItem.getItemName());
        // create price
        final TextView priceView = (TextView) dataView.findViewById(R.id.price_view);
        Double value = mItem.getPrice();
        if (value != 0.0) {
            priceView.setText(df2.format(mItem.getPrice()));
        } else {
            priceView.setText("0.00");
        }
        // create "paid for"
        final TextView payerView = (TextView) dataView.findViewById(R.id.paid_for_view);
        payerView.setText(mItem.getPayerName());

        // create status
        final CheckBox statusView = (CheckBox) dataView.findViewById(R.id.statusCheckBox);
        statusView.setChecked(!mItem.getPayerName().equals("Not yet paid for"));
        // set onChangeListener for status
        statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {

               if(checked) {
                   String username = MainActivity.globals.myProfile.getUsername();

                   mItem.setPayerName(username);
                   payerView.setText("Paid for by " + username);
                } else {
                   mItem.setPayerName("Not yet paid for");
                   payerView.setText("Not yet paid for");
                }
            }
        });
        return dataView;
    }

}



