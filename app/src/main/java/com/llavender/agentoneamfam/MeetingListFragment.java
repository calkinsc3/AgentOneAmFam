package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
    public static ParseObject selectedPolicy;
    public static ParseObject selectedAppointment;

    // POLICIES OR APPOINTMENTS
    public static int mode = -1;

    //REQUIRED FOR UPDATING LIST
    private static View view;
    private static Context context;

    public MeetingListFragment() {
        // Required empty public constructor
    }

    /**
     * Queries parse and populates the listview
     */
    public static void updateList() {

        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "Retrieving data from Parse.com", true);
        final ListView listView = (ListView) view.findViewById(R.id.policies_list_view);
        SharedPreferences prefs = context.getSharedPreferences(Singleton.PREFERENCES, 0);
        ParseQuery<ParseObject> query;

        //SET QUERY FROM MODE
        if (mode == POLICIES) {
            query = ParseQuery.getQuery("Policy");
        } else if (mode == APPOINTMENTS) {
            query = ParseQuery.getQuery("Meeting");
        } else {
            query = null;
        }

        //TODO update to work only for this agent
        //query.whereEqualTo("AgentID" ,  prefs.getString("OfficeUserID", null));

        if (query != null) {
            query.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    progressDialog.dismiss();

                    if (e == null && !list.isEmpty()) {
                        listView.setAdapter(new CustomAdapter(context, list, mode));

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {

        final ListView listView = (ListView) view.findViewById(R.id.policies_list_view);

        //Generate ListVie
        MeetingListFragment.view = view;
        context = getActivity();
        updateList();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomAdapter.ViewHolder vh = (CustomAdapter.ViewHolder) view.getTag();

                switch (mode) {
                    case APPOINTMENTS:
                        selectedAppointment = vh.parseObject;
                        Tools.replaceFragment(new EditAppointment(), getFragmentManager(), true);
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
       // MenuItem add_button = menu.findItem(R.id.add_policy);

        //`make add button visible
        //add_button.setVisible(true);

    }
}
