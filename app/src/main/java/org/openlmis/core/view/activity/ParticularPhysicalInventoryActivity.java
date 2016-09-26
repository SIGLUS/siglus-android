package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.presenter.ParticularPhysicalInventoryPresenter;
import org.openlmis.core.utils.ToastUtil;

import roboguice.RoboGuice;
import rx.Subscription;
import rx.functions.Action1;

public class ParticularPhysicalInventoryActivity extends PhysicalInventoryActivity {

    @Override
    public void initUI() {
        super.initUI();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void initPresenter() {
        presenter = RoboGuice.getInjector(this).getInstance(ParticularPhysicalInventoryPresenter.class);
    }

    @Override
    public void onBackPressed() {
        if (isSearchViewActivity()) {
            searchView.onActionViewCollapsed();
        }
    }

    @Override
    protected View.OnClickListener getSaveOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading();
                Subscription subscription = presenter.saveDraftInventoryObservable().subscribe(onSaveAction, errorAction);
                subscriptions.add(subscription);
            }
        };
    }

    private Action1<Object> onSaveAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            loaded();
            ToastUtil.show(LMISApp.getInstance().getResources().getString(R.string.msg_draft_saved));
        }
    };

    @Override
    protected void goToNextPage() {
        presenter.updateAvgMonthlyConsumption();
        SharedPreferenceMgr.getInstance().setHasLotInfo(true);
        startActivity(HomeActivity.getIntentToMe(this));
        this.finish();
    }

    public static Intent getIntentToMe(Context context) {
        return new Intent(context, ParticularPhysicalInventoryActivity.class);
    }
}
