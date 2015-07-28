package org.openlmis.core.view.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;


import org.openlmis.core.R;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.HomePageAdapter;
import org.openlmis.core.view.fragment.RequisitionListFragment;
import org.openlmis.core.view.fragment.StockCardListFragment;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


@ContentView(R.layout.activity_main_page)
public class HomeActivity extends BaseActivity{


    @InjectView(R.id.viewPager)
    ViewPager viewPager;

    @InjectView(R.id.tabStrip)
    TabLayout tabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ActionBar actionBar = getSupportActionBar();
//        if(actionBar!=null){
//            actionBar.setCustomView(R.layout.design_navigation_item);
//        }

        initUI();
    }

    private void initUI(){
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new StockCardListFragment());
        fragments.add(new RequisitionListFragment());

        viewPager.setAdapter(new HomePageAdapter(getSupportFragmentManager(), fragments));
        tabStrip.setupWithViewPager(viewPager);
        tabStrip.setTabGravity(TabLayout.GRAVITY_FILL);
        tabStrip.setTabTextColors(getResources().getColor(R.color.secondary_text), getResources().getColor(R.color.white));
    }

    @Override
    public Presenter getPresenter() {
        return new Presenter() {
            @Override
            public void onStart() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void attachView(Activity v) {

            }

            @Override
            public void attachIncomingIntent(Intent intent) {

            }

            @Override
            public void initPresenter(Context context) {

            }
        };
    }
}
