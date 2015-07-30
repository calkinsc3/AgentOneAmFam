package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentManager;
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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.text.NumberFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPolicyFragment extends Fragment {

    TextView client;
    EditText description;
    EditText cost;
    EditText address;
    EditText city;
    Spinner stateSpinner;
    EditText zip;
    CheckBox accepted;
    ParseObject policy;
    LinearLayout address2;
    String[] states;
    FragmentManager fm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_policy_information, container, false);

        setHasOptionsMenu(true);
        initializeVariables(view);

        //set the client ID
        client.append(Singleton.getCurrentClient().getObjectId());

        //Set the adapter for the state spinner
        stateSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, states));

        checkOrientationSetLayoutOrientation();

        setCostTextChangedListener();

        return view;
    }

    private void initializeVariables(View view){
        client = (TextView)view.findViewById(R.id.clientID);

        description = (EditText)view.findViewById(R.id.description);
        cost = (EditText)view.findViewById(R.id.cost);
        address = (EditText)view.findViewById(R.id.address);
        city = (EditText)view.findViewById(R.id.city);
        zip = (EditText)view.findViewById(R.id.zip);

        stateSpinner = (Spinner)view.findViewById(R.id.stateSpinner);

        policy = Singleton.getCurrentPolicy();

        accepted = (CheckBox)view.findViewById(R.id.accepted);
        address2 = (LinearLayout)view.findViewById(R.id.address2Layout);

        states = getResources().getStringArray(R.array.states);

        fm = getFragmentManager();
    }

    private void setCostTextChangedListener(){
        cost.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
            public void afterTextChanged(Editable s) {}
        });
    }

    public void checkOrientationSetLayoutOrientation(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            address2.setOrientation(LinearLayout.VERTICAL);

            city.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            zip.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_save);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void createPolicy() {
        String costFormatted = cost.getText().toString();
        costFormatted = costFormatted.replace("$","");
        costFormatted = costFormatted.replace(",","");

        final ParseObject newPolicy = new ParseObject("Policy");
        newPolicy.put("AgentID", ParseUser.getCurrentUser().getObjectId());
        newPolicy.put("ClientID", Singleton.getCurrentClient().getObjectId());
        newPolicy.put("Address", address.getText().toString());
        newPolicy.put("City", city.getText().toString());
        newPolicy.put("State", stateSpinner.getSelectedItem().toString());
        newPolicy.put("Zip", Integer.valueOf(zip.getText().toString()));
        newPolicy.put("Description", description.getText().toString());
        newPolicy.put("Accepted", accepted.isChecked());
        newPolicy.put("Cost", Double.parseDouble(costFormatted));

        newPolicy.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(getActivity(), "New Policy Created", Toast.LENGTH_SHORT).show();
                    Singleton.setCurrentPolicy(newPolicy);
                    startPolicyInformationFragment();
                } else {
                    Log.e("Save Error: ", e.toString());
                    Toast.makeText(getActivity(), "Error saving new Policy", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startPolicyInformationFragment() {
        while (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        }

        //Start the policyInformation Fragment
        fm.beginTransaction().replace(R.id.fragment_container, new PolicyInformationFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //is actually a call to create a new Policy
            case R.id.action_save:
                createPolicy();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}