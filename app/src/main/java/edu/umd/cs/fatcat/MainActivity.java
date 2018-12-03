package edu.umd.cs.fatcat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.umd.cs.fatcat.firebase.FatcatGlobals;
import edu.umd.cs.fatcat.firebase.FatcatListener;
import edu.umd.cs.fatcat.firebase.FirebaseUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordFied;
    private Button mCreateAccount;
    private Button mLogin;
    CallbackManager mCallbackManager;
    LoginButton fbLoginButton;
    private static final String TAG = "MainActivity";
    private static final String EMAIL = "email";
    public static FatcatGlobals globals = new FatcatGlobals();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mEmailField = (EditText) findViewById(R.id.email);
        mPasswordFied = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mCreateAccount = (Button) findViewById(R.id.createAccount);
        mCallbackManager = CallbackManager.Factory.create();

        if(mAuth.getCurrentUser() != null){
            //Intent accountIntent = new Intent(MainActivity.this, CreateAccountActivity.class);
            //startActivity(accountIntent);
        }

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountIntent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(accountIntent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_from_right);

            }
        });


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mEmailField.getText().toString()) || TextUtils.isEmpty(mPasswordFied.getText().toString())){
                    return;
                }else{
                    signInWithEmailAndPassword(mEmailField, mPasswordFied);

                }
            }
        });


        fbLoginButton = findViewById(R.id.fbLoginButton);
        fbLoginButton.setReadPermissions(Arrays.asList(EMAIL));
        fbLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Toast.makeText(MainActivity.this, "You're logged in", Toast.LENGTH_LONG).show();
            final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "Logging in...", "Loading. Please wait...", true);
            dialog.show();
            final boolean[] done = {false};
            FirebaseUtils.updateProfile(currentUser, new FatcatListener() { // Create a profile on the database if one doesn't exist already
                @Override
                public void onReturnData(Object data) {
                    globals.initializeGlobals(new FatcatListener() {
                        @Override
                        public void onReturnData(Object data) { // Once all the data is loaded, start the new activity
                            if (!done[0]) {
                                Log.i("Utils", "Finished initializing global variables");
                                Intent accountIntent = new Intent(MainActivity.this, HomepageActivity.class);
                                dialog.dismiss();
                                done[0] = true;
                                startActivity(accountIntent);
                                overridePendingTransition(R.anim.slide_in_right_fast,R.anim.slide_out_from_left);
                            }
                        }
                    });
                }
            });
        }

    }

    private void signInWithEmailAndPassword(EditText email, EditText password) {

        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MainAcitivity", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MainAcitivity", "signInWithEmail:failure", task.getException());

                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
                            alertDialog.setTitle("Invalid Username or Password");
                            alertDialog.setMessage("Would you like to send a password reset to you email?");

                            alertDialog.setButton(Dialog.BUTTON_POSITIVE,"Send", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    FirebaseAuth.getInstance().sendPasswordResetEmail(mEmailField.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                        Toast.makeText(MainActivity.this, "Email Sent",
                                                                Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Log.d(TAG, "Email not sent.");
                                                        Toast.makeText(MainActivity.this, "Invalid Email",
                                                                Toast.LENGTH_SHORT).show();


                                                    }
                                                }
                                            });
                                }
                            });
                            alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            alertDialog.show();

                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG ,"handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MAIN", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Main", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("exception",e.getMessage());
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}