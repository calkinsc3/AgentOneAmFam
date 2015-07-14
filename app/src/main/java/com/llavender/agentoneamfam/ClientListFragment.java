package com.llavender.agentoneamfam;


import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ClientListFragment extends Fragment {

    private ListView clientListView;


    public ClientListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_client_list, container, false);

        clientListView = (ListView)view.findViewById(R.id.clientListView);
        ObjectArrayAdapter adapter = new ObjectArrayAdapter(getActivity(), R.layout.client_list_item, Singleton.getListOfClients());
        clientListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_input_add);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d("onOptionsItemSelected", "yes");
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

        //declare Array List of items we create
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
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.client_list_item, null);
                vHolder.clientName = (TextView)view.findViewById(R.id.clientName);
                vHolder.addPolicyButton = (ImageView)view.findViewById(R.id.addPolicyButton);
                vHolder.policyListView = (ListView)view.findViewById(R.id.policyList);

                view.setTag(vHolder);
            } else {
                vHolder = (ViewHolder) convertView.getTag();
            }

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Policy");
            query.whereEqualTo("ClientID", Singleton.getListOfClients().get(position).getObjectId());
            vHolder.policies = new ArrayList<>();
            vHolder.policyDescriptions = new ArrayList<>();
            List<ParseObject> policyObjects = new ArrayList<>();

            try {
                policyObjects = query.find();
                vHolder.policies = (ArrayList<ParseObject>)policyObjects;
            } catch (com.parse.ParseException e){
                Log.d("parse e: ", e.toString());
            }
            for(int i = 0; policyObjects.size() > i; i++){
                vHolder.policyDescriptions.add(policyObjects.get(i).getString("Description"));
            }
            vHolder.index = position;

            /**
             * Remember the variable position is sent in as an argument to this method.
             * The variable simply refers to the position of the current object on the list\
             * The ArrayAdapter iterate through the list we sent it
             */
            final ParseObject client = clients.get(position);

            if (client != null) {
                // obtain a reference to the widgets in the defined layout
                TextView clientName = vHolder.clientName;
                ImageView addPolicyButton = vHolder.addPolicyButton;
                ListView policyList = vHolder.policyListView;
                ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, vHolder.policyDescriptions);

                if(clientName != null){
                    clientName.setText(client.getString("Name"));
                    clientName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Singleton.setCurrentClient(client);
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new ClientInformationFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                }
                if(policyList != null){
                    policyList.setAdapter(adapter);

                    policyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //start the Policy Information Fragment
                            Singleton.setCurrentPolicy(vHolder.policies.get(position));
                            fm.beginTransaction().replace(R.id.fragment_container, new PolicyInformationFragment())
                                .addToBackStack(null)
                                .commit();
                        }
                    });
                }
                if (addPolicyButton != null) {
                    addPolicyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Start add client fragment
                            Singleton.setCurrentClient(client);
                            fm.beginTransaction().replace(R.id.fragment_container, new AddPolicyFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                }
            }

            // view must be returned to our current activity
            return view;
        }

        public class ViewHolder {
            TextView clientName;
            ImageView addPolicyButton;
            ListView policyListView;
            ArrayList<String> policyDescriptions;
            ArrayList<ParseObject> policies;
            int index;
        }
    }

}
