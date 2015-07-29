package org.openlmis.core.presenter;


import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;

public class StockCardListPresenter implements Presenter{

    @Inject
    StockRepository stockRepository;

    StockCardListView view;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) {
        view = (StockCardListView) v;
    }

    public interface StockCardListView extends View{

    }
}
