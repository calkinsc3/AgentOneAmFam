package com.llavender.agentoneamfam;

import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyUploads extends Fragment {

    //REQUEST CODES FOR GALLERY INTENT RETURN
    final public static int NEW_UPLOAD = 0;
    final public static int CHANGE_IMAGE = 1;

    //Holds the Upload Object that the image is being changed on
    public static ParseObject tempObject = null;
    public static Fragment fragment;

    //This fragments main view
    private static View mainView = null;

    /**
     * These fields are used for the MyUploads fragment, within the ClaimsInfo fragment
     * they will all be null unless this fragment is withing the ClaimsInfo fragment
     */
    //Holds uploadIDs of the Claim object that this fragment was called from
    private static ArrayList<String> uploadIDs;
    //Holds the policyID of the claim that this fragment was called from
    private static String claimPolicyID;
    //Arguments passed to this fragment
    private static Bundle args;

    /**
     * Calls parse.com and retrieves all photos upload for the current user (Agent).
     * Copies the uploads into a local datastore.
     */
    public static void refreshLocalData(Context context) {
        String userID = ParseUser.getCurrentUser().getObjectId();

        //USED FOR QUERYING UPLOADS SPECIFIC TO A CLAIM (null for creating a new claim)
        ParseQuery<ParseObject> mainQuery = null;

        //Build a query for each upload found in the uploadIDs of the Current Claim
        if(uploadIDs != null && args != null){
            //get list from arguments
            List<ParseQuery<ParseObject>> queries = new ArrayList<>();

            for(int i = 0; i < MyUploads.uploadIDs.size(); i++){
                queries.add(new ParseQuery<>("Upload").whereEqualTo("objectId", MyUploads.uploadIDs.get(i)));
            }

            if(!queries.isEmpty()) {
                mainQuery = ParseQuery.or(queries);
            }

        } else if (uploadIDs == null && args != null){
            mainQuery = new ParseQuery<>("Upload").whereEqualTo("PolicyID", Singleton.getCurrentPolicy().getObjectId());
        }
        //MY UPLOADS: add a single query to mainQuery
        else{
            mainQuery = ParseQuery.getQuery("Upload")
                    .whereEqualTo("UserID", userID);
        }

        //IF A QUERY WAS BUILT (wont enter this code of creating a new Claim)
        if(mainQuery != null) {
            //only get files that have media uploaded
            mainQuery.whereExists("Media");

            try {
                //TODO this blocks the UI thread so we must add a loading dialog or take off the UI thread
                //run the query and set the uploads List to the result
                Singleton.setUploads(mainQuery.find());

                //Build the local backend list with the uploads retrieved
                if (!Singleton.getUploads().isEmpty()) {
                    Singleton.setImages(new ArrayList<>());
                    Singleton.setComments(new ArrayList<String>());

                    for (int i = 0; i < Singleton.getUploads().size(); i++) {
                        ParseObject object = Singleton.getUploads().get(i);
                        ParseFile parseFile = object.getParseFile("Media");

                        Object obj = parseFile.getUrl();

                        String comm = object.getString("Comment");

                        Singleton.getComments().add(comm);
                        Singleton.getImages().add(obj);
                    }

                    updateListView(context);
                } else {
                    Toast.makeText(context, "No Uploads Found.", Toast.LENGTH_LONG).show();
                }
            } catch (ParseException e) {
                Toast.makeText(context, "Parse.com Uploads retrieval failed.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the listview from the singleton backend
     */
    public static void updateListView(Context context) {
        ListView pictureList = (ListView) MyUploads.mainView.findViewById(R.id.my_uploads_list_view);
        ImageAdapter adapter = new ImageAdapter(context, Singleton.getImages(),
                Singleton.getComments(), null, Singleton.IMAGE);
        pictureList.setAdapter(adapter);
    }

    /**
     * Loops through the Comments List (Singleton Backend) and updates the comments in the
     * ParseObject in the concurrent uploads List (Singleton Backend).
     *
     * Once all the uploads have been updated with new comments,
     * save them all to Parse
     *
     * @param context the activity that this fragment belongs to
     */
    public static void saveComments(final Context context){
        ParseObject photo;

        if(Singleton.getComments() != null) {
            //only updates comments
            for (int i = 0; i < Singleton.getComments().size(); i++) {
                photo = Singleton.getUploads().get(i);
                photo.put("Comment", Singleton.getComments().get(i));
            }

            ParseObject.saveAllInBackground(Singleton.getUploads(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(context, "Comments Saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * The args field will contain arguments in these two cases:
         *
         * ClaimsInfo
         * new Claim (ClaimsInfo)
         *
         * and null for: MyUploads
         */
        args = getArguments();
        fragment = this;

        if (this.getArguments() == null) {
            uploadIDs = null;
            claimPolicyID = null;

        } else {
            uploadIDs = args.getStringArrayList("UploadIDs");
            claimPolicyID = args.getString("claimPolicyID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_uploads, container, false);
    }

    /**
     * Handles initial listview population and Listeners for save/delete/change image
     */
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Set mainView
        mainView = view;

        //set the background properly
        if(args == null){

            mainView.setBackground(getResources().getDrawable(R.drawable.clouds));
        }
        else{
            mainView.setBackgroundColor(getResources().getColor(R.color.cloudy_white));

        }

        final TextView header = (TextView) view.findViewById(R.id.title);
        final ImageButton add_button = (ImageButton) view.findViewById(R.id.add_button);
        final com.github.clans.fab.FloatingActionButton fab =
                (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.fab);

        //set fab icon, set title, show fab
        if (args != null) {
            //show top side imageButton for save if on claimsInfo
            add_button.setVisibility(View.VISIBLE);
        } else {
            //show fab if on Claims
            fab.setVisibility(View.VISIBLE);
            fab.setImageResource(android.R.drawable.ic_menu_save);
        }

        //Set text of the Fragment Title
        header.setText("My Uploads");

        refreshLocalData(getActivity());

        /**
         * Fires a Gallery intent to select new pictures to add to Uploads.
         *
         * This button is only visible when inside ClaimsInfo fragment.
         *
         * Activity Request Code is set to NEW_UPLOAD.
         */
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //INTENT FOR MOVING TO GALLERY
                Intent intent = new Intent()
                        .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                //WILL START A CHOOSER ACTIVITY WITH GALLERY AND OTHER OPTIONS IN IT
                MyUploads.fragment.startActivityForResult(Intent
                        .createChooser(intent, "Select Picture(s)"), NEW_UPLOAD);
            }
        });

        /**
         * Save the comments.
         *
         * This button is only visible on the MyUploads screen.
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComments(getActivity());
            }
        });
    }

    /**
     *Handles return from image selection.  Updates parse and refreshes the local datastore.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //USER IS CHANGING A PREVIOUS UPLOAD
            case CHANGE_IMAGE:
                //CHECK FOR VALID RESULT
                if (resultCode == Activity.RESULT_OK) {
                    //HOLDS FINAL IMAGE BYTE ARRAY
                    byte[] imageByte = null;

                    //CONVERT IMAGE TO BYTE ARRAY
                    try {
                        imageByte = Tools.readBytes(getActivity(), data.getData());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (imageByte != null) {
                        //TODO do i need both the save and put + save
                        ParseFile image = new ParseFile("photo.jpeg", imageByte, "jpeg");
                        image.saveInBackground();

                        tempObject.put("Media", image);

                        tempObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Toast.makeText(getActivity(),
                                            "Photo Updated!", Toast.LENGTH_SHORT).show();
                                    refreshLocalData(getActivity());
                                } else {
                                    Toast.makeText(getActivity(),
                                            "Update Cancelled.", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } else {
                    //TELLS THE USER IF THE IMAGE THEY SELECTED WAS NOT RETRIEVED
                    Toast.makeText(getActivity(), "Photo could not be accessed.", Toast.LENGTH_SHORT).show();
                }

                break;

            //USER IS ADDING A NEW UPLOAD
            case NEW_UPLOAD:
                //HOLDS FINAL IMAGE BYTE ARRAY
                List<byte[]> imageByte = new ArrayList<>();
                //boolean true if running from policy

                try{
                    //BUILD LIST OF NEW IMAGES
                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            imageByte.add(Tools.readBytes(getActivity(), clipData.getItemAt(i).getUri()));
                        }
                    } else {
                        imageByte.add(Tools.readBytes(getActivity(), data.getData()));
                    }

                    //BUILD LIST OF NEW PARSE OBJECTS
                    List<ParseObject> toSave = new ArrayList<>();

                    for(byte[] curr : imageByte){
                        final ParseObject obj = new ParseObject("Upload");
                        ParseFile image = new ParseFile("photo.jpeg", curr, "jpeg");

                        image.saveInBackground();

                        obj.put("PolicyID", claimPolicyID);

                        if(!args.getBoolean("FROMPOLICY")) {
                            obj.put("ClaimID", ClaimInfo.selectedClaim.getObjectId());
                        }

                        obj.put("UserID", ParseUser.getCurrentUser().getObjectId());
                        obj.put("Media", image);

                        obj.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    refreshLocalData(getActivity());

                                    final String objectID = obj.getObjectId();

                                    if (!getArguments().getBoolean("FROMPOLICY", false)) {
                                        uploadIDs.add(objectID);

                                        for (int x = 0; x < uploadIDs.size(); x++)
                                            if (uploadIDs.get(x) == null)
                                                uploadIDs.remove(x);

                                        JSONArray jsonArray = new JSONArray(uploadIDs);
                                        Log.i("UPLOAD", jsonArray.toString());

                                        ClaimInfo.selectedClaim.put("UploadIDs", jsonArray);
                                        ClaimInfo.selectedClaim.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Toast.makeText(getActivity(),
                                                            "Upload " + objectID + " saved.",
                                                            Toast.LENGTH_SHORT).show();
                                                    refreshLocalData(getActivity());
                                                } else {
                                                    Toast.makeText(getActivity(), e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(getActivity(), e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        toSave.add(obj);

                        if(!args.getBoolean("FROMPOLICY")) {
                            uploadIDs.add(obj.getObjectId());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}