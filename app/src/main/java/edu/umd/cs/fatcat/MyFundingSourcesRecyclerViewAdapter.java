package edu.umd.cs.fatcat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.umd.cs.fatcat.FundingSourcesFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FundingSourcesItem} and makes a call to the
 * specified {@link FundingSourcesFragment.OnListFragmentInteractionListener}.
 */
public class MyFundingSourcesRecyclerViewAdapter extends RecyclerView.Adapter<MyFundingSourcesRecyclerViewAdapter.ViewHolder> {

    private final List<FundingSourcesItem> mValues;
    private final FundingSourcesFragment.OnListFragmentInteractionListener mListener;

    public MyFundingSourcesRecyclerViewAdapter(List<FundingSourcesItem> items, FundingSourcesFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_fundingsources, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText("Funding source: " + mValues.get(position).getFundId());
        holder.mContentView.setText(mValues.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public FundingSourcesItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.funding_source_id);
            mContentView = (TextView) view.findViewById(R.id.funding_source_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
