package org.openlmis.core.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.openlmis.core.view.fragment.MalariaProgramFragment;
import org.openlmis.core.view.fragment.PTVProgramFragment;

public class PatientDataViewPagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    public PatientDataViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                MalariaProgramFragment tab1 = new MalariaProgramFragment();
                return tab1;
            case 1:
                PTVProgramFragment tab2 = new PTVProgramFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
