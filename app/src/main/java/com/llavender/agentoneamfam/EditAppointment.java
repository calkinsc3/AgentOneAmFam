package com.llavender.agentoneamfam;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
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
 * Edits an appointment and adds the appointment to google calender.
 */
public class EditAppointment extends Fragment {

    /**
     * Public fields required for Google Calendar
     */
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
    static List<Integer> mSelectedUsers;
    static String[] attendeesList;
    static boolean[] checkedUserIDs;
    AlertDialog.Builder builder;
    EditText meeting_entry;
    EditText location_entry;
    EditText start_time_entry;
    EditText start_date_entry;
    EditText end_time_entry;
    EditText end_date_entry;
    EditText comments_entry;
    Calendar startDateCalendar;
    Calendar endDateCalendar;
    //the current users calendarInfo
    String[] calendarInfo;
    // Variables for edit invitees alert dialog.
    private EditText attendees_entry;
    public EditAppointment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_appointment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        meeting_entry = (EditText) view.findViewById(R.id.meeting_entry);
        location_entry = (EditText) view.findViewById(R.id.location_entry);
        start_time_entry = (EditText) view.findViewById(R.id.start_time_entry);
        start_date_entry = (EditText) view.findViewById(R.id.start_date_entry);
        end_time_entry = (EditText) view.findViewById(R.id.end_time_entry);
        end_date_entry = (EditText) view.findViewById(R.id.end_date_entry);
        comments_entry = (EditText) view.findViewById(R.id.comments_entry);
        attendees_entry = (EditText) view.findViewById(R.id.attendees_entry);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        calendarInfo = getCalendar();

        mSelectedUsers = new ArrayList<>();
        checkedUserIDs = new boolean[0];
        builder = new AlertDialog.Builder(getActivity());

        if (MeetingListFragment.selectedAppointment != null) {
            loadSelectedMeeting();
        }

        editInvitees();

        setListeners();

    }

    /**
     * Sets all the Listeners for edittexts and datePickers
     */
    public void setListeners(){

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
                        },
                        startDateCalendar.get(Calendar.YEAR),
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

    }

    /**
     * Loads the information from the meeting selected on the meetingsList fragment
     */
    public void loadSelectedMeeting(){
        String attendees = "";

        // Load info
        meeting_entry.setText(MeetingListFragment.selectedAppointment.getString("Title"));
        location_entry.setText(MeetingListFragment.selectedAppointment.getString("Location"));
        comments_entry.setText(MeetingListFragment.selectedAppointment.getString("Comment"));

        // Load start and end date
        Date startDate = MeetingListFragment.selectedAppointment.getDate("StartDate");
        Date endDate = MeetingListFragment.selectedAppointment.getDate("EndDate");

        startDateCalendar.setTime(startDate);
        endDateCalendar.setTime(endDate);

        Tools.updateTimeEntry(start_time_entry, startDateCalendar);
        Tools.updateTimeEntry(end_time_entry, endDateCalendar);
        Tools.updateDateEntry(start_date_entry, startDateCalendar);
        Tools.updateDateEntry(end_date_entry, endDateCalendar);

        //Load invitees
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

    /**
     * Saves the current appointment (new or old) and uploads it to the user's Google Calendar
     */
    public void saveAppointment(){
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "", true);
        ParseObject appointmentToSave;

        if (MeetingListFragment.selectedAppointment != null) {
            appointmentToSave = MeetingListFragment.selectedAppointment;
        } else {
            appointmentToSave = new ParseObject("Meeting");
        }

        // Save input from user to parse database.
        String title = meeting_entry.getText().toString();
        JSONArray invitedIDS = new JSONArray((Arrays.asList(attendeesList)));
        String location = location_entry.getText().toString();
        Date startDate = startDateCalendar.getTime();
        Date endDate = endDateCalendar.getTime();
        String comments = comments_entry.getText().toString();

        appointmentToSave.put("Title", title);
        appointmentToSave.put("InvitedIDs", invitedIDS);
        appointmentToSave.put("Location", location);
        appointmentToSave.put("StartDate", startDate);
        appointmentToSave.put("EndDate", endDate);
        appointmentToSave.put("Comment", comments);
        // TODO ??
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

    /**
     * Gets the User's google calendar from google
     * @return an array of information regarding the user's calendar
     *         it is put into the array in the format outlined by the google calendar fields
     */
    public String[] getCalendar(){

        // Run query
        Cursor cur;
        ContentResolver cr = getActivity().getContentResolver();
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

    /**
     * Edit invitees editText on click listener.
     */
    private void editInvitees() {
        attendees_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPossibleAttendees();
            }
        });
    }

    /**
     * Gets ids of all the possible users that can be invited (Agents and Clients).
     */
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
                        // Add object ids for all possible attendees to a concurrent string array.
                        String objID = possibleAttendees.get(i).getObjectId();
                        userIDs[i] = objID;

                        // Checks if a user is already added to the list of invitees and adds them
                        // to a list to have them already checked when alert dialog pops up.
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

    /**
     * Shows the alert dialog for all the possible attendees.
     *
     * @param possibleAttendees List of all the users that can be invited to attend the meeting.
     * @param userIDs Object ids of all the users that can be invited to attend the meeting.
     */
    private void showPopupDialog(final List<ParseUser> possibleAttendees, String[] userIDs) {
        builder.setTitle("Select Attendees");

        // Alert dialog with multi-select.
        builder.setMultiChoiceItems(userIDs, checkedUserIDs,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                        if (isChecked && !mSelectedUsers.contains(item)) {
                            // A new user is invited.
                            mSelectedUsers.add(item);
                            checkedUserIDs[item] = true;
                        } else if (mSelectedUsers.contains(item)) {
                            if (mSelectedUsers.size() == 1) {
                                // All the possible invitees are unselected.
                                mSelectedUsers = new ArrayList<>();
                            } else {
                                // A previous invitee was unselected.
                                mSelectedUsers.remove(Integer.valueOf(item));
                                checkedUserIDs[item] = false;
                            }
                        }

                        // Removes the selection of multiple users from the list to display in the
                        // attendees_entry edit text.
                        mSelectedUsers = removeDuplicates(mSelectedUsers);
                    }
                });

        builder.setPositiveButton("Invite", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                attendeesList = new String[mSelectedUsers.size()];
                String attendees = "";

                // Build the string to display the selected invitees in the
                // attendees_entry edit text.
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

    /**
     * Removes all the duplicate items from a list.
     *
     * @param oldList List from which to remove the duplicate items from.
     * @return List one occurrence of each item.
     */
    private List<Integer> removeDuplicates(List<Integer> oldList) {
        Set<Integer> newList = new HashSet<>();
        newList.addAll(oldList);

        return new ArrayList<>(newList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_save).setIcon(android.R.drawable.ic_menu_save);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveAppointment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}