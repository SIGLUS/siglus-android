package org.openlmis.core.view.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;


import org.openlmis.core.R;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.View;
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
        initUI();
    }

    private void initUI() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new StockCardListFragment());
        fragments.add(new RequisitionListFragment());

        String[] titles = new String[]{ getResources().getString(R.string.stock_card_list),
                                        getResources().getString(R.string.requisition_list)};

        viewPager.setAdapter(new HomePageAdapter(getSupportFragmentManager(), fragments, titles));
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
            public void attachView(View v) {

            }
        };
    }
}
