package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
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
import android.widget.Toast;

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
    private EditText password_reentry;
    private EditText name_entry;
    private EditText phone_entry;
    private EditText email_entry;

    private EditText street_entry;
    private EditText city_entry;
    private EditText zip_entry;
    private Spinner state_spinner;

    private Button email_support_button;
    private Button logout_button;

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
        password_reentry = (EditText) view.findViewById(R.id.password_reentry);
        name_entry = (EditText) view.findViewById(R.id.name_entry);
        phone_entry = (EditText) view.findViewById(R.id.phone_entry);
        email_entry = (EditText) view.findViewById(R.id.email_entry);

        street_entry = (EditText) view.findViewById(R.id.address_street);
        city_entry = (EditText) view.findViewById(R.id.address_city);
        zip_entry = (EditText) view.findViewById(R.id.zip_code);

        state_spinner = (Spinner) view.findViewById(R.id.state_spinner);
        email_support_button = (Button) view.findViewById(R.id.email_support_button);
        logout_button = (Button) view.findViewById(R.id.logout_button);

        buttonVisibilities();

        List<String> states = Arrays.asList(getResources().getStringArray(R.array.states));

        //POPULATE STATE DROPDOWN
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.states,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        state_spinner.setAdapter(adapter);

        //load current user info
        curUser = ParseUser.getCurrentUser();

        username_entry.setText(curUser.getUsername());
        street_entry.setText(curUser.get("Address").toString());
        city_entry.setText(curUser.get("City").toString());
        zip_entry.setText(String.valueOf(curUser.getNumber("Zip")));
        name_entry.setText(curUser.get("Name").toString());
        email_entry.setText(curUser.getEmail());
        state_spinner.setSelection(states.indexOf(curUser.getString("State")));

        String phoneText = String.valueOf(curUser.getNumber("phoneNumber"));
        String phoneNumber = ("(" + phoneText.substring(0,3) + ")" + " " +
                phoneText.substring(3,6) + "-" + phoneText.substring(6));

        phone_entry.setText(phoneNumber);
        phone_entry.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        email_support_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_support_button.setEnabled(false);

                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support.intern@amfam.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support Question");
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        });

        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout_button.setEnabled(false);

                Tools.logout(getActivity());
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        buttonVisibilities();
    }

    private void buttonVisibilities() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            email_support_button.setVisibility(View.GONE);
            logout_button.setVisibility(View.GONE);
        } else {
            email_support_button.setVisibility(View.VISIBLE);
            logout_button.setVisibility(View.VISIBLE);
        }
    }

    private void saveSettings() {
        if (!password_entry.getText().toString().equals(password_reentry.getText().toString())) {
            password_entry.setError("Passwords are not equal!");
            password_reentry.setError("Passwords are not equal!");
        } else {
            String phoneUpdate = phone_entry.getText().toString();
            phoneUpdate = phoneUpdate.replace("(","");
            phoneUpdate = phoneUpdate.replace(")","");
            phoneUpdate = phoneUpdate.replace("-","");
            phoneUpdate = phoneUpdate.replace(" ","");

            curUser.put("username", username_entry.getText().toString());
            curUser.put("Address", street_entry.getText().toString());
            curUser.put("City", city_entry.getText().toString());
            curUser.put("State", state_spinner.getSelectedItem().toString());
            curUser.put("Zip", Double.valueOf(zip_entry.getText().toString()));
            curUser.put("Name", name_entry.getText().toString());
            curUser.put("phoneNumber", Double.valueOf(phoneUpdate));
            curUser.setEmail(email_entry.getText().toString());

            if (!password_entry.getText().toString().equals(""))
                curUser.setPassword(password_entry.getText().toString());

            curUser.saveInBackground();
            password_entry.getText().clear();
            password_reentry.getText().clear();

            Toast.makeText(getActivity(), "Settings saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_save);
        menu.findItem(R.id.action_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveSettings();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onCreate(null);

        email_support_button.setEnabled(true);
        logout_button.setEnabled(true);
    }
}