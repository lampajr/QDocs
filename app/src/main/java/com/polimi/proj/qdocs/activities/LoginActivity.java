package com.polimi.proj.qdocs.activities;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {

    //private CallbackManager callbackManager;
    //private LoginButton FacebookButton;
    private Button signUpButton;
    private Button signInButton;
    private EditText emailView;
    private EditText passwordView;
    private String TAG = "LOGIN";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailView = findViewById(R.id.emailText);
        passwordView = findViewById(R.id.passwordText);

        signInButton = findViewById(R.id.submitButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInWithEmailAndPassword(emailView.getText().toString(), passwordView.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, getString(R.string.error_auth),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signUpButton = findViewById(R.id.signUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Go to registration");
                final Intent reg = new Intent(LoginActivity.this, RegistrationActivity.class);
                reg.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(reg);
            }
        });




    }
}
