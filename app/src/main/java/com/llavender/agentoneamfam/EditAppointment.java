package com.llavender.agentoneamfam;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditAppointment extends Fragment {

    public EditAppointment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_appointment, container, false);
    }

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    private EditText attendees_entry;

    AlertDialog.Builder builder;

    static List<Integer> mSelectedUsers;
    static String[] attendeesList;
    static boolean[] checkedUserIDs;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final EditText meeting_entry = (EditText) view.findViewById(R.id.meeting_entry);
        final EditText location_entry = (EditText) view.findViewById(R.id.location_entry);
        final EditText start_time_entry = (EditText) view.findViewById(R.id.start_time_entry);
        final EditText start_date_entry = (EditText) view.findViewById(R.id.start_date_entry);
        final EditText end_time_entry = (EditText) view.findViewById(R.id.end_time_entry);
        final EditText end_date_entry = (EditText) view.findViewById(R.id.end_date_entry);
        final EditText comments_entry = (EditText) view.findViewById(R.id.comments_entry);
        attendees_entry = (EditText) view.findViewById(R.id.attendees_entry);

        ImageButton save_button = (ImageButton) view.findViewById(R.id.save_button);

        final Calendar startDateCalendar = Calendar.getInstance();
        final Calendar endDateCalendar = Calendar.getInstance();

        mSelectedUsers = new ArrayList<>();
        attendeesList = new String[0];
        checkedUserIDs = new boolean[0];
        builder = new AlertDialog.Builder(getActivity());
        String attendees = "";

        //users calendar
        final String[] calendarInfo = getCalendar(getActivity());

        if (MeetingListFragment.selectedAppointment != null) {

            //load info
            meeting_entry.setText(MeetingListFragment.selectedAppointment.getString("Title"));
            location_entry.setText(MeetingListFragment.selectedAppointment.getString("Location"));
            comments_entry.setText(MeetingListFragment.selectedAppointment.getString("Comment"));

            //load start and end date
            Date startDate = MeetingListFragment.selectedAppointment.getDate("StartDate");
            Date endDate = MeetingListFragment.selectedAppointment.getDate("EndDate");

            startDateCalendar.setTime(startDate);
            endDateCalendar.setTime(endDate);

            Tools.updateTimeEntry(start_time_entry, startDateCalendar);
            Tools.updateTimeEntry(end_time_entry, endDateCalendar);
            Tools.updateDateEntry(start_date_entry, startDateCalendar);
            Tools.updateDateEntry(end_date_entry, endDateCalendar);

            //Load participants
//            JSONArray invited = MeetingListFragment.selectedAppointment.getJSONArray("InvitedIDs");
            try {
                JSONArray jArray = MeetingListFragment.selectedAppointment.getJSONArray("InvitedIDs");
                attendeesList = new String[jArray.length()];

                for (int i = 0; i < jArray.length(); i++) {
                    attendeesList[i] = jArray.getString(i);

                    if (i == 0)
                        attendees += jArray.getString(i);
                    else
                        attendees += (", " + jArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            attendees_entry.setText(attendees);
        }

        editInvitees();

        start_date_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {


                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                startDateCalendar.set(Calendar.YEAR, year);
                                startDateCalendar.set(Calendar.MONTH, monthOfYear);
                                startDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Tools.updateDateEntry(start_date_entry, startDateCalendar);

                            }
                        }
                        , startDateCalendar.get(Calendar.YEAR),
                        startDateCalendar.get(Calendar.MONTH),
                        startDateCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        end_date_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                endDateCalendar.set(Calendar.YEAR, year);
                                endDateCalendar.set(Calendar.MONTH, monthOfYear);
                                endDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                Tools.updateDateEntry(end_date_entry, endDateCalendar);

                            }
                        },
                        endDateCalendar.get(Calendar.YEAR),
                        endDateCalendar.get(Calendar.MONTH),
                        endDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        start_time_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {


                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                                startDateCalendar.set(Calendar.HOUR, selectedHour);
                                startDateCalendar.set(Calendar.MINUTE, selectedMinute);
                                Tools.updateTimeEntry(start_time_entry, startDateCalendar);
                            }
                        },

                        startDateCalendar.get(Calendar.HOUR),
                        startDateCalendar.get(Calendar.MINUTE), false).show();

            }
        });

        end_time_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TimePickerDialog(getActivity(),
                        new TimePickerDialog.OnTimeSetListener() {


                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                                endDateCalendar.set(Calendar.HOUR, selectedHour);
                                endDateCalendar.set(Calendar.MINUTE, selectedMinute);
                                Tools.updateTimeEntry(end_time_entry, endDateCalendar);

                            }
                        },
                        endDateCalendar.get(Calendar.HOUR),
                        endDateCalendar.get(Calendar.MINUTE), false).show();

            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "", true);
                ParseObject appointmentToSave;

                if (MeetingListFragment.selectedAppointment != null) {
                    appointmentToSave = MeetingListFragment.selectedAppointment;
                } else {
                    appointmentToSave = new ParseObject("Meeting");
                }

                String title = meeting_entry.getText().toString();
                String invitedIDS = attendees_entry.getText().toString();
                String location = location_entry.getText().toString();
                Date startDate = startDateCalendar.getTime();
                Date endDate = endDateCalendar.getTime();
                String comments = comments_entry.getText().toString();


                appointmentToSave.put("Title", title);
               //TODO
                appointmentToSave.put("InvitedIDs", new JSONArray((Arrays.asList(attendeesList))));
                appointmentToSave.put("Location", location);
                appointmentToSave.put("StartDate", startDate);
                appointmentToSave.put("EndDate", endDate);
                appointmentToSave.put("Comment", comments);
                appointmentToSave.put("Accepted", true);

                appointmentToSave.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        progressDialog.dismiss();

                        if (e == null) {
                            Toast.makeText(getActivity(), "Appointment Saved to Parse.", Toast.LENGTH_SHORT).show();
                            getActivity().onBackPressed();
                        } else {
                            Toast.makeText(getActivity(), "Appointment NOT Saved to Parse.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                                /**
                 * SAVE TO GOOGLE CALENDAR
                 */
                long calID = Long.parseLong(calendarInfo[PROJECTION_ID_INDEX]);

                ContentResolver cr = getActivity().getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startDate.getTime());
                values.put(CalendarContract.Events.DTEND, endDate.getTime());
                values.put(CalendarContract.Events.TITLE, title);
                values.put(CalendarContract.Events.DESCRIPTION, comments);
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                // get the event ID that is the last element in the Uri
                long eventID = Long.parseLong(uri.getLastPathSegment());


                if(eventID != -1) {
                    Toast.makeText(getActivity(), "Appointment saved to google Calendar", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Appointment not saved to google Calendar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static String[] getCalendar(Context context){

        // Run query
        Cursor cur = null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";

        //TODO user email set here + check valid result
        String[] selectionArgs = new String[] { Settings.user_email, "com.google", Settings.user_email};

        // Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        long calID = 0;
        String displayName = null;
        String accountName = null;
        String ownerName = null;

        while (cur.moveToNext()) {
            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
        }

        return new String[] {String.valueOf(calID), displayName, accountName, ownerName};
    }

    private void editInvitees() {
        attendees_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPossibleAttendees();
            }
        });
    }

    private void getPossibleAttendees() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("accountType", "Office");

        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> possibleAttendees, ParseException e) {
                if (e == null && possibleAttendees.size() != 0) {
                    String[] userIDs = new String[possibleAttendees.size()];
                    checkedUserIDs = new boolean[possibleAttendees.size()];

                    for (int i = 0; i < possibleAttendees.size(); i++) {
                        String objID = possibleAttendees.get(i).getObjectId();
                        userIDs[i] = objID;

                        if (attendeesList != null && Arrays.asList(attendeesList).contains(objID)) {
                            mSelectedUsers.add(i);
                            checkedUserIDs[i] = true;
                        }
                    }
                    showPopupDialog(possibleAttendees, userIDs);
                } else if (possibleAttendees.size() == 0) {
                    builder.setTitle("Select Attendees");
                    builder.setMessage("No users found");

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {}
                    });
                } else {
                    //Something went wrong.
                    Log.e("ERROR", e.getMessage());
                }
            }
        });
    }

    private void showPopupDialog(final List<ParseUser> possibleAttendees, String[] userIDs) {
        builder.setTitle("Select Attendees");

        builder.setMultiChoiceItems(userIDs, checkedUserIDs,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                        if (isChecked && !mSelectedUsers.contains(item)) {
                            mSelectedUsers.add(item);
                            checkedUserIDs[item] = true;
                        } else if (mSelectedUsers.contains(item)) {
                            if (mSelectedUsers.size() == 1) {
                                mSelectedUsers = new ArrayList<>();
                            } else {
                                mSelectedUsers.remove(Integer.valueOf(item));
                                checkedUserIDs[item] = false;
                            }
                        }
                        mSelectedUsers = removeDuplicates(mSelectedUsers);
                    }
                });

        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                attendeesList = new String[mSelectedUsers.size()];
                String attendees = "";

                for (int i = 0; i < mSelectedUsers.size(); i++) {
                    String objID = possibleAttendees.get(mSelectedUsers.get(i)).getObjectId();
                    attendeesList[i] = objID;

                    if (i == 0)
                        attendees += objID;
                    else
                        attendees += (", " + objID);
                }

                attendees_entry.setText(attendees);
                mSelectedUsers = new ArrayList<>();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mSelectedUsers = new ArrayList<>();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<Integer> removeDuplicates(List<Integer> oldList) {
        Set<Integer> newList = new HashSet<>();
        newList.addAll(oldList);

        return new ArrayList<>(newList);
    }
}