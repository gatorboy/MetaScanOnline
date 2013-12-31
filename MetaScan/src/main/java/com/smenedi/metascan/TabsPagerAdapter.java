package com.smenedi.metascan;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by smenedi on 12/18/13.
 */
public class TabsPagerAdapter extends FragmentPagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                // Settings fragment activity
                return new SettingsFragment();
            case 1:
                // Results fragment activity
                return ResultsFragment.getInstance();
            case 2:
                // Directory fragment activity
                return  DirectoryFragment.getInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
