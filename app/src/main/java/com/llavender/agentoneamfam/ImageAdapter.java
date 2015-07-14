package com.llavender.agentoneamfam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Custom List Adapter
 * <p/>
 * * handles images and policies
 */
public class ImageAdapter extends BaseAdapter {

    //CLASS VARIABLES
    private Context context;
    private java.util.List<Object> images;
    private List<String> comments;
    private List<ParseObject> objects;
    private int mode;

    /**
     * General Constructor
     *
     * @param context  context of the list to be populated
     * @param images   list of URIs of images
     * @param comments list of Comments associated with each URI
     * @param objects  list of objects (either clients or policies) **CLIENT and POlICY only**
     * @param mode     IMAGE, CLIENT, POLICY
     */
    public ImageAdapter(Context context, List<Object> images, List<String> comments, List<ParseObject> objects, int mode) {
        this.context = context;
        this.images = images;
        this.mode = mode;
        this.comments = comments;
        this.objects = objects;
    }

    public int getCount() {


        if (mode >= Singleton.CLIENT) {
            return objects.size();
        } else {
            return images.size();
        }

    }

    public Object getItem(int position) {

        if (mode >= Singleton.CLIENT) {
            return objects.get(position);
        } else {
            return images.get(position);
        }

    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder vh;
        View view = convertView;

        switch (mode) {


            /**
             *
             * CLAIM MODE
             */
            case Singleton.CLAIM:

                //INFLATE VIEWS AND SET UP VIEW HOLDER
                if (view == null) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.info_list_item, null);

                    vh = new ViewHolder();
                    vh.textView = (TextView) view.findViewById(R.id.info_list_item_text);
                    vh.delete_button = (ImageButton) view.findViewById(R.id.delete_button);

                    view.setTag(vh);


                } else {
                    vh = (ViewHolder) convertView.getTag();
                }

                //GET THE INFO TO DISPLAY BASED ON MODE
                String info = Tools.buildMessage(objects.get(position), mode);

                vh.index = position;

                final ViewHolder vhf2 = vh;


                //SET THE TEXT
                vh.textView.setText(info);

