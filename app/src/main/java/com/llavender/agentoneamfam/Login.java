package com.llavender.agentoneamfam;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;


public class Login extends Activity {

    //PARSE KEYS
    private static final String APPLICATION_ID = "4YBarCfwhDQKdD9w7edqe8fIazqWRXv8RhRbNgd7";
    private static final String CLIENT_KEY = "zUguFYSgfxNkzTw6lQGkCWssT1VCMWBccWD44MFw";

    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username_entry = (EditText) findViewById(R.id.username);
        final EditText password_entry = (EditText) findViewById(R.id.password);
        final Button login_button = (Button) findViewById(R.id.login_button);
        final CheckBox username_checkbox = (CheckBox) findViewById(R.id.remember_username_checkbox);
        final CheckBox login_checkbox = (CheckBox) findViewById(R.id.stay_logged_in_checkbox);
        final SharedPreferences p = getSharedPreferences(Singleton.PREFERENCES, 0);

        //NEEDED FOR ONCLICKLISTENER
        context = this;

        //CHECK FOR LOGIN
        if (p.getString("OfficeUserID", null) != null && p.getBoolean("OfficeStayLoggedIn", false)) {
            loginSuccess();
        } else if (p.getString("OfficeUsername", null) != null) {
            username_checkbox.setChecked(true);
            username_entry.setText(p.getString("OfficeUsername", ""));
        }

        //INITIALIZE PARSE
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, APPLICATION_ID, CLIENT_KEY);

        //LOGIN CLICK
        login_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = ProgressDialog.show(context, "", "Signing in to Parse.com", true);

                final String username = username_entry.getText().toString();
                String password = password_entry.getText().toString();


                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {

                        progressDialog.dismiss();

                        if (e == null && user != null) {

                            //UPDATE SHARED PREFERENCES
                            SharedPreferences.Editor editor = p.edit();
                            editor.putString("OfficeUserID", user.getObjectId());
                            editor.putBoolean("OfficeStayLoggedIn", login_checkbox.isChecked());
                            if (username_checkbox.isChecked()) {
                                editor.putString("OfficeUsername", username);
                            } else {
                                editor.remove("OfficeUsername");
                            }
                            editor.apply();

                            //login successful
                            loginSuccess();

                        } else if (user == null) {
                            loginFail();

                        } else {
                            loginError(e);
                        }
                    }
                });

            }
        });
    }


    public void loginSuccess() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    public void loginFail() {

        Toast.makeText(this, "Wrong Username or Password", Toast.LENGTH_SHORT).show();
    }


    public void loginError(ParseException e) {
        Toast.makeText(this, "Login Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
