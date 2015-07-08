package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseObject;

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
    Spinner stateSpinner;
    EditText zip;
    ParseObject policy;

    public PolicyInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_policy_information, container, false);

        client = (TextView)view.findViewById(R.id.clientID);
        description = (EditText)view.findViewById(R.id.description);
        cost = (EditText)view.findViewById(R.id.cost);
        address = (EditText)view.findViewById(R.id.address);
        city = (EditText)view.findViewById(R.id.city);
        stateSpinner = (Spinner)view.findViewById(R.id.stateSpinner);
        zip = (EditText)view.findViewById(R.id.zip);
        policy = Singleton.getCurrentPolicy();
        String[] states = getResources().getStringArray(R.array.states);

        client.append(policy.getString("ClientID"));
        description.setText(policy.getString("Description"));
        cost.setText(String.valueOf(policy.getNumber("Cost")));
        address.setText(policy.getString("Address"));
        city.setText(policy.getString("City"));
        zip.setText(String.valueOf(policy.getNumber("Zip")));

        stateSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, states));

        stateSpinner.setSelection(Arrays.asList(states).indexOf(policy.getString("State")));



        return view;
    }


}
