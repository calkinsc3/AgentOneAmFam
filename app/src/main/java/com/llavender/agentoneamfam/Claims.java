package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class Claims extends Fragment {

    private static View mainView;
    public static ImageAdapter.ViewHolder selectedClaim;

    public Claims() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_uploads, container, false);
    }

    @Override
    public  void onViewCreated(View view, Bundle savedInstanceState){

        mainView = view;
        view.setBackground(getResources().getDrawable(R.drawable.clouds));

        final ListView pictureList = (ListView) view.findViewById(R.id.my_uploads_list_view);
        final TextView header = (TextView) view.findViewById(R.id.title);
        final ImageButton add_button = (ImageButton) view.findViewById(R.id.add_button);
        final com.github.clans.fab.FloatingActionButton fab = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.fab);

        //set fab icon, set title, show fab
        //only set visible if on the claims screen
        if(this.getArguments() == null) {
            fab.setVisibility(View.VISIBLE);
        }
        else{
            add_button.setVisibility(View.VISIBLE);
        }

        fab.setImageResource(android.R.drawable.ic_input_add);
        header.setText("Claims");

        refreshLocalClaimData(getActivity());


        pictureList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                selectedClaim = (ImageAdapter.ViewHolder) view.getTag();

                Bundle args = new Bundle();
                args.putInt("claimIndex", selectedClaim.index);

                Fragment fragment = new ClaimInfo();
                fragment.setArguments(args);

                Tools.replaceFragment(fragment, getFragmentManager(), true);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //nullify selected claim for good measure
                selectedClaim = null;


                Tools.replaceFragment(new ClaimInfo(), getFragmentManager(), true);
            }
        });


    }

    /**
     * Calls parse.com and retrieves all photos upload for the current user (Agent).
     * Copies the uploads into a local datastore.
     */
    public static void refreshLocalClaimData(Context context) {

        String userID = ParseUser.getCurrentUser().getString("AgentID");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Claim");
       // query.whereEqualTo("PolicyID", MainActivity.selectedPolicy);

        try {
            Singleton.setClaims(query.find());

            if (!Singleton.getClaims().isEmpty()) {

                updateListView(context);

            } else {

                Toast.makeText(context, "No Claims Found", Toast.LENGTH_LONG).show();
            }

        } catch (ParseException e) {

            Toast.makeText(context, "Parse.com Erro:" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Updates the listview from the local datastore
     */
    public static void updateListView(Context context) {

        ListView pictureList = (ListView) mainView.findViewById(R.id.my_uploads_list_view);
        ImageAdapter adapter = new ImageAdapter(context, null, null, Singleton.getClaims(), Singleton.CLAIM);
        pictureList.setAdapter(adapter);
    }

}
