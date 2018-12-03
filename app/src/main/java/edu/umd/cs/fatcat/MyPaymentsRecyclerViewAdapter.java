package edu.umd.cs.fatcat;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.umd.cs.fatcat.dwolla.DwollaUtil;

import java.io.IOException;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link PaymentsFragment.OnListFragmentInteractionListener}.
 */
public class MyPaymentsRecyclerViewAdapter extends RecyclerView.Adapter<MyPaymentsRecyclerViewAdapter.ViewHolder> {

    private final List<PaymentsItem> mValues;
    private final PaymentsFragment.OnListFragmentInteractionListener mListener;

    public MyPaymentsRecyclerViewAdapter(List<PaymentsItem> items, PaymentsFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_payments, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getPaymentId());
        holder.mContentView.setText(mValues.get(position).getAmount());
        if (mValues.get(position).getAmount().startsWith("-")) {
            holder.mContentView.setTextColor(Color.RED);
        } else {
            holder.mContentView.setTextColor(Color.GREEN);
        }

        if (null != mValues.get(position).getStatus()) {
            holder.mStatusView.setText("Status: " + mValues.get(position).getStatus());
        } else {
            PaymentsItem[] args = new PaymentsItem[1];
            args[0] = mValues.get(position);

            DwollaTransferStatusTask statusTask = new DwollaTransferStatusTask();
            statusTask.execute(args);
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
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mStatusView;
        public PaymentsItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.payment_id);
            mContentView = (TextView) view.findViewById(R.id.payment_amount);
            mStatusView = (TextView) view.findViewById(R.id.payments_status);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    private class DwollaTransferStatusTask extends AsyncTask<PaymentsItem, Void, PaymentsItem> {

        @Override
        protected PaymentsItem doInBackground(PaymentsItem... args) {
            DwollaUtil util = new DwollaUtil();

            String status = null;
            try {
                status = util.getTransferStatus(args[0].getPaymentId());
            } catch(IOException e) {
                e.printStackTrace();
            }

            args[0].setStatus(status);
            return args[0];
        }

        @Override
        protected void onPostExecute(PaymentsItem item) {
            notifyDataSetChanged();
        }
    }
}
