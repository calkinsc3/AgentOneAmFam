package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


/**
 * Dual purpose fragment for Policy List and Appointment List
 */
public class MeetingListFragment extends Fragment {

    //MODE TYPES
    public static final int POLICIES = 0;
    public static final int APPOINTMENTS = 1;

    //RESPECTIVE OBJECT HOLDERS
    public static ParseObject selectedAppointment;
    public static ParseObject selectedClient;

    public static List<ParseObject> meetings;

    // POLICIES OR APPOINTMENTS
    public static int mode = -1;

    //REQUIRED FOR UPDATING LIST
    private static View view;
    private static Context context;
    private ListView listView;

    public MeetingListFragment() {
        // Required empty public constructor
    }


    /**
     * Queries parse and populates the listview
     * NEEDS TO REMAIN STATIC: it is called from ImageAdapter
     */
    public static void updateList() {

        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "Retrieving data from Parse.com", true);
        final ListView listView = (ListView) view.findViewById(R.id.clientListView);
        SharedPreferences prefs = context.getSharedPreferences(Singleton.PREFERENCES, 0);
        ParseQuery<ParseObject>  query = ParseQuery.getQuery("Meeting");

        //if this fragment is being inflated withing clientInformation, set the query to filter as such
        if (selectedClient != null) {
            query.whereEqualTo("InvitedIDs", selectedClient.getObjectId());
        }


        //TODO update to work only for this agent
        //query.whereEqualTo("AgentID" ,  prefs.getString("OfficeUserID", null));


        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException e) {

                progressDialog.dismiss();

                if (e == null && !list.isEmpty()) {

                    meetings = list;
                    listView.setAdapter(new ImageAdapter(context, null, null, meetings, Singleton.MEETING));

                } else if (e == null) {
                    switch (mode) {

                        case POLICIES:
                            Toast.makeText(context, "No Policies Found.", Toast.LENGTH_SHORT).show();
                            break;

                        case APPOINTMENTS:
                            Toast.makeText(context, "No Appointments Found.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Toast.makeText(context, "Error from parse:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            selectedClient = Singleton.getCurrentClient();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_client_list, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        listView = (ListView) view.findViewById(R.id.clientListView);

        //Generate ListView
        MeetingListFragment.view = view;
        context = getActivity();

        //reset appointment holder
        selectedAppointment = null;

        updateList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageAdapter.ViewHolder vh = (ImageAdapter.ViewHolder) view.getTag();

                selectedAppointment = meetings.get(vh.index);
                Tools.replaceFragment(new EditAppointment(), getFragmentManager(), true);

            }
        });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_add);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                //move to new meeting
                Tools.replaceFragment(new EditAppointment(), getFragmentManager(), true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
