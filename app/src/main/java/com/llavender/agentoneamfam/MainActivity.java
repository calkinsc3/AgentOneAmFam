package com.llavender.agentoneamfam;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private boolean hasRun = false;
    private ParseUser currentUser;
    private SharedPreferences sharedPref;
    private String[] drawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerItems = getResources().getStringArray(R.array.drawerItems);
        drawerList = (ListView)findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, drawerItems));

        //sets the Up Navigation enabled only if fragments are on backStack
        enableUpAction();
        //Set the navigation drawer navigation
        setDrawerItemClickListener();
        //Enables Parse on startup
        enableParse();
        //checks if a user has selected "Keep me logged in"
        checkLoginStatus();
        //loads the appropriate initial fragment
        loadInitialFragment(savedInstanceState);

    }

    public void checkLoginStatus(){
        //if the user checked "Keep Me Logged In" populate current user
        //else set current user to null
        if (sharedPref.getBoolean("STAYLOGGEDIN", false)) {
            currentUser = ParseUser.getCurrentUser();
            Toast.makeText(this, "Welcome " + currentUser.getString("username"), Toast.LENGTH_SHORT).show();
        } else {
            currentUser = null;
        }
    }

    public void loadInitialFragment(Bundle savedInstanceState){
        if (currentUser != null) {
            // do stuff with the user
            getFragmentManager().beginTransaction().add(R.id.fragment_container, new MainFragment())
                    .addToBackStack("MAIN")
                    .commit();
        } else {
            // show the sign up or login screen
            if (findViewById(R.id.fragment_container) != null) {
                if (savedInstanceState == null) {
                    LoginFragment loginFragment = new LoginFragment();
                    // In case the activity was started with special instructions from an Intent
                    // pass the Intent's extras to the fragment as arguments
                    loginFragment.setArguments(getIntent().getExtras());

                    getFragmentManager().beginTransaction().add(R.id.fragment_container, loginFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }
    }

    public void enableParse(){
        if (!hasRun) {
            Parse.enableLocalDatastore(this);
            Parse.initialize(this, "4YBarCfwhDQKdD9w7edqe8fIazqWRXv8RhRbNgd7", "zUguFYSgfxNkzTw6lQGkCWssT1VCMWBccWD44MFw");
            hasRun = true;
        }
    }

    public void setDrawerItemClickListener(){
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawers();

                //leave this crap here levi!!!!
                final int CLIENTS = 0;
                final int CLAIMS = 1;
                final int SCHEDULE = 2;
                final int SETTINGS = 3;
                final int UPLOADS = 4;


                switch (position) {
                    case CLIENTS:
                        Tools.replaceFragment(new ClientListFragment(), getFragmentManager(), true);
                        break;
                    case CLAIMS:
                        Tools.replaceFragment(new Claims(), getFragmentManager(), true);
                        break;
                    case SCHEDULE:
                        //move to schdulefrage
                        Tools.replaceFragment(new MeetingListFragment(), getFragmentManager(), true);
                        MeetingListFragment.mode = MeetingListFragment.APPOINTMENTS;
                        break;
                    case SETTINGS:
                        Tools.replaceFragment(new Settings(), getFragmentManager(), true);
                        break;
                    case UPLOADS:
                        Tools.replaceFragment(new MyUploads(), getFragmentManager(), true);
                        break;
                    default:
                        Log.d("Error", "Reached default in drawer");
                }
            }
        });
    }

    public void enableUpAction(){
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getFragmentManager().getBackStackEntryCount();
                if (stackHeight > 1) { // if we have something on the stack (doesn't include the current shown fragment). >0 removes initial frag and leave a blank space...use 1 instead.
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setHomeButtonEnabled(false);
                }
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        hasRun = true;
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case R.id.action_logout:
                SharedPreferences.Editor spEditor = sharedPref.edit();
                FragmentManager fm = getFragmentManager();
                spEditor.remove("STAYLOGGEDIN");
                spEditor.commit();

                //logout the parseUser
                ParseUser.logOut();

                //Clear the backStack
                while (fm.getBackStackEntryCount() > 1){
                    fm.popBackStackImmediate();
                }
                //Restart the Login fragment
                fm.beginTransaction().replace(R.id.fragment_container, new LoginFragment())
                        .commit();
                break;

            case R.id.action_contact_support:
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@amfam.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OneAmFamOffice Problem");
                emailIntent.setType("plain/text");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(emailIntent, "Choose Mail Application"));
                }
                break;

            case R.id.action_settings:
                Toast.makeText(this, "One AmFam Office Application. Made By the IS Division Interns", Toast.LENGTH_SHORT).show();
                Tools.replaceFragment(new Settings(), getFragmentManager(), true);
                break;

            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;

            default:
                //Do nothing
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        if (fm.getBackStackEntryCount() >= 1) {
            fm.popBackStackImmediate();
        } else {

            super.onBackPressed();
        }
    }
}
