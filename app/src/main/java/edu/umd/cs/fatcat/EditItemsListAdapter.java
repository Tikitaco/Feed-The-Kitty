package edu.umd.cs.fatcat;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.util.Locale;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;

import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;


public class EditItemsListAdapter extends BaseAdapter {

    private final List<SingleItem> mItems = new ArrayList<>();
    private final Context mContext;
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private FatcatEvent event; // Event being edited


    public EditItemsListAdapter(Context context) {

        mContext = context;

    }

    public EditItemsListAdapter(Context context, List<SingleItem> list, FatcatEvent event) {
        mContext = context;
        mItems.addAll(list);
        this.event = event;
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
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setCurrencySymbol(""); // Don't use null.
            formatter.setDecimalFormatSymbols(symbols);
            priceView.setText(formatter.format(value));
        } else {
            priceView.setText("0.00");
        }
        // create "paid for"
        final TextView payerView = (TextView) dataView.findViewById(R.id.paid_for_view);
        // payerView.setText(mItem.getPayerName());

        // create status
        final CheckBox statusView = (CheckBox) dataView.findViewById(R.id.statusCheckBox);
        statusView.setChecked(!mItem.getPayerName().equals("Not yet paid for"));
        if (!mItem.hasBeenPaidFor() && !mItem.getPayerName().equals(MainActivity.globals.myProfile.getUsername())) {
            statusView.setClickable(false);
            statusView.setButtonTintList(ColorStateList.valueOf(Color.LTGRAY));
        }
        if (!mItem.hasBeenPaidFor()) {
            payerView.setText("Paid for by " + mItem.getPayerName());
        } else {
            payerView.setText("Not yet paid for");
        }
        // set onChangeListener for status
        statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {

                if(checked) {
                    String username = MainActivity.globals.myProfile.getUsername();
                    mItem.setPayerName(username);
                    payerView.setText("Paid for by " + username);
                    FirebaseUtils.paidForItem(event, mItem);
                } else {
                    mItem.setPayerName("Not yet paid for");
                    payerView.setText("Not yet paid for");
                    FirebaseUtils.unpaidForItem(event, mItem);
                }
            }
        });
        return dataView;
    }

}



