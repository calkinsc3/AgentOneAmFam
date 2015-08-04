package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClientListFragment extends Fragment {

    private ListView clientListView;
    private FrameLayout frameLayout;
    private ObjectArrayAdapter adapter;

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();
            int totalItemsHeight = 0;

            for (int i = 0; i < numberOfItems; i++) {
                View item = listAdapter.getView(i, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_list, container, false);

        clientListView = (ListView)view.findViewById(R.id.clientListView);

        frameLayout = (FrameLayout) view.findViewById(R.id.progress_frame);

        frameLayout.setVisibility(View.VISIBLE);
        frameLayout.setBackgroundColor(Color.argb(160, 0, 0, 0));
        queryParseForClients();

        return view;
    }

    private void setListAdapter(){
        adapter = new ObjectArrayAdapter(getActivity(), R.layout.client_list_item, Singleton.getListOfClients());
        clientListView.setAdapter(adapter);
    }

    private void queryParseForClients(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereEqualTo("accountType", "Client");
        query.whereEqualTo("AgentID", ParseUser.getCurrentUser().getObjectId());

        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                frameLayout.setVisibility(View.GONE);
                if (e == null) {
                    Singleton.setListOfClients((ArrayList<ParseUser>) objects);
                    setListAdapter();
                } else {
                    Log.d("loadUser Exception", e.toString());
                    Toast.makeText(getActivity(), "Error from Parse", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setTitle("Add Client");
        menu.findItem(R.id.action_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_input_add);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //is actually a call to create a new Client
            case R.id.action_save:
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddClientFragment())
                        .addToBackStack(null)
                        .commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ObjectArrayAdapter extends ArrayAdapter<ParseObject> {
        private ArrayList<ParseObject> clients;

        /**
         * Constructor overrides constructor for array adapter
         * The only variable we care about is the ArrayList<PlatformVersion> objects
         * it is the list of the objects we want to display
         *
         * @param context The current context.
         * @param resource The resource ID for a layout file containing a layout to use when
         *                           instantiating views.
         * @param clients The objects to represent in the ListView.
         */
        public ObjectArrayAdapter(Context context, int resource, ArrayList clients) {
            super(context, resource, clients);
            this.clients = clients;
        }

        /**
         * Creates a custom view for our list View and populates the data
         *
         * @param position position in the ListView
         * @param convertView the view to be inflated
         * @param parent the parent view
         * @return the view created
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder vHolder;
            final FragmentManager fm = getFragmentManager();

            /**
             * Checking to see if the view is null. If it is we must inflate the view
             * "inflate" means to render/show the view
             */
            if (view == null) {
                vHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater)
                        getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.client_list_item, null);

                vHolder.clientName = (TextView)view.findViewById(R.id.clientName);
                vHolder.addPolicyButton = (ImageView)view.findViewById(R.id.addPolicyButton);
                vHolder.policyListView = (ListView)view.findViewById(R.id.policyList);
                vHolder.policies = new ArrayList<>();
                vHolder.hasCheckedPolicies = false;

                view.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Policy");
            query.whereEqualTo("ClientID", Singleton.getListOfClients().get(position).getObjectId());

            vHolder.policyDescriptions = new ArrayList<>();

            vHolder.index = position;

            if(!vHolder.hasCheckedPolicies) {
                //try {
                vHolder.hasCheckedPolicies = true;
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        vHolder.policies.clear();
                        if (list != null) {
                            vHolder.policies.addAll(list);

                        } else {
                            Log.d("DIB", "Null list");
                        }

                        for(int i = 0; vHolder.policies.size() > i; i++){
                            vHolder.policyDescriptions.add(vHolder.policies.get(i).getString("Description"));
                        }

                        final ParseObject client = clients.get(vHolder.index);

                        if (client != null) {
                            // obtain a reference to the widgets in the defined layout
                            TextView clientName = vHolder.clientName;
                            ImageView addPolicyButton = vHolder.addPolicyButton;

                            ListView policyList = vHolder.policyListView;
                            ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                                    android.R.layout.simple_list_item_1, vHolder.policyDescriptions);

                            //vHolder.innerAdapter = adapter;

                            if(clientName != null){
                                clientName.setText(client.getString("Name"));
                                clientName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Singleton.setCurrentClient(client);
                                        getFragmentManager().beginTransaction()
                                                .replace(R.id.fragment_container,
                                                        new ClientInformationFragment())
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                });
                            }

                            if(policyList != null){
                                policyList.setAdapter(adapter);
                                setListViewHeightBasedOnItems(policyList);

                                policyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        // Start the Policy Information Fragment
                                        Singleton.setCurrentPolicy(vHolder.policies.get(position));
                                        fm.beginTransaction().replace(R.id.fragment_container,
                                                new PolicyInformationFragment())
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                });
                            }

                            if (addPolicyButton != null) {
                                addPolicyButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Start add client fragment
                                        Singleton.setCurrentClient(client);
                                        fm.beginTransaction().replace(R.id.fragment_container,
                                                new AddPolicyFragment())
                                                .addToBackStack(null)
                                                .commit();
                                    }
                                });
                            }
                        }
                    }
                });
            }

            /**
             * Remember the variable position is sent in as an argument to this method.
             * The variable simply refers to the position of the current object on the list\
             * The ArrayAdapter iterate through the list we sent it
             */

            return view;
        }

        public class ViewHolder {
            TextView clientName;
            ImageView addPolicyButton;
            ListView policyListView;
            boolean hasCheckedPolicies = true;

            ArrayList<String> policyDescriptions;
            ArrayList<ParseObject> policies;

            int index;
        }
    }
}