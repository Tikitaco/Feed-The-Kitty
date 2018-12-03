package edu.umd.cs.fatcat;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import edu.umd.cs.fatcat.dwolla.DwollaUtil;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FundingSourcesFragment extends Fragment {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private FloatingActionButton mAddFundButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FundingSourcesFragment() {
    }

    @SuppressWarnings("unused")
    public static FundingSourcesFragment newInstance(int columnCount) {
        FundingSourcesFragment fragment = new FundingSourcesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAddFundButton = getView().findViewById(R.id.add_funding_source_button);
        mAddFundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View popup = inflater.inflate(R.layout.popup_add_funding_source, null);
                final EditText fundName = popup.findViewById(R.id.add_fund_name);
                final EditText routingNumber = popup.findViewById(R.id.add_fund_routing_number);
                final EditText accountNumber = popup.findViewById(R.id.add_fund_account_number);
                final Spinner accountType = popup.findViewById(R.id.add_fund_account_type_spinner);
                Button addFund = popup.findViewById(R.id.add_fund_button);
                Button cancel = popup.findViewById(R.id.add_fund_cancel_button);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add Funding Source");
                builder.setView(popup);

                final AlertDialog dialog = builder.show();

                addFund.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] args = new String[5];
                        args[0] = MainActivity.globals.myProfile.customerId;
                        args[1] = routingNumber.getText().toString();
                        args[2] = accountNumber.getText().toString();
                        args[3] = accountType.getSelectedItem().toString();
                        args[4] = fundName.getText().toString();

                        DwollaFundCreationTask creationTask = new DwollaFundCreationTask();
                        creationTask.execute(args);

                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fundingsources_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.funding_sources_list);

        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }


            Map<String, String> fundingSources = MainActivity.globals.myProfile.fundingSources;
            List<FundingSourcesItem> fundingSourcesItems = new ArrayList();
            for (String key : fundingSources.keySet()) {
                fundingSourcesItems.add(new FundingSourcesItem(key, fundingSources.get(key)));
            }

            recyclerView.setAdapter(new MyFundingSourcesRecyclerViewAdapter(fundingSourcesItems, mListener));
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FundingSourcesItem item);
    }

    private class DwollaFundCreationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            DwollaUtil util = new DwollaUtil();

            String fundId = null;
            try {
                fundId = util.createFundingSource(args[0], args[1], args[2], args[3], args[4]);
                if (null != fundId) {
                    util.initiateMicroDeposit(fundId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (null != fundId) {
                FirebaseUtils.addedFundingSource(fundId, args[4]);
            }

            return fundId;
        }

        @Override
        protected void onPostExecute(String fundId) {
            if (null == fundId) {
                Toast.makeText(getActivity(), "Unable to add a fund source at this time", Toast.LENGTH_LONG).show();
            } else { // Reminder: runs on UI thread so this is ok
                final String fundSource = fundId;

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View popup = inflater.inflate(R.layout.popup_verify_micro_deposits, null);
                final EditText microDeposit1 = popup.findViewById(R.id.micro_deposit_1);
                final EditText microDeposit2 = popup.findViewById(R.id.micro_deposit_2);
                Button verify = popup.findViewById(R.id.verify_micro_deposit_button);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Verify Micro Deposits");
                builder.setView(popup);

                final AlertDialog dialog = builder.show();

                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String[] args = new String[3];
                        args[0] = fundSource;
                        args[1] = microDeposit1.getText().toString();
                        args[2] = microDeposit2.getText().toString();

                        DwollaMicroDepositVerificationTask verificationTask = new DwollaMicroDepositVerificationTask();
                        verificationTask.execute(args);

                        dialog.dismiss();
                    }
                });
            }
        }
    }

    private class DwollaMicroDepositVerificationTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... args) {
            DwollaUtil util = new DwollaUtil();

            boolean success = false;
            try {
                success = util.verifyMicroDeposit(args[0], args[1], args[2]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(getActivity(), "Unable to verify fund source, please contact support", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Fund source successfully added and verified", Toast.LENGTH_LONG).show();
            }
        }

    }
}
