package org.openlmis.core.presenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.model.repository.StockRepository;

public class StockCardListPresenter implements Presenter{

    @Inject
    StockRepository stockRepository;

    Activity activity;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(Activity v) {
        activity = v;
    }

    @Override
    public void attachIncomingIntent(Intent intent) {

    }

    @Override
    public void initPresenter(Context context) {

    }
}
