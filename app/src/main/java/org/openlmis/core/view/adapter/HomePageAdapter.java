package org.openlmis.core.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;


public class HomePageAdapter extends FragmentPagerAdapter{

    ArrayList<Fragment> fragments;

    public HomePageAdapter(FragmentManager fm, ArrayList<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
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

        switch (position){
            case 0:
                return "Ficha de stock";
            case 1:
                return "Requisition";
            default:
                return "Ficha de stock";
        }
    }
}
