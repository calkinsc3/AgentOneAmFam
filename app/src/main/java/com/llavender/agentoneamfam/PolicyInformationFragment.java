package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


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
    Boolean saved;
    ImageButton saveButton;
    ImageButton addUpload;
    ListView photoView;
    ParseObject policy;
    LinearLayout address2;
    ArrayList<ParseObject> images;
    ArrayList<String> comments;
    ObjectArrayAdapter mediaAdapter;

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
        saved = false;
        saveButton =(ImageButton)view.findViewById(R.id.saveButton);
        addUpload = (ImageButton)view.findViewById(R.id.addUpload);
        policy = Singleton.getCurrentPolicy();
        accepted = (CheckBox)view.findViewById(R.id.accepted);
        address2 = (LinearLayout)view.findViewById(R.id.address2Layout);
        comments = new ArrayList<>();
        String[] states = getResources().getStringArray(R.array.states);
        images = new ArrayList<>();

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

        mediaAdapter = new ObjectArrayAdapter(getActivity(), R.layout.client_list_item, Singleton.getMediaFiles());
        photoView.setAdapter(mediaAdapter);

        checkOrientationSetLayoutOrientation();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject policyToSave = Singleton.getCurrentPolicy();

                policyToSave.put("Address", address.getText().toString());
                policyToSave.put("City", city.getText().toString());
                policyToSave.put("State", stateSpinner.getSelectedItem().toString());
                policyToSave.put("Zip", zip.getText().toString());
                policyToSave.put("Description", description.getText().toString());
                policyToSave.put("Accepted", accepted.isChecked());
                policyToSave.put("Cost", Double.parseDouble(cost.getText().toString()));

                policyToSave.saveInBackground();
                Toast.makeText(getActivity(), "Policy Information Saved", Toast.LENGTH_SHORT).show();

                for (int i = 0; Singleton.getMediaFiles().size() > i; i++){
                    ParseObject currFile = Singleton.getMediaFiles().get(i);
                    currFile.put("Comment", comments.get(i));
                    try {
                        currFile.save();
                    } catch(com.parse.ParseException e){
                        Log.d("save: ", e.toString());
                    }
                }
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

        Log.d("result code:", String.valueOf(resultCode));

        if (resultCode == getActivity().RESULT_OK) {
            ClipData clipData = data.getClipData();
            Uri targetUri;
            images.clear();

            if (clipData != null) {
                for (int i = 0; clipData.getItemCount() > i; i++) {
                    try {
                        ParseObject upload = new ParseObject("Upload");
                        ParseFile image = new ParseFile("photo.jpg", Tools.readBytes(getActivity(),
                                clipData.getItemAt(i).getUri()), "jpeg");

                        upload.put("PolicyID", Singleton.getCurrentPolicy().getObjectId());
                        upload.put("UserID", ParseUser.getCurrentUser().getObjectId());
                        upload.put("Media", image);

                        //add to images array
                        images.add(upload);
                    } catch (Exception e) {
                        Log.d("Load Multiple: ", e.toString());
                    }
                }
            } else {
                //get target Uri
                targetUri = data.getData();

                try {
                    ParseObject upload = new ParseObject("Upload");
                    ParseFile image = new ParseFile("photo.jpg", Tools.readBytes(getActivity(),
                            targetUri), "jpeg");

                    upload.put("PolicyID", Singleton.getCurrentPolicy().getObjectId());
                    upload.put("UserID", ParseUser.getCurrentUser().getObjectId());
                    upload.put("Media", image);
                    upload.put("Comment", "");



                    images.add(upload);
                } catch (Exception e) {
                    Log.d("Load Single: ", e.toString());
                }
            }

            ParseObject.saveAllInBackground(images, new SaveCallback() {

                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        if(Singleton.getMediaFiles().size() > 0){
                            Singleton.getMediaFiles().addAll(images);
                        } else {
                            Singleton.setMediaFiles(images);
                        }
                        HashSet<ParseObject> hashMedia = new HashSet<>(Singleton.getMediaFiles());
                        ArrayList<ParseObject> media = new ArrayList<>(hashMedia);

                        ObjectArrayAdapter secondaryAdapter = new ObjectArrayAdapter(getActivity(), R.layout.client_list_item, media);
                        photoView.setAdapter(secondaryAdapter);

//                        mediaAdapter.notifyDataSetChanged();
//                        images.clear();
                    } else {
                        Log.d("Save Error: ", e.toString());
                    }
                }
            });

            super.onActivityResult(requestCode, resultCode, data);
        }
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

            if(position >= comments.size()){
                comments.add("");
            }

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

            vHolder.commentsText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    comments.set(vHolder.index, s.toString());
                }
            });



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
