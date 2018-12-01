package com.example.stephen.fatcat;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.Toast;

public class EventPageAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "My Events", "Invitations" };
    private Context context;
    private Fragment[] fragmentList = new Fragment[tabTitles.length];

    public EventPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public Fragment[] getAllFragments() {
        return fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            fragment = MyEventFragment.newInstance();
        } else if (position == 1) {
            fragment = MyInvitationsListFragmentFragment.newInstance();
        }
        fragmentList[position] = fragment;
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
