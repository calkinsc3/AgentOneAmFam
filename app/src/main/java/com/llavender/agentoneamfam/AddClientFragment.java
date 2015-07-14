package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseUser;

/**
 * Created by nsr009 on 7/6/2015.
 */
public class AddClientFragment extends Fragment {

    private View rootView;

    private EditText nameEdit;
    private EditText phoneEdit;
    private EditText emailEdit;
    private EditText addressEdit;
    private EditText cityEdit;
    private EditText zipEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;

    private Spinner stateSpinner;

    private Button createClientButton;

    private FragmentManager fm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_client, container, false);
        fm = getFragmentManager();
        initializeFields();

        createClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createClient();
                Toast.makeText(getActivity(), "New client created successfully!", Toast.LENGTH_SHORT).show();
                returnToMain();
            }
        });

        return rootView;
    }

    private void returnToMain(){
        while (fm.getBackStackEntryCount() > 1){
            fm.popBackStackImmediate();
        }
        //Restart the Login fragment
        fm.beginTransaction().replace(R.id.fragment_container, new MainFragment())
                .commit();
    }

    private void initializeFields() {

        nameEdit = (EditText) rootView.findViewById(R.id.editName);
        phoneEdit = (EditText) rootView.findViewById(R.id.editPhone);
        emailEdit = (EditText) rootView.findViewById(R.id.editEmail);
        addressEdit = (EditText) rootView.findViewById(R.id.editAddress);
        cityEdit = (EditText) rootView.findViewById(R.id.editCity);
        zipEdit = (EditText) rootView.findViewById(R.id.editZip);
        usernameEdit = (EditText) rootView.findViewById(R.id.editUsername);
        passwordEdit = (EditText) rootView.findViewById(R.id.editPassword);

        stateSpinner = (Spinner) rootView.findViewById(R.id.spinnerState);

        /* Set the spinner with all the states */
        ArrayAdapter<CharSequence> stateSpinnerAdapter =
                ArrayAdapter.createFromResource(getActivity(),
                        R.array.states,android.R.layout.simple_spinner_item);

        stateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stateSpinner.setAdapter(stateSpinnerAdapter);

        createClientButton = (Button) rootView.findViewById(R.id.createClientButton);
    }

    private void createClient() {
        ParseUser newClient = new ParseUser();

        if (passwordEdit.getText().toString().equals("")) {
            passwordEdit.setError("Password Required.");
        } else if (usernameEdit.getText().toString().equals("")) {
            usernameEdit.setError("UserName Required.");
        } else if (emailEdit.getText().toString().equals("")) {
            emailEdit.setError("Email Required.");
        } else {
            newClient.setUsername(usernameEdit.getText().toString());
            newClient.setPassword(passwordEdit.getText().toString());
            newClient.setEmail(emailEdit.getText().toString());

            newClient.put("Name", nameEdit.getText().toString());
            newClient.put("phoneNumber", Double.valueOf(phoneEdit.getText().toString()));
            newClient.put("Address", addressEdit.getText().toString());
            newClient.put("City", cityEdit.getText().toString());
            newClient.put("State", stateSpinner.getSelectedItem().toString());
            newClient.put("Zip", Double.valueOf(zipEdit.getText().toString()));
            newClient.put("accountType", "Client");
            newClient.put("AgentID", ParseUser.getCurrentUser().getObjectId());

            try {
                newClient.signUp();
            } catch (com.parse.ParseException e){
                Log.e("Signup Error", e.toString());
            }
        }
    }
}
