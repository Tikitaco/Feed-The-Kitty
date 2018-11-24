package com.example.stephen.fatcat;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import com.example.stephen.fatcat.com.example.stephen.fatcat.firebase.FatcatEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomepageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CaldendarFragment.OnFragmentInteractionListener,ListFragment.OnFragmentInteractionListener {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private ArrayList<FatcatEvent> events;
    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private CaldendarFragment calendarFragment;
    private ListFragment listFragment;
    private CalendarView mCalendarView;
    private List<EventDay> mEventDays = new ArrayList<EventDay>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        mMainFrame = findViewById(R.id.main_frame);
        mMainNav = findViewById(R.id.Bottom_Nav);

        listFragment = new ListFragment();
        calendarFragment = new CaldendarFragment();
        setFragment(listFragment);

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        //allows people to delete from the list view
                        return true;
                    case R.id.calendarView:
                        //opens calendar fragment
                        setFragment(calendarFragment);
                        return true;
                    case R.id.createEvent:
                        //opens create event would like to change this to a fragment if possible
                        startActivity(new Intent(HomepageActivity.this, CreateEventActivity.class));
                        break;
                }
                return true;
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view) ;

        mNavigationView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        events = new ArrayList<FatcatEvent>();
        loadEvents();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.nav_account:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_friends:
//                startActivity(new Intent(HomepageActivity.this, FriendsActivity.class));
//                finish();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomepageActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.nav_events:
                startActivity(new Intent(HomepageActivity.this, CreateEventActivity.class));
                break;

        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadEvent(FatcatEvent event) {
        Log.i("TAG", "Loaded event: " + event.getName());
        final int index = events.size(); // So the buttons know which event to reference
        events.add(event);
        RelativeLayout layout = findViewById(R.id.linear_view);
        Button b = new Button(this);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FatcatEvent evt = events.get(index);
                Toast.makeText(HomepageActivity.this, evt.getDescription(), Toast.LENGTH_SHORT).show();
            }
        });
        b.setText(event.getName() + " ( " + event.getDate() + " )");
        layout.addView(b);
    }

    public void loadEvents() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events");
        db.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FatcatEvent event = dataSnapshot.getValue(FatcatEvent.class);
                loadEvent(event);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment).addToBackStack(null);
        fragmentTransaction.commit();
    }
    public void onFragmentInteraction(Uri uri){

    }


}
