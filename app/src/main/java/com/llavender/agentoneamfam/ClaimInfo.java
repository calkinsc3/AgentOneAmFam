package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClaimInfo extends Fragment {

    public static ParseObject selectedClaim;

    ParseQuery<ParseUser> clients;
    ParseQuery<ParseObject> policies;
    List<ParseUser> clientList;
    List<String> clientNames;
    List<ParseObject> policyList;
    List<String> policyNames;

    ArrayAdapter clientSpinnerAdapter;
    ArrayAdapter policySpinnerAdapter;

    String clientSpinnerText = "";
    String policySpinnerText = "";

    Spinner clientSpinner;
    Spinner policySpinner;
    EditText damages_entry;
    EditText comments_entry;

    public ClaimInfo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            selectedClaim = Singleton.getClaims().get(this.getArguments().getInt("claimIndex"));
        } catch (NullPointerException e) {
            selectedClaim = null;
        }

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_claim_info, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        damages_entry = (EditText) view.findViewById(R.id.damages_entry);
        comments_entry = (EditText) view.findViewById(R.id.comments_entry);

        clientSpinner = (Spinner) view.findViewById(R.id.client_spinner);
        policySpinner = (Spinner) view.findViewById(R.id.policy_spinner);

        clients = ParseUser.getQuery();
        policies = ParseQuery.getQuery("Policy");

        setSpinners();

        //load the selected claims info into the fields
        if (selectedClaim != null) {
            String damages = selectedClaim.getNumber("Damages").toString();
            BigDecimal parsed = new BigDecimal(damages).setScale(2, BigDecimal.ROUND_FLOOR);
            String formattedDamages = NumberFormat.getCurrencyInstance().format(parsed);

            damages_entry.setText(formattedDamages);
            comments_entry.setText(selectedClaim.getString("Comment"));

            showMyUploads();
        }

        // Formats the damages into currency format.
        damages_entry.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    damages_entry.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");

                    BigDecimal parsed = new BigDecimal(cleanString)
                            .setScale(2, BigDecimal.ROUND_FLOOR)
                            .divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

                    String formatted = NumberFormat.getCurrencyInstance().format(parsed);

                    current = formatted;
                    damages_entry.setText(formatted);
                    damages_entry.setSelection(formatted.length());

                    damages_entry.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Saves the current claim
     */
    private void saveClaim(){
        final ParseObject obj;

        String comments = comments_entry.getText().toString();
        String policyID = policyList.get(policyNames
                .indexOf(policySpinner.getSelectedItem().toString())).getObjectId();

        String damages = damages_entry.getText().toString();
        damages = damages.replace("$","");
        damages = damages.replace(",","");

        if (Claims.selectedClaim != null) {
            obj = Singleton.getClaims().get(Claims.selectedClaim.index);
        } else {
            obj = new ParseObject("Claim");
        }

        obj.put("Damages", Double.parseDouble(damages));
        obj.put("Comment", comments);
        obj.put("PolicyID", policyID);

        obj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Claim Saved.", Toast.LENGTH_SHORT).show();
                    selectedClaim = obj;
                    showMyUploads();

                    // Save comment changes
                    MyUploads.saveComments(getActivity());
                } else {
                    Toast.makeText(getActivity(), "Claim NOT saved:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Shows the myUploads sub fragment below the information displays
     */
    private void showMyUploads(){
        Fragment newFragment = new MyUploads();
        Bundle bundle = new Bundle();
        ArrayList<String> uploadIds = new ArrayList<>();

        if(selectedClaim != null) {
            JSONArray jsonArray = selectedClaim.getJSONArray("UploadIDs");

            // Convert jsonArray to array list.
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    uploadIds.add(jsonArray.getString(i));
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Uploads not retrieved:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            bundle.putStringArrayList("UploadIDs", uploadIds);
            bundle.putString("claimPolicyID", selectedClaim.getString("PolicyID"));
        }

        newFragment.setArguments(bundle);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.bottom_container, newFragment).commit();
    }

    /**
     * Sets the values for the spinners.
     * If it is a new claim, the spinners are set to the first client in the list of clients and
     * their first policy listed and the spinners are clickable. Else, the spinners are set to the
     * client and policy the claim belongs to and the spinners are not clickable.
     */
    private void setSpinners() {
        try {
            clients.whereEqualTo("AgentID", ParseUser.getCurrentUser().getObjectId());
            clientList = clients.find();

            clientNames = new ArrayList<>();
            policyNames = new ArrayList<>();
            policyList = new ArrayList<>();

            // Find the policies of each of the clients and get the client's names.
            for (int i = 0; i < clientList.size(); i++) {
                clientNames.add(clientList.get(i).getString("Name"));
            }

            // Populate the spinners with the client names.
            clientSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, clientNames);
            clientSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            clientSpinner.setAdapter(clientSpinnerAdapter);

        } catch (ParseException pe) {
            pe.printStackTrace();
        }

        if (selectedClaim != null) {
            // Existing claim case.
            clientSpinner.setClickable(false);
            policySpinner.setClickable(false);

            // Get the current policy which the current claim is under.
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Policy");
            query.getInBackground(selectedClaim.getString("PolicyID"), new GetCallback<ParseObject>() {
                public void done(ParseObject policy, ParseException e) {
                    if (e == null) {
                        policyList.add(policy);
                        policyNames.add(policy.getString("Description"));

                        // Populate the policy spinner.
                        policySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item, policyNames);

                        policySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        policySpinner.setAdapter(policySpinnerAdapter);

                        // Set the policy spinner.
                        policySpinner.setSelection(policySpinnerAdapter.getPosition
                                (policy.getString("Description")));

                        try {
                            ParseQuery<ParseUser> query = ParseUser.getQuery();

                            // Set the client spinner.
                            ArrayAdapter clientArrayAdapter = (ArrayAdapter) clientSpinner.getAdapter();
                            clientSpinner.setSelection(clientArrayAdapter.getPosition
                                    (query.get(policy.getString("ClientID")).getString("Name")));
                        } catch (ParseException pe) {
                            pe.printStackTrace();
                        }
                    } else {
                        Log.e("ERROR", e.getMessage());
                    }
                }
            });

        } else {
            // New claim case.
            clientSpinner.setClickable(true);
            policySpinner.setClickable(true);

            spinnerListeners();

            policyList = new ArrayList<>();
            clientSpinnerText = clientNames.get(0);

            try {
                // Get the policies for the selected client.
                policies.whereEqualTo("ClientID",
                        clientList.get(clientNames.indexOf(clientSpinnerText)).getObjectId());
                policyList.addAll(policies.find());

            } catch (ParseException pe) {
                pe.printStackTrace();
            }

            for (int i = 0; i < policyList.size(); i++) {
                policyNames.add(policyList.get(i).getString("Description"));
            }

            // Populate the policy spinner with the policy names.
            policySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, policyNames);
            policySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            policySpinner.setAdapter(policySpinnerAdapter);

            if (policyNames.isEmpty()) {
                Toast.makeText(getActivity(), clientSpinnerText + " doesn't have any policies!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Listeners for the selection of spinner items.
     */
    private void spinnerListeners() {
        // User changes the selected client.
        clientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clientSpinnerText = String.valueOf(clientSpinner.getSelectedItem());
                policyNames = new ArrayList<>();
                policyList = new ArrayList<>();

                // Find all the policies of the new selected client.
                try {
                    policies.whereEqualTo("ClientID",
                            clientList.get(clientNames.indexOf(clientSpinnerText)).getObjectId());
                    policyList.addAll(policies.find());

                } catch (ParseException pe) {
                    pe.printStackTrace();
                }

                for (int i = 0; i < policyList.size(); i++) {
                    policyNames.add(policyList.get(i).getString("Description"));
                }

                // Update the policy spinner with the new list of policies.
                policySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, policyNames);
                policySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                policySpinner.setAdapter(policySpinnerAdapter);

                if (policyNames.isEmpty()) {
                    Toast.makeText(getActivity(), clientSpinnerText + " doesn't have any policies!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // User changes the selected policy.
        policySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                policySpinnerText = String.valueOf(policySpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_save);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:

                if(policyNames.isEmpty()){
                    Toast.makeText(getActivity(), clientSpinnerText + " doesn't have any policies!",
                            Toast.LENGTH_SHORT).show();
                }else{
                    saveClaim();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}