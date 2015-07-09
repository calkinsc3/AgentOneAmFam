package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends Fragment {
    public static String user_email = ParseUser.getCurrentUser().getEmail();


    public Settings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        final EditText username_entry = (EditText) view.findViewById(R.id.username_entry);
        final EditText password_entry = (EditText) view.findViewById(R.id.password_entry);
        final EditText name_entry = (EditText) view.findViewById(R.id.name_entry);
        final EditText phone_entry = (EditText) view.findViewById(R.id.phone_entry);
        final EditText email_entry = (EditText) view.findViewById(R.id.email_entry);


        final EditText street_entry = (EditText) view.findViewById(R.id.address_street);
        final EditText city_entry = (EditText) view.findViewById(R.id.address_city);
        final EditText zip_entry = (EditText) view.findViewById(R.id.zip_code);


        Button email_support_button = (Button) view.findViewById(R.id.email_support_button);
        Button logout_button = (Button) view.findViewById(R.id.logout_button);
        Spinner state_spinner = (Spinner) view.findViewById(R.id.state_spinner);

        List<String> states = Arrays.asList(getResources().getStringArray(R.array.all_abbreviations));

        //POPULATE STATE DROPDOWN
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.all_abbreviations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        state_spinner.setAdapter(adapter);

        //load info
        ParseUser currUser = ParseUser.getCurrentUser();

        username_entry.setText(currUser.getUsername());
        //TODO
        // password_entry.setText(currUser.get("password").toString());
        street_entry.setText(currUser.get("Address").toString());
        city_entry.setText(currUser.get("City").toString());
        zip_entry.setText(currUser.get("Zip").toString());
        name_entry.setText(currUser.get("Name").toString());
        phone_entry.setText(currUser.get("phoneNumber").toString());
        email_entry.setText(currUser.getEmail());
        state_spinner.setSelection(states.indexOf(currUser.getString("State")));



        email_support_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support.intern@amfam.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support Question");
                startActivity(Intent.createChooser(intent, "Send Email"));

            }
        });

//        logout_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Tools.logout(getActivity());
//            }
//        });

    }

}
