package com.example.stephen.fatcat;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriendsListAdapter { //extends BaseAdapter {

//    private final List<ToDoItem> mItems = new ArrayList<ToDoItem>();
//    private final Context mContext;
//
//    private static final String TAG = "Lab-UserInterface";
//
//    public FriendsListAdapter(Context context) {
//
//        mContext = context;
//
//    }
//
//    // Add a ToDoItem to the adapter
//    // Notify observers that the data set has changed
//
//    public void add(ToDoItem item) {
//
//        mItems.add(item);
//        notifyDataSetChanged();
//
//    }
//
//    // Clears the list adapter of all items.
//
//    public void clear() {
//
//        mItems.clear();
//        notifyDataSetChanged();
//
//    }
//
//    // Returns the number of ToDoItems
//
//    @Override
//    public int getCount() {
//
//        return mItems.size();
//
//    }
//
//    // Retrieve the number of ToDoItems
//
//    @Override
//    public Object getItem(int pos) {
//
//        return mItems.get(pos);
//
//    }
//
//    // Get the ID for the ToDoItem
//    // In this case it's just the position
//
//    @Override
//    public long getItemId(int pos) {
//
//        return pos;
//
//    }
//
//    // Create a View for the ToDoItem at specified position
//    // Remember to check whether convertView holds an already allocated View
//    // before created a new View.
//    // Consider using the ViewHolder pattern to make scrolling more efficient
//    // See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        // create layout and toDoItem
//        final ToDoItem toDoItem = mItems.get(position);
//        RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.todo_item, parent, false);
//        // create title
//        final TextView titleView = (TextView) itemLayout.findViewById(R.id.titleView);
//        titleView.setText(toDoItem.getTitle());
//        // create priority
//        final TextView priorityView = (TextView) itemLayout.findViewById(R.id.priorityView);
//        priorityView.setText(toDoItem.getPriority().toString());
//        // create date
//        final TextView dateView = (TextView) itemLayout.findViewById(R.id.dateView);
//        dateView.setText(ToDoItem.FORMAT.format(toDoItem.getDate()));
//        // create status
//        final CheckBox statusView = (CheckBox) itemLayout.findViewById(R.id.statusCheckBox);
//        statusView.setChecked(toDoItem.getStatus() == Status.DONE);
//        // set onChangeListener for status
//        statusView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
//                Log.i(TAG, "Entered onCheckedChanged()");
//
//                if(checked) {
//                    toDoItem.setStatus(Status.DONE);
//                } else {
//                    toDoItem.setStatus(Status.NOTDONE);
//                }
//            }
//        });
//        return itemLayout;
//    }
//
////		ViewHolder holder = new ViewHolder();
////		holder.mTitleView = (TextView) convertView.findViewById(R.id.titleView;
////		holder.mStatusView = (TextView) convertView.findViewById(R.id.statusCheckBox);
////		holder.mPriorityView = (TextView) convertView.findViewById(R.id.priorityView);
////		holder.mDateView = (TextView) convertView.findViewById(R.id.dateView);
////		holder.mItemLayout = parent.layout();
////		holder.position = position;
//
//    static class ViewHolder {
//        int position;
//        RelativeLayout mItemLayout;
//        TextView mTitleView;
//        CheckBox mStatusView;
//        TextView mPriorityView;
//        TextView mDateView;
//    }
}
