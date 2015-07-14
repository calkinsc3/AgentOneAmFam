package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.opengl.Visibility;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

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

    public ClaimInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            selectedClaim = Singleton.getClaims().get(this.getArguments().getInt("claimIndex"));
        }catch (NullPointerException e){
            selectedClaim = null;
        }

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.fragment_claim_info, container, false);
    }

    EditText damages_entry;
    EditText comments_entry;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        damages_entry = (EditText) view.findViewById(R.id.damages_entry);
        comments_entry = (EditText) view.findViewById(R.id.comments_entry);

        clientSpinner = (Spinner) view.findViewById(R.id.client_spinner);
        policySpinner = (Spinner) view.findViewById(R.id.policy_spinner);

        clients = ParseUser.getQuery();
        policies = ParseQuery.getQuery("Policy");

        setSpinners();
        spinnerListeners();

        //load the selected claims info into the fields
        if (selectedClaim != null) {
            damages_entry.setText("$" + selectedClaim.getNumber("Damages").toString());
            comments_entry.setText(selectedClaim.getString("Comment"));
            showMyUploads();
        }


    }

    /**
     * Saves the current claim
     */
    private void saveClaim(){
        final ParseObject obj;

        String damages = damages_entry.getText().toString();
        String comments = comments_entry.getText().toString();
        String policyID = policyList.get(policyNames.indexOf(policySpinnerText)).getObjectId();

        if (Claims.selectedClaim != null) {
            obj = Singleton.getClaims().get(Claims.selectedClaim.index);
        } else {
            obj = new ParseObject("Claim");
        }

        obj.put("Damages", Double.parseDouble(damages.substring(1, damages.length())));
        obj.put("Comment", comments);
        obj.put("PolicyID", policyID);
        obj.put("UploadIDs", new ArrayList<>());

        obj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null) {
                    Toast.makeText(getActivity(), "Claim Saved.", Toast.LENGTH_SHORT).show();
                    selectedClaim = obj;
                    showMyUploads();

                    //save comment changes
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
            query.getInBackground(Singleton.getClaims().get(Claims.selectedClaim.index)
                    .getString("PolicyID"), new GetCallback<ParseObject>() {
                public void done(ParseObject policy, ParseException e) {
                    if (e == null) {
                        policyNames.add(policy.getString("Description"));

                        // Populate the policy spinner.
                        policySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_spinner_item, policyNames);
                        policySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        policySpinner.setAdapter(policySpinnerAdapter);

                        // Set the policy spinner.
                        ArrayAdapter policyArrayAdapter = (ArrayAdapter) policySpinner.getAdapter();
                        policySpinner.setSelection(policyArrayAdapter.getPosition
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

        Log.d("onOptionsItemSelected", "yes");
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
