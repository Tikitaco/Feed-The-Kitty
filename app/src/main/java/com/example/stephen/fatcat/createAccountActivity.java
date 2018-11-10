package com.example.stephen.fatcat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class createAccountActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordFiedl;
    private EditText mPasswordFied2;
    private Button mLogin;
    private FirebaseAuth mAuth;
    private final String TAG = "createAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        //databaseReference replaces Firbase variable in the latest version



        mEmailField = (EditText) findViewById(R.id.createEmail);
        mPasswordFiedl = (EditText) findViewById(R.id.createPassword);
        mPasswordFied2 = (EditText) findViewById(R.id.createPassword2);
        mLogin = (Button) findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
                finish();
            }
        });


    }


    private void startSignIn() {
        if (!mPasswordFiedl.getText().toString().trim().equals(mPasswordFied2.getText().toString().trim())) {
            Toast.makeText(createAccountActivity.this, "Password must match",
                    Toast.LENGTH_SHORT).show();
        } else if (mPasswordFiedl.getText().toString().trim().length() <= 6) {
            Toast.makeText(createAccountActivity.this, "Password must be greater than 6 characters",
                    Toast.LENGTH_SHORT).show();
        } else if (mPasswordFiedl.getText().toString().trim().contains(" ")) {
            Toast.makeText(createAccountActivity.this, "Password must not contain white space",
                    Toast.LENGTH_SHORT).show();
        } else {
            String email = mEmailField.getText().toString().trim();
            String password = mPasswordFiedl.getText().toString().trim();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(createAccountActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });

        }


    }

    private void updateUI(FirebaseUser user){
        if(user!= null){
            Intent accountIntent = new Intent(createAccountActivity.this, MainActivity.class);
            startActivity(accountIntent);
        }else{
            Toast.makeText(createAccountActivity.this, "Error creating user.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}