package org.openlmis.core.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;


public class HomePageAdapter extends FragmentPagerAdapter{

    ArrayList<Fragment> fragments;
    String[] titles;

    public HomePageAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String[] titles){
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position < titles.length){
            return titles[position];
        }else {
            return titles[0];
        }
    }
}
