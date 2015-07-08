package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.ParseObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClientInformationFragment extends Fragment {

    ImageButton phoneButton;
    ImageButton emailButton;
    ImageButton mapButton;
    TextView clientName;
    TextView phoneNumber;
    TextView email;
    TextView address1;
    TextView address2;
    ParseObject client;

    public ClientInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_information, container, false);

        phoneButton = (ImageButton)view.findViewById(R.id.phoneButton);
        emailButton = (ImageButton)view.findViewById(R.id.emailButton);
        mapButton = (ImageButton)view.findViewById(R.id.mapButton);
        clientName = (TextView)view.findViewById(R.id.clientName);
        phoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
        email = (TextView)view.findViewById(R.id.email);
        address1 = (TextView)view.findViewById(R.id.addressLine1);
        address2 = (TextView)view.findViewById(R.id.addressLine2);
        client = Singleton.getCurrentClient();


        clientName.setText(client.getString("Name"));
        phoneNumber.setText(String.valueOf(client.getNumber("phoneNumber")));
        email.setText(client.getString("email"));
        address1.setText(client.getString("Address"));
        address2.setText(client.getString("City") + client.getString("State") + client.getInt("Zip"));

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + String.valueOf(client.getNumber("phoneNumber"))));
                startActivity(intent);
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {client.getString("email")});
                emailIntent.setType("plain/text");
                if (emailIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(emailIntent, "Choose Mail Application"));
                }
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String streetAddress = client.getString("Address").replaceAll(" ", "+") + "+"
                        + client.getString("City") + ",+" + client.getString("State") + "+"
                        + client.getInt("Zip");
                // Create a Uri from an intent string. Use the result to create an Intent.
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + streetAddress);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            }
        });

        return view;
    }


}
