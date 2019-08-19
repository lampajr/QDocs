package com.polimi.proj.qdocs.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;

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

public class RegistrationActivity extends AppCompatActivity {

    private String TAG = "REGISTRATION";
    private EditText emailText;
    private EditText passwordText;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private TextView labelError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        emailText = findViewById(R.id.email_text);
        passwordText = findViewById(R.id.password_text);
        registerButton = findViewById(R.id.submit_button);
        mAuth = FirebaseAuth.getInstance();
        labelError = findViewById(R.id.label_error);

        //TODO: implement get back button toward login

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "start registration");
                if(emailText.getText().toString().equals("") || passwordText.getText().toString().equals("")){
                    if(emailText.getText().toString().equals(""))
                        labelError.setText(getString(R.string.null_email));
                    else
                        labelError.setText(getString(R.string.null_password));

                }
                else {
                   register();
                }
            }
        });
    }

    private void register() {
        mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent main = new Intent(RegistrationActivity.this, MainActivity.class);
                            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(main);

                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {

                                labelError.setText(getString(R.string.invalid_password_registration));

                            } catch (FirebaseAuthInvalidCredentialsException e) {

                                labelError.setText(getString(R.string.error_invalid_email));

                            } catch (FirebaseAuthUserCollisionException e) {

                                labelError.setText(getString(R.string.email_already_used));

                            } catch (Exception e) {
                                Toast.makeText(RegistrationActivity.this, "An error is occurred",
                                        Toast.LENGTH_LONG).show();
                                Log.e(TAG, e.getMessage());
                            }
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        }

                        // ...
                    }
                });
    }
}
