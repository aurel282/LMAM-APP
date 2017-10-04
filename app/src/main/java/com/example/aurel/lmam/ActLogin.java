package com.example.aurel.lmam;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



//FirebaseAuth.getInstance().signOut();                 To disconnect

public class ActLogin extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final String TAG = "LMAM-ActLogin";



    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;

    private String email;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActMain.AccData = new dataApp();



        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mEmailView.setText("aurelien.gek@gmail.com");
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText("testtest");



        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                email = mEmailView.getText().toString();
                password = mPasswordView.getText().toString();

                new MyAsyncTaskLogIn().execute();

            }
        });

        Button mEmailRegisterButton = (Button) findViewById(R.id.email_register_button);
        mEmailRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                email = mEmailView.getText().toString();
                password = mPasswordView.getText().toString();

                new MyAsyncTaskRegister().execute();

            }
        });

        Button mGoogleSignInButton = (Button) findViewById(R.id.google_log_in_button);
        mGoogleSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                email = mEmailView.getText().toString();
                password = mPasswordView.getText().toString();

                new MyAsyncTaskGoogleLogIn().execute();

            }
        });
    }

    private class MyAsyncTaskLogIn extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            ActMain.AccData.FirebaseLogin(email,password);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (ActMain.AccData.isAuthSuccessfull())
            {
                OpenMainActivity();
            }
            else
            {
                Context context = getApplicationContext();
                CharSequence text = "Identification Failed!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    private class MyAsyncTaskRegister extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            ActMain.AccData.FirebaseCreateEmailAccount(email,password);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (ActMain.AccData.isAuthSuccessfull())
            {
                OpenMainActivity();
            }
            else
            {
                Context context = getApplicationContext();
                CharSequence text = "Identification Failed!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }

    private class MyAsyncTaskGoogleLogIn extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            //ActMain.AccData.FirebaseGoogleLogIn();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (ActMain.AccData.isAuthSuccessfull())
            {
                OpenMainActivity();
            }
            else
            {
                Context context = getApplicationContext();
                CharSequence text = "Identification Failed!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        ActMain.AccData.SetAuthStateListener();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device com.example.aurel.lmam.user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the com.example.aurel.lmam.user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(ActLogin.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private void OpenMainActivity()
    {
        Intent myIntent = new Intent(ActLogin.this, ActMain.class);
        ActLogin.this.startActivity(myIntent);
    }


    private interface ProfileQuery
    {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }



}

