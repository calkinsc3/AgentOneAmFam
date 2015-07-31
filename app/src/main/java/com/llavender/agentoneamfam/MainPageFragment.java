package com.llavender.agentoneamfam;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by nsr009 on 6/16/2015.
 */
public class MainPageFragment extends Fragment {

    View rootView;

    Button clients;
    Button claims;
    Button schedule;
    Button settings;
    Button uploads;

    Display display;
    Point size = new Point();

    private static final long CLOUD_SPEED = 500;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_page, container, false);

        clients = (Button) rootView.findViewById(R.id.clientsButton);
        claims = (Button) rootView.findViewById(R.id.claimsButton);
        schedule = (Button) rootView.findViewById(R.id.scheduleButton);
        settings = (Button) rootView.findViewById(R.id.settingsButton);
        uploads = (Button) rootView.findViewById(R.id.uploadsButton);

        display = getActivity().getWindowManager().getDefaultDisplay();
        display.getSize(size);

        ParseQuery<ParseUser> query = ParseUser.getQuery();

        query.whereEqualTo("accountType", "Client");
        query.whereEqualTo("AgentID", ParseUser.getCurrentUser().getObjectId());

        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    Singleton.setListOfClients((ArrayList<ParseUser>) objects);
                } else {
                    Log.d("loadUser Exception", e.toString());
                }
            }
        });

        buttonClickListeners();

        return rootView;
    }

    private void buttonClickListeners() {
        clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clients.setEnabled(false);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_right);
                anim.setDuration(CLOUD_SPEED + 500);
                clients.startAnimation(anim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        Tools.replaceFragment(new ClientListFragment(), getFragmentManager(), true);
                    }
                }, CLOUD_SPEED);
            }
        });

        claims.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claims.setEnabled(false);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_right);
                anim.setDuration(CLOUD_SPEED + 500);
                claims.startAnimation(anim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        Tools.replaceFragment(new Claims(), getFragmentManager(), true);
                    }
                }, CLOUD_SPEED);
            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schedule.setEnabled(false);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_right);
                anim.setDuration(CLOUD_SPEED + 500);
                schedule.startAnimation(anim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        Tools.replaceFragment(new MeetingListFragment(), getFragmentManager(), true);
                    }
                }, CLOUD_SPEED);

                MeetingListFragment.mode = MeetingListFragment.APPOINTMENTS;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setEnabled(false);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_right);
                anim.setDuration(CLOUD_SPEED + 500);
                settings.startAnimation(anim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        Tools.replaceFragment(new Settings(), getFragmentManager(), true);
                    }
                }, CLOUD_SPEED);
            }
        });

        uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploads.setEnabled(false);
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_to_right);
                anim.setDuration(CLOUD_SPEED + 500);
                uploads.startAnimation(anim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // This method will be executed once the timer is over
                        Tools.replaceFragment(new MyUploads(), getFragmentManager(), true);
                    }
                }, CLOUD_SPEED);
            }
        });
    }

    @Override
    public void onResume() {
        super.onCreate(null);

        uploads.setEnabled(true);
        settings.setEnabled(true);
        schedule.setEnabled(true);
        claims.setEnabled(true);
        clients.setEnabled(true);
    }
}