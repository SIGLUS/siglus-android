package org.openlmis.core.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.repository.ProductRepository;

import java.util.List;

public class InventoryPresenter implements Presenter{

    @Inject
    ProductRepository productRepository;

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

    public List<Product> loadMasterProductList() {
        List<Product> list = null;
        try {
            list = productRepository.loadProductList();
        }catch (LMISException e){
            e.printStackTrace();
        }

        return list;
    }

}
