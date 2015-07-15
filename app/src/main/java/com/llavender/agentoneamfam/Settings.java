package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private ParseUser curUser;

    private EditText username_entry;
    private EditText password_entry;
    private EditText name_entry;
    private EditText phone_entry;
    private EditText email_entry;

    private EditText street_entry;
    private EditText city_entry;
    private EditText zip_entry;
    private Spinner state_spinner;

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
        setHasOptionsMenu(true);

        username_entry = (EditText) view.findViewById(R.id.username_entry);
        password_entry = (EditText) view.findViewById(R.id.password_entry);
        name_entry = (EditText) view.findViewById(R.id.name_entry);
        phone_entry = (EditText) view.findViewById(R.id.phone_entry);
        email_entry = (EditText) view.findViewById(R.id.email_entry);

        street_entry = (EditText) view.findViewById(R.id.address_street);
        city_entry = (EditText) view.findViewById(R.id.address_city);
        zip_entry = (EditText) view.findViewById(R.id.zip_code);

        state_spinner = (Spinner) view.findViewById(R.id.state_spinner);
        Button email_support_button = (Button) view.findViewById(R.id.email_support_button);
        Button logout_button = (Button) view.findViewById(R.id.logout_button);

        List<String> states = Arrays.asList(getResources().getStringArray(R.array.all_abbreviations));

        //POPULATE STATE DROPDOWN
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.all_abbreviations, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        state_spinner.setAdapter(adapter);

        //load current user info
        curUser = ParseUser.getCurrentUser();

        username_entry.setText(curUser.getUsername());
        street_entry.setText(curUser.get("Address").toString());
        city_entry.setText(curUser.get("City").toString());
        zip_entry.setText(String.valueOf(curUser.getNumber("Zip")));
        name_entry.setText(curUser.get("Name").toString());
        phone_entry.setText(String.valueOf(curUser.getNumber("phoneNumber")));
        email_entry.setText(curUser.getEmail());
        state_spinner.setSelection(states.indexOf(curUser.getString("State")));

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

    private void saveSettings() {
        curUser.put("username", username_entry.getText().toString());
        curUser.put("Address", street_entry.getText().toString());
        curUser.put("City", city_entry.getText().toString());
        curUser.put("State", state_spinner.getSelectedItem().toString());
        curUser.put("Zip", Double.valueOf(zip_entry.getText().toString()));
        curUser.put("Name", name_entry.getText().toString());
        curUser.put("phoneNumber", Double.valueOf(phone_entry.getText().toString()));
        curUser.setEmail(email_entry.getText().toString());

        if (!password_entry.getText().toString().equals(""))
            curUser.setPassword(password_entry.getText().toString());

        curUser.saveInBackground();
        password_entry.getText().clear();
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
                saveSettings();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}