package org.openlmis.core.view.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import org.openlmis.core.presenter.Presenter;

import roboguice.activity.RoboActionBarActivity;

public abstract class BaseActivity extends RoboActionBarActivity {


    public abstract Presenter getPresenter();
    ProgressDialog loadingDialog;

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


    public void startLoading() {
        if(loadingDialog == null){
            loadingDialog = new ProgressDialog(this);
        }

        loadingDialog.show();
    }

    public void stopLoading() {
        if(loadingDialog !=null){
            loadingDialog.dismiss();
        }
    }

    public void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
