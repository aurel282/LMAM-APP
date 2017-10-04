package com.example.aurel.lmam;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by aurel on 08-12-16.
 */

public  class dataApp {


    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public ActMain getCurrentActivity() {
        return CurrentActivity;
    }

    public void setCurrentActivity(ActMain currentActivity) {
        CurrentActivity = currentActivity;
    }

    private ActMain CurrentActivity;

    public boolean isAuthSuccessfull() {
        return AuthSuccessfull;
    }

    public void setAuthSuccessfull(boolean authSuccessfull) {
        AuthSuccessfull = authSuccessfull;
    }

    private boolean AuthSuccessfull = false;

    private DatabaseReference mDatabaseMessages;
    private DatabaseReference mDatabaseUsers;

    private final String TAG = "DataApp";

    public List<message> lstMessages;
    private List<user> lstUsers;

    int TotalNumberMessages;
    int TotalNumberUsers;
    int TotalNumberPictures;
    Date LastUpdate;

    long LastScreenUpdate = 0;
    private final int IntervalBetweenScreenUpdate = 60;


    ChildEventListener childMessageEventListener = new ChildEventListener()
    {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

            // A new comment has been added, add it to the displayed list
            message Temp = dataSnapshot.getValue(message.class);
            Temp.setKey(dataSnapshot.getKey());

            lstMessages.add(Temp);
            ScreenUpdate();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
            // A comment has changed, use the key to determine if we are displaying this
            // comment and if so displayed the changed comment.
            message Temp  = dataSnapshot.getValue(message.class);
            String commentKey = dataSnapshot.getKey();
            message.ModifyInList(lstMessages,Temp,commentKey);
            ScreenUpdate();

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

            String commentKey = dataSnapshot.getKey();
            Log.d(TAG, "onChildRemoved:" + commentKey);
            message.DeleteInList(lstMessages,commentKey);
            ScreenUpdate();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            // A comment has changed position, use the key to determine if we are
            // displaying this comment and if so move it.
            //Comment movedComment = dataSnapshot.getValue(Comment.class);
            //String commentKey = dataSnapshot.getKey();
            ScreenUpdate();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            Log.w(TAG, "postComments:onCancelled" + databaseError.getDetails());
            Log.w(TAG, "postComments:onCancelled" + databaseError.getMessage());
        }


    };

    public dataApp()
    {
        lstMessages = new ArrayList<>();
        lstUsers = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        LastScreenUpdate = System.currentTimeMillis();

        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //user.getToken(true);
                if (user != null) {
                    // User is signed in
                    setAuthSuccessfull(true);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else
                {
                    setAuthSuccessfull(false);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public dataApp(ActMain CurAct)
    {
        lstMessages = new ArrayList<>();
        lstUsers = new ArrayList<>();

        CurrentActivity = CurAct;

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //user.getToken(true);
                if (user != null) {
                    // User is signed in
                    setAuthSuccessfull(true);
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());


                } else
                {
                    setAuthSuccessfull(false);
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    public void Disconnect()
    {
        mAuth.getInstance().signOut();
        setAuthSuccessfull(false);
        Log.w(TAG, "Disconnected");
    }

    public void FirebaseLogin(String email,String password)
    {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            setAuthSuccessfull(true);
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        }
                        else
                        {
                            setAuthSuccessfull(false);
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                        }
                    }
                });


    }

    public void FirebaseCreateEmailAccount(String email,String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful())
                        {
                            setAuthSuccessfull(true);
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        }
                        else
                        {
                            setAuthSuccessfull(false);
                            Log.w(TAG, "createUserWithEmail:failed", task.getException());
                        }
                    }
                });
    }


    /*
    public void FirebaseGoogleLogIn()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }
 */
    public void SetAuthStateListener()
    {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void RemoveAuthStateListener()
    {
        if (mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void SetDbMessages()
    {
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference().child("DbMessages");
        mDatabaseMessages.addChildEventListener(childMessageEventListener);
        ScreenUpdate();
    }

    public FirebaseUser GetFireBaseUser()
    {
        return user;
    }

    public void ScreenUpdate()
    {
        if  (((System.currentTimeMillis() - LastScreenUpdate) / 1000) > IntervalBetweenScreenUpdate || LastScreenUpdate == 0)
        {
            LastScreenUpdate = System.currentTimeMillis();
            CurrentActivity.UpdateMarkerMessage();
        }
    }







}
