package org.openlmis.core.presenter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;


public interface Presenter {

    void onStart();

    void onStop();

    void attachView(Activity v);

    void attachIncomingIntent(Intent intent);

    void initPresenter(Context context);
}
