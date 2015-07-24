package org.openlmis.core.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.openlmis.core.model.Product;

import java.util.ArrayList;
import java.util.List;

public class InventoryPresenter implements Presenter{

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
        ArrayList<Product> list = new ArrayList<>();
        for (int i =0; i<10 ;i ++){
            Product product = new Product();
            product.setName("Paracetemol " + i);
            product.setUnit("500 ml" + i);

            list.add(product);
        }

        return list;
    }

}
