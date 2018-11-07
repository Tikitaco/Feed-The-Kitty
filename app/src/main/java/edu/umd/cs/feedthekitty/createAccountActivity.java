package edu.umd.cs.feedthekitty;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;


public class createAccountActivity extends AppCompatActivity {

    private EditText mEmailField;
    private EditText mPasswordFiedl;
    private EditText mPasswordFied2;
    private Button mLogin;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        //databaseReference replaces Firbase variable in the latest version

        mEmailField =  (EditText) findViewById(R.id.createEmail);
        mPasswordFiedl = (EditText) findViewById(R.id.createPassword);
        mPasswordFied2 = (EditText) findViewById(R.id.createPassword2);
        mLogin = (Button) findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
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
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
               if(task.isSuccessful()){

               }
            }
        });
    }
}

}