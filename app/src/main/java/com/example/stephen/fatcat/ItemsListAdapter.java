package com.example.stephen.fatcat;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

import android.content.Context;
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


import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;

public class ItemsListAdapter extends BaseAdapter {

    private final List<FatcatEvent.SingleItem> mItems = new ArrayList<>();
    private final Context mContext;
    private static DecimalFormat df2 = new DecimalFormat(".##");

    public ItemsListAdapter(Context context) {

        mContext = context;

    }

    // Add a ToDoItem to the adapter
    // Notify observers that the data set has changed

    public void add(FatcatEvent.SingleItem item) {

        mItems.add(item);
        notifyDataSetChanged();

    }

    // Clears the list adapter of all items.

    public void clear() {

        mItems.clear();
        notifyDataSetChanged();

    }

    // Returns the number of ToDoItems

    @Override
    public int getCount() {

        return mItems.size();

    }

    // Retrieve the number of ToDoItems

    @Override
    public Object getItem(int pos) {

        return mItems.get(pos);

    }

    // Get the ID for the ToDoItem
    // In this case it's just the position

    @Override
    public long getItemId(int pos) {

        return pos;

    }

    // Create a View for the SingleItem at specified position
    // Remember to check whether convertView holds an already allocated View
    // before created a new View.
    // Consider using the ViewHolder pattern to make scrolling more efficient
    // See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // create layout and mItem
        final FatcatEvent.SingleItem mItem = mItems.get(position);
        RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.single_list_item, parent, false);
        // create title of item
        final TextView titleItemView = (TextView) itemLayout.findViewById(R.id.titleItemView);
        titleItemView.setText(mItem.getItemName());
        // create price
        final TextView priceView = (TextView) itemLayout.findViewById(R.id.price_view);
        priceView.setText(df2.format(mItem.getPrice()));
        // create "paid for"
        final TextView dateView = (TextView) itemLayout.findViewById(R.id.paid_for_view);
        dateView.setText(mItem.getPayerName());
        // create status
        final CheckBox statusView = (CheckBox) itemLayout.findViewById(R.id.statusCheckBox);
        statusView.setChecked(!mItem.getPayerName().equals("Not yet paid for"));
        // set onChangeListener for status
        statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {

               if(checked) {

                   LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   View popupView = inflater.inflate(R.layout.single_list_enter_name_popup, null, false);
                   final PopupWindow pw = new PopupWindow(popupView, 440, 440, true);
                   EditText name = (EditText) popupView.findViewById(R.id.enter_name_view2);

                   pw.showAtLocation(popupView, Gravity.CENTER, 0 ,0); //?? popupView

                   Button cancel = (Button) popupView.findViewById(R.id.cancel_name);
                   cancel.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           pw.dismiss();
                           statusView.setChecked(false);
                       }
                   });

                   Button submit = (Button) popupView.findViewById(R.id.submit_name);
                   submit.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           pw.dismiss();
                           statusView.setChecked(true);
                       }
                   });

                    mItem.setPayerName(name.toString());
                } else {
                   mItem.setPayerName("Not yet paid for");
                }
            }
        });
        return itemLayout;
    }
}