                /**
                 * DELETE object
                 */
                vh.delete_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {

                                    //CANCEL
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        dialog.dismiss();

                                        break;

                                    //DELETE IMAGE
                                    case DialogInterface.BUTTON_POSITIVE:
                                        dialog.dismiss();

                                        ParseObject obj = Singleton.getClaims().get(vhf2.index);
                                        //DELETE IN BACKGROUND
                                        obj.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                if (e == null) {
                                                    Toast.makeText(context, "Claim deleted!", Toast.LENGTH_SHORT).show();
                                                    Claims.refreshLocalClaimData(context);
                                                } else {
                                                    Toast.makeText(context, "Claim not deleted:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                }

                            }
                        };

                        //DIALOG USED FOR DELETE/IMAGE CHANGE SELECTION
                        AlertDialog confirmation = new AlertDialog.Builder(context)
                                .setTitle("Are you sure you want to delete this Claim?")
                                .setCancelable(true)
                                .setNegativeButton("Cancel", dialogClick)
                                .setPositiveButton("Delete", dialogClick)
                                .show();
                    }
                });

                break;


            //CASE OF UPLOAD PHOTOS or MY UPLOADS
            case Singleton.IMAGE:

                view = convertView;

                //INFLATE VIEWS AND SET UP VIEW HOLDER
                if (view == null) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.photo_list_item, null);

                    vh = new ViewHolder();
                    vh.editText = (EditText) view.findViewById(R.id.editText);
                    vh.imageButton = (ImageButton) view.findViewById(R.id.imageView);
                    vh.delete_button = (ImageButton) view.findViewById(R.id.delete_button);

                    view.setTag(vh);


                } else {
                    vh = (ViewHolder) convertView.getTag();
                }

                vh.index = position;
                final ViewHolder vhf = vh;

                //URI OF IMAGE TO BE LOADED
                String imageUri = images.get(position).toString();

                //TODO image resizing
                //LOAD THE IMAGE WITH PICASSO LIBRARY
                Picasso.with(context)
                        .load(imageUri)
                        .resize(500, 500)
                        .into(vh.imageButton);

                //TEXT LISTENER THAT HANDLES LOCAL COMMENT UPDATES
                vh.editText.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Singleton.getComments().set(vhf.index, s.toString());
                    }
                });

                /**
                 * DELETE object
                 */
                vh.delete_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        //TODO move to class
                        DialogInterface.OnClickListener dialogClick = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                ParseObject obj = Singleton.getUploads().get(vhf.index);

                                switch (which) {

                                    //CANCEL
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        dialog.dismiss();

                                        break;

                                    //DELETE IMAGE
                                    case DialogInterface.BUTTON_POSITIVE:
                                        dialog.dismiss();

                                        //DELETE IN BACKGROUND
                                        obj.deleteInBackground(new DeleteCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                if (e == null) {
                                                    Toast.makeText(context, "Photo deleted!", Toast.LENGTH_SHORT).show();
                                                    MyUploads.refreshLocalData(context);
                                                } else {
                                                    e.printStackTrace();
                                                    Toast.makeText(context, "Photo not deleted.", Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                }

                            }
                        };

                        //DIALOG USED FOR DELETE/IMAGE CHANGE SELECTION
                        AlertDialog confirmation = new AlertDialog.Builder(context)
                                .setTitle("Are you sure you want to delete the image?")
                                .setCancelable(true)
                                .setNegativeButton("Cancel", dialogClick)
                                .setPositiveButton("Delete", dialogClick)
                                .show();

                    }


                });


                /**
                 * CHANGE IMAGE
                 */
                vh.imageButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        MyUploads.tempObject = null;

                        //TODO
                        //set global var
                        MyUploads.tempObject = Singleton.getUploads().get(vhf.index);

                        //INTENT FOR MOVING TO GALLERY
                        Intent intent = new Intent()
                                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                                .setType("image/*")
                                .setAction(Intent.ACTION_GET_CONTENT);

                        //WILL START A CHOOSER ACTIVITY WITH GALLERY AND OTHER OPTIONS IN IT
                        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select new picture."), MyUploads.CHANGE_IMAGE);

                        return true;
                    }
                });

                //SET THE COMMENT
                if (comments != null) {
                    vh.editText.setText(comments.get(vh.index));
                }

                break;

            case Singleton.MEETING:

                ParseObject curr = objects.get(position);

                //INFLATE VIEWS AND SET UP VIEW HOLDER
                if (convertView == null) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.info_list_item, null);

                    vh = new ViewHolder();
                    vh.textView = (TextView) view.findViewById(R.id.info_list_item_text);
                    vh.delete_button = (ImageButton) view.findViewById(R.id.delete_button);
                    vh.parseObject = curr;
                    view.setTag(vh);
                } else {
                    vh = (ViewHolder) convertView.getTag();
                    curr = vh.parseObject;
                }

                String temp = Tools.buildMessage(objects.get(position), Singleton.MEETING);
                vh.index = position;
                //SET THE TEXT
                vh.textView.setText(temp);

                //DELETE CLICK LISTENER
                final ViewHolder vhf4 = vh;
                vh.delete_button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        final ProgressDialog progressDialog = ProgressDialog.show(context, "", "", true);

                        vhf4.parseObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {

                                progressDialog.dismiss();

                                if (e == null) {
                                    Toast.makeText(context, "Appointment Deleted.", Toast.LENGTH_SHORT).show();
                                    MeetingListFragment.updateList();
                                } else {
                                    Toast.makeText(context, "Unable to Delete Appointment.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }

                            }
                        });

                    }
                });
                break;

            default:
                //INFLATE VIEWS
                if (convertView == null) {

                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.info_list_item, null);
                }
                break;
        }

        return view;

    }


    /**
     * Custom ViewHolder class
     */
    static class ViewHolder {

        ImageButton imageButton;
        EditText editText;
        TextView textView;
        int index;
        ImageButton delete_button;
        ParseObject parseObject;


    }

}