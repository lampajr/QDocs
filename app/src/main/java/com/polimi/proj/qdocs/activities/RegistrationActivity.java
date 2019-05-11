package com.polimi.proj.qdocs.activities;

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

        //TODO: implementare il tasto indietro per tornare al login

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "start registration");
                if(emailText.getText().toString().equals("") || passwordText.getText().toString().equals("")){
                    Toast.makeText(RegistrationActivity.this, "Illegal email or password", Toast.LENGTH_LONG).show();
                }
                else {
                    mAuth.createUserWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        Intent login = new Intent(RegistrationActivity.this, LoginActivity.class);
                                        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(login);

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
        });


    }
}
