package com.example.amidezcod.googlesignin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 0123;
    private com.google.android.gms.common.SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mStatus;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSignInButton = (SignInButton) findViewById(R.id.sign_in);
        mSignOutButton = (Button) findViewById(R.id.sign_out);
        mRevokeButton = (Button) findViewById(R.id.revoke);
        mStatus = (TextView) findViewById(R.id.text);
        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);
        mGoogleApiClient = buildApiClient();
        if (mGoogleApiClient.isConnected())
            updateUI(true);
        else
            updateUI(false);
    }

    private GoogleApiClient buildApiClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        return mGoogleApiClient;

    }

    @Override
    public void onStart() {
        super.onStart();


        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();

            result(result);
            mGoogleApiClient.connect();
            updateUI(true);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {

                    result(googleSignInResult);
                }
            });
        }
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mStatus.setText(getString(R.string.signing_in));
    }


    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                        mStatus.setText(R.string.sign_out);

                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                        mStatus.setText(R.string.revoke_access);

                    }
                });
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.sign_in:
                signIn();
                break;

            case R.id.sign_out:
                signOut();
                break;

            case R.id.revoke:
                revokeAccess();
                break;
            default:
                throw new ExceptionInInitializerError("TAG");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess() && result.getSignInAccount() != null) {
                mGoogleApiClient.connect();
                result(result);
                updateUI(true);


            }
        }
    }


    private void updateUI(boolean b) {
        if (b) {
            mSignInButton.setEnabled(false);
            mSignOutButton.setEnabled(true);
            mRevokeButton.setEnabled(true);
        } else {
            mSignInButton.setEnabled(true);
            mSignOutButton.setEnabled(false);
            mRevokeButton.setEnabled(false);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        updateUI(true);


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void result(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess() && googleSignInResult.getSignInAccount() != null)
            mStatus.setText(String.format("%s\t%s", getString(R.string.signed_in), googleSignInResult.getSignInAccount().getEmail()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}