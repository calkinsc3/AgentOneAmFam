package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class PolicyInformationFragment extends Fragment {

    TextView client;

    EditText description;
    EditText cost;
    EditText address;
    EditText city;
    EditText zip;

    Spinner stateSpinner;

    CheckBox accepted;

    ParseObject policy;

    LinearLayout address2;

    String[] states;

    public PolicyInformationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_policy_information, container, false);

        setHasOptionsMenu(true);

        initializeFields(view);

        includeMyUploadsFragment();

        String costString = policy.getNumber("Cost").toString();
        BigDecimal parsed = new BigDecimal(costString).setScale(2, BigDecimal.ROUND_FLOOR);
        String formattedCost = NumberFormat.getCurrencyInstance().format(parsed);

        client.append(policy.getString("ClientID"));
        description.setText(policy.getString("Description"));
        cost.setText(formattedCost);
        address.setText(policy.getString("Address"));
        city.setText(policy.getString("City"));
        zip.setText(String.valueOf(policy.getNumber("Zip")));
        accepted.setChecked(policy.getBoolean("Accepted"));

        stateSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, states));

        stateSpinner.setSelection(Arrays.asList(states).indexOf(policy.getString("State")));

        checkOrientationSetLayoutOrientation();

        cost.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    cost.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    BigDecimal parsed = new BigDecimal(cleanString)
                            .setScale(2, BigDecimal.ROUND_FLOOR)
                            .divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed);

                    current = formatted;
                    cost.setText(formatted);
                    cost.setSelection(formatted.length());

                    cost.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private void initializeFields(View view) {
        client = (TextView) view.findViewById(R.id.clientID);
        description = (EditText) view.findViewById(R.id.description);
        cost = (EditText) view.findViewById(R.id.cost);
        address = (EditText) view.findViewById(R.id.address);
        city = (EditText) view.findViewById(R.id.city);
        stateSpinner = (Spinner) view.findViewById(R.id.stateSpinner);
        zip = (EditText) view.findViewById(R.id.zip);
        policy = Singleton.getCurrentPolicy();
        accepted = (CheckBox) view.findViewById(R.id.accepted);
        address2 = (LinearLayout) view.findViewById(R.id.address2Layout);
        states = getResources().getStringArray(R.array.states);
    }

    private void includeMyUploadsFragment() {
        Bundle isNewBundle = new Bundle();
        MyUploads myUploads = new MyUploads();

        isNewBundle.putBoolean("FROMPOLICY", true);
        isNewBundle.putString("claimPolicyID", Singleton.getCurrentPolicy().getObjectId());
        myUploads.setArguments(isNewBundle);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.bottom_container, myUploads).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_save);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d("onOptionsItemSelected", "yes");
        switch (item.getItemId()) {
            //is actually a call to create a new Policy
            case R.id.action_save:
                savePolicy();
                // Save comment changes
                MyUploads.saveComments(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkOrientationSetLayoutOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            address2.setOrientation(LinearLayout.VERTICAL);
            city.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            zip.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }
    }

    private void savePolicy() {

        ParseObject policyToSave = Singleton.getCurrentPolicy();

        policyToSave.put("Address", address.getText().toString());
        policyToSave.put("City", city.getText().toString());
        policyToSave.put("State", stateSpinner.getSelectedItem().toString());
        policyToSave.put("Zip", Double.valueOf(zip.getText().toString()));
        policyToSave.put("Description", description.getText().toString());
        policyToSave.put("Accepted", accepted.isChecked());

        String costFormatted = cost.getText().toString();
        costFormatted = costFormatted.replace("$","");
        costFormatted = costFormatted.replace(",","");
        policyToSave.put("Cost", Double.parseDouble(costFormatted));

        policyToSave.saveInBackground();
        Toast.makeText(getActivity(), "Policy Information Saved", Toast.LENGTH_SHORT).show();

    }


}