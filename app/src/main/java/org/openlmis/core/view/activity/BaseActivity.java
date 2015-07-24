package org.openlmis.core.view.activity;

import android.os.Bundle;

import org.openlmis.core.presenter.Presenter;

import roboguice.activity.RoboActionBarActivity;

public abstract class BaseActivity extends RoboActionBarActivity {


    public abstract Presenter getPresenter();

    @Override
    protected void onStart() {
        super.onStart();
        getPresenter().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPresenter().onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPresenter();
    }

    protected void initPresenter(){
        getPresenter().attachView(BaseActivity.this);
        getPresenter().attachIncomingIntent(getIntent());
        getPresenter().initPresenter(BaseActivity.this);
    }
}
