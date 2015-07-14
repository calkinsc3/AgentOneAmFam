package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class PolicyInformationFragment extends Fragment {

    TextView client;
    EditText description;
    EditText cost;
    EditText address;
    EditText city;
    Spinner stateSpinner;
    EditText zip;
    CheckBox accepted;
    ImageButton saveButton;
    ImageButton addUpload;
    ListView photoView;
    ParseObject policy;
    LinearLayout address2;
    ObjectArrayAdapter adapter;

    public PolicyInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_policy_information, container, false);

        client = (TextView)view.findViewById(R.id.clientID);
        description = (EditText)view.findViewById(R.id.description);
        cost = (EditText)view.findViewById(R.id.cost);
        address = (EditText)view.findViewById(R.id.address);
        city = (EditText)view.findViewById(R.id.city);
        stateSpinner = (Spinner)view.findViewById(R.id.stateSpinner);
        zip = (EditText)view.findViewById(R.id.zip);
        photoView = (ListView)view.findViewById(R.id.photoList);
        saveButton =(ImageButton)view.findViewById(R.id.saveButton);
        addUpload = (ImageButton)view.findViewById(R.id.addUpload);
        policy = Singleton.getCurrentPolicy();
        accepted = (CheckBox)view.findViewById(R.id.accepted);
        address2 = (LinearLayout)view.findViewById(R.id.address2Layout);
        String[] states = getResources().getStringArray(R.array.states);

        client.append(policy.getString("ClientID"));
        description.setText(policy.getString("Description"));
        cost.setText(String.valueOf(policy.getNumber("Cost")));
        address.setText(policy.getString("Address"));
        city.setText(policy.getString("City"));
        zip.setText(String.valueOf(policy.getNumber("Zip")));
        accepted.setChecked(policy.getBoolean("Accepted"));

        stateSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, states));

        stateSpinner.setSelection(Arrays.asList(states).indexOf(policy.getString("State")));

        ParseQuery imageQuery = new ParseQuery("Upload");
        imageQuery.whereEqualTo("PolicyID", Singleton.getCurrentPolicy().getObjectId());

        try {
            Singleton.setMediaFiles((ArrayList<ParseObject>) imageQuery.find());
        } catch(com.parse.ParseException e) {
            Log.d("imageQuery: ", e.toString());
        }

        adapter = new ObjectArrayAdapter(getActivity(), R.layout.client_list_item, Singleton.getMediaFiles());
        photoView.setAdapter(adapter);

        checkOrientationSetLayoutOrientation();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject policyToSave = Singleton.getCurrentPolicy();

                policyToSave.put("AgentID", ParseUser.getCurrentUser().getObjectId());
                policyToSave.put("ClientID", Singleton.getCurrentClient().getObjectId());
                policyToSave.put("Address", address.getText().toString());
                policyToSave.put("City", city.getText().toString());
                policyToSave.put("State", stateSpinner.getSelectedItem().toString());
                policyToSave.put("Zip", zip.getText().toString());
                policyToSave.put("Description", description.getText().toString());
                policyToSave.put("Accepted", accepted.isChecked());
                policyToSave.put("Cost", Double.parseDouble(cost.getText().toString()));

                policyToSave.saveInBackground();
                Toast.makeText(getActivity(), "Policy Information Saved", Toast.LENGTH_SHORT).show();
            }
        });

        addUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent, "Select Pictures"), 2);
            }
        });

        return view;
    }

    public void checkOrientationSetLayoutOrientation(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            address2.setOrientation(LinearLayout.VERTICAL);
            city.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
            zip.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//        Singleton.getMediaFiles().add()

        super.onActivityResult(requestCode, resultCode, data);
    }

    public class ObjectArrayAdapter extends ArrayAdapter<ParseObject> {

        //declare Array List of items we create
        private ArrayList<ParseObject> images;


        /**
         * Constructor overrides constructor for array adapter
         * The only variable we care about is the ArrayList<PlatformVersion> objects
         * it is the list of the objects we want to display
         *
         * @param context The current context.
         * @param resource The resource ID for a layout file containing a layout to use when
         *                           instantiating views.
         * @param images The objects to represent in the ListView.
         */
        public ObjectArrayAdapter(Context context, int resource, ArrayList<ParseObject> images) {
            super(context, resource, images);
            this.images = images;
        }

        /**
         * Creates a custom view for our list View and populates the data
         *
         * @param position position in the ListView
         * @param convertView View to change to
         * @param parent the calling class
         * @return view the inflated view
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ViewHolder vHolder;

            /**
             * Checking to see if the view is null. If it is we must inflate the view
             * "inflate" means to render/show the view
             */

            if (view == null) {
                vHolder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.photo_display_item, null);
                vHolder.commentsText = (MultiAutoCompleteTextView) view.findViewById(R.id.CommentsText);
                vHolder.imageToUpload = (ImageView) view.findViewById(R.id.imageToUpload);

                view.setTag(vHolder);

            } else {
                vHolder = (ViewHolder)convertView.getTag();
            }

            vHolder.index = position;

            /**
             * Remember the variable position is sent in as an argument to this method.
             * The variable simply refers to the position of the current object on the list\
             * The ArrayAdapter iterate through the list we sent it
             */
            String url = images.get(position).getParseFile("Media").getUrl();

            if (url != null) {
                // obtain a reference to the widgets in the defined layout
                if (vHolder.imageToUpload != null) {
                    Picasso.with(getContext()).load(url).fit().centerInside().into(vHolder.imageToUpload);
                }
                if (vHolder.commentsText != null) {
                    vHolder.commentsText.setText(images.get(vHolder.index).getString("Comment"));
                }
            }

//            vHolder.commentsText.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    Holder.putInCommentsList(s.toString(), vHolder.index);
//                }
//            });



            // view must be returned to our current activity
            return view;
        }

        private class ViewHolder {
            MultiAutoCompleteTextView commentsText;
            ImageView imageToUpload;
            int index;
        }
    }

}
