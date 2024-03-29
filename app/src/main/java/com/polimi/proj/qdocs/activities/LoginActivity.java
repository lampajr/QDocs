package com.polimi.proj.qdocs.activities;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.polimi.proj.qdocs.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LoginActivity extends AppCompatActivity {


    private static final int GOOGLE_SIGN_IN = 100;
    private LoginButton facebookButton;
    private EditText emailView;
    private EditText passwordView;
    private String TAG = "LOGIN";
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        emailView = findViewById(R.id.email_text);
        passwordView = findViewById(R.id.password_text);

        Button signInButton = findViewById(R.id.submit_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CredentialLogin login = new CredentialLogin();
                login.log();
            }
        });

        Button signUpButton = findViewById(R.id.sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Go to registration");
                final Intent reg = new Intent(LoginActivity.this, RegistrationActivity.class);
                //reg.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(reg);
            }
        });



        facebookButton = findViewById(R.id.facebook_button);
        facebookButton.setReadPermissions("email", "public_profile");
        facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e(TAG, "facebook:onError", exception);
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
        //private SignInButton googleButton;
        Button googleButton = findViewById(R.id.google_button);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.google_button) {
                    Log.d(TAG, "start login with google");
                    signInWithGoogle();
                }
            }
        });

        Button customFacebookButton = findViewById(R.id.custom_facebook_button);
        customFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookButton.performClick();
            }
        });
    }

    /**
     * Handle the facebook access token signing in the user by its credential
     * @param token facebook access token
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            startMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            Log.d(TAG, "On result: google");
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e);
                Toast.makeText(this, getString(R.string.google_sign_in_failed), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Log.d(TAG, "On result: facebook");
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success with google");
                            Log.d(TAG, "user "+mAuth.getCurrentUser());
                            startMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "failure", Toast.LENGTH_LONG).show();
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "On Start");
        if(mAuth.getCurrentUser() != null) {
            //finish();
            startMain();
        }
    }

    private void startMain() {
        Log.d(TAG, "end Login Activity");
        Intent scannerIntent = new Intent(this, MainActivity.class);
        startActivity(scannerIntent);
        finish();
    }

    private class CredentialLogin{
        CredentialLogin(){
            if(emailView.getText() == null || passwordView.getText()  == null) {

                Log.d(TAG, "email: null");
                Log.d(TAG, "psw: null");
            }
            else{
                Log.d(TAG, "email: "+emailView.getText().toString());
                Log.d(TAG, "psw: "+passwordView.getText().toString());
            }
        }

        void log(){
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();

            if(email.equals("")  || password.equals("")){
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_LONG).show();

            }else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    startMain();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, getString(R.string.error_auth),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

}


