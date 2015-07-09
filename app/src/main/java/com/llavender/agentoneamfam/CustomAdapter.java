package com.llavender.agentoneamfam;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.List;

/**
 * Custom adapter to handle policies and appointments
 */
public class CustomAdapter extends BaseAdapter {
    public static final int POLICIES = 0;
    public static final int APPOINTMENTS = 1;


    //CLASS VARIABLES
    private Context context;
    private List<ParseObject> policies;

    private int mode;


    public CustomAdapter(Context context, List<ParseObject> policies, int mode) {
        this.context = context;
        this.policies = policies;
        this.mode = mode;
    }

    public int getCount() {

        return policies.size();

    }

    public Object getItem(int position) {

        return policies.get(position);

    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, final View convertView, ViewGroup parent) {

        ViewHolder vh;
        View view = convertView;
        ParseObject curr = policies.get(position);

        //INFLATE VIEWS AND SET UP VIEW HOLDER
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.info_list_item, null);

            vh = new ViewHolder();
            vh.textView = (TextView) view.findViewById(R.id.info_list_item_text);
            vh.delete = (ImageButton) view.findViewById(R.id.delete_button);
            vh.parseObject = curr;
            view.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
            curr = vh.parseObject;
        }


        //BUILD INFO STRING
        String info = "";
        switch (mode) {

            case POLICIES:
                info = curr.getString("Description") + "\n" + curr.getString("Address") + "\n" + curr.getString("City");
                break;

            case APPOINTMENTS:
                info = curr.getString("Type") + "\n";
                info += "Starts: " + curr.getDate("StartDate") + "\n";
                info += "Ends: " + curr.getDate("EndDate") + "\n";
                info += curr.getString("Comment");
                break;

        }

        //SET THE TEXT
        vh.textView.setText(info);

        //DELETE CLICK LISTENER
        final ViewHolder vhf = vh;
        vh.delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = ProgressDialog.show(context, "", "", true);

                vhf.parseObject.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {

                        progressDialog.dismiss();

                        switch (mode) {

                            case POLICIES:
                                if (e == null) {
                                    Toast.makeText(context, "Policy Deleted.", Toast.LENGTH_SHORT).show();
                                    MeetingListFragment.updateList();
                                } else {
                                    Toast.makeText(context, "Unable to Delete Policy.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                                break;

                            case APPOINTMENTS:
                                if (e == null) {
                                    Toast.makeText(context, "Appointment Deleted.", Toast.LENGTH_SHORT).show();
                                    MeetingListFragment.updateList();
                                } else {
                                    Toast.makeText(context, "Unable to Delete Appointment.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                        }
                    }
                });

            }
        });

        return view;

    }

    static class ViewHolder {

        TextView textView;
        ImageButton delete;
        ParseObject parseObject;

    }

}
