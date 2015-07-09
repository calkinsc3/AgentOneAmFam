package com.llavender.agentoneamfam;


import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.Date;
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







    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        final EditText meeting_entry = (EditText) view.findViewById(R.id.meeting_entry);
        final EditText attendees_entry = (EditText) view.findViewById(R.id.attendees_entry);
        final EditText location_entry = (EditText) view.findViewById(R.id.location_entry);
        final EditText start_time_entry = (EditText) view.findViewById(R.id.start_time_entry);
        final EditText start_date_entry = (EditText) view.findViewById(R.id.start_date_entry);
        final EditText end_time_entry = (EditText) view.findViewById(R.id.end_time_entry);
        final EditText end_date_entry = (EditText) view.findViewById(R.id.end_date_entry);
        final EditText comments_entry = (EditText) view.findViewById(R.id.comments_entry);

        ImageButton save_button = (ImageButton) view.findViewById(R.id.save_button);

        final Calendar startDateCalendar = Calendar.getInstance();
        final Calendar endDateCalendar = Calendar.getInstance();

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
            JSONArray invited = MeetingListFragment.selectedAppointment.getJSONArray("InvitedIDs");

            if(invited != null) attendees_entry.setText(invited.toString());

        }


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
               // appointmentToSave.put("InvitedIDs", invitedIDS);
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


                if(eventID != -1){

                    Toast.makeText(getActivity(), "Appointment saved to google Calendar", Toast.LENGTH_SHORT).show();
                }
                else{

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



}
