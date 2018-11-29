package com.example.stephen.fatcat;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

public class EventPageAdapter extends FragmentPagerAdapter {
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
        Fragment fragment = MyEventFragment.newInstance();
        fragmentList[position] = fragment;
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
