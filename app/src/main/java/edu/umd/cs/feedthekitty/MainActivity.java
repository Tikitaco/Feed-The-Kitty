package edu.umd.cs.feedthekitty;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class MainActivity extends AppCompatActivity {
    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //all the crap below is to test that the facebook login worked it will be removed
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        loginButton = (LoginButton) findViewById(R.id.fbLoginButton);
        textView = (TextView) findViewById(R.id.textView2);
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                textView.setText("Login Success \n" + loginResult.getAccessToken().getUserId() + "\n" + loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                textView.setText("Login Cancelled");
            }

            @Override
            public void onError(FacebookException error) {
                textView.setText("Login Error");
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
