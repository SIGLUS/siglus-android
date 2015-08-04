package org.openlmis.core.presenter;


import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.View;

import java.util.ArrayList;
import java.util.List;

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


    public List<StockCard> loadStockCards(){
        try {
            return  stockRepository.list();
        }catch (LMISException e){
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public void attachView(View v) {
        view = (StockCardListView) v;
    }

    public interface StockCardListView extends View{
    }
}
