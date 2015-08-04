package com.llavender.agentoneamfam;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseUser;


public class MainActivity extends Activity {

    private SharedPreferences sharedPref;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView drawerList;
    private String[] drawerItems;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        setupDrawer();

        //Set the navigation drawer navigation
        setDrawerItemClickListener();
        //Enables Parse on startup
        //checks if a user has selected "Keep me logged in"
        checkLoginStatus();

        if (savedInstanceState == null) {
            Tools.replaceFragment(new MainPageFragment(), getFragmentManager(), true);
        }
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

    public void setDrawerItemClickListener(){
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawers();

                final int HOME = 0;
                final int CLIENTS = 1;
                final int CLAIMS = 2;
                final int SCHEDULE = 3;
                final int SETTINGS = 4;
                final int UPLOADS = 5;

                switch (position) {
                    case HOME:
                        replaceIfNew(new MainPageFragment());
                        break;

                    case CLIENTS:
                        replaceIfNew(new ClientListFragment());
                        break;

                    case CLAIMS:
                        replaceIfNew(new Claims());
                        break;

                    case SCHEDULE:
                        replaceIfNew(new MeetingListFragment());
                        MeetingListFragment.mode = MeetingListFragment.APPOINTMENTS;
                        break;

                    case SETTINGS:
                        replaceIfNew(new Settings());
                        break;

                    case UPLOADS:
                        replaceIfNew(new MyUploads());
                        break;
                    
                    default:
                        Log.d("Error", "Reached default in drawer");
                }
            }
        });
    }

    public void setupDrawer(){
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                R.drawable.ic_launcher,
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerItems = getResources().getStringArray(R.array.drawerItems);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                R.layout.basic_list_item, drawerItems));
    }

    private void replaceIfNew(Fragment fragment){
        FragmentManager fm = getFragmentManager();
        if(!fragment.getClass().toString().equals(fm.findFragmentById(R.id.fragment_container)
                .getClass().toString())){

            Tools.replaceFragment(fragment, fm, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_logout:
                Tools.logout(this);
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
                Toast.makeText(this, "One AmFam Office Application. Made By the IS Division Interns",
                        Toast.LENGTH_SHORT).show();
                Tools.replaceFragment(new Settings(), getFragmentManager(), true);
                break;

            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;

            default:
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