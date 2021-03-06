package edu.umd.cs.fatcat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.applandeo.materialcalendarview.EventDay;
import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FatcatFriend;
import edu.umd.cs.fatcat.firebase.FatcatInvitation;
import edu.umd.cs.fatcat.firebase.FatcatListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.umd.cs.fatcat.firebase.FatcatEvent;
import edu.umd.cs.fatcat.firebase.FatcatFriend;
import edu.umd.cs.fatcat.firebase.FatcatInvitation;

public class HomepageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CaldendarFragment.OnFragmentInteractionListener, FriendListFragment.OnListFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener, EventsListFragment.OnFragmentInteractionListener,
        MyEventFragment.OnListFragmentInteractionListener, MyInvitationsListFragmentFragment.OnListFragmentInteractionListener,
        FundingSourcesFragment.OnListFragmentInteractionListener, PaymentsFragment.OnListFragmentInteractionListener {
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private ArrayList<FatcatEvent> events;
    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private CaldendarFragment calendarFragment;
    private FriendListFragment friendFragment;
    private SettingsFragment settingsFragment;
    private EventsListFragment eventsFragment;
    private PaymentsFragment paymentsFragment;
    private FundingSourcesFragment fundingSourcesFragment;
    private CalendarView mCalendarView;
    private List<EventDay> mEventDays = new ArrayList<EventDay>();
    boolean isCalendar = false;
    public final static int CREATE_ACTIVITY_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        mMainFrame = findViewById(R.id.main_frame);
        mMainNav = findViewById(R.id.Bottom_Nav);

        settingsFragment = new SettingsFragment();
        calendarFragment = new CaldendarFragment();
        friendFragment = new FriendListFragment();
        eventsFragment = new EventsListFragment();
        paymentsFragment = new PaymentsFragment();
        fundingSourcesFragment = new FundingSourcesFragment();
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view) ;
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.payments:
                        // Prompts user to create a payment account or shows existing payments
                        if (MainActivity.globals.myProfile.customerId == null) {
                            Intent i = new Intent(getApplicationContext(), PaymentSetupActivity.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.slide_in_bottom,R.anim.stay_still);
                        } else {
                            setFragment(paymentsFragment);
                        }
                        return true;
                    case R.id.calendarView:
                        //opens calendar fragment
                        if(isCalendar){
                            setFragmentCal(eventsFragment = new EventsListFragment());
                            item.setIcon(R.mipmap.baseline_calendar_today_white_36);
                            item.setTitle("Calendar View");
                            mNavigationView.getMenu().getItem(0).setIcon(R.mipmap.baseline_list_black_36);
                            mNavigationView.getMenu().getItem(0).setTitle("List View");
                            isCalendar = false;
                        }else{
                            setFragmentCal(calendarFragment);
                            item.setIcon(R.mipmap.baseline_list_black_36);
                            item.setTitle("List View");
                            mNavigationView.getMenu().getItem(0).setIcon(R.mipmap.baseline_calendar_today_white_36);
                            mNavigationView.getMenu().getItem(0).setTitle("Event Calendar");
                            isCalendar = true;
                        }
                        return true;
                    case R.id.createEvent:
                        //opens create event would like to change this to a fragment if possible
                        startActivityForResult(new Intent(HomepageActivity.this, CreateEventActivity.class), CREATE_ACTIVITY_REQUEST_CODE);
                        overridePendingTransition(R.anim.slide_in_bottom,R.anim.stay_still);
                        break;
                }
                return true;
            }
        });


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);


        mNavigationView.setNavigationItemSelectedListener(this);

        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        events = new ArrayList<FatcatEvent>();
        setStartingFragment(eventsFragment);

        // loadEvents();
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.nav_account:
                if (isCalendar) {
                    setFragment(calendarFragment);
                } else {
                    setFragment(eventsFragment = new EventsListFragment());
                }
                break;
            case R.id.nav_settings:
                setFragment(settingsFragment);
                break;
            case R.id.nav_friends:
                  setFragment(friendFragment);
//                startActivity(new Intent(HomepageActivity.this, FriendsActivity.class));
//                finish();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomepageActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_from_right);
                finish();
                break;
            case R.id.nav_events:

                startActivity(new Intent(HomepageActivity.this, CreateEventActivity.class));
                break;
            case R.id.nav_bank:
                if (MainActivity.globals.myProfile.customerId == null) {
                    Intent i = new Intent(getApplicationContext(), PaymentSetupActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_from_right);
                } else {
                    setFragment(fundingSourcesFragment);
                }
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

/*
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
    */

    public boolean onOptionsItemSelected(MenuItem item){
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setStartingFragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_frame,fragment);
        fragmentTransaction.commit();
    }
    private void setFragment(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame,fragment).addToBackStack(null);
        fragmentTransaction.commit();
    }
    private void setFragmentCal(android.support.v4.app.Fragment fragment) {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(isCalendar) {
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left_fast, R.anim.slide_out_from_right_fast);
        }else{
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right_fast, R.anim.slide_out_from_left_fast);
        }
        fragmentTransaction.replace(R.id.main_frame,fragment);

        fragmentTransaction.commit();
    }

    public void onFragmentInteraction(Uri uri){

    }


    @Override
    public void onListFragmentInteraction(FatcatFriend friend) {
        Toast.makeText(this, friend.getEmail(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == CREATE_ACTIVITY_REQUEST_CODE) {
            MainActivity.globals.getMyEvents(new FatcatListener<Vector<FatcatEvent>>() {
                @Override
                public void onReturnData(Vector<FatcatEvent> data) {
                    eventsFragment.updateLists();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onListFragmentInteraction(FatcatEvent item) {

    }

    @Override
    public void onListFragmentInteraction(FatcatInvitation item) {

    }

    @Override
    public void onListFragmentInteraction(FundingSourcesItem item) {

    }

    @Override
    public void onListFragmentInteraction(PaymentsItem item) {

    }
}
