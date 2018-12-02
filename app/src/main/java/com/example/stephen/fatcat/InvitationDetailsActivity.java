package com.example.stephen.fatcat;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.stephen.fatcat.R;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatFriend;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatInvitation;

public class InvitationDetailsActivity extends Activity {

    private TextView mEventName;
    private TextView mHost;
    private FatcatInvitation mInvitation;
    private RadioButton goingButton;
    private RadioButton declineButton;
    private RadioButton pendingButton;
    private FatcatFriend host;
    private TextView mStart;
    private TextView mEnd;
    private TextView mDescription;
    private TextView mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_details);
        String event_id = (String) getIntent().getExtras().get("event_id");
        for (FatcatInvitation inv : MainActivity.globals.myInvitations) {
            if (event_id.equals(inv.getEvent().getEventID())) {
                mInvitation = inv;
                break;
            }
        }

        if (mInvitation == null) {
            Toast.makeText(this, "Invalid Invitation", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            for (FatcatFriend friend : MainActivity.globals.friendProfiles) {
                if (friend.getUID().equals(mInvitation.getEvent().getOwnerUID())) {
                    host = friend;
                    break;
                }
            }
        }
        mEventName = (TextView) findViewById(R.id.invitation_event_name);
        mHost = (TextView) findViewById(R.id.invite_host);
        mStart = (TextView) findViewById(R.id.invite_start);
        mEnd = (TextView) findViewById(R.id.invite_end);
        mDescription = (TextView) findViewById(R.id.invite_description);
        goingButton = findViewById(R.id.radio_going);
        declineButton = findViewById(R.id.radio_decline);
        pendingButton = findViewById(R.id.radio_pending);

        mEventName.setText(mInvitation.getEvent().getName());
        mHost.setText("Hosted by " + host.getUsername());
        mStart.setText("Start Time: " + mInvitation.getEvent().getStartTime());
        mEnd.setText("End Time: " + mInvitation.getEvent().getEndTime());
        if (mInvitation.getEvent().getDescription().length() > 0) {
            mDescription.setText("Description: " + mInvitation.getEvent().getDescription());
        } else {
            mDescription.setText("");
        }
        int status = mInvitation.getStatus();

        if (status == FatcatInvitation.ACCEPTED) {
            goingButton.toggle();
            goingButton.setBackgroundColor(Color.GREEN);
        } else if (status == FatcatInvitation.DECLINED) {
            declineButton.toggle();
            declineButton.setBackgroundColor(Color.RED);
            declineButton.setTextColor(Color.WHITE);
        } else if (status == FatcatInvitation.PENDING) {
            pendingButton.toggle();
            pendingButton.setBackgroundColor(Color.YELLOW);
        }

        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pendingButton.isChecked()) {
                    pendingButton.setBackgroundColor(Color.YELLOW);
                    goingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.BLACK);

                }
            }
        });
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (declineButton.isChecked()) {
                    declineButton.setBackgroundColor(Color.RED);
                    pendingButton.setBackgroundColor(0xFAFAFA);
                    goingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.WHITE);
                }
            }
        });
        goingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (goingButton.isChecked()) {
                    goingButton.setBackgroundColor(Color.GREEN);
                    pendingButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setBackgroundColor(0xFAFAFA);
                    declineButton.setTextColor(Color.BLACK);

                }
            }
        });
    }
}
