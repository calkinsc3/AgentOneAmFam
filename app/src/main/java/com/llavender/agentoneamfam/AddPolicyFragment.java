package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseObject;


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
    ImageButton saveButton;
    ListView photoView;
    ParseObject policy;
    LinearLayout address2;


    public AddPolicyFragment() {
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
        photoView = (ListView)view.findViewById(R.id.photoList);
        saveButton =(ImageButton)view.findViewById(R.id.save_button);
        policy = Singleton.getCurrentPolicy();
        accepted = (CheckBox)view.findViewById(R.id.accepted);
        address2 = (LinearLayout)view.findViewById(R.id.address2Layout);
        String[] states = getResources().getStringArray(R.array.states);

        stateSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, states));

        checkOrientationSetLayoutOrientation();

        return view;
    }

    public void checkOrientationSetLayoutOrientation(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            address2.setOrientation(LinearLayout.VERTICAL);
            city.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            zip.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }
    }


}
