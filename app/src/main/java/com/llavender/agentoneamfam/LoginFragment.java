package com.llavender.agentoneamfam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {

    SharedPreferences sharedPref;
    CheckBox saveUserName;
    CheckBox stayLoggedIn;
    SharedPreferences.Editor spEditor;
    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;



    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        //enable action bar
        setHasOptionsMenu(true);
        //enable shared preferences
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        spEditor = sharedPref.edit();

        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) view.findViewById(R.id.email);
        Button mUserNameSignInButton = (Button) view.findViewById(R.id.email_sign_in_button);
        saveUserName = (CheckBox)view.findViewById(R.id.saveUserName);
        stayLoggedIn = (CheckBox)view.findViewById(R.id.stayLoggedIn);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUserNameSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //if the user logged out reset fields
        if (sharedPref.getString("USERNAME", null) == null){
            mUserNameView.setText("");
            mPasswordView.setText("");
        } else {
            mUserNameView.setText(sharedPref.getString("USERNAME", null));
        }

        return view;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid userName, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        UserLoginTask mAuthTask;

        // Reset errors.
        mUserNameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid userName.
        if (TextUtils.isEmpty(userName)) {
            mUserNameView.setError(getString(R.string.error_field_required));
            focusView = mUserNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            mAuthTask = new UserLoginTask(userName, password);
            mAuthTask.execute();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    // Reset the login credentials
    public void resetFields() {
        mUserNameView.setText("");
        mPasswordView.setText("");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.action_logout).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserName;
        private final String mPassword;

        UserLoginTask(String userName, String password) {
            mUserName = userName;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            ParseUser.logInInBackground(mUserName, mPassword, new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        if (stayLoggedIn.isChecked()) {
                            spEditor.putBoolean("STAYLOGGEDIN", stayLoggedIn.isChecked());
                            spEditor.commit();
                        } else {
                            spEditor.remove("STAYLOGGEDIN").commit();
                        }
                        if (saveUserName.isChecked()){
                            spEditor.putString("USERNAME", mUserName);
                            spEditor.commit();
                        } else {
                            spEditor.remove("USERNAME").commit();
                        }

                        // Hooray! The user is logged in.
                        Toast.makeText(getActivity(), "Welcome " + mUserName + "!", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment())
                                .commit();
                        getActivity().invalidateOptionsMenu();
                    } else {
                        // Signin failed. Look at the ParseException to see what happened.
                        Log.d("Failure", e.toString());
                        showProgress(false);
                        resetFields();
                        Toast.makeText(getActivity(), "Invalid username/password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return true;
        }
    }
}


